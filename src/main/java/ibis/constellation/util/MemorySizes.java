/*
 * Copyright 2018 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
