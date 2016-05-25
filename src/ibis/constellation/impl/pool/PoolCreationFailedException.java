package ibis.constellation.impl.pool;

public class PoolCreationFailedException extends Exception {

    public PoolCreationFailedException(String s) {
        super(s);
    }

    public PoolCreationFailedException(String s, Throwable cause) {
        super(s, cause);
    }

}
