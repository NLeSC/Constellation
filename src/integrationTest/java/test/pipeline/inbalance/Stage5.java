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
package test.pipeline.inbalance;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.Event;
import ibis.constellation.Context;

public class Stage5 extends Activity {

    private static final long serialVersionUID = -2003940189338627474L;

    private final ActivityIdentifier parent;
    private final long sleep;

    private Data result3;
    private Data result4;

    public Stage5(ActivityIdentifier parent, int index, long sleep) {

        super(new Context("E", index), true);

        this.parent = parent;
        this.sleep = sleep;
    }

    @Override
    public int initialize(Constellation c) {
        return SUSPEND;
    }

    @Override
    public void cleanup(Constellation c) {

        Data result = processData();

        System.out.println("Finished pipeline: " + result.index);

        c.send(new Event(identifier(), parent, result));
    }

    private Data processData() {

        // Simulate some processing here that takes 'sleep' time
        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            } catch (Exception e) {
                // ignored
            }
        }

        return new Data(result3.index, 5, result3.data);
    }

    @Override
    public int process(Constellation c, Event e) {

        Data data = (Data) e.getData();

        if (data.stage == 3) {
            result3 = data;
        } else {
            result4 = data;
        }

        if (result3 != null && result4 != null) {
            return FINISH;
        } else {
            return SUSPEND;
        }
    }
}
