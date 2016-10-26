package ibis.constellation.impl;

import java.nio.ByteBuffer;
import java.util.List;

import ibis.constellation.ByteBuffers;
import ibis.constellation.Event;

public class EventMessage extends Message implements ByteBuffers {

    private static final long serialVersionUID = -5430024744123215066L;

    public final Event event;

    public EventMessage(final ConstellationIdentifier source,
            final ConstellationIdentifier constellationIdentifier,
            final Event e) {
        super(source, constellationIdentifier);
        this.event = e;
    }

    @Override
    public String toString() {
        return "EventMessage: " + super.toString();
    }

    @Override
    public void pushByteBuffers(List<ByteBuffer> list) {
        if (event != null) {
            event.pushByteBuffers(list);
        }
    }

    @Override
    public void popByteBuffers(List<ByteBuffer> list) {
        if (event != null) {
            event.popByteBuffers(list);
        }
    }
}
