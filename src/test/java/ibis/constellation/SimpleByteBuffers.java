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

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class SimpleByteBuffers implements ByteBuffers, java.io.Serializable {

    private transient List<ByteBuffer> list = new LinkedList<>();
    
    /* (non-Javadoc)
     * @see ibis.constellation.ByteBuffers#pushByteBuffers(java.util.List)
     */
    @Override
    public void pushByteBuffers(List<ByteBuffer> list) {
        this.list.addAll(list);
    }

    /* (non-Javadoc)
     * @see ibis.constellation.ByteBuffers#popByteBuffers(java.util.List)
     */
    @Override
    public void popByteBuffers(List<ByteBuffer> list) {
        list.addAll(this.list);
        this.list.clear();
    }
}
