package ibis.constellation.impl;

import ibis.constellation.ConstellationIdentifier;

public class CombinedMessage extends Message {

    private static final long serialVersionUID = 6623230001381566142L;

    private Message[] messages;

    public CombinedMessage(ConstellationIdentifier source,
            ConstellationIdentifier target, Message[] messages) {
        super(source, target);
        this.messages = messages;
    }

    public Message[] getMessages() {
        return messages;
    }
}
