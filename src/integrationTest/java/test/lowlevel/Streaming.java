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
package test.lowlevel;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.Context;
import ibis.constellation.Event;
import ibis.constellation.NoSuitableExecutorException;
import ibis.constellation.StealStrategy;
import ibis.constellation.util.SingleEventCollector;

public class Streaming extends Activity {

    /*
     * This is a simple streaming example. A sequence of activities is created
     * (length specified on commandline). The first activity repeatedly sends
     * and object to the second activity, which forwards it to the third, etc.
     * Once all object have been received by the last activity, it sends a reply
     * to the application.
     */

    private static final long serialVersionUID = 3379531054395374984L;

    private final ActivityIdentifier root;
    private ActivityIdentifier next;

    private final int length;
    private final int index;
    private final int totaldata;
    private int dataSeen;

    public Streaming(ActivityIdentifier root, int length, int index, int totaldata) {
        super(new Context("S", index), true);
        this.root = root;
        this.length = length;
        this.index = index;
        this.totaldata = totaldata;
    }

    @Override
    public int initialize(Constellation c) {

        if (index < length) {
            // Submit the next job in the sequence
            try {
                next = c.submit(new Streaming(root, length, index + 1, totaldata));
            } catch (NoSuitableExecutorException e) {
                System.err.println("Should not happen: " + e);
                e.printStackTrace(System.err);
            }
        }

        return SUSPEND;
    }

    @Override
    public int process(Constellation c, Event e) {

        if (next != null) {
            c.send(new Event(identifier(), next, e.getData()));
        }

        dataSeen++;

        if (dataSeen == totaldata) {
            return FINISH;
        } else {
            return SUSPEND;
        }
    }

    @Override
    public void cleanup(Constellation c) {

        if (next == null) {
            // only the last replies!
            c.send(new Event(identifier(), root, dataSeen));
        }
    }

    @Override
    public String toString() {
        return "Streaming(" + identifier() + ") " + length;
    }

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();

        int index = 0;

        int length = Integer.parseInt(args[index++]);
        int data = Integer.parseInt(args[index++]);
        int executors = Integer.parseInt(args[index++]);

        System.out.println(
                "Running Streaming with series length " + length + " and " + data + " messages " + executors + " Executors");

        ConstellationConfiguration config = new ConstellationConfiguration(new Context("S"), StealStrategy.SMALLEST,
                StealStrategy.BIGGEST, StealStrategy.BIGGEST);

        Constellation c = ConstellationFactory.createConstellation(config, executors);
        c.activate();

        if (c.isMaster()) {

            SingleEventCollector a = new SingleEventCollector(new Context("S"));

            c.submit(a);

            ActivityIdentifier aid = c.submit(new Streaming(a.identifier(), length, 0, data));

            for (int i = 0; i < data; i++) {
                c.send(new Event(a.identifier(), aid, i));
            }

            long result = (Long) a.waitForEvent().getData();

            long end = System.currentTimeMillis();

            double nsPerJob = (1000.0 * 1000.0 * (end - start)) / (data * length);

            String correct = (result == data) ? " (CORRECT)" : " (WRONG!)";

            System.out.println("Series(" + length + ", " + data + ") = " + result + correct + " total time = " + (end - start)
                    + " job time = " + nsPerJob + " nsec/job");
        }

        c.done();

    }
}
