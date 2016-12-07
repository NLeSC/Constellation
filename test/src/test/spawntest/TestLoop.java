package test.spawntest;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Event;
import ibis.constellation.context.UnitActivityContext;

public class TestLoop extends Activity {

    private static final long serialVersionUID = 5970093414747228592L;

    private final ActivityIdentifier parent;

    private final long count;
    private final int concurrent;
    private final int spawns;

    private int pending;
    private int done;

    private long start;
    private long end;

    public TestLoop(ActivityIdentifier parent, long count, int concurrent, int spawns) {
        super(new UnitActivityContext("TEST", 3), true, true);
        this.parent = parent;
        this.count = count;
        this.concurrent = concurrent;
        this.spawns = spawns;
    }

    @Override
    public void initialize() {
        start = System.currentTimeMillis();

        for (int i = 0; i < concurrent; i++) {
            pending++;
            submit(new SingleTest(identifier(), spawns));
        }

        suspend();
    }

    @Override
    public void process(Event e) {

        done++;

        if (done == count) {
            end = System.currentTimeMillis();
            finish();
            return;
        }

        if (pending < count) {
            pending++;
            submit(new SingleTest(identifier(), spawns));
        }

        suspend();
    }

    @Override
    public void cleanup() {

        double timeSatin = (end - start) / 1000.0;
        double cost = ((end - start) * 1000.0) / (spawns * count);

        System.out.println("spawn = " + timeSatin + " s, time/spawn = " + cost + " us/spawn");

        send(new Event(identifier(), parent, null));
    }
}
