package test.lowlevel;

import java.util.Properties;

import org.junit.Test;

import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.context.UnitActivityContext;
import ibis.constellation.context.UnitExecutorContext;
import ibis.constellation.util.SingleEventCollector;

import static org.junit.Assert.assertEquals;

public class ForkJoinTest {
	
	private void runTest(int branch, int repeat, int nodes, int executors) throws Exception { 
		
		Properties p = new Properties();
	    p.put("ibis.constellation.distributed", "false");

		ConstellationConfiguration config = new ConstellationConfiguration(new UnitExecutorContext("DC")); 
				
		Constellation c = ConstellationFactory.createConstellation(p, config, executors);
		c.activate();
		
        if (c.isMaster()) {

        	int count = branch * repeat;
        	
        	System.out.println("Running F&J with branch factor " + branch + " repeated " + repeat + " times on " + executors + " executors on " + nodes + " nodes (expected jobs: " + count + ")");
        	
    		long start = System.nanoTime();

            SingleEventCollector a = new SingleEventCollector(new UnitActivityContext("DC"));

            c.submit(a);
            c.submit(new ForkJoin(a.identifier(), branch, repeat, true));

            long result = (Long) a.waitForEvent().getData();

			long end = System.nanoTime();

			double nsPerJob = Math.round(((end-start) / count) / (executors/ nodes));     
			
			String correct = (result == count) ? " (CORRECT)" : " (WRONG!)";
						
			System.out.println("F&J(" + branch + ", " + repeat + ") = " + result + correct + " total time = "
					+ Math.round((end - start) / 1000000.0) / 1000.0 + " sec; leaf job time = " + nsPerJob + " ns/job");
			
			assertEquals(result, count);

        }
        c.done();
    }
	
	@Test
	public void test1() throws Exception { 
		runTest(10, 100000, 1, 1);
	}

	@Test
	public void test2() throws Exception { 
		runTest(10, 100000, 1, 2);
	}

	@Test
	public void test3() throws Exception { 
		runTest(10, 100000, 1, 4);
	}

	@Test
	public void test4() throws Exception { 
		runTest(100000, 10, 1, 4);
	}

	
}	

