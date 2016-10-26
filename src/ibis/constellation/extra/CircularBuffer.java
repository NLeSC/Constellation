package ibis.constellation.extra;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.ByteBuffers;

public class CircularBuffer<T> implements Serializable, ByteBuffers {

    private static final long serialVersionUID = 5853279675709435595L;

    private static final Logger log = LoggerFactory
            .getLogger(CircularBuffer.class);

    private Object[] array;

    private int first, next;
    private int size;

    public CircularBuffer(int initialSize) {
        array = new Object[initialSize];
        first = 0;
        next = 0;
    }

    public boolean empty() {
        return (size == 0);
    }

    public int size() {
        return size;
    }

    public void insertFirst(T item) {

        if (item == null) {
            log.error("InsertFirst null!!", new Throwable());
        }

        if (size >= array.length) {
            resize();
        }

        if (first == 0) {
            first = array.length - 1;
        } else {
            first--;
        }

        array[first] = item;
        size++;
    }

    public void insertLast(T item) {

        if (item == null) {
            log.error("insertLast null!!", new Throwable());
        }

        if (size >= array.length) {
            resize();
        }

        array[next++] = item;
        size++;

        if (next >= array.length) {
            next = 0;
        }
    }

    private void resize() {
        Object[] old = array;
        array = new Object[array.length * 2];

        System.arraycopy(old, first, array, 0, old.length - first);
        System.arraycopy(old, 0, array, old.length - first, first);

        first = 0;
        next = old.length;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {

        if (index >= size) {
            return null;
        }

        return (T) array[(first + index) % array.length];
    }

    public T removeFirst() {

        if (size == 0) {
            return null;
        }

        @SuppressWarnings("unchecked")
        T result = (T) array[first];
        array[first] = null;
        first++;
        size--;

        if (first >= array.length) {
            first = 0;
        }

        return result;
    }

    public T removeLast() {

        if (size == 0) {
            return null;
        }

        if (next == 0) {
            next = array.length - 1;
        } else {
            next--;
        }

        @SuppressWarnings("unchecked")
        T result = (T) array[next];
        array[next] = null;
        size--;

        return result;
    }

    public boolean remove(int index) {

        if (index > size - 1) {
            return false;
        }

        if (index == 0) {
            removeFirst();
        } else if (index == size) {
            removeLast();
        } else {
            // TODO: optimize ? i.e., figure out how to move the least data
            int pos = (first + index) % array.length;

            if (next > pos) {
                // We simply move part of the data back
                while (pos < next - 1) {
                    array[pos] = array[pos + 1];
                    pos++;
                }

                next--;
                array[next] = null;
                size--;
            } else {
                // We simply move part of the data forward
                while (pos > first) {
                    array[pos] = array[pos - 1];
                    pos--;
                }

                array[first] = null;
                first++;
                size--;
            }
        }

        return true;
    }

    public boolean remove(T o) {

        if (size == 0) {
            return false;
        }

        int index = first;

        boolean removed = false;

        for (int i = 0; i < size; i++) {

            if (o.equals(array[index])) {
                remove(i);
                removed = true;
            }

            index++;
        }

        return removed;
    }

    @Override
    public String toString() {

        return "CircularBuffer(" + size + ")";

        /*
         *
         * StringBuilder sb = new StringBuilder("CircuralBuffer(" + size +
         * ") - [");
         *
         * for (int i=0;i<size-1;i++) { sb.append(get(i)); sb.append(", "); }
         *
         * if (size > 0) { sb.append(get(size-1)); }
         *
         * sb.append("]");
         *
         * return sb.toString();
         */
    }

    @Override
    public void pushByteBuffers(List<ByteBuffer> list) {
        if (array != null) {
            for (Object a : array) {
                if (a != null && a instanceof ByteBuffers) {
                    ((ByteBuffers) a).pushByteBuffers(list);
                }
            }
        }
    }

    @Override
    public void popByteBuffers(List<ByteBuffer> list) {
        if (array != null) {
            for (Object a : array) {
                if (a != null && a instanceof ByteBuffers) {
                    ((ByteBuffers) a).popByteBuffers(list);
                }
            }
        }
    }
}
