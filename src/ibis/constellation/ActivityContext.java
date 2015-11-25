package ibis.constellation;

import java.io.Serializable;

public abstract class ActivityContext implements Serializable {

    /*
     * Valid contexts:
     * 
     * ActivityContext = unit | or UnitContext = (tag, rank) OrContext = (unit,
     * unit+)
     */

    protected ActivityContext() {
        // empty
    }

    public abstract boolean equals(Object other);

    public abstract boolean satisfiedBy(WorkerContext other, StealStrategy s);

    public boolean isUnit() {
        return false;
    }

    public boolean isOr() {
        return false;
    }
}
