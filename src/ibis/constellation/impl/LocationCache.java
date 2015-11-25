package ibis.constellation.impl;

import ibis.constellation.ActivityIdentifier;
import ibis.constellation.ConstellationIdentifier;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocationCache {

    public static final Logger logger = LoggerFactory
            .getLogger(LocationCache.class);
    private HashMap<ActivityIdentifier, Entry> map = new HashMap<ActivityIdentifier, Entry>();

    public final class Entry {

        public final ConstellationIdentifier id;
        public final long count;

        Entry(ConstellationIdentifier id, long count) {
            this.id = id;
            this.count = count;
        }
    }

    public synchronized ConstellationIdentifier lookup(ActivityIdentifier a) {

        final Entry tmp = map.get(a);

        if (tmp != null) {
            return tmp.id;
        } else {
            return null;
        }
    }

    public synchronized Entry lookupEntry(ActivityIdentifier a) {
        return map.get(a);
    }

    public synchronized ConstellationIdentifier remove(ActivityIdentifier a) {

        final Entry tmp = map.remove(a);

        if (tmp != null) {
            return tmp.id;
        } else {
            return null;
        }
    }

    public synchronized void removeIfEqual(ActivityIdentifier a,
            ConstellationIdentifier c) {

        final Entry tmp = map.get(a);

        if (tmp == null) {
            return;
        }

        if (tmp.id.equals(c)) {
            map.remove(a);
        }
    }

    public synchronized void put(ActivityIdentifier a,
            ConstellationIdentifier c, long count) {

        // NOTE: we only replace an existing entry if count is larger
        final Entry tmp = map.get(a);

        if (tmp == null) {
            map.put(a, new Entry(c, count));
        } else if (tmp.count < count) {
            map.put(a, new Entry(c, count));
        } else if (tmp.count == count && !tmp.id.equals(c)) {
            // SANITY CHECK
            logger.error("nconsistency discovered in " + "LocactionCache: "
                    + tmp.id + "/" + tmp.count + " != " + c + "/" + count);
        }
    }
}
