package ibis.constellation;

import java.io.Serializable;

public abstract class Activity implements Serializable {

    private static final long serialVersionUID = -83331265534440970L;

    private static final byte REQUEST_UNKNOWN = 0;
    private static final byte REQUEST_SUSPEND = 1;
    private static final byte REQUEST_FINISH = 2;

    protected transient Executor executor;

    private ActivityIdentifier identifier;
    private final ActivityContext context;

    private final boolean restrictToLocal;
    private final boolean willReceiveEvents;

    private byte next = REQUEST_UNKNOWN;

    protected Activity(ActivityContext context, boolean restrictToLocal,
            boolean willReceiveEvents) {
        this.context = context;
        this.restrictToLocal = restrictToLocal;
        this.willReceiveEvents = willReceiveEvents;
    }

    protected Activity(ActivityContext context, boolean willReceiveEvents) {
        this(context, false, willReceiveEvents);
    }

    public boolean expectsEvents() {
        return willReceiveEvents;
    }

    public void initialize(ActivityIdentifier id) {
        this.identifier = id;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public ActivityIdentifier identifier() {

        if (identifier == null) {
            throw new IllegalStateException("Activity is not initialized yet");
        }

        return identifier;
    }

    public Executor getExecutor() {

        if (executor == null) {
            throw new IllegalStateException("Activity is not initialized yet");
        }

        return executor;
    }

    public ActivityContext getContext() {
        return context;
    }

    public boolean isRestrictedToLocal() {
        return restrictToLocal;
    }

    public void reset() {
        next = REQUEST_UNKNOWN;
    }

    public boolean mustSuspend() {
        return (next == REQUEST_SUSPEND);
    }

    public boolean mustFinish() {
        return (next == REQUEST_FINISH);
    }

    public void suspend() {

        if (next == REQUEST_FINISH) {
            throw new IllegalStateException(
                    "Activity already requested to finish!");
        }

        next = REQUEST_SUSPEND;
    }

    public void finish() {

        if (next == REQUEST_SUSPEND) {
            throw new IllegalStateException(
                    "Activity already requested to suspend!");
        }

        next = REQUEST_FINISH;
    }

    public abstract void initialize() throws Exception;

    public abstract void process(Event e) throws Exception;

    public abstract void cleanup() throws Exception;

    public abstract void cancel() throws Exception;

    public String toString() {
        return identifier + " " + context;
    }
}
