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
package ibis.constellation.impl.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ibis.constellation.Context;
import ibis.constellation.OrContext;
import ibis.constellation.StealStrategy;
import ibis.constellation.impl.ActivityRecord;
import ibis.constellation.impl.ImplUtil;

public class WorkQueueTest {

    @Test
    public void testStealBiggest() {
        ActivityRecord r = ImplUtil.createActivityRecord();
        WorkQueue q = new SimpleWorkQueue("queue");
        q.enqueue(r);
        ActivityRecord tmp = q.steal(r.getContext(), StealStrategy.BIGGEST);
        assertEquals(r, tmp);
        tmp = q.steal(r.getContext(), StealStrategy.BIGGEST);
        assertNull(tmp);
    }

    @Test
    public void testStealSmallest() {
        ActivityRecord r = ImplUtil.createActivityRecord();
        WorkQueue q = new SimpleWorkQueue("queue");
        q.enqueue(r);
        ActivityRecord tmp = q.steal(r.getContext(), StealStrategy.SMALLEST);
        assertEquals(r, tmp);
        tmp = q.steal(r.getContext(), StealStrategy.SMALLEST);
        assertNull(tmp);
    }

    @Test
    public void testStealOr1() {
        Context a = new Context("A");
        Context b = new Context("B");
        ActivityRecord r = ImplUtil.createActivityRecord(new OrContext(a, b));
        WorkQueue q = new SimpleWorkQueue("queue");
        q.enqueue(r);
        ActivityRecord tmp = q.steal(a, StealStrategy.SMALLEST);
        assertEquals(r, tmp);
        tmp = q.steal(r.getContext(), StealStrategy.SMALLEST);
        assertNull(tmp);
    }

    @Test
    public void testStealOr2() {
        Context a = new Context("A");
        Context b = new Context("B");
        ActivityRecord rab = ImplUtil.createActivityRecord(new OrContext(a, b));
        ActivityRecord ra = ImplUtil.createActivityRecord(a);
        ActivityRecord rb = ImplUtil.createActivityRecord(b);

        WorkQueue q = new SimpleWorkQueue("queue");
        q.enqueue(ra);
        q.enqueue(rb);
        q.enqueue(rab);
        ActivityRecord tmp1 = q.steal(a, StealStrategy.SMALLEST);
        assertNotEquals(tmp1, rb);
        ActivityRecord tmp2 = q.steal(b, StealStrategy.SMALLEST);
        assertNotEquals(tmp1, tmp2);
        assertNotEquals(tmp2, ra);
        ActivityRecord tmp = q.steal(rab.getContext(), StealStrategy.SMALLEST);
        assertNotEquals(tmp, tmp1);
        assertNotEquals(tmp, tmp2);
        tmp = q.steal(rab.getContext(), StealStrategy.SMALLEST);
        assertNull(tmp);
    }

    @Test
    public void testStealSmallest1() {
        Context a = new Context("A");
        Context a1 = new Context("A", 1);
        Context a2 = new Context("A", 2);
        ActivityRecord ra1 = ImplUtil.createActivityRecord(a1);
        ActivityRecord ra2 = ImplUtil.createActivityRecord(a2);

        WorkQueue q = new SimpleWorkQueue("queue");
        q.enqueue(ra1);
        q.enqueue(ra2);
        ActivityRecord tmp1 = q.steal(a, StealStrategy.SMALLEST);
        assertEquals(tmp1, ra1);
        q.enqueue(tmp1);
        ActivityRecord tmp2 = q.steal(a, StealStrategy.SMALLEST);
        assertEquals(tmp2, tmp1);
    }

    @Test
    public void testStealBiggest1() {
        Context a = new Context("A");
        Context a1 = new Context("A", 1);
        Context a2 = new Context("A", 2);
        ActivityRecord ra1 = ImplUtil.createActivityRecord(a1);
        ActivityRecord ra2 = ImplUtil.createActivityRecord(a2);

        WorkQueue q = new SimpleWorkQueue("queue");
        q.enqueue(ra1);
        q.enqueue(ra2);
        ActivityRecord tmp1 = q.steal(a, StealStrategy.BIGGEST);
        assertEquals(tmp1, ra2);
        q.enqueue(tmp1);
        ActivityRecord tmp2 = q.steal(a, StealStrategy.BIGGEST);
        assertEquals(tmp2, tmp1);
    }

    @Test
    public void testStealSmallest2() {
        Context a = new Context("A", 1);
        Context a1 = new Context("A", -4, 4);
        Context a2 = new Context("A", -3, 3);
        ActivityRecord ra1 = ImplUtil.createActivityRecord(a1);
        ActivityRecord ra2 = ImplUtil.createActivityRecord(a2);

        WorkQueue q = new SimpleWorkQueue("queue");
        q.enqueue(ra1);
        q.enqueue(ra2);
        ActivityRecord tmp1 = q.steal(a, StealStrategy.SMALLEST);
        assertEquals(tmp1, ra1);
        q.enqueue(tmp1);
        ActivityRecord tmp2 = q.steal(a, StealStrategy.SMALLEST);
        assertEquals(tmp2, tmp1);
    }

    @Test
    public void testStealBiggest2() {
        Context a = new Context("A", 1);
        Context a1 = new Context("A", -4, 4);
        Context a2 = new Context("A", -3, 3);

        ActivityRecord ra1 = ImplUtil.createActivityRecord(a1);
        ActivityRecord ra2 = ImplUtil.createActivityRecord(a2);

        WorkQueue q = new SimpleWorkQueue("queue");
        q.enqueue(ra1);
        q.enqueue(ra2);
        ActivityRecord tmp1 = q.steal(a, StealStrategy.BIGGEST);
        assertEquals(tmp1, ra2);
        q.enqueue(tmp1);
        ActivityRecord tmp2 = q.steal(a, StealStrategy.BIGGEST);
        assertEquals(tmp2, tmp1);
    }
}
