package ibis.constellation.impl.pool;

public class PoolCreationFailedException extends Exception {

    private static final long serialVersionUID = 9185043786975234642L;

    public PoolCreationFailedException(String s) {
        super(s);
    }

    public PoolCreationFailedException(String s, Throwable cause) {
        super(s, cause);
    }

}
