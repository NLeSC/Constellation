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
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Event;
import ibis.constellation.util.ByteBuffers;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class EventMessageTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNull() {

        ConstellationIdentifierImpl source = ImplUtil.createConstellationIdentifier(42, 43);
        ConstellationIdentifierImpl target = ImplUtil.createConstellationIdentifier(13, 14);

        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(1, 0, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(2, 0, 2, false);

        EventMessage m = new EventMessage(source, target, null);
    }

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

    @Test
    public void testPushBuffersNull() {

        ConstellationIdentifierImpl source = ImplUtil.createConstellationIdentifier(42, 43);
        ConstellationIdentifierImpl target = ImplUtil.createConstellationIdentifier(13, 14);

        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(1, 0, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(2, 0, 2, false);

        Event e = new Event(id1, id2, null);

        EventMessage m = new EventMessage(source, target, e);

        List<ByteBuffer> list = new ArrayList<>();

        // Should be a no-op since e.data = null
        m.pushByteBuffers(list);

        assertTrue(list.isEmpty());
    }

    @Test
    public void testPushBuffersWrongType() {

        ConstellationIdentifierImpl source = ImplUtil.createConstellationIdentifier(42, 43);
        ConstellationIdentifierImpl target = ImplUtil.createConstellationIdentifier(13, 14);

        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(1, 0, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(2, 0, 2, false);

        Event e = new Event(id1, id2, "Hello");

        EventMessage m = new EventMessage(source, target, e);

        List<ByteBuffer> list = new ArrayList<>();

        // Should be a no-op since e.data = null
        m.pushByteBuffers(list);

        assertTrue(list.isEmpty());
    }

    @Test
    public void testPopBuffersNull() {

        ConstellationIdentifierImpl source = ImplUtil.createConstellationIdentifier(42, 43);
        ConstellationIdentifierImpl target = ImplUtil.createConstellationIdentifier(13, 14);

        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(1, 0, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(2, 0, 2, false);

        Event e = new Event(id1, id2, null);

        EventMessage m = new EventMessage(source, target, e);

        List<ByteBuffer> list = new ArrayList<>();
        list.add(ByteBuffer.allocate(42));

        // Should be a no-op since e.data = null
        m.popByteBuffers(list);

        assertEquals(1, list.size());
    }

    @Test
    public void testPopBuffersWrongType() {

        ConstellationIdentifierImpl source = ImplUtil.createConstellationIdentifier(42, 43);
        ConstellationIdentifierImpl target = ImplUtil.createConstellationIdentifier(13, 14);

        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(1, 0, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(2, 0, 2, false);

        Event e = new Event(id1, id2, "Hello");

        EventMessage m = new EventMessage(source, target, e);

        List<ByteBuffer> list = new ArrayList<>();
        list.add(ByteBuffer.allocate(42));

        // Should be a no-op since e.data = null
        m.popByteBuffers(list);

        assertEquals(1, list.size());
    }

    private static class Buf implements Serializable, ByteBuffers {
        boolean pushCalled;
        boolean popCalled;

        @Override
        public void pushByteBuffers(List<ByteBuffer> list) {
            pushCalled = true;
        }

        @Override
        public void popByteBuffers(List<ByteBuffer> list) {
            popCalled = true;
        }

    }

    @Test
    public void testPushBuffers() {

        ConstellationIdentifierImpl source = ImplUtil.createConstellationIdentifier(42, 43);
        ConstellationIdentifierImpl target = ImplUtil.createConstellationIdentifier(13, 14);

        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(1, 0, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(2, 0, 2, false);

        Buf b = new Buf();

        Event e = new Event(id1, id2, b);

        EventMessage m = new EventMessage(source, target, e);

        List<ByteBuffer> list = new ArrayList<>();

        // Should be a no-op since e.data = null
        m.pushByteBuffers(list);

        assertTrue(b.pushCalled);
    }

    @Test
    public void testPopBuffers() {

        ConstellationIdentifierImpl source = ImplUtil.createConstellationIdentifier(42, 43);
        ConstellationIdentifierImpl target = ImplUtil.createConstellationIdentifier(13, 14);

        ActivityIdentifier id1 = ImplUtil.createActivityIdentifier(1, 0, 1, false);
        ActivityIdentifier id2 = ImplUtil.createActivityIdentifier(2, 0, 2, false);

        Buf b = new Buf();

        Event e = new Event(id1, id2, b);

        EventMessage m = new EventMessage(source, target, e);

        List<ByteBuffer> list = new ArrayList<>();
        list.add(ByteBuffer.allocate(42));

        // Should be a no-op since e.data = null
        m.popByteBuffers(list);

        assertTrue(b.popCalled);
    }

}
