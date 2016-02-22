package ibis.constellation.context;

import ibis.constellation.ExecutorContext;

/**
 * A <code>UnitExecutorContext</code> represents a single context, associated
 * with an executor, and determines a specific type of activity that can be
 * executed by the executor.
 */
public class UnitExecutorContext extends ExecutorContext {

    private static final long serialVersionUID = 6134114690113562356L;

    /** A default unit executor context. */
    public static final UnitExecutorContext DEFAULT = new UnitExecutorContext(
            "DEFAULT");

    public final String name;
    protected final int hashCode;

    public UnitExecutorContext(String name) {

        super();

        if (name == null) {
            throw new IllegalArgumentException("Context name cannot be null!");
        }

        this.name = name;
        this.hashCode = name.hashCode();
    }

    @Override
    public boolean isUnit() {
        return true;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "UnitExecutorContext(" + name + ")";
    }
}
