package ibis.constellation;

import ibis.constellation.impl.ActivityIdentifierImpl;

/**
 * An <code>ActivityIdentifier</code> uniquely identifies an {@link Activity} instance. The only valid ActivityIdentifiers are
 * those returned from the constellation system. In other words, it is not possible to create your own activity identifiers.
 *
 * @version 1.0
 * @since 1.0
 */
public final class ActivityIdentifier extends ActivityIdentifierImpl {

    private static final long serialVersionUID = 1469734868148032028L;

    /**
     * Returns a human-readable unique string identifying the {@link Activity} instance to which this ActivityIdentifier refers.
     * This method can be used for debugging prints.
     *
     * @return a string representation of this ActivityIdentifier.
     */
    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Returns a hash code for this activity identifier. Note: two activity identifiers compare equal if they represent the same
     * activity.
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Returns whether the specified object is equal to the current activity identifier. Note: two activity identifiers compare
     * equal if they represent the same activity.
     */
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
