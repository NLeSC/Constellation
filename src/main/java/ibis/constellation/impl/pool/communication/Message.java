package ibis.constellation.impl.pool.communication;

import java.io.Serializable;

public class Message implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public byte opcode;
    public Object contents;

    public Message(byte opcode, Object contents) {
        this.opcode = opcode;
        this.contents = contents;
    }
}
