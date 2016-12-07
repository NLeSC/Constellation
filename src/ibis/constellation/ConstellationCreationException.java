package ibis.constellation;

/**
 * This exception gets thrown when a constellation instance could not be created
 * for some reason.
 */
public class ConstellationCreationException extends Exception {

    private static final long serialVersionUID = 5816385974217284589L;

    /**
     * Creates a ConstellationCreationException.
     *
     * @param s
     *            describes the reason.
     */
    public ConstellationCreationException(String s) {
        super(s);
    }

    /**
     * Creates a ConstellationCreationException.
     *
     * @param s
     *            describes the reason.
     * @param cause
     *            a nested exception that is the cause of this.
     */
    public ConstellationCreationException(String s, Throwable cause) {
        super(s, cause);
    }
}
