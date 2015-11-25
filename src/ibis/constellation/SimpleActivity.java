package ibis.constellation;

public abstract class SimpleActivity extends Activity {

    private static final long serialVersionUID = 1937343247220443457L;

    protected ActivityIdentifier parent;

    protected SimpleActivity(ActivityIdentifier parent,
            ActivityContext context) {
        this(parent, context, false);
    }

    protected SimpleActivity(ActivityIdentifier parent, ActivityContext context,
            boolean restictToLocal) {
        super(context, restictToLocal, false);
        this.parent = parent;
    }

    @Override
    public void initialize() throws Exception {
        simpleActivity();
        finish();
    }

    @Override
    public final void cancel() throws Exception {
        // not used
    }

    @Override
    public final void cleanup() throws Exception {
        // not used
    }

    @Override
    public final void process(Event e) throws Exception {
        // not used
    }

    public abstract void simpleActivity() throws Exception;

    public ActivityIdentifier getParent() {
        return parent;
    }

    public void setParent(ActivityIdentifier parent) {
        this.parent = parent;
    }
}
