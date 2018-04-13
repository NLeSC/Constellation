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

import org.junit.Test;

import ibis.constellation.impl.ImplUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class ActivityTest {

    static class TestID implements ActivityIdentifier { 
        public String toString() { 
            return "TestID";
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createActivityFail() {
        new FakeActivity(null);
    }

    @Test
    public void createActivity1() {
        Context c = new Context("TEST");
        Activity a = new FakeActivity(c);
        assertEquals(a.getContext(), c);
    }
   
    @Test
    public void expectEventTrue() {
        Activity a = new FakeActivity(new Context("TEST"), false, true);
        assertTrue(a.expectsEvents());
    }
    
    @Test
    public void expectEventFalse() {
        Activity a = new FakeActivity(new Context("TEST"), false, false);
        assertFalse(a.expectsEvents());
    }

    @Test
    public void expectMaybeStolenTrue() {
        Activity a = new FakeActivity(new Context("TEST"), true, false);
        assertTrue(a.mayBeStolen());
    }
    
    @Test
    public void expectMaybeStolenFalse() {
        Activity a = new FakeActivity(new Context("TEST"), false, false);
        assertFalse(a.mayBeStolen());
    }

    @Test
    public void setIdentifier() {
        ActivityIdentifier id = ImplUtil.createActivityIdentifier(22, 3, 44, true);
        Activity a = new FakeActivity(new Context("TEST"), true, true);
        a.setIdentifier(id);
        assertEquals(a.identifier(), id);
    }
    
    @Test(expected = IllegalStateException.class)
    public void setIdentifierTwice() {
        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(22, 3, 44, true);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(22, 3, 55, true);
        
        Activity a = new FakeActivity(new Context("TEST"), true, true);
        a.setIdentifier(id1);
        a.setIdentifier(id2); // Should throw exception
    }
    
    @Test(expected = IllegalStateException.class)
    public void setIdentifierEventMismatch1() {
        ActivityIdentifier id = ImplUtil.createActivityIdentifier(22, 3, 44, true);
        Activity a = new FakeActivity(new Context("TEST"), false);
        a.setIdentifier(id); // Should throw exception, as the id claims events are expected, while the activity does not
    }
    
    @Test(expected = IllegalStateException.class)
    public void setIdentifierEventMismatch2() {
        ActivityIdentifier id = ImplUtil.createActivityIdentifier(22, 3, 44, false);
        Activity a = new FakeActivity(new Context("TEST"), true);
        a.setIdentifier(id); // Should throw exception, as the id claims events are not expected, while the activity says they are
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void setIdentifierNull() {
        Activity a = new FakeActivity(new Context("TEST"), false);
        a.setIdentifier(null); // Should throw exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void setIdentifierWrongType() {
        Activity a = new FakeActivity(new Context("TEST"), false);
        a.setIdentifier(new TestID()); // Should throw exception
    }
    
    
    
}
