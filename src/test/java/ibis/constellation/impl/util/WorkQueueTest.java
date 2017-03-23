package ibis.constellation.impl.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        assertTrue(tmp1 == ra || tmp1 == rab);
        ActivityRecord tmp2 = q.steal(b, StealStrategy.SMALLEST);
        assertTrue((tmp2 == rb || tmp2 == rab) && tmp2 != tmp1);
        ActivityRecord tmp = q.steal(rab.getContext(), StealStrategy.SMALLEST);
        assertTrue((tmp == rb || tmp == rab || tmp == ra) && tmp != tmp1 && tmp != tmp2);
        tmp = q.steal(rab.getContext(), StealStrategy.SMALLEST);
        assertNull(tmp);
    }
}
