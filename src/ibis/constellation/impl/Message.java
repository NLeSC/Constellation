package ibis.constellation.impl;

import java.io.Serializable;

import ibis.constellation.ConstellationIdentifier;

/**
 * Base class for {@link StealReply}, {@link StealRequest}, and {@link EventMessage} messages.
 */
public abstract class Message implements Serializable {

    /** Dummy serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Source of this message. */
    public final ConstellationIdentifier source;

    /**
     * Target of this message. Note that the target is not final, because a message may get another target because of relocated
     * activities, or the target may not yet be known at the time of construction.
     */
    public ConstellationIdentifier target;

    /**
     * Constructs a message with the specified source and target.
     *
     * @param source
     *            the source
     * @param target
     *            the target
     */
    Message(final ConstellationIdentifier source, final ConstellationIdentifier target) {
        this.source = source;
        this.target = target;
    }

    /**
     * Constructs a message with the specified source.
     *
     * @param source
     *            the source.
     */
    Message(final ConstellationIdentifier source) {
        this.source = source;
    }

    /**
     * Sets the target to the specified constellation identifier.
     *
     * @param cid
     *            the target.
     */
    synchronized void setTarget(ConstellationIdentifier cid) {
        this.target = cid;
    }

    @Override
    public String toString() {
        String s = "source: ";
        if (source != null) {
            s += source.toString();
        } else {
            s += " none";
        }
        s += "; target: ";
        if (target != null) {
            s += target.toString();
        } else {
            s += " none";
        }
        return s;
    }
}
