package ibis.constellation.impl;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import ibis.constellation.ByteBuffers;
import ibis.constellation.StealPool;
import ibis.constellation.context.ExecutorContext;

public class StealReply extends Message implements ByteBuffers {

    private static final long serialVersionUID = 2655647847327367590L;

    private final StealPool pool;
    private final ExecutorContext context;
    private final ActivityRecord[] work;

    public StealReply(final ConstellationIdentifier source,
            final ConstellationIdentifier target, final StealPool pool,
            final ExecutorContext context, final ActivityRecord work) {

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
            final ExecutorContext context, final ActivityRecord[] work) {
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

    public ExecutorContext getContext() {
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
    public void pushByteBuffers(List<ByteBuffer> list) {
        if (work != null) {
            for (Object a : work) {
                if (a != null && a instanceof ByteBuffers) {
                    ((ByteBuffers) a).pushByteBuffers(list);
                }
            }
        }
    }

    @Override
    public void popByteBuffers(List<ByteBuffer> list) {
        if (work != null) {
            for (Object a : work) {
                if (a != null && a instanceof ByteBuffers) {
                    ((ByteBuffers) a).popByteBuffers(list);
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
