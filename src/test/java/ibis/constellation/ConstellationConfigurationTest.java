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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ibis.constellation.Context;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class ConstellationConfigurationTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void testNull1() {
        AbstractContext context = null;
        StealPool belongsTo = StealPool.WORLD;
        StealPool stealsFrom = StealPool.WORLD;
        StealStrategy localStealStrategy = StealStrategy.SMALLEST;
        StealStrategy constellationStealStrategy = StealStrategy.SMALLEST;
        StealStrategy remoteStealStrategy = StealStrategy.SMALLEST;
        
        new ConstellationConfiguration(context, belongsTo, stealsFrom, localStealStrategy, constellationStealStrategy, remoteStealStrategy);
    }
 
    @Test(expected = IllegalArgumentException.class)
    public void testNull2() {
        AbstractContext context = Context.DEFAULT;
        StealPool belongsTo = null;
        StealPool stealsFrom = StealPool.WORLD;
        StealStrategy localStealStrategy = StealStrategy.SMALLEST;
        StealStrategy constellationStealStrategy = StealStrategy.SMALLEST;
        StealStrategy remoteStealStrategy = StealStrategy.SMALLEST;
        
        new ConstellationConfiguration(context, belongsTo, stealsFrom, localStealStrategy, constellationStealStrategy, remoteStealStrategy);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNull3() {
        AbstractContext context = Context.DEFAULT;
        StealPool belongsTo = StealPool.WORLD;
        StealPool stealsFrom = null;
        StealStrategy localStealStrategy = StealStrategy.SMALLEST;
        StealStrategy constellationStealStrategy = StealStrategy.SMALLEST;
        StealStrategy remoteStealStrategy = StealStrategy.SMALLEST;
        
        new ConstellationConfiguration(context, belongsTo, stealsFrom, localStealStrategy, constellationStealStrategy, remoteStealStrategy);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNull4() {
        AbstractContext context = Context.DEFAULT;
        StealPool belongsTo = StealPool.WORLD;
        StealPool stealsFrom = StealPool.WORLD;
        StealStrategy localStealStrategy = null;
        StealStrategy constellationStealStrategy = StealStrategy.SMALLEST;
        StealStrategy remoteStealStrategy = StealStrategy.SMALLEST;
        
        new ConstellationConfiguration(context, belongsTo, stealsFrom, localStealStrategy, constellationStealStrategy, remoteStealStrategy);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNull5() {
        AbstractContext context = Context.DEFAULT;
        StealPool belongsTo = StealPool.WORLD;
        StealPool stealsFrom = StealPool.WORLD;
        StealStrategy localStealStrategy = StealStrategy.SMALLEST;
        StealStrategy constellationStealStrategy = null;
        StealStrategy remoteStealStrategy = StealStrategy.SMALLEST;
        
        new ConstellationConfiguration(context, belongsTo, stealsFrom, localStealStrategy, constellationStealStrategy, remoteStealStrategy);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNull6() {
        AbstractContext context = Context.DEFAULT;
        StealPool belongsTo = StealPool.WORLD;
        StealPool stealsFrom = StealPool.WORLD;
        StealStrategy localStealStrategy = StealStrategy.SMALLEST;
        StealStrategy constellationStealStrategy = StealStrategy.SMALLEST;
        StealStrategy remoteStealStrategy = null;
        
        new ConstellationConfiguration(context, belongsTo, stealsFrom, localStealStrategy, constellationStealStrategy, remoteStealStrategy);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNull7() {
        AbstractContext context = null;
        
        new ConstellationConfiguration(context);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull8() {
        AbstractContext context = null;
        StealStrategy localStealStrategy = StealStrategy.SMALLEST;
        StealStrategy constellationStealStrategy = StealStrategy.SMALLEST;
        StealStrategy remoteStealStrategy = StealStrategy.SMALLEST;
        
        new ConstellationConfiguration(context, localStealStrategy, constellationStealStrategy, remoteStealStrategy);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNull9() {
        AbstractContext context = Context.DEFAULT;
        StealStrategy localStealStrategy = null;
        StealStrategy constellationStealStrategy = StealStrategy.SMALLEST;
        StealStrategy remoteStealStrategy = StealStrategy.SMALLEST;
        
        new ConstellationConfiguration(context, localStealStrategy, constellationStealStrategy, remoteStealStrategy);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNull10() {
        AbstractContext context = Context.DEFAULT;
        StealStrategy localStealStrategy = StealStrategy.SMALLEST;
        StealStrategy constellationStealStrategy = null;
        StealStrategy remoteStealStrategy = StealStrategy.SMALLEST;
        
        new ConstellationConfiguration(context, localStealStrategy, constellationStealStrategy, remoteStealStrategy);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNull11() {
        AbstractContext context = Context.DEFAULT;
        StealStrategy localStealStrategy = StealStrategy.SMALLEST;
        StealStrategy constellationStealStrategy = StealStrategy.SMALLEST;
        StealStrategy remoteStealStrategy = null;
        
        new ConstellationConfiguration(context, localStealStrategy, constellationStealStrategy, remoteStealStrategy);
    }
    
    @Test
    public void testContext1() {
        AbstractContext context = Context.DEFAULT;
        StealPool belongsTo = StealPool.WORLD;
        StealPool stealsFrom = StealPool.WORLD;
        StealStrategy localStealStrategy = StealStrategy.SMALLEST;
        StealStrategy constellationStealStrategy = StealStrategy.SMALLEST;
        StealStrategy remoteStealStrategy = StealStrategy.SMALLEST;
        
        ConstellationConfiguration c = new ConstellationConfiguration(context, belongsTo, stealsFrom, localStealStrategy, 
                constellationStealStrategy, remoteStealStrategy);
   
        assertEquals(c.getContext(), Context.DEFAULT);
    }
    
    @Test
    public void testContextDefaults1() {
        AbstractContext context = Context.DEFAULT;
        
        ConstellationConfiguration c = new ConstellationConfiguration(context);
        
        assertEquals(c.getContext(), Context.DEFAULT);
    }

    @Test
    public void testDefaults2() {
        AbstractContext context = Context.DEFAULT;
        ConstellationConfiguration c = new ConstellationConfiguration(context);
        assertEquals(c.getBelongsToPool(), StealPool.WORLD);
    }

    @Test
    public void testDefaults3() {
        AbstractContext context = Context.DEFAULT;
        ConstellationConfiguration c = new ConstellationConfiguration(context);
        assertEquals(c.getStealsFrom(), StealPool.WORLD);
    }

    @Test    
    public void testDefaults4() {
        AbstractContext context = Context.DEFAULT;
        ConstellationConfiguration c = new ConstellationConfiguration(context);
        assertEquals(c.getLocalStealStrategy(), StealStrategy.SMALLEST);
    }

    @Test
    public void testDefaults5() {
        AbstractContext context = Context.DEFAULT;
        ConstellationConfiguration c = new ConstellationConfiguration(context);
        assertEquals(c.getConstellationStealStrategy(), StealStrategy.SMALLEST);
    }
    
    @Test
    public void testDefaults6() {
        AbstractContext context = Context.DEFAULT;
        ConstellationConfiguration c = new ConstellationConfiguration(context);
        assertEquals(c.getRemoteStealStrategy(), StealStrategy.SMALLEST);
    }
    
    @Test
    public void testDefaults7() {
        AbstractContext context = Context.DEFAULT;
        StealStrategy steal = StealStrategy.BIGGEST;
        ConstellationConfiguration c = new ConstellationConfiguration(context, steal);
        assertEquals(c.getLocalStealStrategy(), StealStrategy.BIGGEST);
    }
    
    @Test
    public void testDefaults8() {
        AbstractContext context = Context.DEFAULT;
        StealStrategy steal = StealStrategy.BIGGEST;
        ConstellationConfiguration c = new ConstellationConfiguration(context, steal);
        assertEquals(c.getConstellationStealStrategy(), StealStrategy.BIGGEST);
    }
    
    @Test
    public void testDefaults9() {
        AbstractContext context = Context.DEFAULT;
        StealStrategy steal = StealStrategy.BIGGEST;
        ConstellationConfiguration c = new ConstellationConfiguration(context, steal);
        assertEquals(c.getRemoteStealStrategy(), StealStrategy.BIGGEST);
    }

    @Test
    public void testDefaults10() {
        AbstractContext context = Context.DEFAULT;
        StealStrategy steal = StealStrategy.BIGGEST;
        ConstellationConfiguration c = new ConstellationConfiguration(context, steal, steal, steal);
        assertEquals(c.getBelongsToPool(), StealPool.WORLD);
    }
    
    @Test
    public void testDefaults11() {
        AbstractContext context = Context.DEFAULT;
        StealStrategy steal = StealStrategy.BIGGEST;
        ConstellationConfiguration c = new ConstellationConfiguration(context, steal, steal, steal);
        assertEquals(c.getStealsFrom(), StealPool.WORLD);
    }
    
    @Test
    public void testDefaults12() {
        AbstractContext context = Context.DEFAULT;
        StealStrategy steal1 = StealStrategy.BIGGEST;
        StealStrategy steal2 = StealStrategy.SMALLEST;
        
        ConstellationConfiguration c = new ConstellationConfiguration(context, steal1, steal2);
        assertEquals(c.getLocalStealStrategy(), steal1);
    }
    
    @Test
    public void testDefaults13() {
        AbstractContext context = Context.DEFAULT;
        StealStrategy steal1 = StealStrategy.BIGGEST;
        StealStrategy steal2 = StealStrategy.SMALLEST;
        
        ConstellationConfiguration c = new ConstellationConfiguration(context, steal1, steal2);
        assertEquals(c.getConstellationStealStrategy(), steal2);
    }
    
    @Test
    public void testDefaults14() {
        AbstractContext context = Context.DEFAULT;
        StealStrategy steal1 = StealStrategy.BIGGEST;
        StealStrategy steal2 = StealStrategy.SMALLEST;
        
        ConstellationConfiguration c = new ConstellationConfiguration(context, steal1, steal2);
        assertEquals(c.getRemoteStealStrategy(), steal2);
    }
    
    @Test
    public void testSetContext() {
        AbstractContext context = Context.DEFAULT;
        AbstractContext context2 = new Context("A");
        
        ConstellationConfiguration c = new ConstellationConfiguration(context);

        c.setContext(context2);
        
        assertEquals(c.getContext(), context2);
    }

    @Test
    public void testSetLocalStealStrategy() {
        AbstractContext context = Context.DEFAULT;
        StealStrategy steal = StealStrategy.BIGGEST;
        
        ConstellationConfiguration c = new ConstellationConfiguration(context, steal);

        c.setLocalStealStrategy(StealStrategy.SMALLEST);
        
        assertEquals(c.getLocalStealStrategy(), StealStrategy.SMALLEST);
        
    }
    
    @Test
    public void testSetConstellationStealStrategy() {
        AbstractContext context = Context.DEFAULT;
        StealStrategy steal = StealStrategy.BIGGEST;
        
        ConstellationConfiguration c = new ConstellationConfiguration(context, steal);

        c.setConstellationStealStrategy(StealStrategy.SMALLEST);
        
        assertEquals(c.getConstellationStealStrategy(), StealStrategy.SMALLEST);
        
    }
    
    @Test
    public void testSetRemoteStealStrategy() {
        AbstractContext context = Context.DEFAULT;
        StealStrategy steal = StealStrategy.BIGGEST;
        
        ConstellationConfiguration c = new ConstellationConfiguration(context, steal);

        c.setRemoteStealStrategy(StealStrategy.SMALLEST);
        
        assertEquals(c.getRemoteStealStrategy(), StealStrategy.SMALLEST);
        
    }
    
    @Test
    public void testsetBelongsTo() {
        AbstractContext context = Context.DEFAULT;
        StealPool belongsTo = StealPool.WORLD;
        StealPool stealsFrom = StealPool.WORLD;
        StealStrategy localStealStrategy = StealStrategy.SMALLEST;
        StealStrategy constellationStealStrategy = StealStrategy.SMALLEST;
        StealStrategy remoteStealStrategy = StealStrategy.SMALLEST;
        
        ConstellationConfiguration c = new ConstellationConfiguration(context, belongsTo, stealsFrom, localStealStrategy, 
                constellationStealStrategy, remoteStealStrategy);
   
        c.setBelongsToPool(StealPool.NONE);
        
        assertEquals(c.getBelongsToPool(), StealPool.NONE);
    }
    
    @Test
    public void testsetStealsFrom() {
        AbstractContext context = Context.DEFAULT;
        StealPool belongsTo = StealPool.WORLD;
        StealPool stealsFrom = StealPool.WORLD;
        StealStrategy localStealStrategy = StealStrategy.SMALLEST;
        StealStrategy constellationStealStrategy = StealStrategy.SMALLEST;
        StealStrategy remoteStealStrategy = StealStrategy.SMALLEST;
        
        ConstellationConfiguration c = new ConstellationConfiguration(context, belongsTo, stealsFrom, localStealStrategy, 
                constellationStealStrategy, remoteStealStrategy);
   
        c.setStealsFrom(StealPool.NONE);
        
        assertEquals(c.getStealsFrom(), StealPool.NONE);
    }
   
    
    @Test
    public void testBelongsTo() {
        AbstractContext context = Context.DEFAULT;
        StealPool belongsTo = StealPool.NONE;
        StealPool stealsFrom = StealPool.WORLD;
        StealStrategy localStealStrategy = StealStrategy.SMALLEST;
        StealStrategy constellationStealStrategy = StealStrategy.SMALLEST;
        StealStrategy remoteStealStrategy = StealStrategy.SMALLEST;
        
        ConstellationConfiguration c = new ConstellationConfiguration(context, belongsTo, stealsFrom, localStealStrategy, 
                constellationStealStrategy, remoteStealStrategy);
   
        assertEquals(c.getBelongsToPool(), StealPool.NONE);
    }

    @Test
    public void testStealsFrom() {
        AbstractContext context = Context.DEFAULT;
        StealPool belongsTo = StealPool.WORLD;
        StealPool stealsFrom = StealPool.NONE;
        StealStrategy localStealStrategy = StealStrategy.SMALLEST;
        StealStrategy constellationStealStrategy = StealStrategy.SMALLEST;
        StealStrategy remoteStealStrategy = StealStrategy.SMALLEST;
        
        ConstellationConfiguration c = new ConstellationConfiguration(context, belongsTo, stealsFrom, localStealStrategy, 
                constellationStealStrategy, remoteStealStrategy);
   
        assertEquals(c.getStealsFrom(), StealPool.NONE);
    }

    @Test
    public void testLocalStealStrategy() {
        AbstractContext context = Context.DEFAULT;
        StealPool belongsTo = StealPool.NONE;
        StealPool stealsFrom = StealPool.WORLD;
        StealStrategy localStealStrategy = StealStrategy.BIGGEST;
        StealStrategy constellationStealStrategy = StealStrategy.SMALLEST;
        StealStrategy remoteStealStrategy = StealStrategy.SMALLEST;
        
        ConstellationConfiguration c = new ConstellationConfiguration(context, belongsTo, stealsFrom, localStealStrategy, 
                constellationStealStrategy, remoteStealStrategy);
   
        assertEquals(c.getLocalStealStrategy(), StealStrategy.BIGGEST);
    }
    
    @Test
    public void testConstellationStealStrategy() {
        AbstractContext context = Context.DEFAULT;
        StealPool belongsTo = StealPool.NONE;
        StealPool stealsFrom = StealPool.WORLD;
        StealStrategy localStealStrategy = StealStrategy.SMALLEST;
        StealStrategy constellationStealStrategy = StealStrategy.BIGGEST;
        StealStrategy remoteStealStrategy = StealStrategy.SMALLEST;
        
        ConstellationConfiguration c = new ConstellationConfiguration(context, belongsTo, stealsFrom, localStealStrategy, 
                constellationStealStrategy, remoteStealStrategy);
   
        assertEquals(c.getConstellationStealStrategy(), StealStrategy.BIGGEST);
    }
    
    @Test
    public void testRemoteStealStrategy() {
        AbstractContext context = Context.DEFAULT;
        StealPool belongsTo = StealPool.NONE;
        StealPool stealsFrom = StealPool.WORLD;
        StealStrategy localStealStrategy = StealStrategy.SMALLEST;
        StealStrategy constellationStealStrategy = StealStrategy.SMALLEST;
        StealStrategy remoteStealStrategy = StealStrategy.BIGGEST;
        
        ConstellationConfiguration c = new ConstellationConfiguration(context, belongsTo, stealsFrom, localStealStrategy, 
                constellationStealStrategy, remoteStealStrategy);
   
        assertEquals(c.getRemoteStealStrategy(), StealStrategy.BIGGEST);
    }
    
    
}