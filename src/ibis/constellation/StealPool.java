package ibis.constellation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;

public class StealPool implements Serializable {

    private static final long serialVersionUID = 2379118089625564822L;

    public static StealPool WORLD = new StealPool("WORLD", true, false);
    public static StealPool NONE = new StealPool("NONE", false, true);

    private final String tag;
    private final boolean isWorld;
    private final boolean isNone;
    private final boolean isSet;
    private final boolean containsWorld;

    private final StealPool[] set;

    private StealPool(String tag, boolean isWorld, boolean isNone) {
        this.tag = tag;
        this.isSet = false;
        this.isWorld = this.containsWorld = isWorld;
        this.isNone = isNone;
        this.set = null;
    }

    public StealPool(StealPool... set) {

        boolean foundWorld = false;

        if (set == null || set.length == 0) {
            throw new IllegalArgumentException(
                    "StealPool set cannot be empty!");
        }

        HashSet<StealPool> tmp = new HashSet<StealPool>();

        for (int i = 0; i < set.length; i++) {
            if (set[i] == null) {
                throw new IllegalArgumentException(
                        "StealPool set cannot be sparse!");
            }

            if (set[i].isSet) {
                throw new IllegalArgumentException(
                        "StealPool cannot be recursive!");
            }

            tmp.add(set[i]);

            if (set[i].isWorld) {
                foundWorld = true;
            }
        }

        tag = null;
        isSet = true;
        isWorld = isNone = false;
        containsWorld = foundWorld;

        this.set = tmp.toArray(new StealPool[tmp.size()]);
    }

    public StealPool(String tag) {
        this(tag, tag.equals(WORLD.tag), tag.equals(NONE.tag));
    }

    public String getTag() {
        return tag;
    }

    public boolean isSet() {
        return isSet;
    }

    public boolean isWorld() {
        return isWorld;
    }

    public boolean containsWorld() {
        return containsWorld;
    }

    public boolean isNone() {
        return isNone;
    }

    public StealPool[] set() {
        return set;
    }

    @Override
    public String toString() {

        if (isWorld) {
            return "WORLD";
        }

        if (isNone) {
            return "NONE";
        }

        if (isSet) {
            return Arrays.toString(set);
        }

        return tag;
    }

    /*
     * @Override public int hashCode() { final int prime = 31; int result = 1;
     * result = prime * result + ((tag == null) ? 0 : tag.hashCode()); return
     * result; }
     * 
     * @Override public boolean equals(Object obj) { if (this == obj) return
     * true; if (obj == null) return false; if (getClass() != obj.getClass())
     * return false; StealPool other = (StealPool) obj; if (tag == null) { if
     * (other.tag != null) return false; } else if (!tag.equals(other.tag))
     * return false; return true; }
     */

    public static StealPool merge(StealPool... pools) {

        // TODO: we currently see WORLD as just another steal pool ?
        if (pools == null || pools.length == 0) {
            throw new IllegalArgumentException(
                    "StealPool list cannot be empty!");
        }

        HashSet<StealPool> tmp = new HashSet<StealPool>();

        for (int i = 0; i < pools.length; i++) {

            StealPool s = pools[i];

            if (s == null) {
                throw new IllegalArgumentException(
                        "StealPool list cannot be sparse!");
            }

            if (s.isSet) {

                StealPool[] s2 = s.set();

                for (int j = 0; j < s2.length; j++) {

                    if (!s2[j].isNone()) {
                        tmp.add(s2[j]);
                    }
                }
            } else {
                if (!s.isNone()) {
                    tmp.add(s);
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

    /** Generated */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (containsWorld ? 1231 : 1237);
        result = prime * result + (isNone ? 1231 : 1237);
        result = prime * result + (isSet ? 1231 : 1237);
        result = prime * result + (isWorld ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(set);
        result = prime * result + ((tag == null) ? 0 : tag.hashCode());
        return result;
    }

    /** Generated */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StealPool other = (StealPool) obj;
        if (containsWorld != other.containsWorld)
            return false;
        if (isNone != other.isNone)
            return false;
        if (isSet != other.isSet)
            return false;
        if (isWorld != other.isWorld)
            return false;
        if (!Arrays.equals(set, other.set))
            return false;
        if (tag == null) {
            if (other.tag != null)
                return false;
        } else if (!tag.equals(other.tag))
            return false;
        return true;
    }

    public boolean overlap(StealPool other) {

        if (other == this) {
            return true;
        }

        if (isWorld && other.isWorld) {
            return true;
        }

        if (isNone || other.isNone) {
            return false;
        }

        if (isSet) {
            if (other.isSet) {
                // Expensive!
                for (int i = 0; i < set.length; i++) {
                    StealPool tmp = set[i];

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
            if (other.isSet) {
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
