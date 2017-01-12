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

package ibis.constellation.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.Event;
import ibis.constellation.context.ActivityContext;
import ibis.constellation.context.UnitActivityContext;
import ibis.constellation.impl.ImplUtil;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class MultiEventCollectorTest {

    class Waiter extends Thread {

        private MultiEventCollector c;
        private Event[] res;

        Waiter(MultiEventCollector c) {
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

        ActivityContext a = new UnitActivityContext("TEST");

        MultiEventCollector c = new MultiEventCollector(a, 1);

        assertEquals(a, c.getContext());
    }

    @Test
    public void testCreates() {

        MultiEventCollector c = new MultiEventCollector(1);

        assertEquals(UnitActivityContext.DEFAULT, c.getContext());
    }

    @Test
    public void testInitialize() {

        Constellation c = ImplUtil.createFakeConstellation();

        MultiEventCollector e = new MultiEventCollector(1);

        int result = e.initialize(c);

        assertEquals(Activity.SUSPEND, result);
    }

    @Test
    public void testCleanup() {

        Constellation c = ImplUtil.createFakeConstellation();

        MultiEventCollector e = new MultiEventCollector(1);

        e.cleanup(c);

        // TODO: nothing to test for ? 
    }

    @Test
    public void testToString() {

        MultiEventCollector c = new MultiEventCollector(1);

        String s = c.toString();

        assertEquals("MultiEventCollector(null, 1)", s);
    }

    @Test
    public void addEvent() {

        MultiEventCollector c = new MultiEventCollector(1);

        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(0, 1, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(0, 2, 2, false);

        Constellation con = ImplUtil.createFakeConstellation();

        Event e = new Event(id1, id2, null);

        assertFalse(c.isFinished());
        c.process(con, e);
        assertTrue(c.isFinished());
        Event[] res = c.waitForEvents();

        assertArrayEquals(res, new Event[] { e });
    }

    @Test
    public void addMultipleEvent() {

        MultiEventCollector c = new MultiEventCollector(4);

        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(0, 1, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(0, 2, 2, false);

        Constellation con = ImplUtil.createFakeConstellation();

        Event e = new Event(id1, id2, null);
        Event e2 = new Event(id2, id1, null);

        assertFalse(c.isFinished());
        c.process(con, e);
        assertFalse(c.isFinished());
        c.process(con, e2);
        assertFalse(c.isFinished());
        c.process(con, e);
        assertFalse(c.isFinished());
        c.process(con, e2);
        assertTrue(c.isFinished());

        Event[] res = c.waitForEvents();

        assertArrayEquals(res, new Event[] { e, e2, e, e2 });
    }

    @Test
    public void addEventMultiThreaded() {

        MultiEventCollector c = new MultiEventCollector(1);

        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(0, 1, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(0, 2, 2, false);

        Constellation con = ImplUtil.createFakeConstellation();

        Waiter w = new Waiter(c);
        w.start();

        // Give thread time to start.
        sleep(1);

        Event e = new Event(id1, id2, null);

        assertFalse(c.isFinished());
        c.process(con, e);
        assertTrue(c.isFinished());

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
