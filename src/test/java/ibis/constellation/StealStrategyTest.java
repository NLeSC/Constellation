/**
 * Copyright 2013 Netherlands eScience Center
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StealStrategyTest {

    @Test(expected = IllegalArgumentException.class)
    public void createStealStrategyFail() {
        // Should throw a IllegalArgumentException
        new StealStrategy((byte) 100);
    }

    @Test
    public void stealStrategyBiggest() {
        StealStrategy large = new StealStrategy(StealStrategy._BIGGEST);
        assertTrue(large.getStrategy() == StealStrategy._BIGGEST);
    }

    @Test
    public void stealStrategySmallest() {
        StealStrategy small = new StealStrategy(StealStrategy._SMALLEST);
        assertTrue(small.getStrategy() == StealStrategy._SMALLEST);
    }

    @Test
    public void testHashCodeLarge() {
        StealStrategy large = new StealStrategy(StealStrategy._BIGGEST);
        assertEquals(large.hashCode(), StealStrategy.BIGGEST.hashCode());
    }

    @Test
    public void testHashCodeSmall() {
        StealStrategy small = new StealStrategy(StealStrategy._SMALLEST);
        assertEquals(small.hashCode(), StealStrategy.SMALLEST.hashCode());
    }

    @Test
    public void testEqualsLarge() {
        StealStrategy large = new StealStrategy(StealStrategy._BIGGEST);
        assertEquals(large, StealStrategy.BIGGEST);
    }

    @Test
    public void testEqualsSmall() {
        StealStrategy small = new StealStrategy(StealStrategy._SMALLEST);
        assertEquals(small, StealStrategy.SMALLEST);
    }

    @Test
    public void testEqualsSmallBig() {
        assertNotEquals(StealStrategy.BIGGEST, StealStrategy.SMALLEST);
    }

    @Test
    public void testEqualsNull() {
        assertNotEquals(StealStrategy.BIGGEST, null);
    }

}
