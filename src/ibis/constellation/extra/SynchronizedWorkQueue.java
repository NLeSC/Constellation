package ibis.constellation.extra;

import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Event;
import ibis.constellation.StealStrategy;
import ibis.constellation.WorkerContext;
import ibis.constellation.impl.ActivityRecord;

public class SynchronizedWorkQueue extends WorkQueue {

    private WorkQueue queue;

    public SynchronizedWorkQueue(WorkQueue queue) {
        super(queue.id);
        this.queue = queue;
    }

    @Override
    public synchronized ActivityRecord dequeue(boolean head) {
        return queue.dequeue(head);
    }

    @Override
    public synchronized void enqueue(ActivityRecord a) {
        queue.enqueue(a);
    }

    @Override
    public synchronized int size() {
        return queue.size();
    }

    @Override
    public synchronized ActivityRecord steal(WorkerContext c, StealStrategy s) {
        return queue.steal(c, s);
    }

    @Override
    public synchronized void enqueue(ActivityRecord[] a) {
        queue.enqueue(a);
    }

    @Override
    public synchronized ActivityRecord[] dequeue(int count, boolean head) {
        return queue.dequeue(count, head);
    }

    @Override
    public synchronized ActivityRecord[] steal(WorkerContext c, StealStrategy s,
            int count) {
        return queue.steal(c, s, count);
    }

    @Override
    public synchronized boolean contains(ActivityIdentifier id) {
        return queue.contains(id);
    }

    @Override
    public synchronized ActivityRecord lookup(ActivityIdentifier id) {
        return queue.lookup(id);
    }

    @Override
    public synchronized boolean deliver(ActivityIdentifier id, Event e) {
        return queue.deliver(id, e);
    }
}
