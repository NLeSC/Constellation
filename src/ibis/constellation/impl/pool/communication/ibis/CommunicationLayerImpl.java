package ibis.constellation.impl.pool.communication.ibis;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.ByteBufferCache;
import ibis.constellation.ByteBuffers;
import ibis.constellation.ConstellationProperties;
import ibis.constellation.extra.CTimer;
import ibis.constellation.impl.pool.Pool;
import ibis.constellation.impl.pool.PoolCreationFailedException;
import ibis.constellation.impl.pool.communication.CommunicationLayer;
import ibis.constellation.impl.pool.communication.CommunicationMessage;
import ibis.constellation.impl.pool.communication.NodeIdentifier;
import ibis.ipl.Ibis;
import ibis.ipl.IbisCapabilities;
import ibis.ipl.IbisFactory;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.MessageUpcall;
import ibis.ipl.PortType;
import ibis.ipl.ReadMessage;
import ibis.ipl.ReceivePort;
import ibis.ipl.RegistryEventHandler;
import ibis.ipl.SendPort;
import ibis.ipl.WriteMessage;

public class CommunicationLayerImpl
        implements CommunicationLayer, RegistryEventHandler, MessageUpcall {

    private static final Logger logger = LoggerFactory
            .getLogger(CommunicationLayerImpl.class);

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
    private final ReceivePort rports[];

    private Ibis ibis;

    private final IbisIdentifier master;

    private final IbisIdentifier local;

    private final ConcurrentHashMap<IbisIdentifier, SendPort> sendports = new ConcurrentHashMap<IbisIdentifier, SendPort>();

    private long rank = -1;

    private final Pool pool;

    private final boolean closedPool;

    private IbisIdentifier[] ids = null;

    private final ConstellationProperties properties;

    private CTimer communicationTimer;

    public CommunicationLayerImpl(final ConstellationProperties properties,
            Pool pool) throws PoolCreationFailedException {

        closedPool = properties.CLOSED;
        if (closedPool) {
            if (properties.POOLSIZE > 0) {
                properties.setProperty("ibis.pool.size",
                        "" + properties.POOLSIZE);
            }
        }
        this.pool = pool;
        this.properties = properties;

        try {
            ibis = IbisFactory.createIbis(
                    closedPool ? closedIbisCapabilities : openIbisCapabilities,
                    properties, true, closedPool ? null : this, portType);

            if (!closedPool) {
                ibis.registry().enableEvents();
            }

            boolean canBeMaster = properties.MASTER;
            if (canBeMaster) {
                // Elect a server
                master = ibis.registry().elect("Constellation Master");
            } else {
                master = ibis.registry()
                        .getElectionResult("Constellation Master");
            }

            local = ibis.identifier();

            // We determine our rank here. This rank should only be used for
            // debugging purposes!
            String tmp = properties
                    .getProperty(ConstellationProperties.S_PREFIX + "rank");

            if (tmp != null) {
                try {
                    rank = Long.parseLong(tmp);
                } catch (Exception e) {
                    logger.error("Failed to parse rank: " + tmp);
                }
            }

            if (rank == -1) {
                rank = ibis.registry().getSequenceNumber(
                        "constellation-pool-" + master.toString());
            }

            rp = ibis.createReceivePort(portType, "constellation", this);
            rp.enableConnections();

            if (closedPool) {
                ibis.registry().waitUntilPoolClosed();
                ids = ibis.registry().joinedIbises();
                rports = new ReceivePort[ids.length];
                for (int i = 0; i < rports.length; i++) {
                    if (!ids[i].equals(ibis.identifier())) {
                        try {
                            rports[i] = ibis.createReceivePort(portType,
                                    "constellation_" + ids[i].name(), this);
                            rports[i].enableConnections();
                        } catch (Throwable e) {
                            logger.warn("Could not create port", e);
                        }
                    }
                }
            } else {
                rports = null;
            }
        } catch (Throwable e) {
            if (ibis != null) {
                try {
                    ibis.end();
                } catch (Throwable e1) {
                    // ignored
                }
            }
            throw new PoolCreationFailedException("Pool creation failed", e);
        }
    }

    public NodeIdentifier getMaster() {
        return new NodeIdentifierImpl(master);
    }

    @Override
    public NodeIdentifier getMyIdentifier() {
        return new NodeIdentifierImpl(local);
    }

    public int getPoolSize() {
        return ibis.registry().getPoolSize();
    }

    public void terminate() throws IOException {
        if (local.equals(master)) {
            ibis.registry().terminate();
        } else {
            ibis.registry().waitUntilTerminated();
        }
    }

    public void cleanup() {
        // Try to cleanly disconnect all send and receive ports....
        if (logger.isInfoEnabled()) {
            logger.info("disabling receive port");
        }
        try {
            rp.disableConnections();
            rp.disableMessageUpcalls();
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("Clean receive port got execption", e);
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Closing send ports");
        }
        for (SendPort sp : sendports.values()) {
            try {
                sp.close();
            } catch (Exception e) {
                if (logger.isInfoEnabled()) {
                    logger.info("Close sendport got execption", e);
                }
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Closing receive ports");
        }
        try {
            rp.close(10000);
        } catch (IOException e) {
            if (logger.isInfoEnabled()) {
                logger.info("Close receive port got execption", e);
            }
        }
        if (rports != null) {
            for (int i = 0; i < rports.length; i++) {
                if (rports[i] != null) {
                    try {
                        rports[i].close(10000);
                    } catch (IOException e) {
                        if (logger.isInfoEnabled()) {
                            logger.info("Close receive port " + rports[i].name()
                                    + " got execption", e);
                        }
                    }
                }
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Ending ibis");
        }
        try {
            ibis.end();
        } catch (IOException e) {
            if (logger.isInfoEnabled()) {
                logger.info("ibis.end() got execption", e);
            }
        }
    }

    @Override
    public boolean sendMessage(NodeIdentifier destination,
            CommunicationMessage m) {
        SendPort s;
        IbisIdentifier dest = ((NodeIdentifierImpl) destination)
                .getIbisIdentifier();
        try {
            s = getSendPort(dest);
        } catch (IOException e1) {
            logger.warn("POOL failed to connect to " + dest, e1);
            return false;
        }

        int eventNo = -1;
        long sz = 0;
        WriteMessage wm = null;
        try {
            wm = s.newMessage();
            String name = Pool.getString(m.opcode, "write");
            if (communicationTimer != null && m.contents != null) {
                eventNo = communicationTimer.start(name);
            }
            wm.writeByte(m.opcode);
            if (m.contents == null) {
                wm.writeBoolean(false);
            } else {
                wm.writeBoolean(true);
                wm.writeObject(m.contents);
                if (m.contents instanceof ByteBuffers) {
                    wm.flush();
                    ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>();
                    ((ByteBuffers) m.contents).pushByteBuffers(list);
                    if (logger.isInfoEnabled()) {
                        logger.info("Writing " + list.size() + " bytebuffers");
                    }
                    wm.writeInt(list.size());
                    for (ByteBuffer b : list) {
                        b.position(0);
                        b.limit(b.capacity());
                        wm.writeInt(b.capacity());
                    }
                    for (ByteBuffer b : list) {
                        wm.writeByteBuffer(b);
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "Wrote bytebuffer of size " + b.capacity());
                        }
                    }
                }
            }
            sz = wm.finish();
            if (eventNo != -1) {
                communicationTimer.stop(eventNo);
                communicationTimer.addBytes(sz, eventNo);
            }
        } catch (IOException e) {
            logger.warn("Communication to " + dest + " gave exception", e);
            if (wm != null) {
                wm.finish(e);
            }
            if (eventNo != -1) {
                communicationTimer.cancel(eventNo);
            }
            return false;
        }

        return true;
    }

    @Override
    public void upcall(ReadMessage rm)
            throws IOException, ClassNotFoundException {

        IbisIdentifier source = rm.origin().ibisIdentifier();
        int timerEvent = -1;
        byte opcode = rm.readByte();
        boolean hasObject = rm.readBoolean();

        if (communicationTimer != null && hasObject) {
            timerEvent = communicationTimer
                    .start(Pool.getString(opcode, "read"));
        }
        CommunicationMessage m = new CommunicationMessage(opcode, null);

        if (hasObject) {
            long sz = -1;
            try {
                m.contents = rm.readObject();
                if (m.contents != null && m.contents instanceof ByteBuffers) {
                    int nByteBuffers = rm.readInt();
                    ArrayList<ByteBuffer> l = new ArrayList<ByteBuffer>();
                    if (nByteBuffers > 0) {
                        if (logger.isInfoEnabled()) {
                            logger.info(
                                    "Reading " + nByteBuffers + " bytebuffers");
                        }
                        for (int i = 0; i < nByteBuffers; i++) {
                            int capacity = rm.readInt();
                            ByteBuffer b = ByteBufferCache
                                    .getByteBuffer(capacity, false);
                            l.add(b);
                        }
                        for (ByteBuffer b : l) {
                            b.position(0);
                            b.limit(b.capacity());
                            rm.readByteBuffer(b);
                        }
                    }
                    ((ByteBuffers) m.contents).popByteBuffers(l);
                }
                sz = rm.finish();
            } finally {
                if (timerEvent != -1) {
                    if (m.contents == null) {
                        communicationTimer.cancel(timerEvent);
                    } else {
                        communicationTimer.stop(timerEvent);
                        communicationTimer.addBytes(sz, timerEvent);
                    }
                }
            }
        } else {
            rm.finish();
        }
        pool.upcall(new NodeIdentifierImpl(source), m);
    }

    @Override
    public long getRank() {
        return rank;
    }

    @Override
    public void died(IbisIdentifier id) {
        left(id);
    }

    @Override
    public void electionResult(String arg0, IbisIdentifier arg1) {
        // ignored

    }

    @Override
    public void gotSignal(String arg0, IbisIdentifier arg1) {
        // ignored

    }

    @Override
    public void joined(IbisIdentifier arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void left(IbisIdentifier arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void poolClosed() {
        // ignored

    }

    @Override
    public void poolTerminated(IbisIdentifier arg0) {
        // ignored

    }

    private SendPort getSendPort(IbisIdentifier id) throws IOException {

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
            } catch (IOException e) {
                try {
                    sp.close();
                } catch (Throwable e2) {
                    // ignored ?
                }
                if (closedPool) {
                    try {
                        sp = ibis.createSendPort(portType);
                        sp.connect(id, "constellation");
                    } catch (IOException e1) {
                        try {
                            sp.close();
                        } catch (Throwable e2) {
                            // ignored ?
                        }
                        logger.error("Could not connect to " + id.name(), e1);
                        throw e1;
                    }
                } else {
                    logger.error("Could not connect to " + id.name(), e);
                    throw e;
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

    public void activate() {

        if (properties.PROFILE_COMMUNICATION) {
            communicationTimer = pool.getStats().getTimer("java",
                    "data handling", "read/write data");
        } else {
            communicationTimer = null;
        }

        rp.enableMessageUpcalls();
        if (closedPool) {
            for (int i = 0; i < rports.length; i++) {
                if (rports[i] != null) {
                    rports[i].enableMessageUpcalls();
                }
            }
        }
    }

    @Override
    public NodeIdentifier getElectionResult(String electTag, long timeout)
            throws IOException {
        IbisIdentifier id = ibis.registry().getElectionResult(electTag,
                timeout);
        if (id != null) {
            return new NodeIdentifierImpl(id);
        }
        return null;
    }

    @Override
    public NodeIdentifier elect(String electTag) throws IOException {
        return new NodeIdentifierImpl(ibis.registry().elect(electTag));
    }

    @Override
    public NodeIdentifier[] getNodeIdentifiers() {
        if (!closedPool) {
            return null;
        }
        NodeIdentifier[] result = new NodeIdentifier[ids.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new NodeIdentifierImpl(ids[i]);
        }
        return result;
    }

}
