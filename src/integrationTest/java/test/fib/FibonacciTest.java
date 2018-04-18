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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.junit.Test;

import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.ConstellationProperties;
import ibis.constellation.Context;
import ibis.constellation.StealStrategy;
import ibis.constellation.util.SingleEventCollector;
import ibis.ipl.server.Server;

public class FibonacciTest {

    private int result = 0;
    private Throwable exception = null;

    private synchronized void setResult(int r) {
        result = r;
    }

    private synchronized void setException(Throwable e) {
        exception = e;
    }

    private int runFibDistributed(final int executors, final int input) {
        final Properties p = new Properties();
        p.put(ConstellationProperties.S_DISTRIBUTED, "true");
        p.put(ConstellationProperties.S_CLOSED, "true");
        p.put("ibis.pool.name", "test");
        p.put(ConstellationProperties.S_POOLSIZE, "" + executors);
        p.put(ConstellationProperties.S_PROFILE, "true");
        p.put(ConstellationProperties.S_PROFILE_ACTIVITY, "true");
        p.put(ConstellationProperties.S_STATISTICS, "true");
        Server server;
        try {
            Properties serverProps = new Properties();
            serverProps.put("ibis.server.port", "" + (8888 + executors));
            server = new Server(serverProps);
        } catch (Exception e1) {
            e1.printStackTrace(System.out);
            setException(e1);
            return 0;
        }
        try {
            p.put("ibis.server.address", server.getAddress());
            System.out.println("Server address: " + server.getAddress());

            final ConstellationConfiguration e = new ConstellationConfiguration(new Context("fib"), StealStrategy.SMALLEST,
                    StealStrategy.BIGGEST);

            Thread[] threads = new Thread[executors];
            for (int i = 0; i < executors; i++) {
                System.out.println("Creating thread " + i);
                threads[i] = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Constellation c = ConstellationFactory.createConstellation(p, e, 1);
                            c.activate();
                            if (c.isMaster()) {

                                System.out.println("Starting master!");

                                SingleEventCollector a = new SingleEventCollector(new Context("fib"));

                                c.submit(a);
                                c.submit(new Fibonacci(a.identifier(), input, true));
                                setResult((Integer) a.waitForEvent().getData());
                            }
                            c.done();
                        } catch (Throwable e) {
                            e.printStackTrace(System.out);
                            setException(e);
                        }
                    }
                };
            }
            for (int i = 0; i < executors; i++) {
                threads[i].start();
            }
            for (int i = 0; i < executors; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException e1) {
                    // ignored.
                }
            }
        } finally {
            System.out.println("Ending server");
            server.end(1000L);
        }

        synchronized (this) {
            if (exception != null) {
                fail();
            }
            System.out.println("Returning result " + result);
            return result;
        }
    }

    private int runFib(int executors, int input) throws Exception {
        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");

        long start = System.currentTimeMillis();

        ConstellationConfiguration e = new ConstellationConfiguration(new Context("fib"), StealStrategy.SMALLEST,
                StealStrategy.BIGGEST);

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
            return result;
        } else {
            // Should not happen.
            fail();
            //            System.out.println("Starting as slave!");
            //            c.done();
            return 0;
        }
    }

    @Test
    public void fibOnOne() throws Exception {
        assertTrue(runFib(1, 20) == 6765);
        if (exception != null) {
            fail();
        }
        assertTrue(runFibDistributed(1, 20) == 6765);
    }

    @Test
    public void fibOnFour() throws Exception {
        assertTrue(runFib(4, 20) == 6765);
        if (exception != null) {
            fail();
        }
        assertTrue(runFibDistributed(4, 20) == 6765);
    }

    @Test
    public void fibOnEight() throws Exception {
        assertTrue(runFib(8, 20) == 6765);
        if (exception != null) {
            fail();
        }
        assertTrue(runFibDistributed(8, 20) == 6765);
    }
}