package ibis.constellation;

import java.io.Serializable;

public class ExecutorIdentifier implements Serializable {

    private static final long serialVersionUID = -8236873210293335756L;

    public final long id;

    public ExecutorIdentifier(final long id) {
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

        final ExecutorIdentifier other = (ExecutorIdentifier) obj;

        return (id == other.id);
    }

    public String toString() {
        return "EID: " + Integer.toHexString((int) (id >> 32) & 0xffffffff)
                + " " + Integer.toHexString((int) (id & 0xffffffff));
    }
}
