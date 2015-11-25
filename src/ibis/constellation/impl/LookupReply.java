package ibis.constellation.impl;

import ibis.constellation.ActivityIdentifier;
import ibis.constellation.ConstellationIdentifier;

public class LookupReply extends Message {

    private static final long serialVersionUID = 2655647847327367590L;

    public final ActivityIdentifier missing;
    public final ConstellationIdentifier location;
    public final long count;

    public LookupReply(final ConstellationIdentifier source,
            final ConstellationIdentifier target,
            final ActivityIdentifier missing,
            final ConstellationIdentifier location, final long count) {

        super(source, target);
        this.missing = missing;
        this.location = location;
        this.count = count;
    }
}
