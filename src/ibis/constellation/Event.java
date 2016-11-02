package ibis.constellation;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

import ibis.constellation.impl.ActivityIdentifierImpl;

/**
 * An <code>Event</code> can be used for communication between {@link Activity
 * activities}. A common usage is to notify an activity that certain data is
 * available, or that some processing steps have been finished.
 */
public final class Event implements Serializable, ByteBuffers {

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
        if (!(target instanceof ActivityIdentifierImpl)
                || !(source instanceof ActivityIdentifierImpl)) {
            throw new IllegalArgumentException(
                    "target or source of event refers to an object of the wrong type");
        }
        this.source = source;
        this.target = target;
        this.data = data;
    }

    @Override
    public String toString() {
        String s = "source: ";
        if (source != null) {
            s += source.toString();
        } else
            s += " none";
        s += "; target: ";
        if (target != null) {
            s += target.toString();
        } else
            s += " none";

        s += "; data = ";
        if (data != null) {
            s += data.toString();
        } else {
            s += " none";
        }
        return s;
    }

    @Override
    public void pushByteBuffers(List<ByteBuffer> list) {
        if (data != null && data instanceof ByteBuffers) {
            ((ByteBuffers) data).pushByteBuffers(list);
        }
    }

    @Override
    public void popByteBuffers(List<ByteBuffer> list) {
        if (data != null && data instanceof ByteBuffers) {
            ((ByteBuffers) data).popByteBuffers(list);
        }
    }
}
