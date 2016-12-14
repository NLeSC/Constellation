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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import ibis.constellation.impl.ImplUtil;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class EventTest {

    @Test(expected = IllegalArgumentException.class)
    public void createEventFail() {
        new Event(null, null, null);
    }
        
    @Test
    public void createEvent() {
      
        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(1, 0, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(2, 0, 2, false);
        
        Event e = new Event(id1, id2, null);
        
        assertEquals(e.getSource(), id1);
        assertEquals(e.getTarget(), id2);
    }
    
    @Test
    public void createEventWithData() {
      
        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(1, 0, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(2, 0, 2, false);
   
        String data = "Hello World";
        
        Event e = new Event(id1, id2, data);
        
        assertEquals(e.getData(), data);
    }
    
    @Test
    public void pushDataFail() {
      
        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(1, 0, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(2, 0, 2, false);
        
        Event e = new Event(id1, id2, null);
     
        List<ByteBuffer> list = new LinkedList<>();
        
        // This should succeed, even if event data is null
        e.pushByteBuffers(list);
    }

    @Test
    public void popDataFail() {
      
        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(1, 0, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(2, 0, 2, false);
        
        Event e = new Event(id1, id2, null);
     
        List<ByteBuffer> list = new LinkedList<>();
        
        // This should succeed, even if event data is null
        e.popByteBuffers(list);
    }

    
    @Test
    public void popDataFailType() {
      
        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(1, 0, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(2, 0, 2, false);
        
        Event e = new Event(id1, id2, "Hello World");
     
        List<ByteBuffer> list = new LinkedList<>();
        
        // This should succeed, even if event data does not implement ByteBuffers.
        e.popByteBuffers(list);
    }
    
    @Test
    public void pushPopData() {
      
        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(1, 0, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(2, 0, 2, false);
        
        SimpleByteBuffers b = new SimpleByteBuffers();
        
        Event e = new Event(id1, id2, b);
     
        List<ByteBuffer> listIn = new LinkedList<>();
        ByteBuffer tmp = ByteBuffer.allocate(1);
        listIn.add(tmp);
        
        e.pushByteBuffers(listIn);
       
        List<ByteBuffer> listOut = new LinkedList<>();
        e.popByteBuffers(listOut);
        
        assertEquals(listIn, listOut);
    }

    
    
    
}
