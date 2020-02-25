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
package ibis.constellation.impl.pool;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class PoolCreationFailedExceptionTest {

    @Test
    public void testConstructor1() {
        String message = "Hello World";
        PoolCreationFailedException e = new PoolCreationFailedException(message);
        assertEquals(e.getMessage(), message);        
    }
   
    @Test
    public void testConstructor2() {
        String message = "Hello World";
        Exception ex = new Exception("bla");
        PoolCreationFailedException e = new PoolCreationFailedException(message, ex);
        assertEquals(e.getMessage(), message);        
    }

    @Test
    public void testConstructor3() {
        String message = "Hello World";
        Exception ex = new Exception("bla");
        PoolCreationFailedException e = new PoolCreationFailedException(message, ex);
        assertEquals(e.getCause(), ex);        
    }
}
