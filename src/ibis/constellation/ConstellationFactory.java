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
     * Creates a constellation instance, using the specified executor and the
     * system properties.
     *
     * @param e
     *            the executor
     * @return the constellation instance
     * @throws Exception
     *             TODO
     */
    public static Constellation createConstellation(Executor e)
            throws Exception {
        return createConstellation(System.getProperties(), e);
    }

    /**
     * Creates a constellation instance, using the specified executor and
     * properties.
     *
     * @param p
     *            the properties to use
     * @param e
     *            the executor
     * @return the constellation instance
     * @throws Exception
     *             TODO
     */
    public static Constellation createConstellation(Properties p, Executor e)
            throws Exception {
        return createConstellation(p, new Executor[] { e });
    }

    /**
     * Creates a constellation instance, using the specified executors and the
     * system properties.
     *
     * @param e
     *            the executors
     * @return the constellation instance
     * @throws IllegalArgumentException
     *             thrown when no executors are supplied
     * @throws Exception
     *             TODO
     */
    public static Constellation createConstellation(Executor... e)
            throws Exception {

        return createConstellation(System.getProperties(), e);
    }

    /**
     * Creates a constellation instance, using the specified executors and
     * properties.
     *
     * @param p
     *            the properties
     * @param e
     *            the executors
     * @return the constellation instance
     * @throws IllegalArgumentException
     *             thrown when no executors are supplied
     * @throws Exception
     *             TODO
     */
    public static Constellation createConstellation(Properties p, Executor... e)
            throws Exception {

        if (e == null || e.length == 0) {
            throw new IllegalArgumentException("Need at least one executor!");
        }

        // FIXME: We now create the whole stack by default. Should ask
        // properties what is needed!
        DistributedConstellation d = new DistributedConstellation(p);
        MultiThreadedConstellation m = new MultiThreadedConstellation(d, p);

        for (int i = 0; i < e.length; i++) {
            new SingleThreadedConstellation(m, e[i], p);
        }

        return d.getConstellation();
    }

}
