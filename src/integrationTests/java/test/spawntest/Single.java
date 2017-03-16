package test.spawntest;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.Context;
import ibis.constellation.Event;
import ibis.constellation.NoSuitableExecutorException;

public class Single extends Activity {

    private static final long serialVersionUID = 5970093414747228592L;

    private final ActivityIdentifier parent;

    private final int spawns;
    private int replies;

    public Single(ActivityIdentifier parent, int spawns) {
        super(new Context("TEST", 2, 2), false, true);
        this.parent = parent;
        this.spawns = spawns;
    }

    @Override
    public int initialize(Constellation c) {
        for (int i = 0; i < spawns; i++) {
            try {
                c.submit(new Dummy(identifier()));
            } catch (NoSuitableExecutorException e) {
                System.err.println("Should not happen: " + e);
                e.printStackTrace(System.err);
            }
        }

        return SUSPEND;
    }

    @Override
    public int process(Constellation c, Event e) {

        replies++;

        if (replies == spawns) {
            c.send(new Event(identifier(), parent, null));
            return FINISH;
        } else {
            return SUSPEND;
        }
    }

    @Override
    public void cleanup(Constellation c) {
        // unused
    }
}
