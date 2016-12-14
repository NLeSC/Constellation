package ibis.constellation.impl.pool;

import java.io.Serializable;

import ibis.ipl.IbisIdentifier;

class RankInfo implements Serializable {

    private static final long serialVersionUID = 7620973089142583450L;

    public final int rank;
    public final IbisIdentifier id;

    public RankInfo(int rank, IbisIdentifier id) {
        this.rank = rank;
        this.id = id;
    }
}