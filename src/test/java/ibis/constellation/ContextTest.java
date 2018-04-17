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
package ibis.constellation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class ContextTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNull() {
        new Context(null, 42, 45);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRange() {
        new Context("test", 1, 0);
    }

    @Test
    public void testName1() {
        Context c = new Context("tag", 42, 45);
        assertEquals("tag", c.getName());
    }

    @Test
    public void testName2() {
        Context c = new Context("tag", 42);
        assertEquals("tag", c.getName());
    }

    @Test
    public void testName3() {
        Context c = new Context("tag", 42, 45);
        assertEquals("tag", c.getName());
    }

    @Test
    public void testStart1() {
        Context c = new Context("tag", 42, 45);
        assertEquals(42, c.getRangeStart());
    }

    @Test
    public void testStart2() {
        Context c = new Context("tag", 42);
        assertEquals(42, c.getRangeStart());
    }

    @Test
    public void testEnd1() {
        Context c = new Context("tag", 42, 45);
        assertEquals(45, c.getRangeEnd());
    }

    @Test
    public void testEnd2() {
        Context c = new Context("tag", 42);
        assertEquals(42, c.getRangeEnd());
    }

    @Test
    public void testDefaultRange1() {
        Context c = new Context("tag");
        assertEquals(Long.MIN_VALUE, c.getRangeStart());
    }

    @Test
    public void testDefaultRange2() {
        Context c = new Context("tag");
        assertEquals(Long.MAX_VALUE, c.getRangeEnd());
    }

    @Test
    public void testToString() {
        Context c = new Context("tag", 42, 45);
        assertEquals("Context(tag, 42-45)", c.toString());
    }

    @Test
    public void testEquals1() {
        Context c = new Context("tag");
        Context n = null;
        assertFalse(c.equals(n));
    }

    @Test
    public void testEquals2() {
        Context c = new Context("tag");
        assertFalse(c.equals("Hello world"));
    }

    @Test
    public void testEquals3() {
        Context c1 = new Context("tag1");
        Context c2 = new Context("tag2");

        assertFalse(c1.equals(c2));
    }

    @Test
    public void testEquals4() {
        Context c1 = new Context("tag1");
        Context c2 = new Context("tag1");

        assertTrue(c1.equals(c2));
    }

    @Test
    public void testEquals5() {
        Context c1 = new Context("tag1", 0, 10);
        Context c2 = new Context("tag1", 0, 10);

        assertTrue(c1.equals(c2));
    }

    @Test
    public void testEquals6() {
        Context c1 = new Context("tag1", 0, 10);
        Context c2 = new Context("tag1", 1, 10);

        assertFalse(c1.equals(c2));
    }

    @Test
    public void testEquals7() {
        Context c1 = new Context("tag1", 0, 10);
        Context c2 = new Context("tag1", 0, 11);

        assertFalse(c1.equals(c2));
    }

    @Test
    public void testHashcode() {

        String tag = "tag";
        long start = 1;
        long end = 10;

        Context c = new Context(tag, start, end);

        int hashcode = tag.hashCode() ^ (int) ((end ^ (end >>> 32)) ^ (start ^ (start >>> 32)));

        assertEquals(c.hashCode(), hashcode);
    }

}