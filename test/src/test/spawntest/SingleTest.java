package test.spawntest;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Event;
import ibis.constellation.context.UnitActivityContext;

public class SingleTest extends Activity {

    private static final long serialVersionUID = 5970093414747228592L;

    private final ActivityIdentifier parent;

    private final int spawns;
    private int replies;

    public SingleTest(ActivityIdentifier parent, int spawns) {
        super(new UnitActivityContext("TEST", 2), true);
        this.parent = parent;
        this.spawns = spawns;
    }

    @Override
    public void initialize() {
        for (int i = 0; i < spawns; i++) {
            submit(new Dummy(identifier()));
        }

        suspend();
    }

    @Override
    public void process(Event e) {

        replies++;

        if (replies == spawns) {
            send(new Event(identifier(), parent, null));
            finish();
        } else {
            suspend();
        }
    }

    @Override
    public void cleanup() {
        // unused
    }
}
