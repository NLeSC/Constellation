package ibis.constellation.impl;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.Activity;
import ibis.constellation.ActivityContext;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.ByteBuffers;
import ibis.constellation.Event;
import ibis.constellation.extra.CircularBuffer;

public class ActivityRecord implements Serializable, ByteBuffers {

    public static final Logger logger = LoggerFactory
            .getLogger(ActivityRecord.class);
    private static final long serialVersionUID = 6938326535791839797L;

    static final int INITIALIZING = 1;
    static final int SUSPENDED = 2;
    static final int RUNNABLE = 3;
    static final int FINISHING = 4;
    static final int DONE = 5;
    static final int ERROR = Integer.MAX_VALUE;

    public final Activity activity;
    private final CircularBuffer<Event> queue;
    private int state = INITIALIZING;

    private boolean stolen = false;
    private boolean relocated = false;
    private boolean remote = false;

    public ActivityRecord(Activity activity) {
        this.activity = activity;
        queue = new CircularBuffer<Event>(4);
    }

    public void enqueue(Event e) {

        if (state >= FINISHING) {
            throw new IllegalStateException(
                    "Cannot deliver an event to a finished activity! "
                            + activity + " (event from " + e.source + ")");
        }

        queue.insertLast(e);
    }

    Event dequeue() {

        if (queue.size() == 0) {
            return null;
        }

        return queue.removeFirst();
    }

    int pendingEvents() {
        return queue.size();
    }

    public ActivityIdentifier identifier() {
        return activity.identifier();
    }

    boolean isRunnable() {
        return (state == RUNNABLE);
    }

    boolean isFinishing() {
        return state == FINISHING;
    }

    public boolean isStolen() {
        return stolen;
    }

    public void setStolen(boolean value) {
        stolen = value;
    }

    boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean value) {
        remote = value;
    }

    public void setRelocated(boolean value) {
        relocated = value;
    }

    public boolean isRelocated() {
        return relocated;
    }

    public boolean isRestrictedToLocal() {
        return activity.isRestrictedToLocal();
    }

    public boolean isDone() {
        return (state == DONE || state == ERROR);
    }

    public boolean isFresh() {
        return (state == INITIALIZING);
    }

    public boolean needsToRun() {
        return (state == INITIALIZING || state == RUNNABLE
                || state == FINISHING);
    }

    public boolean setRunnable() {

        if (state == RUNNABLE || state == INITIALIZING) {
            // it's already runnable
            return false;
        }

        if (state == SUSPENDED) {
            // it's runnable now
            state = RUNNABLE;
            return true;
        }

        // It cannot be made runnable
        throw new IllegalStateException(
                "INTERNAL ERROR: activity cannot be made runnable!");
    }

    private final void runStateMachine() {
        try {
            switch (state) {

            case INITIALIZING:

                activity.initialize();

                if (((ActivityBase) activity).mustSuspend()) {
                    if (pendingEvents() > 0) {
                        state = RUNNABLE;
                    } else {
                        state = SUSPENDED;
                    }
                } else if (((ActivityBase) activity).mustFinish()) {
                    state = FINISHING;
                } else {
                    throw new IllegalStateException(
                            "ActivityBase did not suspend or finish!");
                }

                ((ActivityBase) activity).reset();
                break;

            case RUNNABLE:

                Event e = dequeue();

                if (e == null) {
                    throw new IllegalStateException(
                            "INTERNAL ERROR: Runnable activity has no pending events!");
                }

                activity.process(e);

                if (((ActivityBase) activity).mustSuspend()) {
                    // We only suspend the job if there are no pending events.
                    if (pendingEvents() > 0) {
                        state = RUNNABLE;
                    } else {
                        state = SUSPENDED;
                    }
                } else if (((ActivityBase) activity).mustFinish()) {
                    state = FINISHING;
                } else {
                    throw new IllegalStateException(
                            "ActivityBase did not suspend or finish!");
                }

                ((ActivityBase) activity).reset();
                break;

            case FINISHING:
                activity.cleanup();

                state = DONE;
                break;

            case DONE:
                throw new IllegalStateException(
                        "INTERNAL ERROR: Running activity that is already done");

            case ERROR:
                throw new IllegalStateException(
                        "INTERNAL ERROR: Running activity that is in an error state!");

            default:
                throw new IllegalStateException(
                        "INTERNAL ERROR: Running activity with unknown state!");
            }

        } catch (Throwable e) {
            logger.error("ActivityBase failed: ", e);
            state = ERROR;
        }

    }

    public void run() {

        // do {
        runStateMachine();
        // } while (!(state != SUSPENDED || state == DONE || state == ERROR));
    }

    private String getStateAsString() {

        switch (state) {

        case INITIALIZING:
            return "initializing";
        case SUSPENDED:
            return "suspended";
        case RUNNABLE:
            return "runnable";
        case FINISHING:
            return "finishing";
        case DONE:
            return "done";
        case ERROR:
            return "error";
        }

        return "unknown";
    }

    @Override
    public String toString() {
        return activity + " STATE: " + getStateAsString() + " "
                + "event queue size = " + queue.size();
    }

    public ActivityContext getContext() {
        return activity.getContext();
    }

    @Override
    public void pushByteBuffers(List<ByteBuffer> list) {
        if (queue != null) {
            queue.pushByteBuffers(list);
        }
        if (activity != null && activity instanceof ByteBuffers) {
            ((ByteBuffers) activity).pushByteBuffers(list);
        }
    }

    @Override
    public void popByteBuffers(List<ByteBuffer> list) {
        if (queue != null) {
            queue.popByteBuffers(list);
        }
        if (activity != null && activity instanceof ByteBuffers) {
            ((ByteBuffers) activity).popByteBuffers(list);
        }
    }
}
