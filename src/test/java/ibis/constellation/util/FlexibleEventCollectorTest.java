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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.Context;
import ibis.constellation.Event;
import ibis.constellation.impl.ImplUtil;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class FlexibleEventCollectorTest {

    class Waiter extends Thread {

        private FlexibleEventCollector c;
        private Event[] res;

        Waiter(FlexibleEventCollector c) {
            this.c = c;
        }

        public synchronized void put(Event[] res) {
            this.res = res;
        }

        public synchronized Event[] get() {
            return res;
        }

        @Override
        public void run() {
            put(c.waitForEvents());
        }
    }

    public void sleep(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (Exception e) {
            // ignored
        }
    }

    @Test
    public void testCreate() {

        Context a = new Context("TEST", 0, 0);

        FlexibleEventCollector c = new FlexibleEventCollector(a);

        assertEquals(a, c.getContext());
    }

    @Test
    public void testInitialize() {

        Context a = new Context("TEST", 0, 0);

        Constellation c = ImplUtil.createFakeConstellation();

        FlexibleEventCollector e = new FlexibleEventCollector(a);

        int result = e.initialize(c);

        assertEquals(Activity.SUSPEND, result);
    }

    @Test
    public void testCleanup() {

        Context a = new Context("TEST", 0, 0);

        Constellation c = ImplUtil.createFakeConstellation();

        FlexibleEventCollector e = new FlexibleEventCollector(a);

        e.cleanup(c);
        assertTrue(c.isMaster());

        // TODO: nothing sensible to test for ?
    }

    @Test
    public void testToString() {

        Context a = new Context("TEST", 0, 0);

        FlexibleEventCollector c = new FlexibleEventCollector(a);

        String s = c.toString();

        assertEquals("FlexibleEventCollector(null)", s);
    }

    @Test
    public void addEvent() {

        Context a = new Context("TEST", 0, 0);

        FlexibleEventCollector c = new FlexibleEventCollector(a);

        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(0, 1, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(0, 2, 2, false);

        Constellation con = ImplUtil.createFakeConstellation();

        Event e = new Event(id1, id2, null);

        c.process(con, e);
        Event[] res = c.waitForEvents();

        assertArrayEquals(res, new Event[] { e });
    }

    @Test
    public void addEventMultiThreaded() {

        Context a = new Context("TEST", 0, 0);

        FlexibleEventCollector c = new FlexibleEventCollector(a);

        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(0, 1, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(0, 2, 2, false);

        Constellation con = ImplUtil.createFakeConstellation();

        Waiter w = new Waiter(c);
        w.start();

        // Give thread time to start.
        sleep(1);

        Event e = new Event(id1, id2, null);

        c.process(con, e);

        // Finish thread
        try {
            w.join();
        } catch (Exception ex) {
            // ignored
        }

        Event[] res = w.get();

        assertArrayEquals(res, new Event[] { e });
    }

}
