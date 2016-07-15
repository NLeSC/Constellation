package test.lowlevel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.Activity;
import ibis.constellation.ActivityContext;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.ConstellationCreationException;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.Event;
import ibis.constellation.Executor;
import ibis.constellation.ExecutorContext;
import ibis.constellation.SimpleExecutor;
import ibis.constellation.SingleEventCollector;
import ibis.constellation.StealStrategy;
import ibis.constellation.context.UnitActivityContext;
import ibis.constellation.context.UnitExecutorContext;

public class DivideAndConquerWithLoadAndContext extends Activity {

    private static final Logger logger = LoggerFactory
            .getLogger(DivideAndConquerWithLoadAndContext.class);

    /*
     * This is a simple divide and conquer example. The user can specify the
     * branch factor and tree depth on the command line. All the application
     * does is calculate the sum of the number of nodes in each subtree.
     */

    private static final long serialVersionUID = 3379531054395374984L;

    private final ActivityIdentifier parent;

    private final int branch;
    private final int depth;
    private final int load;

    private long count = 0;
    private int merged = 0;

    public DivideAndConquerWithLoadAndContext(ActivityContext c,
            ActivityIdentifier parent, int branch, int depth, int load) {
        super(c, depth > 0);
        this.parent = parent;
        this.branch = branch;
        this.depth = depth;
        this.load = load;
    }

    @Override
    public void initialize() {
        logger.debug("Initialize " + identifier() + ", depth = " + depth);
        if (depth == 0) {

            if (load > 0) {

                long start = System.currentTimeMillis();
                long time = 0;

                while (time < load) {
                    time = System.currentTimeMillis() - start;
                }

                count = 1;
            }

            finish();
        } else {

            ActivityContext even = new UnitActivityContext("Even", depth - 1);
            ActivityContext odd = new UnitActivityContext("Odd", depth - 1);

            for (int i = 0; i < branch; i++) {

                if (i % 2 == 0) {
                    submit(new DivideAndConquerWithLoadAndContext(even,
                            identifier(), branch, depth - 1, load));
                } else {
                    submit(new DivideAndConquerWithLoadAndContext(odd,
                            identifier(), branch, depth - 1, load));
                }
            }
            suspend();
        }
    }

    @Override
    public void process(Event e) {
        logger.debug("Got event " + e);
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
        logger.debug("Cleanup " + identifier());
        send(new Event(identifier(), parent, count));
    }

    @Override
    public String toString() {
        return "DC(" + identifier() + ") " + branch + ", " + depth + ", "
                + merged + " -> " + count;
    }

    public static void main(String[] args) {

        long start = System.nanoTime();

        int branch = Integer.parseInt(args[0]);
        int depth = Integer.parseInt(args[1]);
        int load = Integer.parseInt(args[2]);
        int nodes = Integer.parseInt(args[3]);
        int executors = Integer.parseInt(args[4]);
        int rank = Integer.parseInt(args[5]);

        ExecutorContext even = new UnitExecutorContext("Even");
        ExecutorContext odd = new UnitExecutorContext("Odd");

        Executor[] e = new Executor[executors];

        for (int i = 0; i < executors; i++) {

            if (rank % 2 == 0) {
                e[i] = new SimpleExecutor(even, StealStrategy.SMALLEST,
                        StealStrategy.BIGGEST);
            } else {
                e[i] = new SimpleExecutor(odd, StealStrategy.SMALLEST,
                        StealStrategy.BIGGEST);
            }
        }

        Constellation c;
        try {
            c = ConstellationFactory.createConstellation(e);
        } catch (ConstellationCreationException e1) {
            logger.error("Could not create constellation", e1);
            return;
        }
        c.activate();

        logger.info("My rank is " + rank);

        if (c.isMaster()) {

            long count = 0;

            for (int i = 0; i <= depth; i++) {
                count += Math.pow(branch, i);
            }

            double time = (load * Math.pow(branch, depth))
                    / (1000 * (nodes * executors));

            logger.info("Running D&C with even/odd context and branch factor "
                    + branch + " and depth " + depth + " load " + load
                    + " (expected jobs: " + count + ", expected time: " + time
                    + " sec.)");

            SingleEventCollector a = new SingleEventCollector(
                    new UnitActivityContext((rank % 2) == 0 ? "Even" : "Odd"));

            c.submit(a);
            c.submit(new DivideAndConquerWithLoadAndContext(
                    new UnitActivityContext("Even", depth), a.identifier(),
                    branch, depth, load));

            long result = (Long) a.waitForEvent().data;

            long end = System.nanoTime();

            double msPerJob = Math.round(((end - start) / 10000.0) * nodes
                    * executors / Math.pow(branch, depth)) / 100.0;

            String correct = (result == count) ? " (CORRECT)" : " (WRONG!)";

            logger.info("D&C(" + branch + ", " + depth + ") = " + result
                    + correct + " total time = "
                    + Math.round((end - start) / 1000000.0) / 1000.0
                    + " sec; leaf job time = " + msPerJob
                    + " msec/job; overhead = "
                    + Math.round(100 * 100 * (msPerJob - load) / (load)) / 100.0
                    + "%");

        }

        c.done();

    }
}
