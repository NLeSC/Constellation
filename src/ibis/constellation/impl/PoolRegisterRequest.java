package ibis.constellation.impl;

import ibis.ipl.IbisIdentifier;

import java.io.Serializable;

public class PoolRegisterRequest implements Serializable {

    private static final long serialVersionUID = -4258898100133094472L;

    public IbisIdentifier source;
    public String tag;

    PoolRegisterRequest(IbisIdentifier source, String tag) {
        this.source = source;
        this.tag = tag;
    }
}