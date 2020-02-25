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
package ibis.constellation;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class StealPoolTest {

    @Test(expected = IllegalArgumentException.class)
    public void createStealPoolFail() {
        // Should throw a nullpointerexpection
        String s = null;
        new StealPool(s);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mergeStealPoolEmptyArray() {
        StealPool.merge(new StealPool[1]);
    }

    @Test
    public void mergeStealPoolSingle() {
        StealPool a = new StealPool("A");
        StealPool res = StealPool.merge(a);

        assertEquals(a, res);
    }

    @Test
    public void mergeStealPoolNull() {
        StealPool res = StealPool.merge();
        assertEquals(StealPool.NONE, res);
    }

    @Test
    public void mergeStealPoolNull2() {
        StealPool res = StealPool.merge((StealPool[]) null);
        assertEquals(StealPool.NONE, res);
    }

    @Test
    public void mergeStealPoolZeroLengthArray() {
        StealPool res = StealPool.merge(new StealPool[0]);
        assertEquals(StealPool.NONE, res);
    }

    @Test
    public void mergeStealPoolDoubleNone() {
        StealPool res = StealPool.merge(StealPool.NONE, StealPool.NONE);
        assertEquals(StealPool.NONE, res);
    }

    @Test
    public void mergeStealPoolDoubleWorld() {
        StealPool res = StealPool.merge(StealPool.WORLD, StealPool.WORLD);
        assertEquals(StealPool.WORLD, res);
    }

    @Test
    public void mergeStealPoolWorldNone() {
        StealPool res = StealPool.merge(StealPool.WORLD, StealPool.NONE);
        assertEquals(StealPool.WORLD, res);
    }

    @Test
    public void mergeStealPoolWorldTag() {
        StealPool res = StealPool.merge(StealPool.WORLD, new StealPool("A"));
        assertEquals(StealPool.WORLD, res);
    }

    @Test
    public void mergeStealPoolWorldTag2() {
        StealPool res = StealPool.merge(new StealPool("A"), new StealPool("B"), StealPool.WORLD);
        assertEquals(StealPool.WORLD, res);
    }

    @Test
    public void mergeStealPoolDoubleTag() {
        StealPool a1 = new StealPool("A");
        StealPool a2 = new StealPool("A");

        StealPool res = StealPool.merge(a1, a2);

        assertEquals(a1, res);
    }

    @Test
    public void mergeStealPool() {
        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");

        StealPool res = StealPool.merge(a, b);

        StealPool[] tmp = new StealPool[] { a, b };

        assertArrayEquals(tmp, res.set());
    }

    @Test
    public void mergeStealPoolHierachy() {
        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");

        StealPool s1 = StealPool.merge(a, b);

        StealPool c = new StealPool("C");

        StealPool s2 = StealPool.merge(s1, c);

        StealPool[] tmp = new StealPool[] { a, b, c };

        assertArrayEquals(tmp, s2.set());
    }

    @Test
    public void mergeStealPoolHierachyWithWorld() {
        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");

        StealPool s1 = StealPool.merge(a, b);

        StealPool c = new StealPool("C");

        StealPool s2 = StealPool.merge(s1, c);

        StealPool s3 = StealPool.merge(s2, StealPool.WORLD);

        assertEquals(StealPool.WORLD, s3);
    }

    @Test
    public void mergeStealPoolWorld() {
        StealPool a1 = new StealPool("A");

        StealPool res = StealPool.merge(a1, StealPool.WORLD);

        assertEquals(StealPool.WORLD, res);
    }

    @Test
    public void isWorld() {
        StealPool s = StealPool.WORLD;
        assertTrue(s.isWorld());
    }

    @Test
    public void isAlsoWorld() {
        StealPool s = new StealPool("WORLD");
        assertTrue(s.isWorld());
    }

    @Test
    public void isNotWorld() {
        StealPool s = new StealPool("tag");
        assertFalse(s.isWorld());
    }

    @Test
    public void isNone() {
        StealPool s = StealPool.NONE;
        assertTrue(s.isNone());
    }

    @Test
    public void isAlsoNone() {
        StealPool s = new StealPool("NONE");
        assertTrue(s.isNone());
    }

    @Test
    public void isNotNone() {
        StealPool s = new StealPool("tag");
        assertFalse(s.isNone());
    }

    @Test
    public void requestStealPoolSet() {
        StealPool p = new StealPool("tag");

        StealPool[] res = new StealPool[] { p };

        assertArrayEquals(p.set(), res);
    }

    @Test
    public void createStealPool() {
        StealPool p = new StealPool("tag");
        assertEquals(p.getTag(), "tag");
    }

    @Test
    public void testToString() {
        StealPool p = new StealPool("tag");
        assertEquals("tag", p.toString());
    }

    @Test
    public void testToStringSet() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");

        StealPool res = StealPool.merge(a, b);

        assertEquals("[A, B]", res.toString());
    }

    @Test
    public void testEquals1() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");

        assertFalse(a.equals(b));
    }

    @Test
    public void testEquals2() {

        StealPool a = new StealPool("A");

        assertTrue(a.equals(a));
    }

    @Test
    public void testEquals3() {

        StealPool a = new StealPool("A");
        StealPool b = null;

        assertFalse(a.equals(b));
    }

    @Test
    public void testEquals4() {

        StealPool a = new StealPool("A");

        assertFalse(a.equals("Hello World"));
    }

    @Test
    public void testEquals5() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");

        StealPool s1 = StealPool.merge(a, b);

        StealPool c = new StealPool("C");
        StealPool d = new StealPool("D");

        StealPool s2 = StealPool.merge(c, d);

        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEquals6() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");

        StealPool s1 = StealPool.merge(a, b);

        assertFalse(s1.equals(a));
    }

    @Test
    public void testEquals7() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");

        StealPool s1 = StealPool.merge(a, b);

        assertFalse(a.equals(s1));
    }

    @Test
    public void testEquals8() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");

        StealPool s1 = StealPool.merge(a, b);

        StealPool c = new StealPool("C");

        StealPool s2 = StealPool.merge(a, b, c);

        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEquals9() {

        StealPool a1 = new StealPool("A");
        StealPool b1 = new StealPool("B");

        StealPool s1 = StealPool.merge(a1, b1);

        StealPool a2 = new StealPool("A");
        StealPool b2 = new StealPool("B");

        StealPool s2 = StealPool.merge(a2, b2);

        assertTrue(s1.equals(s2));
    }

    @Test
    public void testEquals10() {

        StealPool a1 = new StealPool("A");
        StealPool b1 = new StealPool("B");

        StealPool s1 = StealPool.merge(a1, b1);

        StealPool a2 = new StealPool("B");
        StealPool b2 = new StealPool("A");

        StealPool s2 = StealPool.merge(a2, b2);

        assertTrue(s1.equals(s2));
    }

    @Test
    public void testOverlap1() {

        StealPool a1 = new StealPool("A");
        StealPool b1 = new StealPool("B");

        StealPool s1 = StealPool.merge(a1, b1);

        StealPool a2 = new StealPool("B");
        StealPool b2 = new StealPool("A");

        StealPool s2 = StealPool.merge(a2, b2);

        assertTrue(s1.overlap(s2));
        assertTrue(s2.overlap(s1));
    }

    @Test
    public void testOverlap2() {
        StealPool s = new StealPool("NONE");
        assertFalse(s.overlap(StealPool.NONE));
    }

    @Test
    public void testOverlap3() {
        StealPool s = new StealPool("tag");
        assertTrue(s.overlap(s));
    }

    @Test
    public void testOverlap4() {
        StealPool w = new StealPool("WORLD");
        StealPool a = new StealPool("tag");
        assertTrue(a.overlap(w));
    }

    @Test
    public void testOverlap5() {
        StealPool w = new StealPool("WORLD");
        StealPool a = new StealPool("tag");
        assertTrue(w.overlap(a));
    }

    @Test
    public void testOverlap6() {
        StealPool a = new StealPool("tag");
        StealPool s = new StealPool("NONE");
        assertFalse(s.overlap(a));
    }

    @Test
    public void testOverlap7() {
        StealPool a = new StealPool("tag");
        StealPool s = new StealPool("NONE");
        assertFalse(a.overlap(s));
    }

    @Test
    public void testOverlap8() {

        StealPool a1 = new StealPool("A");
        StealPool b1 = new StealPool("B");

        StealPool s1 = StealPool.merge(a1, b1);

        StealPool a2 = new StealPool("A");

        assertTrue(s1.overlap(a2));
    }

    @Test
    public void testOverlap9() {

        StealPool a1 = new StealPool("A");
        StealPool b1 = new StealPool("B");

        StealPool s1 = StealPool.merge(a1, b1);

        StealPool a2 = new StealPool("A");

        assertTrue(a2.overlap(s1));
    }

    @Test
    public void testOverlap10() {

        StealPool a1 = new StealPool("A");
        StealPool b1 = new StealPool("B");

        StealPool s1 = StealPool.merge(a1, b1);

        StealPool a2 = new StealPool("C");
        StealPool b2 = new StealPool("D");

        StealPool s2 = StealPool.merge(a2, b2);

        assertFalse(s1.overlap(s2));
    }

    @Test
    public void testOverlap11() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");

        assertFalse(a.overlap(b));
    }

    @Test
    public void testOverlap12() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");

        assertFalse(b.overlap(a));
    }

    @Test
    public void testOverlap13() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");
        StealPool c = new StealPool("C");

        StealPool s1 = StealPool.merge(a, b, c);

        StealPool d = new StealPool("D");
        StealPool e = new StealPool("E");

        StealPool s2 = StealPool.merge(d, e);

        assertFalse(s1.overlap(s2));
    }

    @Test
    public void testOverlap14() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");

        StealPool s1 = StealPool.merge(a, b);

        StealPool c = new StealPool("C");
        StealPool d = new StealPool("D");
        StealPool e = new StealPool("E");

        StealPool s2 = StealPool.merge(c, d, e);

        assertFalse(s1.overlap(s2));
    }

    @Test
    public void testOverlap14b() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");

        StealPool s1 = StealPool.merge(a, b);

        StealPool c = new StealPool("C");
        StealPool d = new StealPool("D");
        StealPool e = new StealPool("E");

        StealPool s2 = StealPool.merge(c, d, e);

        assertFalse(s2.overlap(s1));
    }

    @Test
    public void testOverlap15() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");
        StealPool c = new StealPool("C");
        StealPool d = new StealPool("D");
        StealPool e = new StealPool("E");

        StealPool s1 = StealPool.merge(a, b, c, d, e);
        StealPool s2 = StealPool.merge(c, d);

        assertTrue(s1.overlap(s2));
    }

    @Test
    public void testOverlap16() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");
        StealPool c = new StealPool("C");
        StealPool d = new StealPool("D");
        StealPool e = new StealPool("E");

        StealPool s1 = StealPool.merge(a, b, c, d, e);
        StealPool s2 = StealPool.merge(c, d);

        assertTrue(s2.overlap(s1));
    }

    @Test
    public void testOverlap17() {
        StealPool s = new StealPool("NONE");
        assertFalse(StealPool.NONE.overlap(s));
    }

    @Test
    public void testOverlap18() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");
        StealPool c = new StealPool("C");
        StealPool d = new StealPool("D");
        StealPool e = new StealPool("E");

        StealPool s1 = StealPool.merge(a, b, c, d);

        assertFalse(s1.overlap(e));
    }

    @Test
    public void testOverlap19() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");
        StealPool c = new StealPool("C");

        StealPool s1 = StealPool.merge(a, b, c);

        assertTrue(s1.overlap(c));
    }

    @Test
    public void testOverlap20() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");
        StealPool c = new StealPool("C");
        StealPool d = new StealPool("D");
        StealPool e = new StealPool("E");

        StealPool s1 = StealPool.merge(a, b, c, d);

        assertFalse(e.overlap(s1));
    }

    @Test
    public void testOverlap21() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");
        StealPool c = new StealPool("C");

        StealPool s1 = StealPool.merge(a, b, c);

        assertTrue(c.overlap(s1));
    }

    @Test
    public void testOverlap22() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");
        StealPool c = new StealPool("C");

        StealPool s = StealPool.merge(b, c);

        assertFalse(s.overlap(a));
    }

    @Test
    public void testOverlap23() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");
        StealPool c = new StealPool("C");

        StealPool s = StealPool.merge(b, c);

        assertFalse(a.overlap(s));
    }

    @Test
    public void testOverlap24() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");
        StealPool c = new StealPool("C");

        StealPool s1 = StealPool.merge(a, b, c);

        StealPool d = new StealPool("D");
        StealPool e = new StealPool("E");

        StealPool s2 = StealPool.merge(d, e);

        assertFalse(s1.overlap(s2));
    }

    @Test
    public void testHashcode1() {

        StealPool a = new StealPool("A");

        assertEquals("A".hashCode(), a.hashCode());
    }

    @Test
    public void testHashcode2() {

        StealPool a = new StealPool("A");
        StealPool b = new StealPool("B");

        StealPool s = StealPool.merge(a, b);

        StealPool[] tmp = new StealPool[] { a, b };

        assertEquals(Arrays.hashCode(tmp), s.hashCode());
    }

    @Test
    public void testSelect1() {

        StealPool a = new StealPool("A");

        Random r = new Random();

        assertEquals(a, a.randomlySelectPool(r));
    }

}
