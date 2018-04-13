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
 * A <code>SingleEventCollector</code> is an {@link Activity} that just waits for a single event, and saves it. It provides a
 * method {@link #waitForEvent()}, to be used by other activities, to collect the event and block until it arrives, after which
 * the <code>SingleEventCollector</code> will finish.
 */
public class SingleEventCollector extends Activity {

    public static final Logger logger = LoggerFactory.getLogger(SingleEventCollector.class);

    private static final long serialVersionUID = -538414301465754654L;

    private Event event;

    /**
     * Constructs a <code>SingleEventCollector</code> with the specified activity context. Note: this is an activity that will
     * receive events (see {@link Activity#Activity(AbstractContext, boolean)}).
     *
     * @param c
     *            the activity context of this event collector
     */
    public SingleEventCollector(AbstractContext c) {
        super(c, false, true);
    }

    @Override
    public int initialize(Constellation c) {
        if (logger.isDebugEnabled()) {
            logger.debug("Single event collector " + identifier() + " started.");
        }
        return SUSPEND;
    }

    @Override
    public synchronized int process(Constellation c, Event e) {

        if (logger.isDebugEnabled()) {
            logger.debug("Single event collector " + identifier() + " got result!");
        }

        event = e;
        notifyAll();
        return FINISH;
    }

    @Override
    public void cleanup(Constellation c) {
        // empty
    }

    @Override
    public String toString() {
        return "SingleEventCollector(" + identifier() + ")";
    }

    /**
     * This method blocks waiting for this object to receive an event. As soon as it is available, it returns the event.
     *
     * @return the received event.
     */
    public synchronized Event waitForEvent() {
        while (event == null) {
            try {
                wait();
            } catch (Exception e) {
                // ignore
            }
        }

        return event;
    }

    /**
     * This method returns whether the event collector is finished, without blocking.
     *
     * @return whether the event collector is finished.
     */
    public synchronized boolean isFinished() {
        return event != null;
    }
}
