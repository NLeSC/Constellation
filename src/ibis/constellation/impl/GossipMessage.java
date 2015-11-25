package ibis.constellation.impl;

import ibis.constellation.ConstellationIdentifier;

public class GossipMessage extends Message {

    private static final long serialVersionUID = -2735555540658194683L;

    private Gossip[] gossip;

    public GossipMessage(ConstellationIdentifier source,
            ConstellationIdentifier target, Gossip[] gossip) {
        super(source, target);
        this.gossip = gossip;
    }

    public Gossip[] getGossip() {
        return gossip;
    }
}
