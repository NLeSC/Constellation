package ibis.constellation;

/**
 * The CTimer interface provides user-access to the timers used internally in constellation. Applications can create CTimers
 * through {@link Constellation#getTimer()} or {@link Constellation#getTimer(String, String, String)}. Of particular interest is
 * the so-called overall timer, to be accessed by {@link Constellation#getOverallTimer()}, and to be started when the application
 * starts and stopped when the application finishes.
 *
 * A CTimer records various times, such as queued, submitted, start, stop (similar to openCL events), but usually only start and
 * stop are used. Each timer event also has associated with it a device name (for instance "java" or "gtx480"), a thread name
 * (usually the executor identifier), and an action name, for instance "initialize" or "process".
 *
 * Constellation uses some properties to control the behavior of the timers. The {@link ConstellationProperties#PROFILE} property
 * controls the internal timing, and when set will provide timings for, a.o., the invocations of {@link Activity#initialize()} and
 * {@link Activity#process(Event)}. In the end, the timing events will be written to a file specified with the
 * {@link ConstellationProperties#OUTPUT} property, or to <code>System.out</code>.
 */
public interface Timer {

    /**
     * Starts a timer event.
     *
     * @return the event number, to be used when the timer is stopped.
     */
    int start();

    /**
     * Stops a timer event. Unused event numbers are silently ignored.
     *
     * @param eventNo
     *            the event number.
     */
    void stop(int eventNo);

    /**
     * Adds a completed event to the CTimer. This may be useful if for instance the time values are recorded by a GPU, or come
     * from another source. Note that the times are to be provided in nanoseconds and that, to get meaningful results with respect
     * to other events, the provided times need to be "in sync" with {@link System#nanoTime()}.
     *
     * @param device
     *            the device name
     * @param thread
     *            the thread name
     * @param action
     *            the action name
     * @param queued
     *            time when event was queued, in nanoseconds
     * @param submitted
     *            time when event was submitted, in nanoseconds
     * @param start
     *            time when event was started, in nanoseconds
     * @param end
     *            time when event finished, in nanoseconds
     */
    void add(String device, String thread, String action, long queued, long submitted, long start, long end);

    /**
     * Returns the number of events recorded for this CTimer.
     *
     * @return the number of events.
     */
    int nrTimes();

    /**
     * Returns the total time recorded for events in this CTimer, in microseconds.
     *
     * @return the total time.
     */
    double totalTimeVal();

    /**
     * Returns the average time recorded for events in this CTimer, in microseconds.
     *
     * @return the average time.
     */
    public double averageTimeVal();
}
