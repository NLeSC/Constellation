package ibis.constellation;

import java.io.Serializable;

public class WorkerContext implements Serializable {

    private static final long serialVersionUID = 7860363329440102125L;

    protected WorkerContext() {
        // empty
    }

    // public abstract boolean equals(Object other);
    // public abstract boolean satisfiedBy(WorkerContext other);

    public boolean isUnit() {
        return false;
    }

    public boolean isOr() {
        return false;
    }
}
