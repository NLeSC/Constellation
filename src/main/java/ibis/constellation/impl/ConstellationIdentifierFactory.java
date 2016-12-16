package ibis.constellation.impl;

public class ConstellationIdentifierFactory {

    private final int rank;
    private int count;

    public ConstellationIdentifierFactory(int rank) {
        this.rank = rank;
    }

    public synchronized ConstellationIdentifierImpl generateConstellationIdentifier() {
        return new ConstellationIdentifierImpl(rank, count++);
    }

    public boolean isLocal(ConstellationIdentifierImpl cid) {
        return cid.getNodeId() == rank;
    }
}
