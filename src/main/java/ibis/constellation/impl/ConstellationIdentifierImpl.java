package ibis.constellation.impl;

import java.io.Serializable;

import ibis.constellation.ConstellationIdentifier;

/**
 * The sole purpose of a <code>ConstellationIdentifierImpl</code> is to identify a constellation instance.
 */
public final class ConstellationIdentifierImpl implements Serializable, ConstellationIdentifier {

    private static final long serialVersionUID = -8236873210293335756L;

    private final int nodeId;
    private final int localId;

    /**
     * Constructs a constellation identifier using the specified node identification and local identification numbers.
     *
     * @param nodeId
     *            the node identification number
     * @param localId
     *            the local identification number
     */
    public ConstellationIdentifierImpl(final int nodeId, int localId) {
        this.nodeId = nodeId;
        this.localId = localId;
    }

    /**
     * Returns the nodeId of this constellation identifier.
     *
     * @return the nodeId.
     */
    public int getNodeId() {
        return nodeId;
    }

    /**
     * Returns the local identification of this constellation identifier.
     *
     * @return the local identification.
     */
    public int getLocalId() {
        return localId;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (localId ^ nodeId);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final ConstellationIdentifierImpl other = (ConstellationIdentifierImpl) obj;

        return (nodeId == other.nodeId && localId == other.localId);
    }

    @Override
    public String toString() {
        return "CID:" + Integer.toHexString(nodeId) + ":" + Integer.toHexString(localId);
    }
}
