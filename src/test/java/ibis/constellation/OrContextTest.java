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
public class OrContextTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void testNoParameter() {
        new OrContext();
    }
   
    @Test(expected = IllegalArgumentException.class)
    public void testNull() {
        new OrContext((Context[])null);
    }
   
    @Test(expected = IllegalArgumentException.class)
    public void testEmpty() {
        new OrContext(new Context[2]);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testToFew1() {
        new OrContext(new Context("tag", 0, 0));
    }
   
    @Test(expected = IllegalArgumentException.class)
    public void testToFew2() {
        Context [] tmp = new Context[2];
        tmp[0] = new Context("tag", 0, 0);
        new OrContext(tmp);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testWrongIndex1() {
        OrContext c = new OrContext(new Context("A", 0, 0), new Context("B", 0, 0));
        c.get(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongIndex2() {
        OrContext c = new OrContext(new Context("A", 0, 0), new Context("B", 0, 0));
        c.get(42);
    }

    @Test
    public void testSize() {
        OrContext c = new OrContext(new Context("A", 0, 0), new Context("B", 0, 0));
        assertEquals(c.size(), 2);
    }
    
    @Test
    public void testFirst() {
        OrContext c = new OrContext(new Context("A", 0, 0), new Context("B", 0, 0));
        assertEquals(c.get(0), new Context("A", 0, 0));
    }

    @Test
    public void testSecond() {
        OrContext c = new OrContext(new Context("A", 0, 0), new Context("B", 0, 0));
        assertEquals(c.get(1), new Context("B", 0, 0));
    }
    
    @Test
    public void testToString() {
        OrContext c = new OrContext(new Context("A", 0, 0), new Context("B", 0, 0));
        assertEquals("OrContext(Context(A, 0-0), Context(B, 0-0))", c.toString());
    }
}