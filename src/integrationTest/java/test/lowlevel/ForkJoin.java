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

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.Context;
import ibis.constellation.Event;
import ibis.constellation.NoSuitableExecutorException;

public class ForkJoin extends Activity {

    /*
     * This is the Constellation equivalent of the SpawnOverhead test in Satin
     */
    private static final long serialVersionUID = 3379531054395374984L;

    private final ActivityIdentifier parent;

    private static final int COUNT = 1000000;
    private static final int REPEAT = 10;

    private final boolean spawn;

    private final int branch;
    private final int repeat;

    private int merged = 0;
    private int test = 0;
    private long total = 0L;

    public ForkJoin(ActivityIdentifier parent) {
        super(new Context("DC"), false);
        this.parent = parent;
        this.spawn = false;
        this.branch = 0;
        this.repeat = 0;
    }

    public ForkJoin(ActivityIdentifier parent, int branch, int repeat, boolean spawn) {
        super(new Context("DC"), spawn);
        this.parent = parent;
        this.spawn = spawn;
        this.branch = branch;
        this.repeat = repeat;
    }

    private void spawnAll(Constellation c) throws NoSuitableExecutorException {
        for (int i = 0; i < branch; i++) {
            c.submit(new ForkJoin(identifier()));
        }
    }

    @Override
    public int initialize(Constellation c) {

        if (spawn) {
            try {
                spawnAll(c);
            } catch (NoSuitableExecutorException e) {
                System.err.println("Should not happen: " + e);
                e.printStackTrace(System.err);
            }
            return SUSPEND;
        } else {
            c.send(new Event(identifier(), parent, 1L));
            return FINISH;
        }
    }

    @Override
    public int process(Constellation c, Event e) {

        total++;
        merged++;

        if (merged < branch) {
            return SUSPEND;
        }

        // We have finished one set of spawns
        merged = 0;
        test++;

        if (test < repeat) {
            try {
                spawnAll(c);
            } catch (NoSuitableExecutorException e1) {
                System.err.println("Should not happen: " + e1);
                e1.printStackTrace(System.err);
            }
            return SUSPEND;
        }

        //        // We have finished one iteration
        //        long end = System.currentTimeMillis();
        //
        //        double timeSatin = (end - start) / 1000.0;
        //        double cost = ((end - start) * 1000.0) / (SPAWNS_PER_SYNC * COUNT);
        //
        //        System.out.println("spawn = " + timeSatin + " s, time/spawn = " + cost + " us/spawn");
        //
        //        test = 0;
        //        repeat++;
        //
        //        if (repeat < REPEAT) {
        //            start = System.currentTimeMillis();
        //            spawnAll(c);
        //            return SUSPEND;
        //        }

        // We have finished completely. Send message to event collector.
        c.send(new Event(identifier(), parent, total));
        return FINISH;
    }

    @Override
    public void cleanup(Constellation c) {
        // empty!
    }
}
