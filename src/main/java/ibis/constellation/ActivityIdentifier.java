package ibis.constellation;

/**
 * An <code>ActivityIdentifier</code> uniquely identifies an {@link Activity} instance. The only valid ActivityIdentifiers are
 * those returned from the constellation system. In other words, it is not possible to create your own activity identifiers.
 *
 * @version 1.0
 * @since 1.0
 */
public interface ActivityIdentifier {

    @Override
    public String toString();
}
