package ibis.constellation.impl;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.ActivityIdentifierFactory;
import ibis.constellation.ConstellationIdentifier;
import ibis.constellation.Event;
import ibis.constellation.Stats;
import ibis.constellation.StealPool;
import ibis.constellation.WorkerContext;
import ibis.constellation.context.OrWorkerContext;
import ibis.constellation.context.UnitWorkerContext;
import ibis.constellation.extra.ConstellationIdentifierFactory;
import ibis.constellation.extra.Debug;
import ibis.constellation.extra.SimpleConstellationIdentifierFactory;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiThreadedConstellation {

    private static final Logger logger = LoggerFactory
            .getLogger(MultiThreadedConstellation.class);

    private final DistributedConstellation parent;

    private ArrayList<SingleThreadedConstellation> incomingWorkers;

    private SingleThreadedConstellation[] workers;

    private StealPool belongsTo;
    private StealPool stealsFrom;

    private boolean[][] poolMatrix;

    private int workerCount;

    private final ConstellationIdentifier identifier;

    private final Random random = new Random();

    private boolean active = false;

    private WorkerContext myContext;

    private final ConstellationIdentifierFactory cidFactory;

    private long startID = 0;
    private long blockSize = 1000000;

    private final int localStealSize;

    private final Stats stats;

    public MultiThreadedConstellation(Properties p) throws Exception {
        this(null, p);
    }

    public MultiThreadedConstellation(DistributedConstellation parent,
            Properties p) throws Exception {

        this.parent = parent;

        if (parent != null) {
            cidFactory = parent.getConstellationIdentifierFactory(null);
            identifier = parent.identifier();
        } else {
            cidFactory = new SimpleConstellationIdentifierFactory();
            identifier = cidFactory.generateConstellationIdentifier();
        }

        incomingWorkers = new ArrayList<SingleThreadedConstellation>();
        myContext = UnitWorkerContext.DEFAULT;

        String tmp = p.getProperty("ibis.constellation.stealsize.local");

        if (tmp != null && tmp.length() > 0) {
            localStealSize = Integer.parseInt(tmp);
        } else {
            localStealSize = 1;
        }

        if (logger.isInfoEnabled()) {
            logger.info("MultiThreaded: steal size set to " + localStealSize);
            logger.info("Starting MultiThreadedConstellation " + identifier);
        }

        parent.register(this);
        stats = parent.getStats();
    }

    public Stats getStats() {
        return stats;
    }

    int next = 0;

    synchronized ActivityIdentifier performSubmit(Activity a) {

        // Round robin submit (for testing)
        int index = next++;
        next = next % workerCount;
        return workers[index].performSubmit(a);
        // return workers[selectRandomWorker()].performSubmit(a);
    }

    void performSend(Event e) {

        // Since we don't known where the target activity is located, we simply
        // send the message to it's parent constellation (which may be local).
        handleEventMessage(
                new EventMessage(identifier, e.target.getOrigin(), e));
    }

    void performCancel(ActivityIdentifier aid) {
        logger.error("INTERNAL ERROR: cancel not implemented!");
    }

    private SingleThreadedConstellation getWorker(ConstellationIdentifier cid) {

        for (SingleThreadedConstellation b : workers) {
            if (cid.equals(b.identifier())) {
                return b;
            }
        }

        return null;
    }

    /*
     * private int selectTargetWorker(ConstellationIdentifier exclude) {
     * 
     * if (workerCount == 1) { return -1; }
     * 
     * int rnd = selectRandomWorker();
     * 
     * ConstellationIdentifier cid = workers[rnd].identifier();
     * 
     * if (cid.equals(exclude)) { return ((rnd+1)%workerCount); }
     * 
     * return rnd; }
     */
    private int selectRandomWorker() {
        // This return a random number between 0 .. workerCount-1
        return random.nextInt(workerCount);
    }

    PrintStream getOutput() {
        return System.out;
    }

    synchronized ActivityIdentifierFactory getActivityIdentifierFactory(
            ConstellationIdentifier cid) {

        ActivityIdentifierFactory tmp = new ActivityIdentifierFactory(cid.id,
                startID, startID + blockSize);

        startID += blockSize;
        return tmp;
    }

    /*
     * FIXME REMOVE!!
     * 
     * public ActivityIdentifier submit(Activity a) {
     * 
     * if (Debug.DEBUG_SUBMIT) { logger.info(
     * "LOCAL SUBMIT activity with context " + a.getContext()); }
     * 
     * ActivityIdentifier id = createActivityID(a.expectsEvents());
     * a.initialize(id);
     * 
     * // Add externally submitted activities to the lookup table.
     * exportedActivities.add(id, identifier);
     * 
     * if (a.isRestrictedToLocal()) { restrictedQueue.enqueue(new
     * ActivityRecord(a)); } else { queue.enqueue(new ActivityRecord(a)); }
     * 
     * if (Debug.DEBUG_SUBMIT) { logger.info("created " + id + " at " +
     * System.currentTimeMillis()); }
     * 
     * System.out.println("LOCAL ENQ: " + id + " " + a.getContext());
     * 
     * return id; }
     */

    private ConstellationIdentifier deliverLocally(ConstellationIdentifier cid,
            EventMessage m) {

        SingleThreadedConstellation st = getWorker(cid);

        if (st == null) {
            logger.error("TimerEvent target " + m.target
                    + " cannot be found (event dropped)");
            return null;
        }

        return st.deliverEventMessage(m);
    }

    void handleEventMessage(EventMessage m) {
        // One of our children wishes to send a message to 'm.target',
        // which may be local or remote.

        // System.out.println("MT: routing event to " + m.event.target + " at "
        // + m.target + " (" + cidFactory.isLocal(m.target) + ") from " +
        // m.source);

        if (cidFactory.isLocal(m.target)) {

            ConstellationIdentifier cid = deliverLocally(m.target, m);

            if (cid != null) {

                if (cid.equals(m.target)) {
                    logger.error(
                            "INTERNAL ERROR: loop in event routing! (dropping event)");
                    return;
                }

                // System.out.println("Rerouting event from " + m.target +
                // " to " + cid);

                // The activity has been relocated or stolen, so try again
                m.setTarget(cid);
                handleEventMessage(m);
            }

            // So try again.
            /*
             * cid = deliverLocally(cid, m);
             * 
             * if (cid != null) { logger.error("INTERNAL ERROR: activity " +
             * m.event.target +
             * " seems to have been relocated several times! (event dropped)");
             * } }
             */
        } else {

            if (parent == null) {
                logger.error("TimerEvent target " + m.target
                        + " cannot be found (event dropped)");
                return;
            }

            // System.out.println("Remote send of event to activity " +
            // m.event.target + " at " + m.target);

            parent.handleApplicationMessage(m, true);
        }
    }

    boolean handleStealReply(SingleThreadedConstellation src, StealReply m) {

        SingleThreadedConstellation b = getWorker(m.target);

        if (b != null) {
            b.deliverStealReply(m);
            return true;
        }

        if (parent != null) {
            return parent.handleStealReply(m);
        }

        logger.error("Received steal reply for unknown target " + m.target
                + " (reclaiming work and dropping reply)");
        return false;
    }

    ActivityRecord[] handleStealRequest(SingleThreadedConstellation c,
            int stealSize) {
        // a steal request from below

        final WorkerContext context = c.getContext();
        final StealPool pool = c.stealsFrom();

        if (Debug.DEBUG_STEAL && logger.isInfoEnabled()) {
            logger.info("M STEAL REQUEST from child " + c.identifier()
                    + " with context " + context + " to pool " + pool);
        }

        // First attempt to satisfy the request locally without waiting for
        // anyone
        final int rnd = selectRandomWorker();
        final int rank = c.getRank();

        ActivityRecord[] result = new ActivityRecord[localStealSize];

        for (int i = 0; i < workerCount; i++) {

            SingleThreadedConstellation tmp = workers[(rnd + i) % workerCount];

            if (tmp != c && poolMatrix[rank][tmp.getRank()]) {

                int size = tmp.attemptSteal(result, context,
                        c.getConstellationStealStrategy(), pool, c.identifier(),
                        localStealSize, true);

                if (size > 0) {
                    return result;
                }
            }
        }

        // If this fails, we do a remote steal followed by an enqueued steal at
        // a random suitable peer.
        StealRequest sr = new StealRequest(c.identifier(), context,
                c.getLocalStealStrategy(), c.getConstellationStealStrategy(),
                c.getRemoteStealStrategy(), pool, stealSize);

        if (parent != null) {
            parent.handleStealRequest(sr);
        }

        for (int i = 0; i < workerCount; i++) {

            SingleThreadedConstellation tmp = workers[(rnd + i) % workerCount];

            if (tmp != c && poolMatrix[rank][tmp.getRank()]) {
                tmp.deliverStealRequest(sr);
                return null;
            }
        }

        return null;
    }

    ConstellationIdentifierFactory getCohortIdentifierFactory(
            ConstellationIdentifier cid) {
        return parent.getConstellationIdentifierFactory(cid);
    }

    synchronized void register(SingleThreadedConstellation cohort)
            throws Exception {

        if (active) {
            throw new Exception("Cannot register new BottomCohort while "
                    + "TopCohort is active!");
        }

        incomingWorkers.add(cohort);
    }

    synchronized WorkerContext getContext() {
        return myContext;
    }

    // FIXME: does NOT merge steal strategies!
    private WorkerContext mergeContext() {

        // We should now combine all contexts of our workers into one
        HashMap<String, UnitWorkerContext> map = new HashMap<String, UnitWorkerContext>();

        for (int i = 0; i < workerCount; i++) {

            WorkerContext tmp = workers[i].getContext();

            if (tmp.isUnit()) {

                UnitWorkerContext u = (UnitWorkerContext) tmp;

                String name = u.name;

                if (!map.containsKey(name)) {
                    map.put(name, u);
                }
            } else if (tmp.isOr()) {
                OrWorkerContext o = (OrWorkerContext) tmp;

                for (int j = 0; j < o.size(); j++) {
                    UnitWorkerContext u = o.get(j);

                    if (u != null) {
                        String name = u.name;

                        if (!map.containsKey(name)) {
                            map.put(name, u);
                        }
                    }
                }
            }
        }

        if (map.size() == 0) {
            // should not happen ?
            return UnitWorkerContext.DEFAULT;
        } else if (map.size() == 1) {
            return map.values().iterator().next();
        } else {
            UnitWorkerContext[] contexts = map.values()
                    .toArray(new UnitWorkerContext[map.size()]);
            return new OrWorkerContext(contexts, false);
        }
    }

    public ConstellationIdentifier identifier() {
        return identifier;
    }

    public boolean activate() {

        synchronized (this) {
            if (active) {
                return false;
            }

            active = true;

            workerCount = incomingWorkers.size();
            workers = incomingWorkers
                    .toArray(new SingleThreadedConstellation[workerCount]);

            StealPool[] tmp = new StealPool[workerCount];

            poolMatrix = new boolean[workerCount][workerCount];

            for (int i = 0; i < workerCount; i++) {
                workers[i].setRank(i);
                tmp[i] = workers[i].belongsTo();
            }

            for (int i = 0; i < workerCount; i++) {
                for (int j = 0; j < workerCount; j++) {
                    // poolMatrix[i][j] = tmp[i].overlap(tmp[j]);
                    // Timo: I think you meant this:
                    poolMatrix[i][j] = workers[i].stealsFrom().overlap(tmp[j]);
                }
            }

            belongsTo = StealPool.merge(tmp);

            for (int i = 0; i < workerCount; i++) {
                tmp[i] = workers[i].stealsFrom();
            }

            stealsFrom = StealPool.merge(tmp);

            // No workers may be added after this point
            incomingWorkers = null;

            myContext = mergeContext();
        }

        if (parent != null) {
            parent.belongsTo(belongsTo);
            parent.stealsFrom(stealsFrom);
        }

        for (int i = 0; i < workerCount; i++) {
            if (logger.isInfoEnabled()) {
                logger.info("Activating worker " + i);
            }
            workers[i].performActivate();
        }

        return true;
    }

    public void done() {

        logger.info("done");

        if (active) {
            for (SingleThreadedConstellation u : workers) {
                u.performDone();
            }
        } else {
            for (SingleThreadedConstellation u : incomingWorkers) {
                u.performDone();
            }
        }
    }

    /*
     * 
     * ActivityIdentifier deliverSubmit(Activity a) {
     * 
     * 
     * if (PUSHDOWN_SUBMITS) {
     * 
     * if (Debug.DEBUG_SUBMIT) { logger.info(
     * "M PUSHDOWN SUBMIT activity with context " + a.getContext()); }
     * 
     * // We do a simple round-robin distribution of the jobs here. if
     * (nextSubmit >= workers.length) { nextSubmit = 0; }
     * 
     * if (Debug.DEBUG_SUBMIT) { logger.info("FORWARD SUBMIT to child " +
     * workers[nextSubmit].identifier()); }
     * 
     * return workers[nextSubmit++].deliverSubmit(a); }
     * 
     * if (Debug.DEBUG_SUBMIT) { logger.info(
     * "M LOCAL SUBMIT activity with context " + a.getContext()); }
     * 
     * ActivityIdentifier id = createActivityID(a.expectsEvents());
     * a.initialize(id);
     * 
     * if (Debug.DEBUG_SUBMIT) { logger.info("created " + id + " at " +
     * System.currentTimeMillis() + " from DIST"); }
     * 
     * return id; }
     */

    void deliverStealRequest(StealRequest sr) {
        // steal request delivered by our parent.

        if (logger.isDebugEnabled()) {
            logger.info("M REMOTE STEAL REQUEST from child " + sr.source
                    + " context " + sr.context + " pool " + sr.pool);
        }

        final int rnd = selectRandomWorker();

        // First attempt to satisfy the request without bothering anyone
        for (int i = 0; i < workerCount; i++) {

            SingleThreadedConstellation tmp = workers[(rnd + i) % workerCount];

            StealPool p = tmp.belongsTo();

            if (sr.pool.overlap(p)) {
                // We're allowed to steal!

                if (logger.isDebugEnabled()) {
                    logger.debug("Found steal target: " + tmp.identifier()
                            + ", pool = " + p);
                }
                ActivityRecord[] result = tmp.attemptSteal(sr.context,
                        sr.remoteStrategy, sr.pool, sr.source, sr.size, false);

                if (result != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("... and got a job!");
                    }
                    // We've managed to find some work!
                    if (!parent.handleStealReply(new StealReply(identifier,
                            sr.source, sr.pool, sr.context, result))) {
                        tmp.reclaim(result);
                    }

                    return;
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("... but no jobs available!");
                    }
                }
            }
        }

        // No job was found. Let's just post a request at a random location.
        for (int i = 0; i < workerCount; i++) {

            SingleThreadedConstellation tmp = workers[(rnd + i) % workerCount];

            StealPool p = tmp.belongsTo();

            if (sr.pool.overlap(p) && tmp.wrapper.QUEUED_JOB_LIMIT > 0) {
                tmp.deliverStealRequest(sr);
                return;
            }
        }

        // No steal request was posted either. Apparently, we are not able to
        // fulfill this request in the first place! Let's send an empty
        // reply....
        parent.handleStealReply(new StealReply(identifier, sr.source, sr.pool,
                sr.context, (ActivityRecord) null));
    }

    void deliverStealReply(StealReply sr) {
        // steal reply delivered by our parent

        if (logger.isDebugEnabled()) {
            logger.info("M receive STEAL reply from " + sr.source);
        }

        SingleThreadedConstellation b = getWorker(sr.target);

        if (b == null) {
            logger.error("Reveived steal reply for unknown target " + sr.target
                    + " (selecting random target)");
            b = workers[selectRandomWorker()];
        }

        b.deliverStealReply(sr);
    }

    void deliverEventMessage(EventMessage am) {
        // event delivered by our parent

        SingleThreadedConstellation st = getWorker(am.target);

        if (st == null) {
            logger.error("Failed to locate event target activity " + am.target
                    + " for remote event (dropping event)");
            return;
        }

        ConstellationIdentifier cid = st.deliverEventMessage(am);

        if (cid == null) {
            // Message was delivered -- we're done!
            return;
        }

        // The activity is no longer at the expected location.
        am.setTarget(cid);

        if (cidFactory.isLocal(cid)) {
            // It has been relocated
            st = getWorker(cid);

            if (st == null) {
                logger.error("Failed to locate event target activity "
                        + am.target + " for remote event (dropping event)");
                return;
            }

            // System.out.println("Forwarding message to new location
            // (relocated): "
            // + cid);

            // NOTE: this should always return null!
            cid = st.deliverEventMessage(am);

            // Sanity check
            if (st == null) {
                logger.error("INTERNAL ERROR: target activity "
                        + am.event.target + " has moved more that once!");
            }
        } else {
            // it has been exported
            // System.out.println("Forwarding message to new location
            // (exported): "
            // + cid);
            parent.handleApplicationMessage(am, true);
        }
    }
}
