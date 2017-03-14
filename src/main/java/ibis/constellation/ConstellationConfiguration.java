/**
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ibis.constellation;

/**
 * A <code>ConstellationConfiguration</code> represents some capabilities for running {@link Activity activities}. These
 * capabilities could represent a single core, a multiple-core processor, some specialized hardware, or an entire cluster. It is
 * up to the application how these configurations represent the hardware.
 *
 * An executor that corresponds to a specific <code>ConstellationConfiguration</code> is a member of a {@link StealPool}, and can
 * steal activities from another {@link StealPool}. These steal pools are to be provided to the constructor of the
 * <code>ConstellationConfiguration</code>.
 *
 * Also, three (possibly different) steal strategies, represented by {@link StealStrategy} are to be provided:
 * <ul>
 * <li>a local steal strategy, which decides which activities to execute when stealing from the executor at hand;</li>
 * <li>a "constellation-wide" steal strategy, which decides which activities to execute when stealing from within this
 * constellation instance, but from other executors;</li>
 * <li>a remote steal strategy, which decides which activities to execute when stealing from other constellation instances.</li>
 * </ul>
 */
public class ConstellationConfiguration {

    // TODO: why are the fields not final? Why are there 'set' methods?
    private AbstractContext context;

    private StealPool belongsTo;
    private StealPool stealsFrom;

    private StealStrategy localStealStrategy;
    private StealStrategy constellationStealStrategy;
    private StealStrategy remoteStealStrategy;

    /**
     * Constructs a <code>ConstellationConfiguration</code> with the specified parameters. Executors with this configuration will
     * behave as described by the parameter section below.
     *
     * @param myPool
     *            steal pool that these executors will belong to.
     * @param stealsFrom
     *            steal pool that these executors will steal from.
     * @param context
     *            context of these executors, to be used in finding matching activities
     * @param localStealStrategy
     *            steal strategy for local steals
     * @param constellationStealStrategy
     *            steal strategy for steals within this constellation, from other executors.
     * @param remoteStealStrategy
     *            steal strategy for stealing from other constellation instances.
     * @throws IllegalArgumentException
     *             when any of the specified parameters is null.
     */
    public ConstellationConfiguration(AbstractContext context, StealPool belongsTo, StealPool stealsFrom,
            StealStrategy localStealStrategy, StealStrategy constellationStealStrategy, StealStrategy remoteStealStrategy) {

        if (context == null) {
            throw new IllegalArgumentException("Context may not be null");
        } else {
            this.context = context;
        }

        if (localStealStrategy == null) {
            throw new IllegalArgumentException("Local steal strategy may not be null");
        } else {
            this.localStealStrategy = localStealStrategy;
        }

        if (constellationStealStrategy == null) {
            throw new IllegalArgumentException("Constellation steal strategy may not be null");
        } else {
            this.constellationStealStrategy = constellationStealStrategy;
        }

        if (remoteStealStrategy == null) {
            throw new IllegalArgumentException("Remote steal strategy may not be null");
        } else {
            this.remoteStealStrategy = remoteStealStrategy;
        }

        if (belongsTo == null) {
            throw new IllegalArgumentException("Steal pool this Constellation belongs to may not be null");
        } else {
            this.belongsTo = belongsTo;
        }

        if (stealsFrom == null) {
            throw new IllegalArgumentException("Steal pool this Constellation steals from may not be null");
        } else {
            this.stealsFrom = stealsFrom;
        }
    }

    /**
     * Constructs a <code>ConstellationConfiguration</code> with the specified parameters. Executors with this configuration will
     * behave as described by the parameter section below. This version of the constructor uses {@link StealPool#WORLD} for both
     * steal pools.
     *
     * @param context
     *            context of these executors, to be used in finding matching activities
     * @param localStealStrategy
     *            steal strategy for local steals
     * @param constellationStealStrategy
     *            steal strategy for steals within this constellation, from other executors.
     * @param remoteStealStrategy
     *            steal strategy for stealing from other constellation instances.
     * @throws IllegalArgumentException
     *             when any of the specified parameters is null.
     */
    public ConstellationConfiguration(AbstractContext context, StealStrategy localStealStrategy,
            StealStrategy constellationStealStrategy, StealStrategy remoteStealStrategy) {
        this(context, StealPool.WORLD, StealPool.WORLD, localStealStrategy, constellationStealStrategy, remoteStealStrategy);
    }

    /**
     * Constructs a <code>ConstellationConfiguration</code> with the specified parameters. Executors with this configuration will
     * behave as described by the parameter section below. This version of the constructor uses {@link StealPool#WORLD} for both
     * steal pools.
     *
     * @param context
     *            context of these executors, to be used in finding matching activities
     * @param stealStrategy
     *            steal strategy for all steals
     * @throws IllegalArgumentException
     *             when any of the specified parameters is null.
     */
    public ConstellationConfiguration(AbstractContext context, StealStrategy stealStrategy) {
        this(context, StealPool.WORLD, StealPool.WORLD, stealStrategy, stealStrategy, stealStrategy);
    }

    /**
     * Constructs a <code>ConstellationConfiguration</code> with the specified parameters. Executors with this configuration will
     * behave as described by the parameter section below. This version of the constructor uses {@link StealPool#WORLD} for both
     * steal pools.
     *
     * @param myPool
     *            steal pool that these executors will belong to.
     * @param localStealStrategy
     *            steal strategy for local steals
     * @param remoteStealStrategy
     *            steal strategy for stealing from other constellation instances or other executors.
     * @throws IllegalArgumentException
     *             when any of the specified parameters is null.
     */
    public ConstellationConfiguration(AbstractContext context, StealStrategy localStealStrategy,
            StealStrategy remoteStealStrategy) {
        this(context, StealPool.WORLD, StealPool.WORLD, localStealStrategy, remoteStealStrategy, remoteStealStrategy);
    }

    /**
     * Constructs a <code>ConstellationConfiguration</code> with the specified parameters. Executors with this configuration will
     * behave as described by the parameter section below. This version of the constructor uses {@link StealPool#WORLD} for both
     * steal pools, and {@link StealStrategy#SMALLEST} for the steal strategies.
     *
     * @param context
     *            context of these executors, to be used in finding matching activities
     * @throws IllegalArgumentException
     *             when any of the specified parameters is null.
     */
    public ConstellationConfiguration(AbstractContext context) {
        this(context, StealStrategy.SMALLEST);
    }

    /**
     * Returns the context of this <code>ConstellationConfiguration</code>.
     *
     * @return the context
     */
    public AbstractContext getContext() {
        return context;
    }

    public void setContext(AbstractContext context) {
        this.context = context;
    }

    /**
     * Returns the local steal strategy of this<code>ConstellationConfiguration</code>.
     *
     * @return the local steal strategy.
     */
    public StealStrategy getLocalStealStrategy() {
        return localStealStrategy;
    }

    public void setLocalStealStrategy(StealStrategy localStealStrategy) {
        this.localStealStrategy = localStealStrategy;
    }

    /**
     * Returns the constellation steal strategy of this <code>ConstellationConfiguration</code>.
     *
     * @return the constellation steal strategy.
     */
    public StealStrategy getConstellationStealStrategy() {
        return constellationStealStrategy;
    }

    public void setConstellationStealStrategy(StealStrategy constellationStealStrategy) {
        this.constellationStealStrategy = constellationStealStrategy;
    }

    /**
     * Returns the remote steal strategy of this <code>ConstellationConfiguration</code>.
     *
     * @return the remote steal strategy.
     */
    public StealStrategy getRemoteStealStrategy() {
        return remoteStealStrategy;
    }

    public void setRemoteStealStrategy(StealStrategy remoteStealStrategy) {
        this.remoteStealStrategy = remoteStealStrategy;
    }

    /**
     * Returns the pool to which executors of this <code>ConstellationConfiguration</code> belong.
     *
     * @return the membership pool.
     */
    public StealPool getBelongsToPool() {
        return belongsTo;
    }

    public void setBelongsToPool(StealPool myPool) {
        this.belongsTo = myPool;
    }

    /**
     * Returns the pool from which executors of this <code>ConstellationConfiguration</code> can steal.
     *
     * @return the pool to steal from.
     */
    public StealPool getStealsFrom() {
        return stealsFrom;
    }

    public void setStealsFrom(StealPool stealsFrom) {
        this.stealsFrom = stealsFrom;
    }
}
