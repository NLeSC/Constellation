package ibis.constellation.util;

/**
 * Utility to convert memory sizes to strings in a reasonable format.
 */
public class MemorySizes {

    public static final long KB = 1024;
    public static final long MB = 1024 * KB;
    public static final long GB = 1024 * MB;

    private static final String[] units = new String[] { "", "k", "M", "G", "T" };

    /**
     * Converts the specified memory size to a string.
     *
     * @param bytes
     *            the memory size
     * @return the string
     */
    public static String toStringBytes(long bytes) {
        long b = bytes;
        for (String unit : units) {
            if (b / (1024 * 10) == 0) {
                return b + unit + "B";
            } else {
                b /= 1024;
            }
        }
        return b + units[units.length - 1] + "B";
    }

}
