package ibis.constellation;

/**
 * The <code>Concluder</code> interface provides a hook that is invoked when all
 * constellation instances of a pool have decided to terminate, see
 * {@link Constellation#done(Concluder)}. This allows applications, for
 * instance, to collect statistics.
 */
public interface Concluder {

    /**
     * This method is invoked when all instances of a constellation pool have
     * decided to terminate, see {@link Constellation#done(Concluder)}. TODO
     * what happens when this method throws an exception?
     */
    public void conclude();
}
