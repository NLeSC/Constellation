package ibis.constellation;

import java.io.Serializable;

public class ActivityIdentifier implements Serializable {

    private static final long serialVersionUID = 4785081436543353644L;

    // public static final long UNKNOWN = Long.MAX_VALUE;

    // The globally unique UUID for this activity is "EID:AID"
    // "EID" is the id of the executor on which this actvity was created,
    // "AID" is the sequence number of this activity on that executor.
    public final long EID;
    public final long AID;
    public final boolean expectsEvents;

    // The last known location for the activity. If the location is not known,
    // 'UNKNOWN' is used.

    // private long lastKnownEID;

    public ActivityIdentifier(long high, long low, boolean expectsEvents) {
        this.EID = high;
        this.AID = low;
        this.expectsEvents = expectsEvents;
    }

    /*
     * public ConstellationIdentifier getLastKnownLocation() { if (lastKnownEID
     * == UNKNOWN) { return null; }
     * 
     * return new ConstellationIdentifier(lastKnownEID); }
     * 
     * public void setLastKnownLocation(ConstellationIdentifier cid) {
     * lastKnownEID = cid.id; }
     * 
     * public void clearLastKnownLocation() { lastKnownEID = UNKNOWN; }
     */

    public boolean expectsEvents() {
        return expectsEvents;
    }

    public ConstellationIdentifier getOrigin() {
        return new ConstellationIdentifier(EID);
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (int) (EID ^ (EID >>> 32));
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

        return (EID == other.EID && AID == other.AID);
    }

    public String toString() {
        return "AID: " + Integer.toHexString((int) (EID >> 32) & 0xffffffff)
                + ":" + Integer.toHexString((int) (EID & 0xffffffff)) + ":"
                + Long.toHexString(AID); /*
                                          * + " (" + Integer.toHexString((int)(
                                          * lastKnownEID >> 32) & 0xffffffff) +
                                          * " " + Integer.toHexString((int)(
                                          * lastKnownEID & 0xffffffff)) + ")";
                                          */
    }
}
