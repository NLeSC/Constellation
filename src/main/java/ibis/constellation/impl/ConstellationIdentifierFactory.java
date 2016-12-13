package ibis.constellation.impl;

public class ConstellationIdentifierFactory {

    private final long rank;
    private final long mask;
    private int count;

    public ConstellationIdentifierFactory(long rank) {
        this.rank = rank;
        this.mask = rank << 32;
    }

    public synchronized ConstellationIdentifier generateConstellationIdentifier() {
        return new ConstellationIdentifier(mask | count++);
    }

    public boolean isLocal(ConstellationIdentifier cid) {
        return ((cid.getId() >> 32) & 0xffffffff) == rank;
    }
}