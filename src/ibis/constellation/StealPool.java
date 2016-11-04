package ibis.constellation;

import ibis.constellation.context.ActivityContext;
import ibis.constellation.context.ExecutorContext;
import ibis.constellation.impl.StealPoolImpl;

/**
 * A <code>StealPool</code> is one of the mechanisms to determine the activities
 * that are to be executed by an {@link Executor}. Each executor has two steal
 * pools associated with it: the one it belongs to and the one it can steal
 * from. In addition, an executor can only execute activities whose
 * {@link ActivityContext} matches with the {@link ExecutorContext} of this
 * executor.
 *
 * A <code>StealPool</code> consists of either a set of other steal pools, or a
 * single string identification.
 */
public final class StealPool extends StealPoolImpl {

    /**
     * An executor that belongs to the <code>WORLD</code> steal pool generates
     * activities that can in principle be stolen by any other executor. An
     * executor that can steal from this steal pool can steal from any pool.
     */
    public static StealPool WORLD = new StealPool("WORLD");

    /**
     * An executor that belongs to the <code>NONE</code> steal pool generates
     * activities that cannot be stolen. An executor that can steal from this
     * steal pool can in fact not steal at all.
     */
    public static StealPool NONE = new StealPool("NONE");

    /**
     * Constructs a StealPool that in fact is a collection of other steal pools.
     * If an executor can steal from such a steal pool, it can in fact steal
     * from any of its members. If an executor belongs to such a steal pool, it
     * can be the steal target of any executor having a member of this steal
     * pool as its steal target.
     *
     * TODO: why not allow collection steal pools as argument and get rid of the
     * merge method?
     *
     * @param set
     *            the list of steal pools comprising the created steal pool.
     * @throws IllegalArgumentException
     *             when one or more of the arguments is itself a collection of
     *             other steal pools
     */
    public StealPool(StealPool... set) {
        super(set);
    }

    /**
     * Constructs a StealPool of the specified tag.
     *
     * @param tag
     *            the tag
     */
    public StealPool(String tag) {
        super(tag);
    }

    /**
     * Returns a StealPool that is the merge result of the steal pools passed as
     * arguments. If the argument list is empty, {@link #NONE} is returned. If
     * the argument list contains {@link #WORLD}, {@link WORLD} is returned.
     *
     * @param pools
     *            the steal pool arguments
     * @return the merge result
     * @throws IllegalArgumentException
     *             when the argument list contains a null pointer.
     */
    public static StealPool merge(StealPool... pools) {
        return StealPoolImpl.merge(pools);
    }
}
