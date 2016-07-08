package test.lowlevel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.Event;
import ibis.constellation.Executor;
import ibis.constellation.SimpleExecutor;
import ibis.constellation.SingleEventCollector;
import ibis.constellation.StealStrategy;
import ibis.constellation.context.UnitActivityContext;
import ibis.constellation.context.UnitExecutorContext;

public class DivideAndConquerClean extends Activity {

    /*
     * This is a simple divide and conquer example. The user can specify the
     * branch factor and tree depth on the command line. All the application
     * does is calculate the sum of the number of nodes in each subtree.
     */

    private static final long serialVersionUID = 3379531054395374984L;

    private static final Logger logger = LoggerFactory
            .getLogger(DivideAndConquerClean.class);

    private final ActivityIdentifier parent;

    private final int branch;
    private final int depth;

    private int merged = 0;
    private long count = 1;

    public DivideAndConquerClean(ActivityIdentifier parent, int branch,
            int depth) {
        super(new UnitActivityContext("DC", depth), depth > 0);
        this.parent = parent;
        this.branch = branch;
        this.depth = depth;
    }

    @Override
    public void initialize() {

        if (depth == 0) {
            finish();
        } else {
            for (int i = 0; i < branch; i++) {
                submit(new DivideAndConquerClean(identifier(), branch,
                        depth - 1));
            }
            suspend();
        }
    }

    @Override
    public void process(Event e) {

        count += (Long) e.data;

        merged++;

        if (merged < branch) {
            suspend();
        } else {
            finish();
        }
    }

    @Override
    public void cleanup() {
        send(new Event(identifier(), parent, count));
    }

    @Override
    public String toString() {
        return "DC(" + identifier() + ") " + branch + ", " + depth + ", "
                + merged + " -> " + count;
    }

    public static void main(String[] args) throws Exception {

        int branch = Integer.parseInt(args[0]);
        int depth = Integer.parseInt(args[1]);
        // int load = Integer.parseInt(args[2]); Ignored for this version
        int nodes = Integer.parseInt(args[3]);
        int executors = Integer.parseInt(args[4]);

        long start = System.nanoTime();

        Executor[] e = new Executor[executors];

        for (int i = 0; i < executors; i++) {
            e[i] = new SimpleExecutor(new UnitExecutorContext("DC"),
                    StealStrategy.SMALLEST, StealStrategy.BIGGEST);
        }

        Constellation c = ConstellationFactory.createConstellation(e);
        c.activate();

        long count = 0;

        for (int i = 0; i <= depth; i++) {
            count += Math.pow(branch, i);
        }

        if (c.isMaster()) {

            logger.info(
                    "Running D&C with branch factor " + branch + " and depth "
                            + depth + " (expected jobs: " + count + ")");

            SingleEventCollector a = new SingleEventCollector(
                    new UnitActivityContext("DC"));

            c.submit(a);
            c.submit(new DivideAndConquerClean(a.identifier(), branch, depth));

            long result = (Long) a.waitForEvent().data;

            long end = System.nanoTime();

            double msPerJob = Math.round(((end - start) / 10000.0) * executors
                    * nodes / Math.pow(branch, depth)) / 100.0;

            String correct = (result == count) ? " (CORRECT)" : " (WRONG!)";
            logger.info("D&C(" + branch + ", " + depth + ") = " + result
                    + correct + " total time = "
                    + Math.round((end - start) / 1000000.0) / 1000.0
                    + " sec; leaf job time = " + msPerJob + " msec/job");
        }

        c.done();
    }

}
