package ibis.constellation.impl.pool;

import java.io.Serializable;

import ibis.constellation.impl.pool.communication.NodeIdentifier;

class PoolRegisterRequest implements Serializable {

    private static final long serialVersionUID = -4258898100133094472L;

    public NodeIdentifier source;
    public String tag;

    PoolRegisterRequest(NodeIdentifier source, String tag) {
        this.source = source;
        this.tag = tag;
    }
}