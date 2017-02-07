package ibis.constellation.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.AbstractContext;
import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.ConstellationCreationException;
import ibis.constellation.ConstellationIdentifier;
import ibis.constellation.ConstellationProperties;
import ibis.constellation.Context;
import ibis.constellation.Event;
import ibis.constellation.OrContext;
import ibis.constellation.StealPool;
import ibis.constellation.impl.util.Stats;

public class MultiThreadedConstellation {

    private static final Logger logger = LoggerFactory.getLogger(MultiThreadedConstellation.class);

    private final DistributedConstellation parent;

    private ArrayList<SingleThreadedConstellation> incomingWorkers;

    private SingleThreadedConstellation[] workers;

    private boolean[][] poolMatrix;

    private int workerCount;

    private final ConstellationIdentifierImpl identifier;

    private final Random random = new Random();

    private boolean active = false;

    private AbstractContext myContext;

    private final ConstellationIdentifierFactory cidFactory;

    private final int localStealSize;

    private final Stats stats;

    private final Facade facade = new Facade();

    private class Facade implements Constellation {

        /* Following methods implement the Constellation interface */

        @Override
        public ActivityIdentifier submit(Activity activity) {
            return performSubmit(activity);
        }

        @Override
        public void send(Event e) {
            if (!((ActivityIdentifierImpl) e.getTarget()).expectsEvents()) {
                throw new IllegalArgumentException("Target activity " + e.getTarget() + "  does not expect an event!");
            }

            // An external application wishes to send an event to 'e.target'.
            performSend(e);
        }
        //
        // @Override
        // public void cancel(ActivityIdentifierImpl aid) {
        // // ignored!
        // }

        @Override
        public boolean activate() {
            return MultiThreadedConstellation.this.activate();
        }

        @Override
        public void done() {
            if (logger.isInfoEnabled()) {
                logger.info("Calling performDone");
            }
            MultiThreadedConstellation.this.done();
        }

        @Override
        public boolean isMaster() {
            return parent == null;
        }

        @Override
        public ConstellationIdentifier identifier() {
            return identifier;
        }

        @Override
        public TimerImpl getTimer(String standardDevice, String standardThread, String standardAction) {
            return stats.getTimer(standardDevice, standardThread, standardAction);
        }

        @Override
        public TimerImpl getTimer() {
            return stats.getTimer();
        }

        @Override
        public TimerImpl getOverallTimer() {
            return stats.getOverallTimer();
        }
    }

    public MultiThreadedConstellation(ConstellationProperties p) throws ConstellationCreationException {
        this(null, p);
    }

    public MultiThreadedConstellation(DistributedConstellation parent, ConstellationProperties properties)
            throws ConstellationCreationException {

        this.parent = parent;

        if (parent != null) {
            cidFactory = parent.getConstellationIdentifierFactory();
            identifier = parent.identifier();
        } else {
            cidFactory = new ConstellationIdentifierFactory(0);
            identifier = cidFactory.generateConstellationIdentifier();
        }

        PROFILE = properties.PROFILE;

        PROFILE = properties.PROFILE;

        incomingWorkers = new ArrayList<SingleThreadedConstellation>();

        localStealSize = properties.STEAL_SIZE;

        if (logger.isInfoEnabled()) {
            logger.info("MultiThreaded: steal size set to " + localStealSize);
            logger.info("Starting MultiThreadedConstellation " + identifier);
        }

        if (parent != null) {
            parent.register(this);
            stats = parent.getStats();
        } else {
            stats = new Stats(identifier.toString());
        }
    }

    public Stats getStats() {
        return stats;
    }

    private int next = 0;

    private boolean PROFILE;

    public synchronized ActivityIdentifier performSubmit(Activity activity) {

        for (int i = 0; i < workerCount; i++) {
            // Round robin submit (for testing)
            int index = next++;
            next = next % workerCount;
            SingleThreadedConstellation e = workers[index];

            if (ContextMatch.match(e.getContext(), activity.getContext())) {
                return e.performSubmit(activity);
            }
            if (e.belongsTo().isWorld()) {
                return e.performSubmit(activity);
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("No local executor for this activity: " + activity.identifier().toString());
        }

        // throw new Error("submit: no suitable executor found");

        int i = next++;
        next = next % workerCount;
        return workers[i].performSubmit(activity);

    }

    public void performSend(Event e) {

        // Since we don't known where the target activity is located, we simply
        // send the message to it's parent constellation (which may be local).
        handleEventMessage(new EventMessage(identifier, ((ActivityIdentifierImpl) e.getTarget()).getOrigin(), e));
    }

    public void performCancel(ActivityIdentifier aid) {
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

    private int selectRandomWorker() {
        // This return a random number between 0 .. workerCount-1
        return random.nextInt(workerCount);
    }

    // Delivers the specified message to the specified constellation.
    // This method returns null if either the destination constellation could
    // not be found (which is an error situation), or the message gets
    // delivered.
    // When the message cannot be delivered, the constellation identifier where
    // it should be sent instead is returned.
    private ConstellationIdentifierImpl deliverLocally(ConstellationIdentifierImpl cid, EventMessage m) {

        SingleThreadedConstellation st = getWorker(cid);

        if (st == null) {
            logger.error("TimerEvent target " + m.target + " cannot be found (event dropped)");
            return null;
        }

        return st.deliverEventMessage(m);
    }

    public void handleEventMessage(EventMessage m) {
        // One of our children wishes to send a message to 'm.target',
        // which may be local or remote.

        if (cidFactory.isLocal(m.target)) {

            ConstellationIdentifierImpl cid = deliverLocally(m.target, m);

            if (cid != null) {

                if (cid.equals(m.target)) {
                    logger.error("INTERNAL ERROR: loop in event routing! (dropping event)");
                    return;
                }

                // The activity has been relocated or stolen, so try again
                m.setTarget(cid);
                handleEventMessage(m);
            }
        } else {

            if (parent == null) {
                logger.error("TimerEvent target " + m.target + " cannot be found (event dropped)");
                return;
            }

            parent.handleApplicationMessage(m, true);
        }
    }

    public boolean handleStealReply(SingleThreadedConstellation src, StealReply m) {

        SingleThreadedConstellation b = getWorker(m.target);

        if (b != null) {
            b.deliverStealReply(m);
            return true;
        }

        if (parent != null) {
            return parent.handleStealReply(m);
        }

        logger.error("Received steal reply for unknown target " + m.target + " (reclaiming work and dropping reply)");
        return false;
    }

    public ActivityRecord[] handleStealRequest(final SingleThreadedConstellation c, final int stealSize) {
        // a steal request from below

        final AbstractContext context = c.getContext();
        final StealPool pool = c.stealsFrom();

        if (logger.isTraceEnabled()) {
            logger.trace("M STEAL REQUEST from child " + c.identifier() + " with context " + context + " to pool " + pool);
        }

        // First attempt to satisfy the request locally without waiting for
        // anyone
        final int rnd = selectRandomWorker();
        final int rank = c.getRank();

        final ActivityRecord[] result = new ActivityRecord[localStealSize];

        for (int i = 0; i < workerCount; i++) {

            final SingleThreadedConstellation tmp = workers[(rnd + i) % workerCount];

            if (tmp != c && poolMatrix[rank][tmp.getRank()]) {

                final int size = tmp.attemptSteal(result, context, c.getConstellationStealStrategy(), pool, c.identifier(),
                        localStealSize, true);

                if (size > 0) {
                    return result;
                }
            }
        }

        // If this fails, we do a remote steal followed by an enqueued steal at
        // a random suitable peer.
        final StealRequest sr = new StealRequest(c.identifier(), context, c.getLocalStealStrategy(),
                c.getConstellationStealStrategy(), c.getRemoteStealStrategy(), pool, stealSize);

        if (parent != null) {
            parent.handleStealRequest(sr);
        }

        for (int i = 0; i < workerCount; i++) {

            final SingleThreadedConstellation tmp = workers[(rnd + i) % workerCount];

            if (tmp != c && poolMatrix[rank][tmp.getRank()]) {
                tmp.deliverStealRequest(sr);
                return null;
            }
        }

        return null;
    }

    public ConstellationIdentifierFactory getConstellationIdentifierFactory() {
        return cidFactory;
    }

    public synchronized void register(SingleThreadedConstellation constellation) throws ConstellationCreationException {

        if (active) {
            throw new ConstellationCreationException(
                    "Cannot register new BottomConstellation while " + "TopConstellation is active!");
        }

        incomingWorkers.add(constellation);
    }

    public synchronized AbstractContext getContext() {
        return myContext;
    }

    private AbstractContext mergeContext() {

        // We should now combine all contexts of our workers into one
        HashSet<Context> map = new HashSet<>();

        for (int i = 0; i < workerCount; i++) {

            AbstractContext tmp = workers[i].getContext();

            if (tmp instanceof Context) {
                map.add((Context) tmp);
            } else {
                OrContext o = (OrContext) tmp;

                for (Context r : o) {
                    map.add(r);
                }
            }
        }

        assert (map.size() > 0);

        if (map.size() == 1) {
            return map.iterator().next();
        } else {
            return new OrContext(map.toArray(new Context[map.size()]));
        }
    }

    public ConstellationIdentifier identifier() {
        return identifier;
    }

    public boolean activate() {

        StealPool stealsFrom;
        StealPool belongsTo;

        synchronized (this) {
            if (active) {
                return false;
            }

            active = true;

            workerCount = incomingWorkers.size();
            workers = incomingWorkers.toArray(new SingleThreadedConstellation[workerCount]);
            // No workers may be added after this point
            incomingWorkers = null;

            StealPool[] workerStealsFrom = new StealPool[workerCount];
            StealPool[] workerBelongsTo = new StealPool[workerCount];

            poolMatrix = new boolean[workerCount][workerCount];

            for (int i = 0; i < workerCount; i++) {
                workers[i].setRank(i);
                workerBelongsTo[i] = workers[i].belongsTo();
                workerStealsFrom[i] = workers[i].stealsFrom();
            }

            for (int i = 0; i < workerCount; i++) {
                for (int j = 0; j < workerCount; j++) {
                    poolMatrix[i][j] = workerStealsFrom[i].overlap(workerBelongsTo[j]);
                }
            }

            belongsTo = StealPool.merge(workerBelongsTo);
            stealsFrom = StealPool.merge(workerStealsFrom);
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

        if (PROFILE) {
            if (logger.isInfoEnabled()) {
                logger.info("Printing statistics");
            }
            stats.printStats(System.out);
        }
    }

    public void deliverStealRequest(StealRequest sr) {
        // steal request delivered by our parent.

        if (logger.isDebugEnabled()) {
            logger.info("M REMOTE STEAL REQUEST from child " + sr.source + " context " + sr.context + " pool " + sr.pool);
        }

        final int rnd = selectRandomWorker();

        // First attempt to satisfy the request without bothering anyone
        for (int i = 0; i < workerCount; i++) {

            SingleThreadedConstellation tmp = workers[(rnd + i) % workerCount];

            StealPool p = tmp.belongsTo();

            if (sr.pool.overlap(p)) {
                // We're allowed to steal!

                if (logger.isDebugEnabled()) {
                    logger.debug("Found steal target: " + tmp.identifier() + ", pool = " + p);
                }
                ActivityRecord[] result = tmp.attemptSteal(sr.context, sr.remoteStrategy, sr.pool, sr.source, sr.size, false);

                if (result != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("... and got a job!");
                    }
                    // We've managed to find some work!
                    if (!parent.handleStealReply(new StealReply(identifier, sr.source, sr.pool, sr.context, result))) {
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

            if (sr.pool.overlap(p) && tmp.getWrapper().getJobLimit() > 0) {
                tmp.deliverStealRequest(sr);
                return;
            }
        }

        // No steal request was posted either. Apparently, we are not able to
        // fulfill this request in the first place! Let's send an empty
        // reply....
        parent.handleStealReply(new StealReply(identifier, sr.source, sr.pool, sr.context, (ActivityRecord) null));
    }

    public void deliverStealReply(StealReply sr) {
        // steal reply delivered by our parent

        if (logger.isDebugEnabled()) {
            logger.info("M receive STEAL reply from " + sr.source);
        }

        SingleThreadedConstellation b = getWorker(sr.target);

        if (b == null) {
            logger.error("Reveived steal reply for unknown target " + sr.target + " (selecting random target)");
            b = workers[selectRandomWorker()];
        }

        b.deliverStealReply(sr);
    }

    public void deliverEventMessage(EventMessage am) {
        // event delivered by our parent

        SingleThreadedConstellation st = getWorker(am.target);

        if (st == null) {
            logger.error("Failed to locate event target activity " + am.target + " for remote event (dropping event)");
            return;
        }

        ConstellationIdentifierImpl cid = st.deliverEventMessage(am);

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
                logger.error("Failed to locate event target activity " + am.target + " for remote event (dropping event)");
                return;
            }

            // NOTE: this should always return null!
            cid = st.deliverEventMessage(am);

        } else {
            // it has been exported
            parent.handleApplicationMessage(am, true);
        }
    }

    public Constellation getConstellation() {
        return facade;

    }
}
