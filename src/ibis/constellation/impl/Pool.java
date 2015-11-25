package ibis.constellation.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.CTimer;
import ibis.constellation.ConstellationIdentifier;
import ibis.constellation.ObjectData;
import ibis.constellation.Stats;
import ibis.constellation.StealPool;
import ibis.constellation.extra.Debug;
import ibis.constellation.extra.TimeSyncInfo;
import ibis.ipl.Ibis;
import ibis.ipl.IbisCapabilities;
import ibis.ipl.IbisFactory;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.MessageUpcall;
import ibis.ipl.PortType;
import ibis.ipl.ReadMessage;
import ibis.ipl.ReceivePort;
import ibis.ipl.Registry;
import ibis.ipl.RegistryEventHandler;
import ibis.ipl.SendPort;
import ibis.ipl.WriteMessage;
import ibis.util.TypedProperties;

public class Pool implements RegistryEventHandler, MessageUpcall {

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

    private final PortType portType = new PortType(PortType.COMMUNICATION_FIFO,
            PortType.COMMUNICATION_RELIABLE, PortType.SERIALIZATION_OBJECT,
            PortType.RECEIVE_AUTO_UPCALLS, PortType.RECEIVE_TIMEOUT,
            PortType.CONNECTION_MANY_TO_ONE);

    private static final IbisCapabilities openIbisCapabilities = new IbisCapabilities(
            IbisCapabilities.MALLEABLE, IbisCapabilities.TERMINATION,
            IbisCapabilities.ELECTIONS_STRICT,
            IbisCapabilities.MEMBERSHIP_TOTALLY_ORDERED);
    private static final IbisCapabilities closedIbisCapabilities = new IbisCapabilities(
            IbisCapabilities.CLOSED_WORLD, IbisCapabilities.TERMINATION,
            IbisCapabilities.ELECTIONS_STRICT,
            IbisCapabilities.MEMBERSHIP_TOTALLY_ORDERED);

    private final ReceivePort rp;

    private final ConcurrentHashMap<IbisIdentifier, SendPort> sendports = new ConcurrentHashMap<IbisIdentifier, SendPort>();

    private final ConcurrentHashMap<Integer, IbisIdentifier> locationCache = new ConcurrentHashMap<Integer, IbisIdentifier>();

    private final DistributedConstellationIdentifierFactory cidFactory;

    private final Ibis ibis;
    private final IbisIdentifier local;
    private final IbisIdentifier master;

    private long rank = -1;

    private boolean isMaster;

    private final Random random = new Random();

    private final boolean closedPool;

    private final CTimer communicationTimer;

    // private long received;
    // private long send;

    // private boolean active = false;

    // private StealPoolInfo poolInfo = new StealPoolInfo();

    private LinkedList<Message> pending = new LinkedList<Message>();

    private final HashMap<IbisIdentifier, Long> times = new HashMap<IbisIdentifier, Long>();
    private final TimeSyncInfo syncInfo;

    class PoolUpdater extends Thread {

        private static final long MIN_DELAY = 1000;
        private static final long MAX_DELAY = 10000;

        private long deadline = 0;
        private long currentDelay = MIN_DELAY;
        private boolean done;

        private ArrayList<String> tags = new ArrayList<String>();
        private ArrayList<PoolInfo> updates = new ArrayList<PoolInfo>();

        public synchronized void addTag(String tag) {
            if (!tags.contains(tag)) {
                tags.add(tag);
            }
        }

        public synchronized String[] getTags() {
            return tags.toArray(new String[tags.size()]);
        }

        public synchronized void enqueueUpdate(PoolInfo info) {
            updates.add(info);
            notifyAll();
        }

        public synchronized PoolInfo dequeueUpdate() {

            if (updates.size() == 0) {
                return null;
            }

            // Dequeue in LIFO order too prevent unnecessary updates
            return updates.remove(updates.size() - 1);
        }

        private synchronized boolean getDone() {
            return done;
        }

        public synchronized void done() {
            done = true;
        }

        private void processUpdates() {

            PoolInfo update = dequeueUpdate();

            if (update == null) {
                // No updates
                // currentDelay += MIN_DELAY;

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

            for (int i = 0; i < pools.length; i++) {
                requestUpdate(pools[i]);
            }
        }

        private void waitUntilDeadLine() {

            long sleep = deadline - System.currentTimeMillis();

            if (sleep > 0) {
                try {
                    synchronized (this) {
                        wait(sleep);
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        @Override
        public void run() {

            while (!getDone()) {

                processUpdates();

                long now = System.currentTimeMillis();

                if (now >= deadline) {
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
    private Stats stats;

    private int gotStats;
    private final boolean limitSenders;

    public Pool(final DistributedConstellation owner, final Properties p)
            throws Exception {

        TypedProperties properties = new TypedProperties(p);
        this.owner = owner;
        closedPool = properties.getBooleanProperty("ibis.constellation.closed",
                false);
        limitSenders = properties
                .getBooleanProperty("ibis.constellation.limitSenders", false);
        ibis = IbisFactory.createIbis(
                closedPool ? closedIbisCapabilities : openIbisCapabilities, p,
                true, closedPool ? null : this, portType);

        local = ibis.identifier();

        if (!closedPool) {
            ibis.registry().enableEvents();
        }

        if (limitSenders) {
            ibis.registry().addTokens(getId(), 1);
        }

        String tmp = properties.getProperty("ibis.constellation.master",
                "auto");

        if (tmp.equalsIgnoreCase("auto") || tmp.equalsIgnoreCase("true")) {
            // Elect a server
            master = ibis.registry().elect("Constellation Master");
        } else if (tmp.equalsIgnoreCase("false")) {
            master = ibis.registry().getElectionResult("Constellation Master");
        } else {
            master = null;
        }

        if (master == null) {
            throw new Exception("Failed to find master!");
        }

        // We determine our rank here. This rank should only be used for
        // debugging purposes!
        tmp = System.getProperty("ibis.constellation.rank");

        if (tmp != null) {
            try {
                rank = Long.parseLong(tmp);
            } catch (Exception e) {
                logger.error("Failed to parse rank: " + tmp);
                rank = -1;
            }
        }

        if (rank == -1) {
            rank = ibis.registry().getSequenceNumber(
                    "constellation-pool-" + master.toString());
        }

        isMaster = local.equals(master);

        rp = ibis.createReceivePort(portType, "constellation", this);
        rp.enableConnections();

        // MOVED: to activate
        // rp.enableMessageUpcalls();

        cidFactory = new DistributedConstellationIdentifierFactory(rank);

        locationCache.put((int) rank, local);

        // Register my rank at the master
        if (!isMaster) {
            doForward(master, OPCODE_RANK_REGISTER_REQUEST,
                    new RankInfo((int) rank, local));
            syncInfo = null;
        } else {
            syncInfo = new TimeSyncInfo(master.name());
        }

        // Start the updater thread...
        updater.start();
        if (closedPool) {
            ibis.registry().waitUntilPoolClosed();
        }

        stats = new Stats(getId());

        communicationTimer = stats.getTimer("java", "data receiver",
                "receive data");
    }

    Stats getStats() {
        return stats;
    }

    public void activate() {
        if (logger.isInfoEnabled()) {
            logger.info("Activating POOL on " + ibis.identifier());
        }
        // synchronized (this) {
        // active = true;
        // }

        // processPendingMessages();
        rp.enableMessageUpcalls();
        if (closedPool) {
            IbisIdentifier[] ids = ibis.registry().joinedIbises();
            ReceivePort[] ports = new ReceivePort[ids.length];
            for (int i = 0; i < ports.length; i++) {
                if (!ids[i].equals(ibis.identifier())) {
                    try {
                        ports[i] = ibis.createReceivePort(portType,
                                "constellation_" + ids[i].name(), this);
                        ports[i].enableConnections();
                        ports[i].enableMessageUpcalls();
                    } catch (Throwable e) {
                        logger.warn("Could not create port", e);
                    }
                }
            }
            if (isMaster()) {
                for (IbisIdentifier id : ids) {
                    if (!id.equals(ibis.identifier())) {
                        // First do a pingpong to make sure that the other side
                        // has
                        // upcalls enabled already.
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
                for (IbisIdentifier id : ids) {
                    if (!id.equals(ibis.identifier())) {
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

    private void processPendingMessages() {

        while (pending.size() > 0) {
            Message m = pending.removeFirst();

            if (m instanceof StealRequest) {
                if (logger.isInfoEnabled()) {
                    logger.info("POOL processing PENDING StealRequest from "
                            + m.source);
                }
                owner.deliverRemoteStealRequest((StealRequest) m);

            } else if (m instanceof EventMessage) {
                if (logger.isInfoEnabled()) {
                    logger.info(
                            "POOL processing PENDING ApplicationMessage from "
                                    + m.source);
                }
                owner.deliverRemoteEvent((EventMessage) m);
                /*
                 * } else if (m instanceof UndeliverableEvent) {
                 *
                 * logger.warn(
                 * "POOL processing PENDING UndeliverableEvent from " +
                 * m.source);
                 * owner.deliverUndeliverableEvent((UndeliverableEvent)m);
                 */
            } else {
                // Should never happen!
                logger.warn("POOL DROP unknown pending message ! " + m);
            }
        }
    }

    /*
     * public synchronized CohortIdentifier generateCohortIdentifier(int worker)
     * { return new DistributedCohortIdentifier(local, rank, worker); }
     *///

    public DistributedConstellationIdentifierFactory getCIDFactory() {
        return cidFactory;
    }

    public boolean isLocal(ConstellationIdentifier id) {
        return (rank << 32 ^ id.id) == 0;
    }

    private SendPort getSendPort(IbisIdentifier id) {

        // TODO: not fault tolerant!!!
        if (id.equals(ibis.identifier())) {
            logger.error("POOL Sending to myself!", new Throwable());
        }

        SendPort sp = sendports.get(id);

        if (sp == null) {
            if (logger.isInfoEnabled()) {
                logger.info(
                        "Connecting to " + id + " from " + ibis.identifier());
            }
            try {
                sp = ibis.createSendPort(portType);
                if (closedPool) {
                    sp.connect(id, "constellation_" + ibis.identifier().name(),
                            10000, true);
                } else {
                    sp.connect(id, "constellation");
                }
            } catch (Throwable e) {
                try {
                    sp.close();
                } catch (Throwable e2) {
                    // ignored ?
                }
                if (closedPool) {
                    try {
                        sp = ibis.createSendPort(portType);
                        sp.connect(id, "constellation");
                    } catch (Throwable e1) {
                        try {
                            sp.close();
                        } catch (Throwable e2) {
                            // ignored ?
                        }
                        logger.error("Could not connect to " + id.name(), e1);
                    }
                } else {
                    logger.error("Could not connect to " + id.name(), e);
                }
            }

            if (logger.isInfoEnabled()) {
                logger.info("Succesfully connected to " + id + " from "
                        + ibis.identifier());
            }

            SendPort sp2 = sendports.putIfAbsent(id, sp);

            if (sp2 != null) {
                // Someone managed to sneak in between our get and put!
                try {
                    sp.close();
                } catch (Exception e) {
                    // ignored
                }

                sp = sp2;
            }
        }

        return sp;
    }

    /*
     * private void releaseSendPort(IbisIdentifier id, SendPort sp) { // empty }
     */

    @Override
    public void died(IbisIdentifier id) {
        left(id);
    }

    @Override
    public void electionResult(String name, IbisIdentifier winner) {
        // ignored ?
    }

    @Override
    public void gotSignal(String signal, IbisIdentifier source) {
        // ignored
    }

    @Override
    public void joined(IbisIdentifier id) {

        // synchronized (others) {
        // if (!id.equals(local)) {
        // others.add(id);
        // logger.warn("JOINED " + id);
        // }
        // }
    }

    @Override
    public void left(IbisIdentifier id) {

        // FIXME: cleanup!
        // sendports.remove(id);
    }

    @Override
    public void poolClosed() {
        // ignored
    }

    @Override
    public void poolTerminated(IbisIdentifier id) {
        // ignored
    }

    public void terminate() throws IOException {
        if (isMaster) {
            ibis.registry().terminate();
        } else {
            ibis.registry().waitUntilTerminated();
        }
    }

    public void handleStats() {
        Stats stats = owner.getStats();
        if (isMaster) {
            stats.setSyncInfo(syncInfo);
            if (logger.isInfoEnabled()) {
                logger.info("waiting for stats of other nodes");
            }

            if (closedPool) {
                synchronized (this) {
                    int nClients = ibis.registry().getPoolSize() - 1;
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
                    Thread.sleep(5000);
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

    public void cleanup() {
        updater.done();
        try {
            ibis.end();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getRank() {
        return rank;
    }

    public boolean isMaster() {
        return isMaster;
    }

    /*
     * private IbisIdentifier selectRandomTarget() {
     *
     * synchronized (others) {
     *
     * int size = others.size();
     *
     * if (size == 0) { return null; }
     *
     * return others.get(random.nextInt(size)); } }
     */

    private IbisIdentifier translate(ConstellationIdentifier cid) {
        int rank = (int) ((cid.id >> 32) & 0xffffffff);
        return lookupRank(rank);
    }

    private boolean doForward(IbisIdentifier id, byte opcode, Object data) {

        SendPort s = getSendPort(id);

        if (s == null) {
            logger.warn("POOL failed to connect to " + id);
            return false;
        }
        int eventNo = -1;
        long sz = 0;
        try {
            if (limitSenders) {
                if (opcode == OPCODE_EVENT_MESSAGE) {
                    long start = System.currentTimeMillis();
                    String token = ibis.registry().getToken(id.name());
                    while (token == null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Getting token " + id.name());
                        }
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                        token = ibis.registry().getToken(id.name());
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Got token " + token + " after "
                                + (System.currentTimeMillis() - start)
                                + " ms.");
                    }
                    /*
                     * } else if (opcode == OPCODE_STEAL_REQUEST) { String token
                     * = ibis.registry().getToken(id.name()); if (token == null)
                     * { return false; }
                     */
                }
            }
            WriteMessage wm = s.newMessage();
            String name = getString(opcode, "write");

            // boolean mustStartTimer = (opcode == OPCODE_STEAL_REPLY || opcode
            // == OPCODE_EVENT_MESSAGE);
            boolean mustStartTimer = name != null && communicationTimer != null;
            if (opcode == OPCODE_STEAL_REPLY) {
                StealReply r = (StealReply) data;
                if (r.getSize() == 0) {
                    mustStartTimer = false;
                }
            }
            if (mustStartTimer) {
                eventNo = communicationTimer.start(name);
            }
            wm.writeByte(opcode);
            wm.writeObject(data);
            if (data != null && data instanceof ObjectData) {
                wm.flush();
                ((ObjectData) data).writeData(wm);
            }
            sz = wm.finish();
            if (eventNo != -1) {
                if (logger.isDebugEnabled() && opcode == OPCODE_STEAL_REPLY) {
                    StealReply r = (StealReply) data;
                    logger.debug("Gave " + r.getSize() + " jobs away");
                }
                communicationTimer.stop(eventNo);
                communicationTimer.addBytes(sz, eventNo);
            }
        } catch (Throwable e) {
            logger.warn("POOL lost communication to " + id, e);
            if (eventNo != -1) {
                communicationTimer.cancel(eventNo);
            }
            return false;
        } finally {
            if (limitSenders && (opcode == OPCODE_EVENT_MESSAGE /*
                                                                 * || opcode ==
                                                                 * OPCODE_STEAL_REQUEST
                                                                 */)) {
                try {
                    ibis.registry().addTokens(id.name(), 1);
                } catch (Throwable e) {
                    logger.warn("Got exception when returning token", e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Gave token " + id.name() + " back");
                }
            }
        }

        return true;
    }

    public boolean forward(StealReply sr) {

        // logger.info("POOL:FORWARD StealReply from " + sr.source +
        // " to " + sr.target);

        return forward(sr, OPCODE_STEAL_REPLY);
    }

    public boolean forward(EventMessage em) {

        // logger.info("POOL:FORWARD EventMessage from " + em.source +
        // " to " + em.target + " target " + em.event.target);

        return forward(em, OPCODE_EVENT_MESSAGE);
    }

    private boolean forward(Message m, byte opcode) {

        ConstellationIdentifier target = m.target;

        if (Debug.DEBUG_COMMUNICATION && logger.isInfoEnabled()) {
            logger.info("POOL FORWARD Message from " + m.source + " to "
                    + m.target + " " + m);
        }

        IbisIdentifier id = translate(target);

        if (id == null) {
            logger.warn("POOL failed to translate " + target
                    + " to an IbisIdentifier");
            return false;
        }

        return doForward(id, opcode, m);
    }

    public boolean forwardToMaster(StealRequest m) {
        return doForward(master, OPCODE_STEAL_REQUEST, m);
    }

    /*
     * public boolean randomForward(Message m) {
     *
     * IbisIdentifier rnd = selectRandomTarget();
     *
     * if (rnd == null) { logger.warning(
     * "POOL failed to randomly select target: " + "no other cohorts found");
     * return false; }
     *
     * forward(rnd, OPCODE_MESSAGE, m);
     *
     * return true; }
     */

    public ConstellationIdentifier selectTarget() {
        return null;
    }

    /*
     * private void addPendingMessage(Message m) { pending.add(m); }
     */

    private void registerRank(RankInfo info) {
        registerRank(info.rank, info.id);
    }

    private void registerRank(int rank, IbisIdentifier id) {
        IbisIdentifier old = locationCache.put(rank, id);

        // sanity check
        if (old != null && !old.equals(id)) {
            logger.error("Location cache overwriting rank " + rank
                    + " with different id! " + old + " != " + id);
        }
    }

    private void registerRank(ConstellationIdentifier cid, IbisIdentifier id) {
        int rank = (int) ((cid.id >> 32) & 0xffffffff);
        registerRank(rank, id);
    }

    public IbisIdentifier lookupRank(int rank) {

        // Do a local lookup
        IbisIdentifier tmp = locationCache.get(rank);

        // Return if we have a result, or if there is no one that we can ask
        if (tmp != null || isMaster) {
            return tmp;
        }

        // Forward a request to the master for the 'IbisID' of 'rank'
        doForward(master, OPCODE_RANK_LOOKUP_REQUEST,
                new RankInfo(rank, local));

        return null;
    }

    private void lookupRankRequest(RankInfo info) {

        IbisIdentifier tmp = locationCache.get(info.rank);

        if (tmp == null) {
            logger.warn("Location lookup for rank " + rank
                    + " returned null! Dropping reply");
            // Timo: drop reply, sender will retry automatically, and does not
            // handle null replies well.
            return;
        }

        doForward(info.id, OPCODE_RANK_LOOKUP_REPLY,
                new RankInfo(info.rank, tmp));
    }

    public void getTimeOfOther(IbisIdentifier id) {
        // Send something just to set up the connection.
        doForward(id, OPCODE_NOTHING, null);
        if (logger.isDebugEnabled()) {
            logger.debug("Obtaining time from " + id.name());
        }
        long myTime = System.nanoTime();
        times.put(id, new Long(myTime));
        doForward(id, OPCODE_REQUEST_TIME, null);
    }

    @Override
    public void upcall(ReadMessage rm)
            throws IOException, ClassNotFoundException {

        int timerEvent = -1;
        byte opcode = rm.readByte();

        if (opcode == OPCODE_STEAL_REPLY || opcode == OPCODE_EVENT_MESSAGE) {
            timerEvent = communicationTimer.start(getString(opcode, "read"));
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

        long sz = -1;
        IbisIdentifier source = rm.origin().ibisIdentifier();
        Object data = null;
        try {
            data = rm.readObject();
            if (data != null && data instanceof ObjectData) {
                ((ObjectData) data).readData(rm);
            }

            if (opcode == OPCODE_SEND_TIME) {
                long l = ((Long) data).longValue();
                Long myTime = times.get(source);
                if (myTime == null) {
                    logger.warn("Ignored roque time answer");
                    return;
                }
                long interval = (System.nanoTime() - myTime.longValue());
                long half = interval / 2;
                long offset = myTime.longValue() + half - l;
                if (logger.isDebugEnabled()) {
                    logger.debug("source = " + source.name() + ", offset = "
                            + offset + ", interval = " + interval);
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

            sz = rm.finish();
        } finally {
            if (timerEvent != -1) {
                if (opcode == OPCODE_STEAL_REPLY && (data == null
                        || ((StealReply) data).getSize() == 0)) {
                    communicationTimer.cancel(timerEvent);
                } else {
                    communicationTimer.stop(timerEvent);
                    communicationTimer.addBytes(sz, timerEvent);
                    if (logger.isDebugEnabled()
                            && opcode == OPCODE_STEAL_REPLY) {
                        logger.debug("Jobs stolen from " + source.name() + ": "
                                + ((StealReply) data).toString());
                    }
                }
            }
        }

        switch (opcode) {
        case OPCODE_STATISTICS:
            owner.getStats().add((Stats) data);
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

            if (Debug.DEBUG_COMMUNICATION || Debug.DEBUG_STEAL) {
                logger.info("POOL RECEIVE StealRequest from " + m.source);
            }

            m.setRemote();
            owner.deliverRemoteStealRequest(m);
        }
            break;

        case OPCODE_STEAL_REPLY: {
            StealReply m = (StealReply) data;
            registerRank(m.source, source);

            if (Debug.DEBUG_COMMUNICATION || Debug.DEBUG_STEAL) {
                logger.info("POOL RECEIVE StealReply from " + m.source);
            }

            owner.deliverRemoteStealReply(m);
        }
            break;

        case OPCODE_EVENT_MESSAGE: {
            EventMessage m = (EventMessage) data;
            registerRank(m.source, source);

            if (Debug.DEBUG_COMMUNICATION || Debug.DEBUG_EVENTS) {
                logger.info("POOL RECEIVE EventMessage from " + m.source);
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
        }

        /*
         * if (m instanceof CombinedMessage) {
         *
         * Message [] messages = ((CombinedMessage)m).getMessages();
         *
         * for (int i=0;i<messages.length;i++) { boolean rmFinished =
         * handleMessage(messages[i], rm);
         *
         * if (rmFinished) { rm = null; } } } else { handleMessage(m, rm); }
         */
    }

    /*
     * public void broadcast(Message m) {
     *
     * // This seems to produce many problems... int size = 0;
     *
     * synchronized (others) { size = others.size(); }
     *
     * for (int i=0;i<size;i++) {
     *
     * IbisIdentifier tmp = null;
     *
     * synchronized (others) { if (i < others.size()) { tmp = others.get(i); } }
     *
     * if (tmp == null) { logger.warning("POOL failed to retrieve Ibis " + i); }
     * else { forward(tmp, OPCODE_MESSAGE, m); } } }
     */

    public boolean randomForwardToPool(StealPool pool, StealRequest sr) {

        // NOTE: We know the pool is not NULL or NONE, and not a set
        PoolInfo info = null;

        synchronized (pools) {
            info = pools.get(pool.getTag());
        }

        if (info == null) {
            logger.warn("Failed to randomly select node in pool "
                    + pool.getTag() + ", pool does not exist?");
            return false;
        }

        IbisIdentifier id = info.selectRandom(random);

        if (id == null) {
            logger.warn(
                    "Failed to randomly select node in pool " + pool.getTag());
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

    public StealPool randomlySelectPool(StealPool pool) {

        // NOTE: We know the pool is not NULL or NONE.
        if (pool.isSet()) {
            StealPool[] tmp = pool.set();
            pool = tmp[random.nextInt(tmp.length)];
        }

        return pool;
    }

    private void performRegisterWithPool(PoolRegisterRequest request) {

        PoolInfo tmp = null;

        if (logger.isInfoEnabled()) {
            logger.info("Processing register request " + request.tag + " from "
                    + request.source);
        }

        synchronized (pools) {
            tmp = pools.get(request.tag);
        }

        if (tmp == null) {
            logger.error("Failed to find pool " + request.tag + " to register "
                    + request.source);
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
            logger.warn("Failed to find pool " + request.tag
                    + " for update request from " + request.source);
            return;
        }

        if (tmp.currentTimeStamp() > request.timestamp) {
            doForward(request.source, OPCODE_POOL_UPDATE_REPLY, tmp);
        } else {
            logger.info("No updates found for pool " + request.tag + " / "
                    + request.timestamp);
        }
    }

    private void requestRegisterWithPool(IbisIdentifier master, String tag) {
        if (logger.isInfoEnabled()) {
            logger.info("Sending register request for pool " + tag + " to "
                    + master);
        }

        doForward(master, OPCODE_POOL_REGISTER_REQUEST,
                new PoolRegisterRequest(local, tag));
    }

    private void requestUpdate(IbisIdentifier master, String tag,
            long timestamp) {
        if (logger.isInfoEnabled()) {
            logger.info("Sending update request for pool " + tag + " to "
                    + master + " for timestamp " + timestamp);
        }

        doForward(master, OPCODE_POOL_UPDATE_REQUEST,
                new PoolUpdateRequest(local, tag, timestamp));
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

            // Next, elect a master for this pool.
            Registry reg = ibis.registry();

            String electTag = "STEALPOOL$" + tag;

            logger.info("Electing master for POOL " + electTag);

            IbisIdentifier id = reg.elect(electTag);

            boolean master = id.equals(ibis.identifier());

            logger.info(
                    "Master for POOL " + electTag + " is " + id + " " + master);

            // Next, create the pool locally, or register ourselves at the
            // master.
            if (master) {

                synchronized (pools) {
                    PoolInfo info = pools.get(tag);

                    if (info.hasMembers()) {
                        logger.warn(
                                "Hit race in pool registration! -- will recover!");
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
                        if (!info.isDummy) {
                            logger.error(
                                    "INTERNAL ERROR: Removed non-dummy PoolInfo!");
                        }
                    } else {
                        logger.warn("Failed to find dummy PoolInfo!");
                    }
                }

                requestRegisterWithPool(id, tag);
            }

        } catch (IOException e) {
            logger.warn("Failed to register pool " + tag, e);
            e.printStackTrace(System.err);
        }
    }

    public void followPool(String tag) {

        try {
            // Simple case: we own the pool or already follow it.
            synchronized (pools) {
                if (pools.containsKey(tag)) {
                    return;
                }
            }

            // Complex case: we are not part of the pool, but interested anyway
            Registry reg = ibis.registry();

            String electTag = "STEALPOOL$" + tag;

            IbisIdentifier id = null;

            // TODO: will repeat for ever if pool master does not exist...
            while (id == null) {
                if (logger.isDebugEnabled()) {
                    logger.info("Searching master for POOL " + electTag);
                }
                id = reg.getElectionResult(electTag, 1000);
            }

            boolean master = id.equals(ibis.identifier());

            logger.info("Found master for POOL " + electTag + " " + id + " "
                    + master);

            if (master) {
                // Assuming the pools are static and registered in
                // the right order this should not happen
                logger.error(
                        "INTERNAL ERROR: election of follow pool returned self!");
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
            PoolInfo tmp = pools.get(info.tag);

            if (tmp == null) {
                logger.warn("Received spurious pool update! " + info.tag);
                return;
            }

            if (info.currentTimeStamp() > tmp.currentTimeStamp()) {
                pools.put(info.tag, info);
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

            if (tmp == null || tmp.isDummy) {
                logger.warn(
                        "Cannot request update for " + tag + ": unknown pool!");
                return;
            }

        }

        requestUpdate(tmp.master, tag, tmp.currentTimeStamp());
    }

    public String getId() {
        return local.name();
    }

    public String getString(int opcode, String readOrWrite) {
        switch (opcode) {
        case OPCODE_EVENT_MESSAGE:
            return readOrWrite + " event message";
        case OPCODE_STEAL_REQUEST:
            return readOrWrite + " steal request";
        case OPCODE_STEAL_REPLY:
            return readOrWrite + " steal reply";
        default:
            return readOrWrite + " other";
        }
    }

}
