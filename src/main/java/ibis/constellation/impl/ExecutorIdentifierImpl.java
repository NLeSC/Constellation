package ibis.constellation.impl;

import java.io.Serializable;

import ibis.constellation.ExecutorIdentifier;

public class ExecutorIdentifierImpl implements ExecutorIdentifier, Serializable {

    private static final long serialVersionUID = -8236873210293335756L;

    private final int nodeId;
    private final int localId;

    /**
     * Constructs an Executor identifier using the specified node identification and local identification numbers.
     *
     * @param nodeId
     *            the node identification number
     * @param localId
     *            the local identification number
     */
    public ExecutorIdentifierImpl(final int nodeId, int localId) {
        this.nodeId = nodeId;
        this.localId = localId;
    }

    /**
     * Returns the nodeId of this executor identifier.
     *
     * @return the nodeId.
     */
    public int getNodeId() {
        return nodeId;
    }

    /**
     * Returns the local identification of this executor identifier.
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

        final ExecutorIdentifierImpl other = (ExecutorIdentifierImpl) obj;

        return (nodeId == other.nodeId && localId == other.localId);
    }

    @Override
    public String toString() {
        return "EID:" + Integer.toHexString(nodeId) + ":" + Integer.toHexString(localId);
    }
}
