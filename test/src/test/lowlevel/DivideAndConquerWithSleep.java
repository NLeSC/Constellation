package test.lowlevel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.ConstellationCreationException;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.Event;
import ibis.constellation.Executor;
import ibis.constellation.SimpleExecutor;
import ibis.constellation.SingleEventCollector;
import ibis.constellation.StealStrategy;
import ibis.constellation.context.UnitActivityContext;
import ibis.constellation.context.UnitExecutorContext;

public class DivideAndConquerWithSleep extends Activity {

    /*
     * This is a simple divide and conquer example. The user can specify the
     * branch factor and tree depth on the command line. All the application
     * does is calculate the sum of the number of nodes in each subtree.
     */

    private static final long serialVersionUID = 3379531054395374984L;

    private static final Logger logger = LoggerFactory.getLogger(DivideAndConquerWithSleep.class);

    private final ActivityIdentifier parent;

    private final int branch;
    private final int depth;
    private final int load;

    private int merged = 0;
    private long count = 1;

    public DivideAndConquerWithSleep(ActivityIdentifier parent, int branch, int depth, int load) {
        super(new UnitActivityContext("DC", depth), depth > 0);
        this.parent = parent;
        this.branch = branch;
        this.depth = depth;
        this.load = load;
    }

    @Override
    public void initialize() {

        if (depth == 0) {

            if (load > 0) {
                try {
                    Thread.sleep(load);
                } catch (Exception e) {
                    // ignore
                }
            }

            finish();
        } else {
            logger.debug("Spawning " + branch + " jobs with depth " + (depth - 1));
            for (int i = 0; i < branch; i++) {
                submit(new DivideAndConquerWithSleep(identifier(), branch, depth - 1, load));
            }
            suspend();
        }
    }

    @Override
    public void process(Event e) {

        count += (Long) e.getData();

        merged++;

        if (merged < branch) {
            suspend();
        } else {
            finish();
        }
    }

    @Override
    public void cleanup() {
        if (depth > 0) {
            logger.debug("Done with " + branch + " jobs of depth " + (depth - 1));
        }
        send(new Event(identifier(), parent, count));
    }

    @Override
    public String toString() {
        return "DC(" + identifier() + ") " + branch + ", " + depth + ", " + merged + " -> " + count;
    }

    public static void main(String[] args) {

        long start = System.nanoTime();

        int branch = Integer.parseInt(args[0]);
        int depth = Integer.parseInt(args[1]);
        int load = Integer.parseInt(args[2]);
        int nodes = Integer.parseInt(args[3]);
        int executors = Integer.parseInt(args[4]);

        Executor[] e = new Executor[executors];

        for (int i = 0; i < executors; i++) {
            e[i] = new SimpleExecutor(new UnitExecutorContext("DC"), StealStrategy.SMALLEST, StealStrategy.BIGGEST);
        }

        Constellation c;
        try {
            c = ConstellationFactory.createConstellation(e);
        } catch (ConstellationCreationException e1) {
            logger.error("Could not create constellation", e1);
            return;
        }
        c.activate();

        if (c.isMaster()) {

            long count = 0;

            for (int i = 0; i <= depth; i++) {
                count += Math.pow(branch, i);
            }

            double time = (load * Math.pow(branch, depth)) / (1000 * (nodes * executors));

            logger.info("Running D&C with branch factor " + branch + " and depth " + depth + " load " + load + " (expected jobs: "
                    + count + ", expected time: " + time + " sec.)");

            SingleEventCollector a = new SingleEventCollector(new UnitActivityContext("DC"));

            c.submit(a);
            c.submit(new DivideAndConquerWithSleep(a.identifier(), branch, depth, load));

            long result = (Long) a.waitForEvent().getData();

            long end = System.nanoTime();

            double msPerJob = Math.round(((end - start) / 10000.0) * nodes * executors / Math.pow(branch, depth)) / 100.0;

            String correct = (result == count) ? " (CORRECT)" : " (WRONG!)";

            logger.info("D&C(" + branch + ", " + depth + ") = " + result + correct + " total time = "
                    + Math.round((end - start) / 1000000.0) / 1000.0 + " sec; leaf job time = " + msPerJob
                    + " msec/job; overhead = " + Math.round(100 * 100 * (msPerJob - load) / (load)) / 100.0 + "%");

        }

        c.done();

    }
}
