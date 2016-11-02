package ibis.constellation.extra;

import java.util.concurrent.ConcurrentHashMap;

import ibis.constellation.impl.ActivityIdentifierImpl;
import ibis.constellation.impl.ConstellationIdentifier;

public class ActivityLocationLookup {

    private ConcurrentHashMap<ActivityIdentifierImpl, ConstellationIdentifier> locations = new ConcurrentHashMap<ActivityIdentifierImpl, ConstellationIdentifier>();

    public void add(ActivityIdentifierImpl aid, ConstellationIdentifier cid) {
        locations.put(aid, cid);
    }

    public ConstellationIdentifier remove(ActivityIdentifierImpl aid) {
        return locations.remove(aid);
    }

    public ConstellationIdentifier lookup(ActivityIdentifierImpl aid) {
        return locations.get(aid);
    }
}
