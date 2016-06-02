package ibis.constellation.impl;

import java.io.Serializable;

public final class ConstellationIdentifier implements Serializable {

    private static final long serialVersionUID = -8236873210293335756L;

    private final long id;

    public long getId() {
        return id;
    }

    public ConstellationIdentifier(final long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        final ConstellationIdentifier other = (ConstellationIdentifier) obj;

        return (id == other.id);
    }

    @Override
    public String toString() {
        return "CID: " + Integer.toHexString((int) (id >> 32) & 0xffffffff)
                + ":" + Integer.toHexString((int) (id & 0xffffffff));
    }
}
