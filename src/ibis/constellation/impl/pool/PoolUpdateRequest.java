package ibis.constellation.impl.pool;

import java.io.Serializable;

import ibis.ipl.IbisIdentifier;

class PoolUpdateRequest implements Serializable {
    private static final long serialVersionUID = -4258898100133094472L;

    public IbisIdentifier source;
    public String tag;
    public long timestamp;

    PoolUpdateRequest(IbisIdentifier source, String tag, long timestamp) {
        this.source = source;
        this.tag = tag;
        this.timestamp = timestamp;
    }
}