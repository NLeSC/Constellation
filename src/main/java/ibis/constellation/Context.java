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
package ibis.constellation;

/**
 * A <code>Context</code> represents a single context, associated with an executor or activity, and determines either a specific
 * type of activity that can be executed by an executor, or vice versa. A <code>Context</code> is characterized by a name,
 * typically a user-supplied string. A <code>Context</code> includes a range, which could for instance be used to indicate
 * priorities or size.
 */

public class Context extends AbstractContext {

    /* Generated */
    private static final long serialVersionUID = 4622096971913707525L;

    public static final Context DEFAULT = new Context("DEFAULT");

    private final String name;
    private final long rangeStart;
    private final long rangeEnd;

    public Context(String name, long rangeStart, long rangeEnd) {

        if (name == null) {
            throw new IllegalArgumentException("Name of Context may not be null!");
        }

        if (rangeEnd < rangeStart) {
            throw new IllegalArgumentException("Context range start must be smaller than range end!");
        }

        this.name = name;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
    }

    public Context(String name, long rank) {
        this(name, rank, rank);
    }

    public Context(String name) {
        this(name, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    /**
     * Returns the context name used to construct this Context.
     *
     * @return the context name.
     */
    public String getName() {
        return name;
    }

    public long getRangeStart() {
        return rangeStart;
    }

    public long getRangeEnd() {
        return rangeEnd;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (!(o instanceof Context)) {
            return false;
        }

        Context other = (Context) o;
        return (rangeStart == other.rangeStart && rangeEnd == other.rangeEnd && name.equals(other.name));
    }

    @Override
    public int hashCode() {
        return name.hashCode() ^ (int) ((rangeEnd ^ (rangeEnd >>> 32)) ^ (rangeStart ^ (rangeStart >>> 32)));
    }

    @Override
    public String toString() {
        return "Context(" + getName() + ", " + rangeStart + "-" + rangeEnd + ")";
    }
}
