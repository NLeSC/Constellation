package ibis.constellation.extra;

import ibis.constellation.StealStrategy;
import ibis.constellation.context.ExecutorContext;
import ibis.constellation.impl.ActivityRecord;

public abstract class WorkQueue {

    private final String id;

    protected WorkQueue(String id) {
        this.id = id;
    }

    public abstract void enqueue(ActivityRecord a);

    public abstract ActivityRecord steal(ExecutorContext c, StealStrategy s);

    public abstract int size();

    public void enqueue(ActivityRecord[] a) {
        for (int i = 0; i < a.length; i++) {
            enqueue(a[i]);
        }
    }

    public int steal(ExecutorContext c, StealStrategy s, ActivityRecord[] dst,
            int off, int len) {

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
