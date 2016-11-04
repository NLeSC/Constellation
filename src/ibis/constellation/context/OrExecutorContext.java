package ibis.constellation.context;

/**
 * An <code>OrExecutorContext</code> represents an executor context that
 * consists of several (more than 1) unit executor contexts. This may represent
 * the fact that an executor may be able to execute more than one type of
 * activity.
 */
public final class OrExecutorContext extends ExecutorContext {

    private static final long serialVersionUID = -1202476921345669674L;

    private final UnitExecutorContext[] unitContexts;

    private final boolean ordered;

    /**
     * Constructs an executor context that consists of more than one unit
     * executor context.
     *
     * @param contexts
     *            the unit executor contexts
     * @param ordered
     *            whether the order within the unit executor contexts is
     *            significant.
     * @exception IllegalArgumentException
     *                is thrown when the length of the list of unit-contexts is
     *                smaller than 2.
     */
    public OrExecutorContext(UnitExecutorContext[] contexts, boolean ordered) {
        super();

        if (contexts == null || contexts.length < 2) {
            throw new IllegalArgumentException("Invalid arguments to "
                    + "OrContext: 2 or more contexts required!");
        }

        unitContexts = contexts.clone();
        this.ordered = ordered;
    }

    /**
     * Returns the number of unit-contexts of which this context exists.
     *
     * @return the number of unit-contexts.
     */
    public int size() {
        return unitContexts.length;
    }

    /**
     * Returns the unit-context corresponding to the specified index, or
     * <code>null</code> if it does not exist.
     *
     * @param index
     *            the index
     * @return the corresponding unit-context.
     */
    public UnitExecutorContext get(int index) {

        if (index >= 0 && index < unitContexts.length) {
            return unitContexts[index];
        } else {
            return null;
        }
    }

    /**
     * Returns the unit-contexts of which this executor context consists.
     *
     * @return the unit-contexts.
     */
    public UnitExecutorContext[] getContexts() {
        return unitContexts.clone();
    }

    /**
     * Returns whether the order of the unit-contexts within this context is
     * significant.
     *
     * @return whether the order of the unit-contexts is significant.
     */
    public boolean isOrdered() {
        return ordered;
    }

    @Override
    public String toString() {

        StringBuilder result = new StringBuilder("OrExecutorContext(");

        for (int i = 0; i < unitContexts.length - 1; i++) {
            result.append(unitContexts[i].toString());
            result.append(" or ");
        }

        result.append(unitContexts[unitContexts.length - 1].toString());
        result.append(")");
        return result.toString();
    }
}
