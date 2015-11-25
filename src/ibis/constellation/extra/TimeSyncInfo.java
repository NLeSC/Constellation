package ibis.constellation.extra;

import java.util.HashMap;

public class TimeSyncInfo extends HashMap<String, Long>
        implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String thisNode;

    public TimeSyncInfo(String id) {
        super();
        put(id, new Long(0));
    }

    public long getOffsetToMaster(String node) {
        Long tsm = get(node);
        return tsm.longValue();
    }
}
