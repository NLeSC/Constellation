package ibis.constellation;

import java.io.Serializable;

/**
 * In Constellation, a program consists of a collection of loosely coupled
 * activities, which communicate using {@link Event Events}. Each
 * <code>Activity</code> represents an action that is to be performed by the
 * application, i.e. process some <code>Events</code>, or run a task.
 *
 * This class is the base class for all activities.
 */
public abstract class Activity implements Serializable {

    private static final long serialVersionUID = -83331265534440970L;

    private static final byte REQUEST_UNKNOWN = 0;
    private static final byte REQUEST_SUSPEND = 1;
    private static final byte REQUEST_FINISH = 2;

    private transient Executor executor;

    private ActivityIdentifier identifier;
    private final ActivityContext context;

    private final boolean restrictToLocal;
    private final boolean willReceiveEvents;

    private byte next = REQUEST_UNKNOWN;

    /**
     * Initializes this <code>Activity</code> with the specified parameters.
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
    protected Activity(ActivityContext context, boolean restrictToLocal,
            boolean willReceiveEvents) {
        this.context = context;
        this.restrictToLocal = restrictToLocal;
        this.willReceiveEvents = willReceiveEvents;
    }

    /**
     * Initializes this <code>Activity</code> with the specified parameters.
     * This version calls
     * {@link Activity#Activity(ActivityContext, boolean, boolean)}, with
     * <code>false</code> for the <code>restrictToLocal</code> parameter.
     *
     * @param context
     *            the context that specifies which executors can actually
     *            execute this activity.
     * @param willReceiveEvents
     *            when set, specifies that this activity can receive events.
     */
    protected Activity(ActivityContext context, boolean willReceiveEvents) {
        this(context, false, willReceiveEvents);
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

    /**
     * <strong>This method is not part of the user interface!</strong>.
     * Initializes the activity identifier.
     *
     * @param id
     *            the activity identifier to initialize with.
     */
    public void initialize(ActivityIdentifier id) {
        this.identifier = id;
    }

    /**
     * <strong>This method is not part of the user interface!</strong>.
     * Initializes the executor field.
     *
     * @param executor
     *            the executor to initialize with.
     */
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    /**
     * Returns the activity identifier of this activity.
     *
     * @return the activity identifier
     * @exception IllegalStateException
     *                is thrown when the activity is not initialized yet.
     */
    public ActivityIdentifier identifier() {

        if (identifier == null) {
            throw new IllegalStateException("Activity is not initialized yet");
        }

        return identifier;
    }

    /**
     * Returns the executor of this activity.
     *
     * @return the executor
     * @exception IllegalStateException
     *                is thrown when the activity is not initialized yet.
     */
    public Executor getExecutor() {

        if (executor == null) {
            throw new IllegalStateException("Activity is not initialized yet");
        }

        return executor;
    }

    /**
     * Submits an activity using the executor of the current activity.
     *
     * @param job
     *            the activity to be submitted
     * @return the activity identifier of the submitted activity
     */
    public ActivityIdentifier submit(Activity job) {
        return getExecutor().submit(job);
    }

    /**
     * Sends an event using the executor of the current activity.
     *
     * TODO: what if the destination cannot be found? The send() is probably
     * asynchronous.
     *
     * @param e
     *            the event to be sent
     */
    public void send(Event e) {
        getExecutor().send(e);
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
    public boolean isRestrictedToLocal() {
        return restrictToLocal;
    }

    /**
     * Resets the request state of this activity. Usually not called by the
     * application.
     */
    public void reset() {
        next = REQUEST_UNKNOWN;
    }

    /**
     * Returns <code>true</code> if the activity requested to be suspended,
     * <code>false</code> otherwise.
     *
     * @return whether the activity requested to be suspended.
     */
    public boolean mustSuspend() {
        return (next == REQUEST_SUSPEND);
    }

    /**
     * Returns <code>true</code> if the activity requested to be finished,
     * <code>false</code> otherwise.
     *
     * @return whether the activity requested to be finished.
     */
    public boolean mustFinish() {
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
    public void suspend() {

        if (next == REQUEST_FINISH) {
            throw new IllegalStateException(
                    "Activity already requested to finish!");
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
    public void finish() {

        if (next == REQUEST_SUSPEND) {
            throw new IllegalStateException(
                    "Activity already requested to suspend!");
        }

        next = REQUEST_FINISH;
    }

    /**
     * This method, to be implemented by the activity, should perform the
     * initial processing when the activity is first activated. In the end, it
     * should call {@link #suspend()} or {@link #finish()}, depending on what
     * the activity is to do next: {@link #suspend()} when it expects events it
     * wants to wait for, and {@link #finish()} when it is done.
     *
     * Note that this method does not throw checked exceptions. It can, however,
     * throw runtime exceptions or errors, and constellation should deal with
     * that.
     */
    public abstract void initialize();

    /**
     * This method, to be implemented by the activity, is called when the
     * activity should handle the specified event. In the end, it should call
     * {@link #suspend()} or {@link #finish()}, depending on what the activity
     * is to do next: {@link #suspend()} when it expects other events, and
     * {@link #finish()} when it is done.
     *
     * Note that this method does not throw checked exceptions. It can, however,
     * throw runtime exceptions or errors, and constellation should deal with
     * that.
     * 
     * @param e
     *            the event.
     *
     */
    public abstract void process(Event e);

    /**
     * This method, to be implemented by the activity, is called when the
     * activity is actually finished. It allows the activity, for instance, to
     * send events to its parent activity, and to otherwise cleanup.
     *
     * Note that this method does not throw checked exceptions. It can, however,
     * throw runtime exceptions or errors, and constellation should deal with
     * that.
     */
    public abstract void cleanup();

    // /**
    // * Todo never called???
    // */
    // public abstract void cancel();

    @Override
    public String toString() {
        return identifier + " " + context;
    }
}
