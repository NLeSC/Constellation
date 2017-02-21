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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;

import ibis.constellation.AbstractContext;
import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationCreationException;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.Context;
import ibis.constellation.Event;
import ibis.constellation.NoSuitableExecutorException;
import ibis.constellation.impl.ImplUtil;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class SimpleActivityTest {

    static class FakeActivity extends SimpleActivity {

        private static final long serialVersionUID = -6877538462329090090L;

        boolean hasRun = false;

        public FakeActivity(ActivityIdentifier parent, AbstractContext context) {
            super(parent, context);
        }

        public FakeActivity(ActivityIdentifier parent, AbstractContext context, boolean mayBeStolen) {
            super(parent, context, mayBeStolen);
        }

        public synchronized boolean hasRun() {
            return hasRun;
        }

        @Override
        public void simpleActivity(Constellation c) {
            synchronized (this) {
                hasRun = true;
            }
        }
    }

    private static Constellation createConstellation() throws ConstellationCreationException {

        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");

        Context a = new Context("A", 0, 0);

        ConstellationConfiguration e = new ConstellationConfiguration(a);

        return ConstellationFactory.createConstellation(p, e, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createTest() {
        new FakeActivity(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createTest2() {
        ActivityIdentifier a = ImplUtil.createActivityIdentifier(0, 0, 0, false);
        new FakeActivity(a, null);
    }

    @Test
    public void testGetParent() {
        ActivityIdentifier p = ImplUtil.createActivityIdentifier(0, 0, 0, false);
        Context c = new Context("A", 0, 0);
        SimpleActivity s = new FakeActivity(p, c);
        assertEquals(p, s.getParent());
    }

    @Test
    public void testGetParentNull() {
        Context c = new Context("A", 0, 0);
        FakeActivity a = new FakeActivity(null, c);
        assertNull(a.getParent());
    }

    @Test
    public void testGetContex() {
        ActivityIdentifier p = ImplUtil.createActivityIdentifier(0, 0, 0, false);
        Context c = new Context("A", 0, 0);
        SimpleActivity s = new FakeActivity(p, c);
        assertEquals(c, s.getContext());
    }

    @Test
    public void testGetIdentifier() {
        ActivityIdentifier p = ImplUtil.createActivityIdentifier(0, 0, 0, false);
        ActivityIdentifier a = ImplUtil.createActivityIdentifier(0, 0, 1, false);
        Context c = new Context("A", 0, 0);
        SimpleActivity s = new FakeActivity(p, c);
        s.setIdentifier(a);

        assertEquals(a, s.identifier());
    }

    @Test
    public void testInitialize() {
        ActivityIdentifier p = ImplUtil.createActivityIdentifier(0, 0, 0, false);
        ActivityIdentifier a = ImplUtil.createActivityIdentifier(0, 0, 1, false);
        Context c = new Context("A", 0, 0);

        Constellation co = ImplUtil.createFakeConstellation();

        FakeActivity s = new FakeActivity(p, c);
        s.setIdentifier(a);

        int result = s.initialize(co);

        assertEquals(Activity.FINISH, result);
        assertTrue(s.hasRun());
    }

    @Test
    public void testProcess() {
        ActivityIdentifier p = ImplUtil.createActivityIdentifier(0, 0, 0, false);
        ActivityIdentifier a = ImplUtil.createActivityIdentifier(0, 0, 1, false);
        Context c = new Context("A", 0, 0);

        Constellation co = ImplUtil.createFakeConstellation();

        FakeActivity s = new FakeActivity(p, c);
        s.setIdentifier(a);

        s.initialize(co);

        Event e = new Event(p, a, 1);

        int result = s.process(co, e);

        assertEquals(Activity.FINISH, result);
    }

    @Test
    public void testCleanup() {
        ActivityIdentifier p = ImplUtil.createActivityIdentifier(0, 0, 0, false);
        ActivityIdentifier a = ImplUtil.createActivityIdentifier(0, 0, 1, false);
        Context c = new Context("A", 0, 0);

        Constellation co = ImplUtil.createFakeConstellation();

        FakeActivity s = new FakeActivity(p, c);
        s.setIdentifier(a);

        s.initialize(co);
        s.process(co, new Event(p, a, 1));
        s.cleanup(co);

        assertTrue(s.hasRun());
    }

    @Test
    public void testSubmit() throws ConstellationCreationException, InterruptedException, NoSuitableExecutorException {

        Constellation c = createConstellation();
        c.activate();

        FakeActivity a = new FakeActivity(null, new Context("A", 0, 0));

        ActivityIdentifier id = c.submit(a);

        assertEquals(id, a.identifier());

        Thread.sleep(1000);

        assertTrue(a.hasRun());

        c.done();
    }

}
