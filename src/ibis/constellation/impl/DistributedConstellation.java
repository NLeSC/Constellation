package ibis.constellation.impl;

import java.io.PrintStream;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Concluder;
import ibis.constellation.Constellation;
import ibis.constellation.ConstellationCreationException;
import ibis.constellation.ConstellationProperties;
import ibis.constellation.Event;
import ibis.constellation.ExecutorContext;
import ibis.constellation.StealPool;
import ibis.constellation.context.OrExecutorContext;
import ibis.constellation.context.UnitExecutorContext;
import ibis.constellation.extra.CTimer;
import ibis.constellation.extra.ConstellationIdentifierFactory;
import ibis.constellation.extra.Stats;
import ibis.constellation.impl.pool.Pool;
import ibis.constellation.impl.pool.PoolCreationFailedException;

public class DistributedConstellation {

    private static final Logger logger = LoggerFactory
            .getLogger(DistributedConstellation.class);

    private static final int STEAL_POOL = 1;
    private static final int STEAL_MASTER = 2;
    private static final int STEAL_NONE = 3;

    private final boolean REMOTE_STEAL_THROTTLE;

    // FIXME setting this too low at startup causes load imbalance!
    // machines keep hammering the master for work, and (after a while)
    // get a flood of replies.
    private final long REMOTE_STEAL_TIMEOUT;

    private boolean active;

    private MultiThreadedConstellation subConstellation;

    private final ConstellationIdentifier identifier;

    private final Pool pool;

    private final DistributedConstellationIdentifierFactory cidFactory;

    private final DeliveryThread delivery;

    private ExecutorContext myContext;

    private long stealReplyDeadLine;

    private final int stealing;

    private final long start;

    private final Facade facade = new Facade();

    private class PendingSteal {

        final String pool;

        final HashMap<String, Long> deadlines = new HashMap<String, Long>();

        PendingSteal(String pool) {
            this.pool = pool;
        }

        @Override
        public String toString() {
            return "PendingSteal: pool = " + pool + ", deadlines for "
                    + deadlines.entrySet().toString();
        }

        boolean setPending(UnitExecutorContext c, boolean value) {

            if (!value) {
                // Reset the pending value for this context. We don't care if
                // if was set or not.
                deadlines.remove(c.name);
                return false;
            }

            long time = System.currentTimeMillis();

            Long deadline = deadlines.get(c.name);

            if (deadline == null) {
                // No pending set for this context. so set it.
                deadlines.put(c.name, time + REMOTE_STEAL_TIMEOUT);
                return false;
            }

            if (time < deadline.longValue()) {
                // Pending set for this context, and the deadline has not passed
                // yet, so we're not allowed to steal.
                return true;
            }

            // Pending set for this context, but the deadline has passed, so we
            // are allowed to reset it.
            deadlines.put(c.name, time + REMOTE_STEAL_TIMEOUT);
            return false;
        }
    }

    private final HashMap<String, PendingSteal> stealThrottle = new HashMap<String, PendingSteal>();

    private final Stats stats;

    private boolean PROFILE;

    private class Facade implements Constellation {

        /* Following methods implement the Constellation interface */

        @Override
        public ActivityIdentifier submit(Activity a) {
            return performSubmit(a);
        }

        @Override
        public void send(Event e) {

            if (!e.target.expectsEvents()) {
                throw new IllegalArgumentException("Target activity " + e.target
                        + "  does not expect an event!");
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
            return performActivate();
        }

        @Override
        public void done() {
            if (logger.isInfoEnabled()) {
                logger.info("Calling performDone");
            }
            performDone();
        }

        @Override
        public void done(Concluder concluder) {
            if (logger.isInfoEnabled()) {
                logger.info("Calling performDone");
            }
            performDone(concluder);
        }

        @Override
        public boolean isMaster() {
            return pool.isMaster();
        }

        @Override
        public String identifier() {
            return identifier.toString();
        }

        @Override
        public CTimer getTimer(String standardDevice, String standardThread,
                String standardAction) {
            return stats.getTimer(standardDevice, standardThread,
                    standardAction);
        }

        @Override
        public CTimer getTimer() {
            return stats.getTimer();
        }

        @Override
        public CTimer getOverallTimer() {
            return stats.getOverallTimer();
        }
    }

    public DistributedConstellation(ConstellationProperties props)
            throws ConstellationCreationException {

        String stealName = props.STEALSTRATEGY;

        if (stealName.equalsIgnoreCase("mw")) {
            stealing = STEAL_MASTER;
        } else if (stealName.equalsIgnoreCase("none")) {
            stealing = STEAL_NONE;
        } else if (stealName.equalsIgnoreCase("pool")) {
            stealing = STEAL_POOL;
        } else {
            logger.error("Unknown stealing strategy: " + stealName);
            throw new IllegalArgumentException(
                    "Unknown stealing strategy: " + stealName);
        }

        REMOTE_STEAL_THROTTLE = props.REMOTESTEAL_THROTTLE;

        REMOTE_STEAL_TIMEOUT = props.REMOTESTEAL_TIMEOUT;

        PROFILE = props.PROFILE;

        // Init communication here...
        try {
            pool = new Pool(this, props);
            cidFactory = new DistributedConstellationIdentifierFactory(
                    pool.getRank());
            identifier = cidFactory.generateConstellationIdentifier();
            stats = new Stats(identifier.toString());

            myContext = UnitExecutorContext.DEFAULT;

            delivery = new DeliveryThread(this);
            delivery.start();

            start = System.currentTimeMillis();

            if (logger.isInfoEnabled()) {
                logger.info("DistributeConstellation : " + identifier.getId());
                logger.info(
                        "               throttle : " + REMOTE_STEAL_THROTTLE);
                logger.info(
                        "         throttle delay : " + REMOTE_STEAL_TIMEOUT);
                logger.info("               stealing : " + stealName);
                logger.info("                  start : " + start);
                logger.info("Starting DistributedConstellation " + identifier
                        + " / " + myContext);
            }
        } catch (PoolCreationFailedException e) {
            throw new ConstellationCreationException(
                    "could not create DistributedConstellation", e);
        }

    }

    private boolean performActivate() {

        synchronized (this) {
            active = true;
        }

        pool.activate();
        return subConstellation.activate();
    }

    private void performDone() {
        performDone(null);
    }

    private void performDone(Concluder concluder) {
        try {
            // NOTE: this will proceed directly on the master. On other
            // instances, it blocks until the master terminates.
            pool.terminate();
        } catch (Throwable e) {
            logger.warn("Failed to terminate pool!", e);
        }

        logger.info("Pool terminated");
        subConstellation.done();
        logger.info("Subconstellation done");

        if (concluder != null) {
            try {
                concluder.conclude();
            } catch (Throwable e) {
                logger.warn("Conclude threw exception: ", e);
            }
            logger.info("Concluded");
        }

        pool.handleStats();
        logger.info("HandleStats done");

        if (PROFILE && pool.isMaster()) {
            if (logger.isInfoEnabled()) {
                logger.info("Printing statistics");
            }
            stats.printStats(System.out);
        }
        pool.cleanup();
    }

    private synchronized boolean setPendingSteal(StealPool pool,
            ExecutorContext context, boolean value) {

        // Per (singular) StealPool we check for each of the contexts if a steal
        // request is pending. If one of the context is not pending yet, we
        // record the steal for all context and allow the request.

        PendingSteal tmp = stealThrottle.get(pool.getTag());

        if (tmp == null) {
            // When the stealpool is not in use, we create it, provided that we
            // are not setting the value to false.

            if (!value) {
                return false;
            }

            tmp = new PendingSteal(pool.getTag());
            stealThrottle.put(pool.getTag(), tmp);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("setPendingSteal: context = " + context + ", tmp = "
                    + tmp + ", value = " + value);
        }

        boolean result = true;

        if (context.isOr()) {

            OrExecutorContext ow = (OrExecutorContext) context;

            for (int i = 0; i < ow.size(); i++) {
                UnitExecutorContext uw = ow.get(i);
                boolean r = tmp.setPending(uw, value);
                result = result && r;
            }

        } else {
            result = tmp.setPending((UnitExecutorContext) context, value);
        }

        return result;
    }

    ConstellationIdentifier identifier() {
        return identifier;
    }

    public Constellation getConstellation() {
        return facade;
    }

    PrintStream getOutput() {
        return System.out;
    }

    ActivityIdentifier performSubmit(Activity a) {
        return subConstellation.performSubmit(a);
    }

    void performSend(Event e) {
        subConstellation.performSend(e);
    }

    void performCancel(ActivityIdentifier aid) {
        logger.error("Cancel not implemented!");
    }

    public void deliverRemoteStealRequest(StealRequest sr) {
        // Steal request from network
        //
        // This method is called from an finished upcall. Therefore it
        // may block for a long period of time or communicate.

        if (logger.isDebugEnabled()) {
            logger.debug("D REMOTE STEAL REQUEST from constellation "
                    + sr.source + " context " + sr.context);
        }

        subConstellation.deliverStealRequest(sr);
    }

    public void deliverRemoteStealReply(StealReply sr) {
        // StealReply from network.
        //
        // This method is called from an unfinished upcall. It may NOT
        // block for a long period of time or communicate!

        setPendingSteal(sr.getPool(), sr.getContext(), false);

        if (sr.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Got empty steal reply for " + sr.target.toString()
                        + " from " + sr.source.toString());
            }
            // ignore empty steal requests.
            return;
        }

        subConstellation.deliverStealReply(sr);
    }

    public void deliverRemoteEvent(EventMessage re) {
        // Event from network.
        //
        // This method is called from an finished upcall. Therefore it
        // may block for a long period of time or communicate.
        subConstellation.deliverEventMessage(re);
    }

    void handleStealRequest(StealRequest sr) {
        // steal request from below
        // TODO: ADD POOL AND CONTEXT AWARE THROTTLING!!!!

        // A steal request coming in from the subconstellation below.

        if (stealing == STEAL_NONE) {
            if (logger.isDebugEnabled()) {
                logger.debug("D STEAL REQUEST swizzled from " + sr.source);
            }
            return;
        }

        if (pool.isTerminated()) {
            if (logger.isDebugEnabled()) {
                logger.debug("D STEAL REQUEST from " + sr.source
                        + " not sent, pool is terminated");
            }
            return;
        }

        if (stealing == STEAL_MASTER && pool.isMaster()) {
            // Master does not steal from itself!
            return;
        }

        if (stealing == STEAL_POOL && (sr.pool == null || sr.pool.isNone())) {
            // Stealing from nobody is easy!
            return;
        }

        StealPool sp = pool.randomlySelectPool(sr.pool);

        if (REMOTE_STEAL_THROTTLE) {

            boolean pending = setPendingSteal(sp, sr.context, true);

            if (pending) {
                // We have already send out a steal in this slot, so
                // we're not allowed to send another one.
                return;
            }
        }

        if (stealing == STEAL_MASTER) {
            if (pool.forwardToMaster(sr)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("D MASTER FORWARD steal request from child "
                            + sr.source);
                }
            } else {
                // Could not send steal request, so reset slot
                setPendingSteal(sp, sr.context, false);
            }

        } else if (stealing == STEAL_POOL) {
            if (pool.randomForwardToPool(sp, sr)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("D RANDOM FORWARD steal request from child "
                            + sr.source + " to POOL " + sp.getTag());
                }
            } else {
                // Could not send steal request, so reset slot
                setPendingSteal(sp, sr.context, false);
            }
        } else {
            logger.error(
                    "D STEAL REQUEST unknown stealing strategy " + stealing);
        }
    }

    boolean handleApplicationMessage(EventMessage m, boolean enqueueOnFail) {

        // This is triggered as a result of someone in our constellation sending
        // a message (bottom up) or as a result of a incoming remote message
        // being forwarded to some other constellation (when an activity is
        // exported).

        ConstellationIdentifier target = m.target;

        // Sanity check
        if (cidFactory.isLocal(target)) {
            logger.error(
                    "Received message for local constellation (dropped message!)");
            return true;
        }

        if (pool.forward(m)) {
            return true;
        }

        if (enqueueOnFail) {
            if (logger.isInfoEnabled()) {
                logger.info("Failed to forward message to remote constellation "
                        + target + " (will retry!)");
            }
            delivery.enqueue(m);
            return true;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Failed to forward message to remote constellation "
                    + target + " (may retry)");
        }
        return false;
    }

    boolean handleStealReply(StealReply m) {

        // Handle a steal reply (bottom up)
        ConstellationIdentifier target = m.target;

        // Sanity check
        if (cidFactory.isLocal(target)) {
            logger.error(
                    "Received steal reply for local constellation (reclaiming work and dropped reply)");
            return false;
        }

        if (!pool.forward(m)) {
            // If the send fails we reclaim the work.

            if (!m.isEmpty()) {
                logger.info("Failed to deliver steal reply to " + target
                        + " (reclaiming work and dropping reply)");
                return false;
            } else {
                logger.info("Failed to deliver empty steal reply to " + target
                        + " (dropping reply)");
            }
        }

        return true;
    }

    ConstellationIdentifierFactory getConstellationIdentifierFactory(
            ConstellationIdentifier cid) {
        return cidFactory;
    }

    synchronized void register(MultiThreadedConstellation c) {

        if (active || subConstellation != null) {
            throw new Error("Cannot register BottomConstellation");
        }

        subConstellation = c;
    }

    void belongsTo(StealPool belongsTo) {

        if (belongsTo == null) {
            logger.error("Constellation does not belong to any pool!");
            return;
        }

        if (belongsTo.isNone()) {
            // We don't belong to any pool. As a result, no one can steal from
            // us.
            return;
        }

        if (belongsTo.isSet()) {

            StealPool[] set = belongsTo.set();

            for (int i = 0; i < set.length; i++) {

                // TODO: Why do we care if the stealpool is world ?
                if (!set[i].isNone()) {
                    pool.registerWithPool(set[i].getTag());
                }
            }

        } else {
            if (!belongsTo.isNone()) {
                pool.registerWithPool(belongsTo.getTag());
            }
        }
    }

    void stealsFrom(StealPool stealsFrom) {

        if (stealsFrom == null) {
            logger.warn("Constellation does not steal from any pool!");
            return;
        }

        if (stealsFrom.isNone()) {
            // We explicitly don't steal from any pool.
            return;
        }

        if (stealsFrom.isSet()) {

            StealPool[] set = stealsFrom.set();

            for (int i = 0; i < set.length; i++) {
                pool.followPool(set[i].getTag());
            }

        } else {
            pool.followPool(stealsFrom.getTag());
        }
    }

    public Stats getStats() {
        return stats;
    }

}
