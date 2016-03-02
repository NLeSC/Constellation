package ibis.constellation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.context.UnitExecutorContext;

/**
 * A <code>SimpleExecutor</code> is an {@link Executor} whose {@link #run()}
 * method calls {@link #processActivities()} repetitively, until it indicates
 * that there are no more activities for this executor.
 */
public class SimpleExecutor extends Executor {

    protected static final Logger logger = LoggerFactory
            .getLogger(SimpleExecutor.class);
    private static final long serialVersionUID = -2498570099898761363L;

    /**
     * Constructs a <code>SimpleExecutor</code> with the specified parameters.
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
    public SimpleExecutor(StealPool myPool, StealPool stealsFrom,
            ExecutorContext context, StealStrategy localStealStrategy,
            StealStrategy constellationStealStrategy,
            StealStrategy remoteStealStrategy) {
        super(myPool, stealsFrom, context, localStealStrategy,
                constellationStealStrategy, remoteStealStrategy);
    }

    /**
     * Constructs a <code>SimpleExecutor</code> with the specified parameters.
     * It uses {@link StealStrategy#ANY} for the local --, constellation --, and
     * remote steal strategies.
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
     */
    public SimpleExecutor(StealPool myPool, StealPool stealsFrom,
            ExecutorContext context) {
        super(myPool, stealsFrom, context, StealStrategy.ANY, StealStrategy.ANY,
                StealStrategy.ANY);
    }

    /**
     * Constructs a <code>SimpleExecutor</code> with the specified parameters.
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
     * @param st
     *            the steal strategy for local, constellation, and remote
     *
     */
    public SimpleExecutor(StealPool myPool, StealPool stealsFrom,
            ExecutorContext context, StealStrategy st) {
        super(myPool, stealsFrom, context, st, st, st);
    }

    /**
     * Constructs a <code>SimpleExecutor</code> with default values for the
     * possible parameters: {@link StealPool#WORLD} for both the steal pool this
     * executor will belong to and the steal pool this executor will steal from,
     * {@link UnitExecutorContext#DEFAULT} for the executor context, and
     * {@link StealStrategy#ANY} for the steal strategies.
     */
    public SimpleExecutor() {
        super(StealPool.WORLD, StealPool.WORLD, UnitExecutorContext.DEFAULT,
                StealStrategy.ANY, StealStrategy.ANY, StealStrategy.ANY);
    }

    /**
     * Constructs a <code>SimpleExecutor</code> with default values for most of
     * the possible parameters: {@link StealPool#WORLD} for both the steal pool
     * this executor will belong to and the steal pool this executor will steal
     * from, and {@link StealStrategy#ANY} for the steal strategies.
     *
     * @param context
     *            context of this executor, to be used in finding matching
     *            activities
     */
    public SimpleExecutor(ExecutorContext context) {
        super(StealPool.WORLD, StealPool.WORLD, context, StealStrategy.ANY,
                StealStrategy.ANY, StealStrategy.ANY);
    }

    /**
     * Constructs a <code>SimpleExecutor</code> with default values for some of
     * the possible parameters: {@link StealPool#WORLD} for both the steal pool
     * this executor will belong to and the steal pool this executor will steal
     * from.
     *
     * @param context
     *            context of this executor, to be used in finding matching
     *            activities
     * @param st
     *            the steal strategy for local, constellation, and remote
     */
    public SimpleExecutor(ExecutorContext context, StealStrategy st) {
        super(StealPool.WORLD, StealPool.WORLD, context, st, st, st);
    }

    /**
     * Constructs a <code>SimpleExecutor</code> with default values for some of
     * the possible parameters: {@link StealPool#WORLD} for both the steal pool
     * this executor will belong to and the steal pool this executor will steal
     * from.
     *
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
    public SimpleExecutor(ExecutorContext context,
            StealStrategy localStealStrategy,
            StealStrategy constellationStealStrategy,
            StealStrategy remoteStealStrategy) {
        super(StealPool.WORLD, StealPool.WORLD, context, localStealStrategy,
                constellationStealStrategy, remoteStealStrategy);
    }

    /**
     * Constructs a <code>SimpleExecutor</code> with default values for some of
     * the possible parameters: {@link StealPool#WORLD} for both the steal pool
     * this executor will belong to and the steal pool this executor will steal
     * from.
     *
     * @param context
     *            context of this executor, to be used in finding matching
     *            activities
     * @param localStealStrategy
     *            steal strategy for local steals
     * @param remoteStealStrategy
     *            steal strategy for stealing from other constellation instances
     *            and from other executors within this constellation instance.
     */
    public SimpleExecutor(ExecutorContext context,
            StealStrategy localStealStrategy,
            StealStrategy remoteStealStrategy) {
        super(StealPool.WORLD, StealPool.WORLD, context, localStealStrategy,
                remoteStealStrategy, remoteStealStrategy);
    }

    @Override
    public void run() {

        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder(
                    "\nStarting Executor: " + identifier() + "\n");

            sb.append("        context: " + getContext() + "\n");
            sb.append("           pool: " + belongsTo() + "\n");
            sb.append("    steals from: " + stealsFrom() + "\n");
            sb.append("          local: " + getLocalStealStrategy() + "\n");
            sb.append("  constellation: " + getConstellationStealStrategy()
                    + "\n");
            sb.append("         remote: " + getRemoteStealStrategy() + "\n");
            sb.append("--------------------------");

            logger.info(sb.toString());
        }

        boolean done = false;

        while (!done) {
            done = processActivities();
        }

        logger.info("Executor done!");

    }

}
