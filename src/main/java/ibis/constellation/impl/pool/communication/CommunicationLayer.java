package ibis.constellation.impl.pool.communication;

import java.io.IOException;

public interface CommunicationLayer {

    public NodeIdentifier getMyIdentifier();

    public boolean sendMessage(NodeIdentifier dest, Message m);

    // Message receipt is implicit, in that the upcall() method of Pool gets
    // called.

    public NodeIdentifier getMaster();

    public int getRank();

    public int getPoolSize();

    public void terminate() throws IOException;

    public void cleanup();

    public NodeIdentifier getElectionResult(String electTag, long timeout) throws IOException;

    public NodeIdentifier elect(String electTag) throws IOException;

    public void activate();

    public NodeIdentifier[] getNodeIdentifiers();
}
