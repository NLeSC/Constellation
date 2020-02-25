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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StealStrategyTest {

    @Test
    public void stealStrategyBiggestToString() {
        StealStrategy large = StealStrategy.BIGGEST;
        assertTrue(large.toString().equals("BIGGEST"));
    }

    @Test
    public void stealStrategySmallestToString() {
        StealStrategy large = StealStrategy.SMALLEST;
        assertTrue(large.toString().equals("SMALLEST"));
    }

    @Test
    public void testHashCodeBiggest() {
        assertEquals(StealStrategy.BIGGEST.hashCode(), 1);
    }

    @Test
    public void testHashCodeSmallest() {
        assertEquals(StealStrategy.SMALLEST.hashCode(), 2);
    }

    @Test
    public void testEqualsLarge() {
        assertTrue(StealStrategy.BIGGEST.equals(StealStrategy.BIGGEST));
    }

    @Test
    public void testEqualsSmall() {
        assertTrue(StealStrategy.SMALLEST.equals(StealStrategy.SMALLEST));
    }

    @Test
    public void testEqualsSmallBig() {
        assertFalse(StealStrategy.BIGGEST.equals(StealStrategy.SMALLEST));
    }

    @Test
    public void testEqualsNull() {
        StealStrategy s = null;
        assertFalse(StealStrategy.BIGGEST.equals(s));
    }

    @Test
    public void testEqualsWrongType() {
        assertFalse(StealStrategy.BIGGEST.equals("hello world"));
    }
}
