/*
 * Copyright 2018 Netherlands eScience Center
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.AbstractContext;
import ibis.constellation.Activity;
import ibis.constellation.Constellation;
import ibis.constellation.Event;

/**
 * A <code>MultiEventCollector</code> is an {@link Activity} that just waits for a specific number of events, and collects them.
 * It provides a method {@link #waitForEvents()}, to be used by other activities, to collect the events and block until the
 * specified number of events is reached, after which the <code>MultiEventCollector</code> will finish.
 */
public class MultiEventCollector extends Activity {

    public static final Logger logger = LoggerFactory.getLogger(MultiEventCollector.class);

    private static final long serialVersionUID = -538414301465754654L;

    private final Event[] events;
    private int count;

    /**
     * Constructs a <code>MultiEventCollector</code> with the specified activity context and event count. Note: this is an
     * activity that will receive events (see {@link Activity#Activity(AbstractContext, boolean)}).
     *
     * @param c
     *            the activity context of this event collector
     * @param events
     *            the number of events to be collected
     */
    public MultiEventCollector(AbstractContext c, int events) {
        super(c, false, true);
        this.events = new Event[events];
    }

    @Override
    public int initialize(Constellation c) {
        return SUSPEND;
    }

    @Override
    public synchronized int process(Constellation c, Event e) {

        if (logger.isDebugEnabled()) {
            logger.debug("MultiEventCollector: received event " + count + " of " + events.length);
        }

        events[count++] = e;

        if (count == events.length) {
            notifyAll();
            return FINISH;
        } else {
            return SUSPEND;
        }
    }

    @Override
    public void cleanup(Constellation c) {
        // empty
    }

    @Override
    public String toString() {
        return "MultiEventCollector(" + identifier() + ", " + events.length + ")";
    }

    /**
     * This method blocks waiting for the specified number of events. As soon as they are available, it creates an array
     * containing these events, and returns the array.
     *
     * @return an array containing the received events.
     */
    public synchronized Event[] waitForEvents() {
        while (count != events.length) {
            try {
                wait();
            } catch (Exception e) {
                // ignore
            }
        }

        return events;
    }

    /**
     * This method returns whether the event collector is finished, without blocking.
     *
     * @return whether the event collector is finished.
     */
    public synchronized boolean isFinished() {
        return count == events.length;
    }
}
