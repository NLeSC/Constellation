/**
 * Copyright 2013 Netherlands eScience Center
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

package ibis.constellation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class ConstellationPropertiesTest {

    @Test
    public void testDistributedFalse1() {

        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");

        ConstellationProperties cp = new ConstellationProperties(p);

        assertFalse(cp.DISTRIBUTED);
    }

    @Test
    public void testDistributedTrue1() {

        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "true");

        ConstellationProperties cp = new ConstellationProperties(p);

        assertTrue(cp.DISTRIBUTED);
    }

    @Test
    public void testDistributedTrue2() {

        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "1");

        ConstellationProperties cp = new ConstellationProperties(p);

        assertTrue(cp.DISTRIBUTED);
    }

    @Test
    public void testDistributedTrue3() {

        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "on");

        ConstellationProperties cp = new ConstellationProperties(p);

        assertTrue(cp.DISTRIBUTED);
    }

    @Test
    public void testDistributedTrue4() {

        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "");

        ConstellationProperties cp = new ConstellationProperties(p);

        assertTrue(cp.DISTRIBUTED);
    }

    @Test
    public void testDistributedTrue5() {

        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "yes");

        ConstellationProperties cp = new ConstellationProperties(p);

        assertTrue(cp.DISTRIBUTED);
    }

    @Test
    public void testPoolsize() {

        Properties p = System.getProperties();
        p.put("ibis.constellation.poolSize", "2");

        ConstellationProperties cp = new ConstellationProperties(p);

        assertEquals(cp.POOLSIZE, 2);
    }

    @Test(expected = NumberFormatException.class)
    public void testPoolsizeFails() {

        // Note: this pollutes the system properties, which may affect later runs!
        Properties p = System.getProperties();
        p.put("ibis.constellation.poolSize", "foobar");
        /* ConstellationProperties cp = */ new ConstellationProperties(p);
    }

}
