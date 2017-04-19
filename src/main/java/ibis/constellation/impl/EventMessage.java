package ibis.constellation.impl;

import java.nio.ByteBuffer;
import java.util.List;

import ibis.constellation.Event;
import ibis.constellation.util.ByteBuffers;

public class EventMessage extends Message implements ByteBuffers {

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
