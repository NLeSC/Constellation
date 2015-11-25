package ibis.constellation.extra;

import ibis.constellation.ConstellationIdentifier;

public interface ConstellationIdentifierFactory {
    public ConstellationIdentifier generateConstellationIdentifier();

    public boolean isLocal(ConstellationIdentifier cid);
}
