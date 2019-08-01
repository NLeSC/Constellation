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

public class Stage1 extends SimpleActivity {

    private static final long serialVersionUID = -3987089095770723454L;

    private final long sleep;
    private final Data data;

    public Stage1(ActivityIdentifier parent, long sleep, Data data) {

        super(parent, new Context("A", data.index));

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

        try {
            c.submit(new Stage2(getParent(), 200, new Data(data.index, 1, data.data)));
        } catch (NoSuitableExecutorException e) {
            System.err.println("Should not happen: " + e);
            e.printStackTrace(System.err);
        }
    }
}
