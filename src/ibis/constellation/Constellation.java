package ibis.constellation;

/**
 * Main interface to Constellation.
 *
 * @version 1.0
 * @since 1.0
 */
public interface Constellation {

    /**
     * Submit an activity.
     *
     * The submitted ActivityBase will be inserted into Constellation and executed
     * if and when a suitable Executor is found. An ActivityIdentifierImpl is
     * returned that can be used to refer to this submitted ActivityBase at a later
     * moment in time.
     *
     * It is up to the user to make sure that this constellation instance has a
     * suitable executor, or, if the contexts don't match, an executor that can
     * be stolen from.
     *
     * @param job
     *            the ActivityBase to submit
     * @return ActivityIdentifierImpl that can be used to refer to the submitted
     *         ActivityBase.
     */
    public ActivityIdentifier submit(Activity job);

    /**
     * Send an event.
     *
     * @param e
     *            the Event to send.
     */
    public void send(Event e);

    // /**
    // * Cancel an ActivityBase.
    // *
    // * Todo: figure out and describe semantics of this.
    // *
    // * @param activity
    // * activity to be cancelled
    // */
    // public void cancel(ActivityIdentifierImpl activity);

    /**
     * Activate this Constellation implementation.
     *
     * Constellation instances start out in in inactive state when they are
     * created. This allows the application to configure Constellation (for
     * example, by setting up the desired combination of distributed and local
     * constellation instances).
     *
     * Upon activation, the Constellation instance will activate all
     * sub-constellations, and activate its own executors, steal pools, event
     * queues, etc.
     *
     * @return if the Constellation was activated.
     */
    public boolean activate();

    /**
     * Terminate Constellation.
     *
     * When terminating all sub-constellations will be terminated. Termination
     * may block until all other running constellation implementations in a Pool
     * have also decided to terminate. When this is the case, the
     * {@link Concluder#conclude()} method is called, allowing the application
     * to run some finalization code of its own.
     *
     * @param concluder
     *            object with a {@link Concluder#conclude()} method
     */
    public void done(Concluder concluder);

    /**
     * Terminate Constellation.
     *
     * When terminating all sub-constellations will be terminated. Termination
     * may also block until all other running constellation instances in a Pool
     * have also decided to terminate.
     */
    public void done();

    /**
     * Returns <code>true</code> if this Constellation instance is the master,
     * <code>false</code> otherwise.
     *
     * @return whether this Constellation instance is the master.
     */
    public boolean isMaster();

    /**
     * Returns a unique identifier for this Constellation instance.
     *
     * This identifier can be used to uniquely refer to a running Constellation
     * instance.
     *
     * @return a string that uniquely identifies this Constellation instance.
     */
    public String identifier();

    public CTimer getTimer(String device, String thread, String action);

    public CTimer getTimer();

    public CTimer getOverallTimer();
}
