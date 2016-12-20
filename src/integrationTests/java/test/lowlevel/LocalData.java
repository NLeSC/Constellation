package test.lowlevel;

import java.util.HashMap;

public class LocalData {

    private static LocalData local = new LocalData();

    public static LocalData getLocalData() {
        return local;
    }

    private HashMap<Object, Object> data = new HashMap<Object, Object>();

    public Object get(Object key) {
        return data.get(key);
    }

    public Object put(Object key, Object value) {
        return data.put(key, value);
    }

    public Object remove(Object key) {
        return data.remove(key);
    }

}
