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

package ibis.constellation.context;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class UnitExecutorContextTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void testNull() {
        new UnitExecutorContext(null);
    }
    
    @Test
    public void testGetName() {
        UnitExecutorContext c = new UnitExecutorContext("A");
        assertEquals("A", c.getName());
    }
   
    @Test
    public void testToString() {
        UnitExecutorContext c = new UnitExecutorContext("A");
        assertEquals("UnitExecutorContext(A)", c.toString());
    }
    
    @Test
    public void testHashCode() {
        UnitExecutorContext c = new UnitExecutorContext("A");
        assertEquals("A".hashCode(), c.hashCode());
    }
    
    @Test
    public void testEqualsNull() {
        UnitExecutorContext c = new UnitExecutorContext("A");
        assertFalse(c.equals(null));
    }

    @Test
    public void testEqualsWrongType() {
        UnitExecutorContext c = new UnitExecutorContext("A");
        assertFalse(c.equals("Hello World"));
    }

    @Test
    public void testEqualsSelf() {
        UnitExecutorContext c = new UnitExecutorContext("A");
        assertTrue(c.equals(c));
    }
    
    @Test
    public void testEquals() {
        UnitExecutorContext c1 = new UnitExecutorContext("A");
        UnitExecutorContext c2 = new UnitExecutorContext("A");
        
        assertTrue(c1.equals(c2));
    }
    
    @Test
    public void testNotEquals() {
        UnitExecutorContext c1 = new UnitExecutorContext("A");
        UnitExecutorContext c2 = new UnitExecutorContext("B");
        
        assertFalse(c1.equals(c2));
    }
    
    
}
