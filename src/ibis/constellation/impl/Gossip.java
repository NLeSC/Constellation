package ibis.constellation.impl;

import ibis.constellation.ConstellationIdentifier;
import ibis.constellation.ExecutorContext;

import java.io.Serializable;

public class Gossip implements Serializable {

    private static final long serialVersionUID = 2068820337089838573L;

    public final ConstellationIdentifier id;
    public final ExecutorContext context;
    public final long timestamp;

    public Gossip(final ConstellationIdentifier id, final ExecutorContext context,
            final long timestamp) {

        super();
        this.id = id;
        this.context = context;
        this.timestamp = timestamp;
    }
}
