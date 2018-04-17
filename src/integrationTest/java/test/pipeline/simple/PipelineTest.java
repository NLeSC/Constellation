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
package test.pipeline.simple;

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

public class PipelineTest {

    // default values
    private static int NODES = 1;
    private static int EXECUTORS = 4;
    private static int JOBS = 1000;
    private static int SLEEP = 1;
    private static int DATA = 1024;

    @Test
    public void test() throws ConstellationCreationException, NoSuitableExecutorException {

        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");

        int nodes = NODES;
        int executors = EXECUTORS;
        int jobs = JOBS;
        long sleep = SLEEP;
        int data = DATA;

        ConstellationConfiguration config = new ConstellationConfiguration(new Context("X"), StealStrategy.BIGGEST,
                StealStrategy.SMALLEST);

        Constellation c = ConstellationFactory.createConstellation(p, config, executors);
        c.activate();

        if (c.isMaster()) {

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

        } else {
            fail();
        }

        c.done();
    }
}
