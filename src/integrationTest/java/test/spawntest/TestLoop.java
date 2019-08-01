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
package test.spawntest;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.Context;
import ibis.constellation.Event;
import ibis.constellation.NoSuitableExecutorException;

public class TestLoop extends Activity {

    private static final long serialVersionUID = 5970093414747228592L;

    private final ActivityIdentifier parent;

    private final long count;
    private final int concurrent;
    private final int spawns;

    private int pending;
    private int done;

    private long start;
    private long end;

    public TestLoop(ActivityIdentifier parent, long count, int concurrent, int spawns) {
        super(new Context("TEST", 3, 3), true, true);
        this.parent = parent;
        this.count = count;
        this.concurrent = concurrent;
        this.spawns = spawns;
    }

    @Override
    public int initialize(Constellation c) {
        start = System.currentTimeMillis();

        for (int i = 0; i < concurrent; i++) {
            pending++;
            try {
                c.submit(new Single(identifier(), spawns));
            } catch (NoSuitableExecutorException e) {
                System.err.println("Should not happen: " + e);
                e.printStackTrace(System.err);
            }
        }

        return SUSPEND;
    }

    @Override
    public int process(Constellation c, Event e) {

        done++;

        if (done == count) {
            end = System.currentTimeMillis();
            return FINISH;
        }

        if (pending < count) {
            pending++;
            try {
                c.submit(new Single(identifier(), spawns));
            } catch (NoSuitableExecutorException e1) {
                System.err.println("Should not happen: " + e1);
                e1.printStackTrace(System.err);
            }
        }

        return SUSPEND;
    }

    @Override
    public void cleanup(Constellation c) {

        double timeSatin = (end - start) / 1000.0;
        double cost = ((end - start) * 1000.0) / (spawns * count);

        System.out.println("spawn = " + timeSatin + " s, time/spawn = " + cost + " us/spawn");

        c.send(new Event(identifier(), parent, null));
    }
}
