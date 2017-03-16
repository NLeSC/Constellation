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

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * When an object implements this interface, it is assumed to contain {@link ByteBuffer}s, which are not {@link Serializable}, but
 * may need to be passed on to another node anyway. The methods in this interface allow the object to either pass on its byte
 * buffers to the layer below, or to obtain its byte buffers from the layer below. This mechanism may also provide a fast path for
 * direct byte buffers. Constellation enables this interface for {@link Event events} and {@link Activity activities}. When
 * constellation serializes an event or activity, after serialization it will call {@link #pushByteBuffers(List)} to obtain any
 * ByteBuffers to be transferred, and transfer them. On the other side, it will, after deserialization, call
 * {@link #popByteBuffers(List)} to pop the corresponding ByteBuffers from the list.
 *
 */
public interface ByteBuffers {

    /**
     * Handle for obtaining the byte buffers from the object. When called, the object is supposed to add its {@link ByteBuffer}s
     * to the list. When this method gets called, the object is supposed to have been serialized already. Note: an application is
     * not supposed to call this method. Constellation is responsible for that.
     *
     * @param list
     *            the list to add byte buffers to.
     */
    public void pushByteBuffers(List<ByteBuffer> list);

    /**
     * Handle for passing on byte buffers to the object. The object may use the byte buffers in the list directly, i.e. it does
     * not have to copy them. ByteBuffers should be removed from the head of the list. When this method gets called, the
     * serializable data of the object is supposed to have been read already. Note: an application is not supposed to call this
     * method. Constellation is responsible for that.
     *
     * @param list
     *            the list to obtain buffers from.
     */
    public void popByteBuffers(List<ByteBuffer> list);

}
