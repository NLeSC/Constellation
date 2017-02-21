package ibis.constellation;

/**
 * This exception gets thrown when an activity is submitted from which constellation can figure out that there will not be a
 * suitable executor that can execute it.
 *
 */
public class NoSuitableExecutorException extends Exception {

    private static final long serialVersionUID = 5816385974217284589L;

    /**
     * Creates a NoSuitableExecutorException.
     *
     * @param s
     *            describes the reason.
     */
    public NoSuitableExecutorException(String s) {
        super(s);
    }

    /**
     * Creates a NoSuitableExecutorException.
     *
     * @param s
     *            describes the reason.
     * @param cause
     *            a nested exception that is the cause of this.
     */
    public NoSuitableExecutorException(String s, Throwable cause) {
        super(s, cause);
    }
}
