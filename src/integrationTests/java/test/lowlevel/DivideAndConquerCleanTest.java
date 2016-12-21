package test.lowlevel;

import java.util.Properties;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.StealStrategy;
import ibis.constellation.context.UnitActivityContext;
import ibis.constellation.context.UnitExecutorContext;
import ibis.constellation.util.SingleEventCollector;

public class DivideAndConquerCleanTest {

	private static final Logger logger = LoggerFactory.getLogger(DivideAndConquerClean.class);

	private void runTest(int branch, int depth, int nodes, int executors) throws Exception { 

		long start = System.nanoTime();

		Properties p = new Properties();
	    p.put("ibis.constellation.distributed", "false");

		ConstellationConfiguration config = new ConstellationConfiguration(new UnitExecutorContext("DC"), 
				StealStrategy.SMALLEST, StealStrategy.BIGGEST, StealStrategy.BIGGEST);

		Constellation c = ConstellationFactory.createConstellation(p, config, executors);
		c.activate();

		long count = 0;

		for (int i = 0; i <= depth; i++) {
			count += Math.pow(branch, i);
		}

		if (c.isMaster()) {

			logger.info("Running D&C with branch factor " + branch + " and depth " + depth + " (expected jobs: " + count + ")");

			SingleEventCollector a = new SingleEventCollector(new UnitActivityContext("DC"));

			c.submit(a);
			c.submit(new DivideAndConquerClean(a.identifier(), branch, depth));

			long result = (Long) a.waitForEvent().getData();

			long end = System.nanoTime();

			double msPerJob = Math.round(((end - start) / 10000.0) * executors * nodes / Math.pow(branch, depth)) / 100.0;

			String correct = (result == count) ? " (CORRECT)" : " (WRONG!)";
			logger.info("D&C(" + branch + ", " + depth + ") = " + result + correct + " total time = "
					+ Math.round((end - start) / 1000000.0) / 1000.0 + " sec; leaf job time = " + msPerJob + " msec/job");
		}

		c.done();
	}
	
	@Test
	public void fibOnOne() throws Exception {
		runTest(2, 5, 1, 1);
	}
}
