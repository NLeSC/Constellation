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

import ibis.constellation.StealStrategy;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class UnitActivityContextTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void testNull() {
        new UnitActivityContext(null);
    }
    
    @Test
    public void testName() {
      
        UnitActivityContext c = new UnitActivityContext("tag");
        
        assertEquals("tag", c.getName());
    }
    
    @Test
    public void testNameAndRank() {
      
        UnitActivityContext c = new UnitActivityContext("tag", 42);
        
        assertEquals("tag", c.getName());
        assertEquals(42, c.getRank());
    }

    
    @Test
    public void testSatisfiedBy1() {
      
        UnitActivityContext c = new UnitActivityContext("tag");
        
        ExecutorContext e = new UnitExecutorContext("tag");
        
        StealStrategy s = StealStrategy.ANY;
        
        assertTrue(c.satisfiedBy(e, s));
    }

    @Test
    public void testSatisfiedBy2() {
      
        UnitActivityContext c = new UnitActivityContext("tag");
        
        ExecutorContext e = new UnitExecutorContext("tag");
        
        StealStrategy s = StealStrategy.BIGGEST;
        
        assertTrue(c.satisfiedBy(e, s));
    }

    @Test
    public void testSatisfiedBy3() {
      
        UnitActivityContext c = new UnitActivityContext("tag");
        
        ExecutorContext e = new UnitExecutorContext("tag");
        
        StealStrategy s = StealStrategy.SMALLEST;
        
        assertTrue(c.satisfiedBy(e, s));
    }

    @Test
    public void testSatisfiedBy4() {
      
        UnitActivityContext c = new UnitActivityContext("tag");
        
        ExecutorContext e = new UnitExecutorContext("other");
        
        StealStrategy s = StealStrategy.ANY;
        
        assertFalse(c.satisfiedBy(e, s));
    }

    @Test
    public void testSatisfiedBy5() {
      
        UnitActivityContext c = new UnitActivityContext("tag");
        
        StealStrategy s = StealStrategy.ANY;
        
        assertFalse(c.satisfiedBy(null, s));
    }

    @Test
    public void testSatisfiedBy6() {
      
        UnitActivityContext c = new UnitActivityContext("tag");
        
        UnitExecutorContext [] ecs = new UnitExecutorContext[] { new UnitExecutorContext("other"), new UnitExecutorContext("tag") };
        
        ExecutorContext e = new OrExecutorContext(ecs, true);
        
        StealStrategy s = StealStrategy.ANY;
        
        assertTrue(c.satisfiedBy(e, s));
    }

    @Test
    public void testSatisfiedBy7() {
      
        UnitActivityContext c = new UnitActivityContext("tag");
        
        UnitExecutorContext [] ecs = new UnitExecutorContext[] { new UnitExecutorContext("other"), new UnitExecutorContext("other2") };
        
        ExecutorContext e = new OrExecutorContext(ecs, true);
        
        StealStrategy s = StealStrategy.ANY;
        
        assertFalse(c.satisfiedBy(e, s));
    }
    
    @Test
    public void testSatisfiedBy8() {
      
        UnitActivityContext c = new UnitActivityContext("tag", 42);
        
        ExecutorContext e = new UnitExecutorContext("tag");
        
        StealStrategy s = new StealStrategy(StealStrategy._VALUE, 42);
        
        assertTrue(c.satisfiedBy(e, s));
    }

    @Test
    public void testSatisfiedBy9() {
      
        UnitActivityContext c = new UnitActivityContext("tag", 42);
        
        ExecutorContext e = new UnitExecutorContext("tag");
        
        StealStrategy s = new StealStrategy(StealStrategy._VALUE, 0);
        
        assertFalse(c.satisfiedBy(e, s));
    }
    
    @Test
    public void testSatisfiedBy10() {
      
        UnitActivityContext c = new UnitActivityContext("tag", 42);
        
        ExecutorContext e = new UnitExecutorContext("tag");
        
        StealStrategy s = new StealStrategy(StealStrategy._RANGE, 0, 100);
        
        assertTrue(c.satisfiedBy(e, s));
    }
    
    @Test
    public void testSatisfiedBy11() {
      
        UnitActivityContext c = new UnitActivityContext("tag", 42);
        
        ExecutorContext e = new UnitExecutorContext("tag");
        
        StealStrategy s = new StealStrategy(StealStrategy._RANGE, 42, 100);
        
        assertTrue(c.satisfiedBy(e, s));
    }
    
    @Test
    public void testSatisfiedBy12() {
      
        UnitActivityContext c = new UnitActivityContext("tag", 42);
        
        ExecutorContext e = new UnitExecutorContext("tag");
        
        StealStrategy s = new StealStrategy(StealStrategy._RANGE, 0, 42);
        
        assertTrue(c.satisfiedBy(e, s));
    }
    
    @Test
    public void testSatisfiedBy13() {
      
        UnitActivityContext c = new UnitActivityContext("tag", 42);
        
        ExecutorContext e = new UnitExecutorContext("tag");
        
        StealStrategy s = new StealStrategy(StealStrategy._RANGE, 50, 100);
        
        assertFalse(c.satisfiedBy(e, s));
    }
    
    @Test
    public void testSatisfiedBy14() {
      
        UnitActivityContext c = new UnitActivityContext("tag", 42);
        
        ExecutorContext e = new UnitExecutorContext("tag");
        
        StealStrategy s = new StealStrategy(StealStrategy._RANGE, 0, 30);
        
        assertFalse(c.satisfiedBy(e, s));
    }

    @Test
    public void testToString() {
      
        UnitActivityContext c = new UnitActivityContext("tag", 42);
        assertEquals("UnitActivityContext(tag, 42)", c.toString());
    }

    @Test
    public void testEquals1() {
      
        UnitActivityContext c = new UnitActivityContext("tag", 42);
        assertFalse(c.equals(null));
    }

    @Test
    public void testEquals2() {
      
        UnitActivityContext c = new UnitActivityContext("tag", 42);
        assertFalse(c.equals("Hello World"));
    }
    
    @Test
    public void testEquals3() {
      
        UnitActivityContext c = new UnitActivityContext("tag", 42);
        assertTrue(c.equals(c));
    }

    @Test
    public void testEquals4() {
      
        UnitActivityContext c1 = new UnitActivityContext("tag", 42);
        UnitActivityContext c2 = new UnitActivityContext("tag", 42);

        assertTrue(c1.equals(c2));
    }

    @Test
    public void testEquals5() {
      
        UnitActivityContext c1 = new UnitActivityContext("tag", 42);
        UnitActivityContext c2 = new UnitActivityContext("tag", 43);

        assertFalse(c1.equals(c2));
    }

    @Test
    public void testEquals6() {
      
        UnitActivityContext c1 = new UnitActivityContext("tag", 42);
        UnitActivityContext c2 = new UnitActivityContext("other", 42);

        assertFalse(c1.equals(c2));
    }

    @Test
    public void testHashcode1() {
      
        UnitActivityContext c = new UnitActivityContext("tag");
        
        assertEquals("tag".hashCode(), c.hashCode());
    }

    
    @Test
    public void testHashcode2() {
      
        UnitActivityContext c = new UnitActivityContext("tag", 42);
        
        assertEquals("tag".hashCode(), c.hashCode());
    }

    

    
}
