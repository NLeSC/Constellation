package ibis.constellation;

import java.io.Serializable;

/**
 * An <code>ActivityContext</code> represents some characterization of the
 * activity at hand, and is instrumental in determining which executors can
 * execute this activity, by means of the
 * {@link #satisfiedBy(ExecutorContext, StealStrategy)} method.
 */
public abstract class ActivityContext implements Serializable {

    protected ActivityContext() {
        // empty
    }

    /**
     * Determines if the specified executor context, in combination with the
     * specified steal strategy, satisfies this activity context (so that it can
     * actually be executed by the executor calling this method).
     * 
     * @param executorContext
     *            the executor context to match
     * @param stealStrategy
     *            the steal strategy at hand
     * @return whether this activity context satisfies the conditions imposed by
     *         the supplied executor context and steal strategy.
     */
    public abstract boolean satisfiedBy(ExecutorContext executorContext,
            StealStrategy stealStrategy);

    /**
     * An <code>ActivityContext</code> can either be a unit-context or a
     * or-context. The latter is a combination of several activity contexts. The
     * <code>isUnit</code> method returns <code>true</code> if this context is a
     * unit-context, <code>false</code> otherwise.
     *
     * @return whether this context is a unit-context.
     */
    public boolean isUnit() {
        return false;
    }

    /**
     * An <code>ActivityContext</code> can either be a unit-context or a
     * or-context. The latter is a combination of several activity contexts. The
     * <code>isOr</code> method returns <code>true</code> if this context is a
     * combination-context, <code>false</code> otherwise.
     *
     * @return whether this context is a combination-context.
     */
    public boolean isOr() {
        return false;
    }
}
