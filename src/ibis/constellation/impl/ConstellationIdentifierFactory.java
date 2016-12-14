package ibis.constellation.impl;

import ibis.constellation.ConstellationIdentifier;

public class ConstellationIdentifierFactory {

    private final int rank;
    private int count;

    public ConstellationIdentifierFactory(int rank) {
        this.rank = rank;
    }

    public synchronized ConstellationIdentifier generateConstellationIdentifier() {
        return new ConstellationIdentifier(rank, count++);
    }

    public boolean isLocal(ConstellationIdentifier cid) {
        return cid.getNodeId() == rank;
    }
}
