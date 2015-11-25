package ibis.constellation.extra;

import ibis.constellation.ConstellationIdentifier;

public class SimpleConstellationIdentifierFactory
        implements ConstellationIdentifierFactory {

    private long count;

    public synchronized ConstellationIdentifier generateConstellationIdentifier() {
        return new ConstellationIdentifier(count++);
    }

    public boolean isLocal(ConstellationIdentifier cid) {
        return true;
    }
}
