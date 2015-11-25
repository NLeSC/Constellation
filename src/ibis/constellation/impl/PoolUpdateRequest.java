package ibis.constellation.impl;

import ibis.ipl.IbisIdentifier;

import java.io.Serializable;

public class PoolUpdateRequest implements Serializable {
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