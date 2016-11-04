package ibis.constellation.context;

import java.util.Arrays;
import java.util.Comparator;

import ibis.constellation.StealStrategy;

/**
 * An <code>OrActivityContext</code> represents an activity context that
 * consists of several (more than 1) unit activity contexts. This may represent
 * the fact that an activity may be executed by more than one type of executor.
 */
public final class OrActivityContext extends ActivityContext {

    private static final long serialVersionUID = -1202476921345669674L;

    private final UnitActivityContext[] unitContexts;

    private final int hashCode;
    private final boolean ordered;

    private static class UnitActivityContextSorter
            implements Comparator<UnitActivityContext> {

        public int compare(UnitActivityContext u1, UnitActivityContext u2) {

            if (u1.hashCode() == u2.hashCode()) {

                if (u1.getRank() == u2.getRank()) {
                    return 0;
                } else if (u1.getRank() < u2.getRank()) {
                    return -1;
                } else {
                    return 1;
                }

            } else if (u1.hashCode() < u2.hashCode()) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private static int generateHash(UnitActivityContext[] in) {

        // NOTE: result depends on order of elements in array!
        // NOTE: does not take rank into account

        int hashCode = 1;

        for (int i = 0; i < in.length; i++) {
            hashCode = 31 * hashCode + (in[i] == null ? 0 : in[i].hashCode());
        }

        return hashCode;
    }

    /**
     * Constructs an activity context consisting of a list of unit-contexts.
     *
     * @param unit
     *            the list of unit-contexts.
     * @param ordered
     *            whether the order of the unit-contexts is significant
     * @exception IllegalArgumentException
     *                is thrown when the length of the list of unit-contexts is
     *                smaller than 2.
     */
    public OrActivityContext(UnitActivityContext[] unit, boolean ordered) {
        super();

        if (unit == null || unit.length < 2) {
            throw new IllegalArgumentException("Invalid arguments to "
                    + "OrContext: 2 or more contexts required!");
        }

        unitContexts = unit.clone();
        this.ordered = ordered;

        if (!ordered) {
            // When the OrContext is unordered, the order of the elements is
            // unimportant. We therefore sort it to get a uniform order,
            // regardless of the user defined order.
            Arrays.sort(unitContexts, new UnitActivityContextSorter());
        }

        hashCode = generateHash(unitContexts);
    }

    /**
     * Constructs an activity context consisting of a list of unit-contexts. The
     * order in the list is not significant.
     *
     * @param unit
     *            the list of unit-contexts.
     * @exception IllegalArgumentException
     *                is thrown when the length of the list of unit-contexts is
     *                smaller than 2.
     */
    public OrActivityContext(UnitActivityContext[] unit) {
        this(unit, false);
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
    public UnitActivityContext get(int index) {
        if (index < 0 || index >= unitContexts.length) {
            return null;
        }
        return unitContexts[index];
    }

    /**
     * Determines whether the provided unit-context is a member of this activity
     * context, and returns <code>true</code> if it is, <code>false</code>
     * otherwise.
     *
     * @param u
     *            the unit-context
     * @return whether the unit-context is a member of this activity context.
     */
    public boolean contains(UnitActivityContext u) {
        for (int i = 0; i < unitContexts.length; i++) {
            if (u.equals(unitContexts[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the unit-contexts of which this activity context consists.
     *
     * @return the unit-contexts.
     */
    public UnitActivityContext[] getContexts() {
        return unitContexts.clone();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {

        // NOTE: potentially very expensive operations, especially with large
        // contexts that are equal

        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        OrActivityContext other = (OrActivityContext) obj;

        if (hashCode != other.hashCode) {
            return false;
        }

        if (unitContexts.length != other.unitContexts.length) {
            return false;
        }

        for (int i = 0; i < unitContexts.length; i++) {
            if (!other.contains(unitContexts[i])) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {

        StringBuilder b = new StringBuilder();

        b.append("OrContext(");

        for (int i = 0; i < unitContexts.length; i++) {
            b.append(unitContexts[i]);

            if (i != unitContexts.length - 1) {
                b.append(" or ");
            }
        }

        b.append(")");

        return b.toString();
    }

    private boolean satisfiedBy(UnitExecutorContext offer, StealStrategy s) {

        for (int i = 0; i < unitContexts.length; i++) {

            UnitActivityContext tmp = unitContexts[i];

            if (tmp.satisfiedBy(offer, s)) {
                return true;
            }
        }

        return false;
    }

    private boolean satisfiedBy(OrExecutorContext offer, StealStrategy s) {

        UnitExecutorContext[] tmp = offer.getContexts();

        for (int i = 0; i < tmp.length; i++) {

            if (satisfiedBy(tmp[i], s)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean satisfiedBy(ExecutorContext offer, StealStrategy s) {

        if (offer == null) {
            return false;
        }

        if (offer instanceof UnitExecutorContext) {
            return satisfiedBy((UnitExecutorContext) offer, s);
        }

        assert(offer instanceof OrExecutorContext);
        return satisfiedBy((OrExecutorContext) offer, s);
    }

}
