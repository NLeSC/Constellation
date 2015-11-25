package ibis.constellation.impl;

import ibis.ipl.IbisIdentifier;

import java.io.Serializable;

public class RankInfo implements Serializable {

    private static final long serialVersionUID = 7620973089142583450L;

    public int rank;
    public IbisIdentifier id;

    public RankInfo(int rank, IbisIdentifier id) {
        this.rank = rank;
        this.id = id;
    }
}
