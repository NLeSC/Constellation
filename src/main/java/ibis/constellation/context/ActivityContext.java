package ibis.constellation.context;

import java.io.Serializable;

import ibis.constellation.StealStrategy;

/**
 * An <code>ActivityContext</code> represents some characterization of the activity at hand, and is instrumental in determining
 * which executors can execute this activity, by means of the {@link #satisfiedBy(ExecutorContext, StealStrategy)} method.
 */
public abstract class ActivityContext implements Serializable {

    /* Generated */
    private static final long serialVersionUID = 3149128711424555747L;

    ActivityContext() {
        // empty. Package-private, so that users cannot create activity context
        // types of their own.
    }

    /**
     * Determines if the specified executor context, in combination with the specified steal strategy, satisfies this activity
     * context (so that it can actually be executed by the executor calling this method).
     *
     * @param executorContext
     *            the executor context to match
     * @param stealStrategy
     *            the steal strategy at hand
     * @return whether this activity context satisfies the conditions imposed by the supplied executor context and steal strategy.
     */
    public abstract boolean satisfiedBy(ExecutorContext executorContext, StealStrategy stealStrategy);
}
