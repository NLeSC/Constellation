package test.lowlevel;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.Event;
import ibis.constellation.Context;

public class ForkJoin extends Activity {

    /*
     * This is the Constellation equivalent of the SpawnOverhead test in Satin
     */
    private static final long serialVersionUID = 3379531054395374984L;

    private final ActivityIdentifier parent;

    private static final int COUNT = 1000000;
    private static final int REPEAT = 10;

    private final boolean spawn;

    private final int branch;
    private final int repeat;
    
    private int merged = 0;
    private int test = 0;
    private long total = 0L;
    private long start;

    public ForkJoin(ActivityIdentifier parent) {
        super(new Context("DC"), false);
        this.parent = parent;
        this.spawn = false;
        this.branch = 0;
        this.repeat = 0;
    }
    
    public ForkJoin(ActivityIdentifier parent, int branch, int repeat, boolean spawn) {
        super(new Context("DC"), spawn);
        this.parent = parent;
        this.spawn = spawn;
        this.branch = branch;
        this.repeat = repeat;
    }

    private void spawnAll(Constellation c) {
        for (int i = 0; i < branch; i++) {
            c.submit(new ForkJoin(identifier()));
        }
    }

    @Override
    public int initialize(Constellation c) {

        if (spawn) {

            start = System.currentTimeMillis();

            spawnAll(c);
            return SUSPEND;
        } else {
            c.send(new Event(identifier(), parent, 1L));
            return FINISH;
        }
    }

    @Override
    public int process(Constellation c, Event e) {

    	total++;
        merged++;

        if (merged < branch) {
            return SUSPEND;
        }

        // We have finished one set of spawns
        merged = 0;
        test++;

        if (test < repeat) {
            spawnAll(c);
            return SUSPEND;
        }

//        // We have finished one iteration
//        long end = System.currentTimeMillis();
//
//        double timeSatin = (end - start) / 1000.0;
//        double cost = ((end - start) * 1000.0) / (SPAWNS_PER_SYNC * COUNT);
//
//        System.out.println("spawn = " + timeSatin + " s, time/spawn = " + cost + " us/spawn");
//
//        test = 0;
//        repeat++;
//
//        if (repeat < REPEAT) {
//            start = System.currentTimeMillis();
//            spawnAll(c);
//            return SUSPEND;
//        }

        // We have finished completely. Send message to event collector.
        c.send(new Event(identifier(), parent, total));
        return FINISH;
    }

    @Override
    public void cleanup(Constellation c) {
        // empty!
    }
}
