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

package ibis.constellation.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A cache of direct {@link ByteBuffer}s of specific sizes. This class is not specifically part of constellation, but it may be
 * useful for applications. This class is thread-safe.
 */
public class ByteBufferCache {

    private static final Logger logger = LoggerFactory.getLogger(ByteBufferCache.class);

    private static Map<Integer, List<ByteBuffer>> freeList = new HashMap<Integer, List<ByteBuffer>>();
    private static Map<Integer, FreelistFiller> fillers = new HashMap<Integer, FreelistFiller>();

    private static long inUse = 0;
    private static long available = 0;

    // Background thread creating new bytebuffers as needed.
    private static class FreelistFiller extends Thread {
        private final int sz;
        private final int increment;
        private final int threshold;

        FreelistFiller(int sz, int cnt) {
            this.sz = sz;
            this.threshold = cnt / 3;
            this.increment = cnt / 2;
            this.setDaemon(true);
        }

        @Override
        public void run() {
            for (;;) {
                int cnt;
                synchronized (freeList) {
                    try {
                        freeList.wait();
                    } catch (Throwable e) {
                        // ignore
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Filler woke up");
                    }
                    List<ByteBuffer> l = freeList.get(sz);
                    cnt = increment - l.size();
                }
                for (int i = 0; i < cnt; i++) {
                    ByteBuffer v = ByteBuffer.allocateDirect(sz).order(ByteOrder.nativeOrder());
                    if (logger.isDebugEnabled()) {
                        inUse += sz;
                    }
                    makeAvailableByteBuffer(v);
                }
            }
        }
    }

    /**
     * Make the specified byte buffer available for use, that is, append it to the list of available byte buffers.
     *
     * @param b
     *            the byte buffer to be made available.
     */
    public static void makeAvailableByteBuffer(ByteBuffer b) {
        if (logger.isDebugEnabled()) {
            logger.debug("Making ByteBuffer " + System.identityHashCode(b) + " available");
        }
        int sz = b.capacity();
        synchronized (freeList) {
            List<ByteBuffer> l = freeList.get(sz);
            if (l == null) {
                l = new ArrayList<ByteBuffer>();
                freeList.put(sz, l);
            }
            l.add(b);
            if (logger.isDebugEnabled()) {
                available += sz;
                inUse -= sz;
                logger.debug(l.size() + " ByteBuffers of size " + MemorySizes.toStringBytes(sz) + " available");
                logUse();
            }
        }
    }

    private static byte[] initBuffer = new byte[65536];

    /**
     * Obtains a byte buffer of the specified size. If one cannot be found in the cache, a new one is allocated. If it needs to be
     * clear(ed), the <code>needsClearing</code> flag should be set to <code>true</code>.
     *
     * @param sz
     *            size of the byte buffer to be obtained.
     * @param needsClearing
     *            whether the buffer must be cleared.
     * @return the obtained byte buffer.
     */
    public static ByteBuffer getByteBuffer(int sz, boolean needsClearing) {
        ByteBuffer b;
        synchronized (freeList) {
            List<ByteBuffer> l = freeList.get(sz);
            if (l == null || l.size() == 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Allocating new bytebuffer of size " + MemorySizes.toStringBytes(sz));
                }
                b = ByteBuffer.allocateDirect(sz).order(ByteOrder.nativeOrder());
                if (logger.isDebugEnabled()) {
                    inUse += sz;
                    logger.debug("obtaining ByteBuffer " + System.identityHashCode(b) + "(not from cache)");
                    logUse();
                }
                return b;
            }
            b = l.remove(0);
            FreelistFiller f = fillers.get(sz);
            if (f != null) {
                if (l.size() < f.threshold) {
                    freeList.notify();
                }
            }
            if (logger.isDebugEnabled()) {
                inUse += sz;
                available -= sz;
                logger.debug("obtaining ByteBuffer " + System.identityHashCode(b) + " of size " + MemorySizes.toStringBytes(sz)
                        + " from cache");
                logUse();
            }
        }
        if (needsClearing) {
            // Clear buffer.
            b.position(0);
            while (b.position() + initBuffer.length <= b.capacity()) {
                b.put(initBuffer);
            }
            b.put(initBuffer, 0, b.capacity() - b.position());
        }
        return b;
    }

    /**
     * Initializes the byte buffer cache with the specified number of buffers of the specified size.
     *
     * @param sz
     *            the size of the byte buffers
     * @param count
     *            the number of byte buffers
     */
    public static void initializeByteBuffers(int sz, int count) {
        if (logger.isDebugEnabled()) {
            logger.debug("Allocating " + count + " buffers of size " + " " + MemorySizes.toStringBytes(sz));
        }
        for (int i = 0; i < count; i++) {
            ByteBuffer v = ByteBuffer.allocateDirect(sz).order(ByteOrder.nativeOrder());
            if (logger.isDebugEnabled()) {
                inUse += sz;
            }
            makeAvailableByteBuffer(v);
        }
        FreelistFiller filler = new FreelistFiller(sz, count);
        fillers.put(sz, filler);
        filler.start();
    }

    private static void logUse() {
        logger.debug("in use: " + MemorySizes.toStringBytes(inUse) + ", available: " + MemorySizes.toStringBytes(available));
    }

}
