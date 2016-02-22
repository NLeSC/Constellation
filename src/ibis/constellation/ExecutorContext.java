package ibis.constellation;

import java.io.Serializable;

/**
 * An <code>ExecutorContext</code> describes the types of activities that an
 * executor can execute, by means of their {@link ActivityContext activity
 * contexts}. The
 * {@link ActivityContext#satisfiedBy(ExecutorContext, StealStrategy)} method
 * determines the matching between executors and activities.
 */
public class ExecutorContext implements Serializable {

    private static final long serialVersionUID = 7860363329440102125L;

    protected ExecutorContext() {
        // empty
    }

    /**
     * An <code>ExecutorContext</code> can either be a unit-context or a
     * or-context. The latter is a combination of several executor contexts. The
     * <code>isUnit</code> method returns <code>true</code> if this context is a
     * unit-context, <code>false</code> otherwise.
     * 
     * @return whether this context is a unit-context.
     */
    public boolean isUnit() {
        return false;
    }

    /**
     * An <code>ExecutorContext</code> can either be a unit-context or a
     * or-context. The latter is a combination of several executor contexts. The
     * <code>isOr</code> method returns <code>true</code> if this context is a
     * combination-context, <code>false</code> otherwise.
     * 
     * @return whether this context is a combination-context.
     */
    public boolean isOr() {
        return false;
    }
}
