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

import org.junit.Test;

import ibis.constellation.ConstellationIdentifier;
import ibis.constellation.StealPool;
import ibis.constellation.StealStrategy;
import ibis.constellation.context.ExecutorContext;
import ibis.constellation.context.UnitExecutorContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class StealReplyTest {
    
    @Test
    public void testStealReplySource() {

        ConstellationIdentifier src = ImplUtil.createConstellationIdentifier(0, 0);
        ConstellationIdentifier dst = ImplUtil.createConstellationIdentifier(1, 1);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealPool sp = StealPool.WORLD;
        
        ActivityRecord work = new ActivityRecord(null);
        
        StealReply tmp = new StealReply(src, dst, sp, exc, work);
        
        assertEquals(src, tmp.source);
    }
    
    @Test
    public void testStealReplyTarget() {

        ConstellationIdentifier src = ImplUtil.createConstellationIdentifier(0, 0);
        ConstellationIdentifier dst = ImplUtil.createConstellationIdentifier(1, 1);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealPool sp = StealPool.WORLD;
        
        ActivityRecord work = new ActivityRecord(null);
        
        StealReply tmp = new StealReply(src, dst, sp, exc, work);
        
        assertEquals(dst, tmp.target);
    }


    @Test
    public void testStealReplyContext() {

        ConstellationIdentifier src = ImplUtil.createConstellationIdentifier(0, 0);
        ConstellationIdentifier dst = ImplUtil.createConstellationIdentifier(1, 1);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealPool sp = StealPool.WORLD;
        
        ActivityRecord work = new ActivityRecord(null);
        
        StealReply tmp = new StealReply(src, dst, sp, exc, work);
        
        assertEquals(exc, tmp.getContext());
    }


    @Test
    public void testStealReplyPool() {

        ConstellationIdentifier src = ImplUtil.createConstellationIdentifier(0, 0);
        ConstellationIdentifier dst = ImplUtil.createConstellationIdentifier(1, 1);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealPool sp = StealPool.WORLD;
        
        ActivityRecord work = new ActivityRecord(null);
        
        StealReply tmp = new StealReply(src, dst, sp, exc, work);
        
        assertEquals(sp, tmp.getPool());
    }


    @Test
    public void testStealReplyWork() {

        ConstellationIdentifier src = ImplUtil.createConstellationIdentifier(0, 0);
        ConstellationIdentifier dst = ImplUtil.createConstellationIdentifier(1, 1);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealPool sp = StealPool.WORLD;
        
        ActivityRecord work = new ActivityRecord(null);
        
        StealReply tmp = new StealReply(src, dst, sp, exc, work);
        
        assertArrayEquals(new ActivityRecord[]{ work }, tmp.getWork());
    }

    
    @Test
    public void testStealReplySize1() {

        ConstellationIdentifier src = ImplUtil.createConstellationIdentifier(0, 0);
        ConstellationIdentifier dst = ImplUtil.createConstellationIdentifier(1, 1);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealPool sp = StealPool.WORLD;
        
        ActivityRecord work = new ActivityRecord(null);
        
        StealReply tmp = new StealReply(src, dst, sp, exc, work);
        
        assertEquals(1, tmp.getSize());
    }

    @Test
    public void testStealReplySize2() {

        ConstellationIdentifier src = ImplUtil.createConstellationIdentifier(0, 0);
        ConstellationIdentifier dst = ImplUtil.createConstellationIdentifier(1, 1);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealPool sp = StealPool.WORLD;
        
        StealReply tmp = new StealReply(src, dst, sp, exc, (ActivityRecord)null);
        
        assertEquals(0, tmp.getSize());
    }

    @Test
    public void testStealReplySize3() {

        ConstellationIdentifier src = ImplUtil.createConstellationIdentifier(0, 0);
        ConstellationIdentifier dst = ImplUtil.createConstellationIdentifier(1, 1);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealPool sp = StealPool.WORLD;
        
        ActivityRecord [] work = new ActivityRecord[] { new ActivityRecord(null), new ActivityRecord(null) };
        
        StealReply tmp = new StealReply(src, dst, sp, exc, work);
        
        assertEquals(2, tmp.getSize());
    }
    
    @Test
    public void testStealReplyEmpty1() {

        ConstellationIdentifier src = ImplUtil.createConstellationIdentifier(0, 0);
        ConstellationIdentifier dst = ImplUtil.createConstellationIdentifier(1, 1);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealPool sp = StealPool.WORLD;
        
        StealReply tmp = new StealReply(src, dst, sp, exc, (ActivityRecord)null);
        
        assertTrue(tmp.isEmpty());
    }

    @Test
    public void testStealReplyEmpty2() {

        ConstellationIdentifier src = ImplUtil.createConstellationIdentifier(0, 0);
        ConstellationIdentifier dst = ImplUtil.createConstellationIdentifier(1, 1);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealPool sp = StealPool.WORLD;
        
        StealReply tmp = new StealReply(src, dst, sp, exc, new ActivityRecord[0]);
        
        assertTrue(tmp.isEmpty());
    }

    @Test
    public void testStealReplyNotEmpty() {

        ConstellationIdentifier src = ImplUtil.createConstellationIdentifier(0, 0);
        ConstellationIdentifier dst = ImplUtil.createConstellationIdentifier(1, 1);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealPool sp = StealPool.WORLD;
        
        StealReply tmp = new StealReply(src, dst, sp, exc, new ActivityRecord(null));
        
        assertFalse(tmp.isEmpty());
    }
    
}