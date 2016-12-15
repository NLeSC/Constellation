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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import ibis.constellation.StealStrategy;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class OrActivityContextTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void testNull() {
        new OrActivityContext(null, false);
    }
   
    @Test(expected = IllegalArgumentException.class)
    public void testEmpty() {
        UnitActivityContext [] tmp = new UnitActivityContext[0];
        new OrActivityContext(tmp, false);
    }
   
    @Test(expected = IllegalArgumentException.class)
    public void testToSmall() {
        UnitActivityContext [] tmp = new UnitActivityContext[] { new UnitActivityContext("tag") };
        new OrActivityContext(tmp, false);
    }
  
    @Test(expected = IllegalArgumentException.class)
    public void testToContainsNull() {
        UnitActivityContext [] tmp = new UnitActivityContext[] { new UnitActivityContext("A"), null,  
                                                                 new UnitActivityContext("B") };
        OrActivityContext c = new OrActivityContext(tmp, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContainsNull2() {
        new OrActivityContext(new UnitActivityContext[2], true);
    }

    
    @Test(expected = IllegalArgumentException.class)
    public void testToContainsNull3() {
        UnitActivityContext [] tmp = new UnitActivityContext[] { new UnitActivityContext("A"), null,  
                                                                 new UnitActivityContext("B") };
        OrActivityContext c = new OrActivityContext(tmp, false);
    }
    
    @Test
    public void testToOK() {
        UnitActivityContext [] tmp = new UnitActivityContext[] { new UnitActivityContext("A"), new UnitActivityContext("B") };
        OrActivityContext c = new OrActivityContext(tmp, true);
        assertArrayEquals(tmp, c.getContexts());
    }
     
    @Test
    public void testOrder() {
        UnitActivityContext [] tmp = new UnitActivityContext[] { new UnitActivityContext("A", 2), new UnitActivityContext("A", 1) }; 
        OrActivityContext c = new OrActivityContext(tmp, false);
        
        UnitActivityContext [] res = new UnitActivityContext[] { new UnitActivityContext("A", 1), new UnitActivityContext("A", 2) };      
        assertArrayEquals(res, c.getContexts());
    }

    @Test
    public void testOrder2() {
        UnitActivityContext [] tmp = new UnitActivityContext[] { new UnitActivityContext("A", 2), new UnitActivityContext("B", 1) }; 
        OrActivityContext c = new OrActivityContext(tmp, false);
        
        UnitActivityContext [] res = new UnitActivityContext[] { new UnitActivityContext("A", 2), new UnitActivityContext("B", 1) };
        assertArrayEquals(res, c.getContexts());      
    }

    @Test
    public void testOrder3() {
        UnitActivityContext [] tmp = new UnitActivityContext[] { new UnitActivityContext("A", 1), new UnitActivityContext("A", 1) }; 
        OrActivityContext c = new OrActivityContext(tmp, false);
        assertArrayEquals(tmp, c.getContexts());
    }

    @Test
    public void testOrder4() {
        UnitActivityContext [] tmp = new UnitActivityContext[] { new UnitActivityContext("B", 1), new UnitActivityContext("A", 2) }; 
        OrActivityContext c = new OrActivityContext(tmp, false);
        
        UnitActivityContext [] res = new UnitActivityContext[] { new UnitActivityContext("A", 2), new UnitActivityContext("B", 1) };
        assertArrayEquals(res, c.getContexts());      
    }

    @Test
    public void testOK() {
        UnitActivityContext [] tmp = new UnitActivityContext[] { new UnitActivityContext("A", 2), new UnitActivityContext("B", 1) }; 
        OrActivityContext c = new OrActivityContext(tmp);
        assertArrayEquals(tmp, c.getContexts());
    }

    @Test
    public void testSize() {
        UnitActivityContext [] tmp = new UnitActivityContext[] { new UnitActivityContext("A", 2), new UnitActivityContext("B", 1) }; 
        OrActivityContext c = new OrActivityContext(tmp);
        assertEquals(2, c.size());
    }
    
    @Test
    public void testGet1() {
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
        
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
        
        assertEquals(a, c.get(0));
    }

    @Test
    public void testGet2() {
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
        
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
        
        assertEquals(b, c.get(1));
    }
    
    @Test
    public void testGet3() {
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
        
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
        
        assertEquals(null, c.get(-1));
    }
    
    @Test
    public void testGet4() {
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
        
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
        
        assertEquals(null, c.get(3));
    }

    @Test
    public void testContains1() {
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
        
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
        
        assertTrue(c.contains(a));
    }
    
    @Test
    public void testContains2() {
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
        
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
        
        assertTrue(c.contains(b));
    }

    @Test
    public void testContains3() {
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
        UnitActivityContext d = new UnitActivityContext("D");
        
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
        
        assertFalse(c.contains(d));
    }
    
    @Test
    public void testSatisfiedBy1() {
      
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
        
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
        
        ExecutorContext e = new UnitExecutorContext("A");
        
        StealStrategy s = StealStrategy.ANY;
        
        assertTrue(c.satisfiedBy(e, s));
    }

    @Test
    public void testSatisfiedBy2() {
      
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
        
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
        
        ExecutorContext e = new UnitExecutorContext("D");
        
        StealStrategy s = StealStrategy.ANY;
        
        assertFalse(c.satisfiedBy(e, s));
    }

    @Test
    public void testSatisfiedBy3() {
      
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
        
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
        
        StealStrategy s = StealStrategy.ANY;
        
        assertFalse(c.satisfiedBy(null, s));
    }
    
    @Test
    public void testSatisfiedBy4() {
      
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
         
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
  
        UnitExecutorContext e1 = new UnitExecutorContext("B");
        UnitExecutorContext e2 = new UnitExecutorContext("C");
        
        UnitExecutorContext [] tmp2 = new UnitExecutorContext [] { e1, e2 }; 
        
        ExecutorContext ex = new OrExecutorContext(tmp2, true); 
        
        StealStrategy s = StealStrategy.ANY;
        
        assertTrue(c.satisfiedBy(ex, s));
    }
  
    @Test
    public void testSatisfiedBy5() {
      
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
         
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
  
        UnitExecutorContext e1 = new UnitExecutorContext("C");
        UnitExecutorContext e2 = new UnitExecutorContext("D");
        
        UnitExecutorContext [] tmp2 = new UnitExecutorContext [] { e1, e2 }; 
        
        ExecutorContext ex = new OrExecutorContext(tmp2, true); 
        
        StealStrategy s = StealStrategy.ANY;
        
        assertFalse(c.satisfiedBy(ex, s));
    }
    
    
    @Test
    public void testToString() {
      
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
         
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
        
        assertEquals("OrContext(UnitActivityContext(A, 0) or UnitActivityContext(B, 0))", c.toString());
    }

    @Test
      
    public void testHashcode1() {
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
         
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
        
        int hashCode = 1;

        for (UnitActivityContext element : tmp) {
            hashCode = 31 * hashCode + (element == null ? 0 : element.hashCode());
        }
        
        assertEquals(hashCode, c.hashCode());
    }

    @Test
    public void testEquals1() {
      
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
         
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
        
        assertFalse(c.equals(null));
    }

    @Test
    public void testEquals2() {
      
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
         
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
        
        assertFalse(c.equals("Hello World"));
    }
    
    @Test
    public void testEquals3() {
      
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
         
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
        
        assertTrue(c.equals(c));
    }
    
    @Test
    public void testEquals4() {
      
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
        
        UnitActivityContext a1 = new UnitActivityContext("A");
        UnitActivityContext b1 = new UnitActivityContext("B");
        UnitActivityContext [] tmp1 = new UnitActivityContext[] { a1, b1 };
        OrActivityContext c1 = new OrActivityContext(tmp1, true);
     
        assertTrue(c.equals(c1));
    }

    @Test
    public void testEquals5() {
      
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
        
        UnitActivityContext a1 = new UnitActivityContext("D");
        UnitActivityContext b1 = new UnitActivityContext("E");
        UnitActivityContext [] tmp1 = new UnitActivityContext[] { a1, b1 };
        OrActivityContext c1 = new OrActivityContext(tmp1, true);
     
        assertFalse(c.equals(c1));
    }
    
    @Test
    public void testEquals6() {
      
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, true);
        
        UnitActivityContext a1 = new UnitActivityContext("B");
        UnitActivityContext b1 = new UnitActivityContext("A");
        UnitActivityContext [] tmp1 = new UnitActivityContext[] { a1, b1 };
        OrActivityContext c1 = new OrActivityContext(tmp1, true);
     
        assertFalse(c.equals(c1));
    }

    @Test
    public void testEquals7() {
      
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext c = new OrActivityContext(tmp, false);
        
        UnitActivityContext a1 = new UnitActivityContext("B");
        UnitActivityContext b1 = new UnitActivityContext("A");
        UnitActivityContext [] tmp1 = new UnitActivityContext[] { a1, b1 };
        OrActivityContext c1 = new OrActivityContext(tmp1, false);
     
        assertTrue(c.equals(c1));
    }
    
    @Test
    public void testEquals8() {
      
        UnitActivityContext a = new UnitActivityContext("A");
        UnitActivityContext b = new UnitActivityContext("B");
        UnitActivityContext [] tmp = new UnitActivityContext[] { a, b };
        OrActivityContext cx1 = new OrActivityContext(tmp, false);
        
        UnitActivityContext a1 = new UnitActivityContext("A");
        UnitActivityContext b1 = new UnitActivityContext("B");
        UnitActivityContext c1 = new UnitActivityContext("C");
        
        UnitActivityContext [] tmp1 = new UnitActivityContext[] { a1, b1, c1 };
        OrActivityContext cx2 = new OrActivityContext(tmp1, false);
     
        assertFalse(cx1.equals(cx2));
    }
    
    
    
}

