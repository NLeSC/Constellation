package ibis.constellation.impl.pool;

import java.io.Serializable;

import ibis.constellation.impl.pool.communication.NodeIdentifier;

public class RankInfo implements Serializable {

    private static final long serialVersionUID = 7620973089142583450L;

    public final int rank;
    public final NodeIdentifier id;

    public RankInfo(int rank, NodeIdentifier id) {
        this.rank = rank;
        this.id = id;
    }
}