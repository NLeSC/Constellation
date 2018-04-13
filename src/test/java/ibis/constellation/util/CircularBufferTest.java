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

import ibis.constellation.impl.util.CircularBuffer;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class CircularBufferTest {

    @Test
    public void testEmpty() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        assertTrue(t.empty());
    }
    
    @Test
    public void testNotEmpty() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        t.insertFirst("Hello World");
        assertFalse(t.empty());
    }
  
    @Test
    public void testCapacity() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        assertEquals(t.capacity(), 10);
    }
    
    @Test
    public void testSize0() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        assertEquals(t.size(), 0);
    }
    
    @Test
    public void testSize1() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        t.insertFirst("Hello World");
        assertEquals(t.size(), 1);
    }
    
    @Test
    public void testSize2() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        t.insertFirst("A");
        t.insertLast("B");
        assertEquals(t.size(), 2);
    }
    
    @Test
    public void testRemoveFirst1() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        t.insertFirst("A");
        t.insertFirst("B");
        String tmp = t.removeFirst();
        assertEquals(tmp, "B");
    }
    
    @Test
    public void testRemoveFirst2() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        t.insertFirst("A");
        t.insertLast("B");
        String tmp = t.removeFirst();
        assertEquals(tmp, "A");
    }
    
    @Test
    public void testRemoveLast1() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        t.insertFirst("A");
        t.insertFirst("B");
        String tmp = t.removeLast();
        assertEquals(tmp, "A");
    }
    
    @Test
    public void testRemoveLast2() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        t.insertFirst("A");
        t.insertLast("B");
        String tmp = t.removeLast();
        assertEquals(tmp, "B");
    }
    
    @Test
    public void testRemoveNull() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        String tmp = t.removeLast();
        assertEquals(tmp, null);
    }
    
    @Test
    public void testRemoveNull2() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        String tmp = t.removeFirst();
        assertEquals(tmp, null);
    }

    @Test
    public void testRemoveString() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        assertFalse(t.remove("Hello"));
    }
   
    @Test
    public void testRemoveString2() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        t.insertFirst("B");
        t.insertFirst("C");
        assertFalse(t.remove("A"));
    }
   
    @Test
    public void testRemoveString3() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        t.insertFirst("A");
        t.insertFirst("B");
        t.insertFirst("C");
        assertTrue(t.remove("A"));
    }
 
    @Test
    public void testToString() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        assertEquals(t.toString(), "CircularBuffer(0)");
    }
    
    @Test
    public void testGetNull() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        t.insertLast("B");
        String tmp = t.get(1);
        assertEquals(tmp, null);
    }
    
    @Test
    public void testGet() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        t.insertFirst("A");
        t.insertFirst("B");
        String tmp = t.get(1);
        assertEquals(tmp, "A");
    }

    @Test
    public void testGet2() {
        CircularBuffer<String> t = new CircularBuffer<>(10);
        t.insertFirst("A");
        t.insertFirst("B");
        String tmp = t.get(-1);
        assertEquals(tmp, null);
    }
    
    @Test
    public void testResize1() {
        CircularBuffer<String> t = new CircularBuffer<>(1);
        t.insertFirst("A");
        t.insertFirst("A");
        assertEquals(t.capacity(), 2);
    }
    
    @Test
    public void testResize2() {
        CircularBuffer<String> t = new CircularBuffer<>(1);
        t.insertFirst("A");
        t.insertFirst("A");
        t.insertFirst("A");
        assertEquals(t.capacity(), 4);
    }
    
    @Test
    public void testResize3() {
        CircularBuffer<String> t = new CircularBuffer<>(1);
        t.insertLast("A");
        t.insertLast("A");
        t.insertLast("A");
        assertEquals(t.capacity(), 4);
    }
    
    @Test
    public void testRemove1() {
        CircularBuffer<String> t = new CircularBuffer<>(1);
        assertFalse(t.remove(0));
    }

    @Test
    public void testRemove2() {
        CircularBuffer<String> t = new CircularBuffer<>(1);
        t.insertFirst("A");
        assertFalse(t.remove(1));
    }
    
    @Test
    public void testRemove3() {
        CircularBuffer<String> t = new CircularBuffer<>(1);
        t.insertFirst("A");
        assertTrue(t.remove(0));
    }

    @Test
    public void testRemove4() {
        CircularBuffer<String> t = new CircularBuffer<>(1);
        t.insertFirst("A");
        t.remove(0);
        assertEquals(t.size(), 0);
    }
    
    @Test
    public void testRemove5() {
        CircularBuffer<String> t = new CircularBuffer<>(1);
        t.insertFirst("A");
        t.remove(1); // should fail
        assertEquals(t.size(), 1);
    }
    
    @Test
    public void testRemove6() {
        CircularBuffer<String> t = new CircularBuffer<>(1);
        t.insertFirst("A");
        t.insertFirst("B");
        t.remove(1);
        assertEquals(t.size(), 1);
    }

    @Test
    public void testRemove7() {
        CircularBuffer<String> t = new CircularBuffer<>(1);
        t.insertFirst("A");
        t.insertFirst("B");
        t.remove(1);
        String tmp = t.get(0);
        assertEquals(tmp, "B");
    }
    
    @Test
    public void testRemove8() {
        CircularBuffer<String> t = new CircularBuffer<>(1);
        t.insertFirst("A");
        t.insertFirst("B");
        t.insertFirst("C");
        t.remove(1);
        String tmp = t.get(0);
        assertEquals(tmp, "C");
    }
    
    @Test
    public void testRemove9() {
        CircularBuffer<String> t = new CircularBuffer<>(1);
        t.insertFirst("A");
        t.insertFirst("B");
        t.insertFirst("C");
        t.remove(1);
        String tmp = t.get(1);
        assertEquals(tmp, "A");
    }
    
    @Test
    public void testRemove10() {
        CircularBuffer<String> t = new CircularBuffer<>(1);
        t.insertFirst("A");
        t.insertFirst("B");
        t.insertFirst("C");
        t.insertFirst("D");
        t.insertFirst("E");
        t.insertFirst("F");
        t.remove(4);
        String tmp = t.get(4);
        assertEquals(tmp, "A");
    }

    @Test
    public void testRemove11() {
        CircularBuffer<String> t = new CircularBuffer<>(5);
        t.insertFirst("A");
        t.insertFirst("B");
        t.insertFirst("C");
        t.insertFirst("D");
        t.insertFirst("E");
        t.insertFirst("F");
        t.removeFirst(); // Removes F and leaves hole at start.
        t.insertLast("X"); // Inserts X into hole as last 
        t.remove(4); // Should remove A
        String tmp = t.get(4);
        assertEquals(tmp, "X");
    }
    
    @Test
    public void testRemove12() {
        CircularBuffer<String> t = new CircularBuffer<>(5);
        t.insertFirst("A");
        t.insertFirst("B");
        t.insertFirst("C");
        t.insertFirst("D");
        t.insertFirst("E");
        t.insertFirst("F");
        t.removeFirst(); // Removes F and leaves hole at start.
        t.remove(1);     // Should remove D
        String tmp = t.get(1);
        assertEquals(tmp, "C");
    }
}

