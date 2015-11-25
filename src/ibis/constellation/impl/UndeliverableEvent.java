package ibis.constellation.impl;

import ibis.constellation.ConstellationIdentifier;
import ibis.constellation.Event;

public class UndeliverableEvent extends Message {

    private static final long serialVersionUID = 2006268088130257399L;

    public final Event event;

    public UndeliverableEvent(final ConstellationIdentifier source,
            final ConstellationIdentifier target, final Event event) {

        super(source, target);
        this.event = event;
    }

}
