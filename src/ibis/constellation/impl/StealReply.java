package ibis.constellation.impl;

import ibis.constellation.ConstellationIdentifier;
import ibis.constellation.ObjectData;
import ibis.constellation.StealPool;
import ibis.constellation.WorkerContext;
import ibis.ipl.ReadMessage;
import ibis.ipl.WriteMessage;

import java.io.IOException;
import java.util.Arrays;

public class StealReply extends Message implements ObjectData {

    private static final long serialVersionUID = 2655647847327367590L;

    private final StealPool pool;
    private final WorkerContext context;
    private final ActivityRecord[] work;

    public StealReply(final ConstellationIdentifier source,
            final ConstellationIdentifier target, final StealPool pool,
            final WorkerContext context, final ActivityRecord work) {

        super(source, target);

        if (work == null) {
            this.work = null;
        } else {
            this.work = new ActivityRecord[] { work };
        }
        this.pool = pool;
        this.context = context;
    }

    public StealReply(final ConstellationIdentifier source,
            final ConstellationIdentifier target, final StealPool pool,
            final WorkerContext context, final ActivityRecord[] work) {
        super(source, target);

        this.pool = pool;
        this.work = work;
        this.context = context;
    }

    public boolean isEmpty() {
        return (work == null || work.length == 0);
    }

    public StealPool getPool() {
        return pool;
    }

    public WorkerContext getContext() {
        return context;
    }

    public ActivityRecord[] getWork() {
        return work;
    }

    public ActivityRecord getWork(int i) {
        return work[i];
    }

    public int getSize() {

        if (work == null) {
            return 0;
        }

        // Note: assumes array is filled!
        return work.length;
    }

    @Override
    public void writeData(WriteMessage m) throws IOException {
        if (work != null) {
            for (Object a : work) {
                if (a != null && a instanceof ObjectData) {
                    ((ObjectData) a).writeData(m);
                }
            }
        }
    }

    @Override
    public void readData(ReadMessage m) throws IOException {
        if (work != null) {
            for (Object a : work) {
                if (a != null && a instanceof ObjectData) {
                    ((ObjectData) a).readData(m);
                }
            }
        }
    }

    @Override
    public String toString() {
        if (work == null) {
            return "no jobs";
        }
        return Arrays.toString(work);
    }
}
