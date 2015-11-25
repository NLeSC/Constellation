package ibis.constellation.extra;

import ibis.constellation.StealPool;
import ibis.ipl.IbisIdentifier;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StealPoolInfo {

    private final HashMap<String, ArrayList<Object>> map = new HashMap<String, ArrayList<Object>>();

    public static final Logger log = LoggerFactory
            .getLogger(StealPoolInfo.class);

    public StealPoolInfo() {
        // Nothing to see here... move along!
    }

    private void remove(String tag, Object o) {

        ArrayList<Object> tmp = map.get(tag);

        if (tmp == null || !tmp.remove(o)) {
            log.error("Failed remove from StealPoolInfo!");
        }
    }

    private void add(String tag, Object o) {

        ArrayList<Object> tmp = map.get(tag);

        if (tmp == null) {
            tmp = new ArrayList<Object>();
        }

        // Sanity check, should never happen ?
        if (tmp.contains(o)) {
            log.error("Double add to StealPoolInfo");
            return;
        }

        tmp.add(o);
    }

    private void remove(StealPool pool, Object o) {

        if (pool.isSet()) {
            StealPool[] tmp = pool.set();

            for (int i = 0; i < tmp.length; i++) {
                remove(tmp[i].getTag(), o);
            }
        } else {
            remove(pool.getTag(), o);
        }
    }

    private void add(StealPool pool, Object o) {

        if (pool.isSet()) {
            StealPool[] tmp = pool.set();

            for (int i = 0; i < tmp.length; i++) {
                add(tmp[i].getTag(), o);
            }
        } else {
            add(pool.getTag(), o);
        }
    }

    public synchronized void update(StealPool oldPool, StealPool newPool,
            Object o) {

        if (oldPool != null) {
            remove(oldPool, o);
        }

        if (newPool != null) {
            add(oldPool, o);
        }
    }

    public IbisIdentifier selectRandom(String tag) {
        return null;
    }
}
