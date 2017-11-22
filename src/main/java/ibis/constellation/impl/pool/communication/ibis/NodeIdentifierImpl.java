package ibis.constellation.impl.pool.communication.ibis;

import ibis.constellation.impl.pool.communication.NodeIdentifier;
import ibis.ipl.IbisIdentifier;

public class NodeIdentifierImpl implements NodeIdentifier {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final IbisIdentifier id;

    public NodeIdentifierImpl(IbisIdentifier id) {
        this.id = id;
    }

    @Override
    public String name() {
        return id.name();
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NodeIdentifierImpl)) {
            return false;
        }
        NodeIdentifierImpl other = (NodeIdentifierImpl) o;
        return id.equals(other.id);
    }

    IbisIdentifier getIbisIdentifier() {
        return id;
    }

}
