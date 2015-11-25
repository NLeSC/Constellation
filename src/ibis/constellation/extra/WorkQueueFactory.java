package ibis.constellation.extra;

public class WorkQueueFactory {

    public static WorkQueue createQueue(String type, boolean sync, String id)
            throws Exception {

        if (type == null) {
            type = "smartsorted";
        }

        WorkQueue result = null;

        if (type.equals("smartsorted")) {
            result = new SmartSortedWorkQueue(id);
        } else {
            throw new Exception("Unknown workqueue type: " + type);
        }

        if (sync) {
            result = new SynchronizedWorkQueue(result);
        }

        return result;
    }
}
