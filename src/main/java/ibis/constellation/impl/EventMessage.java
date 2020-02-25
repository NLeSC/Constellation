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

import java.nio.ByteBuffer;
import java.util.List;

import ibis.constellation.Event;
import ibis.constellation.util.ByteBuffers;

public class EventMessage extends AbstractMessage implements ByteBuffers {

    private static final long serialVersionUID = -5430024744123215066L;

    public final Event event;

    public EventMessage(final ConstellationIdentifierImpl source, final ConstellationIdentifierImpl target, final Event e) {
        super(source, target);
        
        if (e == null) { 
            throw new IllegalArgumentException("EventMessage may not get null as event");
        }
        
        this.event = e;
    }

    @Override
    public String toString() {
        return "EventMessage: " + super.toString();
    }

    @Override
    public void pushByteBuffers(List<ByteBuffer> list) {
        Object tmp = event.getData();
            
        if (tmp != null && tmp instanceof ByteBuffers) {
            ((ByteBuffers) tmp).pushByteBuffers(list);
        }
    }

    @Override
    public void popByteBuffers(List<ByteBuffer> list) {
        Object tmp = event.getData();
            
        if (tmp != null && tmp instanceof ByteBuffers) {
            ((ByteBuffers) tmp).popByteBuffers(list);
        }
    }
}
