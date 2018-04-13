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

import java.util.Arrays;

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

public class DivideAndConquerWithChecks extends Activity {

    /*
     * This is a simple divide and conquer example. The user can specify the
     * branch factor and tree depth on the command line. All the application
     * does is calculate the sum of the number of nodes in each subtree.
     */

    private static final long serialVersionUID = 3379531054395374984L;

    private final ActivityIdentifier parent;

    private final int branch;
    private final int depth;

    private int merged = 0;

    private long count = 1;

    private boolean done = false;

    private ActivityIdentifier[] children;
    private ActivityIdentifier[] received;

    public DivideAndConquerWithChecks(ActivityIdentifier parent, int branch, int depth) {
        super(new Context("DC", depth), true);
        this.parent = parent;
        this.branch = branch;
        this.depth = depth;
    }

    @Override
    public int initialize(Constellation c) {

        if (depth == 0) {
            return FINISH;
        } else {

            if (children != null) {
                System.out.println("EEP: initialize called twice !!!");
            }

            children = new ActivityIdentifier[branch];
            received = new ActivityIdentifier[branch];

            for (int i = 0; i < branch; i++) {
                try {
                    children[i] = c.submit(new DivideAndConquerWithChecks(identifier(), branch, depth - 1));
                } catch (NoSuitableExecutorException e) {
                    System.err.println("Should not happen: " + e);
                    e.printStackTrace(System.err);
                }
            }
            return SUSPEND;
        }
    }

    private void checkSource(Event e) {

        if (children == null) {
            System.out.println("EEP: leaf node " + identifier() + " got stray message! " + e.getSource() + " " + e.getTarget());
        }

        for (ActivityIdentifier a : children) {
            if (a.equals(e.getSource())) {
                return;
            }
        }

        System.out.println("EEP: node " + identifier() + " got stray message! " + e.getSource() + " " + e.getTarget() + " "
                + Arrays.toString(children));

    }

    @Override
    public int process(Constellation c, Event e) {

        checkSource(e);

        received[merged] = e.getSource();

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

        if (!done) {
            c.send(new Event(identifier(), parent, count));
            done = true;
        } else {
            System.out.println("EEP! Cleanup called twice!");
            new Exception().printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "DC(" + identifier() + " " + Arrays.toString(children) + " " + Arrays.toString(received) + ") " + branch + ", "
                + depth + ", " + merged + " -> " + count;
    }

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();

        ConstellationConfiguration config = new ConstellationConfiguration(new Context("DC"), StealStrategy.SMALLEST,
                StealStrategy.BIGGEST, StealStrategy.BIGGEST);

        Constellation c = ConstellationFactory.createConstellation(config);

        c.activate();

        if (c.isMaster()) {

            int index = 0;

            int branch = Integer.parseInt(args[index++]);
            int depth = Integer.parseInt(args[index++]);

            long count = 0;

            for (int i = 0; i <= depth; i++) {
                count += Math.pow(branch, i);
            }

            System.out.println(
                    "Running D&C with branch factor " + branch + " and depth " + depth + " (expected jobs: " + count + ")");

            SingleEventCollector a = new SingleEventCollector(new Context("DC"));

            c.submit(a);
            c.submit(new DivideAndConquerWithChecks(a.identifier(), branch, depth));

            long result = (Long) a.waitForEvent().getData();

            long end = System.currentTimeMillis();

            double nsPerJob = (1000.0 * 1000.0 * (end - start)) / count;

            String correct = (result == count) ? " (CORRECT)" : " (WRONG!)";

            System.out.println("D&C(" + branch + ", " + depth + ") = " + result + correct + " total time = " + (end - start)
                    + " job time = " + nsPerJob + " nsec/job");
        }
        c.done();

    }
}
