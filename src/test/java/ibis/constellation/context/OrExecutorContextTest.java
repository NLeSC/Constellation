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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class OrExecutorContextTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNull() {
        new OrExecutorContext(null, false);
    }
   
    @Test(expected = IllegalArgumentException.class)
    public void testEmpty() {
        UnitExecutorContext [] tmp = new UnitExecutorContext[0];
        new OrExecutorContext(tmp, false);
    }
   
    @Test(expected = IllegalArgumentException.class)
    public void testToSmall() {
        UnitExecutorContext [] tmp = new UnitExecutorContext[] { new UnitExecutorContext("tag") };
        new OrExecutorContext(tmp, false);
    }
  
    @Test(expected = IllegalArgumentException.class)
    public void testToContainsNull() {
        UnitExecutorContext [] tmp = new UnitExecutorContext[] { new UnitExecutorContext("A"), null,  
                                                                 new UnitExecutorContext("B") };
        new OrExecutorContext(tmp, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContainsNull2() {
        new OrExecutorContext(new UnitExecutorContext[2], true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContainsNull3() {
        UnitExecutorContext [] tmp = new UnitExecutorContext[] { new UnitExecutorContext("A"), null,  
                                                                 new UnitExecutorContext("B") };
        new OrExecutorContext(tmp, false);
    }
    
    @Test
    public void testToOK() {
        UnitExecutorContext [] tmp = new UnitExecutorContext[] { new UnitExecutorContext("A"), new UnitExecutorContext("B") };
        OrExecutorContext c = new OrExecutorContext(tmp, true);
        assertArrayEquals(tmp, c.getContexts());
    }
    
    @Test
    public void testGet1() {
        UnitExecutorContext a = new UnitExecutorContext("A");
        UnitExecutorContext b = new UnitExecutorContext("B");
        
        UnitExecutorContext [] tmp = new UnitExecutorContext[] { a, b };
        OrExecutorContext c = new OrExecutorContext(tmp, true);
        
        assertEquals(a, c.get(0));
    }

    @Test
    public void testGet2() {
        UnitExecutorContext a = new UnitExecutorContext("A");
        UnitExecutorContext b = new UnitExecutorContext("B");
        
        UnitExecutorContext [] tmp = new UnitExecutorContext[] { a, b };
        OrExecutorContext c = new OrExecutorContext(tmp, true);
        
        assertEquals(b, c.get(1));
    }
    
    @Test
    public void testGet3() {
        UnitExecutorContext a = new UnitExecutorContext("A");
        UnitExecutorContext b = new UnitExecutorContext("B");
        
        UnitExecutorContext [] tmp = new UnitExecutorContext[] { a, b };
        OrExecutorContext c = new OrExecutorContext(tmp, true);
        
        assertEquals(null, c.get(-1));
    }

    @Test
    public void testGet4() {
        UnitExecutorContext a = new UnitExecutorContext("A");
        UnitExecutorContext b = new UnitExecutorContext("B");
        
        UnitExecutorContext [] tmp = new UnitExecutorContext[] { a, b };
        OrExecutorContext c = new OrExecutorContext(tmp, true);
        
        assertEquals(null, c.get(3));
    }

    @Test
    public void testOrdered1() {
        UnitExecutorContext a = new UnitExecutorContext("A");
        UnitExecutorContext b = new UnitExecutorContext("B");
        
        UnitExecutorContext [] tmp = new UnitExecutorContext[] { a, b };
        OrExecutorContext c = new OrExecutorContext(tmp, true);
        
        assertTrue(c.isOrdered());
    }
    
    @Test
    public void testOrdered2() {
        UnitExecutorContext a = new UnitExecutorContext("A");
        UnitExecutorContext b = new UnitExecutorContext("B");
        
        UnitExecutorContext [] tmp = new UnitExecutorContext[] { a, b };
        OrExecutorContext c = new OrExecutorContext(tmp, false);
        
        assertFalse(c.isOrdered());
    }
   
    @Test
    public void testToString() {
        UnitExecutorContext a = new UnitExecutorContext("A");
        UnitExecutorContext b = new UnitExecutorContext("B");
        
        UnitExecutorContext [] tmp = new UnitExecutorContext[] { a, b };
        OrExecutorContext c = new OrExecutorContext(tmp, false);
        
        assertEquals("OrExecutorContext(UnitExecutorContext(A) or UnitExecutorContext(B))" , c.toString());
    }
    
}
