package ibis.constellation;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteBufferCache {

    static final Logger logger = LoggerFactory.getLogger(ByteBufferCache.class);

    private static Map<Integer, List<ByteBuffer>> freeList = new HashMap<Integer, List<ByteBuffer>>();
    private static Map<Integer, FreelistFiller> fillers = new HashMap<Integer, FreelistFiller>();

    // Background thread creating new bytebuffers as needed.
    private static class FreelistFiller extends Thread {
	final int sz;
	final int increment;
	final int threshold;

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
		    ByteBuffer v = ByteBuffer.allocateDirect(sz).order(
			    ByteOrder.nativeOrder());
		    releaseByteBuffer(v);
		}
	    }
	}
    }

    public static void releaseByteBuffer(ByteBuffer b) {
	if (logger.isDebugEnabled()) {
	    logger.debug("Releasing bytebuffer " + System.identityHashCode(b));
	}
	int sz = b.capacity();
	synchronized (freeList) {
	    List<ByteBuffer> l = freeList.get(sz);
	    if (l == null) {
		l = new ArrayList<ByteBuffer>();
		freeList.put(sz, l);
	    }
	    l.add(b);
	}
    }

    private static byte[] initBuffer = new byte[65536];

    public static ByteBuffer getByteBuffer(int sz, boolean needsClearing) {
	ByteBuffer b;
	synchronized (freeList) {
	    List<ByteBuffer> l = freeList.get(sz);
	    if (l == null || l.size() == 0) {
		if (logger.isDebugEnabled()) {
		    logger.debug("Allocating new bytebuffer");
		}
		return ByteBuffer.allocateDirect(sz).order(
			ByteOrder.nativeOrder());
	    }
	    b = l.remove(0);
	    FreelistFiller f = fillers.get(sz);
	    if (l.size() < f.threshold) {
		freeList.notify();
	    }
	    if (logger.isDebugEnabled()) {
		logger.debug("bytebuffer " + System.identityHashCode(b)
			+ " from cache");
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

    public static void initializeByteBuffers(int sz, int count) {
	if (logger.isDebugEnabled()) {
	    logger.debug("Allocating " + count + " buffers of size " + sz);
	}
	for (int i = 0; i < count; i++) {
	    ByteBuffer v = ByteBuffer.allocateDirect(sz).order(
		    ByteOrder.nativeOrder());
	    releaseByteBuffer(v);
	}
	FreelistFiller filler = new FreelistFiller(sz, count);
	fillers.put(sz, filler);
	filler.start();
    }

}
