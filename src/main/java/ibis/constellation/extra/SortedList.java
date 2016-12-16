package ibis.constellation.extra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.impl.ActivityRecord;

public class SortedList {

    public static final Logger log = LoggerFactory.getLogger(SortedList.class);

    static class Node {
        private Node next;
        private Node prev;

        private final long rank;
        private final ActivityRecord data;

        Node(ActivityRecord data, long rank) {
            this.data = data;
            this.rank = rank;
        }
    }

    private final String name;

    private Node head = new Node(null, Long.MIN_VALUE);
    private Node tail = new Node(null, Long.MAX_VALUE);
    private int size;

    public SortedList(String name) {
        this.name = name;
        head.next = tail;
        tail.prev = head;
        size = 0;
    }

    public void insert(ActivityRecord a, long rank) {

        Node n = new Node(a, rank);

        Node current = head.next;

        for (;;) {
            // Check if the new element goes at/before the current. Always succeeds for tail
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
    }

    public ActivityRecord removeHead() {

        if (size == 0) {
            return null;
        }

        Node v = head.next;
        v.next.prev = head;
        head.next = v.next;
        size--;

        return v.data;
    }

    public ActivityRecord removeTail() {

        if (size == 0) {
            return null;
        }

        Node v = tail.prev;
        v.prev.next = tail;
        tail.prev = v.prev;
        size--;

        return v.data;
    }

    public int size() {
        return size;
    }

    public boolean removeByReference(ActivityRecord o) {

        Node current = head.next;

        while (current.data != null) {

            if (current.data == o) {
                // Found it
                current.prev.next = current.next;
                current.next.prev = current.prev;
                size--;
                return true;
            }

            current = current.next;
        }

        return false;
    }

    public ActivityRecord removeOneInRange(long start, long end) {

        Node current = head.next;

        if (size == 0) {
            return null;
        }

        while (current.data != null && current.rank < start) {
            current = current.next;
        }

        if (current.data == null || current.rank > end) {
            return null;
        }

        // Found it

        current.prev.next = current.next;
        current.next.prev = current.prev;
        size--;

        return current.data;
    }

    public String getName() {
        return name;
    }

}
