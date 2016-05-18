package ibis.constellation.impl;

import java.io.Serializable;

public abstract class Message implements Serializable {

    public final ConstellationIdentifier source;
    public ConstellationIdentifier target;

    private transient boolean stale = false;

    protected Message(final ConstellationIdentifier source,
            final ConstellationIdentifier target) {
        this.source = source;
        this.target = target;
    }

    protected Message(final ConstellationIdentifier source) {
        this.source = source;
    }

    public synchronized void setTarget(ConstellationIdentifier cid) {
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

    public ActivityIdentifier targetActivity() {
        return null;
    }

    @Override
    public String toString() {
        String s = "source: ";
        if (source != null) {
            s += source.toString();
        } else
            s += " none";
        s += "; target: ";
        if (target != null) {
            s += target.toString();
        } else
            s += " none";
        return s;
    }
}
