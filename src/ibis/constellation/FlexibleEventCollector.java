package ibis.constellation;

import ibis.constellation.context.UnitActivityContext;

import java.util.ArrayList;

public class FlexibleEventCollector extends Activity {

    private static final long serialVersionUID = -538414301465754654L;

    private final ArrayList<Event> events = new ArrayList<Event>();
    private boolean waiting = false;
    private int count;

    public FlexibleEventCollector(ActivityContext c) {
        super(c, true);
    }

    public FlexibleEventCollector() {
        super(UnitActivityContext.DEFAULT, true);
    }

    @Override
    public void initialize() throws Exception {
        suspend();
    }

    @Override
    public synchronized void process(Event e) throws Exception {

        events.add(e);
        count++;

        if (waiting) {
            notifyAll();
        }

        suspend();
    }

    public void cleanup() throws Exception {
        // empty
    }

    public void cancel() throws Exception {
        // empty
    }

    public String toString() {
        return "FlexibleEventCollector(" + identifier() + ")";
    }

    public synchronized Event[] waitForEvents() {

        while (events.size() == 0) {

            waiting = true;

            try {
                wait();
            } catch (Exception e) {
                // ignore
            }

            waiting = false;
        }

        Event[] result = events.toArray(new Event[events.size()]);

        events.clear();

        return result;
    }
}
