package ibis.constellation.context;

import java.io.Serializable;

import ibis.constellation.StealStrategy;

/**
 * An <code>ExecutorContext</code> describes the types of activities that an executor can execute, by means of their
 * {@link ActivityContext activity contexts}. The {@link ActivityContext#satisfiedBy(ExecutorContext, StealStrategy)} method
 * determines the matching between executors and activities.
 */
public abstract class ExecutorContext implements Serializable {

    private static final long serialVersionUID = 7860363329440102125L;

    ExecutorContext() {
        // empty. Package-private, so that users cannot create executor context
        // types of their own.
    }
}
