package ibis.constellation.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.ConstellationCreationException;
import ibis.constellation.ConstellationProperties;
import ibis.constellation.Event;
import ibis.constellation.StealPool;
import ibis.constellation.context.ExecutorContext;
import ibis.constellation.context.OrExecutorContext;
import ibis.constellation.context.UnitExecutorContext;
import ibis.constellation.extra.CTimer;
import ibis.constellation.extra.Stats;
import ibis.constellation.impl.pool.Pool;
import ibis.constellation.impl.pool.PoolCreationFailedException;

/**
 * A <code>DistributedConstellation</code> sits between the communication pool
 * and the underlying sub-constellation, which is a
 * {@link MultiThreadedConstellation}.
 *
 * Its main tasks are to pass messages from the sub-constellation to the
 * communication pool, and vice versa, and to serve as a facade implementing the
 * {@link Constellation} interface for the application.
 */
public class DistributedConstellation {

    private static final Logger logger = LoggerFactory
            .getLogger(DistributedConstellation.class);

    /**
     * Steal strategy, as determined by properties. Value is one of
     * {@link #STEAL_POOL}, {@link #STEAL_MASTER}, {@link #STEAL_NONE}.
     */
    private final int stealStrategy;

    /** Steal from pool steal strategy. */
    private static final int STEAL_POOL = 1;

    /** Steal from master steal strategy. */
    private static final int STEAL_MASTER = 2;

    /** Don't steal (no-)steal strategy. */
    private static final int STEAL_NONE = 3;

    /** Whether remote steals are to be throttled. */
    private final boolean REMOTE_STEAL_THROTTLE;

    // FIXME setting this too low at startup causes load imbalance!
    // machines keep hammering the master for work, and (after a while)
    // get a flood of replies.
    /** Timeout for remote steal attempts. */
    private final long REMOTE_STEAL_TIMEOUT;

    /** Whether we have been activated. */
    private boolean active;

    /** The sub-constellation. */
    private MultiThreadedConstellation subConstellation;

    /** Our identification. */
    private final ConstellationIdentifier identifier;

    /** The communication pool. */
    private final Pool pool;

    /** Constellation identifier factory for sub-constellation(s). */
    private final ConstellationIdentifierFactory cidFactory;

    /** Separate thread for delivering delayed event messages. */
    private final DeliveryThread delivery;

    /** Collected executor context of sub-constellation. */
    private ExecutorContext myContext;

    /** The constellation facade. */
    private final Facade facade = new Facade();

    /**
     * Random number generator to randomly select whatever needs to be randomly
     * selected.
     */
    private final Random random = new Random();

    /**
     * A <code>PendingSteal</code> object contains the deadlines for its steal
     * pool, for several executor contexts.
     */
    private class PendingSteal {

        /** The steal pool tag. */
        final String pool;

        /** Deadlines for different unit executor contexts. */
        final HashMap<String, Long> deadlines = new HashMap<String, Long>();

        /**
         * Constructs a <code>PendingSteal</code> object with the specified
         * steal pool tag.
         *
         * @param pool
         *            the steal pool tag.
         */
        PendingSteal(String pool) {
            this.pool = pool;
        }

        @Override
        public String toString() {
            return "PendingSteal: pool = " + pool + ", deadlines for "
                    + deadlines.entrySet().toString();
        }

        /**
         * Sets or resets the deadline for the specified unit executor context.
         *
         * @param c
         *            unit executor context for with to set/reset the deadline
         * @param value
         *            whether to set or reset the deadline
         * @return whether there was a deadline for this unit executor context.
         */
        boolean setPending(UnitExecutorContext c, boolean value) {

            if (!value) {
                // Reset the pending value for this context. We don't care if
                // if was set or not.
                deadlines.remove(c.getName());
                return false;
            }

            long time = System.currentTimeMillis();

            Long deadline = deadlines.get(c.getName());

            if (deadline == null) {
                // No pending set for this context. so set it.
                deadlines.put(c.getName(), time + REMOTE_STEAL_TIMEOUT);
                return false;
            }

            if (time < deadline.longValue()) {
                // Pending set for this context, and the deadline has not passed
                // yet, so we're not allowed to steal.
                return true;
            }

            // Pending set for this context, but the deadline has passed, so we
            // are allowed to reset it.
            deadlines.put(c.getName(), time + REMOTE_STEAL_TIMEOUT);
            return false;
        }
    }

    private final HashMap<String, PendingSteal> stealThrottle = new HashMap<String, PendingSteal>();

    private final Stats stats;

    private boolean PROFILE;

    /**
     * A <code>DeliveryThread</code> is a thread object dealing with delayed
     * delivery of event messages.
     *
     * Internally, the delivery thread uses two linked lists which are used to
     * add messages that are to be sent. Only one of these is used to append
     * messages to, the other is used to actually send messages out. When the
     * delivery thread wakes up, it switches the lists, so that new messages get
     * appended to the other list, while the first list gets processed.
     */
    private class DeliveryThread extends Thread {

        /** The minimum delay. */
        private final static long MIN_DELAY = 50;

        /** The maximum delay. */
        private final static long MAX_DELAY = MIN_DELAY * 16;

        /** Event message list for incoming messages. */
        private LinkedList<EventMessage> incoming = new LinkedList<EventMessage>();

        /**
         * Event message list for messages about to be sent. Only to be touched
         * by the delivery thread.
         */
        private LinkedList<EventMessage> outgoing = new LinkedList<EventMessage>();

        /** Messages that could not be sent yet. */
        private final LinkedList<EventMessage> old = new LinkedList<EventMessage>();

        /** The current deadline. */
        private long deadline = 0;

        /** The current delay. */
        private long currentDelay = MIN_DELAY;

        /**
         * Creates and starts a <code>Delivery</code> object as a daemon thread.
         */
        DeliveryThread() {
            super("EventMessage DeliveryThread");
            setDaemon(true);
            deadline = System.currentTimeMillis() + MIN_DELAY;
        }

        /**
         * Appends an event message to the incoming queue.
         *
         * It also resets the deadline when it is higher than the minimum delay.
         *
         * @param m
         *            the event message to append.
         */
        synchronized void enqueue(EventMessage m) {
            incoming.addLast(m);

            // reset the deadline when new messages have been added.
            currentDelay = MIN_DELAY;
            long tmp = System.currentTimeMillis() + currentDelay;

            if (tmp < deadline) {
                deadline = tmp;
                notifyAll();
            }
        }

        /**
         * Swaps the incoming and outgoing lists.
         *
         * The outgoing list is supposed to be empty so can serve as new
         * incoming list. Note that the delivery thread is the only one touching
         * the outgoing list.
         *
         * @return the new outgoing list.
         */
        private synchronized LinkedList<EventMessage> swap() {
            LinkedList<EventMessage> tmp = incoming;
            incoming = outgoing;
            outgoing = tmp;
            assert (incoming.size() == 0);

            return tmp;
        }

        /**
         * Makes the delivery thread wait until the current deadline.
         */
        private synchronized void waitForDeadline() {

            long t = deadline - System.currentTimeMillis();

            while (t > 0) {
                try {
                    wait(t);
                } catch (Exception e) {
                    // ignore
                }

                t = deadline - System.currentTimeMillis();
            }
        }

        /**
         * Determines the new deadline. Needs to be synchronized because
         * {@link #enqueue(EventMessage)} also uses {@link #currentDelay} and
         * {@link #deadline}.
         */
        private synchronized void determineDeadline() {

            currentDelay = currentDelay * 2;

            if (currentDelay > MAX_DELAY) {
                currentDelay = MAX_DELAY;
            }

            deadline += currentDelay;
        }

        /**
         * Tries to send all event messages from the specified list.
         *
         * When a message is sent successfully, it is removed from the list.
         *
         * @param l
         *            the list of event messages
         * @return the number of event messages successfully sent.
         */
        private int attemptSend(LinkedList<EventMessage> l) {

            final int size = l.size();

            if (size == 0) {
                return 0;
            }

            for (int i = 0; i < size; i++) {
                EventMessage m = l.removeFirst();

                if (!handleApplicationMessage(m, false)) {
                    l.addLast(m);
                }
            }

            return (size - l.size());
        }

        @Override
        public void run() {

            while (true) {

                waitForDeadline();

                // First try to send any old messages that are still pending.
                attemptSend(old);

                // Next, get any new messages we've obtained and try to send
                // them.
                LinkedList<EventMessage> incoming = swap();
                attemptSend(incoming);

                if (incoming.size() > 0) {
                    // If we have any new message left, they are now appended to
                    // old
                    old.addAll(incoming);
                    incoming.clear();
                }

                // Increment the delay and determine new deadline
                determineDeadline();
            }
        }
    }

    /**
     * Facade implementing the {@link Constellation} interface for this
     * <code>DistributedConstellation</code>.
     */
    private class Facade implements Constellation {

        @Override
        public ActivityIdentifier submit(Activity a) {
            return subConstellation.performSubmit(a);
        }

        @Override
        public void send(Event e) {
            if (!((ActivityIdentifierImpl) e.getTarget()).expectsEvents()) {
                throw new IllegalArgumentException("Target activity "
                        + e.getTarget() + "  does not expect an event!");
            }

            // An external application wishes to send an event to
            // 'e.getTarget()'.
            subConstellation.performSend(e);
        }
        //
        // @Override
        // public void cancel(ActivityIdentifierImpl aid) {
        // // ignored!
        // }

        @Override
        public boolean activate() {
            synchronized (this) {
                if (active) {
                    return true;
                }
                active = true;
            }

            pool.activate();
            return subConstellation.activate();
        }

        @Override
        public void done() {
            performDone();
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

    /**
     * Creates a <code>DistributedConstellation</code>.
     *
     * @param props
     *            the properties to use
     * @throws ConstellationCreationException
     *             is thrown when the communication pool could not be created
     *             for some reason
     * @throws IllegalArgumentException
     *             is thrown when a property value is not recognized
     *
     */
    public DistributedConstellation(ConstellationProperties props)
            throws ConstellationCreationException {

        String stealName = props.STEALSTRATEGY;

        if (stealName.equalsIgnoreCase("mw")) {
            stealStrategy = STEAL_MASTER;
        } else if (stealName.equalsIgnoreCase("none")) {
            stealStrategy = STEAL_NONE;
        } else if (stealName.equalsIgnoreCase("pool")) {
            stealStrategy = STEAL_POOL;
        } else {
            logger.error("Unknown stealStrategy strategy: " + stealName);
            throw new IllegalArgumentException(
                    "Unknown stealStrategy strategy: " + stealName);
        }

        REMOTE_STEAL_THROTTLE = props.REMOTESTEAL_THROTTLE;

        REMOTE_STEAL_TIMEOUT = props.REMOTESTEAL_TIMEOUT;

        PROFILE = props.PROFILE;

        // Init communication here...
        try {
            pool = new Pool(this, props);
            cidFactory = new ConstellationIdentifierFactory(pool.getRank());
            identifier = cidFactory.generateConstellationIdentifier();
            stats = new Stats(pool.identifier());

            myContext = UnitExecutorContext.DEFAULT;

            delivery = new DeliveryThread();
            delivery.start();

            if (logger.isInfoEnabled()) {
                logger.info("DistributeConstellation : " + identifier.getId());
                logger.info(
                        "               throttle : " + REMOTE_STEAL_THROTTLE);
                logger.info(
                        "         throttle delay : " + REMOTE_STEAL_TIMEOUT);
                logger.info("               stealStrategy : " + stealName);
                logger.info("Starting DistributedConstellation " + identifier
                        + " / " + myContext);
            }
        } catch (PoolCreationFailedException e) {
            throw new ConstellationCreationException(
                    "could not create DistributedConstellation", e);
        }

    }

    /**
     * Implements {@link Constellation#done()} for this
     * <code>DistributedConstellation</code>.
     *
     * It terminates the pool, notifies the sub-constellation, and deals with
     * statistics.
     */
    private void performDone() {
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

    /**
     * Checks and sets flags for pending steals for particular steal pools and
     * contexts.
     *
     * Per (singular) steal pool we check for each of the contexts if a steal
     * request is pending. If one of the context is not pending yet, we record
     * the steal for all context and allow the request.
     *
     * @param pool
     *            the steal pool
     * @param context
     *            the executor context
     * @param value
     *            value to set the pending flag to.
     * @return whether there already is a pending steal.
     */
    private synchronized boolean setPendingSteal(StealPool pool,
            ExecutorContext context, boolean value) {

        String poolTag = pool.getTag();
        PendingSteal tmp = stealThrottle.get(poolTag);

        if (tmp == null) {
            // When the stealpool is not in use, we create it, provided that we
            // are not setting the value to false.

            if (!value) {
                return false;
            }

            tmp = new PendingSteal(poolTag);
            stealThrottle.put(poolTag, tmp);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("setPendingSteal: context = " + context + ", tmp = "
                    + tmp + ", value = " + value);
        }

        boolean result = true;

        if (context instanceof OrExecutorContext) {

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

    /**
     * Returns the identifier of this constellation.
     *
     * @return the constellation identifier.
     */
    ConstellationIdentifier identifier() {
        return identifier;
    }

    /**
     * Returns the facade implementing the {@link Constellation} interface for a
     * <code>DistributedConstellation</code>.
     *
     * @return the constellation facade.
     */
    public Constellation getConstellation() {
        return facade;
    }

    /**
     * Deals with a steal request delivered by the network (i.e. another node).
     *
     * The steal request is dealt with by passing it on to the sub-constellation
     * below.
     *
     * @param re
     *            the steal request message.
     */
    public void deliverRemoteStealRequest(StealRequest sr) {
        if (logger.isDebugEnabled()) {
            logger.debug("D REMOTE STEAL REQUEST from constellation "
                    + sr.source + " context " + sr.context);
        }

        subConstellation.deliverStealRequest(sr);
    }

    /**
     * Deals with a steal reply delivered by the network (i.e. another node).
     *
     * The steal reply is dealt with by checking if it contains any work, and if
     * so, pass it on to the sub-constellation below.
     *
     * @param sr
     *            the steal reply.
     */
    public void deliverRemoteStealReply(StealReply sr) {

        // Reset any pending steal attempts for this pool and context, because
        // we now got an answer.
        setPendingSteal(sr.getPool(), sr.getContext(), false);

        if (sr.isEmpty()) {
            // No work in this steal reply.
            if (logger.isDebugEnabled()) {
                logger.debug("Got empty steal reply for " + sr.target.toString()
                        + " from " + sr.source.toString());
            }
            return;
        }

        subConstellation.deliverStealReply(sr);
    }

    /**
     * Deals with an event delivered by the network (i.e. another node).
     *
     * The event is dealt with by passing it on to the sub-constellation below.
     *
     * @param re
     *            the event message.
     */
    public void deliverRemoteEvent(EventMessage re) {
        subConstellation.deliverEventMessage(re);
    }

    /**
     * Deals with a steal request from the sub-constellation below.
     *
     * The action taken depends on the steal strategy, steal pool, et cetera.
     *
     * @param sr
     *            the steal request.
     */
    void handleStealRequest(StealRequest sr) {
        if (stealStrategy == STEAL_NONE) {
            // drop steal request
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

        if (stealStrategy == STEAL_MASTER && pool.isMaster()) {
            // Master does not steal from itself!
            return;
        }

        if (stealStrategy == STEAL_POOL
                && (sr.pool == null || sr.pool.isNone())) {
            // Stealing from nobody is easy!
            return;
        }

        StealPool sp = sr.pool.randomlySelectPool(random);

        if (REMOTE_STEAL_THROTTLE) {

            boolean pending = setPendingSteal(sp, sr.context, true);

            if (pending) {
                // We have already send out a steal in this slot, so
                // we're not allowed to send another one.
                return;
            }
        }

        if (stealStrategy == STEAL_MASTER) {
            if (pool.forwardToMaster(sr)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("D MASTER FORWARD steal request from child "
                            + sr.source);
                }
            } else {
                // Could not send steal request, so reset slot
                setPendingSteal(sp, sr.context, false);
            }

        } else if (stealStrategy == STEAL_POOL) {
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
            logger.error("D STEAL REQUEST unknown stealStrategy strategy "
                    + stealStrategy);
        }
    }

    /**
     * Handles an event message, either remote or from below.
     *
     * This method gets called, either as a result of someone in our
     * constellation sending a message (bottom up) or as a result of a incoming
     * remote message being forwarded to some other constellation (when an
     * activity is exported).
     *
     * @param m
     *            the event message
     * @param enqueueOnFail
     *            when set, delivery will be retried when it fails.
     * @return <code>true</code> when the message is dealt with,
     *         <code>false</code> otherwise.
     */
    boolean handleApplicationMessage(EventMessage m, boolean enqueueOnFail) {

        ConstellationIdentifier target = m.target;

        // Sanity check
        // if (cidFactory.isLocal(target)) {
        // logger.error(
        // "Received message for local constellation (dropped message!)");
        // return true;
        // }
        assert (!cidFactory.isLocal(target));

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

    /**
     * Receives a steal reply from below, and forwards it to the pool.
     *
     * If the pool fails to deal with the reply, any work is reclaimed,
     * indicated by returning <code>false</code>. Otherwise, if the pool
     * succeeds in forwarding the reply, <code>true</code> is returned. If the
     * pool fails, but there is no work involved, <code>true</code> is returned
     * as well, because no further action is warranted.
     *
     * @param m
     *            the steal reply
     * @return <code>false</code> if the work in the message must be reclaimed,
     *         <code>true</code> otherwise.
     */
    boolean handleStealReply(StealReply m) {

        // Handle a steal reply (bottom up)
        ConstellationIdentifier target = m.target;

        // Sanity check
        assert (!cidFactory.isLocal(target));

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

    /**
     * Provides a constellation identifier factory to produce identifiers for
     * sub-constellation instances (both multithreaded and singlethreaded).
     *
     * @return the constellation identifier factory.
     */
    ConstellationIdentifierFactory getConstellationIdentifierFactory() {
        return cidFactory;
    }

    /**
     * Registers the underlying multithreaded constellation.
     *
     * @param c
     *            the underlying multithreaded constellation.
     */
    synchronized void register(MultiThreadedConstellation c) {

        if (active || subConstellation != null) {
            throw new Error("Cannot register BottomConstellation");
        }

        subConstellation = c;
    }

    /**
     * Informs this constellation to which pool it belongs.
     *
     * This method is called from the MultiThreadedConstellation below. The tags
     * from the pool get passed on to the communication layer, allowing this
     * layer to build a picture of which pool is running where.
     *
     * @param belongsTo
     *            the pool to which this constellation belongs.
     */
    void belongsTo(StealPool belongsTo) {

        assert (belongsTo != null);

        if (belongsTo.isNone()) {
            // We don't belong to any pool. As a result, no one can steal from
            // us.
            return;
        }

        StealPool[] set = belongsTo.set();
        for (int i = 0; i < set.length; i++) {
            pool.registerWithPool(set[i].getTag());
        }
    }

    /**
     * Informs this constellation from which pool it is stealStrategy.
     *
     * This method is called from the MultiThreadedConstellation below. The tags
     * from the pool get passed on to the communication layer, allowing this
     * layer to build a picture of which nodes we are interested in when
     * stealStrategy.
     *
     * @param stealsFrom
     *            the pool from which this constellation is stealStrategy.
     */
    void stealsFrom(StealPool stealsFrom) {

        assert (stealsFrom != null);

        if (stealsFrom.isNone()) {
            // We explicitly don't steal from any pool.
            return;
        }

        StealPool[] set = stealsFrom.set();

        for (int i = 0; i < set.length; i++) {
            pool.followPool(set[i].getTag());
        }
    }

    public Stats getStats() {
        return stats;
    }

}
