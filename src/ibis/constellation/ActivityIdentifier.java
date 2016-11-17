package ibis.constellation;

import java.io.Serializable;

/**
 * An <code>ActivityIdentifier</code> uniquely identifies an {@link Activity}
 * instance. The only valid ActivityIdentifiers are those returned from the
 * constellation system. In other words, it is not possible to create your own
 * activity identifiers.
 *
 * TODO: make this impossible in the API.
 *
 * @version 1.0
 * @since 1.0
 */
public interface ActivityIdentifier extends Serializable {

    /**
     * Returns a human-readable unique string identifying the {@link Activity}
     * instance to which this ActivityIdentifier refers. This method can be used
     * for debugging prints.
     *
     * @return a string representation of this ActivityIdentifier.
     */
    public String toString();
}
