package ibis.constellation.impl.pool.communication;

import java.io.Serializable;

public class CommunicationMessage implements Serializable {
    public byte opcode;
    public Object contents;

    public CommunicationMessage(byte opcode, Object contents) {
        this.opcode = opcode;
        this.contents = contents;
    }
}
