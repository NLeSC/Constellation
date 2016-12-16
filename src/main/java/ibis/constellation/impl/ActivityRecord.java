package ibis.constellation.impl;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.ByteBuffers;
import ibis.constellation.Event;
import ibis.constellation.context.ActivityContext;
import ibis.constellation.extra.CircularBuffer;

public class ActivityRecord implements Serializable, ByteBuffers {

    private static final Logger logger = LoggerFactory.getLogger(ActivityRecord.class);
    private static final long serialVersionUID = 6938326535791839797L;

    private static final int INITIALIZING = 1;
    private static final int SUSPENDED = 2;
    private static final int RUNNABLE = 3;
    private static final int FINISHING = 4;
    private static final int DONE = 5;
    private static final int ERROR = Integer.MAX_VALUE;

    private final Activity activity;
    private final CircularBuffer<Event> queue;
    private int state = INITIALIZING;

    private boolean stolen = false;
    private boolean relocated = false;
    private boolean remote = false;

    ActivityRecord(Activity activity) {
        this.activity = activity;
        queue = new CircularBuffer<Event>(4);
    }

    void enqueue(Event e) {

        if (state >= FINISHING) {
            throw new IllegalStateException(
                    "Cannot deliver an event to a finished activity! " + getActivity() + " (event from " + e.getSource() + ")");
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
        return getActivity().identifier();
    }

    boolean isRunnable() {
        return (state == RUNNABLE);
    }

    boolean isFinishing() {
        return state == FINISHING;
    }

    boolean isStolen() {
        return stolen;
    }

    void setStolen(boolean value) {
        stolen = value;
    }

    boolean isRemote() {
        return remote;
    }

    void setRemote(boolean value) {
        remote = value;
    }

    void setRelocated(boolean value) {
        relocated = value;
    }

    boolean isRelocated() {
        return relocated;
    }

    boolean isRestrictedToLocal() {
        return getActivity().isRestrictedToLocal();
    }

    boolean isDone() {
        return (state == DONE || state == ERROR);
    }

    boolean isFresh() {
        return (state == INITIALIZING);
    }

    boolean needsToRun() {
        return (state == INITIALIZING || state == RUNNABLE || state == FINISHING);
    }

    boolean setRunnable() {

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
        throw new IllegalStateException("INTERNAL ERROR: activity cannot be made runnable!");
    }

    private final void runStateMachine() {
        try {
            switch (state) {

            case INITIALIZING:

                getActivity().initialize();

                if (getActivity().mustSuspend()) {
                    if (pendingEvents() > 0) {
                        state = RUNNABLE;
                    } else {
                        state = SUSPENDED;
                    }
                } else if (getActivity().mustFinish()) {
                    state = FINISHING;
                } else {
                    throw new IllegalStateException("ActivityBase did not suspend or finish!");
                }

                getActivity().reset();
                break;

            case RUNNABLE:

                Event e = dequeue();

                if (e == null) {
                    throw new IllegalStateException("INTERNAL ERROR: Runnable activity has no pending events!");
                }

                getActivity().process(e);

                if (getActivity().mustSuspend()) {
                    // We only suspend the job if there are no pending events.
                    if (pendingEvents() > 0) {
                        state = RUNNABLE;
                    } else {
                        state = SUSPENDED;
                    }
                } else if (getActivity().mustFinish()) {
                    state = FINISHING;
                } else {
                    throw new IllegalStateException("ActivityBase did not suspend or finish!");
                }

                getActivity().reset();
                break;

            case FINISHING:
                getActivity().cleanup();

                state = DONE;
                break;

            case DONE:
                throw new IllegalStateException("INTERNAL ERROR: Running activity that is already done");

            case ERROR:
                throw new IllegalStateException("INTERNAL ERROR: Running activity that is in an error state!");

            default:
                throw new IllegalStateException("INTERNAL ERROR: Running activity with unknown state!");
            }

        } catch (Throwable e) {
            logger.error("ActivityBase failed: ", e);
            state = ERROR;
        }

    }

    void run() {
        runStateMachine();
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
        default:
            return "unknown";
        }
    }

    @Override
    public String toString() {
        return getActivity() + " STATE: " + getStateAsString() + " " + "event queue size = " + queue.size();
    }

    ActivityContext getContext() {
        return getActivity().getContext();
    }

    @Override
    public void pushByteBuffers(List<ByteBuffer> list) {
        if (queue != null) {
            queue.pushByteBuffers(list);
        }
        if (getActivity() != null && getActivity() instanceof ByteBuffers) {
            ((ByteBuffers) getActivity()).pushByteBuffers(list);
        }
    }

    @Override
    public void popByteBuffers(List<ByteBuffer> list) {
        if (queue != null) {
            queue.popByteBuffers(list);
        }
        if (getActivity() != null && getActivity() instanceof ByteBuffers) {
            ((ByteBuffers) getActivity()).popByteBuffers(list);
        }
    }

    public ActivityBase getActivity() {
        return activity;
    }
}
