package ibis.constellation.impl;

import java.io.Serializable;

import ibis.constellation.ActivityContext;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Event;
import ibis.constellation.Executor;

public abstract class ActivityBase implements Serializable {

    private static final byte REQUEST_UNKNOWN = 0;
    private static final byte REQUEST_SUSPEND = 1;
    private static final byte REQUEST_FINISH = 2;

    private transient Executor executor;

    private ActivityIdentifierImpl identifier;
    private final ActivityContext context;

    private final boolean restrictToLocal;
    private final boolean willReceiveEvents;

    private byte next = REQUEST_UNKNOWN;

    /**
     * Initializes this <code>ActivityBase</code> with the specified parameters.
     *
     * @param context
     *            the context that specifies which executors can actually
     *            execute this activity.
     * @param restrictToLocal
     *            when set, specifies that this activity can only be executed by
     *            a local executor.
     * @param willReceiveEvents
     *            when set, specifies that this activity can receive events.
     */
    protected ActivityBase(ActivityContext context, boolean restrictToLocal,
            boolean willReceiveEvents) {
        this.context = context;
        this.restrictToLocal = restrictToLocal;
        this.willReceiveEvents = willReceiveEvents;
    }

    /**
     * Returns <code>true</code> if this activity may receive events,
     * <code>false</code> otherwise.
     *
     * @return whether this activity may receive events.
     */
    public boolean expectsEvents() {
        return willReceiveEvents;
    }

    public final void initialize(ActivityIdentifierImpl id) {
        this.identifier = id;
    }

    final void setExecutor(Executor executor) {
        this.executor = executor;
    }

    protected ActivityIdentifier identifier() {
        if (identifier == null) {
            throw new IllegalStateException(
                    "ActivityBase is not initialized yet");
        }

        return identifier;
    }

    ActivityIdentifierImpl identifierImpl() {
        if (identifier == null) {
            throw new IllegalStateException(
                    "ActivityBase is not initialized yet");
        }
        return identifier;
    }

    protected Executor getExecutor() {

        if (executor == null) {
            throw new IllegalStateException(
                    "ActivityBase is not initialized yet");
        }

        return executor;
    }

    /**
     * Returns the context of this activity.
     *
     * @return the activity context.
     */
    public ActivityContext getContext() {
        return context;
    }

    /**
     * Returns <code>true</code> if this activity can only be executed by a
     * local executor, <code>false</code> otherwise.
     *
     * @return whether this activity can only be executed by a local executor.
     */
    protected boolean isRestrictedToLocal() {
        return restrictToLocal;
    }

    /**
     * Resets the request state of this activity. Usually not called by the
     * application.
     */
    void reset() {
        next = REQUEST_UNKNOWN;
    }

    /**
     * Returns <code>true</code> if the activity requested to be suspended,
     * <code>false</code> otherwise.
     *
     * @return whether the activity requested to be suspended.
     */
    boolean mustSuspend() {
        return (next == REQUEST_SUSPEND);
    }

    /**
     * Returns <code>true</code> if the activity requested to be finished,
     * <code>false</code> otherwise.
     *
     * @return whether the activity requested to be finished.
     */
    boolean mustFinish() {
        return (next == REQUEST_FINISH);
    }

    /**
     * Requests that the current activity will be suspended. Usually called from
     * {@link #initialize()} or {@link #process(Event)}.
     *
     * @exception IllegalStateException
     *                is thrown when the activity already requested to be
     *                finished.
     */
    protected void suspend() {

        if (next == REQUEST_FINISH) {
            throw new IllegalStateException(
                    "ActivityBase already requested to finish!");
        }

        next = REQUEST_SUSPEND;
    }

    /**
     * Requests that the current activity will be finished. Usually called from
     * {@link #initialize()} or {@link #process(Event)}.
     *
     * @exception IllegalStateException
     *                is thrown when the activity already requested to be
     *                suspended.
     */
    protected void finish() {

        if (next == REQUEST_SUSPEND) {
            throw new IllegalStateException(
                    "ActivityBase already requested to suspend!");
        }

        next = REQUEST_FINISH;
    }

    // /**
    // * Todo never called???
    // */
    // public abstract void cancel();

    public abstract void initialize();

    public abstract void process(Event e);

    public abstract void cleanup();

    @Override
    public String toString() {
        return identifier + " " + context;
    }
}
