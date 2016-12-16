package ibis.constellation.impl;

import ibis.constellation.ActivityIdentifier;

public class IdChecker {

    private IdChecker() {
        // empty, avoid construction.
    }

    public static void checkActivityIdentifier(ActivityIdentifier id, String s) {
        if (id == null) {
            throw new IllegalArgumentException(s + " is null");
        }
        if (!(id instanceof ActivityIdentifierImpl)) {
            throw new IllegalArgumentException(s + " does not come from constellation");
        }
    }
}
