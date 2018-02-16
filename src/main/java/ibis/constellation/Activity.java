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

import java.io.Serializable;

import ibis.constellation.impl.ActivityIdentifierImpl;

/**
 * In Constellation, a program consists of a collection of loosely coupled activities, which may communicate using {@link Event
 * Events}. Each <code>Activity</code> represents an action that is to be performed by the application, i.e. run a task or process
 * some <code>Events</code>.
 *
 * This class is the base class for all application activities. Applications should extend this class and implement the
 * {@link initialize}, {@link process} and {@link cleanup} methods.
 *
 * After creating an Activity, it can be submitted to Constellation using {@link Constellation.submit}. During this submit, a
 * globally unique {@link ActivityIdentifier} will be assigned to the Activity. Using {@link setIdentifier} this identifier will
 * be stored in the Activity. Use the {@link identifier} method to retrieve it.
 *
 * The Activity will be scheduled to run on a Constellation that matches the {@link ActivityContext}. When it starts running,
 * {@link initialize} will be invoked once. This method may perform any processing that is needed, but should not block
 * indefinitely as this may result in deadlocks. When finished, the method should either return {@link FINISH} of {@link SUSPEND}.
 *
 * By returning {@link FINISH} the Activity indicates it no further processing is needed. By returning {@link SUSPEND} the
 * Activity indicates it expects an {@link Event}. The Activity is then suspend by Constellation, until the {@Event} arrives.
 *
 * Upon arrival of the {@link Event}, the {@link process} method will be invoked. After processing the event, {@link process} can
 * indicate if more events are expected (by returning {@link SUSPEND} or if the Activity is done (by returning {@FINISH}.
 *
 * After {@link FINISH} is returned by either {@link initialize} of {@link process}, the {@link cleanup} method is invoked. After
 * this method returns, the Activity is finished and will nore receive any more processingtime.
 *
 */
public abstract class Activity implements Serializable {

    private static final long serialVersionUID = -83331265534440970L;

    /**
     * Value to be returned by {@link #initialize()} or {@link #process()} when no further processing is needed.
     */
    public static final int FINISH = 0;

    /**
     * Value to be returned by {@link #initialize()} or {@link #process()} when (further) events are expected.
     */
    public static final int SUSPEND = 1;

    private final AbstractContext context;
    private final boolean mayBeStolen;
    private final boolean expectsEvents;

    private ActivityIdentifier identifier;

    /**
     * Create an Activity with a specified context, and indicate if this Activity may be stolen by other Constellations, and if it
     * expects to receive Events.
     *
     * @param context
     *            the context in which this activity should be run.
     * @param mayBeStolen
     *            if this activity may be stolen by other Constellations
     * @param expectsEvents
     *            if this Activity expects events
     */
    public Activity(AbstractContext context, boolean mayBeStolen, boolean expectsEvents) {

        if (context == null) {
            throw new IllegalArgumentException("Activity must have a context");
        }

        this.context = context;
        this.mayBeStolen = mayBeStolen;
        this.expectsEvents = expectsEvents;
    }

    /**
     * Create an Activity with a specified context, and specify if it expects to receive Events. When using this constructor, it
     * is assumed that this Activity is allowed to be stolen by other Constellations.
     *
     * @param context
     *            the context in which this activity should be run.
     * @param expectsEvents
     *            if this Activity expects events
     */
    public Activity(AbstractContext context, boolean expectsEvents) {
        this(context, true, expectsEvents);
    }

    /**
     * This is a callback method used by the Constellation to assign a globally unique {@link ActivityIdentifier} to this
     * Activity. This method may only be invoked once, since the identifier may not change once set. Any subsequent invocation
     * will result in an {@link IllegalStateException}.
     *
     * @param identifier
     *            the globally unique {@link ActivityIdentifier}
     */
    public void setIdentifier(ActivityIdentifier identifier) {

        if (identifier == null) { 
            throw new IllegalArgumentException("ActivityIdentifier may not be null");
        }
        
        if (this.identifier != null) {
            throw new IllegalStateException("ActivityIdentifier already set");
        }
        
        if (!(identifier instanceof ActivityIdentifierImpl)) { 
            throw new IllegalArgumentException("Unknown ActivityIdentifier implementation");
        }
        
        if (((ActivityIdentifierImpl) identifier).expectsEvents() != expectsEvents) { 
            throw new IllegalStateException("ActivityIdentifier expects events does not match Activity");
        }
        
        this.identifier = identifier;
    }

    /**
     * Returns the globally unique {@link ActivityIdentifier} assigned to this Activity, or <code>null</code> if this Activity has
     * not been submitted yet.
     *
     * @return the globally unique {@link ActivityIdentifier} of this Activity or <code>null</code>.
     */
    public ActivityIdentifier identifier() {
        return identifier;
    }

    /**
     * Returns the {@link ActivityContext} of this Activity.
     *
     * @return the {@link ActivityContext} of this Activity.
     */
    public AbstractContext getContext() {
        return context;
    }

    /**
     * Returns if this Activity may be stolen by another {@link Constellation}.
     *
     * @return if this Activity may be stolen by another {@link Constellation}.
     */
    public boolean mayBeStolen() {
        return mayBeStolen;
    }

    /**
     * Returns if this Activity expects {@link Event}s.
     *
     * @return if this Activity expects {@link Event}s.
     */
    public boolean expectsEvents() {
        return expectsEvents;
    }

    /**
     * The implementation of this method should perform the initial processing when the activity is first activated. It may
     * perform as much processing as needed, but should not block for a prolonged period, as this may result in deadlocks.
     *
     * When finished, it should return either {@link #SUSPEND} or {@link #FINISH}, depending on what the activity is to do next:
     * {@link #sSUSPEND} when it wants to wait for events, and {@link #FINISH} when it is done.
     *
     * Note that this method does not throw checked exceptions. It can, however, throw runtime exceptions or errors, and the
     * {@link Constellation} running this Activity will deal with that.
     *
     * @param constellation
     *            the {@link Constellation} on which the Activity is running
     *
     * @return either {@link FINISH} if this Activity is done or {@link SUSPEND} if this Activity expects events.
     */
    public abstract int initialize(Constellation constellation);

    /**
     * The implementation of this method is called when an event is received for this activity. After the event is processed it
     * must return {@link #SUSPEND} or {@link #FINISH}, depending on what the activity is to do next: {@link #SUSPEND} when it
     * expects other events, and {@link #FINISH} when it is done.
     *
     * This method is invoked once at a time, even if more events arrive more or less simultaneously.
     *
     * Note that this method does not throw checked exceptions. It can, however, throw runtime exceptions or errors, and the
     * {@link Constellation} running this Activity will deal with that.
     *
     * @param constellation
     *            the {@link Constellation} on which the Activity is running
     * @param event
     *            the {@link Event} to process.
     * @return either {@link FINISH} if this Activity is done or {@link SUSPEND} if this Activity expects events.
     */
    public abstract int process(Constellation constellation, Event event);

    /**
     * The implementation of this method is called when the activity is finished. It allows the activity to perform cleanup, and,
     * for example, send events to its parent activity.
     *
     * Note that this method does not throw checked exceptions. It can, however, throw runtime exceptions or errors, and the
     * {@link Constellation} running this Activity will deal with that.
     *
     * @param constellation
     *            the {@link Constellation} on which the Activity is running
     */
    public abstract void cleanup(Constellation constellation);
}
