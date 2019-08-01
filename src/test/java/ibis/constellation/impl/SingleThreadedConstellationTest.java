/*
 * Copyright 2019 Vrije Universiteit Amsterdam
 *                Netherlands eScience Center
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

import static org.junit.Assert.*;

import org.junit.Test;

import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationProperties;
import ibis.constellation.Context;
import ibis.constellation.StealStrategy;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class SingleThreadedConstellationTest {
   
    @Test(expected = IllegalArgumentException.class)
    public void testCreateFail1() throws Exception { 
        new SingleThreadedConstellation(null, null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateFail2() throws Exception { 
        ConstellationConfiguration config = new ConstellationConfiguration(Context.DEFAULT);
        new SingleThreadedConstellation(config, null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateFail3() throws Exception { 
        ConstellationProperties props = new ConstellationProperties();
        new SingleThreadedConstellation(null, props);
    }
    
    @Test
    public void testContext() throws Exception { 
        ConstellationConfiguration config = new ConstellationConfiguration(new Context("test"));
        ConstellationProperties props = new ConstellationProperties();
        SingleThreadedConstellation st = new SingleThreadedConstellation(config, props);
        
        assertEquals(new Context("test"), st.getContext());
    }

    @Test
    public void testLocalStealStrategy() throws Exception { 
        ConstellationConfiguration config = new ConstellationConfiguration(Context.DEFAULT, 
                StealStrategy.BIGGEST, StealStrategy.SMALLEST, StealStrategy.SMALLEST);
        
        ConstellationProperties props = new ConstellationProperties();
        SingleThreadedConstellation st = new SingleThreadedConstellation(config, props);
        
        assertEquals(StealStrategy.BIGGEST, st.getLocalStealStrategy());
    }
   
    @Test
    public void testConstellationStealStrategy() throws Exception { 
        ConstellationConfiguration config = new ConstellationConfiguration(Context.DEFAULT, 
                StealStrategy.SMALLEST, StealStrategy.BIGGEST, StealStrategy.SMALLEST);
        
        ConstellationProperties props = new ConstellationProperties();
        SingleThreadedConstellation st = new SingleThreadedConstellation(config, props);
        
        assertEquals(StealStrategy.BIGGEST, st.getConstellationStealStrategy());
    }
    
    @Test
    public void testRemoteStealStrategy() throws Exception { 
        ConstellationConfiguration config = new ConstellationConfiguration(Context.DEFAULT, 
                StealStrategy.SMALLEST, StealStrategy.SMALLEST, StealStrategy.BIGGEST);
        
        ConstellationProperties props = new ConstellationProperties();
        SingleThreadedConstellation st = new SingleThreadedConstellation(config, props);
        
        assertEquals(StealStrategy.BIGGEST, st.getRemoteStealStrategy());
    }
    
    


}


