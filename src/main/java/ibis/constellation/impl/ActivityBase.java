package ibis.constellation.impl;

import java.io.Serializable;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Event;
import ibis.constellation.Executor;
import ibis.constellation.context.ActivityContext;

/**
 * As the title suggests: the base class for {@link Activity activities}.
 *
 * This class contains some activity-related stuff that the user should not be bothered with.
 */
public abstract class ActivityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Next action after {@link Activity#process()} or {@link Activity#process(Event)} is not known.
     */
    private static final byte REQUEST_UNKNOWN = 0;

    /**
     * Next action after {@link Activity#process()} or {@link Activity#process(Event)} is to suspend.
     */
    private static final byte REQUEST_SUSPEND = 1;

    /**
     * Next action after {@link Activity#process()} or {@link Activity#process(Event)} is to finish.
     */
    private static final byte REQUEST_FINISH = 2;

    /**
     * Next action after {@link Activity#process()} or {@link Activity#process(Event)}.
     */
    private byte next = REQUEST_UNKNOWN;

    /** {@link Executor} currently associated with this action. */
    private transient Executor executor;

    /** Identification of this activity. */
    private ActivityIdentifierImpl identifier;

    /** The context of this activity. */
    private final ActivityContext context;

    /** Whether this activity may only be executed by local executors. */
    private final boolean restrictToLocal;

    /** Whether this activity may receive events. */
    private final boolean willReceiveEvents;

    /**
     * Initializes this <code>ActivityBase</code> with the specified parameters.
     *
     * @param context
     *            the context that specifies which executors can actually execute this activity.
     * @param restrictToLocal
     *            when set, specifies that this activity can only be executed by a local executor.
     * @param willReceiveEvents
     *            when set, specifies that this activity can receive events.
     */
    protected ActivityBase(ActivityContext context, boolean restrictToLocal, boolean willReceiveEvents) {
        this.context = context;
        this.restrictToLocal = restrictToLocal;
        this.willReceiveEvents = willReceiveEvents;
        this.identifier = ActivityIdentifierImpl.createActivityIdentifier(null, -1, false);
    }

    /**
     * Returns <code>true</code> if this activity may receive events, <code>false</code> otherwise.
     *
     * @return whether this activity may receive events.
     */
    boolean expectsEvents() {
        return willReceiveEvents;
    }

    /**
     * Sets the identification of this activity.
     *
     * @param id
     *            the activity identifier
     */
    void initialize(ActivityIdentifierImpl id) {
        this.identifier = id;
    }

    /**
     * Sets the executor that is going to execute this activity.
     *
     * @param executor
     *            the executor
     */
    void setExecutor(Executor executor) {
        this.executor = executor;
    }

    /**
     * Returns the activity identifier of this activity. Note that this identifier only exists after the activity has been
     * submitted.
     *
     * @return the activity identifier, or a dummy when the activity has not been submitted yet.
     */
    protected ActivityIdentifier identifier() {
        if (identifier == null) {
            throw new IllegalStateException("Activity has not been submitted yet");
        }

        return identifier;
    }

    public ActivityIdentifierImpl identifierImpl() {
        return identifier;
    }

    /**
     * Returns the executor of this activity. Note that this executor does not exist before {@link Activity#initialize()} has been
     * called.
     *
     * @throws IllegalStateException
     *             is thrown when the activity is not initialized yet.
     * @return the executor
     */
    protected Executor getExecutor() {

        if (executor == null) {
            throw new IllegalStateException("Activity is not initialized yet");
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
     * Returns <code>true</code> if this activity can only be executed by a local executor, <code>false</code> otherwise.
     *
     * @return whether this activity can only be executed by a local executor.
     */
    protected boolean isRestrictedToLocal() {
        return restrictToLocal;
    }

    /**
     * Resets the request state of this activity.
     */
    void reset() {
        next = REQUEST_UNKNOWN;
    }

    /**
     * Returns <code>true</code> if the activity requested to be suspended, <code>false</code> otherwise.
     *
     * @return whether the activity requested to be suspended.
     */
    boolean mustSuspend() {
        return (next == REQUEST_SUSPEND);
    }

    /**
     * Returns <code>true</code> if the activity requested to be finished, <code>false</code> otherwise.
     *
     * @return whether the activity requested to be finished.
     */
    boolean mustFinish() {
        return (next == REQUEST_FINISH);
    }

    /**
     * Requests that the current activity will be suspended. Usually called from {@link #initialize()} or {@link #process(Event)}.
     *
     * @exception IllegalStateException
     *                is thrown when the activity already requested to be finished.
     */
    protected void suspend() throws IllegalStateException {

        if (next == REQUEST_FINISH) {
            throw new IllegalStateException("ActivityBase already requested to finish!");
        }

        next = REQUEST_SUSPEND;
    }

    /**
     * Requests that the current activity will be finished. Usually called from {@link #initialize()} or {@link #process(Event)}.
     *
     * @exception IllegalStateException
     *                is thrown when the activity already requested to be suspended.
     */
    protected void finish() throws IllegalStateException {

        if (next == REQUEST_SUSPEND) {
            throw new IllegalStateException("ActivityBase already requested to suspend!");
        }

        next = REQUEST_FINISH;
    }

    // /**
    // * Todo never called???
    // */
    // public abstract void cancel();

    /**
     * This method, to be implemented by the activity, should perform the initial processing when the activity is first activated.
     * In the end, it should call {@link #suspend()} or {@link #finish()}, depending on what the activity is to do next:
     * {@link #suspend()} when it expects events it wants to wait for, and {@link #finish()} when it is done.
     *
     * Note that this method does not throw checked exceptions. It can, however, throw runtime exceptions or errors, and
     * constellation should deal with that.
     */
    public abstract void initialize();

    /**
     * This method, to be implemented by the activity, is called when the activity should handle the specified event. In the end,
     * it should call {@link #suspend()} or {@link #finish()}, depending on what the activity is to do next: {@link #suspend()}
     * when it expects other events, and {@link #finish()} when it is done.
     *
     * This method is invoked once at a time, even if more events arrive more or less simultaneously.
     *
     * Note that this method does not throw checked exceptions. It can, however, throw runtime exceptions or errors, and
     * constellation should deal with that.
     *
     * @param e
     *            the event.
     *
     */
    public abstract void process(Event e);

    /**
     * This method, to be implemented by the activity, is called when the activity is actually finished. It allows the activity,
     * for instance, to send events to its parent activity, and to otherwise cleanup.
     *
     * Note that this method does not throw checked exceptions. It can, however, throw runtime exceptions or errors, and
     * constellation should deal with that.
     */
    public abstract void cleanup();

    @Override
    public String toString() {
        return identifier + " " + context;
    }
}
