package ibis.constellation.extra;

import ibis.constellation.impl.ActivityRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SortedList {

    public static final Logger log = LoggerFactory.getLogger(SortedList.class);

    static class Node {
        Node next;
        Node prev;

        final long rank;
        final ActivityRecord data;

        Node(ActivityRecord data, long rank) {
            this.data = data;
            this.rank = rank;
        }
    }

    private final String name;

    private Node head;
    private Node tail;
    private int size;

    public SortedList(String name) {
        this.name = name;
        head = tail = null;
        size = 0;
    }

    public synchronized void insert(ActivityRecord a, long rank) {

        Node n = new Node(a, rank);

        // Check if the list is empty
        if (size == 0) {
            head = tail = n;
            size = 1;
            return;
        }

        // Check if the list contains a single element
        if (size == 1) {

            if (rank <= head.rank) {
                n.next = head;
                head.prev = n;
                head = n;
            } else {
                n.prev = tail;
                tail.next = n;
                tail = n;
            }

            size = 2;
            return;
        }

        // Check if the new element goes before/at the head
        if (rank <= head.rank) {
            n.next = head;
            head.prev = n;
            head = n;
            size++;
            return;
        }

        // Check if the new element goes at/after the tail
        if (rank >= tail.rank) {
            n.prev = tail;
            tail.next = n;
            tail = n;
            size++;
            return;
        }

        Node current = head.next;

        while (current != null) {

            // Check if the new element goes at/before the current
            if (rank <= current.rank) {

                n.prev = current.prev;
                current.prev.next = n;

                n.next = current;
                current.prev = n;

                size++;
                return;
            }

            current = current.next;
        }

        // When we run out of nodes we insert at the end -- SHOULD NOT HAPPEN!--
        log.error("Sorted list screwed up!!!");

        n.prev = tail;
        tail.next = n;
        tail = n;
        size++;
    }

    public synchronized ActivityRecord removeHead() {

        if (size == 0) {
            return null;
        }

        ActivityRecord tmp = head.data;

        if (size == 1) {
            head = tail = null;
            size = 0;
            return tmp;
        }

        head = head.next;
        head.prev = null;
        size--;

        return tmp;
    }

    public synchronized ActivityRecord removeTail() {

        if (size == 0) {
            return null;
        }

        ActivityRecord tmp = tail.data;

        if (size == 1) {
            head = tail = null;
            size = 0;
            return tmp;
        }

        tail = tail.prev;
        tail.next = null;
        size--;

        return tmp;
    }

    public synchronized int size() {
        return size;
    }

    public synchronized boolean removeByReference(ActivityRecord o) {

        Node current = head;

        while (current != null) {

            if (current.data == o) {

                // Found it
                if (size == 1) {
                    head = tail = null;
                    size = 0;
                    return true;
                }

                if (current == head) {
                    head = head.next;
                    head.prev = null;
                    size--;
                    return true;
                }

                if (current == tail) {
                    tail = tail.prev;
                    tail.next = null;
                    size--;
                    return true;
                }

                current.prev.next = current.next;
                current.next.prev = current.prev;
                current.prev = null;
                current.next = null;
                size--;
                return true;
            }

            current = current.next;
        }

        return false;
    }

    public synchronized ActivityRecord removeOneInRange(long start, long end) {

        Node current = head;

        while (current != null && current.rank < start) {
            current = current.next;
        }

        if (current == null || current.rank > end) {
            return null;
        }

        // Found it
        if (size == 1) {
            head = tail = null;
            size = 0;
            return current.data;
        }

        if (current == head) {
            head = head.next;
            head.prev = null;
            size--;
            return current.data;
        }

        if (current == tail) {
            tail = tail.prev;
            tail.next = null;
            size--;
            return current.data;
        }

        current.prev.next = current.next;
        current.next.prev = current.prev;
        current.prev = null;
        current.next = null;
        size--;

        return current.data;
    }

    public synchronized ActivityRecord[] removeSetInRange(long start, long end,
            int count) {

        // NOTE: this selects the smallest part of the range, which is not what
        // we want ?
        if (size == 0) {
            return null;
        }

        Node s = head;

        while (s != null && s.rank < start) {
            s = s.next;
        }

        if (s == null || s.rank > end) {
            return null;
        }

        Node e = s;
        int len = 1;

        while (e != tail && e.next.rank <= end && len < count) {
            len++;
            e = e.next;
        }

        // We now have a sublist of length 'len' from 's' (inclusive) to 'e'
        // (inclusive)
        ActivityRecord[] result = new ActivityRecord[len];

        if (len == size) {
            // The entire list is selected
            for (int i = 0; i < len; i++) {
                result[i++] = head.data;
                head = head.next;
            }

            head = tail = null;
            size = 0;
            return result;
        }

        // Only part is selected. Unlink first
        if (s == head) {
            // The head part of the list is selected
            head = e.next;
            head.prev = null;
        } else if (e == tail) {
            // The tail part of the list is selected
            tail = s.prev;
            tail.next = null;
        } else {
            // Arbitrary center section is selected
            s.prev.next = e.next;
            e.next.prev = s.prev;
        }

        s.prev = null;
        e.next = null;
        size -= len;

        // Next copy the data
        for (int i = 0; i < len; i++) {
            result[i++] = s.data;
            s = s.next;
        }

        return result;
    }
}
