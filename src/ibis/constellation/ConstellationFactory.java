package ibis.constellation;

import ibis.constellation.impl.DistributedConstellation;
import ibis.constellation.impl.MultiThreadedConstellation;
import ibis.constellation.impl.SingleThreadedConstellation;

import java.util.Properties;

public class ConstellationFactory {

    public static Constellation createConstellation(Executor e)
            throws Exception {
        return createConstellation(System.getProperties(), e);
    }

    public static Constellation createConstellation(Properties p, Executor e)
            throws Exception {
        return createConstellation(p, new Executor[] { e });
    }

    public static Constellation createConstellation(Executor... e)
            throws Exception {

        return createConstellation(System.getProperties(), e);
    }

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
