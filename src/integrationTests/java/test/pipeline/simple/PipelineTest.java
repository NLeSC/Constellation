package test.pipeline.simple;

import java.util.Properties;

import org.junit.Test;

import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationCreationException;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.util.MultiEventCollector;
import ibis.constellation.Context;
import ibis.constellation.StealStrategy;

public class PipelineTest {

	// default values
	private static int NODES = 1;
	private static int RANK = 0;
	private static int EXECUTORS = 4;
	private static int JOBS = 1000;
	private static int SLEEP = 1;
	private static int DATA = 1024;

	@Test
	public void test() throws ConstellationCreationException {

		Properties p = new Properties();
		p.put("ibis.constellation.distributed", "false");

        int nodes = NODES;
        int rank = RANK;
        int executors = EXECUTORS;
        int jobs = JOBS;
        long sleep = SLEEP;
        int data = DATA;

        ConstellationConfiguration config = 
        		new ConstellationConfiguration(new Context("X"), StealStrategy.BIGGEST, StealStrategy.SMALLEST);
            
        Constellation c = ConstellationFactory.createConstellation(p, config, executors);
        c.activate();

        if (rank == 0) {

        	long start = System.currentTimeMillis();

        	MultiEventCollector me = new MultiEventCollector(new Context("X"), jobs);

        	c.submit(me);

        	for (int i = 0; i < jobs; i++) {

        		//System.out.println("SUBMIT " + i);

        		c.submit(new Pipeline(me.identifier(), i, 0, nodes * executors - 1, sleep, new byte[data]));
        	}

        	//System.out.println("SUBMIT DONE");

        	me.waitForEvents();

        	long end = System.currentTimeMillis();

        	System.out.println("Total processing time: " + (end - start) + " ms.");

        }

        c.done();
    }
}
