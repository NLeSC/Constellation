package ibis.constellation;

import java.io.IOException;
import java.io.Serializable;

import ibis.ipl.ReadMessage;
import ibis.ipl.WriteMessage;

/**
 * An <code>Event</code> can be used for communication between {@link Activity
 * activities}. A common usage is to notify an activity that certain data is
 * available, or that some processing steps have been finished.
 */
public class Event implements Serializable, ObjectData {

    private static final long serialVersionUID = 8672434537078611592L;

    /** The source activity of this event. */
    public final ActivityIdentifier source;

    /** The destination activity of this event. */
    public final ActivityIdentifier target;

    /** The data of this event. */
    public final Object data;

    /**
     * Constructs an event with the specified parameters: a source, a target,
     * and its data.
     *
     * @param source
     *            the source activity of this event
     * @param target
     *            the target activity for this event
     * @param data
     *            the data of this event
     */
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
