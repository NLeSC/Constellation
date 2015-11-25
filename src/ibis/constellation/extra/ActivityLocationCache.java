package ibis.constellation.extra;

import ibis.constellation.ActivityIdentifier;
import ibis.constellation.ConstellationIdentifier;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class ActivityLocationCache {

    // The assumption is that there won't be (much) contention on adding an
    // entry, but
    // there will be contention on the lookup. Therefore the register method may
    // contain
    // an expensive lock, while the lookup method uses a map that allows
    // concurrent access.

    private final ConcurrentHashMap<ActivityIdentifier, ConstellationIdentifier> locations;

    private final int maxSize;
    private final LinkedList<ActivityIdentifier> order = new LinkedList<ActivityIdentifier>();

    public ActivityLocationCache(int maxSize) {
        this.maxSize = maxSize;
        locations = new ConcurrentHashMap<ActivityIdentifier, ConstellationIdentifier>(
                maxSize);
    }

    public void add(ActivityIdentifier aid, ConstellationIdentifier cid) {

        ConstellationIdentifier tmp = locations.put(aid, cid);

        if (tmp != null) {
            // We've replaced an existing entry

            if (!tmp.equals(cid)) {

                // Change order in list to reflect update
                synchronized (order) {
                    order.remove(aid);
                    order.addLast(aid);
                }
            }

            return;
        }

        ActivityIdentifier a = null;

        synchronized (order) {
            if (order.size() >= maxSize) {
                a = order.removeFirst();
            }
            order.addLast(aid);
        }

        if (a != null) {
            locations.remove(a);
        }
    }

    public ConstellationIdentifier lookup(ActivityIdentifier aid) {
        return locations.get(aid);
    }
}
