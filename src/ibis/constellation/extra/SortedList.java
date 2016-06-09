package ibis.constellation.extra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.impl.ActivityRecord;

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

    public void insert(ActivityRecord a, long rank) {

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

    public ActivityRecord removeHead() {

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

    public ActivityRecord removeTail() {

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

    public int size() {
        return size;
    }

    public boolean removeByReference(ActivityRecord o) {

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

    public ActivityRecord removeOneInRange(long start, long end) {

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

}
