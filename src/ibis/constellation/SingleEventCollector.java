package ibis.constellation;

import ibis.constellation.context.UnitActivityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleEventCollector extends Activity {

    public static final Logger logger = LoggerFactory
            .getLogger(SingleEventCollector.class);

    private static final long serialVersionUID = -538414301465754654L;

    private Event event;
    private final boolean verbose;

    public SingleEventCollector(ActivityContext c, boolean verbose) {
        super(c, true, true);
        this.verbose = verbose;
    }

    public SingleEventCollector(ActivityContext c) {
        this(c, false);
    }

    public SingleEventCollector() {
        this(UnitActivityContext.DEFAULT, false);
    }

    @Override
    public void initialize() throws Exception {
        suspend();
    }

    @Override
    public synchronized void process(Event e) throws Exception {

        if (verbose) {
            logger.info("SINGLE EVENT COLLECTOR ( " + identifier()
                    + ") GOT RESULT!");
        }

        event = e;
        notifyAll();
        finish();
    }

    @Override
    public void cleanup() throws Exception {
        // empty
    }

    @Override
    public void cancel() throws Exception {
        // empty
    }

    @Override
    public String toString() {
        return "SingleEventCollector(" + identifier() + ")";
    }

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
