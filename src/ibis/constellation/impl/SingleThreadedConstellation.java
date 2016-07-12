package ibis.constellation.impl;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.Activity;
import ibis.constellation.ActivityContext;
import ibis.constellation.Constellation;
import ibis.constellation.ConstellationProperties;
import ibis.constellation.Event;
import ibis.constellation.Executor;
import ibis.constellation.ExecutorContext;
import ibis.constellation.StealPool;
import ibis.constellation.StealStrategy;
import ibis.constellation.extra.ActivityLocationLookup;
import ibis.constellation.extra.CTimer;
import ibis.constellation.extra.CircularBuffer;
import ibis.constellation.extra.SmartSortedWorkQueue;
import ibis.constellation.extra.Stats;
import ibis.constellation.extra.WorkQueue;

public class SingleThreadedConstellation extends Thread {

    static final Logger logger = LoggerFactory
            .getLogger(SingleThreadedConstellation.class);

    private final boolean PROFILE_STEALS;

    private final MultiThreadedConstellation parent;

    private final ActivityLocationLookup exportedActivities = new ActivityLocationLookup();
    private final ActivityLocationLookup relocatedActivities = new ActivityLocationLookup();

    final ExecutorWrapper wrapper;

    // Fresh work that anyone may steal
    private final WorkQueue fresh;

    // Fresh work that can only be stolen by one of my peers
    private final WorkQueue restricted;

    // Work that is stolen from an external constellation. It may be run by me
    // or one of my peers.
    private final WorkQueue stolen;

    // Work that has a context that is not supported by our local executor.
    private final WorkQueue wrongContext;

    // Work that may not leave this machine, but has a context that is not
    // supported by our local executor.
    private final WorkQueue restrictedWrongContext;

    // Work that is relocated. Only our local executor may run it.
    private final CircularBuffer<ActivityRecord> relocated = new CircularBuffer<ActivityRecord>(
            1);

    // Hashmap allowing quick lookup of the activities in our 4 queues.
    private HashMap<ibis.constellation.ActivityIdentifier, ActivityRecord> lookup = new HashMap<ibis.constellation.ActivityIdentifier, ActivityRecord>();

    private final ConstellationIdentifier identifier;

    private PrintStream out;

    // private final Thread thread;

    private StealPool myPool;
    private StealPool stealPool;

    private int rank;

    private boolean active;

    private static class PendingRequests {

        final ArrayList<EventMessage> deliveredApplicationMessages = new ArrayList<EventMessage>();

        final HashMap<ConstellationIdentifier, StealRequest> stealRequests = new HashMap<ConstellationIdentifier, StealRequest>();

        @Override
        public String toString() {
            return "QUEUES: " + deliveredApplicationMessages.size() + " "
                    + stealRequests.size();
        }
    }

    private final int stealSize;
    private final int stealDelay;

    private long nextStealDeadline;

    private PendingRequests incoming = new PendingRequests();
    private PendingRequests processing = new PendingRequests();

    private ActivityIdentifierFactory aidFactory;
    private long startID = 0;
    private long blockSize = 1000000;

    private boolean done = false;

    private final Stats stats;
    private final CTimer stealTimer;

    private final boolean ignoreEmptyStealReplies;

    private volatile boolean havePendingRequests = false;

    private boolean seenDone = false;

    SingleThreadedConstellation(Executor executor, ConstellationProperties p) {
        this(null, executor, p);
    }

    public SingleThreadedConstellation(MultiThreadedConstellation parent,
            Executor executor, ConstellationProperties props) {

        PROFILE_STEALS = props.PROFILE_STEAL;

        // this.thread = this;
        this.parent = parent;

        if (parent != null) {
            identifier = parent.getConstellationIdentifierFactory(null)
                    .generateConstellationIdentifier();
            aidFactory = parent.getActivityIdentifierFactory(identifier);
        } else {
            // We're on our own
            identifier = new ConstellationIdentifier(0);
            aidFactory = getActivityIdentifierFactory(identifier);
        }

        stolen = new SmartSortedWorkQueue("ST(" + identifier + ")-stolen");
        restricted = new SmartSortedWorkQueue(
                "ST(" + identifier + ")-restricted");
        fresh = new SmartSortedWorkQueue("ST(" + identifier + ")-fresh");
        wrongContext = new SmartSortedWorkQueue("ST(" + identifier + ")-wrong");
        restrictedWrongContext = new SmartSortedWorkQueue(
                "ST(" + identifier + ")-restrictedwrong");

        super.setName(identifier().toString());

        String outfile = props.OUTPUT;

        if (outfile != null) {
            String filename = outfile + "." + identifier.getId();

            try {
                out = new PrintStream(new BufferedOutputStream(
                        new FileOutputStream(filename)));
            } catch (Throwable e) {
                logger.error("Failed to open output file " + filename);
                out = System.out;
            }

        } else {
            out = System.out;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Starting SingleThreadedConstellation: " + identifier);
        }

        stealDelay = props.STEAL_DELAY;

        if (logger.isInfoEnabled()) {
            logger.info("SingleThreaded: steal delay set to " + stealDelay
                    + " ms.");
        }

        stealSize = props.STEAL_SIZE;

        if (logger.isInfoEnabled()) {
            logger.info("SingleThreaded: steal size set to " + stealSize);
        }

        ignoreEmptyStealReplies = props.STEAL_IGNORE_EMPTY_REPLIES;

        if (logger.isInfoEnabled()) {
            logger.info("SingleThreaded: ignore empty steal replies set to "
                    + ignoreEmptyStealReplies);
        }

        if (parent != null) {
            parent.register(this);
            stats = parent.getStats();
        } else {
            stats = new Stats(identifier.toString());
        }

        stealTimer = stats.getTimer("java", identifier().toString(), "steal");

        wrapper = new ExecutorWrapper(this, executor, props, identifier);

        myPool = wrapper.belongsTo();
        stealPool = wrapper.stealsFrom();

    }

    void setRank(int rank) {
        this.rank = rank;
    }

    int getRank() {
        return rank;
    }

    StealPool belongsTo() {
        return myPool;
    }

    StealPool stealsFrom() {
        return stealPool;
    }

    ExecutorContext getContext() {
        return wrapper.getContext();
    }

    StealStrategy getLocalStealStrategy() {
        return wrapper.getLocalStealStrategy();
    }

    StealStrategy getConstellationStealStrategy() {
        return wrapper.getConstellationStealStrategy();
    }

    StealStrategy getRemoteStealStrategy() {
        return wrapper.getRemoteStealStrategy();
    }

    ConstellationIdentifier identifier() {
        return identifier;
    }

    ibis.constellation.ActivityIdentifier performSubmit(Activity a) {
        /*
         * This method is called by MultiThreadedConstellation, in case the user
         * calls submit() on a constellation instance. The
         * MultiThreadedConstellation then picks a specific
         * SingleThreadedConstellation, and the activity should be submitted to
         * its wrapper, because this executor may not be able to steal.
         */
        return wrapper.submit(a);
    }

    ibis.constellation.ActivityIdentifier doSubmit(ActivityRecord ar,
            ActivityContext c, ActivityIdentifier id) {

        Activity a = ar.activity;

        if (c.satisfiedBy(wrapper.getContext(), StealStrategy.ANY)) {

            synchronized (this) {
                lookup.put(a.identifier(), ar);

                if (a.isRestrictedToLocal()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Submit job to restricted, length was "
                                + restricted.size());
                    }
                    restricted.enqueue(ar);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Submit job to fresh, length was "
                                + fresh.size());
                    }
                    fresh.enqueue(ar);
                }
            }
        } else {
            deliverWrongContext(ar);
        }

        return id;
    }

    void performSend(Event e) {
        logger.error("INTERNAL ERROR: Send not implemented!");
    }

    void performCancel(ActivityIdentifier aid) {
        logger.error("INTERNAL ERROR: Cancel not implemented!");
    }

    boolean performActivate() {

        synchronized (this) {
            if (active) {
                return false;
            }

            active = true;
        }

        start();
        return true;
    }

    synchronized void performDone() {

        if (!active) {
            return;
        }

        done = true;
        havePendingRequests = true;
        notifyAll();
        while (!seenDone) {
            try {
                wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    private ActivityRecord[] trim(ActivityRecord[] a, int count) {
        ActivityRecord[] result = new ActivityRecord[count];
        System.arraycopy(a, 0, result, 0, count);
        return result;
    }

    ActivityRecord[] attemptSteal(ExecutorContext context, StealStrategy s,
            StealPool pool, ConstellationIdentifier source, int size,
            boolean local) {

        ActivityRecord[] result = new ActivityRecord[size];

        int count = attemptSteal(result, context, s, pool, source, size, local);

        if (count == 0) {
            return null;
        }

        return trim(result, count);
    }

    synchronized int attemptSteal(ActivityRecord[] tmp, ExecutorContext context,
            StealStrategy s, StealPool pool, ConstellationIdentifier src,
            int size, boolean local) {

        // attempted steal request from parent. Expects an immediate reply

        // sanity check
        if (src.equals(identifier)) {
            logger.error("INTERAL ERROR: attemp steal from self!",
                    new Throwable());
            return 0;
        }

        if (!pool.overlap(wrapper.belongsTo())) {
            logger.info("attemptSteal: wrong pool!");
            return 0;
        }

        // First steal from the activities that I cannot run myself.
        int offset = wrongContext.steal(context, s, tmp, 0, size);
        if (logger.isDebugEnabled() && !local) {
            logger.debug("Stole " + offset + " jobs from wrongContext of "
                    + identifier.getId() + ", size = " + wrongContext.size());
        }

        if (local) {

            // Only peers from our own constellation are allowed to steal
            // restricted or stolen jobs.
            if (offset < size) {
                offset += restrictedWrongContext.steal(context, s, tmp, offset,
                        size - offset);
            }

            if (offset < size) {
                offset += restricted.steal(context, s, tmp, offset,
                        size - offset);
            }

            if (offset < size) {
                offset += stolen.steal(context, s, tmp, offset, size - offset);
            }
        }

        // Anyone may steal a fresh job
        if (offset < size) {
            int n = fresh.steal(context, s, tmp, offset, size - offset);
            offset += n;
            if (logger.isDebugEnabled() && !local) {
                logger.debug("Stole " + n + " jobs from fresh, size = "
                        + fresh.size());
            }

        }

        if (offset == 0) {
            // steal failed, no activities stolen
            return 0;
        }

        // Success. Trim if necessary
        if (offset != size) {
            tmp = trim(tmp, offset);
        }

        // Next, remove activities from lookup, and mark and register them as
        // relocated or stolen/exported
        registerLeavingActivities(tmp, offset, src, local);

        return offset;
    }

    private synchronized void registerLeavingActivities(ActivityRecord[] ar,
            int len, ConstellationIdentifier dest, boolean isLocal) {

        for (int i = 0; i < len; i++) {
            if (ar[i] != null) {
                lookup.remove(ar[i].identifier());

                if (isLocal) {
                    ar[i].setRelocated(true);
                    relocatedActivities.add(ar[i].identifier(), dest);
                } else {
                    ar[i].setStolen(true);
                    exportedActivities.add(ar[i].identifier(), dest);
                }
            }
        }
    }

    void deliverStealRequest(StealRequest sr) {
        // steal request (possibly remote) to enqueue and handle later
        if (logger.isTraceEnabled()) {
            logger.trace("S REMOTE STEAL REQUEST from " + sr.source
                    + " context " + sr.context);
        }
        postStealRequest(sr);
    }

    private synchronized boolean pushWorkToExecutor(StealStrategy s) {

        // Push all relocated activities to our executor.
        if (relocated.size() > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found work on relocated list: " + relocated.size()
                        + " jobs");
            }
            while (relocated.size() > 0) {
                ActivityRecord ar = relocated.removeFirst();
                lookup.remove(ar.identifier());
                wrapper.addPrivateActivity(ar);
            }

            return true;
        }

        // Else: push one restricted activity to our executor
        if (restricted.size() > 0) {
            ActivityRecord ar = restricted.steal(wrapper.getContext(), s);
            if (ar != null) {
                lookup.remove(ar.identifier());
                wrapper.addPrivateActivity(ar);
                return true;
            }
        }

        // Else: push one stolen activity to our executor
        if (stolen.size() > 0) {
            ActivityRecord ar = stolen.steal(wrapper.getContext(), s);
            if (ar != null) {
                lookup.remove(ar.identifier());
                wrapper.addPrivateActivity(ar);
                return true;
            }
        }

        // Else: push one fresh activity to our executor
        if (fresh.size() > 0) {
            ActivityRecord ar = fresh.steal(wrapper.getContext(), s);
            if (ar != null) {
                lookup.remove(ar.identifier());
                wrapper.addPrivateActivity(ar);
                return true;
            }
        }

        return false;
    }

    void deliverStealReply(StealReply sr) {

        if (sr.isEmpty()) {
            // ignore empty replies
            return;
        }

        // If we get a non-empty steal reply, we simply enqueue it locally.
        ActivityRecord[] tmp = sr.getWork();

        synchronized (this) {

            for (int i = 0; i < tmp.length; i++) {
                ActivityRecord a = tmp[i];

                if (a != null) {
                    // two options here: either the job is stolen (from a remote
                    // constellation) or
                    // relocated (from a peer in our local constellation).
                    // Stolen jobs may be
                    // relocated later, but relocated jobs must be executed by
                    // this executor.

                    // Timo: Add it to lookup as well!
                    lookup.put(a.identifier(), a);
                    if (a.isRelocated()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Putting " + a.identifier().toString()
                                    + " on relocated list of "
                                    + this.identifier().toString());
                        }
                        relocated.insertLast(a);
                    } else {
                        stolen.enqueue(a);
                    }
                    signal();
                }
            }
        }
    }

    synchronized ConstellationIdentifier deliverEventMessage(EventMessage m) {
        // A message from above. The target must be local (in one of my queues,
        // or in the queues of the executor) or its new location must be known
        // locally.
        //
        // POSSIBLE RACE CONDITIONS:
        //
        // 1) When the message overtakes a steal request, it may arrive before
        // the activity has arrived. As a result, the activity cannot be found
        // here yet.
        //
        // 2) The activity may be registered in relocated or exported but
        // a) it may not have arrived at the destination yet
        // b) it may about to be reclaimed because the target could not be
        // reached
        //

        // The target activity may be in one of my local queues

        Event e = m.event;

        ActivityRecord tmp = lookup.get(e.target);

        if (tmp != null) {
            tmp.enqueue(e);
            return null;
        }

        // If not, it may have been relocated
        ConstellationIdentifier cid = relocatedActivities.lookup(e.target);

        if (cid != null) {
            return cid;
        }

        // If not, it may have been stolen
        cid = exportedActivities.lookup(e.target);

        if (cid != null) {
            return cid;
        }

        // If not, is should be in the queue of my executor
        postEventMessage(m);
        return null;
    }

    ActivityIdentifierFactory getActivityIdentifierFactory(
            ConstellationIdentifier cid) {

        if (parent == null) {
            synchronized (this) {
                ActivityIdentifierFactory tmp = new ActivityIdentifierFactory(
                        cid, startID, startID + blockSize);
                startID += blockSize;
                return tmp;
            }
        }

        return parent.getActivityIdentifierFactory(cid);
    }

    boolean isMaster() {
        return parent == null;
    }

    void handleEvent(Event e) {
        // An event pushed up by our executor. We know the
        // executor itself does not contain the target activity

        ConstellationIdentifier cid = null;

        synchronized (this) {

            // See if the activity is in one of our queues
            ActivityRecord tmp = lookup.get(e.target);

            if (tmp != null) {
                // It is, so enqueue it and return.
                tmp.enqueue(e);
                return;
            }

            // See if we have exported it somewhere
            cid = exportedActivities.lookup(e.target);

            if (cid == null) {
                // If not, it may have been relocated
                cid = relocatedActivities.lookup(e.target);
            }

            if (cid == null) {
                // If not, we simply send the event to the parent
                cid = ((ActivityIdentifier) e.target).getOrigin();
            }
        }

        if (cid.equals(identifier)) {
            // the target is local, which means we have lost a local activity
            logger.error("Activity " + e.target
                    + " does no longer exist! (event dropped)");
            return;
        }

        parent.handleEventMessage(new EventMessage(identifier, cid, e));
    }

    private synchronized final void signal() {
        havePendingRequests = true;
        notifyAll();
    }

    private void postStealRequest(StealRequest s) {

        // sanity check
        if (s.source.equals(identifier)) {
            logger.error("INTERAL ERROR: posted steal request from self!",
                    new Throwable());
            return;
        }

        synchronized (incoming) {

            if (logger.isTraceEnabled()) {
                StealRequest tmp = incoming.stealRequests.get(s.source);

                if (tmp != null) {
                    logger.trace("Steal request overtaken: " + s.source);
                }
            }

            incoming.stealRequests.put(s.source, s);
        }
        signal();
    }

    private void postEventMessage(EventMessage m) {
        synchronized (incoming) {
            incoming.deliveredApplicationMessages.add(m);
        }
        signal();
    }

    private synchronized ActivityIdentifier createActivityID(
            boolean expectsEvents) {

        try {
            return aidFactory.createActivityID(expectsEvents);
        } catch (Exception e) {
            // Oops, we ran out of IDs. Get some more from our parent!
            if (parent != null) {
                aidFactory = parent.getActivityIdentifierFactory(identifier);
            } else {
                aidFactory = getActivityIdentifierFactory(identifier);
            }
        }

        try {
            return aidFactory.createActivityID(expectsEvents);
        } catch (Exception e) {
            throw new RuntimeException(
                    "INTERNAL ERROR: failed to create new ID block!", e);
        }
    }

    private synchronized boolean getDone() {
        if (done) {
            seenDone = true;
            notifyAll();
            return true;
        }
        return false;
    }

    private void swapEventQueues() {

        if (logger.isTraceEnabled()) {
            logger.trace("Processing events while idle!\n" + incoming.toString()
                    + "\n" + processing.toString());
        }

        synchronized (incoming) {
            PendingRequests tmp = incoming;
            incoming = processing;
            processing = tmp;
            // NOTE: havePendingRequests needs to be set here to prevent a gap
            // between doing the swap + setting it to false. Another submit
            // could potentially use this gap to insert a new event. This would
            // lead to a race condition!
            havePendingRequests = false;
        }
    }

    private void processRemoteMessages() {

        if (processing.deliveredApplicationMessages.size() > 0) {

            for (int i = 0; i < processing.deliveredApplicationMessages
                    .size(); i++) {

                EventMessage m = processing.deliveredApplicationMessages.get(i);

                if (!wrapper.queueEvent(m.event)) {
                    // Failed to deliver event locally. Check if the activity is
                    // now in one of the local queues. If not, return to parent.
                    if (logger.isInfoEnabled()) {
                        logger.info("Failed to deliver message from " + m.source
                                + " / " + m.event.source + " to " + m.target
                                + " / " + m.event.target + " (resending)");
                    }

                    handleEvent(m.event);
                }
            }

            processing.deliveredApplicationMessages.clear();
        }
    }

    void reclaim(ActivityRecord[] a) {

        if (a == null) {
            return;
        }

        for (int i = 0; i < a.length; i++) {

            ActivityRecord ar = a[i];

            if (ar != null) {

                ActivityContext c = ar.getContext();

                if (ar.isRelocated()) {
                    // We should unset the relocation flag if an activity is
                    // returned.
                    ar.setRelocated(false);
                    relocated.remove(ar);
                } else if (ar.isStolen()) {
                    // We should unset the stolen flag if an activity is
                    // returned.
                    ar.setStolen(false);
                    exportedActivities.remove(ar.identifier());
                }

                if (c.satisfiedBy(wrapper.getContext(), StealStrategy.ANY)) {

                    synchronized (this) {
                        lookup.put(ar.identifier(), ar);

                        if (ar.isRestrictedToLocal()) {
                            restricted.enqueue(ar);
                        } else if (ar.isStolen()) {
                            stolen.enqueue(ar);
                        } else {
                            fresh.enqueue(ar);
                        }
                    }
                } else {
                    deliverWrongContext(ar);
                }
            }
        }
    }

    private void processStealRequests() {

        StealRequest[] requests = processing.stealRequests.values()
                .toArray(new StealRequest[0]);
        processing.stealRequests.clear();

        for (StealRequest s : requests) {

            // Make sure the steal request is still valid!
            if (!s.getStale()) {

                ActivityRecord[] a = null;

                synchronized (this) {
                    // We grab the lock here to prevent other threads (from
                    // above) from doing a
                    // lookup in the relocated/exported tables while we are
                    // removing activities
                    // from the executor's queue.

                    StealStrategy tmp = s.isLocal() ? s.constellationStrategy
                            : s.remoteStrategy;

                    // NOTE: a is allowed to be null
                    a = wrapper.steal(s.context, tmp, s.isLocal(), s.size,
                            s.source);

                    if (a != null) {
                        // We have a result. Register the leaving activities.
                        registerLeavingActivities(a, a.length, s.source,
                                s.isLocal());
                    }
                }

                if (a != null) {
                    if (!parent.handleStealReply(this, new StealReply(
                            wrapper.id(), s.source, s.pool, s.context, a))) {
                        reclaim(a);
                    }
                } else if (!ignoreEmptyStealReplies) {
                    // No result, but we send a reply anyway.
                    parent.handleStealReply(this, new StealReply(wrapper.id(),
                            s.source, s.pool, s.context, a));
                } else {
                    // No result, and we're not supposed to tell anyone
                    if (logger.isDebugEnabled()) {
                        logger.debug("IGNORING empty steal reply");
                    }
                }

            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("DROPPING STALE STEAL REQUEST");
                }
            }
        }
    }

    private void processEvents() {

        swapEventQueues();

        processRemoteMessages();
        processStealRequests();
    }

    private boolean pauseUntil(long deadline) {

        long pauseTime = deadline - System.currentTimeMillis();

        if (pauseTime > 0) {

            synchronized (this) {
                boolean wake = havePendingRequests;

                while (!wake) {

                    try {
                        wait(pauseTime);
                    } catch (Throwable e) {
                        // ignored
                    }

                    wake = havePendingRequests;

                    if (!wake) {
                        pauseTime = deadline - System.currentTimeMillis();
                        wake = (pauseTime <= 0);
                    }
                }
            }
        }

        return havePendingRequests;
    }

    private long stealAllowed() {

        if (stealDelay > 0) {

            long now = System.currentTimeMillis();

            if (logger.isDebugEnabled()) {
                logger.debug("nextStealDeadline - now = "
                        + (nextStealDeadline - now));
            }
            if (now >= nextStealDeadline) {
                nextStealDeadline = now + stealDelay;
                return 0;
            }

            return nextStealDeadline;
        } else {
            return 0;
        }
    }

    private void resetStealDeadline() {
        logger.debug("Resetting steal deadline");
        nextStealDeadline = 0;
    }

    synchronized void deliverWrongContext(ActivityRecord a) {
        // Timo: we should add it to the lookup as well
        lookup.put(a.identifier(), a);

        if (a.isRestrictedToLocal()) {
            restrictedWrongContext.enqueue(a);
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Added job to restrictedWrongContext queue; length = "
                                + restrictedWrongContext.size());
            }
        } else {
            wrongContext.enqueue(a);
            if (logger.isDebugEnabled()) {
                logger.debug("Added job to wrongContext queue; length = "
                        + wrongContext.size());
            }
        }
    }

    boolean processActivities() {

        if (havePendingRequests) {
            processEvents();
        }

        // NOTE: one problem here is that we cannot tell if we did any work
        // or not. We would like to know, since this allows us to reset
        // several variables (e.g., sleepIndex)

        boolean more = wrapper.process();

        while (more && !havePendingRequests) {
            more = wrapper.process();
        }

        if (stealsFrom() == StealPool.NONE) {
            synchronized (this) {
                while (!havePendingRequests) {
                    try {
                        wait();
                    } catch (Throwable e) {
                        // ignore
                    }
                }
            }
            return getDone();
        }

        int evnt = 0;
        if (PROFILE_STEALS) {
            evnt = stealTimer.start();
        }

        while (!more && !havePendingRequests) {
            // Our executor has run out of work. See if we can find some.

            // Check if there is any matching work in one of the local queues...
            more = pushWorkToExecutor(wrapper.getLocalStealStrategy());

            // If no work was found we send a steal request to our parent.
            if (!more) {

                long nextDeadline = stealAllowed();

                if (nextDeadline == 0) {

                    if (logger.isTraceEnabled()) {
                        logger.trace("GENERATING STEAL REQUEST at " + identifier
                                + " with context " + getContext());
                    }

                    ActivityRecord[] result = parent.handleStealRequest(this,
                            stealSize);

                    if (result != null) {

                        for (int i = 0; i < result.length; i++) {
                            if (result[i] != null) {
                                wrapper.addPrivateActivity(result[i]);
                                more = true;
                            }
                        }

                        if (more) {
                            // ignore steal deadline when we are successful!
                            resetStealDeadline();
                        }
                    }
                } else {
                    more = pauseUntil(nextDeadline);
                }
            }
        }

        if (PROFILE_STEALS) {
            stealTimer.stop(evnt);
        }

        return getDone();
    }

    @Override
    public void run() {

        long start = System.currentTimeMillis();

        wrapper.runExecutor();

        printStatistics(System.currentTimeMillis() - start);
    }

    public void printStatistics(long totalTime) {

        final long messagesInternal = wrapper.getMessagesInternal();
        final long messagesExternal = wrapper.getMessagesExternal();
        final double messagesTime = wrapper.getMessagesTimer().totalTimeVal()
                / 1000.0;

        final long activitiesSubmitted = wrapper.getActivitiesSubmitted();
        final long activitiesAdded = wrapper.getActivitiesAdded();

        final long wrongContextSubmitted = wrapper.getWrongContextSubmitted();
        final long wrongContextAdded = wrapper.getWrongContextAdded();
        final long wrongContextDiscovered = wrapper.getWrongContextDiscovered();

        final long steals = wrapper.getSteals();
        final long stealSuccessIn = wrapper.getStealSuccess();
        final long stolen = wrapper.getStolen();

        double idleTime = stealTimer.totalTimeVal() / 1000.0;

        final double idlePerc = (100.0 * idleTime) / totalTime;
        final double messPerc = (100.0 * messagesTime) / totalTime;

        final double initializeTime = wrapper.getInitializeTimer()
                .totalTimeVal() / 1000.0;
        final int activitiesInvoked = wrapper.getInitializeTimer().nrTimes();
        final double processTime = wrapper.getProcessTimer().totalTimeVal()
                / 1000.0;
        final double cleanupTime = wrapper.getCleanupTimer().totalTimeVal()
                / 1000.0;

        final double fact = ((double) activitiesInvoked)
                / (activitiesSubmitted + activitiesAdded);
        final double initializePerc = (100.0 * initializeTime) / totalTime;
        final double processPerc = (100.0 * processTime) / totalTime;
        final double cleanupPerc = (100.0 * cleanupTime) / totalTime;

        synchronized (out) {

            out.println(identifier + " statistics");
            out.println(" Time");
            out.println("   total        : " + totalTime + " ms.");
            if (wrapper.PROFILE) {
                out.println("   initialize   : " + initializeTime + " ms. ("
                        + initializePerc + " %)");
                out.println("     process    : " + processTime + " ms. ("
                        + processPerc + " %)");
                out.println("   cleanup      : " + cleanupTime + " ms. ("
                        + cleanupPerc + " %)");
                out.println("   message time : " + messagesTime + " ms. ("
                        + messPerc + " %)");
            }

            if (PROFILE_STEALS) {
                out.println("   idle count   : " + stealTimer.nrTimes());
                out.println("   idle time    : " + idleTime + " ms. ("
                        + idlePerc + " %)");
            }

            out.println(" Activities");
            out.println("   submitted    : " + activitiesSubmitted);
            out.println("   added        : " + activitiesAdded);
            if (wrapper.PROFILE) {
                out.println("   invoked      : " + activitiesInvoked + " ("
                        + fact + " /act)");
            }
            out.println("  Wrong Context");
            out.println("   submitted    : " + wrongContextSubmitted);
            out.println("   added        : " + wrongContextAdded);
            out.println("   discovered   : " + wrongContextDiscovered);
            out.println(" Messages");
            out.println("   internal     : " + messagesInternal);
            out.println("   external     : " + messagesExternal);
            out.println(" Steals");
            out.println("   incoming     : " + steals);
            out.println("   success      : " + stealSuccessIn);
            out.println("   stolen       : " + stolen);
        }

        out.flush();
    }

    public CTimer getTimer(String standardDevice, String standardThread,
            String standardAction) {
        return stats.getTimer(standardDevice, standardThread, standardAction);
    }

    public Constellation getConstellation() {
        return wrapper;
    }

    Stats getStats() {
        return stats;
    }

}
