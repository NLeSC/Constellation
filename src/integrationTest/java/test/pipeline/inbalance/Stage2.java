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
package test.pipeline.inbalance;

import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.Context;
import ibis.constellation.NoSuitableExecutorException;
import ibis.constellation.util.SimpleActivity;

public class Stage2 extends SimpleActivity {

    private static final long serialVersionUID = -3987089095770723454L;

    private final long sleep;
    private final Data data;

    public Stage2(ActivityIdentifier parent, long sleep, Data data) {

        super(parent, new Context("B", data.index));
        this.sleep = sleep;
        this.data = data;
    }

    @Override
    public void simpleActivity(Constellation c) {

        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            } catch (Exception e) {
                // ignored
            }
        }

        // Submit stage5 first, as it it used to gather the results from
        // stage3&4
        try {
            ActivityIdentifier id = c.submit(new Stage5(getParent(), data.index, 100));

            c.submit(new Stage3(id, 1000, data));

            c.submit(new Stage4(id, 600, data));
        } catch (NoSuitableExecutorException e) {
            System.err.println("Should not happen: " + e);
            e.printStackTrace(System.err);
        }

    }
}
