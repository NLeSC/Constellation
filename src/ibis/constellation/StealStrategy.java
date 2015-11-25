package ibis.constellation;

import java.io.Serializable;

public class StealStrategy implements Serializable {

    private static final long serialVersionUID = 8376483895062977483L;

    public static final byte _BIGGEST = 1;
    public static final byte _SMALLEST = 2;
    public static final byte _VALUE = 3;
    public static final byte _RANGE = 4;
    public static final byte _ANY = 5;

    public static final StealStrategy ANY = new StealStrategy(_ANY);
    public static final StealStrategy BIGGEST = new StealStrategy(_BIGGEST);
    public static final StealStrategy SMALLEST = new StealStrategy(_SMALLEST);

    public final byte strategy;
    public final long start;
    public final long end;

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

        switch (strategy) {
        case _BIGGEST:
            return "BIGGEST";
        case _SMALLEST:
            return "SMALLEST";
        case _ANY:
            return "ANY";
        case _VALUE:
            return "VALUE(" + start + ")";
        case _RANGE:
            return "RANGE(" + start + " - " + end + ")";
        default:
            return "UNKNOWN";
        }
    }
}
