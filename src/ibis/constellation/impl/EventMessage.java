package ibis.constellation.impl;

import java.io.IOException;

import ibis.constellation.Event;
import ibis.constellation.ObjectData;
import ibis.ipl.ReadMessage;
import ibis.ipl.WriteMessage;

public class EventMessage extends Message implements ObjectData {

    private static final long serialVersionUID = -5430024744123215066L;

    public final Event event;

    public EventMessage(final ConstellationIdentifier source,
            final ConstellationIdentifier constellationIdentifier,
            final Event e) {
        super(source, constellationIdentifier);
        this.event = e;
    }

    @Override
    public void writeData(WriteMessage m) throws IOException {
        if (event != null) {
            event.writeData(m);
        }
    }

    @Override
    public void readData(ReadMessage m) throws IOException {
        if (event != null) {
            event.readData(m);
        }
    }
}
