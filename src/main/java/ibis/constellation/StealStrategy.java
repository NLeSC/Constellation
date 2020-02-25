/*
 * Copyright 2019 Vrije Universiteit Amsterdam
 *                Netherlands eScience Center
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

import java.io.Serializable;

/**
 * A <code>StealStrategy</code> describes a strategy, to be used by an executor, for stealing activities.
 *
 * Activities can be sorted by their Context range, and an executor can have, for instance, a preference for "big" jobs or "small"
 * jobs, or jobs with a rank within a particular range. The strategies are described by particular opcodes, some of which have
 * additional attributes.
 */
public final class StealStrategy implements Serializable {

    private static final long serialVersionUID = 8376483895062977483L;

    /** Predefined "steal activity with highest range" strategy. */
    public static final StealStrategy BIGGEST = new StealStrategy(true);

    /** Predefined "steal activity with lowest range" strategy. */
    public static final StealStrategy SMALLEST = new StealStrategy(false);

    /** The strategy, either biggest (true) or smallest (false) */
    private final boolean biggest;

    /**
     * Constructs a steal strategy object with the specified opcode.
     *
     * @param opcode
     *            the opcode
     * @exception IllegalArgumentException
     *                is thrown in case of an unknown opcode or when a opcode is specified that requires a value or range.
     */
    private StealStrategy(boolean biggest) {
        this.biggest = biggest;        
    }

    @Override
    public String toString() {
        if (biggest) { 
            return "BIGGEST";
        } else { 
            return "SMALLEST";
        }
    }

    /**
     * Returns the strategy opcode of this strategy, one of {@link #BIGGEST}, {@link #SMALLEST}.
     *
     * @return the strategy opcode.
     */
    //public byte getStrategy() {
    //    return strategy;
   // }

    @Override
    public int hashCode() {
        return (biggest ? 1 : 2);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof StealStrategy)) {
            return false;
        }
        StealStrategy s = (StealStrategy) o;
        return biggest == s.biggest;
    }
}
