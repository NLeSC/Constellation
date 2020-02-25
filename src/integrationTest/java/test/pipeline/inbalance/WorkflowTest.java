/*
 * Copyright 2019 Vrije Universiteit Amsterdam
 *                Netherlands eScience Center
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
package test.pipeline.inbalance;

import static org.junit.Assert.fail;

import java.util.Properties;

import org.junit.Test;

import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationCreationException;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.Context;
import ibis.constellation.NoSuitableExecutorException;
import ibis.constellation.StealStrategy;
import ibis.constellation.util.MultiEventCollector;

public class WorkflowTest {

    private static int JOBS = 10;
    private static int SIZE = 1024;

    @Test
    public void test() throws ConstellationCreationException {

        // Simple test that creates, starts and stops a set of constellations.
        // When the lot is running, it deploys a series of jobs.
        int jobs = JOBS;
        int size = SIZE;

        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");

        ConstellationConfiguration[] config = new ConstellationConfiguration[] {
                new ConstellationConfiguration(new Context("MASTER"), StealStrategy.SMALLEST),
                new ConstellationConfiguration(new Context("A"), StealStrategy.SMALLEST),
                new ConstellationConfiguration(new Context("B"), StealStrategy.SMALLEST),
                new ConstellationConfiguration(new Context("X"), StealStrategy.SMALLEST),
                new ConstellationConfiguration(new Context("E"), StealStrategy.SMALLEST) };

        Constellation c = ConstellationFactory.createConstellation(p, config);
        c.activate();

        int expected = (1600 * JOBS) + 300;

        if (c.isMaster()) {

            long start = System.currentTimeMillis();

            MultiEventCollector me = new MultiEventCollector(new Context("MASTER"), jobs);
            try {
                c.submit(me);
            } catch (NoSuitableExecutorException e) {
                System.err.println("Should not happen: " + e);
                e.printStackTrace(System.err);
            }

            for (int i = 0; i < jobs; i++) {

                //System.out.println("SUBMIT " + i);

                Data data = new Data(i, 0, new byte[size]);
                try {
                    c.submit(new Stage1(me.identifier(), 100, data));
                } catch (NoSuitableExecutorException e) {
                    System.err.println("Should not happen: " + e);
                    e.printStackTrace(System.err);
                }
            }

            //System.out.println("SUBMIT DONE");

            me.waitForEvents();

            long end = System.currentTimeMillis();

            System.out.println("Total processing time: " + (end - start) + " ms. (expected = " + expected + " ms.)");
        } else {
            fail();
        }

        c.done();
    }

}
