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
package ibis.constellation;

import java.util.Properties;

import org.junit.Test;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class ConstellationFactoryTest {

    @Test(expected = IllegalArgumentException.class)
    public void testCreationNull() throws ConstellationCreationException {
        ConstellationConfiguration config = null;
        ConstellationFactory.createConstellation(config);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreationNullCount() throws ConstellationCreationException {
        ConstellationConfiguration config = null;
        ConstellationFactory.createConstellation(config, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreationEmpty() throws ConstellationCreationException {
        ConstellationConfiguration[] config = new ConstellationConfiguration[0];
        ConstellationFactory.createConstellation(config);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreationNullArray() throws ConstellationCreationException {
        ConstellationConfiguration[] config = null;
        ConstellationFactory.createConstellation(config);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreationEmptyArrray() throws ConstellationCreationException {
        ConstellationConfiguration[] config = new ConstellationConfiguration[4];
        ConstellationFactory.createConstellation(config);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreationEmptyArgument() throws ConstellationCreationException {
        Context cxt = new Context("DEFAULT", 0, 0);

        ConstellationConfiguration config = new ConstellationConfiguration(cxt);
        ConstellationFactory.createConstellation(config, null, config);
    }

    @Test
    public void createDefault1() throws ConstellationCreationException {

        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");

        Context cxt = new Context("DEFAULT", 0, 0);

        ConstellationConfiguration config = new ConstellationConfiguration(cxt);
        Constellation c = ConstellationFactory.createConstellation(p, config);
        c.activate();
        c.done();
    }

    @Test
    public void createDefault2() throws ConstellationCreationException {

        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");
        ConstellationProperties cp = new ConstellationProperties(p);

        Context cxt = new Context("DEFAULT", 0, 0);

        ConstellationConfiguration config = new ConstellationConfiguration(cxt);
        Constellation c = ConstellationFactory.createConstellation(cp, config);
        c.activate();
        c.done();
    }

    @Test
    public void createDefault3() throws ConstellationCreationException {

        Properties p = System.getProperties();
        p.put("ibis.constellation.distributed", "false");

        Context cxt = new Context("DEFAULT", 0, 0);

        ConstellationConfiguration config = new ConstellationConfiguration(cxt);
        Constellation c = ConstellationFactory.createConstellation(config);
        c.activate();
        c.done();
    }

    @Test
    public void createDefault4() throws ConstellationCreationException {

        Properties p = System.getProperties();
        p.put("ibis.constellation.distributed", "false");

        Context cxt = new Context("DEFAULT", 0, 0);

        ConstellationConfiguration config = new ConstellationConfiguration(cxt);
        Constellation c = ConstellationFactory.createConstellation(config, config);
        c.activate();
        c.done();
    }

    @Test
    public void createOne() throws ConstellationCreationException {
        int count = 1;

        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");

        Context cxt = new Context("DEFAULT", 0, 0);
        ConstellationConfiguration config = new ConstellationConfiguration(cxt);
        Constellation c = ConstellationFactory.createConstellation(p, config, count);

        c.activate();
        c.done();
    }

    @Test
    public void createFourTheSame() throws ConstellationCreationException {
        int count = 4;

        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");

        Context cxt = new Context("DEFAULT", 0, 0);
        ConstellationConfiguration config = new ConstellationConfiguration(cxt);
        Constellation c = ConstellationFactory.createConstellation(p, config, count);
        c.activate();
        c.done();
    }

    @Test
    public void createFourTheSame2() throws ConstellationCreationException {
        int count = 4;

        Properties p = System.getProperties();
        p.put("ibis.constellation.distributed", "false");

        Context cxt = new Context("DEFAULT", 0, 0);
        ConstellationConfiguration config = new ConstellationConfiguration(cxt);
        Constellation c = ConstellationFactory.createConstellation(config, count);
        c.activate();
        c.done();
    }

    @Test
    public void createFourDifferent() throws ConstellationCreationException {

        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");

        ConstellationConfiguration[] config = new ConstellationConfiguration[4];

        config[0] = new ConstellationConfiguration(new Context("ONE", 1, 1));
        config[1] = new ConstellationConfiguration(new Context("TWO", 2, 2));
        config[2] = new ConstellationConfiguration(new Context("THREE", 3, 3));
        config[3] = new ConstellationConfiguration(new Context("FOUR", 4, 4));

        Constellation c = ConstellationFactory.createConstellation(p, config);
        c.activate();
        c.done();
    }

}
