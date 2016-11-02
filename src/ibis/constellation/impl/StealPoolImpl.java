package ibis.constellation.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;

import ibis.constellation.StealPool;

public class StealPoolImpl implements Serializable {

    private static final long serialVersionUID = 2379118089625564822L;

    protected final String tag;
    private final StealPool[] set;

    private static final StealPoolImpl WORLD = StealPool.WORLD;
    private static final StealPoolImpl NONE = StealPool.NONE;

    public StealPoolImpl(StealPoolImpl... set) {

        if (set == null || set.length == 0) {
            throw new IllegalArgumentException(
                    "StealPool set cannot be empty!");
        }

        HashSet<StealPoolImpl> tmp = new HashSet<StealPoolImpl>();

        for (int i = 0; i < set.length; i++) {
            if (set[i] == null) {
                throw new IllegalArgumentException(
                        "StealPool set cannot be sparse!");
            }

            if (set[i].set != null) {
                throw new IllegalArgumentException(
                        "StealPool cannot be recursive!");
            }

            if (set[i].equals(StealPool.NONE)) {
                continue;
            }

            tmp.add(set[i]);

            if (set[i].equals(StealPool.WORLD)) {
                tag = "WORLD";
                this.set = null;
                return;
            }
        }

        if (tmp.size() <= 1) {
            if (tmp.size() == 1) {
                tag = tmp.iterator().next().tag;
            } else {
                tag = "NONE";
            }
            this.set = null;
            return;
        }

        tag = null;
        this.set = tmp.toArray(new StealPool[tmp.size()]);
    }

    public StealPoolImpl(String tag) {
        this.tag = tag;
        this.set = null;
    }

    public String getTag() {
        return tag;
    }

    public boolean isSet() {
        return set != null;
    }

    public boolean isWorld() {
        return this.equals(StealPool.WORLD);
    }

    public boolean isNone() {
        return this.equals(StealPool.NONE);
    }

    public StealPool[] set() {
        return set;
    }

    @Override
    public String toString() {
        if (set != null) {
            return Arrays.toString(set);
        }

        return tag;
    }

    public static StealPool merge(StealPool... pools) {
        if (pools == null || pools.length == 0) {
            return StealPool.NONE;
        }

        HashSet<StealPool> tmp = new HashSet<StealPool>();

        for (int i = 0; i < pools.length; i++) {
            if (pools[i] == null) {
                throw new IllegalArgumentException(
                        "StealPool list cannot be sparse!");
            }
        }
        for (int i = 0; i < pools.length; i++) {
            StealPoolImpl s = pools[i];
            if (s.set != null) {
                StealPoolImpl s2 = merge(s.set());
                // Now it is flattened.
                if (s2.equals(StealPool.WORLD)) {
                    return StealPool.WORLD;
                }
                if (s2.set != null) {
                    for (StealPool s3 : s2.set()) {
                        if (!s3.equals(StealPool.NONE)) {
                            tmp.add(s3);
                        }
                    }
                }
            } else {
                if (s.equals(StealPool.WORLD)) {
                    return StealPool.WORLD;
                }
                if (!s.equals(StealPool.NONE)) {
                    tmp.add(pools[i]);
                }
            }
        }

        if (tmp.size() == 0) {
            // May happen if all StealPools are NONE
            return StealPool.NONE;
        }

        if (tmp.size() == 1) {
            return tmp.iterator().next();
        }

        return new StealPool(tmp.toArray(new StealPool[tmp.size()]));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        if (set != null) {
            result = prime * result + Arrays.hashCode(set);
        } else {
            result = prime * result + tag.hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StealPoolImpl other = (StealPoolImpl) obj;
        if (set == null) {
            if (other.set != null) {
                return false;
            }
        } else {
            if (other.set == null) {
                return false;
            }
            if (other.set.length != set.length) {
                return false;
            }
            // Expensive ...
            for (int i = 0; i < set.length; i++) {
                StealPoolImpl tmp = set[i];
                boolean found = false;

                for (int j = 0; j < other.set.length; j++) {
                    if (tmp.tag.equals(other.set[j].tag)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }
        return tag.equals(other.tag);
    }

    public boolean overlap(StealPoolImpl other) {

        // None does not overlap with anything, not even with None.
        if (isNone() || other.isNone()) {
            return false;
        }

        if (other == this) {
            return true;
        }

        // WORLD overlaps with anything.
        if (isWorld() || other.isWorld()) {
            return true;
        }

        if (isSet()) {
            if (other.isSet()) {
                // Expensive!
                for (int i = 0; i < set.length; i++) {
                    StealPoolImpl tmp = set[i];

                    for (int j = 0; j < other.set.length; j++) {
                        if (tmp.tag.equals(other.set[j].tag)) {
                            return true;
                        }
                    }
                }
            } else {
                for (int i = 0; i < set.length; i++) {
                    if (other.tag.equals(set[i].tag)) {
                        return true;
                    }
                }
            }
        } else {
            if (other.isSet()) {
                for (int i = 0; i < other.set.length; i++) {
                    if (tag.equals(other.set[i].tag)) {
                        return true;
                    }
                }
            } else {
                return tag.equals(other.tag);
            }
        }

        return false;
    }
}
