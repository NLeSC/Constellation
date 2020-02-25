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

import java.util.Properties;

import ibis.constellation.impl.DistributedConstellation;
import ibis.constellation.impl.MultiThreadedConstellation;
import ibis.constellation.impl.SingleThreadedConstellation;

/**
 * The <code>ConstellationFactory</code> provides several static methods to create a {@link Constellation} instance.
 */
public class ConstellationFactory {
   
    /**
     * Creates a constellation instance, using the specified executor and the system properties.
     *
     * If the system property <code>ibis.constellation.distributed</code> is not set, or set to "true", a distributed
     * constellation instance is created. If not, a singlethreaded constellation is created.
     *
     * @param e
     *            the executor
     * @return the constellation instance
     * @throws IllegalArgumentException
     *             thrown when no executors are supplied, or in case of incorrect property values.
     * @throws ConstellationCreationException
     *             thrown when the constellation instance could not be created for some reason.
     */
    public static Constellation createConstellation(ConstellationConfiguration e) throws ConstellationCreationException {
        return createConstellation(System.getProperties(), e);
    }

    /**
     * Creates a constellation instance, using the specified executor and the system properties.
     *
     * If the system property <code>ibis.constellation.distributed</code> is not set, or set to "true", a distributed
     * constellation instance is created. If not, a singlethreaded constellation is created.
     *
     * @param e
     *            the executor
     * @return the constellation instance
     * @throws IllegalArgumentException
     *             thrown when no executors are supplied, or in case of incorrect property values.
     * @throws ConstellationCreationException
     *             thrown when the constellation instance could not be created for some reason.
     */
    public static Constellation createConstellation(ConstellationConfiguration e, int count)
            throws ConstellationCreationException {
        return createConstellation(System.getProperties(), e, count);
    }

    /**
     * Creates a constellation instance, using the specified executor and properties.
     *
     * If the property <code>ibis.constellation.distributed</code> is not set, or set to "true", a distributed constellation
     * instance is created. If not, a singlethreaded constellation is created.
     *
     * @param p
     *            the properties to use
     * @param e
     *            the executor
     * @return the constellation instance
     * @throws IllegalArgumentException
     *             thrown when no executors are supplied, or in case of incorrect property values.
     * @throws ConstellationCreationException
     *             thrown when the constellation instance could not be created for some reason.
     */
    public static Constellation createConstellation(Properties p, ConstellationConfiguration e)
            throws ConstellationCreationException {
        return createConstellation(p, new ConstellationConfiguration[] { e });
    }

    /**
     * Creates a constellation instance, using the specified executor and properties.
     *
     * If the property <code>ibis.constellation.distributed</code> is not set, or set to "true", a distributed constellation
     * instance is created. If not, a singlethreaded constellation is created.
     *
     * @param p
     *            the properties to use
     * @param e
     *            the executor
     * @return the constellation instance
     * @throws IllegalArgumentException
     *             thrown when no executors are supplied, or in case of incorrect property values.
     * @throws ConstellationCreationException
     *             thrown when the constellation instance could not be created for some reason.
     */
    public static Constellation createConstellation(Properties p, ConstellationConfiguration e, int count)
            throws ConstellationCreationException {

        ConstellationConfiguration[] config = new ConstellationConfiguration[count];

        for (int i = 0; i < count; i++) {
            config[i] = e;
        }

        return createConstellation(p, config);
    }

    /**
     * Creates a constellation instance, using the specified executors and the system properties.
     *
     * If the system property <code>ibis.constellation.distributed</code> is not set, or set to "true", a distributed
     * constellation instance is created. If not, depending on the number of executors, either a multithreaded constellation or a
     * singlethreaded constellation is created.
     *
     * @param e
     *            the executors
     * @return the constellation instance
     * @throws IllegalArgumentException
     *             thrown when no executors are supplied, or in case of incorrect property values.
     * @throws ConstellationCreationException
     *             thrown when the constellation instance could not be created for some reason.
     */
    public static Constellation createConstellation(ConstellationConfiguration... e) throws ConstellationCreationException {

        return createConstellation(System.getProperties(), e);
    }

    /**
     * Creates a constellation instance, using the specified executors and properties.
     *
     * If the property <code>ibis.constellation.distributed</code> is not set, or set to "true", a distributed constellation
     * instance is created. If not, depending on the number of executors, either a multithreaded constellation or a singlethreaded
     * constellation is created.
     *
     * @param p
     *            the properties
     * @param e
     *            the executors
     * @return the constellation instance
     * @throws IllegalArgumentException
     *             thrown when no executors are supplied, or in case of incorrect property values.
     * @throws ConstellationCreationException
     *             thrown when the constellation instance could not be created for some reason.
     */
    public static Constellation createConstellation(Properties p, ConstellationConfiguration... e)
            throws ConstellationCreationException {

        ConstellationProperties props;

        if (p instanceof ConstellationProperties) {
            props = (ConstellationProperties) p;
        } else {
            props = new ConstellationProperties(p);
        }

        boolean needsDistributed = props.DISTRIBUTED;

        return createConstellation(needsDistributed, props, e);
    }

    /**
     * Creates a constellation instance, using the specified executors and properties.
     *
     * If the <code>needsDistributed</code> parameter is set, a distributed constellation instance is created. If it is not set,
     * depending on the number of executors, either a multithreaded constellation or a singlethreaded constellation is created.
     *
     * @param needsDistributed
     *            when set, a distributed constellation instance is created
     * @param props
     *            the properties
     * @param e
     *            the executors
     * @return the constellation instance
     * @throws IllegalArgumentException
     *             thrown when no executors are supplied, or in case of incorrect property values.
     * @throws ConstellationCreationException
     *             thrown when the constellation instance could not be created for some reason.
     */
    private static Constellation createConstellation(boolean needsDistributed, ConstellationProperties props,
            ConstellationConfiguration... c) throws ConstellationCreationException {

        DistributedConstellation d = null;

        if (c == null || c.length == 0) {
            throw new IllegalArgumentException("Need at least one Constellation configuration!");
        }

        for (ConstellationConfiguration tmp : c) {
            if (tmp == null) {
                throw new IllegalArgumentException("Constellation configuration may not be null");
            }
        }

        if (needsDistributed) {
            d = new DistributedConstellation(props, c);
            return d.getConstellation();
        }

        MultiThreadedConstellation m = null;

        if (c.length > 1) {
            m = new MultiThreadedConstellation(null, props, c);
            return m.getConstellation();
        }

        SingleThreadedConstellation s = new SingleThreadedConstellation(null, c[0], props);
        return s.getConstellation();
    }

}
