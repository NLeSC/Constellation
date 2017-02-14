package ibis.constellation.impl.util;

import ibis.constellation.AbstractContext;
import ibis.constellation.StealStrategy;
import ibis.constellation.impl.ActivityRecord;

public abstract class WorkQueue {

    private final String id;

    protected WorkQueue(String id) {
        this.id = id;
    }

    public abstract void enqueue(ActivityRecord a);

    public abstract ActivityRecord steal(AbstractContext c, StealStrategy s);

    public abstract int size();

    public void enqueue(ActivityRecord[] a) {
        for (ActivityRecord element : a) {
            enqueue(element);
        }
    }

    // TODO: fix steal to allow multiple in one go!
    public int steal(AbstractContext c, StealStrategy s, ActivityRecord[] dst, int off, int len) {

        for (int i = off; i < off + len; i++) {
            dst[i] = steal(c, s);

            if (dst[i] == null) {
                return (i - off);
            }
        }

        return len;
    }

    protected final String getId() {
        return id;
    }

}
