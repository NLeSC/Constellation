package ibis.constellation;

import java.io.IOException;

import ibis.ipl.ReadMessage;
import ibis.ipl.WriteMessage;

/**
 * This interface allows objects to read data from and write data to Ibis
 * messages. This may be useful for data that is not {@link java.io.Serializable
 * serializable}, for instance {@link java.nio.ByteBuffer ByteBuffers}. In
 * particular, this may speed up communication when the Ibis implementation has
 * a fast path for direct byte buffers.
 */
public interface ObjectData {

    /**
     * Handle for writing data to the {@link ibis.ipl.WriteMessage WriteMessage}
     * .
     *
     * @param m
     *            the write message to write to.
     * @throws IOException
     *             thrown in case of an IO error.
     */
    public void writeData(WriteMessage m) throws IOException;

    /**
     * Handle for reading data from the {@link ibis.ipl.ReadMessage ReadMessage}
     * .
     *
     * @param m
     *            the read message to read from.
     * @throws IOException
     *             thrown in case of an IO error.
     */
    public void readData(ReadMessage m) throws IOException;

}
