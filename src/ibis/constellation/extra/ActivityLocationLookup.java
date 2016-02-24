package ibis.constellation.extra;

import java.util.concurrent.ConcurrentHashMap;

import ibis.constellation.ActivityIdentifier;
import ibis.constellation.impl.ConstellationIdentifier;

public class ActivityLocationLookup {

    private ConcurrentHashMap<ActivityIdentifier, ConstellationIdentifier> locations = new ConcurrentHashMap<ActivityIdentifier, ConstellationIdentifier>();

    public void add(ActivityIdentifier aid, ConstellationIdentifier cid) {
        locations.put(aid, cid);
    }

    public ConstellationIdentifier remove(ActivityIdentifier aid) {
        return locations.remove(aid);
    }

    public ConstellationIdentifier lookup(ActivityIdentifier aid) {
        return locations.get(aid);
    }
}
