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

public class Single extends Activity {

    private static final long serialVersionUID = 5970093414747228592L;

    private final ActivityIdentifier parent;

    private final int spawns;
    private int replies;

    public Single(ActivityIdentifier parent, int spawns) {
        super(new Context("TEST", 2, 2), false, true);
        this.parent = parent;
        this.spawns = spawns;
    }

    @Override
    public int initialize(Constellation c) {
        for (int i = 0; i < spawns; i++) {
            try {
                c.submit(new Dummy(identifier()));
            } catch (NoSuitableExecutorException e) {
                System.err.println("Should not happen: " + e);
                e.printStackTrace(System.err);
            }
        }

        return SUSPEND;
    }

    @Override
    public int process(Constellation c, Event e) {

        replies++;

        if (replies == spawns) {
            c.send(new Event(identifier(), parent, null));
            return FINISH;
        } else {
            return SUSPEND;
        }
    }

    @Override
    public void cleanup(Constellation c) {
        // unused
    }
}
