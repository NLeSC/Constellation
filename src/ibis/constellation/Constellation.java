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
     * The submitted Activity will be inserted into Constellation and executed if and when a suitable Executor is found. An
     * ActivityIdentifier is returned that can be used to refer to this submitted Activity at a later moment in time.
     *
     * It is up to the user to make sure that this constellation instance has a suitable executor, or, if the contexts don't
     * match, an executor that can be stolen from.
     *
     * @param job
     *            the Activity to submit
     * @return ActivityIdentifier that can be used to refer to the submitted Activity.
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
    // * Cancel an Activity.
    // *
    // * Todo: figure out and describe semantics of this.
    // *
    // * @param activity
    // * activity to be cancelled
    // */
    // public void cancel(ActivityIdentifier activity);

    /**
     * Activate this Constellation implementation.
     *
     * Constellation instances start out in in inactive state when they are created. This allows the application to configure
     * Constellation (for example, by setting up the desired combination of distributed and local constellation instances).
     *
     * Upon activation, the Constellation instance will activate all sub-constellations, and activate its own executors, steal
     * pools, event queues, etc.
     *
     * @return if the Constellation was activated.
     */
    public boolean activate();

    /**
     * Terminate Constellation.
     *
     * When terminating all sub-constellations will be terminated. Termination may also block until all other running
     * constellation instances in a Pool have also decided to terminate.
     */
    public void done();

    /**
     * Returns <code>true</code> if this Constellation instance is the master, <code>false</code> otherwise.
     *
     * @return whether this Constellation instance is the master.
     */
    public boolean isMaster();

    /**
     * Returns a unique identifier for this Constellation instance.
     *
     * This identifier can be used to uniquely refer to a running Constellation instance.
     *
     * @return a string that uniquely identifies this Constellation instance.
     */
    public String identifier();

    /**
     * Creates a {@link CTimer} with the specified device, thread, and action name.
     *
     * @param device
     *            the device name
     * @param thread
     *            the thread name
     * @param action
     *            the action name
     * @return the CTimer object
     */
    public CTimer getTimer(String device, String thread, String action);

    /**
     * Creates a {@link CTimer} without device, thread, or action name.
     *
     * @return the CTimer object
     */
    public CTimer getTimer();

    /**
     * Returns the overall timer.
     *
     * This timer needs to be started and stopped by the main program.
     *
     * @return the overall timer.
     */
    public CTimer getOverallTimer();
}
