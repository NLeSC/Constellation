package ibis.constellation.impl.pool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.ConstellationProperties;
import ibis.constellation.StealPool;
import ibis.constellation.impl.ConstellationIdentifierImpl;
import ibis.constellation.impl.DistributedConstellation;
import ibis.constellation.impl.EventMessage;
import ibis.constellation.impl.MessageBase;
import ibis.constellation.impl.StealReply;
import ibis.constellation.impl.StealRequest;
import ibis.constellation.impl.pool.communication.CommunicationLayer;
import ibis.constellation.impl.pool.communication.Message;
import ibis.constellation.impl.pool.communication.NodeIdentifier;
import ibis.constellation.impl.pool.communication.ibis.CommunicationLayerImpl;
import ibis.constellation.impl.util.Stats;
import ibis.constellation.impl.util.TimeSyncInfo;

public class Pool {

    private static final Logger logger = LoggerFactory.getLogger(Pool.class);

    private static final byte OPCODE_EVENT_MESSAGE = 10;
    private static final byte OPCODE_STEAL_REQUEST = 11;
    private static final byte OPCODE_STEAL_REPLY = 12;

    private static final byte OPCODE_POOL_REGISTER_REQUEST = 43;
    private static final byte OPCODE_POOL_UPDATE_REQUEST = 44;
    private static final byte OPCODE_POOL_UPDATE_REPLY = 45;

    private static final byte OPCODE_RANK_REGISTER_REQUEST = 53;
    private static final byte OPCODE_RANK_LOOKUP_REQUEST = 54;
    private static final byte OPCODE_RANK_LOOKUP_REPLY = 55;

    private static final byte OPCODE_REQUEST_TIME = 63;
    private static final byte OPCODE_SEND_TIME = 64;

    private static final byte OPCODE_STATISTICS = 73;

    private static final byte OPCODE_NOTHING = 83;
    private static final byte OPCODE_RELEASE = 84;

    private static final byte OPCODE_PING = 93;
    private static final byte OPCODE_PONG = 94;

    private DistributedConstellation owner;

    private final ConcurrentHashMap<Integer, NodeIdentifier> locationCache = new ConcurrentHashMap<Integer, NodeIdentifier>();

    private final NodeIdentifier local;
    private final NodeIdentifier master;

    private int rank = -1;

    private boolean isMaster;

    private final Random random = new Random();

    private final boolean closedPool;

    private final HashMap<NodeIdentifier, Long> times = new HashMap<NodeIdentifier, Long>();

    private final TimeSyncInfo syncInfo;

    class PoolUpdater extends Thread {

        private static final long MIN_DELAY = 1000;
        private static final long MAX_DELAY = 10000;
        private static final long INCR_DELAY = 1000;

        private long deadline = 0;
        private long currentDelay = MIN_DELAY;
        private boolean done;

        private ArrayList<String> tags = new ArrayList<String>();
        private ArrayList<PoolInfo> updates = new ArrayList<PoolInfo>();

        private synchronized void addTag(String tag) {
            if (logger.isInfoEnabled()) {
                logger.info("Adding tag " + tag + " to PoolUpdater");
            }
            if (!tags.contains(tag)) {
                tags.add(tag);
            }
        }

        private synchronized String[] getTags() {
            return tags.toArray(new String[tags.size()]);
        }

        private synchronized void enqueueUpdate(PoolInfo info) {
            if (logger.isInfoEnabled()) {
                logger.info("Enqueueing PoolInfo update");
            }
            updates.add(info);
            notifyAll();
        }

        private synchronized PoolInfo dequeueUpdate() {

            if (updates.size() == 0) {
                return null;
            }

            if (logger.isInfoEnabled()) {
                logger.info("Dequeueing PoolInfo update");
            }
            // Dequeue in LIFO order too prevent unnecessary updates
            return updates.remove(updates.size() - 1);
        }

        private synchronized boolean getDone() {
            return done;
        }

        private synchronized void done() {
            done = true;
        }

        private void processUpdates() {

            PoolInfo update = dequeueUpdate();

            if (update == null) {
                // No updates
                currentDelay += INCR_DELAY;

                if (currentDelay >= MAX_DELAY) {
                    currentDelay = MAX_DELAY;
                }

                return;
            }

            currentDelay = MIN_DELAY;

            while (update != null) {
                performUpdate(update);
                update = dequeueUpdate();
            }
        }

        private void sendUpdateRequests() {

            String[] pools = getTags();

            for (String pool : pools) {
                requestUpdate(pool);
            }
        }

        private void waitUntilDeadLine() {

            long sleep = deadline - System.currentTimeMillis();

            while (sleep > 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("PoolUpdater sleeping " + sleep + " ms");
                }
                try {
                    synchronized (this) {
                        wait(sleep);
                    }
                } catch (Exception e) {
                    // ignore
                }
                sleep = deadline - System.currentTimeMillis();
            }
        }

        @Override
        public void run() {

            if (logger.isInfoEnabled()) {
                logger.info("Starting PoolUpdater");
            }

            while (!getDone()) {

                processUpdates();

                long now = System.currentTimeMillis();

                if (now >= deadline) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("PoolUpdater requesting updates");
                    }

                    sendUpdateRequests();
                    deadline = now + currentDelay;
                }

                waitUntilDeadLine();
            }
        }
    }

    private HashMap<String, PoolInfo> pools = new HashMap<String, PoolInfo>();
    private PoolUpdater updater = new PoolUpdater();
    private boolean gotRelease;
    private boolean gotAnswer;
    private boolean gotPong;
    private final ConstellationProperties properties;

    private int gotStats;

    private boolean terminated;

    private NodeIdentifier[] ids = null;

    private final CommunicationLayer comm;

    private boolean cleanup;

    public Pool(final DistributedConstellation owner, final ConstellationProperties properties)
            throws PoolCreationFailedException {

        this.owner = owner;
        closedPool = properties.CLOSED;
        this.properties = properties;

        if (closedPool && properties.POOLSIZE > 0) {
            properties.setProperty("ibis.pool.size", "" + properties.POOLSIZE);
        }

        comm = new CommunicationLayerImpl(properties, this);
        local = comm.getMyIdentifier();
        master = comm.getMaster();
        rank = comm.getRank();
        isMaster = local.equals(master);
        locationCache.put(rank, local);

        // Register my rank at the master
        if (!isMaster) {
            doForward(master, OPCODE_RANK_REGISTER_REQUEST, new RankInfo(rank, local));
            syncInfo = null;
        } else {
            syncInfo = new TimeSyncInfo(master.name());
        }

        // Start the updater thread...
        updater.start();

        if (closedPool) {
            ids = comm.getNodeIdentifiers();
        }

        logger.info("Pool created");

    }

    public Stats getStats() {
        return owner.getStats();
    }

    public void activate() {
        comm.activate();
        if (logger.isInfoEnabled()) {
            logger.info("Activating POOL on " + local);
        }

        if (closedPool) {
            if (isMaster()) {
                for (NodeIdentifier id : ids) {
                    if (!id.equals(local)) {
                        // First do a pingpong to make sure that the other side
                        // has upcalls enabled already.
                        doForward(id, OPCODE_PING, null);
                        synchronized (this) {
                            while (!gotPong) {
                                try {
                                    wait();
                                } catch (Throwable e) {
                                    // ignore
                                }
                            }
                            gotPong = false;
                        }
                        getTimeOfOther(id);
                        synchronized (this) {
                            while (!gotAnswer) {
                                try {
                                    wait();
                                } catch (Throwable e) {
                                    // ignore
                                }
                            }
                            gotAnswer = false;
                        }
                    }
                }
                for (NodeIdentifier id : ids) {
                    if (!id.equals(local)) {
                        doForward(id, OPCODE_RELEASE, null);
                    }
                }
            } else {
                synchronized (this) {
                    while (!gotRelease) {
                        try {
                            wait();
                        } catch (Throwable e) {
                            // ignore
                        }
                    }
                }
            }
        }
    }

    public boolean isLocal(ConstellationIdentifierImpl id) {
        return rank == id.getNodeId();
    }

    public void terminate() throws IOException {
        comm.terminate();
        updater.done();
        terminated = true;
    }

    public void handleStats() {
        if (properties.PROFILE) { // Only if profiling.
            Stats stats = owner.getStats();
            if (isMaster) {
                stats.setSyncInfo(syncInfo);
                if (logger.isInfoEnabled()) {
                    logger.info("waiting for stats of other nodes");
                }

                if (closedPool) {
                    synchronized (this) {
                        int nClients = comm.getPoolSize() - 1;
                        long time = System.currentTimeMillis();
                        while (gotStats < nClients) {
                            try {
                                wait(1000);
                            } catch (Throwable e) {
                                // ignore
                            }
                            if (System.currentTimeMillis() - time > 60000) {
                                break;
                            }
                        }
                    }
                } else {
                    try {
                        Thread.sleep(30000);
                    } catch (Throwable e) {
                        // ignore
                    }
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Sending statistics to master");
                }
                synchronized (stats) {
                    doForward(master, OPCODE_STATISTICS, stats);
                }
            }
        }
    }

    public void cleanup() {
        synchronized (this) {
            cleanup = true;
        }
        updater.done();
        comm.cleanup();
    }

    public int getRank() {
        return rank;
    }

    public boolean isMaster() {
        return isMaster;
    }

    private NodeIdentifier translate(ConstellationIdentifierImpl cid) {
        int rank = cid.getNodeId();
        return lookupRank(rank);
    }

    private boolean doForward(NodeIdentifier dest, byte opcode, Object data) {
        Message m = new Message(opcode, data);
        synchronized (this) {
            if (cleanup) {
                return true;
            }
        }
        return comm.sendMessage(dest, m);
    }

    public boolean forward(StealReply sr) {

        // logger.info("POOL:FORWARD StealReply from " + sr.source +
        // " to " + sr.target);

        return forward(sr, OPCODE_STEAL_REPLY);
    }

    public boolean forward(EventMessage em) {
        return forward(em, OPCODE_EVENT_MESSAGE);
    }

    private boolean forward(MessageBase m, byte opcode) {

        ConstellationIdentifierImpl target = m.target;

        if (logger.isTraceEnabled()) {
            logger.trace("POOL FORWARD Message from " + m.source + " to " + m.target + " " + m);
        }

        NodeIdentifier id = translate(target);

        if (id == null) {
            if (logger.isInfoEnabled()) {
                logger.info("POOL failed to translate " + target + " to a NodeIdentifier");
            }
            return false;
        }

        if (logger.isDebugEnabled() && opcode == OPCODE_EVENT_MESSAGE) {
            logger.debug("Sending " + m + " to " + id);
        }

        return doForward(id, opcode, m);
    }

    public boolean forwardToMaster(StealRequest m) {
        return doForward(master, OPCODE_STEAL_REQUEST, m);
    }

    private void registerRank(RankInfo info) {
        registerRank(info.rank, info.id);
    }

    private void registerRank(int rank, NodeIdentifier id) {
        NodeIdentifier old = locationCache.put(rank, id);

        if (logger.isInfoEnabled() && old == null) {
            logger.info("Register rank " + rank + ", id = " + id);
        }

        // sanity check
        if (old != null && !old.equals(id)) {
            logger.error("Location cache overwriting rank " + rank + " with different id! " + old + " != " + id, new Throwable());
        }
    }

    private void registerRank(ConstellationIdentifierImpl cid, NodeIdentifier id) {
        int rank = cid.getNodeId();
        registerRank(rank, id);
    }

    private NodeIdentifier lookupRank(int rank) {

        // Do a local lookup
        NodeIdentifier tmp = locationCache.get(rank);

        // Return if we have a result, or if there is no one that we can ask
        if (tmp != null || isMaster) {
            return tmp;
        }

        // Forward a request to the master for the id of rank
        doForward(master, OPCODE_RANK_LOOKUP_REQUEST, new RankInfo(rank, local));

        return null;
    }

    private void lookupRankRequest(RankInfo info) {

        NodeIdentifier tmp = locationCache.get(info.rank);

        if (tmp == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Location lookup for rank " + rank + " returned null! Dropping reply");
            }
            // Timo: drop reply, sender will retry automatically, and does not
            // handle null replies well.
            return;
        }

        doForward(info.id, OPCODE_RANK_LOOKUP_REPLY, new RankInfo(info.rank, tmp));
    }

    private void getTimeOfOther(NodeIdentifier id) {
        // Send something just to set up the connection.
        doForward(id, OPCODE_NOTHING, null);
        if (logger.isDebugEnabled()) {
            logger.debug("Obtaining time from " + id.name());
        }
        long myTime = System.nanoTime();
        synchronized (times) {
            times.put(id, new Long(myTime));
        }
        doForward(id, OPCODE_REQUEST_TIME, null);
    }

    public void upcall(NodeIdentifier source, Message rm) {

        byte opcode = rm.opcode;

        if (logger.isDebugEnabled()) {
            logger.debug(getString(opcode, "Got") + " from " + source.name());
        }

        if (opcode == OPCODE_NOTHING) {
            return;
        }
        if (opcode == OPCODE_RELEASE) {
            synchronized (this) {
                gotRelease = true;
                notifyAll();
            }
            return;
        }

        Object data = rm.contents;

        if (opcode == OPCODE_SEND_TIME) {
            long l = ((Long) data).longValue();
            Long myTime = times.get(source);
            if (myTime == null) {
                logger.warn("Ignored rogue time answer");
                return;
            }
            long interval = (System.nanoTime() - myTime.longValue());
            long half = interval / 2;
            long offset = myTime.longValue() + half - l;
            if (logger.isDebugEnabled()) {
                logger.debug("source = " + source.name() + ", offset = " + offset + ", interval = " + interval);
            }
            syncInfo.put(source.name(), new Long(offset));
            if (closedPool) {
                synchronized (this) {
                    gotAnswer = true;
                    notifyAll();
                }
            }
            return;
        }

        if (logger.isDebugEnabled() && opcode == OPCODE_STEAL_REPLY && data != null) {
            logger.debug("Jobs stolen from " + source.name() + ": " + ((StealReply) data).toString());

        }

        switch (opcode) {
        case OPCODE_STATISTICS:
            owner.getStats().add((Stats) data);
            comm.cleanup(source); // To speed up termination
            synchronized (this) {
                gotStats++;
                notifyAll();
            }
            break;
        case OPCODE_REQUEST_TIME:
            doForward(source, OPCODE_SEND_TIME, new Long(System.nanoTime()));
            break;
        case OPCODE_PING:
            doForward(source, OPCODE_PONG, null);
            break;
        case OPCODE_PONG:
            synchronized (this) {
                gotPong = true;
                notifyAll();
            }
            break;
        case OPCODE_STEAL_REQUEST: {
            StealRequest m = (StealRequest) data;
            registerRank(m.source, source);

            if (logger.isTraceEnabled()) {
                logger.trace("POOL RECEIVE StealRequest from " + m.source);
            }

            m.setRemote();
            owner.deliverRemoteStealRequest(m);
        }
            break;

        case OPCODE_STEAL_REPLY: {
            StealReply m = (StealReply) data;
            registerRank(m.source, source);

            if (logger.isTraceEnabled()) {
                logger.trace("POOL RECEIVE StealReply from " + m.source);
            }

            owner.deliverRemoteStealReply(m);
        }
            break;

        case OPCODE_EVENT_MESSAGE: {
            EventMessage m = (EventMessage) data;
            // registerRank(m.source, source); NO! event messages can be
            // forwarded when an activation was stolen. --Ceriel

            if (logger.isInfoEnabled()) {
                logger.info("RECEIVE EventMessage; " + m);
            }

            owner.deliverRemoteEvent(m);
        }
            break;

        case OPCODE_POOL_REGISTER_REQUEST: {
            performRegisterWithPool((PoolRegisterRequest) data);
        }
            break;

        case OPCODE_POOL_UPDATE_REQUEST: {
            performUpdateRequest((PoolUpdateRequest) data);
        }
            break;

        case OPCODE_POOL_UPDATE_REPLY: {
            updater.enqueueUpdate((PoolInfo) data);
        }
            break;

        case OPCODE_RANK_REGISTER_REQUEST:
            registerRank((RankInfo) data);
            if (!closedPool) {
                getTimeOfOther(source);
            }
            break;

        case OPCODE_RANK_LOOKUP_REPLY: {
            registerRank((RankInfo) data);
        }
            break;

        case OPCODE_RANK_LOOKUP_REQUEST: {
            lookupRankRequest((RankInfo) data);
        }
            break;

        default:
            logger.error("Received unknown message opcode: " + opcode);
            break;
        }
    }

    public boolean randomForwardToPool(StealPool pool, StealRequest sr) {

        // NOTE: We know the pool is not NULL or NONE, and not a set
        PoolInfo info = null;

        synchronized (pools) {
            info = pools.get(pool.getTag());
        }

        if (info == null) {
            logger.warn("Failed to randomly select node in pool " + pool.getTag() + ", pool does not exist?");
            return false;
        }

        NodeIdentifier id = info.selectRandom(random);

        if (id == null) {
            logger.warn("Failed to randomly select node in pool " + pool.getTag());
            return false;
        }

        // If the chosen id is the local one, don't just return false, because
        // this hampers the remote steal throttle mechanism. Just try again to
        // select a node, as long as there is a choice.
        while (id == null || id.equals(local)) {
            if (info.nMembers() <= 1) {
                return false;
            }
            id = info.selectRandom(random);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Sending steal request to " + id.name());
        }
        return doForward(id, OPCODE_STEAL_REQUEST, sr);
    }

    private void performRegisterWithPool(PoolRegisterRequest request) {

        PoolInfo tmp = null;

        if (logger.isDebugEnabled()) {
            logger.debug("Processing register request " + request.tag + " from " + request.source);
        }

        synchronized (pools) {
            tmp = pools.get(request.tag);
        }

        if (tmp == null) {
            logger.error("Failed to find pool " + request.tag + " to register " + request.source);
            return;
        }

        tmp.addMember(request.source);
    }

    private void performUpdateRequest(PoolUpdateRequest request) {

        PoolInfo tmp = null;

        synchronized (pools) {
            tmp = pools.get(request.tag);
        }

        if (tmp == null) {
            logger.warn("Failed to find pool " + request.tag + " for update request from " + request.source);
            return;
        }

        if (tmp.currentTimeStamp() > request.timestamp) {
            synchronized (tmp) {
                // Copy to avoid ConcurrentModificationException.
                tmp = new PoolInfo(tmp);
            }
            doForward(request.source, OPCODE_POOL_UPDATE_REPLY, tmp);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No updates found for pool " + request.tag + " / " + request.timestamp);
            }
        }
    }

    private void requestRegisterWithPool(NodeIdentifier master, String tag) {
        if (logger.isInfoEnabled()) {
            logger.info("Sending register request for pool " + tag + " to " + master);
        }

        doForward(master, OPCODE_POOL_REGISTER_REQUEST, new PoolRegisterRequest(local, tag));
    }

    private void requestUpdate(NodeIdentifier master, String tag, long timestamp) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending update request for pool " + tag + " to " + master + " for timestamp " + timestamp);
        }

        doForward(master, OPCODE_POOL_UPDATE_REQUEST, new PoolUpdateRequest(local, tag, timestamp));
    }

    public void registerWithPool(String tag) {

        try {
            // First check if the pool is already registered. If not, we need to
            // add a temporary PoolInfo object to our hashmap to ensure that we
            // catch any register requests if we become the master!

            synchronized (pools) {
                PoolInfo info = pools.get(tag);

                if (info != null) {
                    logger.info("Pool " + tag + " already registered!");
                    return;
                }

                pools.put(tag, new PoolInfo(tag));
            }

            String electTag = "STEALPOOL$" + tag;

            logger.info("Electing master for POOL " + electTag);

            NodeIdentifier id = comm.elect(electTag);

            boolean master = id.equals(local);

            logger.info("Master for POOL " + electTag + " is " + id + " " + master);

            // Next, create the pool locally, or register ourselves at the
            // master.
            if (master) {

                synchronized (pools) {
                    PoolInfo info = pools.get(tag);

                    if (info.hasMembers()) {
                        logger.warn("Hit race in pool registration! -- will recover!");
                        pools.put(tag, new PoolInfo(info, id));
                    } else {
                        pools.put(tag, new PoolInfo(tag, id, true));
                    }
                }
            } else {
                // We remove the unused PoolInfo
                synchronized (pools) {
                    PoolInfo info = pools.remove(tag);

                    // Sanity checks
                    if (info != null) {
                        if (!info.isDummy()) {
                            logger.error("INTERNAL ERROR: Removed non-dummy PoolInfo!");
                        }
                    } else {
                        logger.warn("Failed to find dummy PoolInfo!");
                    }
                }

                requestRegisterWithPool(id, tag);
            }

        } catch (IOException e) {
            logger.warn("Failed to register pool " + tag, e);
        }
    }

    public void followPool(String tag) {

        // logger.info("Searching for pool " + tag);

        try {
            // Simple case: we own the pool or already follow it.
            synchronized (pools) {
                if (pools.containsKey(tag)) {
                    return;
                }
            }

            String electTag = "STEALPOOL$" + tag;

            NodeIdentifier id = comm.getElectionResult(electTag, 1000);

            // TODO: will repeat for ever if pool master does not exist...
            while (id == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Searching master for POOL " + electTag);
                }
                id = comm.getElectionResult(electTag, 1000);
            }

            boolean master = id.equals(local);

            logger.info("Found master for POOL " + electTag + " " + id + " " + master);

            if (master) {
                // Assuming the pools are static and registered in
                // the right order this should not happen
                logger.error("INTERNAL ERROR: election of follow pool returned self!");
                return;
            }

            synchronized (pools) {
                pools.put(tag, new PoolInfo(tag, id, false));
            }

            updater.addTag(tag);
        } catch (IOException e) {
            logger.warn("Failed to register pool " + tag, e);
        }
    }

    private void performUpdate(PoolInfo info) {

        synchronized (pools) {
            PoolInfo tmp = pools.get(info.getTag());

            if (tmp == null) {
                logger.warn("Received spurious pool update! " + info.getTag());
                return;
            }

            if (info.currentTimeStamp() > tmp.currentTimeStamp()) {
                pools.put(info.getTag(), info);
            }
        }
    }

    private void requestUpdate(String tag) {

        PoolInfo tmp = null;

        if (logger.isInfoEnabled()) {
            logger.info("Requesting update for pool " + tag);
        }

        synchronized (pools) {
            tmp = pools.get(tag);

            if (tmp == null || tmp.isDummy()) {
                logger.warn("Cannot request update for " + tag + ": unknown pool!");
                return;
            }

        }

        requestUpdate(tmp.getMaster(), tag, tmp.currentTimeStamp());
    }

    public static String getString(int opcode, String readOrWrite) {
        switch (opcode) {
        case OPCODE_EVENT_MESSAGE:
            return readOrWrite + " event message";
        case OPCODE_STEAL_REQUEST:
            return readOrWrite + " steal request";
        case OPCODE_STEAL_REPLY:
            return readOrWrite + " steal reply";
        case OPCODE_POOL_REGISTER_REQUEST:
            return readOrWrite + " pool register request";
        case OPCODE_POOL_UPDATE_REQUEST:
            return readOrWrite + " pool update request";
        case OPCODE_POOL_UPDATE_REPLY:
            return readOrWrite + " pool update reply";
        case OPCODE_RANK_REGISTER_REQUEST:
            return readOrWrite + " rank register request";
        case OPCODE_RANK_LOOKUP_REQUEST:
            return readOrWrite + " rank lookup request";
        case OPCODE_RANK_LOOKUP_REPLY:
            return readOrWrite + " rank lookup reply";
        case OPCODE_STATISTICS:
            return readOrWrite + " statistics";
        case OPCODE_REQUEST_TIME:
            return readOrWrite + " request time";
        case OPCODE_SEND_TIME:
            return readOrWrite + " send time";
        case OPCODE_NOTHING:
            return readOrWrite + " nothing";
        case OPCODE_RELEASE:
            return readOrWrite + " release";
        case OPCODE_PING:
            return readOrWrite + " ping";
        case OPCODE_PONG:
            return readOrWrite + " pong";

        default:
            return readOrWrite + " other";
        }
    }

    public boolean isTerminated() {
        return terminated;
    }

}
