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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import ibis.constellation.StealPool;
import ibis.constellation.StealStrategy;
import ibis.constellation.context.ExecutorContext;
import ibis.constellation.context.UnitExecutorContext;

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
    public void createStealRequest2() {

        ConstellationIdentifierImpl cid = ImplUtil.createConstellationIdentifier(0, 0);

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

        ConstellationIdentifierImpl cid = ImplUtil.createConstellationIdentifier(0, 0);

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

        ConstellationIdentifierImpl cid = ImplUtil.createConstellationIdentifier(0, 0);

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

        ConstellationIdentifierImpl cid = ImplUtil.createConstellationIdentifier(0, 0);

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

        ConstellationIdentifierImpl cid = ImplUtil.createConstellationIdentifier(0, 0);

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

        ConstellationIdentifierImpl cid = ImplUtil.createConstellationIdentifier(0, 0);

        ExecutorContext exc = UnitExecutorContext.DEFAULT;

        StealStrategy lss = StealStrategy.ANY;
        StealStrategy css = StealStrategy.BIGGEST;
        StealStrategy rss = StealStrategy.SMALLEST;

        StealPool sp = StealPool.WORLD;

        StealRequest tmp = new StealRequest(cid, exc, lss, css, rss, sp, 42);

        ConstellationIdentifierImpl target = ImplUtil.createConstellationIdentifier(1, 1);

        tmp.setTarget(target);

        assertEquals(target, tmp.target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setTargetNull() {

        ConstellationIdentifierImpl cid = ImplUtil.createConstellationIdentifier(0, 0);

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

        ConstellationIdentifierImpl cid = ImplUtil.createConstellationIdentifier(0, 0);

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
