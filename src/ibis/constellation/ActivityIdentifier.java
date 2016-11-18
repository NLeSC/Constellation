package ibis.constellation;

import ibis.constellation.impl.ActivityIdentifierImpl;

/**
 * An <code>ActivityIdentifier</code> uniquely identifies an {@link Activity}
 * instance. The only valid ActivityIdentifiers are those returned from the
 * constellation system. In other words, it is not possible to create your own
 * activity identifiers.
 * 
 * @version 1.0
 * @since 1.0
 */
public final class ActivityIdentifier extends ActivityIdentifierImpl {

    /**
     * Returns a human-readable unique string identifying the {@link Activity}
     * instance to which this ActivityIdentifier refers. This method can be used
     * for debugging prints.
     *
     * @return a string representation of this ActivityIdentifier.
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
