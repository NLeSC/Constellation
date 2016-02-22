package ibis.constellation.context;

import java.util.Arrays;
import java.util.Comparator;

import ibis.constellation.ActivityContext;
import ibis.constellation.ExecutorContext;
import ibis.constellation.StealStrategy;

public class OrActivityContext extends ActivityContext {

    private static final long serialVersionUID = -1202476921345669674L;

    protected final UnitActivityContext[] unitContexts;

    protected final int hashCode;
    protected final boolean ordered;

    protected static class UnitActivityContextSorter
            implements Comparator<UnitActivityContext> {

        public int compare(UnitActivityContext u1, UnitActivityContext u2) {

            if (u1.hashCode == u2.hashCode) {

                if (u1.rank == u2.rank) {
                    return 0;
                } else if (u1.rank < u2.rank) {
                    return -1;
                } else {
                    return 1;
                }

            } else if (u1.hashCode < u2.hashCode) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private static UnitActivityContext[] sort(UnitActivityContext[] in) {
        Arrays.sort(in, new UnitActivityContextSorter());
        return in;
    }

    private static int generateHash(UnitActivityContext[] in) {

        // NOTE: result depends on order of elements in array!
        // NOTE: does not take rank into account

        int hashCode = 1;

        for (int i = 0; i < in.length; i++) {
            hashCode = 31 * hashCode + (in[i] == null ? 0 : in[i].hashCode);
        }

        return hashCode;
    }

    public OrActivityContext(UnitActivityContext[] unit, boolean ordered) {
        super();

        if (unit == null || unit.length < 2) {
            throw new IllegalArgumentException("Invalid arguments to "
                    + "OrContext: 2 or more contexts required!");
        }

        unitContexts = unit;
        this.ordered = ordered;

        if (!ordered) {
            // When the OrContext is unordered, the order of the elements is
            // unimportant.
            // We therefore sort it to get a uniform order, regardless of the
            // user defined
            // order.
            sort(unitContexts);
        }

        hashCode = 31 * generateHash(unitContexts);
    }

    public OrActivityContext(UnitActivityContext[] unit) {
        this(unit, false);
    }

    public int size() {
        return unitContexts.length;
    }

    public UnitActivityContext get(int index) {
        return unitContexts[index];
    }

    public boolean contains(UnitActivityContext u) {

        // TODO: use the fact that the unitContexts are sorted!!!!
        for (int i = 0; i < unitContexts.length; i++) {
            if (u.equals(unitContexts[i])) {
                return true;
            }
        }

        return false;
    }

    public int countUnitContexts() {
        return unitContexts.length;
    }

    public UnitActivityContext[] getContexts() {
        return unitContexts.clone();
    }

    @Override
    public boolean isOr() {
        return true;
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

        if (offer.isUnit()) {
            return satisfiedBy((UnitExecutorContext) offer, s);
        }

        if (offer.isOr()) {
            return satisfiedBy((OrExecutorContext) offer, s);
        }

        return false;
    }

}
