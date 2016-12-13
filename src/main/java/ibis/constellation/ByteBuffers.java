package ibis.constellation;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * When an object implements this interface, it is assumed to contain {@link ByteBuffer}s, which are not {@link Serializable}, but
 * may need to be passed on to another node anyway. The methods in this interface allow the object to either pass on its byte
 * buffers to the layer below, or to obtain its byte buffers from the layer below. This mechanism may also provide a fast path for
 * direct byte buffers.
 */
public interface ByteBuffers {

    /**
     * Handle for obtaining the byte buffers from the object. When called, the object is supposed to add its {@link ByteBuffer}s
     * to the list. When this method gets called, the object is supposed to have been serialized already.
     *
     * @param list
     *            the list to add byte buffers to.
     */
    public void pushByteBuffers(List<ByteBuffer> list);

    /**
     * Handle for passing on byte buffers to the object. The object may use the byte buffers in the list directly, i.e. it does
     * not have to copy them. ByteBuffers should be removed from the head of the list. When this method gets called, the
     * serializable data of the object is supposed to have been read already.
     *
     * @param list
     *            the list to obtain buffers from.
     */
    public void popByteBuffers(List<ByteBuffer> list);

}
