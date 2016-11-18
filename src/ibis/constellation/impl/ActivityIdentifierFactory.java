package ibis.constellation.impl;

import ibis.constellation.ActivityIdentifier;

class ActivityIdentifierFactory {

    private final ConstellationIdentifier cid;
    private final long end;
    private long current;

    ActivityIdentifierFactory(final ConstellationIdentifier cid,
            final long start, final long end) {
        this.cid = cid;
        this.current = start;
        this.end = end;
    }

    ActivityIdentifier createActivityID(boolean events) {

        if (current >= end) {
            throw new Error("Out of identifiers!");
        }

        return ActivityIdentifierImpl.createActivityIdentifier(cid, current++,
                events);
    }
}
