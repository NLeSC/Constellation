package ibis.constellation.extra;

import ibis.constellation.ObjectData;
import ibis.ipl.ReadMessage;
import ibis.ipl.WriteMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CircularBuffer implements Serializable, ObjectData {

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

    public void insertFirst(Object item) {

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

    public void insertLast(Object item) {

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

    public Object get(int index) {

        if (index >= size) {
            return null;
        }

        return array[(first + index) % array.length];
    }

    public Object removeFirst() {

        if (size == 0) {
            return null;
        }

        Object result = array[first];
        array[first] = null;
        first++;
        size--;

        if (first >= array.length) {
            first = 0;
        }

        return result;
    }

    public Object removeLast() {

        if (size == 0) {
            return null;
        }

        if (next == 0) {
            next = array.length - 1;
        } else {
            next--;
        }

        Object result = array[next];
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

    public boolean remove(Object o) {

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

    public void clear() {

        if (size > 0) {
            // Note; may be inefficient with large, relatively empty arrays!
            Arrays.fill(array, null);
        }

        // while (size > 0) {
        // array[first++] = null;
        // size--;
        //
        // if (first >= array.length) {
        // first = 0;
        // }
        // }

        first = next = size = 0;
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
    public void writeData(WriteMessage m) throws IOException {
        if (array != null) {
            for (Object a : array) {
                if (a != null && a instanceof ObjectData) {
                    ((ObjectData) a).writeData(m);
                }
            }
        }
    }

    @Override
    public void readData(ReadMessage m) throws IOException {
        if (array != null) {
            for (Object a : array) {
                if (a != null && a instanceof ObjectData) {
                    ((ObjectData) a).readData(m);
                }
            }
        }
    }

}
