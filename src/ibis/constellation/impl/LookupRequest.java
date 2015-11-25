package ibis.constellation.impl;

import ibis.constellation.ActivityIdentifier;
import ibis.constellation.ConstellationIdentifier;

public class LookupRequest extends Message {

    private static final long serialVersionUID = 2655647847327367590L;

    public final ActivityIdentifier missing;

    public LookupRequest(final ConstellationIdentifier source,
            final ActivityIdentifier missing) {
        super(source);
        this.missing = missing;
    }

    @Override
    public boolean requiresLookup() {
        return true;
    }

    @Override
    public ActivityIdentifier targetActivity() {
        return missing;
    }

}
