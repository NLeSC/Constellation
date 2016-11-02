package ibis.constellation.impl;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;

/**
 * An <code>ActivityIdentifierImpl</code> uniquely identifies an
 * {@link Activity} instance.
 *
 * @version 1.0
 * @since 1.0
 */
public class ActivityIdentifierImpl implements ActivityIdentifier {

    /* Generated */
    private static final long serialVersionUID = 4785081436543353644L;

    // The globally unique UUID for this activity is "CID:AID"
    // "CID" is the id of the Constellation on which this ActivityBase was
    // created,
    // "AID" is the sequence number of this activity on that executor.
    private final ConstellationIdentifier CID;
    private final long AID;
    private final boolean expectsEvents;

    /**
     * Constructs an activity identifier, using the specified constellation id
     * and sequence number on that constellation instance.
     *
     * @param cid
     *            the constellation id
     * @param aid
     *            the sequence number
     * @param expectsEvents
     *            whether this activity expects events
     */
    ActivityIdentifierImpl(ConstellationIdentifier cid, long aid,
            boolean expectsEvents) {
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
    ConstellationIdentifier getOrigin() {
        return CID;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (int) (CID.getId() ^ (CID.getId() >>> 32));
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

        final ActivityIdentifierImpl other = (ActivityIdentifierImpl) obj;

        return (CID.equals(other.CID) && AID == other.AID);
    }

    @Override
    public String toString() {
        return "AID: "
                + Integer.toHexString((int) (CID.getId() >> 32) & 0xffffffff)
                + ":" + Integer.toHexString((int) (CID.getId() & 0xffffffff))
                + ":" + Long.toHexString(AID);
    }
}
