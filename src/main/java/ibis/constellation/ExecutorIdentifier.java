package ibis.constellation;

/**
 * The sole purpose of a <code>ExecutionIdentifier</code> is to identify a constellation instance. The only valid
 * ExecutionIdentifiers are those returned by the constellation system.
 */
public interface ExecutorIdentifier {
    @Override
    public String toString();
}
