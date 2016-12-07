package ibis.constellation.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.StealStrategy;

public final class UnitActivityContext extends ActivityContext {

    private static final long serialVersionUID = 6134114690113562356L;

    private static final Logger log = LoggerFactory.getLogger(UnitActivityContext.class);

    public static final long DEFAULT_RANK = 0;

    public static final UnitActivityContext DEFAULT = new UnitActivityContext("DEFAULT", DEFAULT_RANK);

    private final String name;
    private final long rank;

    private final int hashCode;

    public UnitActivityContext(String name, long rank) {

        super();

        if (name == null) {
            throw new IllegalArgumentException("Context name cannot be null!");
        }

        this.name = name;
        this.rank = rank;
        this.hashCode = name.hashCode();
    }

    public String getName() {
        return name;
    }

    public long getRank() {
        return rank;
    }

    public UnitActivityContext(String name) {
        this(name, DEFAULT_RANK);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        UnitActivityContext other = (UnitActivityContext) obj;

        if (hashCode != other.hashCode) {
            return false;
        }

        return (rank == other.rank && name.equals(other.name));
    }

    @Override
    public String toString() {
        return "UnitActivityContext(" + name + ", " + rank + ")";
    }

    private boolean satisfiedBy(UnitExecutorContext offer, StealStrategy s) {

        if (!name.equals(offer.getName())) {
            return false;
        }
        if (log.isDebugEnabled()) {
            log.debug("Matching context string: " + name);
        }

        switch (s.getStrategy()) {
        case StealStrategy._BIGGEST:
        case StealStrategy._SMALLEST:
        case StealStrategy._ANY:
            return true;

        case StealStrategy._VALUE:
            return (rank == s.getValue());

        case StealStrategy._RANGE:
            return (rank >= s.getStartOfRange() && rank <= s.getEndOfRange());
        }

        return false;
    }

    private boolean satisfiedBy(OrExecutorContext offer, StealStrategy s) {

        for (int i = 0; i < offer.size(); i++) {

            UnitExecutorContext c = offer.get(i);

            if (satisfiedBy(c, s)) {
                return true;
            }

        }

        return false;
    }

    @Override
    public boolean satisfiedBy(ExecutorContext offer, StealStrategy s) {

        // This does NOT take the rank into account.
        if (offer == null) {
            return false;
        }

        if (offer instanceof UnitExecutorContext) {
            return satisfiedBy((UnitExecutorContext) offer, s);
        }

        assert (offer instanceof OrExecutorContext);
        return satisfiedBy((OrExecutorContext) offer, s);
    }
}
