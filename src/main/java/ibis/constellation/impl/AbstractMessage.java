package ibis.constellation.impl;

import java.io.Serializable;

public abstract class AbstractMessage implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public final ConstellationIdentifierImpl source;
    public ConstellationIdentifierImpl target;

    private transient boolean stale = false;

    protected AbstractMessage(final ConstellationIdentifierImpl source, final ConstellationIdentifierImpl target) {
        this.source = source;
        this.target = target;
    }

    protected AbstractMessage(final ConstellationIdentifierImpl source) {
        checkNull(source, "source may not be null");
        this.source = source;
    }

    protected void checkNull(Object o, String message) {
        if (o == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public synchronized void setTarget(ConstellationIdentifierImpl cid) {
        checkNull(cid, "source may not be null");
        this.target = cid;
    }

    public synchronized boolean getStale() {
        return stale;
    }

    public synchronized boolean atomicSetStale() {
        boolean old = stale;
        stale = true;
        return old;
    }

    public ActivityIdentifierImpl targetActivity() {
        return null;
    }

    @Override
    public String toString() {
        String s = "source: ";
        if (source != null) {
            s += source.toString();
        } else {
            s += "none";
        }
        s += "; target: ";
        if (target != null) {
            s += target.toString();
        } else {
            s += "none";
        }
        return s;
    }
}
