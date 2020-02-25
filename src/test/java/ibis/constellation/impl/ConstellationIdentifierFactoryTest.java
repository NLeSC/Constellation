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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class ConstellationIdentifierFactoryTest {

    @Test
    public void testGenerate1() {
        ConstellationIdentifierFactory cf = new ConstellationIdentifierFactory(42);
        ConstellationIdentifierImpl id = cf.generateConstellationIdentifier();
        assertEquals(id.getNodeId(), 42);
    }
        
    @Test
    public void testGenerate2() {
        ConstellationIdentifierFactory cf = new ConstellationIdentifierFactory(42);
        ConstellationIdentifierImpl id = cf.generateConstellationIdentifier();
        assertEquals(id.getLocalId(), 0);
    }
    
    @Test
    public void testGenerate3() {
        ConstellationIdentifierFactory cf = new ConstellationIdentifierFactory(42);
        cf.generateConstellationIdentifier();
        ConstellationIdentifierImpl id = cf.generateConstellationIdentifier();
        assertEquals(id.getLocalId(), 1);
    }
    
    @Test
    public void testIsLocal1() {
        ConstellationIdentifierFactory cf = new ConstellationIdentifierFactory(42);
        ConstellationIdentifierImpl id = cf.generateConstellationIdentifier();
        assertTrue(cf.isLocal(id));
    }

    @Test
    public void testIsLocal2() {
        ConstellationIdentifierFactory cf = new ConstellationIdentifierFactory(42);
        ConstellationIdentifierImpl id = ImplUtil.createConstellationIdentifier(44, 2);
        assertFalse(cf.isLocal(id));
    }
    

}
