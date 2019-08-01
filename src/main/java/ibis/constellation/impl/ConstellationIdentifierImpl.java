/*
 * Copyright 2019 Vrije Universiteit Amsterdam
 *                Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        return (PRIME * localId) ^ nodeId;
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
