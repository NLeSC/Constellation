package ibis.constellation.impl;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.ConstellationCreationException;
import ibis.constellation.ConstellationIdentifier;
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

    private static final Logger logger = LoggerFactory.getLogger(ExecutorWrapper.class);

    private final boolean PROFILE;
    private final boolean PROFILE_COMM;

    final int QUEUED_JOB_LIMIT;

    private final SingleThreadedConstellation parent;

    private final ConstellationIdentifierImpl identifier;

    private final Executor executor;

    private final ExecutorContext myContext;

    private final StealStrategy localStealStrategy;
    private final StealStrategy constellationStealStrategy;
    private final StealStrategy remoteStealStrategy;

    private HashMap<ActivityIdentifier, ActivityRecord> lookup = new HashMap<ActivityIdentifier, ActivityRecord>();

    private final WorkQueue restricted;
    private final WorkQueue fresh;

    private CircularBuffer<ActivityRecord> runnable = new CircularBuffer<ActivityRecord>(1);
    private CircularBuffer<ActivityRecord> relocated = new CircularBuffer<ActivityRecord>(1);

    private long activityCounter = 0;

    private final CTimer initializeTimer;
    private final CTimer cleanupTimer;
    private final CTimer processTimer;

    private long activitiesSubmitted;
    private long wrongContextSubmitted;

    private long steals;
    private long stealSuccess;
    private long stolenJobs;

    private long messagesInternal;
    private long messagesExternal;
    private final CTimer messagesTimer;

    private final ExecutorIdentifierImpl executorIdentifier;

    ExecutorWrapper(SingleThreadedConstellation parent, Executor executor, ConstellationProperties p,
            ConstellationIdentifierImpl identifier) throws ConstellationCreationException {
        this.parent = parent;
        this.identifier = identifier;
        this.executorIdentifier = new ExecutorIdentifierImpl(identifier.getNodeId(), identifier.getLocalId());
        this.executor = executor;

        QUEUED_JOB_LIMIT = p.QUEUED_JOB_LIMIT;

        PROFILE = p.PROFILE;
        PROFILE_COMM = p.PROFILE_COMMUNICATION;

        if (logger.isInfoEnabled()) {
            logger.info("Executor set job limit to " + QUEUED_JOB_LIMIT);
        }

        restricted = new SmartSortedWorkQueue("ExecutorWrapper(" + identifier + ")-restricted");
        fresh = new SmartSortedWorkQueue("ExecutorWrapper(" + identifier + ")-fresh");

        if (!executor.connect(this)) {
            throw new ConstellationCreationException("Executor is already embedded");
        }
        myContext = executor.getContext();

        localStealStrategy = executor.getLocalStealStrategy();
        constellationStealStrategy = executor.getConstellationStealStrategy();
        remoteStealStrategy = executor.getRemoteStealStrategy();
        messagesTimer = parent.getTimer("java", parent.identifier().toString(), "message sending");
        initializeTimer = parent.getTimer("java", parent.identifier().toString(), "initialize");
        cleanupTimer = parent.getTimer("java", parent.identifier().toString(), "cleanup");
        processTimer = parent.getTimer("java", parent.identifier().toString(), "process");

    }

    private void cancel(ActivityIdentifier activityIdentifier) {

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
        if (lookup.size() > 0) {
            logger.warn("Quiting Constellation with " + lookup.size() + " activities in queue");
        }
        parent.performDone();
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

    void addPrivateActivity(ActivityRecord a) {
        // add an activity that only I am allowed to run, either because
        // it is relocated, or because we have just obtained it and we don't
        // want anyone else to steal it from us.

        lookup.put(a.identifier(), a);
        relocated.insertLast(a);
    }

    private synchronized ActivityIdentifierImpl createActivityID(boolean events) {
        return ActivityIdentifierImpl.createActivityIdentifier(identifier, activityCounter++, events);
    }

    @Override
    public ActivityIdentifier submit(Activity a) {
        // Create an activity identifier and initialize the activity with it.
        ActivityBase base = a;
        ActivityIdentifierImpl id = createActivityID(base.expectsEvents());
        base.initialize(id);

        ActivityRecord ar = new ActivityRecord(a);
        ActivityContext c = a.getContext();

        activitiesSubmitted++;

        if (restricted.size() + fresh.size() >= QUEUED_JOB_LIMIT) {
            // If we have too much work on our hands we push it to our
            // parent. Added bonus is that others can access it without
            // interrupting me.
            return parent.doSubmit(ar, c, id);
        }

        if (c.satisfiedBy(myContext, StealStrategy.ANY)) {

            lookup.put(a.identifier(), ar);

            if (ar.isRestrictedToLocal()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Submit job to restricted of " + identifier + ", length was " + restricted.size());
                }
                restricted.enqueue(ar);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Submit job to fresh of " + identifier + ", length was " + fresh.size());
                }
                fresh.enqueue(ar);
            }
            // Expensive call, but otherwise parent may not see that there
            // is work to do ... this is really only needed when the submit
            // is called from the main program, not if it is called from the
            // activity. But testing for that may be expensive as well.
            parent.signal();

        } else {
            wrongContextSubmitted++;
            parent.deliverWrongContext(ar);
        }

        return id;
    }

    @Override
    public void send(Event e) {

        ActivityIdentifier target = e.getTarget();
        ActivityIdentifier source = e.getSource();
        int evt = 0;

        if (logger.isDebugEnabled()) {
            logger.debug("SEND EVENT " + source + " to " + target);
        }

        if (PROFILE_COMM) {
            evt = messagesTimer.start();
        }

        // First check if the activity is local.
        ActivityRecord ar;

        ar = lookup.get(target);
        if (ar != null) {
            messagesInternal++;
        } else {
            messagesExternal++;
        }

        if (ar != null) {
            ar.enqueue(e);

            boolean change = ar.setRunnable();

            if (change) {
                runnable.insertLast(ar);
            }

        } else {
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

        logger.error("ERROR: Cannot deliver event: Failed to find activity " + e.getTarget());

        return false;
    }

    protected ActivityRecord[] steal(ExecutorContext context, StealStrategy s, boolean allowRestricted, int count,
            ConstellationIdentifier source) {

        steals++;

        ActivityRecord[] result = new ActivityRecord[count];

        if (logger.isTraceEnabled()) {
            logger.trace("STEAL BASE(" + identifier + "): activities F: " + fresh.size() + " W: "
                    + /* wrongContext.size() + */" R: " + runnable.size() + " L: " + lookup.size());
        }

        int r = 0;

        if (allowRestricted) {
            r = restricted.steal(context, s, result, 0, count);
        }
        if (r < count) {
            r += fresh.steal(context, s, result, r, count - r);
        }

        if (r != 0) {
            for (int i = 0; i < r; i++) {
                if (result[i].isStolen()) {
                    // Sanity check, should not happen.
                    logger.warn("INTERNAL ERROR: return stolen job " + identifier);
                }

                lookup.remove(result[i].identifier());

                if (logger.isTraceEnabled()) {
                    logger.trace("STOLEN " + result[i].identifier());
                }
            }
            stolenJobs += r;
            stealSuccess++;
            return result;
        }
        return null;
    }

    private void process(ActivityRecord tmp) {
        int evt = 0;

        tmp.getActivity().setExecutor(executor);

        CTimer timer = tmp.isFinishing() ? cleanupTimer : tmp.isRunnable() ? processTimer : initializeTimer;

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
            if (logger.isDebugEnabled()) {
                logger.debug("Processing activity " + tmp.identifier());
            }
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

    long getWrongContextSubmitted() {
        return wrongContextSubmitted;
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
    public ConstellationIdentifierImpl identifier() {
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
        return parent.performActivate();
    }

    public boolean processActitivies() {
        return parent.processActivities();
    }

    void runExecutor() {

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
    public CTimer getTimer(String standardDevice, String standardThread, String standardAction) {
        return parent.getStats().getTimer(standardDevice, standardThread, standardAction);
    }

    @Override
    public CTimer getTimer() {
        return parent.getStats().getTimer();
    }

    @Override
    public CTimer getOverallTimer() {
        return parent.getStats().getOverallTimer();
    }

    public ExecutorIdentifierImpl executorIdentifier() {
        return executorIdentifier;
    }
}
