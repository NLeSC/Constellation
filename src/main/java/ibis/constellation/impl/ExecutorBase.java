package ibis.constellation.impl;

import java.io.Serializable;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Event;

/**
 * Base class of an executor, hiding some implementation details.
 */
public abstract class ExecutorBase implements Serializable {

    private static final long serialVersionUID = 1L;

    private ExecutorWrapper owner = null;

    /**
     * Indicates to the executor that it can start running activities. This is usually repetitively called from the {@link #run()}
     * method, which can return when processActivities() returns <code>true</code>.
     *
     * @return whether the {@link #run()} method should return.
     */
    protected boolean processActivities() {
        if (owner == null) {
            throw new Error("processActivities() called but this executor is not embedded in a constellation instance yet");
        }
        return owner.processActitivies();
    }

    /**
     * Submits an activity to the current executor.
     *
     * @param job
     *            the activity to submit
     * @return the activity identifier identifying this activity within constellation
     */
    public ActivityIdentifier submit(Activity job) {
        if (owner == null) {
            throw new Error("submit() called but this executor is not embedded in a constellation instance yet");
        }
        return owner.submit(job);
    }

    /**
     * Returns the constellation identifier of the current constellation instance.
     *
     * @return the constellation identifier
     */
    public String identifier() {
        if (owner == null) {
            throw new Error("identifier() called but this executor is not embedded in a constellation instance yet");
        }
        return owner.identifier().toString();
    }

    /**
     * Sends the specified event. The destination activity is encoded in the event.
     *
     * @param e
     *            the event to send
     */
    public void send(Event e) {
        if (owner == null) {
            throw new Error("send() called but this executor is not embedded in a constellation instance yet");
        }
        owner.send(e);
    }

    synchronized void connect(ExecutorWrapper owner) {

        if (this.owner != null) {
            throw new Error("Executor already connected!");
        }

        this.owner = owner;
    }

    /**
     * This is the main method of this executor. Usually, it repeatedly calls {@link #processActivities()} until it returns
     * <code>true</code>.
     */
    public abstract void run();
}
