package ibis.constellation;

import java.util.Properties;

import ibis.constellation.impl.DistributedConstellation;
import ibis.constellation.impl.MultiThreadedConstellation;
import ibis.constellation.impl.SingleThreadedConstellation;

/**
 * The <code>ConstellationFactory</code> provides several static methods to
 * create a {@link Constellation} instance.
 */
public class ConstellationFactory {

    /**
     * Prevent instantiation of this object type.
     */
    private ConstellationFactory() {
        // nothing
    }

    /**
     * Creates a constellation instance, using the specified executor and the
     * system properties.
     *
     * If the system property <code>ibis.constellation.distributed</code> is not
     * set, or set to "true", a distributed constellation instance is created.
     * If it is set to "false", a singlethreaded constellation is created.
     * Otherwise, it is apparently set to an unrecognized value, so an
     * IllegalArgumentException exception is thrown.
     *
     * @param e
     *            the executor
     * @return the constellation instance
     * @throws IllegalArgumentException
     *             thrown when no executors are supplied, or in case of
     *             incorrect property values.
     * @throws ConstellationCreationException
     *             thrown when the constellation instance could not be created
     *             for some reason.
     */
    public static Constellation createConstellation(Executor e)
            throws ConstellationCreationException {
        return createConstellation(System.getProperties(), e);
    }

    /**
     * Creates a constellation instance, using the specified executor and
     * properties.
     *
     * If the property <code>ibis.constellation.distributed</code> is not set,
     * or set to "true", a distributed constellation instance is created. If it
     * is set to "false", a singlethreaded constellation is created. Otherwise,
     * it is apparently set to an unrecognized value, so an
     * IllegalArgumentException exception is thrown.
     *
     * @param p
     *            the properties to use
     * @param e
     *            the executor
     * @return the constellation instance
     * @throws IllegalArgumentException
     *             thrown when no executors are supplied, or in case of
     *             incorrect property values.
     * @throws ConstellationCreationException
     *             thrown when the constellation instance could not be created
     *             for some reason.
     */
    public static Constellation createConstellation(Properties p, Executor e)
            throws ConstellationCreationException {
        return createConstellation(p, new Executor[] { e });
    }

    /**
     * Creates a constellation instance, using the specified executors and the
     * system properties.
     *
     * If the system property <code>ibis.constellation.distributed</code> is not
     * set, or set to "true", a distributed constellation instance is created.
     * If it is set to "false", depending on the number of executors, either a
     * multithreaded constellation or a singlethreaded constellation is created.
     * Otherwise, it is apparently set to an unrecognized value, so an
     * IllegalArgumentException exception is thrown.
     *
     * @param e
     *            the executors
     * @return the constellation instance
     * @throws IllegalArgumentException
     *             thrown when no executors are supplied, or in case of
     *             incorrect property values.
     * @throws ConstellationCreationException
     *             thrown when the constellation instance could not be created
     *             for some reason.
     */
    public static Constellation createConstellation(Executor... e)
            throws ConstellationCreationException {

        return createConstellation(System.getProperties(), e);
    }

    /**
     * Creates a constellation instance, using the specified executors and
     * properties.
     *
     * If the property <code>ibis.constellation.distributed</code> is not set,
     * or set to "true", a distributed constellation instance is created. If it
     * is set to "false", depending on the number of executors, either a
     * multithreaded constellation or a singlethreaded constellation is created.
     * Otherwise, it is apparently set to an unrecognized value, so an
     * IllegalArgumentException exception is thrown.
     *
     * @param p
     *            the properties
     * @param e
     *            the executors
     * @return the constellation instance
     * @throws IllegalArgumentException
     *             thrown when no executors are supplied, or in case of
     *             incorrect property values.
     * @throws ConstellationCreationException
     *             thrown when the constellation instance could not be created
     *             for some reason.
     */
    public static Constellation createConstellation(Properties p, Executor... e)
            throws ConstellationCreationException {

        if (e == null || e.length == 0) {
            throw new IllegalArgumentException("Need at least one executor!");
        }

        ConstellationProperties props;

        if (p instanceof ConstellationProperties) {
            props = (ConstellationProperties) p;
        } else {
            props = new ConstellationProperties(p);
        }

        boolean needsDistributed = props.DISTRIBUTED;

        DistributedConstellation d = needsDistributed
                ? new DistributedConstellation(props) : null;

        MultiThreadedConstellation m = (needsDistributed || e.length > 1)
                ? new MultiThreadedConstellation(d, props) : null;

        SingleThreadedConstellation s = null;

        for (int i = 0; i < e.length; i++) {
            s = new SingleThreadedConstellation(m, e[i], props);
        }

        return d != null ? d.getConstellation()
                : m != null ? m.getConstellation() : s.getConstellation();
    }

}
