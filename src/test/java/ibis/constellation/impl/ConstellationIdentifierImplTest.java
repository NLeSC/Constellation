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
public class ConstellationIdentifierImplTest {

    @Test
    public void testConstructor1() {
        ConstellationIdentifierImpl c = new ConstellationIdentifierImpl(42, 43);
        assertEquals(42, c.getNodeId());
    }

    @Test
    public void testConstructor2() {
        ConstellationIdentifierImpl c = new ConstellationIdentifierImpl(42, 43);
        assertEquals(43, c.getLocalId());
    }

    @Test
    public void testHash() {
        ConstellationIdentifierImpl c = new ConstellationIdentifierImpl(42, 43);
        assertEquals((31 * 43) ^ 42, c.hashCode());
    }

    @Test
    public void testEquals1() {
        ConstellationIdentifierImpl c = new ConstellationIdentifierImpl(42, 43);
        assertTrue(c.equals(c));
    }

    @Test
    public void testEquals2() {
        ConstellationIdentifierImpl c = new ConstellationIdentifierImpl(42, 43);
        ConstellationIdentifierImpl b = null;
        assertFalse(c.equals(b));
    }

    @Test
    public void testEquals3() {
        ConstellationIdentifierImpl c = new ConstellationIdentifierImpl(42, 43);
        assertFalse(c.equals("Hello"));
    }

    @Test
    public void testEquals4() {
        ConstellationIdentifierImpl c = new ConstellationIdentifierImpl(42, 43);
        ConstellationIdentifierImpl c2 = new ConstellationIdentifierImpl(42, 44);
        assertFalse(c.equals(c2));
    }

    @Test
    public void testEquals5() {
        ConstellationIdentifierImpl c = new ConstellationIdentifierImpl(42, 43);
        ConstellationIdentifierImpl c2 = new ConstellationIdentifierImpl(43, 43);
        assertFalse(c.equals(c2));
    }

    @Test
    public void testEquals6() {
        ConstellationIdentifierImpl c = new ConstellationIdentifierImpl(42, 43);
        ConstellationIdentifierImpl c2 = new ConstellationIdentifierImpl(42, 43);
        assertTrue(c.equals(c2));
    }

    @Test
    public void testToString() {
        ConstellationIdentifierImpl c = new ConstellationIdentifierImpl(42, 43);
        assertEquals(c.toString(), "CID:" + Integer.toHexString(42) + ":" + Integer.toHexString(43));
    }
}
