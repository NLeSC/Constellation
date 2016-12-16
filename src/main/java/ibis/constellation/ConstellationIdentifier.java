package ibis.constellation;

/**
 * The sole purpose of a <code>ConstellationIdentifier</code> is to identify a constellation instance. The only valid
 * ConstellationIdentifiers are those returned by the constellation system.
 */
public interface ConstellationIdentifier {
    @Override
    public String toString();
}
