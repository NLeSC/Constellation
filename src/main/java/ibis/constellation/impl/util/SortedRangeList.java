/*
 * Copyright 2018 Netherlands eScience Center
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
package ibis.constellation.impl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.impl.ActivityRecord;

public class SortedRangeList {

    public static final Logger log = LoggerFactory.getLogger(SortedRangeList.class);

    static class Node {
        private Node next;
        private Node prev;

        private final long start;
        private final long end;
        
        private final ActivityRecord data;

        Node(ActivityRecord data, long start, long end) {
            this.data = data;
            this.start = start;
            this.end = end;
        }
    }

    private final String name;

    private Node head = new Node(null, Long.MIN_VALUE, Long.MIN_VALUE);
    private Node tail = new Node(null, Long.MAX_VALUE, Long.MAX_VALUE);
    private int size;

    public SortedRangeList(String name) {
        this.name = name;
        head.next = tail;
        tail.prev = head;
        size = 0;
    }

    public void insert(ActivityRecord a, long start, long end) {

        Node n = new Node(a, start, end);

        Node current = head.next;

        for (;;) {
            // Check if the new element goes at/before the current. Always succeeds for tail
            if (start < current.start || (start == current.start && end <= current.end)) {

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

    public ActivityRecord removeSmallestInRange(long start, long end) {

        Node current = head.next;

        if (size == 0) {
            return null;
        }

        while (current.data != null && current.end < start) {
            current = current.next;
        }

        if (current.data == null || end < current.start) {
            return null;
        }

        // Found an element with overlap!
        current.prev.next = current.next;
        current.next.prev = current.prev;
        size--;

        return current.data;
    }

    public ActivityRecord removeBiggestInRange(long start, long end) {

        Node current = tail.prev;

        if (size == 0) {
            return null;
        }

        while (current.data != null && end < current.start) {
            current = current.prev;
        }

        if (current.data == null || current.end < start) {
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
