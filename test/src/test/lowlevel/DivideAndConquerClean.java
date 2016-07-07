package test.lowlevel;

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

        long start = System.currentTimeMillis();

        int index = 0;

        int executors = Integer.parseInt(args[index++]);

        Executor[] e = new Executor[executors];

        for (int i = 0; i < executors; i++) {
            e[i] = new SimpleExecutor(new UnitExecutorContext("DC"),
                    StealStrategy.SMALLEST, StealStrategy.BIGGEST);
        }

        Constellation c = ConstellationFactory.createConstellation(e);
        c.activate();

        int branch = Integer.parseInt(args[index++]);
        int depth = Integer.parseInt(args[index++]);

        long count = 0;

        for (int i = 0; i <= depth; i++) {
            count += Math.pow(branch, i);
        }

        if (c.isMaster()) {

            System.out.println(
                    "Running D&C with branch factor " + branch + " and depth "
                            + depth + " (expected jobs: " + count + ")");

            SingleEventCollector a = new SingleEventCollector(
                    new UnitActivityContext("DC"));

            c.submit(a);
            c.submit(new DivideAndConquerClean(a.identifier(), branch, depth));

            long result = (Long) a.waitForEvent().data;

            long end = System.currentTimeMillis();

            double nsPerJob = (1000.0 * 1000.0 * (end - start)) / count;

            String correct = (result == count) ? " (CORRECT)" : " (WRONG!)";

            System.out.println("D&C(" + branch + ", " + depth + ") = " + result
                    + correct + " total time = " + (end - start)
                    + " job time = " + nsPerJob + " nsec/job");
        }

        c.done();
    }

}
