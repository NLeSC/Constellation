package ibis.constellation.impl;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.Activity;
import ibis.constellation.Concluder;
import ibis.constellation.Constellation;
import ibis.constellation.ConstellationProperties;
import ibis.constellation.Event;
import ibis.constellation.Executor;
import ibis.constellation.StealPool;
import ibis.constellation.StealStrategy;
import ibis.constellation.context.ActivityContext;
import ibis.constellation.context.ExecutorContext;
import ibis.constellation.extra.CTimer;
import ibis.constellation.extra.CircularBuffer;
import ibis.constellation.extra.SmartSortedWorkQueue;
import ibis.constellation.extra.WorkQueue;

public class ExecutorWrapper implements Constellation {

    static final Logger logger = LoggerFactory.getLogger(ExecutorWrapper.class);

    final boolean PROFILE;
    final boolean PROFILE_COMM;

    final int QUEUED_JOB_LIMIT;

    private final SingleThreadedConstellation parent;

    private final ConstellationIdentifier identifier;

    private final ExecutorBase executor;

    private final ExecutorContext myContext;

    private final StealStrategy localStealStrategy;
    private final StealStrategy constellationStealStrategy;
    private final StealStrategy remoteStealStrategy;

    private HashMap<ibis.constellation.ActivityIdentifier, ActivityRecord> lookup = new HashMap<ibis.constellation.ActivityIdentifier, ActivityRecord>();

    private final WorkQueue restricted;
    private final WorkQueue fresh;

    private CircularBuffer<ActivityRecord> runnable = new CircularBuffer<ActivityRecord>(
            1);
    private CircularBuffer<ActivityRecord> relocated = new CircularBuffer<ActivityRecord>(
            1);

    private ActivityIdentifierFactory generator;

    private final CTimer initializeTimer;
    private final CTimer cleanupTimer;
    private final CTimer processTimer;

    private long activitiesSubmitted;
    private long activitiesAdded;

    private long wrongContextSubmitted;
    private long wrongContextAdded;

    private long wrongContextDiscovered;

    private long steals;
    private long stealSuccess;
    private long stolenJobs;

    private long messagesInternal;
    private long messagesExternal;
    private final CTimer messagesTimer;

    ExecutorWrapper(SingleThreadedConstellation parent, ExecutorBase executor,
            ConstellationProperties p, ConstellationIdentifier identifier) {
        this.parent = parent;
        this.identifier = identifier;
        this.generator = parent.getActivityIdentifierFactory(identifier);
        this.executor = executor;

        QUEUED_JOB_LIMIT = p.QUEUED_JOB_LIMIT;

        PROFILE = p.PROFILE;
        PROFILE_COMM = p.PROFILE_COMMUNICATION;

        if (logger.isInfoEnabled()) {
            logger.info("Executor set job limit to " + QUEUED_JOB_LIMIT);
        }

        restricted = new SmartSortedWorkQueue(
                "ExecutorWrapper(" + identifier + ")-restricted");
        fresh = new SmartSortedWorkQueue(
                "ExecutorWrapper(" + identifier + ")-fresh");

        executor.connect(this);
        myContext = executor.getContext();

        localStealStrategy = executor.getLocalStealStrategy();
        constellationStealStrategy = executor.getConstellationStealStrategy();
        remoteStealStrategy = executor.getRemoteStealStrategy();
        messagesTimer = parent.getTimer("java", parent.identifier().toString(),
                "message sending");
        initializeTimer = parent.getTimer("java",
                parent.identifier().toString(), "initialize");
        cleanupTimer = parent.getTimer("java", parent.identifier().toString(),
                "cleanup");
        processTimer = parent.getTimer("java", parent.identifier().toString(),
                "process");

    }

    private void cancel(
            ibis.constellation.ActivityIdentifier activityIdentifier) {

        ActivityRecord ar = lookup.remove(activityIdentifier);

        if (ar == null) {
            return;
        }

        if (ar.needsToRun()) {
            runnable.remove(ar);
        }
    }

    @Override
    public void done() {
        done(null);
    }

    @Override
    public void done(Concluder concluder) {
        if (lookup.size() > 0) {
            logger.warn("Quiting Constellation with " + lookup.size()
                    + " activities in queue");
        }
    }

    private ActivityRecord dequeue() {

        // Try to dequeue an activity that we can run.

        // First see if any suspended activities have woken up.
        int size = runnable.size();

        if (size > 0) {
            return runnable.removeFirst();
        }

        // Next see if we have any relocated activities.
        size = relocated.size();

        if (size > 0) {
            return relocated.removeFirst();
        }

        // Next see if there are any activities that cannot
        // leave this constellation
        size = restricted.size();

        if (size > 0) {
            return restricted.steal(myContext, localStealStrategy);
        }

        // Finally, see if there are any fresh activities.
        size = fresh.size();

        if (size > 0) {
            return fresh.steal(myContext, localStealStrategy);
        }

        return null;
    }

    private ActivityIdentifierImpl createActivityID(boolean expectsEvents) {

        try {
            return generator.createActivityID(expectsEvents);
        } catch (Throwable e) {
            // Oops, we ran out of IDs. Get some more from our parent!
            if (parent != null) {
                generator = parent.getActivityIdentifierFactory(identifier);
            }
        }

        try {
            return generator.createActivityID(expectsEvents);
        } catch (Exception e) {
            throw new RuntimeException(
                    "INTERNAL ERROR: failed to create new ID block!", e);
        }

        // return new MTIdentifier(nextID++);
    }

    void addPrivateActivity(ActivityRecord a) {
        // add an activity that only I am allowed to run, either because
        // it is relocated, or because we have just obtained it and we don't
        // want anyone else to steal it from us.

        lookup.put(a.identifier(), a);
        relocated.insertLast(a);
    }

    protected ActivityRecord lookup(ActivityIdentifierImpl id) {
        return lookup.get(id);
    }

    @Override
    public ibis.constellation.ActivityIdentifier submit(Activity a) {

        activitiesSubmitted++;

        // Create an activity identifier and initialize the activity with it.
        ActivityIdentifierImpl id = createActivityID(a.expectsEvents());
        a.initialize(id);

        ActivityRecord ar = new ActivityRecord(a);
        ActivityContext c = a.getContext();

        if (restricted.size() + fresh.size() >= QUEUED_JOB_LIMIT) {
            // If we have too much work on our hands we push it to out parent.
            // Added bonus is that others can access it without interrupting me.
            return parent.doSubmit(ar, c, id);
        }

        if (c.satisfiedBy(myContext, StealStrategy.ANY)) {

            lookup.put(a.identifier(), ar);

            if (ar.isRestrictedToLocal()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Submit job to restricted, length was "
                            + restricted.size());
                }
                restricted.enqueue(ar);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Submit job to fresh, length was " + fresh.size());
                }
                fresh.enqueue(ar);
            }
        } else {
            wrongContextSubmitted++;
            // TODO: shouldn't we batch these calls.
            parent.deliverWrongContext(ar);
        }

        return id;
    }

    @Override
    public void send(Event e) {

        ActivityIdentifierImpl target = (ActivityIdentifierImpl) e.getTarget();
        ActivityIdentifierImpl source = (ActivityIdentifierImpl) e.getSource();
        int evt = 0;

        if (PROFILE_COMM) {
            evt = messagesTimer.start();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("SEND EVENT " + source + " to " + target);
        }

        // First check if the activity is local.
        ActivityRecord ar = lookup.get(target);

        if (ar != null) {

            messagesInternal++;

            ar.enqueue(e);

            boolean change = ar.setRunnable();

            if (change) {
                runnable.insertLast(ar);
            }

        } else {
            messagesExternal++;
            // ActivityBase is not local, so let our parent handle it.
            parent.handleEvent(e);
        }

        if (PROFILE_COMM) {
            messagesTimer.stop(evt);
        }
    }

    boolean queueEvent(Event e) {

        ActivityRecord ar = lookup.get(e.getTarget());

        if (ar != null) {

            ar.enqueue(e);

            boolean change = ar.setRunnable();

            if (change) {
                runnable.insertLast(ar);
            }

            return true;
        }

        logger.error("ERROR: Cannot deliver event: Failed to find activity "
                + e.getTarget());

        return false;
    }

    protected ActivityRecord[] steal(ExecutorContext context, StealStrategy s,
            boolean allowRestricted, int count,
            ConstellationIdentifier source) {

        // logger.warn("In STEAL on BASE " + context + " " + count);

        steals++;

        ActivityRecord[] result = new ActivityRecord[count];

        // FIXME: Optimize this!!!
        for (int i = 0; i < count; i++) {
            result[i] = doSteal(context, s, allowRestricted);

            if (result[i] == null) {

                if (i == 0) {
                    return null;
                } else {
                    stolenJobs += i;
                    stealSuccess++;
                    return result;
                }
            }
        }

        stolenJobs += count;
        stealSuccess++;
        return result;
    }

    private ActivityRecord doSteal(ExecutorContext context, StealStrategy s,
            boolean allowRestricted) {

        if (logger.isTraceEnabled()) {
            logger.trace("STEAL BASE(" + identifier + "): activities F: "
                    + fresh.size() + " W: " + /* wrongContext.size() + */" R: "
                    + runnable.size() + " L: " + lookup.size());
        }

        if (allowRestricted) {

            ActivityRecord r = restricted.steal(context, s);

            if (r != null) {

                // Sanity check -- should not happen!
                if (r.isStolen()) {
                    logger.warn(
                            "INTERNAL ERROR: return stolen job " + identifier);
                }

                lookup.remove(r.identifier());

                if (logger.isTraceEnabled()) {
                    logger.trace("STOLEN " + r.identifier());
                }

                return r;
            }

            // If restricted fails we try the regular queue
        }

        ActivityRecord r = fresh.steal(context, s);

        if (r != null) {

            // Sanity check -- should not happen!
            if (r.isStolen()) {
                logger.error("INTERNAL ERROR: return stolen job " + identifier);
            }

            lookup.remove(r.identifier());

            if (logger.isDebugEnabled()) {
                logger.debug("STOLEN " + r.identifier());
            }

            return r;
        }

        return null;
    }

    private void process(ActivityRecord tmp) {
        int evt = 0;

        tmp.getActivity().setExecutor((Executor) executor);

        CTimer timer = tmp.isFinishing() ? cleanupTimer
                : tmp.isRunnable() ? processTimer : initializeTimer;

        if (PROFILE) {
            evt = timer.start();
        }

        tmp.run();

        if (PROFILE) {
            timer.stop(evt);
        }

        if (tmp.needsToRun()) {
            runnable.insertFirst(tmp);
        } else if (tmp.isDone()) {
            cancel(tmp.identifier());
        }

    }

    boolean process() {

        ActivityRecord tmp = dequeue();

        // NOTE: the queue is guaranteed to only contain activities that we can
        // run. Whenever new activities are added or the context of
        // this constellation changes we filter out all activities that do not
        // match.

        if (tmp != null) {
            process(tmp);
            return true;
        }

        return false;
    }

    CTimer getInitializeTimer() {
        return initializeTimer;
    }

    CTimer getProcessTimer() {
        return processTimer;
    }

    CTimer getCleanupTimer() {
        return cleanupTimer;
    }

    long getActivitiesSubmitted() {
        return activitiesSubmitted;
    }

    long getActivitiesAdded() {
        return activitiesAdded;
    }

    long getWrongContextSubmitted() {
        return wrongContextSubmitted;
    }

    long getWrongContextAdded() {
        return wrongContextAdded;
    }

    long getWrongContextDiscovered() {
        return wrongContextDiscovered;
    }

    long getMessagesInternal() {
        return messagesInternal;
    }

    long getMessagesExternal() {
        return messagesExternal;
    }

    CTimer getMessagesTimer() {
        return messagesTimer;
    }

    long getSteals() {
        return steals;
    }

    long getStealSuccess() {
        return stealSuccess;
    }

    long getStolen() {
        return stolenJobs;
    }

    @Override
    public String identifier() {
        return identifier.toString();
    }

    ConstellationIdentifier id() {
        return identifier;
    }

    @Override
    public boolean isMaster() {
        return parent.isMaster();
    }

    public ExecutorContext getContext() {
        return myContext;
    }

    public StealStrategy getLocalStealStrategy() {
        return localStealStrategy;
    }

    public StealStrategy getConstellationStealStrategy() {
        return constellationStealStrategy;
    }

    public StealStrategy getRemoteStealStrategy() {
        return remoteStealStrategy;
    }

    @Override
    public boolean activate() {
        /*
         * if (parent != null) { return true; }
         *
         * while (process());
         */
        return false;
    }

    public boolean processActitivies() {
        return parent.processActivities();
    }

    public void runExecutor() {

        try {
            executor.run();
        } catch (Exception e) {
            logger.error("Executor terminated unexpectedly!", e);
        }
    }

    StealPool belongsTo() {
        return executor.belongsTo();
    }

    StealPool stealsFrom() {
        return executor.stealsFrom();
    }

    @Override
    public CTimer getTimer(String standardDevice, String standardThread,
            String standardAction) {
        return parent.getStats().getTimer(standardDevice, standardThread,
                standardAction);
    }

    @Override
    public CTimer getTimer() {
        return parent.getStats().getTimer();
    }

    @Override
    public CTimer getOverallTimer() {
        return parent.getStats().getOverallTimer();
    }
}
