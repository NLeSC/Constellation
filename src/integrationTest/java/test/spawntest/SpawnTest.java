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
package test.spawntest;

import static org.junit.Assert.fail;

import java.util.Properties;

import org.junit.Test;

import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationCreationException;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.Context;
import ibis.constellation.StealStrategy;
import ibis.constellation.util.SingleEventCollector;

public class SpawnTest {

    private static final int SPAWNS_PER_SYNC = 10;
    private static final int COUNT = 1000;
    private static final int REPEAT = 5;
    private static final int CONCURRENT = 100;

    @Test
    public void test() throws ConstellationCreationException {
        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");

        ConstellationConfiguration config = new ConstellationConfiguration(new Context("TEST", 0, Long.MAX_VALUE),
                StealStrategy.SMALLEST, StealStrategy.BIGGEST);

        Constellation c = ConstellationFactory.createConstellation(p, config);

        c.activate();

        if (c.isMaster()) {
            for (int i = 0; i < REPEAT; i++) {
                SingleEventCollector a = new SingleEventCollector(new Context("TEST", 0, 0));
                try {
                    ActivityIdentifier id = c.submit(a);
                    c.submit(new TestLoop(id, COUNT, CONCURRENT, SPAWNS_PER_SYNC));
                    a.waitForEvent();
                } catch (Throwable e) {
                    fail();
                }
            }
        }

        c.done();
    }
}
