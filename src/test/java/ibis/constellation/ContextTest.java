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

package ibis.constellation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ibis.constellation.Context;

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
}