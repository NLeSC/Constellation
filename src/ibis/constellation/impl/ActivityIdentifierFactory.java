package ibis.constellation.impl;

public class ActivityIdentifierFactory {

    private final ConstellationIdentifier cid;
    private final long end;
    private long current;

    public ActivityIdentifierFactory(final ConstellationIdentifier cid,
            final long start, final long end) {
        this.cid = cid;
        this.current = start;
        this.end = end;
    }

    public ActivityIdentifier createActivityID(boolean events) {

        if (current >= end) {
            throw new Error("Out of identifiers!");
        }

        return new ActivityIdentifier(cid, current++, events);
    }
}
