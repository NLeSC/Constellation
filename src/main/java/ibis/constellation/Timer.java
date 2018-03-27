/**
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ibis.constellation;

/**
 * The Timer interface provides user-access to the timing mechanism used internally in constellation. Applications can create
 * Timers through {@link Constellation#getTimer()} or {@link Constellation#getTimer(String, String, String)}. Of particular
 * interest is the so-called "overall timer", to be accessed by {@link Constellation#getOverallTimer()}, and to be started when
 * the application starts and stopped when the application finishes.
 *
 * A Timer records various times, such as queued, submitted, start, stop (similar to openCL events), but usually only start and
 * stop are used. Each timer event also has associated with it a device name (for instance "java" or "gtx480"), a thread name
 * (usually the executor identifier), and an action name, for instance "initialize" or "process".
 *
 * Constellation uses some properties to control the behavior of the timers. The {@link ConstellationProperties#PROFILE} property
 * controls the timing. When the {@link ConstellationProperties#PROFILE_ACTIVITY} is set timings will be provided for, a.o., the
 * invocations of {@link Activity#initialize} and {@link Activity#process}. In the end, the timing events will be written
 * to a file specified with the {@link ConstellationProperties#PROFILE_OUTPUT} property, or to <code>System.out</code>.
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
     * Adds a completed event to the Timer. This may be useful if for instance the time values are recorded by a GPU, or come from
     * another source. Note that the times are to be provided in nanoseconds and that, to get meaningful results with respect to
     * other events, the provided times need to be "in sync" with {@link System#nanoTime()}.
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
     * Adds a completed event to the Timer. This may be useful if for instance the time values are recorded by a GPU, or come from
     * another source. Note that the times are to be provided in nanoseconds and that, to get meaningful results with respect to
     * other events, the provided times need to be "in sync" with {@link System#nanoTime()}.
     *
     * This version has no separate queued or submitted time. These are assumed to be equal to the start time. The device, thread
     * and action names are taken from the timer itself.
     *
     * @param start
     *            time when event was started, in nanoseconds
     * @param end
     *            time when event finished, in nanoseconds
     */
    void add(long start, long end);

    /**
     * Returns the number of events recorded for this Timer.
     *
     * @return the number of events.
     */
    int nrTimes();

    /**
     * Returns the total time recorded for events in this Timer, in microseconds.
     *
     * @return the total time.
     */
    double totalTimeVal();

    /**
     * Returns the average time recorded for events in this Timer, in microseconds.
     *
     * @return the average time.
     */
    public double averageTimeVal();
}
