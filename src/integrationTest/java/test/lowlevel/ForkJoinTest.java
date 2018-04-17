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

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Test;

import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.Context;
import ibis.constellation.util.SingleEventCollector;

public class ForkJoinTest {

    private long runTest(int branch, int repeat, int nodes, int executors) throws Exception {

        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");

        ConstellationConfiguration config = new ConstellationConfiguration(new Context("DC"));

        Constellation c = ConstellationFactory.createConstellation(p, config, executors);
        c.activate();
        long result = 0;

        if (c.isMaster()) {

            System.out.println("Running F&J with branch factor " + branch + " repeated " + repeat + " times on " + executors
                    + " executors on " + nodes + " nodes");

            long start = System.nanoTime();

            SingleEventCollector a = new SingleEventCollector(new Context("DC"));

            c.submit(a);
            c.submit(new ForkJoin(a.identifier(), branch, repeat, true));

            result = (Long) a.waitForEvent().getData();

            long end = System.nanoTime();

            double nsPerJob = Math.round(((end - start) / result) / (executors / nodes));

            System.out.println("F&J(" + branch + ", " + repeat + ") = " + result + " total time = "
                    + Math.round((end - start) / 1000000.0) / 1000.0 + " sec; leaf job time = " + nsPerJob + " ns/job");

        }
        c.done();
        return result;
    }

    @Test
    public void test1() throws Exception {
        long count = 10 * 100000;
        long result = runTest(10, 100000, 1, 1);
        assertEquals(result, count);
    }

    @Test
    public void test2() throws Exception {
        long count = 10 * 100000;
        long result = runTest(10, 100000, 1, 2);
        assertEquals(result, count);
    }

    @Test
    public void test3() throws Exception {
        long count = 10 * 100000;
        long result = runTest(10, 100000, 1, 4);
        assertEquals(result, count);
    }

    @Test
    public void test4() throws Exception {
        long count = 10 * 100000;
        long result = runTest(100000, 10, 1, 4);
        assertEquals(result, count);
    }

}
