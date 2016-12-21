package test.lowlevel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationCreationException;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.Event;
import ibis.constellation.util.SingleEventCollector;
import ibis.constellation.StealStrategy;
import ibis.constellation.context.UnitActivityContext;
import ibis.constellation.context.UnitExecutorContext;

public class DivideAndConquerWithLoad extends Activity {

    /*
     * This is a simple divide and conquer example. The user can specify the
     * branch factor and tree depth on the command line. All the application
     * does is calculate the sum of the number of nodes in each subtree.
     */

    private static final long serialVersionUID = 3379531054395374984L;

    private static final Logger logger = LoggerFactory.getLogger(DivideAndConquerWithLoad.class);

    private final ActivityIdentifier parent;

    private final int branch;
    private final int depth;
    private final int load;

    private long count = 1;
    private int merged = 0;

    public DivideAndConquerWithLoad(ActivityIdentifier parent, int branch, int depth, int load) {
        super(new UnitActivityContext("DC", depth), depth > 0);
        this.parent = parent;
        this.branch = branch;
        this.depth = depth;
        this.load = load;
    }
    
    @Override
    public int initialize(Constellation c) {
        logger.debug("Initialize " + identifier() + ", depth = " + depth);
        if (depth == 0) {

            if (load > 0) {

                long start = System.currentTimeMillis();
                long time = 0;

                while (time < load) {
                    time = System.currentTimeMillis() - start;
                }
            }

            return FINISH;
        } else {
            for (int i = 0; i < branch; i++) {
                c.submit(new DivideAndConquerWithLoad(identifier(), branch, depth - 1, load));
            }
            return SUSPEND;
        }
    }

    @Override
    public int process(Constellation c, Event e) {
        logger.debug("Got event " + e);
        count += (Long) e.getData();

        merged++;

        if (merged < branch) {
            return SUSPEND;
        } else {
            return FINISH;
        }
    }

    @Override
    public void cleanup(Constellation c) {
        logger.debug("Cleanup " + identifier());
        c.send(new Event(identifier(), parent, count));
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

        ConstellationConfiguration config = new ConstellationConfiguration(new UnitExecutorContext("DC"), StealStrategy.SMALLEST,
                StealStrategy.BIGGEST, StealStrategy.BIGGEST);
        
        Constellation c;
        try {
            c = ConstellationFactory.createConstellation(config, executors);
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
            ActivityIdentifier id = c.submit(a);
            c.submit(new DivideAndConquerWithLoad(id, branch, depth, load));

            long result = (long) a.waitForEvent().getData();

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