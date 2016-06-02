package ibis.constellation;

import java.io.Serializable;

/**
 * An <code>ActivityIdentifier</code> uniquely identifies an {@link Activity}
 * instance.
 *
 * @version 1.0
 * @since 1.0
 */
public abstract class ActivityIdentifier implements Serializable {

    /**
     * Returns <code>true</code> if this activity expects events,
     * <code>false</code> otherwise.
     *
     * @return whether this activity expects events.
     */
    public abstract boolean expectsEvents();
}
