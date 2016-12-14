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
import static org.junit.Assert.assertFalse;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class StealRequestTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void createStealRequestNull() {
        new StealRequest(null, null, null, null, null, null, 0);
    }
        
    @Test
    public void createStealRequest1() {

        ConstellationIdentifier cid = ImplUtil.createConstellationIdentifier(0, 0);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealStrategy lss = StealStrategy.ANY;
        StealStrategy css = StealStrategy.BIGGEST;
        StealStrategy rss = StealStrategy.SMALLEST;
        
        StealPool sp = StealPool.WORLD;
        
        StealRequest tmp = new StealRequest(cid, exc, lss, css, rss, sp, 42);

        assertEquals(tmp.localStrategy, lss);
    }

    @Test
    public void createStealRequest2() {

        ConstellationIdentifier cid = ImplUtil.createConstellationIdentifier(0, 0);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealStrategy lss = StealStrategy.ANY;
        StealStrategy css = StealStrategy.BIGGEST;
        StealStrategy rss = StealStrategy.SMALLEST;
        
        StealPool sp = StealPool.WORLD;
        
        StealRequest tmp = new StealRequest(cid, exc, lss, css, rss, sp, 42);

        assertEquals(tmp.constellationStrategy, css);
    }

    @Test
    public void createStealRequest3() {

        ConstellationIdentifier cid = ImplUtil.createConstellationIdentifier(0, 0);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealStrategy lss = StealStrategy.ANY;
        StealStrategy css = StealStrategy.BIGGEST;
        StealStrategy rss = StealStrategy.SMALLEST;
        
        StealPool sp = StealPool.WORLD;
        
        StealRequest tmp = new StealRequest(cid, exc, lss, css, rss, sp, 42);

        assertEquals(tmp.remoteStrategy, rss);        
    }

    @Test
    public void createStealRequest4() {

        ConstellationIdentifier cid = ImplUtil.createConstellationIdentifier(0, 0);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealStrategy lss = StealStrategy.ANY;
        StealStrategy css = StealStrategy.BIGGEST;
        StealStrategy rss = StealStrategy.SMALLEST;
        
        StealPool sp = StealPool.WORLD;
        
        StealRequest tmp = new StealRequest(cid, exc, lss, css, rss, sp, 42);

        assertEquals(tmp.context, exc);        
    }

    @Test
    public void createStealRequest5() {

        ConstellationIdentifier cid = ImplUtil.createConstellationIdentifier(0, 0);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealStrategy lss = StealStrategy.ANY;
        StealStrategy css = StealStrategy.BIGGEST;
        StealStrategy rss = StealStrategy.SMALLEST;
        
        StealPool sp = StealPool.WORLD;
        
        StealRequest tmp = new StealRequest(cid, exc, lss, css, rss, sp, 42);

        assertEquals(tmp.pool, sp);
    }

    @Test
    public void createStealRequest6() {

        ConstellationIdentifier cid = ImplUtil.createConstellationIdentifier(0, 0);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealStrategy lss = StealStrategy.ANY;
        StealStrategy css = StealStrategy.BIGGEST;
        StealStrategy rss = StealStrategy.SMALLEST;
        
        StealPool sp = StealPool.WORLD;
        
        StealRequest tmp = new StealRequest(cid, exc, lss, css, rss, sp, 42);

        assertEquals(tmp.source, cid);
    }

    @Test
    public void setTarget() {

        ConstellationIdentifier cid = ImplUtil.createConstellationIdentifier(0, 0);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealStrategy lss = StealStrategy.ANY;
        StealStrategy css = StealStrategy.BIGGEST;
        StealStrategy rss = StealStrategy.SMALLEST;
        
        StealPool sp = StealPool.WORLD;
        
        StealRequest tmp = new StealRequest(cid, exc, lss, css, rss, sp, 42);

        ConstellationIdentifier target = ImplUtil.createConstellationIdentifier(1, 1);
        
        tmp.setTarget(target);
        
        assertEquals(target, tmp.target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setTargetNull() {

        ConstellationIdentifier cid = ImplUtil.createConstellationIdentifier(0, 0);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealStrategy lss = StealStrategy.ANY;
        StealStrategy css = StealStrategy.BIGGEST;
        StealStrategy rss = StealStrategy.SMALLEST;
        
        StealPool sp = StealPool.WORLD;
        
        StealRequest tmp = new StealRequest(cid, exc, lss, css, rss, sp, 42);

        tmp.setTarget(null);
    }
    
    
    @Test
    public void setRemote() {

        ConstellationIdentifier cid = ImplUtil.createConstellationIdentifier(0, 0);
        
        ExecutorContext exc = UnitExecutorContext.DEFAULT; 
        
        StealStrategy lss = StealStrategy.ANY;
        StealStrategy css = StealStrategy.BIGGEST;
        StealStrategy rss = StealStrategy.SMALLEST;
        
        StealPool sp = StealPool.WORLD;
        
        StealRequest tmp = new StealRequest(cid, exc, lss, css, rss, sp, 42);

        tmp.setRemote();
        
        assertFalse(tmp.isLocal());
    }
}
