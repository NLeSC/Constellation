package ibis.constellation.impl.pool.communication;

public class Message {
    public byte opcode;
    public Object contents;
    public NodeIdentifier node; // destination or origin

    public Message(byte opcode, Object contents, NodeIdentifier node) {
        this.opcode = opcode;
        this.contents = contents;
        this.node = node;
    }
}
