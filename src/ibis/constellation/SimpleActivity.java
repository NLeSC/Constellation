package ibis.constellation;

import ibis.constellation.context.ActivityContext;

/**
 * A <code>SimpleActivity</code> is an activity that only requires a single
 * invocation of its {@link #simpleActivity()} method. In particular, it has
 * (default) implementations of {@link Activity#initialize()} and
 * {@link Activity#cleanup()}, and it does not expect events. It does provide a
 * concept of a parent activity.
 *
 * TODO: move to a utility package?
 */
public abstract class SimpleActivity extends Activity {

    private static final long serialVersionUID = 1937343247220443457L;

    private ActivityIdentifier parent;

    /**
     * Constructs a <code>SimpleActivity</code> with the specified parameters.
     * This activity can only be executed by a local executor.
     *
     * @param parent
     *            the activity identifier of the parent activity (may be
     *            <code>null</code>).
     * @param context
     *            the context that specifies which executors can actually
     *            execute this activity.
     */
    protected SimpleActivity(ActivityIdentifier parent,
            ActivityContext context) {
        this(parent, context, false);
    }

    /**
     * Constructs a <code>SimpleActivity</code> with the specified parameters.
     *
     * @param parent
     *            the activity identifier of the parent activity (may be
     *            <code>null</code>).
     * @param context
     *            the context that specifies which executors can actually
     *            execute this activity.
     * @param restrictToLocal
     *            when set, specifies that this activity can only be executed by
     *            a local executor.
     */
    protected SimpleActivity(ActivityIdentifier parent, ActivityContext context,
            boolean restictToLocal) {
        super(context, restictToLocal, false);
        setParent(parent);
    }

    /**
     * {@inheritDoc}
     *
     * This version of {@link Activity#initialize()} calls
     * {@link #simpleActivity()} and then {@link Activity#finish()}.
     */
    @Override
    public void initialize() {
        simpleActivity();
        finish();
    }

    @Override
    public final void cleanup() {
        // not used
    }

    @Override
    public final void process(Event e) {
        // not used
    }

    /**
     * This method, to be implemented by the activity, is called once, after
     * which the activity will {@link #finish()}.
     *
     */
    public abstract void simpleActivity();

    /**
     * Returns the activity identifier of the parent (as set by
     * {@link #setParent(ActivityIdentifier)}, so it may be <code>null</code>).
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
    public void setParent(ActivityIdentifier parent) {
        this.parent = parent;
    }
}
