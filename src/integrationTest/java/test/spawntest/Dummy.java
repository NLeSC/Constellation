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

import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.Event;
import ibis.constellation.Context;
import ibis.constellation.util.SimpleActivity;

public class Dummy extends SimpleActivity {

    private static final long serialVersionUID = 5970093414747228592L;

    public Dummy(ActivityIdentifier parent) {
        super(parent, new Context("TEST", 1, 1));
    }

    @Override
    public void simpleActivity(Constellation c) {

        double tmp = 0.33333333;

        long time = System.nanoTime();

        do {
            tmp = Math.cos(tmp);
        } while (System.nanoTime() - time < 100000);

        c.send(new Event(identifier(), getParent(), null));
    }

}
