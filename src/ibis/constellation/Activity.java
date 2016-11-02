package ibis.constellation;

import java.io.Serializable;

import ibis.constellation.impl.ActivityBase;

/**
 * In Constellation, a program consists of a collection of loosely coupled
 * activities, which communicate using {@link Event Events}. Each
 * <code>Activity</code> represents an action that is to be performed by the
 * application, i.e. process some <code>Events</code>, or run a task.
 *
 * This class is the base class for all activities.
 */
public abstract class Activity extends ActivityBase implements Serializable {

    private static final long serialVersionUID = -83331265534440970L;

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
        super(context, restrictToLocal, willReceiveEvents);
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
     * Returns the activity identifier of this activity.
     *
     * @return the activity identifier
     * @exception IllegalStateException
     *                is thrown when the activity is not initialized yet.
     */
    @Override
    public ActivityIdentifier identifier() {
        return super.identifier();
    }

    /**
     * Returns the executor of this activity.
     *
     * @return the executor
     * @exception IllegalStateException
     *                is thrown when the activity is not initialized yet.
     */
    @Override
    public Executor getExecutor() {
        return super.getExecutor();
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
     * Note that if the destination cannot be found, no exception will result,
     * since the implementation is probably asynchronous.
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
    @Override
    public ActivityContext getContext() {
        return super.getContext();
    }

    /**
     * Requests that the current activity will be suspended. Usually called from
     * {@link #initialize()} or {@link #process(Event)}.
     *
     * @exception IllegalStateException
     *                is thrown when the activity already requested to be
     *                finished.
     */
    @Override
    public void suspend() {
        super.suspend();
    }

    /**
     * Requests that the current activity will be finished. Usually called from
     * {@link #initialize()} or {@link #process(Event)}.
     *
     * @exception IllegalStateException
     *                is thrown when the activity already requested to be
     *                suspended.
     */
    @Override
    public void finish() {
        super.finish();
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
     * This method is invoked once at a time, even if more events arrive more or
     * less simultaneously.
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
}
