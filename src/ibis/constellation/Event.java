package ibis.constellation;

import ibis.ipl.ReadMessage;
import ibis.ipl.WriteMessage;

import java.io.IOException;
import java.io.Serializable;

public class Event implements Serializable, ObjectData {

    private static final long serialVersionUID = 8672434537078611592L;

    public final ActivityIdentifier source;
    public final ActivityIdentifier target;

    public final Object data;

    public Event(ActivityIdentifier source, ActivityIdentifier target,
            Object data) {
        this.source = source;
        this.target = target;
        this.data = data;
    }

    @Override
    public void writeData(WriteMessage m) throws IOException {
        if (data != null && data instanceof ObjectData) {
            ((ObjectData) data).writeData(m);
        }
    }

    @Override
    public void readData(ReadMessage m) throws IOException {
        if (data != null && data instanceof ObjectData) {
            ((ObjectData) data).readData(m);
        }
    }
}
