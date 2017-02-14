package test.pipeline.inbalance;

import java.util.Properties;

import org.junit.Test;

import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationCreationException;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.util.MultiEventCollector;
import ibis.constellation.Context;
import ibis.constellation.StealStrategy;

public class WorkflowTest {

	private static int JOBS = 10;
	private static int SIZE = 1024;
	private static int RANK = 0;
	
	@Test
	public void test() throws ConstellationCreationException {

		// Simple test that creates, starts and stops a set of constellations.
		// When the lot is running, it deploys a series of jobs.
		int jobs = JOBS;
		int size = SIZE;
		int rank = RANK;

		Properties p = new Properties();
		p.put("ibis.constellation.distributed", "false");
		
		ConstellationConfiguration [] config = new ConstellationConfiguration[] { 
				new ConstellationConfiguration(new Context("MASTER"), StealStrategy.SMALLEST), 
				new ConstellationConfiguration(new Context("A"), StealStrategy.SMALLEST), 
				new ConstellationConfiguration(new Context("B"), StealStrategy.SMALLEST),
				new ConstellationConfiguration(new Context("X"), StealStrategy.SMALLEST),
				new ConstellationConfiguration(new Context("E"), StealStrategy.SMALLEST) };

		Constellation c = ConstellationFactory.createConstellation(p, config);
		c.activate();

		int expected = (1600 * JOBS) + 300;
		
		if (rank == 0) {

			long start = System.currentTimeMillis();

			MultiEventCollector me = new MultiEventCollector(new Context("MASTER"), jobs);
			c.submit(me);

			for (int i = 0; i < jobs; i++) {

				//System.out.println("SUBMIT " + i);

				Data data = new Data(i, 0, new byte[size]);
				c.submit(new Stage1(me.identifier(), 100, data));
			}

			//System.out.println("SUBMIT DONE");

			me.waitForEvents();

			long end = System.currentTimeMillis();

			System.out.println("Total processing time: " + (end - start) + " ms. (expected = " + expected + " ms.)");
		}

		c.done();
	}

}
