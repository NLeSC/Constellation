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
package test.fib;

import java.util.Properties;

import org.junit.Test;

import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.StealStrategy;
import ibis.constellation.Context;
import ibis.constellation.util.SingleEventCollector;

public class FibonacciTest {

    private void runFib(int executors, int input) throws Exception { 
        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");

        long start = System.currentTimeMillis();

        ConstellationConfiguration e = new ConstellationConfiguration(new Context("fib"), 
                StealStrategy.SMALLEST, StealStrategy.BIGGEST);

        Constellation c = ConstellationFactory.createConstellation(p, e, executors);
        c.activate();

        if (c.isMaster()) {

            System.out.println("Starting as master!");

            SingleEventCollector a = new SingleEventCollector(new Context("fib"));

            c.submit(a);
            c.submit(new Fibonacci(a.identifier(), input, true));

            int result = (Integer) a.waitForEvent().getData();

            c.done();

            long end = System.currentTimeMillis();

            System.out.println("FIB: Fib(" + input + ") on " + executors + " threads = " + result + " (" + (end - start) + ")");
        } else {
            System.out.println("Starting as slave!");
            c.done();
        }
    }

    @Test
    public void fibOnOne() throws Exception {
        runFib(1, 34);
    }

    @Test
    public void fibOnFour() throws Exception {
        runFib(4, 34);
    }

    @Test
    public void fibOnEight() throws Exception {
        runFib(8, 34);
    }
}