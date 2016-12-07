package ibis.constellation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;

import ibis.constellation.context.ActivityContext;
import ibis.constellation.context.ExecutorContext;

/**
 * A <code>StealPool</code> is one of the mechanisms to determine the activities that are to be executed by an {@link Executor}.
 * Each executor has two steal pools associated with it: the one it belongs to and the one it can steal from. In addition, an
 * executor can only execute activities whose {@link ActivityContext} matches with the {@link ExecutorContext} of this executor.
 *
 * A <code>StealPool</code> consists of either a set of other steal pools, or a single string identification.
 */
public final class StealPool implements Serializable {

    private static final long serialVersionUID = -5231970051093339530L;

    private final String tag;
    private final StealPool[] set;

    /**
     * An executor that belongs to the <code>WORLD</code> steal pool generates activities that can in principle be stolen by any
     * other executor. An executor that can steal from this steal pool can steal from any pool, except <code>NONE</code>.
     */
    public static StealPool WORLD = new StealPool("WORLD");

    /**
     * An executor that belongs to the <code>NONE</code> steal pool generates activities that cannot be stolen. An executor that
     * can steal from this steal pool can in fact not steal at all. Note that <code>NONE</code> takes preference over
     * <code>WORLD</code>.
     */
    public static StealPool NONE = new StealPool("NONE");

    /**
     * Constructs a StealPool that in fact is a collection of other steal pools. If an executor can steal from such a steal pool,
     * it can in fact steal from any of its members. If an executor belongs to such a steal pool, it can be the steal target of
     * any executor having a member of this steal pool as its steal target.
     *
     * This constructor is private, so that the only way it can be used is through the {@link #merge(StealPool...)} method.
     *
     * @param set
     *            the list of steal pools comprising the created steal pool.
     * @throws IllegalArgumentException
     *             thrown when the argument list has null references or less than 2 elements.
     */
    private StealPool(StealPool... set) {
        if (set == null || set.length < 2) {
            throw new IllegalArgumentException("StealPool set should have at least 2 elements!");
        }

        this.set = set.clone();

        // Return sorted result.
        Arrays.sort(this.set, new Comparator<StealPool>() {

            @Override
            public int compare(StealPool o1, StealPool o2) {
                assert (o1.tag != null && o2.tag != null);
                return o1.tag.compareTo(o2.tag);
            }

        });
        tag = null;
    }

    /**
     * Constructs a StealPool of the specified tag.
     *
     * @param tag
     *            the tag
     */
    public StealPool(String tag) {
        this.tag = tag;
        this.set = null;
    }

    /**
     * Returns a StealPool that is the merge result of the steal pools passed as arguments. If the argument list is empty,
     * {@link #NONE} is returned. If the argument list contains {@link #WORLD}, {@link #WORLD} is returned.
     *
     * @param pools
     *            the steal pool arguments
     * @return the merge result
     * @throws IllegalArgumentException
     *             when the argument list contains a null pointer.
     */
    public static StealPool merge(StealPool... pools) {
        if (pools == null || pools.length == 0) {
            return NONE;
        }

        HashSet<StealPool> tmp = new HashSet<StealPool>();

        for (int i = 0; i < pools.length; i++) {
            if (pools[i] == null) {
                throw new IllegalArgumentException("StealPool list cannot have null references!");
            }
        }
        for (int i = 0; i < pools.length; i++) {
            StealPool s = pools[i];
            if (s.set != null) {
                StealPool s2 = merge(s.set);
                // Now it is flattened.
                if (s2.equals(WORLD)) {
                    return WORLD;
                }
                if (s2.set != null) {
                    for (StealPool s3 : s2.set) {
                        assert (s3.set == null);
                        if (!s3.equals(NONE)) {
                            tmp.add(s3);
                        }
                    }
                }
            } else {
                if (s.equals(WORLD)) {
                    return WORLD;
                }
                if (!s.equals(NONE)) {
                    tmp.add(pools[i]);
                }
            }
        }

        if (tmp.size() == 0) {
            // May happen if all StealPools are NONE
            return NONE;
        }

        if (tmp.size() == 1) {
            return tmp.iterator().next();
        }

        return new StealPool(tmp.toArray(new StealPool[tmp.size()]));
    }

    /**
     * Determines if this steal pool has some member steal pool in common with the specified steal pool. {@link #NONE} never
     * overlaps, not even with {@link #NONE}. And then {@link #WORLD} overlaps with any steal pool, except {@link #NONE}.
     *
     * @param other
     *            the steal pool to determine overlap with.
     * @return whether there is overlap between the steal pools.
     */
    public boolean overlap(StealPool other) {

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

        if (set != null) {
            if (other.set != null) {
                int i = 0, oi = 0;
                while (i < set.length && oi < other.set.length) {
                    int cmp = set[i].tag.compareTo(other.set[oi].tag);
                    if (cmp < 0) {
                        i++;
                    } else if (cmp > 0) {
                        oi++;
                    } else {
                        return true;
                    }
                }
            } else {
                for (int i = 0; i < set.length; i++) {
                    int cmp = other.tag.compareTo(set[i].tag);
                    if (cmp == 0) {
                        return true;
                    }
                    if (cmp < 0) {
                        return false;
                    }
                }
            }
        } else {
            if (other.set != null) {
                for (int i = 0; i < other.set.length; i++) {
                    int cmp = tag.compareTo(other.set[i].tag);
                    if (cmp == 0) {
                        return true;
                    }
                    if (cmp < 0) {
                        return false;
                    }
                }
            } else {
                return tag.equals(other.tag);
            }
        }

        return false;
    }

    /**
     * Returns the tag of this steal pool. It this steal pool in fact consists of other steal pools, <code>null</code> is
     * returned.
     *
     * @return the tag of this steal pool.
     */
    public String getTag() {
        return tag;
    }

    /**
     * Determines if this steal pool is equal to the {@link #WORLD} steal pool.
     *
     * @return if this steal pool is the {@link #WORLD} steal pool.
     */
    public boolean isWorld() {
        return this.equals(WORLD);
    }

    /**
     * Determines if this steal pool is equal to the {@link #NONE} steal pool.
     *
     * @return if this steal pool is the {@link #NONE} steal pool.
     */
    public boolean isNone() {
        return this.equals(NONE);
    }

    /**
     * Returns the list of steal pools of which this steal pool consists. If this steal pool has no members, a list with only this
     * steal pools is returned.
     *
     * @return the member steal pools.
     */
    public StealPool[] set() {
        if (set == null) {
            return new StealPool[] { this };
        }
        return set.clone();
    }

    /**
     * Selects a random member steal pool from the steal pool at hand.
     *
     * @param random
     *            the random number generator to use
     *
     * @return a random member steal pool.
     */
    public StealPool randomlySelectPool(Random random) {
        StealPool[] tmp = set();
        return tmp[random.nextInt(tmp.length)];
    }

    @Override
    public String toString() {
        if (set != null) {
            return Arrays.toString(set);
        }

        return tag;
    }

    @Override
    public int hashCode() {
        if (set != null) {
            return Arrays.hashCode(set);
        }
        return tag.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StealPool other = (StealPool) obj;
        if (set == null) {
            if (other.set != null) {
                return false;
            }
            return tag.equals(other.tag);
        }
        if (other.set == null) {
            return false;
        }
        if (other.set.length != set.length) {
            return false;
        }
        for (int i = 0; i < set.length; i++) {
            if (!set[i].equals(other.set[i])) {
                return false;
            }
        }
        return true;
    }
}
