package ibis.constellation.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.Activity;
import ibis.constellation.Constellation;
import ibis.constellation.Event;
import ibis.constellation.context.ActivityContext;
import ibis.constellation.context.UnitActivityContext;

/**
 * A <code>SingleEventCollector</code> is an {@link Activity} that just waits for a single event, and saves it. It provides a
 * method {@link #waitForEvent()}, to be used by other activities, to collect the event and block until it arrives, after which
 * the <code>SingleEventCollector</code> will finish.
 *
 * TODO: move to an utility package?
 */
public class SingleEventCollector extends Activity {

    public static final Logger logger = LoggerFactory.getLogger(SingleEventCollector.class);

    private static final long serialVersionUID = -538414301465754654L;

    private Event event;
    
    /**
     * Constructs a <code>SingleEventCollector</code> with the specified activity context. Note: this is an activity that will
     * receive events (see {@link Activity#Activity(ActivityContext, boolean)}).
     *
     * @param c
     *            the activity context of this event collector
     */
    public SingleEventCollector(ActivityContext c) {
        super(c, false, true);
    }

    /**
     * Constructs a <code>SingleEventCollector</code> with the default activity context. Note: this is an activity that will
     * receive events (see {@link Activity#Activity(ActivityContext, boolean)}).
     */
    public SingleEventCollector() {
        this(UnitActivityContext.DEFAULT);
    }
    
    @Override
    public int initialize(Constellation c) {
        if (logger.isDebugEnabled()) {
            logger.debug("Single event collector " + identifier() + " started.");
        }
        return SUSPEND;
    }

    @Override
    public synchronized int process(Constellation c, Event e) {

        if (logger.isDebugEnabled()) {
            logger.debug("Single event collector " + identifier() + " got result!");
        }

        event = e;
        notifyAll();
        return FINISH;
    }

    @Override
    public void cleanup(Constellation c) {
        // empty
    }

    @Override
    public String toString() {
        return "SingleEventCollector(" + identifier() + ")";
    }

    /**
     * This method blocks waiting for this object to receive an event. As soon as it is available, it returns the event.
     *
     * @return the received event.
     */
    public synchronized Event waitForEvent() {
        while (event == null) {
            try {
                wait();
            } catch (Exception e) {
                // ignore
            }
        }

        return event;
    }
}
