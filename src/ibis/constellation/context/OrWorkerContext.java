package ibis.constellation.context;

import ibis.constellation.WorkerContext;

public class OrWorkerContext extends WorkerContext {

    private static final long serialVersionUID = -1202476921345669674L;

    protected final UnitWorkerContext[] unitContexts;

    protected final boolean ordered;

    public OrWorkerContext(UnitWorkerContext[] unit, boolean ordered) {
        super();

        if (unit == null || unit.length < 2) {
            throw new IllegalArgumentException("Invalid arguments to "
                    + "OrContext: 2 or more contexts required!");
        }

        unitContexts = unit;
        this.ordered = ordered;
    }

    @Override
    public boolean isOr() {
        return true;
    }

    public int size() {
        return unitContexts.length;
    }

    public UnitWorkerContext get(int index) {

        if (index >= 0 && index < unitContexts.length) {
            return unitContexts[index];
        } else {
            return null;
        }
    }

    public UnitWorkerContext[] getContexts() {
        return unitContexts.clone();
    }

    public boolean isOrdered() {
        return ordered;
    }

    public String toString() {

        StringBuilder result = new StringBuilder("OrWorkerContext(");

        for (int i = 0; i < unitContexts.length - 1; i++) {
            result.append(unitContexts[i].toString());
            result.append(" or ");
        }

        result.append(unitContexts[unitContexts.length - 1].toString());
        result.append(")");
        return result.toString();
    }
}
