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

public class Streaming extends Activity {

    /*
     * This is a simple streaming example. A sequence of activities is created
     * (length specified on commandline). The first activity repeatedly sends
     * and object to the second activity, which forwards it to the third, etc.
     * Once all object have been received by the last activity, it sends a reply
     * to the application.
     */

    private static final long serialVersionUID = 3379531054395374984L;

    private final ActivityIdentifier root;
    private ActivityIdentifier next;

    private final int length;
    private final int index;
    private final int totaldata;
    private int dataSeen;

    public Streaming(ActivityIdentifier root, int length, int index,
            int totaldata) {
        super(new UnitActivityContext("S", index), true);
        this.root = root;
        this.length = length;
        this.index = index;
        this.totaldata = totaldata;
    }

    @Override
    public void initialize() {

        if (index < length) {
            // Submit the next job in the sequence
            next = submit(new Streaming(root, length, index + 1, totaldata));
        }

        suspend();
    }

    @Override
    public void process(Event e) {

        if (next != null) {
            send(new Event(identifier(), next, e.data));
        }

        dataSeen++;

        if (dataSeen == totaldata) {
            finish();
        } else {
            suspend();
        }
    }

    @Override
    public void cleanup() {

        if (next == null) {
            // only the last replies!
            send(new Event(identifier(), root, dataSeen));
        }
    }

    public String toString() {
        return "Streaming(" + identifier() + ") " + length;
    }

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();

        int index = 0;

        int length = Integer.parseInt(args[index++]);
        int data = Integer.parseInt(args[index++]);
        int executors = Integer.parseInt(args[index++]);

        System.out.println("Running Streaming with series length " + length
                + " and " + data + " messages " + executors + " Executors");

        Executor[] e = new Executor[executors];

        for (int i = 0; i < executors; i++) {
            e[i] = new SimpleExecutor(new UnitExecutorContext("S"),
                    StealStrategy.SMALLEST, StealStrategy.BIGGEST);
        }

        Constellation c = ConstellationFactory.createConstellation(e);
        c.activate();

        if (c.isMaster()) {

            SingleEventCollector a = new SingleEventCollector(
                    new UnitActivityContext("S"));

            c.submit(a);

            ActivityIdentifier aid = c
                    .submit(new Streaming(a.identifier(), length, 0, data));

            for (int i = 0; i < data; i++) {
                c.send(new Event(a.identifier(), aid, i));
            }

            long result = (Long) a.waitForEvent().data;

            long end = System.currentTimeMillis();

            double nsPerJob = (1000.0 * 1000.0 * (end - start))
                    / (data * length);

            String correct = (result == data) ? " (CORRECT)" : " (WRONG!)";

            System.out.println("Series(" + length + ", " + data + ") = "
                    + result + correct + " total time = " + (end - start)
                    + " job time = " + nsPerJob + " nsec/job");
        }

        c.done();

    }
}
