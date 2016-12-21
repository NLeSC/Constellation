package test.spawntest;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
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
    public int initialize(Constellation c) {
        start = System.currentTimeMillis();
        
        for (int i = 0; i < concurrent; i++) {
            pending++;
            c.submit(new Single(identifier(), spawns));
        }

        return SUSPEND;
    }

    @Override
    public int process(Constellation c, Event e) {

        done++;

        if (done == count) {
            end = System.currentTimeMillis();
            return FINISH;
        }

        if (pending < count) {
            pending++;
            c.submit(new Single(identifier(), spawns));
        }

        return SUSPEND;
    }

    @Override
    public void cleanup(Constellation c) {

        double timeSatin = (end - start) / 1000.0;
        double cost = ((end - start) * 1000.0) / (spawns * count);

        System.out.println("spawn = " + timeSatin + " s, time/spawn = " + cost + " us/spawn");

        c.send(new Event(identifier(), parent, null));
    }
}
