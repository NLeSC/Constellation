package ibis.constellation;

import java.io.Serializable;

/**
 * An <code>ActivityIdentifier</code> uniquely identifies an {@link Activity}
 * instance.
 *
 * @version 1.0
 * @since 1.0
 */
public class ActivityIdentifier implements Serializable {

    /* Generated */
    private static final long serialVersionUID = 4785081436543353644L;

    // The globally unique UUID for this activity is "CID:AID"
    // "CID" is the id of the Constellation on which this Activity was created,
    // "AID" is the sequence number of this activity on that executor.
    private final long CID;
    private final long AID;
    private final boolean expectsEvents;

    /**
     * Constructs an activity identifier, using the specified constellation id
     * and sequence number on that constellation instance.
     *
     * TODO: should not be visible to the user. Make this an abstract class or
     * interface???
     *
     * @param cid
     *            the constellation id
     * @param aid
     *            the sequence number
     * @param expectsEvents
     *            whether this activity expects events
     */
    public ActivityIdentifier(long cid, long aid, boolean expectsEvents) {
        this.CID = cid;
        this.AID = aid;
        this.expectsEvents = expectsEvents;
    }

    /**
     * Returns <code>true</code> if this activity expects events,
     * <code>false</code> otherwise.
     *
     * @return whether this activity expects events.
     */
    public boolean expectsEvents() {
        return expectsEvents;
    }

    /**
     * Returns the constellation identifier that created this activity.
     *
     * @return the constellation identifier.
     */
    public ConstellationIdentifier getOrigin() {
        return new ConstellationIdentifier(CID);
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (int) (CID ^ (CID >>> 32));
        result = PRIME * result + (int) (AID ^ (AID >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        final ActivityIdentifier other = (ActivityIdentifier) obj;

        return (CID == other.CID && AID == other.AID);
    }

    @Override
    public String toString() {
        return "AID: " + Integer.toHexString((int) (CID >> 32) & 0xffffffff)
                + ":" + Integer.toHexString((int) (CID & 0xffffffff)) + ":"
                + Long.toHexString(AID); /*
                                          * + " (" + Integer.toHexString((int)(
                                          * lastKnownEID >> 32) & 0xffffffff) +
                                          * " " + Integer.toHexString((int)(
                                          * lastKnownEID & 0xffffffff)) + ")";
                                          */
    }
}
