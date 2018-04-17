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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
public class SingleEventCollectorTest {

    class Waiter extends Thread {

        private SingleEventCollector c;
        private Event res;

        Waiter(SingleEventCollector c) {
            this.c = c;
        }

        public synchronized void put(Event res) {
            this.res = res;
        }

        public synchronized Event get() {
            return res;
        }

        @Override
        public void run() {
            put(c.waitForEvent());
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

        SingleEventCollector c = new SingleEventCollector(a);

        assertEquals(a, c.getContext());
    }

    @Test
    public void testInitialize() {

        Constellation c = ImplUtil.createFakeConstellation();

        Context a = new Context("TEST", 0, 0);

        SingleEventCollector e = new SingleEventCollector(a);

        int result = e.initialize(c);

        assertEquals(Activity.SUSPEND, result);
    }

    @Test
    public void testCleanup() {

        Constellation c = ImplUtil.createFakeConstellation();

        Context a = new Context("TEST", 0, 0);

        SingleEventCollector e = new SingleEventCollector(a);

        e.cleanup(c);

        assertTrue(c.isMaster());

        // TODO: nothing sensible to test for ?
    }

    @Test
    public void testToString() {

        Context a = new Context("TEST", 0, 0);

        SingleEventCollector c = new SingleEventCollector(a);

        String s = c.toString();

        assertEquals("SingleEventCollector(null)", s);
    }

    @Test
    public void addEvent() {

        Context a = new Context("TEST", 0, 0);

        SingleEventCollector c = new SingleEventCollector(a);

        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(0, 1, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(0, 2, 2, false);

        Constellation con = ImplUtil.createFakeConstellation();

        Event e = new Event(id1, id2, null);

        assertFalse(c.isFinished());

        c.process(con, e);

        assertTrue(c.isFinished());

        Event res = c.waitForEvent();

        assertEquals(res, e);
    }

    @Test
    public void addEventMultiThreaded() {

        Context a = new Context("TEST", 0, 0);

        SingleEventCollector c = new SingleEventCollector(a);

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

        // Finish thread
        try {
            w.join();
        } catch (Exception ex) {
            // ignored
        }

        assertTrue(c.isFinished());

        Event res = w.get();

        assertEquals(res, e);
    }

}
