package ibis.constellation.impl;

import java.util.concurrent.locks.ReentrantLock;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;

/**
 * An <code>ActivityIdentifierImpl</code> uniquely identifies an
 * {@link Activity} instance.
 *
 * @version 1.0
 * @since 1.0
 */
public abstract class ActivityIdentifierImpl {

    /* Generated */
    private static final long serialVersionUID = 4785081436543353644L;

    private static final ReentrantLock lock = new ReentrantLock();

    // The globally unique UUID for this activity is "CID:AID"
    // "CID" is the id of the Constellation on which this ActivityBase was
    // created,
    // "AID" is the sequence number of this activity on that executor.
    private ConstellationIdentifier CID;
    private long AID;
    private boolean expectsEvents;

    protected ActivityIdentifierImpl() {
        // Make sure that we can only create an ActivityIdentifier through the
        // createActivityIdentifier method.
        if (!lock.isHeldByCurrentThread()) {
            throw new Error("Invalid creation of activity identifier");
        }
    }

    static ActivityIdentifier createActivityIdentifier(
            ConstellationIdentifier cid, long aid, boolean expectsEvents) {
        lock.lock();
        try {
            ActivityIdentifierImpl id = new ActivityIdentifier();
            id.CID = cid;
            id.AID = aid;
            id.expectsEvents = expectsEvents;
            return (ActivityIdentifier) id;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns <code>true</code> if this activity expects events,
     * <code>false</code> otherwise.
     *
     * @return whether this activity expects events.
     */
    boolean expectsEvents() {
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
        int result = (int) (CID.getId() ^ (CID.getId() >>> 32));
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
