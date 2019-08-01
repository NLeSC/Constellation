/*
 * Copyright 2019 Vrije Universiteit Amsterdam
 *                Netherlands eScience Center
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
package ibis.constellation.util;

import java.util.ArrayList;

import ibis.constellation.AbstractContext;
import ibis.constellation.Activity;
import ibis.constellation.Constellation;
import ibis.constellation.Event;

/**
 * A <code>FlexibleEventCollector</code> is an {@link Activity} that just waits for events, indefinitely, and collects them. It
 * provides a method {@link #waitForEvents()}, to be used by other activities, to collect the events collected so far.
 */
public class FlexibleEventCollector extends Activity {

    private static final long serialVersionUID = -538414301465754654L;

    private final ArrayList<Event> events = new ArrayList<Event>();
    private boolean waiting = false;

    /**
     * Constructs a <code>FlexibleEventCollector</code> with the specified activity context. Note: this is an activity that will
     * receive events (see {@link Activity#Activity(AbstractContext, boolean)}).
     *
     * @param c
     *            the activity context
     */
    public FlexibleEventCollector(AbstractContext c) {
        super(c, false, true);
    }

    @Override
    public int initialize(Constellation c) {
        return SUSPEND;
    }

    @Override
    public synchronized int process(Constellation c, Event e) {

        events.add(e);

        if (waiting) {
            notifyAll();
        }

        return SUSPEND;
    }

    @Override
    public void cleanup(Constellation c) {
        // empty
    }

    @Override
    public String toString() {
        return "FlexibleEventCollector(" + identifier() + ")";
    }

    /**
     * This method blocks waiting for events. As soon as one or more are available, it creates an array containing these events,
     * clears the event list of this collector, and returns the array.
     *
     * @return an array containing the received events.
     */
    public synchronized Event[] waitForEvents() {

        while (events.size() == 0) {

            waiting = true;

            try {
                wait();
            } catch (Throwable e) {
                // ignore
            }

            waiting = false;
        }

        Event[] result = events.toArray(new Event[events.size()]);

        events.clear();

        return result;
    }
}
