package ibis.constellation;

import java.io.Serializable;

/**
 * A <code>StealStrategy</code> describes a strategy, to be used by an executor,
 * for stealing activities.
 *
 * Activities can be sorted by their
 * {@link ibis.constellation.context.UnitActivityContext#rank rank}, and an
 * executor can have, for instance, a preference for "big" jobs or "small" jobs,
 * or jobs with a rank within a particular range. The strategies are described
 * by particular opcodes, some of which have additional attributes.
 */
public final class StealStrategy implements Serializable {

    private static final long serialVersionUID = 8376483895062977483L;

    /** Opcode describing the "steal activity with biggest rank" strategy. */
    public static final byte _BIGGEST = 1;

    /** Opcode describing the "steal activity with smallest rank" strategy. */
    public static final byte _SMALLEST = 2;

    /** Opcode describing the "steal activity with specific rank" strategy. */
    public static final byte _VALUE = 3;

    /**
     * Opcode describing the "steal activity with a rank in a specific range"
     * strategy.
     */
    public static final byte _RANGE = 4;

    /** Opcode describing the "don't care" strategy. */
    public static final byte _ANY = 5;

    /** Predefined "don't care" strategy. */
    public static final StealStrategy ANY = new StealStrategy(_ANY);

    /** Predefined "steal activity with biggest rank" strategy. */
    public static final StealStrategy BIGGEST = new StealStrategy(_BIGGEST);

    /** Predefined "steal activity with smallest rank" strategy. */
    public static final StealStrategy SMALLEST = new StealStrategy(_SMALLEST);

    /** The strategy. */
    private final byte strategy;

    /** Start of the specific range, if present. */
    private final long start;

    /** End of the specific range, if present. */
    private final long end;

    /**
     * Constructs a steal strategy object with the specified opcode.
     *
     * @param opcode
     *            the opcode
     * @exception IllegalArgumentException
     *                is thrown in case of an unknown opcode or when a opcode is
     *                specified that requires a value or range.
     */
    public StealStrategy(byte opcode) {

        switch (opcode) {
        case _BIGGEST:
        case _SMALLEST:
        case _ANY:
            break;
        case _VALUE:
            throw new IllegalArgumentException("Value required!");
        case _RANGE:
            throw new IllegalArgumentException("Range required!");
        default:
            throw new IllegalArgumentException("Unknown opcode!");
        }

        strategy = opcode;
        start = end = 0;
    }

    /**
     * Constructs a steal strategy object with the specified opcode and value.
     *
     * @param opcode
     *            the opcode
     * @param value
     *            the rank value
     * @exception IllegalArgumentException
     *                is thrown in case of an unknown opcode or when a opcode is
     *                specified that requires a range or no value.
     */
    public StealStrategy(byte opcode, long value) {

        switch (opcode) {
        case _BIGGEST:
        case _SMALLEST:
        case _ANY:
            throw new IllegalArgumentException("No value allowed!");
        case _VALUE:
            break;
        case _RANGE:
            throw new IllegalArgumentException("Range required!");
        default:
            throw new IllegalArgumentException("Unknown opcode!");
        }

        strategy = _VALUE;
        start = end = value;
    }

    /**
     * Constructs a steal strategy object with the specified opcode and range.
     *
     * @param opcode
     *            the opcode
     * @param start
     *            the start value of the range
     * @param end
     *            the end value of the range
     * @exception IllegalArgumentException
     *                is thrown in case of an unknown opcode or when a opcode is
     *                specified that requires a single value or no value.
     */
    public StealStrategy(byte opcode, long start, long end) {

        switch (opcode) {
        case _BIGGEST:
        case _SMALLEST:
        case _ANY:
        case _VALUE:
            throw new IllegalArgumentException("No range allowed!");
        case _RANGE:
            break;
        default:
            throw new IllegalArgumentException("Unknown opcode!");
        }

        strategy = _RANGE;
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {

        switch (getStrategy()) {
        case _BIGGEST:
            return "BIGGEST";
        case _SMALLEST:
            return "SMALLEST";
        case _ANY:
            return "ANY";
        case _VALUE:
            return "VALUE(" + getValue() + ")";
        case _RANGE:
            return "RANGE(" + getStartOfRange() + " - " + getEndOfRange() + ")";
        default:
            return "UNKNOWN";
        }
    }

    /**
     * Returns the strategy opcode of this strategy, one of {@link #_ANY},
     * {@link #_BIGGEST}, {@link #_RANGE}, {@link #_SMALLEST}, {@link #_VALUE}.
     *
     * @return the strategy opcode.
     */
    public byte getStrategy() {
        return strategy;
    }

    /**
     * Returns the start of the rank range in case of a {@link #_RANGE}
     * strategy.
     *
     * @return the start of the rank range.
     */
    public long getStartOfRange() {
        return start;
    }

    /**
     * Returns the rank value in case of a {@link #_VALUE} strategy.
     *
     * @return the rank value.
     */
    public long getValue() {
        return start;
    }

    /**
     * Returns the end of the rank range in case of a {@link #_RANGE} strategy.
     *
     * @return the end of the rank range.
     */
    public long getEndOfRange() {
        return end;
    }
}
