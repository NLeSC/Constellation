package ibis.constellation.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.Activity;
import ibis.constellation.Constellation;
import ibis.constellation.Event;
import ibis.constellation.context.ActivityContext;
import ibis.constellation.context.UnitActivityContext;

/**
 * A <code>MultiEventCollector</code> is an {@link Activity} that just waits for a specific number of events, and collects them.
 * It provides a method {@link #waitForEvents()}, to be used by other activities, to collect the events and block until the
 * specified number of events is reached, after which the <code>MultiEventCollector</code> will finish.
 *
 * TODO: move to a utility package?
 */
public class MultiEventCollector extends Activity {

    public static final Logger logger = LoggerFactory.getLogger(MultiEventCollector.class);

    private static final long serialVersionUID = -538414301465754654L;

    private final Event[] events;
    private int count;
    
    /**
     * Constructs a <code>MultiEventCollector</code> with the specified activity context and event count. Note: this is an
     * activity that will receive events (see {@link Activity#Activity(ActivityContext, boolean)}).
     *
     * @param c
     *            the activity context of this event collector
     * @param events
     *            the number of events to be collected
     */
    public MultiEventCollector(ActivityContext c, int events) {
        super(c, false, true);
        this.events = new Event[events];
    }

    /**
     * Constructs a <code>MultiEventCollector</code> with the default activity context and specified event count. Note: this is an
     * activity that will receive events (see {@link Activity#Activity(ActivityContext, boolean)}).
     *
     * @param events
     *            the number of events to be collected
     */
    public MultiEventCollector(int events) {
        this(UnitActivityContext.DEFAULT, events);
    }
    
    @Override
    public int initialize(Constellation c) {
        return SUSPEND;
    }
    
    @Override
    public synchronized int process(Constellation c, Event e) {

        if (logger.isDebugEnabled()) {
            logger.debug("MultiEventCollector: received event " + count + " of " + events.length);
        }

        events[count++] = e;

        if (count == events.length) {
            notifyAll();
            return FINISH;
        } else {
            return SUSPEND;
        }
    }

    @Override
    public void cleanup(Constellation c) {
        // empty
    }

    @Override
    public String toString() {
        return "MultiEventCollector(" + identifier() + ", " + events.length + ")";
    }

    /**
     * This method blocks waiting for the specified number of events. As soon as they are available, it creates an array
     * containing these events, and returns the array.
     *
     * @return an array containing the received events.
     */
    public synchronized Event[] waitForEvents() {
        while (count != events.length) {
            try {
                wait();
            } catch (Exception e) {
                // ignore
            }
        }

        return events;
    }
}
