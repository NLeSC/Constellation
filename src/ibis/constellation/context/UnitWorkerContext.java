package ibis.constellation.context;

import ibis.constellation.WorkerContext;

public class UnitWorkerContext extends WorkerContext {

    private static final long serialVersionUID = 6134114690113562356L;

    public static final UnitWorkerContext DEFAULT = new UnitWorkerContext(
            "DEFAULT");

    public final String name;
    protected final int hashCode;

    public UnitWorkerContext(String name) {

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

    public String toString() {
        return "UnitWorkerContext(" + name + ")";
    }
}
