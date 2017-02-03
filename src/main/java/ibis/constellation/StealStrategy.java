package ibis.constellation;

import java.io.Serializable;

/**
 * A <code>StealStrategy</code> describes a strategy, to be used by an executor, for stealing activities.
 *
 * Activities can be sorted by their {@link ibis.constellation.context.UnitActivityContext#rank rank}, and an executor can have,
 * for instance, a preference for "big" jobs or "small" jobs, or jobs with a rank within a particular range. The strategies are
 * described by particular opcodes, some of which have additional attributes.
 */
public final class StealStrategy implements Serializable {

    private static final long serialVersionUID = 8376483895062977483L;

    /** Opcode describing the "steal activity with biggest rank" strategy. */
    public static final byte _BIGGEST = 1;

    /** Opcode describing the "steal activity with smallest rank" strategy. */
    public static final byte _SMALLEST = 2;

    /** Predefined "steal activity with biggest rank" strategy. */
    public static final StealStrategy BIGGEST = new StealStrategy(_BIGGEST);

    /** Predefined "steal activity with smallest rank" strategy. */
    public static final StealStrategy SMALLEST = new StealStrategy(_SMALLEST);

    /** The strategy. */
    private final byte strategy;

    /**
     * Constructs a steal strategy object with the specified opcode.
     *
     * @param opcode
     *            the opcode
     * @exception IllegalArgumentException
     *                is thrown in case of an unknown opcode or when a opcode is specified that requires a value or range.
     */
    public StealStrategy(byte opcode) {

        switch (opcode) {
        case _BIGGEST:
        case _SMALLEST:
            strategy = opcode;
            return;
        default:
            throw new IllegalArgumentException("Unknown opcode!");
        }
    }
    @Override
    public String toString() {

        switch (getStrategy()) {
        case _BIGGEST:
            return "BIGGEST";
        case _SMALLEST:
            return "SMALLEST";
        default:
            return "UNKNOWN";
        }
    }

    /**
     * Returns the strategy opcode of this strategy, one of {@link #_ANY}, {@link #_BIGGEST}, {@link #_RANGE}, {@link #_SMALLEST},
     * {@link #_VALUE}.
     *
     * @return the strategy opcode.
     */
    public byte getStrategy() {
        return strategy;
    }
}
