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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class DivideAndConquerClean extends Activity {

    /*
     * This is a simple divide and conquer example. The user can specify the
     * branch factor and tree depth on the command line. All the application
     * does is calculate the sum of the number of nodes in each subtree.
     */

    private static final long serialVersionUID = 3379531054395374984L;

    private static final Logger logger = LoggerFactory.getLogger(DivideAndConquerClean.class);

    private final ActivityIdentifier parent;

    private final int branch;
    private final int depth;

    private int merged = 0;
    private long count = 1;

    public DivideAndConquerClean(ActivityIdentifier parent, int branch, int depth) {
        super(new Context("DC", depth), depth > 0);
        this.parent = parent;
        this.branch = branch;
        this.depth = depth;
    }

    @Override
    public int initialize(Constellation c) {

        if (depth == 0) {
            return FINISH;
        } else {
            for (int i = 0; i < branch; i++) {
                try {
                    c.submit(new DivideAndConquerClean(identifier(), branch, depth - 1));
                } catch (NoSuitableExecutorException e) {
                    System.err.println("Should not happen: " + e);
                    e.printStackTrace(System.err);
                }
            }
            return SUSPEND;
        }
    }

    @Override
    public int process(Constellation c, Event e) {

        count += (Long) e.getData();

        merged++;

        if (merged < branch) {
            return SUSPEND;
        } else {
            return FINISH;
        }
    }

    @Override
    public void cleanup(Constellation c) {
        c.send(new Event(identifier(), parent, count));
    }

    @Override
    public String toString() {
        return "DC(" + identifier() + ") " + branch + ", " + depth + ", " + merged + " -> " + count;
    }

    public static void main(String[] args) throws Exception {

        int branch = Integer.parseInt(args[0]);
        int depth = Integer.parseInt(args[1]);
        // int load = Integer.parseInt(args[2]); Ignored for this version
        int nodes = Integer.parseInt(args[3]);
        int executors = Integer.parseInt(args[4]);

        long start = System.nanoTime();

        ConstellationConfiguration config = new ConstellationConfiguration(new Context("DC"), StealStrategy.SMALLEST,
                StealStrategy.BIGGEST, StealStrategy.BIGGEST);

        Constellation c = ConstellationFactory.createConstellation(config, executors);
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

            double msPerJob = Math.round(((end - start) / 10000.0) * executors * nodes / Math.pow(branch, depth)) / 100.0;

            String correct = (result == count) ? " (CORRECT)" : " (WRONG!)";
            logger.info("D&C(" + branch + ", " + depth + ") = " + result + correct + " total time = "
                    + Math.round((end - start) / 1000000.0) / 1000.0 + " sec; leaf job time = " + msPerJob + " msec/job");
        }

        c.done();
    }

}
