/*
 * Copyright 2018 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.lowlevel;

import java.util.Properties;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.StealStrategy;
import ibis.constellation.Context;
import ibis.constellation.util.SingleEventCollector;

public class DivideAndConquerCleanTest {

	private static final Logger logger = LoggerFactory.getLogger(DivideAndConquerClean.class);

	private void runTest(int branch, int depth, int nodes, int executors) throws Exception { 

		long start = System.nanoTime();

		Properties p = new Properties();
	    p.put("ibis.constellation.distributed", "false");

		ConstellationConfiguration config = new ConstellationConfiguration(new Context("DC"), 
				StealStrategy.SMALLEST, StealStrategy.BIGGEST, StealStrategy.BIGGEST);

		Constellation c = ConstellationFactory.createConstellation(p, config, executors);
		c.activate();

		long count = 0;

		for (int i = 0; i <= depth; i++) {
			count += Math.pow(branch, i);
		}

		if (c.isMaster()) {

			logger.info("Running D&C with branch factor " + branch + " and depth " + depth + " (expected jobs: " + count + ")");

			SingleEventCollector a = new SingleEventCollector(new Context("DC"));

			c.submit(a);
			c.submit(new DivideAndConquerClean(a.identifier(), branch, depth));

			long result = (Long) a.waitForEvent().getData();

			long end = System.nanoTime();

			double nsPerJob = Math.round(((end - start) / count) * (executors*nodes));

			String correct = (result == count) ? " (CORRECT)" : " (WRONG!)";
						
			logger.info("D&C(" + branch + ", " + depth + ") = " + result + correct + " total time = "
					+ Math.round((end - start) / 1000000.0) / 1000.0 + " sec; leaf job time = " + nsPerJob + " nsec/job");
			
			assertEquals(result, count);
		}

		c.done();
	}
	
	@Test
	public void fibOnOne() throws Exception {
		runTest(2, 21, 1, 1);
	}
	
	@Test
	public void fibOnTwo() throws Exception {
		runTest(2, 21, 1, 2);
	}
	
	@Test
	public void fibOnFour() throws Exception {
		runTest(2, 21, 1, 4);
	}
	
	
}
