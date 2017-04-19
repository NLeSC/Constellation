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

package ibis.constellation.impl.pool;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * @version 1.0
 * @since 1.0
 *
 */
public class PoolUpdateRequestTest {

    @Test
    public void testConstructor1() { 
        PoolUpdateRequest t = new PoolUpdateRequest(null, "Hello", 42L);
        assertEquals(42L, t.timestamp);
    }
    
    @Test
    public void testConstructor2() { 
        PoolUpdateRequest t = new PoolUpdateRequest(null, "Hello", 42L);
        assertEquals("Hello", t.tag);
    }
    
    @Test
    public void testConstructor3() { 
        PoolUpdateRequest t = new PoolUpdateRequest(null, "Hello", 42L);
        assertEquals(null, t.source);
    }
}
