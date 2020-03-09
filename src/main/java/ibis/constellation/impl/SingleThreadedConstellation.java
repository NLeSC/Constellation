/*
 * Copyright 2019 Vrije Universiteit Amsterdam
 *                Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ibis.constellation.impl;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.AbstractContext;
import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationCreationException;
import ibis.constellation.ConstellationProperties;
import ibis.constellation.Event;
import ibis.constellation.NoSuitableExecutorException;
import ibis.constellation.StealPool;
import ibis.constellation.StealStrategy;
import ibis.constellation.impl.util.CircularBuffer;
import ibis.constellation.impl.util.SimpleWorkQueue;
import ibis.constellation.impl.util.WorkQueue;
import nl.junglecomputing.timer.Profiling;
import nl.junglecomputing.timer.TimerImpl;

public class SingleThreadedConstellation extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(SingleThreadedConstellation.class);

    private final boolean PROFILE_STEALS;

    private final MultiThreadedConstellation parent;

    private final Map<ActivityIdentifierImpl, ConstellationIdentifierImpl> exportedActivities = new ConcurrentHashMap<ActivityIdentifierImpl, ConstellationIdentifierImpl>();
    private final Map<ActivityIdentifierImpl, ConstellationIdentifierImpl> relocatedActivities = new ConcurrentHashMap<ActivityIdentifierImpl, ConstellationIdentifierImpl>();

    private final ExecutorWrapper wrapper;

    public ExecutorWrapper getWrapper() {
        return wrapper;
    }

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
    private final CircularBuffer<ActivityRecord> relocated = new CircularBuffer<ActivityRecord>(1);

    // Hashmap allowing quick lookup of the activities in our 4 queues.
    private final HashMap<ActivityIdentifierImpl, ActivityRecord> lookup = new HashMap<ActivityIdentifierImpl, ActivityRecord>();

    private final ConstellationIdentifierImpl identifier;

    private PrintStream out;

    // private final Thread thread;

    private final StealPool myPool;
    private final StealPool stealPool;

    private int rank;

    private boolean active;

    private static class PendingRequests {

        private final ArrayList<EventMessage> deliveredApplicationMessages = new ArrayList<EventMessage>();

        private final HashMap<ConstellationIdentifierImpl, StealRequest> stealRequests = new HashMap<ConstellationIdentifierImpl, StealRequest>();

        @Override
        public String toString() {
            return "QUEUES: " + deliveredApplicationMessages.size() + " " + stealRequests.size();
        }
    }

    private final int stealSize;
    private final int stealDelay;

    private long nextStealDeadline;

    private PendingRequests incoming = new PendingRequests();
    private PendingRequests processing = new PendingRequests();

    private boolean done = false;

    private final Profiling profiling;
    private final TimerImpl stealTimer;

    private final boolean ignoreEmptyStealReplies;

    private volatile boolean havePendingRequests = false;

    private boolean seenDone = false;

    private final boolean PRINT_STATISTICS;

    private final boolean PROFILE;

    private long stolenJobs;

    private long stealSuccess;

    private long steals;

    private long remoteStolen;

    SingleThreadedConstellation(final ConstellationConfiguration executor, final ConstellationProperties p) throws ConstellationCreationException {
        this(null, executor, p);
    }

    public SingleThreadedConstellation(final MultiThreadedConstellation parent, final ConstellationConfiguration config, final ConstellationProperties props)
            throws ConstellationCreationException {

        if (config == null) {
            throw new IllegalArgumentException("SingleThreadedConstellation expects ConstellationConfiguration");
        }

        if (props == null) {
            throw new IllegalArgumentException("SingleThreadedConstellation expects ConstellationProperties");
        }

        PROFILE_STEALS = props.PROFILE_STEAL;
        PRINT_STATISTICS = props.STATISTICS;
        PROFILE = props.PROFILE;

        logger.info("PROFILE_STEALS = " + PROFILE_STEALS);

        logger.info("PROFILE_STEALS = " + PROFILE_STEALS);

        // this.thread = this;
        this.parent = parent;

        if (parent != null) {
            identifier = parent.getConstellationIdentifierFactory().generateConstellationIdentifier();
        } else {
            // We're on our own
            identifier = new ConstellationIdentifierImpl(0, 0);
        }

        stolen = new SimpleWorkQueue("ST(" + identifier + ")-stolen");
        restricted = new SimpleWorkQueue("ST(" + identifier + ")-restricted");
        fresh = new SimpleWorkQueue("ST(" + identifier + ")-fresh");
        wrongContext = new SimpleWorkQueue("ST(" + identifier + ")-wrong");
        restrictedWrongContext = new SimpleWorkQueue("ST(" + identifier + ")-restrictedwrong");

        super.setName(identifier().toString());

        final String outfile = props.STATISTICS_OUTPUT;

        if (outfile != null) {
            final String filename = outfile + "." + identifier.getNodeId() + "." + identifier.getLocalId();

            try {
                out = new PrintStream(new BufferedOutputStream(new FileOutputStream(filename)));
            } catch (final Throwable e) {
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
            logger.info("SingleThreaded: steal delay set to " + stealDelay + " ms.");
        }

        stealSize = props.STEAL_SIZE;

        if (logger.isInfoEnabled()) {
            logger.info("SingleThreaded: steal size set to " + stealSize);
        }

        ignoreEmptyStealReplies = props.STEAL_IGNORE_EMPTY_REPLIES;

        if (logger.isInfoEnabled()) {
            logger.info("SingleThreaded: ignore empty steal replies set to " + ignoreEmptyStealReplies);
        }

        if (parent != null) {
            profiling = parent.getProfiling();
        } else {
            profiling = new Profiling(identifier.toString());
        }

        stealTimer = profiling.getTimer("java", identifier().toString(), "steal");

        wrapper = new ExecutorWrapper(this, props, identifier, config);

        myPool = wrapper.belongsTo();
        stealPool = wrapper.stealsFrom();

    }

    public void setRank(final int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public StealPool belongsTo() {
        return myPool;
    }

    public StealPool stealsFrom() {
        return stealPool;
    }

    public AbstractContext getContext() {
        return wrapper.getContext();
    }

    public StealStrategy getLocalStealStrategy() {
        return wrapper.getLocalStealStrategy();
    }

    public StealStrategy getConstellationStealStrategy() {
        return wrapper.getConstellationStealStrategy();
    }

    public StealStrategy getRemoteStealStrategy() {
        return wrapper.getRemoteStealStrategy();
    }

    public ConstellationIdentifierImpl identifier() {
        return identifier;
    }

    public ActivityIdentifier performSubmit(final Activity activity) throws NoSuitableExecutorException {
        /*
         * This method is called by MultiThreadedConstellation, in case the user calls submit() on a constellation instance. The MultiThreadedConstellation then
         * picks a specific SingleThreadedConstellation, and the activity should be submitted to its wrapper, because this executor may not be able to steal.
         */
        return wrapper.submit(activity);
    }

    public ActivityIdentifierImpl doSubmit(final ActivityRecord ar, final AbstractContext c, final ActivityIdentifierImpl id) {

        if (ContextMatch.match(c, wrapper.getContext())) {

            synchronized (this) {
                lookup.put(ar.identifier(), ar);

                if (ar.isRestrictedToLocal()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Submit job to restricted, length was " + restricted.size());
                    }
                    restricted.enqueue(ar);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Submit job to fresh, length was " + fresh.size());
                    }
                    fresh.enqueue(ar);
                }
            }
        } else {
            deliverWrongContext(ar);
        }

        return id;
    }

    public void performSend(final Event e) {
        logger.error("INTERNAL ERROR: Send not implemented!");
    }

    public void performCancel(final ActivityIdentifier aid) {
        logger.error("INTERNAL ERROR: Cancel not implemented!");
    }

    public boolean performActivate() {

        synchronized (this) {
            if (active) {
                return false;
            }

            active = true;
        }

        start();
        return true;
    }

    public synchronized void performDone() {

        if (!active) {
            return;
        }

        done = true;
        havePendingRequests = true;
        notifyAll();
        if (parent == null) {
            return;
        }
        while (!seenDone) {
            try {
                wait();
            } catch (final InterruptedException e) {
                // ignore
            }
        }
    }

    private ActivityRecord[] trim(final ActivityRecord[] a, final int count) {
        if (a.length > count) {
            return Arrays.copyOf(a, count);
        }
        return a;
    }

    public ActivityRecord[] attemptSteal(final AbstractContext context, final StealStrategy s, final StealPool pool, final ConstellationIdentifierImpl source,
            final int size, final boolean local) {

        final ActivityRecord[] result = new ActivityRecord[size];

        final int count = attemptSteal(result, context, s, pool, source, size, local);

        if (count == 0) {
            return null;
        }

        return trim(result, count);
    }

    private int localSteal(final AbstractContext context, final StealStrategy s, final ActivityRecord[] result, final int o, final int size) {
        int offset = o;
        if (offset < size) {
            offset += restrictedWrongContext.steal(context, s, result, offset, size - offset);
        }

        if (offset < size) {
            offset += restricted.steal(context, s, result, offset, size - offset);
        }

        if (offset < size) {
            offset += stolen.steal(context, s, result, offset, size - offset);
        }

        return offset;
    }

    public synchronized int attemptSteal(final ActivityRecord[] tmp, final AbstractContext context, final StealStrategy s, final StealPool pool,
            final ConstellationIdentifierImpl src, final int size, final boolean local) {

        // attempted steal request from parent. Expects an immediate reply
        steals++;

        // sanity check
        if (src.equals(identifier)) {
            logger.error("INTERAL ERROR: attemp steal from self!", new Throwable());
            return 0;
        }

        if (!pool.overlap(wrapper.belongsTo())) {
            logger.info("attemptSteal: wrong pool!");
            return 0;
        }

        // First steal from the activities that I cannot run myself.
        final int fromWrong = wrongContext.steal(context, s, tmp, 0, size);
        int offset = fromWrong;

        if (local && offset < size) {
            // Only peers from our own constellation are allowed to steal
            // restricted or stolen jobs.
            offset = localSteal(context, s, tmp, offset, size);
        }

        // Anyone may steal a fresh job
        int fromFresh = 0;
        if (offset < size) {
            fromFresh = fresh.steal(context, s, tmp, offset, size - offset);
            offset += fromFresh;
        }

        if (offset == 0) {
            // steal failed, no activities stolen
            return 0;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Stole " + offset + " jobs from " + identifier + ": " + fromWrong + " from wrongContext, " + fromFresh + " from fresh");
        }

        // Next, remove activities from lookup, and mark and register them as
        // relocated or stolen/exported
        registerLeavingActivities(tmp, offset, src, local);

        stolenJobs += offset;
        stealSuccess++;

        return offset;
    }

    private synchronized void registerLeavingActivities(final ActivityRecord[] ar, final int len, final ConstellationIdentifierImpl dest,
            final boolean isLocal) {

        for (int i = 0; i < len; i++) {
            if (ar[i] != null) {
                lookup.remove(ar[i].identifier());

                if (isLocal) {
                    ar[i].setRelocated(true);
                    relocatedActivities.put(ar[i].identifier(), dest);
                } else {
                    ar[i].setStolen(true);
                    exportedActivities.put(ar[i].identifier(), dest);
                }
            }
        }
    }

    public void deliverStealRequest(final StealRequest sr) {
        // steal request (possibly remote) to enqueue and handle later
        if (logger.isTraceEnabled()) {
            logger.trace("S REMOTE STEAL REQUEST from " + sr.source + " context " + sr.context);
        }
        postStealRequest(sr);
    }

    private synchronized boolean pushWorkFromQueue(final WorkQueue queue, final StealStrategy s) {
        if (queue.size() > 0) {
            final ActivityRecord ar = queue.steal(wrapper.getContext(), s);
            if (ar != null) {
                lookup.remove(ar.identifier());
                wrapper.addPrivateActivity(ar);
                return true;
            }
        }
        return false;
    }

    private synchronized boolean pushRelocatedToExecutor() {
        // Push all relocated activities to our executor.
        if (relocated.size() > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found work on relocated list: " + relocated.size() + " jobs");
            }
            while (relocated.size() > 0) {
                final ActivityRecord ar = relocated.removeFirst();
                lookup.remove(ar.identifier());
                wrapper.addPrivateActivity(ar);
            }

            return true;
        }
        return false;
    }

    private synchronized boolean pushWorkToExecutor(final StealStrategy s) {

        if (pushRelocatedToExecutor()) {
            return true;
        }

        // Else: try to push one restricted activity to our executor
        if (pushWorkFromQueue(restricted, s)) {
            return true;
        }

        // Else: try to push one stolen activity to our executor
        if (pushWorkFromQueue(stolen, s)) {
            return true;
        }

        // Else: try to push one fresh activity to our executor
        return pushWorkFromQueue(fresh, s);
    }

    public void deliverStealReply(final StealReply sr) {

        if (sr.isEmpty()) {
            // ignore empty replies
            return;
        }

        // If we get a non-empty steal reply, we simply enqueue it locally.
        final ActivityRecord[] tmp = sr.getWork();

        remoteStolen += tmp.length;

        synchronized (this) {

            for (final ActivityRecord a : tmp) {
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
                            logger.debug("Putting " + a.identifier().toString() + " on relocated list of " + this.identifier().toString());
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

    public synchronized ConstellationIdentifierImpl deliverEventMessage(final EventMessage m) {
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
        // When the message can be delivered, null is returned. When not, the
        // constellation identifier where it should be sent instead is returned.

        final Event e = m.event;
        final ActivityIdentifierImpl target = (ActivityIdentifierImpl) e.getTarget();

        final ActivityRecord tmp = lookup.get(target);

        if (tmp != null) {
            // We found the destination activity and enqueue the event for it.
            tmp.enqueue(e);
            return null;
        }

        // If not, it may have been relocated
        ConstellationIdentifierImpl cid = relocatedActivities.get(target);

        if (cid != null) {
            return cid;
        }

        // If not, it may have been stolen
        cid = exportedActivities.get(target);

        if (cid != null) {
            return cid;
        }

        // If not, it should be in the queue of my executor
        postEventMessage(m);
        return null;
    }

    public boolean isMaster() {
        return parent == null;
    }

    public void handleEvent(final Event e) {
        // An event pushed up by our executor. We know the
        // executor itself does not contain the target activity

        ConstellationIdentifierImpl cid = null;

        final ActivityIdentifierImpl target = (ActivityIdentifierImpl) e.getTarget();

        synchronized (this) {

            // See if the activity is in one of our queues
            final ActivityRecord tmp = lookup.get(e.getTarget());

            if (tmp != null) {
                // It is, so enqueue it and return.
                tmp.enqueue(e);
                return;
            }

            // See if we have exported it somewhere
            cid = exportedActivities.get(target);

            if (cid == null) {
                // If not, it may have been relocated
                cid = relocatedActivities.get(target);
            }

            if (cid == null) {
                // If not, we simply send the event to the parent
                cid = target.getOrigin();
            }
        }

        if (cid.equals(identifier)) {
            // the target is local, which means we have lost a local activity
            logger.error("Activity " + e.getTarget() + " does no longer exist! (event dropped)");
            return;
        }

        parent.handleEventMessage(new EventMessage(identifier, cid, e));
    }

    public synchronized final void signal() {
        havePendingRequests = true;
        notifyAll();
    }

    private synchronized void postStealRequest(final StealRequest s) {

        // sanity check
        if (s.source.equals(identifier)) {
            logger.error("INTERAL ERROR: posted steal request from self!", new Throwable());
            return;
        }

        if (logger.isTraceEnabled()) {
            final StealRequest tmp = incoming.stealRequests.get(s.source);

            if (tmp != null) {
                logger.trace("Steal request overtaken: " + s.source);
            }
        }

        incoming.stealRequests.put(s.source, s);

        signal();
    }

    private synchronized void postEventMessage(final EventMessage m) {
        incoming.deliveredApplicationMessages.add(m);
        signal();
    }

    private synchronized boolean getDone() {
        if (done) {
            seenDone = true;
            notifyAll();
            return true;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("getDone returns false");
        }
        return false;
    }

    private synchronized void swapEventQueues() {

        if (logger.isTraceEnabled()) {
            logger.trace("Processing events while idle!\n" + incoming.toString() + "\n" + processing.toString());
        }

        final PendingRequests tmp = incoming;
        incoming = processing;
        processing = tmp;
        // NOTE: havePendingRequests needs to be set here to prevent a gap
        // between doing the swap + setting it to false. Another submit
        // could potentially use this gap to insert a new event. This would
        // lead to a race condition!
        // But it is probably better to only reset it if done is not set?
        havePendingRequests = done;
    }

    private void processRemoteMessages() {
        for (final EventMessage m : processing.deliveredApplicationMessages) {
            if (!wrapper.queueEvent(m.event)) {
                // Failed to deliver event locally. Check if the activity is
                // now in one of the local queues. If not, return to parent.
                if (logger.isInfoEnabled()) {
                    logger.info("Failed to deliver message from " + m.source + " / " + m.event.getSource() + " to " + m.target + " / " + m.event.getTarget()
                            + " (resending)");
                }

                handleEvent(m.event);
            }
        }
        processing.deliveredApplicationMessages.clear();
    }

    /**
     * Reclaim is used to re-insert activities into the queue whenever a steal reply failed to be sent.
     *
     * @param a
     *            the ActivityRecords to reclaim
     */
    public void reclaim(final ActivityRecord[] a) {

        if (a == null) {
            return;
        }

        for (final ActivityRecord ar : a) {

            if (ar != null) {

                final AbstractContext c = ar.getContext();

                if (ar.isRelocated()) {
                    // We should unset the relocation flag if an activity is returned.
                    ar.setRelocated(false);
                    relocated.remove(ar);
                } else if (ar.isStolen()) {
                    // We should unset the stolen flag if an activity is returned.
                    ar.setStolen(false);
                    exportedActivities.remove(ar.identifier());
                }

                if (ContextMatch.match(c, wrapper.getContext())) {

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

        final Collection<StealRequest> requests = processing.stealRequests.values();

        for (final StealRequest s : requests) {

            ActivityRecord[] a = null;

            synchronized (this) {

                // We grab the lock here to prevent other threads (from above) from doing a lookup in the
                // relocated/exported tables while we are removing activities from the executor's queue.

                final StealStrategy tmp = s.isLocal() ? s.constellationStrategy : s.remoteStrategy;

                // NOTE: a is allowed to be null
                a = wrapper.steal(s.context, tmp, s.isLocal(), s.size, s.source);

                if (a != null) {
                    // We have a result. Register the leaving activities.
                    registerLeavingActivities(a, a.length, s.source, s.isLocal());
                }
            }

            if (a != null) {
                if (!parent.handleStealReply(this, new StealReply(wrapper.identifier(), s.source, s.pool, s.context, a))) {
                    reclaim(a);
                }
            } else if (!ignoreEmptyStealReplies) {
                // No result, but we send a reply anyway.
                parent.handleStealReply(this, new StealReply(wrapper.identifier(), s.source, s.pool, s.context, a));
            } else {
                // No result, and we're not supposed to tell anyone
                if (logger.isDebugEnabled()) {
                    logger.debug("IGNORING empty steal reply");
                }
            }
        }
        processing.stealRequests.clear();
    }

    private void processEvents() {
        swapEventQueues();
        processRemoteMessages();
        processStealRequests();
    }

    private synchronized boolean pauseUntil(final long deadline) {

        long pauseTime = deadline - System.currentTimeMillis();

        while (pauseTime > 0 && !havePendingRequests) {
            try {
                wait(pauseTime);
            } catch (final Throwable e) {
                // ignored
            }
            if (!havePendingRequests) {
                pauseTime = deadline - System.currentTimeMillis();
            } else {
                pauseTime = 0;
            }
        }

        return havePendingRequests;
    }

    private long stealAllowed() {

        if (stealDelay > 0) {

            final long now = System.currentTimeMillis();

            if (logger.isDebugEnabled()) {
                logger.debug("nextStealDeadline - now = " + (nextStealDeadline - now));
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

    public synchronized void deliverWrongContext(final ActivityRecord a) {
        // Timo: we should add it to the lookup as well
        lookup.put(a.identifier(), a);

        if (a.isRestrictedToLocal()) {
            restrictedWrongContext.enqueue(a);
            if (logger.isDebugEnabled()) {
                logger.debug("Added job to restrictedWrongContext queue; length = " + restrictedWrongContext.size());
            }
        } else {
            wrongContext.enqueue(a);
            if (logger.isDebugEnabled()) {
                logger.debug("Added job to wrongContext queue; length = " + wrongContext.size());
            }
        }
    }

    private synchronized void waitForRequest() {
        while (!havePendingRequests) {
            try {
                wait();
            } catch (final Throwable e) {
                // ignore
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Woke up in processActivities");
            }
        }
    }

    // An Activity.processActivities call ultimately ends up here.
    // We should make progress on each call, either by processing requests, or by doing work.
    // Either that, or we should sleep for a while.
    public boolean processActivities() {
        boolean haveRequests = false;
        synchronized (this) {
            if (havePendingRequests) {
                if (getDone()) {
                    return true;
                }
                haveRequests = true;
            }
        }
        if (haveRequests) {
            processEvents();
        }

        if (wrapper.process() || pushWorkToExecutor(wrapper.getLocalStealStrategy())) {
            // Either we processed an activity, or we pushed one to the wrapper.
            return false;
        }

        if (parent == null || stealsFrom() == StealPool.NONE) {
            // Cannot steal, either because there is no-one to steal from, or because of the NONE stealpool.
            waitForRequest();
            return getDone();
        }

        final long nextDeadline = stealAllowed();

        if (nextDeadline == 0) {
            stealFromParent();
        } else {
            pauseUntil(nextDeadline);
        }

        return false;
    }

    private void stealFromParent() {

        int evnt = 0;
        if (PROFILE_STEALS) {
            evnt = stealTimer.start();
        }
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("GENERATING STEAL REQUEST at " + identifier + " with context " + getContext());
            }
            final ActivityRecord[] result = parent.handleStealRequest(this, stealSize);

            if (result != null) {
                boolean more = false;
                for (final ActivityRecord element : result) {
                    if (element != null) {
                        wrapper.addPrivateActivity(element);
                        more = true;
                    }
                }
                if (more) {
                    // ignore steal deadline when we are successful!
                    resetStealDeadline();
                }
            }

        } finally {
            if (PROFILE_STEALS) {
                stealTimer.stop(evnt);
            }
        }
    }

    @Override
    public void run() {

        final long start = System.currentTimeMillis();

        wrapper.runExecutor();

        if (PRINT_STATISTICS) {
            printStatistics(System.currentTimeMillis() - start);
        }
    }

    public void printStatistics(final long totalTime) {

        final long messagesInternal = wrapper.getMessagesInternal();
        final long messagesExternal = wrapper.getMessagesExternal();
        final double messagesTime = wrapper.getMessagesTimer().totalTimeVal() / 1000.0;

        final long activitiesSubmitted = wrapper.getActivitiesSubmitted();

        final long wrongContextSubmitted = wrapper.getWrongContextSubmitted();

        final long steals = wrapper.getSteals() + this.steals;
        final long stealSuccessIn = wrapper.getStealSuccess() + this.stealSuccess;
        final long stolen = wrapper.getStolen() + this.stolenJobs;

        final double idleTime = stealTimer.totalTimeVal() / 1000.0;

        final double idlePerc = (100.0 * idleTime) / totalTime;
        final double messPerc = (100.0 * messagesTime) / totalTime;

        final double initializeTime = wrapper.getInitializeTimer().totalTimeVal() / 1000.0;
        final int activitiesInvoked = wrapper.getInitializeTimer().nrTimes();
        final double processTime = wrapper.getProcessTimer().totalTimeVal() / 1000.0;
        final double cleanupTime = wrapper.getCleanupTimer().totalTimeVal() / 1000.0;

        final double fact = ((double) activitiesInvoked) / (activitiesSubmitted);
        final double initializePerc = (100.0 * initializeTime) / totalTime;
        final double processPerc = (100.0 * processTime) / totalTime;
        final double cleanupPerc = (100.0 * cleanupTime) / totalTime;

        synchronized (out) {

            out.println(identifier + " statistics");
            out.println(" Time");
            out.println("   total           : " + totalTime + " ms.");
            if (PROFILE) {
                out.println("   initialize      : " + initializeTime + " ms. (" + initializePerc + " %)");
                out.println("     process       : " + processTime + " ms. (" + processPerc + " %)");
                out.println("   cleanup         : " + cleanupTime + " ms. (" + cleanupPerc + " %)");
                out.println("   message time    : " + messagesTime + " ms. (" + messPerc + " %)");
            }

            if (PROFILE_STEALS) {
                out.println("   idle count      : " + stealTimer.nrTimes());
                out.println("   idle time       : " + idleTime + " ms. (" + idlePerc + " %)");
            }

            out.println(" Activities");
            out.println("   submitted       : " + activitiesSubmitted);
            if (PROFILE) {
                out.println("   invoked         : " + activitiesInvoked + " (" + fact + " /act)");
            }
            out.println("  Wrong Context");
            out.println("   submitted       : " + wrongContextSubmitted);
            out.println(" Messages");
            out.println("   internal        : " + messagesInternal);
            out.println("   external        : " + messagesExternal);
            out.println(" Steals");
            out.println("   incoming        : " + steals);
            out.println("   success         : " + stealSuccessIn);
            out.println("   stolenFromMe    : " + stolen);
            out.println("   stolenfromRemote: " + remoteStolen);
        }

        out.flush();
    }

    public TimerImpl getTimer(final String standardDevice, final String standardThread, final String standardAction) {
        return profiling.getTimer(standardDevice, standardThread, standardAction);
    }

    public Constellation getConstellation() {
        return wrapper;
    }

    public Profiling getProfiling() {
        return profiling;
    }

}
