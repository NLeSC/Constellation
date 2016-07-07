package test.pipeline.inbalance;

import java.io.Serializable;

public class Data implements Serializable {

    private static final long serialVersionUID = -1823086406536340980L;

    public final int index;
    public final int stage;
    public final byte[] data;

    public Data(int index, int stage, byte[] data) {
        this.index = index;
        this.stage = stage;
        this.data = data;
    }
}
