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
package ibis.constellation.impl.pool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.ConstellationProperties;
import ibis.constellation.StealPool;
import ibis.constellation.impl.AbstractMessage;
import ibis.constellation.impl.ConstellationIdentifierImpl;
import ibis.constellation.impl.DistributedConstellation;
import ibis.constellation.impl.EventMessage;
import ibis.constellation.impl.StealReply;
import ibis.constellation.impl.StealRequest;
import ibis.constellation.util.ByteBufferCache;
import ibis.constellation.util.ByteBuffers;
import ibis.ipl.IbisIdentifier;
import nl.junglecomputing.pidgin.Pidgin;
import nl.junglecomputing.pidgin.PidginFactory;
import nl.junglecomputing.pidgin.Upcall;
import nl.junglecomputing.pidgin.UpcallChannel;
import nl.junglecomputing.timer.Profiling;
import nl.junglecomputing.timer.TimeSyncInfo;

public class Pool implements Upcall {

    private static final Logger logger = LoggerFactory.getLogger(Pool.class);

    public final String CHANNEL_ADMIN = "constellation_ADMIN";
    public final String CHANNEL_STEAL = "constellation_STEAL";
    public final String CHANNEL_EVENT = "constellation_EVENT";

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

    private static final byte OPCODE_PROFILING = 73;

    private static final byte OPCODE_NOTHING = 83;
    private static final byte OPCODE_RELEASE = 84;

    private static final byte OPCODE_PING = 93;
    private static final byte OPCODE_PONG = 94;

    private DistributedConstellation owner;

    private final ConcurrentHashMap<Integer, IbisIdentifier> locationCache = new ConcurrentHashMap<Integer, IbisIdentifier>();

    private final IbisIdentifier local;
    private final IbisIdentifier master;

    private int rank = -1;

    private boolean isMaster;

    private final Random random = new Random();

    private final boolean closedPool;

    private final HashMap<IbisIdentifier, Long> times = new HashMap<IbisIdentifier, Long>();

    private final Profiling profiling;

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

    private int gotProfiling;

    private boolean terminated;

    private IbisIdentifier[] ids = null;

    private final Pidgin comm;

    private boolean cleanup;

    private UpcallChannel adminChannel;
    private UpcallChannel stealChannel;
    private UpcallChannel eventChannel;

    public Pool(final DistributedConstellation owner, final ConstellationProperties properties) throws PoolCreationFailedException {

        this.owner = owner;
        closedPool = properties.CLOSED;
        this.properties = properties;

        if (closedPool && properties.POOLSIZE > 0) {
            properties.setProperty("ibis.pool.size", "" + properties.POOLSIZE);
        }

        try {
            comm = PidginFactory.create(properties);
            local = comm.getMyIdentifier();
            master = comm.getMaster();
            rank = comm.getRank();
            isMaster = local.equals(master);
            locationCache.put(rank, local);

            profiling = new Profiling(local.name());

            adminChannel = comm.createUpcallChannel("constellation_ADMIN", this); // , profiling.getTimer("pidgin", "channel_admin", ""));
            stealChannel = comm.createUpcallChannel("constellation_STEAL", this); // , profiling.getTimer("pidgin", "channel_steal", ""));
            eventChannel = comm.createUpcallChannel("constellation_EVENT", this); // , profiling.getTimer("pidgin", "channel_event", ""));

        } catch (Exception e) {
            throw new PoolCreationFailedException("Failed to create pool", e);
        }

        // Register my rank at the master
        if (!isMaster) {
            doForward(adminChannel, master, OPCODE_RANK_REGISTER_REQUEST, new RankInfo(rank, local));
            syncInfo = null;
        } else {
            syncInfo = new TimeSyncInfo(master.name());
        }

        // Start the updater thread...
        updater.start();

        if (closedPool) {
            ids = comm.getAllIdentifiers();
        }

        logger.info("Pool created");

    }

    public String getId() {
        return local.name();
    }

    public Profiling getProfiling() {
        return profiling;
    }

    public void activate() {

        try {
            adminChannel.activate();
            stealChannel.activate();
            eventChannel.activate();
        } catch (Exception e) {
            logger.error("Failed to activate", e);
        }

        if (logger.isInfoEnabled()) {
            logger.info("Activating POOL on " + local);
        }

        if (closedPool) {
            if (isMaster()) {
                for (IbisIdentifier id : ids) {
                    if (!id.equals(local)) {
                        // First do a pingpong to make sure that the other side
                        // has upcalls enabled already.
                        doForward(adminChannel, id, OPCODE_PING, null);
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
                for (IbisIdentifier id : ids) {
                    if (!id.equals(local)) {
                        doForward(adminChannel, id, OPCODE_RELEASE, null);
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
        PidginFactory.terminate();
        updater.done();
        terminated = true;
    }

    public void handleProfiling() {
        if (properties.PROFILE) { // Only if profiling.
            Profiling profiling = owner.getProfiling();
            if (isMaster) {
                profiling.setSyncInfo(syncInfo);
                if (logger.isInfoEnabled()) {
                    logger.info("waiting for profiling of other nodes");
                }

                if (closedPool) {
                    synchronized (this) {
                        int nClients = comm.getPoolSize() - 1;
                        long time = System.currentTimeMillis();
                        while (gotProfiling < nClients) {
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
                synchronized (profiling) {
                    doForward(adminChannel, master, OPCODE_PROFILING, profiling);
                }
            }
        }
    }

    public void cleanup() {
        synchronized (this) {
            cleanup = true;
        }
        updater.done();

        try {
            adminChannel.deactivate();
            stealChannel.deactivate();
            eventChannel.deactivate();

            // comm.removeChannel(CHANNEL_ADMIN);
            // comm.removeChannel(CHANNEL_STEAL);
            // comm.removeChannel(CHANNEL_EVENT);

        } catch (Exception e) {
            logger.warn("Failed to deactivate!", e);
        }
    }

    public int getRank() {
        return rank;
    }

    public boolean isMaster() {
        return isMaster;
    }

    private IbisIdentifier translate(ConstellationIdentifierImpl cid) {
        return lookupRank(cid.getNodeId());
    }

    // private boolean doForward(IbisIdentifier dest, byte opcode, Object data) {
    // Message m = new Message(opcode, data);
    // synchronized (this) {
    // if (cleanup) {
    // return true;
    // }
    // }
    // return comm.sendMessage(dest, m);
    // }

    private boolean doForward(UpcallChannel channel, IbisIdentifier dest, byte opcode, Object data) {

        synchronized (this) {
            if (cleanup) {
                return true;
            }
        }

        ByteBuffer[] buffers = null;

        if (data instanceof ByteBuffers) {

            ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>();
            ((ByteBuffers) data).pushByteBuffers(list);

            if (logger.isDebugEnabled()) {
                logger.debug("Writing " + list.size() + " bytebuffers");
            }

            // Bit of a hac, as we expect an array
            buffers = new ByteBuffer[list.size()];

            int index = 0;

            for (ByteBuffer b : list) {
                b.position(0);
                b.limit(b.capacity());
                buffers[index++] = b;
            }
        }

        try {
            channel.sendMessage(dest, opcode, data, buffers);
        } catch (Exception e) {
            logger.warn("POOL failed to forward message", e);
            return false;
        }

        return true;
    }

    public boolean forward(StealReply sr) {

        // logger.info("POOL:FORWARD StealReply from " + sr.source +
        // " to " + sr.target);

        return forward(stealChannel, sr, OPCODE_STEAL_REPLY);
    }

    public boolean forward(EventMessage em) {
        return forward(eventChannel, em, OPCODE_EVENT_MESSAGE);
    }

    private boolean forward(UpcallChannel channel, AbstractMessage m, byte opcode) {

        ConstellationIdentifierImpl target = m.target;

        if (logger.isTraceEnabled()) {
            logger.trace("POOL FORWARD Message from " + m.source + " to " + m.target + " " + m);
        }

        IbisIdentifier id = translate(target);

        if (id == null) {
            if (logger.isInfoEnabled()) {
                logger.info("POOL failed to translate " + target + " to a IbisIdentifier");
            }
            return false;
        }

        if (logger.isDebugEnabled() && opcode == OPCODE_EVENT_MESSAGE) {
            logger.debug("Sending " + m + " to " + id);
        }

        return doForward(channel, id, opcode, m);
    }

    public boolean forwardToMaster(StealRequest m) {
        return doForward(stealChannel, master, OPCODE_STEAL_REQUEST, m);
    }

    private void registerRank(RankInfo info) {
        registerRank(info.rank, info.id);
    }

    private void registerRank(int rank, IbisIdentifier id) {
        IbisIdentifier old = locationCache.put(rank, id);

        if (logger.isInfoEnabled() && old == null) {
            logger.info("Register rank " + rank + ", id = " + id);
        }

        // sanity check
        if (old != null && !old.equals(id)) {
            logger.error("Location cache overwriting rank " + rank + " with different id! " + old + " != " + id, new Throwable());
        }
    }

    private void registerRank(ConstellationIdentifierImpl cid, IbisIdentifier id) {
        registerRank(cid.getNodeId(), id);
    }

    private IbisIdentifier lookupRank(int rank) {

        // Do a local lookup
        IbisIdentifier tmp = locationCache.get(rank);

        // Return if we have a result, or if there is no one that we can ask
        if (tmp != null || isMaster) {
            return tmp;
        }

        // Forward a request to the master for the id of rank
        doForward(adminChannel, master, OPCODE_RANK_LOOKUP_REQUEST, new RankInfo(rank, local));

        return null;
    }

    private void lookupRankRequest(RankInfo info) {

        IbisIdentifier tmp = locationCache.get(info.rank);

        if (tmp == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Location lookup for rank " + rank + " returned null! Dropping reply");
            }
            // Timo: drop reply, sender will retry automatically, and does not
            // handle null replies well.
            return;
        }

        doForward(adminChannel, info.id, OPCODE_RANK_LOOKUP_REPLY, new RankInfo(info.rank, tmp));
    }

    private void getTimeOfOther(IbisIdentifier id) {
        // Send something just to set up the connection.
        doForward(adminChannel, id, OPCODE_NOTHING, null);
        if (logger.isDebugEnabled()) {
            logger.debug("Obtaining time from " + id.name());
        }
        long myTime = System.nanoTime();
        synchronized (times) {
            times.put(id, new Long(myTime));
        }
        doForward(adminChannel, id, OPCODE_REQUEST_TIME, null);
    }

    private void sendTime(long l, IbisIdentifier source) {
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
    }

    private void gotStealRequest(StealRequest m, IbisIdentifier source) {
        registerRank(m.source, source);

        if (logger.isTraceEnabled()) {
            logger.trace("POOL RECEIVE StealRequest from " + m.source);
        }

        m.setRemote();
        owner.deliverRemoteStealRequest(m);
    }

    private synchronized void gotRelease() {
        gotRelease = true;
        notifyAll();
    }

    private void gotProfiling(Profiling data, IbisIdentifier source) {
        owner.getProfiling().add(data);

        // comm.cleanup(source); // To speed up termination

        synchronized (this) {
            gotProfiling++;
            notifyAll();
        }
    }

    private void gotStealReply(StealReply m, IbisIdentifier source) {
        registerRank(m.source, source);

        if (logger.isTraceEnabled()) {
            logger.trace("POOL RECEIVE StealReply from " + m.source);
        }
        if (logger.isDebugEnabled() && m != null) {
            logger.debug("Jobs stolen from " + source.name() + ": " + m.toString());
        }

        owner.deliverRemoteStealReply(m);
    }

    private void gotEvent(EventMessage m) {
        // registerRank(m.source, source); NO! event messages can be
        // forwarded when an activation was stolen. --Ceriel

        if (logger.isInfoEnabled()) {
            logger.info("RECEIVE EventMessage; " + m);
        }

        owner.deliverRemoteEvent(m);
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

        IbisIdentifier id = info.selectRandom(random);

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
        return doForward(stealChannel, id, OPCODE_STEAL_REQUEST, sr);
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
            doForward(adminChannel, request.source, OPCODE_POOL_UPDATE_REPLY, tmp);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No updates found for pool " + request.tag + " / " + request.timestamp);
            }
        }
    }

    private void requestRegisterWithPool(IbisIdentifier master, String tag) {
        if (logger.isInfoEnabled()) {
            logger.info("Sending register request for pool " + tag + " to " + master);
        }

        doForward(adminChannel, master, OPCODE_POOL_REGISTER_REQUEST, new PoolRegisterRequest(local, tag));
    }

    private void requestUpdate(IbisIdentifier master, String tag, long timestamp) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending update request for pool " + tag + " to " + master + " for timestamp " + timestamp);
        }

        doForward(adminChannel, master, OPCODE_POOL_UPDATE_REQUEST, new PoolUpdateRequest(local, tag, timestamp));
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

            IbisIdentifier id = comm.elect(electTag);

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

            IbisIdentifier id = comm.getElectionResult(electTag, 1000);

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
        case OPCODE_PROFILING:
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

    @Override
    public ByteBuffer[] allocateByteBuffers(String channel, IbisIdentifier sender, byte opcode, Object data, int[] sizes) {

        // We should allocate the appropriate amount and sizes of ByteBuffers here.
        ByteBuffer[] buffers = new ByteBuffer[sizes.length];

        for (int i = 0; i < sizes.length; i++) {
            ByteBuffer b = ByteBufferCache.getByteBuffer(sizes[i], false);
            b.position(0);
            b.limit(b.capacity());
            buffers[i] = b;
        }

        return buffers;
    }

    @Override
    public void receiveMessage(String channel, IbisIdentifier source, byte opcode, Object data, ByteBuffer[] buffers) {

        if (logger.isDebugEnabled()) {
            logger.debug(getString(opcode, "Got") + " from " + source.name());
        }

        switch (opcode) {
        case OPCODE_NOTHING:
            break;
        case OPCODE_RELEASE:
            gotRelease();
            break;
        case OPCODE_SEND_TIME:
            sendTime(((Long) data).longValue(), source);
            return;
        case OPCODE_PROFILING:
            gotProfiling((Profiling) data, source);
            break;
        case OPCODE_REQUEST_TIME:
            doForward(adminChannel, source, OPCODE_SEND_TIME, new Long(System.nanoTime()));
            break;
        case OPCODE_PING:
            doForward(adminChannel, source, OPCODE_PONG, null);
            break;
        case OPCODE_PONG:
            synchronized (this) {
                gotPong = true;
                notifyAll();
            }
            break;
        case OPCODE_STEAL_REQUEST:
            gotStealRequest((StealRequest) data, source);
            break;

        case OPCODE_STEAL_REPLY:
            gotStealReply((StealReply) data, source);
            break;

        case OPCODE_EVENT_MESSAGE:
            gotEvent((EventMessage) data);
            break;

        case OPCODE_POOL_REGISTER_REQUEST:
            performRegisterWithPool((PoolRegisterRequest) data);
            break;

        case OPCODE_POOL_UPDATE_REQUEST:
            performUpdateRequest((PoolUpdateRequest) data);
            break;

        case OPCODE_POOL_UPDATE_REPLY:
            updater.enqueueUpdate((PoolInfo) data);
            break;

        case OPCODE_RANK_REGISTER_REQUEST:
            registerRank((RankInfo) data);
            if (!closedPool) {
                getTimeOfOther(source);
            }
            break;

        case OPCODE_RANK_LOOKUP_REPLY:
            registerRank((RankInfo) data);
            break;

        case OPCODE_RANK_LOOKUP_REQUEST:
            lookupRankRequest((RankInfo) data);
            break;

        default:
            logger.error("Received unknown message opcode: " + opcode);
            break;
        }
    }
}
