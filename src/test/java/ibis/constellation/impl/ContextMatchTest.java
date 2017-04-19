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

package ibis.constellation.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ibis.constellation.OrContext;
import ibis.constellation.Context;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class ContextMatchTest {
    
    @Test
    public void constructor() {
        // For coverage
        new ContextMatch();
    }
    
    @Test
    public void noNullNull() {
        assertFalse(ContextMatch.match(null, null));
    }
    
    @Test
    public void noMatchRangeContextNull1() {
        
        Context a = new Context("A", 1, 1);
        assertFalse(ContextMatch.match(a, null));
    }
    
    @Test
    public void noMatchRangeContextNull2() {
        
        Context a = new Context("A", 1, 1);
        assertFalse(ContextMatch.match(null, a));
    }
    
    
    @Test
    public void matchRangeContext1() {
        
        Context a = new Context("A", 1, 1);
        Context b = new Context("A", 1, 1);
        assertTrue(ContextMatch.match(a, b));
    }
    
    @Test
    public void matchRangeContext2() {
        
        Context a = new Context("A", 1, 10);
        Context b = new Context("A", 8, 20);        
        assertTrue(ContextMatch.match(a, b));
    }
    
    @Test
    public void matchRangeContext3() {
        
        Context a = new Context("A", 8, 20);
        Context b = new Context("A", 1, 10);
        assertTrue(ContextMatch.match(a, b));
    }
   
    @Test
    public void noMatchRangeContext1() {
        
        Context a = new Context("A", 1, 1);
        Context b = new Context("A", 2, 2);
        assertFalse(ContextMatch.match(a, b));
    }

    @Test
    public void noMatchRangeContext2() {
        
        Context a = new Context("A", 2, 2);
        Context b = new Context("A", 1, 1);
        assertFalse(ContextMatch.match(a, b));
    }

    @Test
    public void noMatchRangeContext3() {
        
        Context a = new Context("A", 1, 1);
        Context b = new Context("B", 1, 1);
        assertFalse(ContextMatch.match(a, b));
    }

    @Test
    public void noMatchOrContextNull1() {
        
        OrContext a = new OrContext(new Context("A", 1, 1), new Context("B", 1, 1));
        assertFalse(ContextMatch.match(a, null));
    }
    
    @Test
    public void noMatchOrContextNull2() {
        
        OrContext a = new OrContext(new Context("A", 1, 1), new Context("B", 1, 1));
        assertFalse(ContextMatch.match(null, a));
    }
        
    @Test
    public void noMatchOrContextRangeContext1() {
        
        OrContext a = new OrContext(new Context("A", 1, 1), new Context("B", 1, 1));
        Context c = new Context("C", 1, 1);
        assertFalse(ContextMatch.match(a, c));
    }
    
    @Test
    public void noMatchOrContextRangeContext2() {
        
        Context c = new Context("C", 1, 1);
        OrContext a = new OrContext(new Context("A", 1, 1), new Context("B", 1, 1));
        assertFalse(ContextMatch.match(c, a));
    }
    
    @Test
    public void matchOrContextRangeContext1() {
        
        Context c = new Context("A", 1, 1);
        OrContext a = new OrContext(new Context("A", 1, 1), new Context("B", 1, 1));
        assertTrue(ContextMatch.match(c, a));
    }
    
    @Test
    public void matchOrContextRangeContext2() {
        
        OrContext a = new OrContext(new Context("A", 1, 1), new Context("B", 1, 1));
        Context c = new Context("A", 1, 1);
        assertTrue(ContextMatch.match(c, a));
    }
    
    @Test
    public void matchOrContextRangeContext3() {
        
        Context c = new Context("B", 1, 1);
        OrContext a = new OrContext(new Context("A", 1, 1), new Context("B", 1, 1));
        assertTrue(ContextMatch.match(c, a));
    }
    
    @Test
    public void matchOrContextRangeContext4() {
        
        OrContext a = new OrContext(new Context("A", 1, 1), new Context("B", 1, 1));
        Context c = new Context("B", 1, 1);
        assertTrue(ContextMatch.match(c, a));
    }
    
    
    @Test
    public void noMatchOrContextOrContext1() {
        
        OrContext a = new OrContext(new Context("A", 1, 1), new Context("B", 1, 1));
        OrContext b = new OrContext(new Context("C", 1, 1), new Context("D", 1, 1));
        assertFalse(ContextMatch.match(a, b));
    }
    
    @Test
    public void noMatchOrContextOrContext2() {
        
        OrContext a = new OrContext(new Context("A", 1, 1), new Context("B", 1, 1));
        OrContext b = new OrContext(new Context("A", 2, 2), new Context("B", 2, 2));
        assertFalse(ContextMatch.match(a, b));
    }
    
    @Test
    public void matchOrContextOrContext1() {
        OrContext a = new OrContext(new Context("A", 1, 1), new Context("B", 1, 1));
        OrContext b = new OrContext(new Context("C", 1, 1), new Context("B", 1, 1));
        assertTrue(ContextMatch.match(a, b));
    }
    
    @Test
    public void matchOrContextOrContext2() {
        OrContext a = new OrContext(new Context("A", 1, 1), new Context("A", 3, 3));
        OrContext b = new OrContext(new Context("A", 2, 3), new Context("B", 1, 1));
        assertTrue(ContextMatch.match(a, b));
    }
    
    
}

