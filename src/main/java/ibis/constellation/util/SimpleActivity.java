package ibis.constellation.util;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.Event;
import ibis.constellation.context.ActivityContext;

/**
 * A <code>SimpleActivity</code> is an activity that only requires a single invocation of its {@link #simpleActivity()} method. In
 * particular, it has (default) implementations of {@link Activity#initialize()} and {@link Activity#cleanup()}, and it does not
 * expect events. It does provide a concept of a parent activity.
 *
 * TODO: move to a utility package?
 */
public abstract class SimpleActivity extends Activity {

    private static final long serialVersionUID = 1937343247220443457L;

    private ActivityIdentifier parent;

    /**
     * Constructs a <code>SimpleActivity</code> with the specified parameters. This activity can only be executed by a local
     * executor.
     *
     * @param parent
     *            the activity identifier of the parent activity (may be <code>null</code>).
     * @param context
     *            the context that specifies which executors can actually execute this activity.
     */
    protected SimpleActivity(ActivityIdentifier parent, ActivityContext context) {
        this(parent, context, true);
    }

    /**
     * Constructs a <code>SimpleActivity</code> with the specified parameters.
     *
     * @param parent
     *            the activity identifier of the parent activity (may be <code>null</code>).
     * @param context
     *            the context that specifies which executors can actually execute this activity.
     * @param restrictToLocal
     *            when set, specifies that this activity can only be executed by a local executor.
     */
    protected SimpleActivity(ActivityIdentifier parent, ActivityContext context, boolean mayBeStolen) {
        super(context, mayBeStolen, false);
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     *
     * This version of {@link Activity#initialize()} calls {@link #simpleActivity()} and then {@link Activity#finish()}.
     */
    @Override
    public int initialize(Constellation c) {
        simpleActivity(c);
        return FINISH;
    }

    @Override
    public final int process(Constellation c, Event e) {
        // not used
        return FINISH;
    }

    @Override
    public final void cleanup(Constellation c) {
        // not used
    }

    /**
     * This method, to be implemented by the activity, is called once, after which the activity will {@link #finish()}.
     *
     */
    public abstract void simpleActivity(Constellation c);

    /**
     * Returns the activity identifier of the parent (as set by {@link #setParent(ActivityIdentifier)}, so it may be
     * <code>null</code>).
     *
     * @return the parent activity identifier
     */
    public ActivityIdentifier getParent() {
        return parent;
    }

    /**
     * Sets the parent activity identifier to the specified parent.
     *
     * @param parent
     *            the parent activity identifier.
     */
//    public void setParent(ActivityIdentifier parent) {
//        this.parent = parent;
//    }
}