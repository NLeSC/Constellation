package ibis.constellation;

import java.util.ArrayList;

import ibis.constellation.context.UnitActivityContext;

/**
 * A <code>FlexibleEventCollector</code> is an {@link Activity} that just waits
 * for events, indefinitely, and collects them. It provides a method
 * {@link #waitForEvents()}, to be used by other activities, to collect the
 * events collected so far.
 *
 * TODO: move to a utility package?
 */
public class FlexibleEventCollector extends Activity {

    private static final long serialVersionUID = -538414301465754654L;

    private final ArrayList<Event> events = new ArrayList<Event>();
    private boolean waiting = false;

    /**
     * Constructs a <code>FlexibleEventCollector</code> with the specified
     * activity context. Note: this is an activity that will receive events (see
     * {@link Activity#Activity(ActivityContext, boolean)}).
     *
     * @param c
     *            the activity context
     */
    public FlexibleEventCollector(ActivityContext c) {
        super(c, true);
    }

    /**
     * Constructs a <code>FlexibleEventCollector</code> with the default
     * activity context.
     */
    public FlexibleEventCollector() {
        super(UnitActivityContext.DEFAULT, true);
    }

    @Override
    public void initialize() {
        suspend();
    }

    @Override
    public synchronized void process(Event e) {

        events.add(e);

        if (waiting) {
            notifyAll();
        }

        suspend();
    }

    @Override
    public void cleanup() {
        // empty
    }

    @Override
    public String toString() {
        return "FlexibleEventCollector(" + identifier() + ")";
    }

    /**
     * This method blocks waiting for events. As soon as one or more are
     * available, it creates an array containing these events, clears the event
     * list of this collector, and returns the array.
     *
     * @return an array containing the received events.
     */
    public synchronized Event[] waitForEvents() {

        while (events.size() == 0) {

            waiting = true;

            try {
                wait();
            } catch (Throwable e) {
                // ignore
            }

            waiting = false;
        }

        Event[] result = events.toArray(new Event[events.size()]);

        events.clear();

        return result;
    }
}
