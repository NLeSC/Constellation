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

import org.junit.Test;

import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Event;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class EventMessageTest {

    @Test
    public void testConstructor1() {

        ConstellationIdentifierImpl source = ImplUtil.createConstellationIdentifier(42, 43);
        ConstellationIdentifierImpl target = ImplUtil.createConstellationIdentifier(13, 14);

        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(1, 0, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(2, 0, 2, false);

        Event e = new Event(id1, id2, null);

        EventMessage m = new EventMessage(source, target, e);

        assertEquals(e, m.event);
    }

    @Test
    public void testConstructor2() {

        ConstellationIdentifierImpl source = ImplUtil.createConstellationIdentifier(42, 43);
        ConstellationIdentifierImpl target = ImplUtil.createConstellationIdentifier(13, 14);

        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(1, 0, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(2, 0, 2, false);

        Event e = new Event(id1, id2, null);

        EventMessage m = new EventMessage(source, target, e);

        assertEquals(source, m.source);
    }

    @Test
    public void testConstructor3() {

        ConstellationIdentifierImpl source = ImplUtil.createConstellationIdentifier(42, 43);
        ConstellationIdentifierImpl target = ImplUtil.createConstellationIdentifier(13, 14);

        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(1, 0, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(2, 0, 2, false);

        Event e = new Event(id1, id2, null);

        EventMessage m = new EventMessage(source, target, e);

        assertEquals(target, m.target);
    }

    @Test
    public void testToString1() {

        ConstellationIdentifierImpl source = ImplUtil.createConstellationIdentifier(42, 43);
        ConstellationIdentifierImpl target = ImplUtil.createConstellationIdentifier(13, 14);

        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(1, 0, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(2, 0, 2, false);

        Event e = new Event(id1, id2, null);

        EventMessage m = new EventMessage(source, target, e);

        assertEquals("EventMessage: source: " + source.toString() + "; target: " + target.toString(), m.toString());
    }

    @Test
    public void testToString2() {

        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(1, 0, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(2, 0, 2, false);

        Event e = new Event(id1, id2, null);

        EventMessage m = new EventMessage(null, null, e);

        assertEquals("EventMessage: source: none; target: none", m.toString());
    }

}
