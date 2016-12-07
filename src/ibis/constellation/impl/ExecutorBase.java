package ibis.constellation.impl;

import java.io.Serializable;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Event;
import ibis.constellation.StealPool;
import ibis.constellation.StealStrategy;
import ibis.constellation.context.ExecutorContext;
import ibis.constellation.context.UnitExecutorContext;

/**
 * Base class of an executor, hiding some implementation details.
 */
public abstract class ExecutorBase implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ExecutorContext context;

    private final StealStrategy localStealStrategy;
    private final StealStrategy constellationStealStrategy;
    private final StealStrategy remoteStealStrategy;

    private final StealPool myPool;
    private final StealPool stealsFrom;

    private ExecutorWrapper owner = null;

    /**
     * Constructs an <code>Executor</code> with the specified parameters.
     *
     * @param myPool
     *            steal pool that this executor will belong to. May be <code>null</code>m in which case {@link StealPool#NONE} is
     *            assumed
     * @param stealsFrom
     *            steal pool that this executor will steal from. May be <code>null</code>m in which case {@link StealPool#NONE} is
     *            assumed
     * @param context
     *            context of this executor, to be used in finding matching activities
     * @param localStealStrategy
     *            steal strategy for local steals
     * @param constellationStealStrategy
     *            steal strategy for steals within this constellation, from other executors.
     * @param remoteStealStrategy
     *            steal strategy for stealing from other constellation instances.
     */
    protected ExecutorBase(StealPool myPool, StealPool stealsFrom, ExecutorContext context, StealStrategy localStealStrategy,
            StealStrategy constellationStealStrategy, StealStrategy remoteStealStrategy) {

        if (myPool == null) {
            this.myPool = StealPool.NONE;
        } else {
            this.myPool = myPool;
        }

        if (stealsFrom == null) {
            this.stealsFrom = StealPool.NONE;
        } else {
            this.stealsFrom = stealsFrom;
        }

        if (context == null) {
            this.context = UnitExecutorContext.DEFAULT;
        } else {
            this.context = context;
        }

        if (localStealStrategy == null) {
            this.localStealStrategy = StealStrategy.ANY;
        } else {
            this.localStealStrategy = localStealStrategy;
        }

        if (constellationStealStrategy == null) {
            this.constellationStealStrategy = StealStrategy.ANY;
        } else {
            this.constellationStealStrategy = constellationStealStrategy;
        }

        if (remoteStealStrategy == null) {
            this.remoteStealStrategy = StealStrategy.ANY;
        } else {
            this.remoteStealStrategy = remoteStealStrategy;
        }
    }

    /**
     * Constructs an <code>Executor</code> with default values for the possible parameters: {@link StealPool#WORLD} for both the
     * steal pool this executor will belong to and the steal pool this executor will steal from,
     * {@link UnitExecutorContext#DEFAULT} for the executor context, and {@link StealStrategy#ANY} for the steal strategies.
     */
    protected ExecutorBase() {
        this(StealPool.WORLD, StealPool.WORLD, UnitExecutorContext.DEFAULT, StealStrategy.ANY, StealStrategy.ANY,
                StealStrategy.ANY);
    }

    /**
     * Indicates to the executor that it can start running activities. This is usually repetitively called from the {@link #run()}
     * method, which can return when processActivities() returns <code>true</code>.
     *
     * @return whether the {@link #run()} method should return.
     */
    protected boolean processActivities() {
        if (owner == null) {
            throw new Error("processActivities() called but this executor is not embedded in a constellation instance yet");
        }
        return owner.processActitivies();
    }

    /**
     * Returns the steal pool this executor steals from.
     *
     * @return the steal pool this executor steals from
     */
    public StealPool stealsFrom() {
        return stealsFrom;
    }

    /**
     * Returns the local steal strategy, which is the strategy used when this executor is stealing from itself.
     *
     * @return the local steal strategy
     */
    public StealStrategy getLocalStealStrategy() {
        return localStealStrategy;
    }

    /**
     * Returns the steal strategy used when stealing within the current constellation (but from other executors).
     *
     * @return the steal strategy for stealing within the current constellation
     */
    public StealStrategy getConstellationStealStrategy() {
        return constellationStealStrategy;
    }

    /**
     * Returns the steal strategy used when stealing from other constellation instances.
     *
     * @return the remote steal strategy
     */
    public StealStrategy getRemoteStealStrategy() {
        return remoteStealStrategy;
    }

    /**
     * Submits an activity to the current executor.
     *
     * @param job
     *            the activity to submit
     * @return the activity identifier identifying this activity within constellation
     */
    public ActivityIdentifier submit(Activity job) {
        if (owner == null) {
            throw new Error("submit() called but this executor is not embedded in a constellation instance yet");
        }
        return owner.submit(job);
    }

    /**
     * Returns the constellation identifier of the current constellation instance.
     *
     * @return the constellation identifier
     */
    public String identifier() {
        if (owner == null) {
            throw new Error("identifier() called but this executor is not embedded in a constellation instance yet");
        }
        return owner.identifier();
    }

    /**
     * Sends the specified event. The destination activity is encoded in the event.
     *
     * @param e
     *            the event to send
     */
    public void send(Event e) {
        if (owner == null) {
            throw new Error("send() called but this executor is not embedded in a constellation instance yet");
        }
        owner.send(e);
    }

    /**
     * Returns the context of this executor.
     *
     * @return the executor's context
     */
    public ExecutorContext getContext() {
        return context;
    }

    synchronized void connect(ExecutorWrapper owner) {

        if (this.owner != null) {
            throw new Error("Executor already connected!");
        }

        this.owner = owner;
    }

    /**
     * Returns the steal pool that this executor belongs to.
     *
     * @return the steal pool this executor belongs to.
     */
    public StealPool belongsTo() {
        return myPool;
    }

    /**
     * This is the main method of this executor. Usually, it repeatedly calls {@link #processActivities()} until it returns
     * <code>true</code>.
     */
    public abstract void run();
}
