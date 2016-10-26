package ibis.constellation;

import java.io.Serializable;

import ibis.constellation.context.UnitExecutorContext;
import ibis.constellation.impl.ExecutorBase;

/**
 * An <code>Executor</code> represents some hardware capable of running
 * {@link Activity activities}. This could be a single core, a multiple-core
 * processor, some specialized hardware, or an entire cluster. It is up to the
 * application how executors represent the hardware. Constellation provides the
 * {@link SimpleExecutor} implementation, but applications may provide others. A
 * {@link ExecutorContext} provides labels to classify the capabilities of the
 * executor.
 *
 * Executors are member of a {@link StealPool}, and are able to steal activities
 * from another {@link StealPool}. These steal pools are to be provided to the
 * constructor.
 *
 * Also, three (possibly different) steal strategies, represented by
 * {@link StealStrategy} are to be provided:
 * <ul>
 * <li>a local steal strategy, which decides which activities to execute when
 * stealing from the executor at hand;</li>
 * <li>a "constellation-wide" steal strategy, which decides which activities to
 * execute when stealing from within this constellation, but from other
 * executors;</li>
 * <li>a remote steal strategy, which decides which activities to execute when
 * stealing from other constellation instances.</li>
 * </ul>
 */
public abstract class Executor extends ExecutorBase implements Serializable {

    private static final long serialVersionUID = 6808516395963593310L;

    /**
     * Constructs an <code>Executor</code> with the specified parameters.
     *
     * @param myPool
     *            steal pool that this executor will belong to. May be
     *            <code>null</code>m in which case {@link StealPool#NONE} is
     *            assumed
     * @param stealsFrom
     *            steal pool that this executor will steal from. May be
     *            <code>null</code>m in which case {@link StealPool#NONE} is
     *            assumed
     * @param context
     *            context of this executor, to be used in finding matching
     *            activities
     * @param localStealStrategy
     *            steal strategy for local steals
     * @param constellationStealStrategy
     *            steal strategy for steals within this constellation, from
     *            other executors.
     * @param remoteStealStrategy
     *            steal strategy for stealing from other constellation
     *            instances.
     */
    protected Executor(StealPool myPool, StealPool stealsFrom,
            ExecutorContext context, StealStrategy localStealStrategy,
            StealStrategy constellationStealStrategy,
            StealStrategy remoteStealStrategy) {
        super(myPool, stealsFrom, context, localStealStrategy,
                constellationStealStrategy, remoteStealStrategy);
    }

    /**
     * Constructs an <code>Executor</code> with default values for the possible
     * parameters: {@link StealPool#WORLD} for both the steal pool this executor
     * will belong to and the steal pool this executor will steal from,
     * {@link UnitExecutorContext#DEFAULT} for the executor context, and
     * {@link StealStrategy#ANY} for the steal strategies.
     */
    protected Executor() {
        this(StealPool.WORLD, StealPool.WORLD, UnitExecutorContext.DEFAULT,
                StealStrategy.ANY, StealStrategy.ANY, StealStrategy.ANY);
    }

    /**
     * Indicates to the executor that it can start running activities. This is
     * usually repetitively called from the {@link #run()} method, which can
     * return when processActivities() returns <code>true</code>.
     *
     * @return whether the {@link #run()} method should return.
     */
    @Override
    protected final boolean processActivities() {
        return super.processActivities();
    }

    /**
     * This is the main method of this executor. Usually, it repeatedly calls
     * {@link #processActivities()} until it returns <code>true</code>.
     */
    public abstract void run();
}
