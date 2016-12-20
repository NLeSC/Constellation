package test.pipeline.simple;

import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.util.MultiEventCollector;
import ibis.constellation.StealStrategy;
import ibis.constellation.context.UnitActivityContext;
import ibis.constellation.context.UnitExecutorContext;

public class PipelineTest {

    public static void main(String[] args) {

        // Simple test that creates, starts and stops a set of constellation
        // instances.
        // When the lot is running, it deploys a series of jobs.

        int nodes = Integer.parseInt(args[0]);
        int rank = Integer.parseInt(args[1]);
        int executors = Integer.parseInt(args[2]);

        int jobs = Integer.parseInt(args[3]);
        long sleep = Long.parseLong(args[4]);
        int data = Integer.parseInt(args[5]);

        try {
            
            ConstellationConfiguration config = 
                    new ConstellationConfiguration(new UnitExecutorContext("X"), StealStrategy.BIGGEST, StealStrategy.SMALLEST);
            
            Constellation c = ConstellationFactory.createConstellation(config, executors);
            c.activate();

            if (rank == 0) {

                long start = System.currentTimeMillis();

                MultiEventCollector me = new MultiEventCollector(new UnitActivityContext("X"), jobs);

                c.submit(me);

                for (int i = 0; i < jobs; i++) {

                    System.out.println("SUBMIT " + i);

                    c.submit(new Pipeline(me.identifier(), i, 0, nodes * executors - 1, sleep, new byte[data]));
                }

                System.out.println("SUBMIT DONE");

                me.waitForEvents();

                long end = System.currentTimeMillis();

                System.out.println("Total processing time: " + (end - start) + " ms.");

            }

            c.done();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
