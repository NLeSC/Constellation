package test.lowlevel;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.Event;
import ibis.constellation.context.UnitActivityContext;
import ibis.constellation.context.UnitExecutorContext;
import ibis.constellation.util.SingleEventCollector;

public class DivideAndConquerSpawnTest extends Activity {

    /*
     * This is the Constellation equivalent of the SpawnOverhead test in Satin
     */
    private static final long serialVersionUID = 3379531054395374984L;

    private final ActivityIdentifier parent;

    private static final int SPAWNS_PER_SYNC = 10;
    private static final int COUNT = 1000000;
    private static final int REPEAT = 10;

    private final boolean spawn;

    private int merged = 0;

    private int repeat = 0;
    private int test = 0;

    private long start;

    public DivideAndConquerSpawnTest(ActivityIdentifier parent, boolean spawn) {
        super(new UnitActivityContext("DC"), spawn);
        this.parent = parent;
        this.spawn = spawn;
    }

    private void spawnAll(Constellation c) {
        for (int i = 0; i < SPAWNS_PER_SYNC; i++) {
            c.submit(new DivideAndConquerSpawnTest(identifier(), false));
        }
    }

    @Override
    public int initialize(Constellation c) {

        if (spawn) {

            start = System.currentTimeMillis();

            spawnAll(c);
            return SUSPEND;
        } else {
            c.send(new Event(identifier(), parent, 1));
            return FINISH;
        }
    }

    @Override
    public int process(Constellation c, Event e) {

        merged++;

        if (merged < SPAWNS_PER_SYNC) {
            return SUSPEND;
        }

        // We have finished one set of spawns
        merged = 0;
        test++;

        if (test <= COUNT) {
            spawnAll(c);
            return SUSPEND;
        }

        // We have finished one iteration
        long end = System.currentTimeMillis();

        double timeSatin = (end - start) / 1000.0;
        double cost = ((end - start) * 1000.0) / (SPAWNS_PER_SYNC * COUNT);

        System.out.println("spawn = " + timeSatin + " s, time/spawn = " + cost + " us/spawn");

        test = 0;
        repeat++;

        if (repeat < REPEAT) {
            start = System.currentTimeMillis();
            spawnAll(c);
            return SUSPEND;
        }

        // We have finished completely. Send message to event collector.
        c.send(new Event(identifier(), parent, 1));
        return FINISH;
    }

    @Override
    public void cleanup(Constellation c) {
        // empty!
    }

    public static void main(String[] args) throws Exception {

        ConstellationConfiguration config = new ConstellationConfiguration(new UnitExecutorContext("DC"));
        Constellation c = ConstellationFactory.createConstellation(config);
        c.activate();

        if (c.isMaster()) {

            SingleEventCollector a = new SingleEventCollector(new UnitActivityContext("DC"));

            c.submit(a);
            c.submit(new DivideAndConquerSpawnTest(a.identifier(), true));

            a.waitForEvent();

        }
        c.done();

    }
}
