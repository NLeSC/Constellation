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

import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.Context;
import ibis.constellation.Event;
import ibis.constellation.NoSuitableExecutorException;
import ibis.constellation.util.SimpleActivity;

public class Pipeline extends SimpleActivity {

    private static final long serialVersionUID = -3987089095770723454L;

    private final int index;
    private final int current;
    private final int last;
    private final long sleep;
    private final Object data;

    public Pipeline(ActivityIdentifier parent, int index, int current, int last, long sleep, Object data) {

        super(parent, new Context("X", current));

        this.index = index;
        this.current = current;
        this.last = last;
        this.sleep = sleep;
        this.data = data;
    }

    @Override
    public void simpleActivity(Constellation c) {

        //System.out.println("RUNNING pipeline " + index + " " + current + " " + last);

        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            } catch (Exception e) {
                // ignored
            }
        }

        if (current == last) {

            //System.out.println("Sending pipeline reply");

            c.send(new Event(identifier(), getParent(), data));
        } else {

            //System.out.println("Submitting pipeline stage: " + index + " " + (current + 1) + " " + last);

            try {
                c.submit(new Pipeline(getParent(), index, current + 1, last, sleep, data));
            } catch (NoSuitableExecutorException e) {
                System.err.println("Should not happen: " + e);
                e.printStackTrace(System.err);
            }
        }
    }
}
