package ibis.constellation.impl.pool;

import java.io.Serializable;

import ibis.constellation.impl.pool.communication.NodeIdentifier;

class PoolUpdateRequest implements Serializable {
    private static final long serialVersionUID = -4258898100133094472L;

    public NodeIdentifier source;
    public String tag;
    public long timestamp;

    PoolUpdateRequest(NodeIdentifier source, String tag, long timestamp) {
        this.source = source;
        this.tag = tag;
        this.timestamp = timestamp;
    }
}