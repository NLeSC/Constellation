package ibis.constellation;

/**
 * This exception gets thrown when an executor calls a method that requires it to be embedded in a constellation instance, but is
 * not embedded yet.
 */
public class UnEmbeddedExecutorException extends RuntimeException {

    private static final long serialVersionUID = 5816385974217284589L;

    /**
     * Creates a UnEmbeddedExecutorException.
     *
     * @param s
     *            describes the reason.
     */
    public UnEmbeddedExecutorException(String s) {
        super(s);
    }

    /**
     * Creates a UnEmbeddedExecutorException.
     *
     * @param s
     *            describes the reason.
     * @param cause
     *            a nested exception that is the cause of this.
     */
    public UnEmbeddedExecutorException(String s, Throwable cause) {
        super(s, cause);
    }
}
