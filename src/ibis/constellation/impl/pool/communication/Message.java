package ibis.constellation.impl.pool.communication;

import java.io.Serializable;

public class Message implements Serializable {
    public byte opcode;
    public Object contents;

    public Message(byte opcode, Object contents) {
        this.opcode = opcode;
        this.contents = contents;
    }
}
