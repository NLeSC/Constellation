package ibis.constellation.impl;

import java.util.LinkedList;

class DeliveryThread extends Thread {

    private final static long MIN_DELAY = 50;
    private final static long MAX_DELAY = MIN_DELAY * 16;

    private LinkedList<EventMessage> incoming1 = new LinkedList<EventMessage>();
    private LinkedList<EventMessage> incoming2 = new LinkedList<EventMessage>();

    private final LinkedList<EventMessage> old = new LinkedList<EventMessage>();

    private long deadline = 0;
    private long currentDelay = MIN_DELAY;

    private final DistributedConstellation parent;

    public DeliveryThread(DistributedConstellation parent) {
        super("EventMessage DeliveryThread");
        setDaemon(true);
        this.parent = parent;
        deadline = System.currentTimeMillis() + MIN_DELAY;
    }

    public synchronized void enqueue(EventMessage m) {
        incoming1.addLast(m);

        // reset the deadline when new messages have been added.
        currentDelay = MIN_DELAY;
        long tmp = System.currentTimeMillis() + currentDelay;

        if (tmp < deadline) {
            deadline = tmp;
            notifyAll();
        }
    }

    private synchronized LinkedList<EventMessage> swap() {
        LinkedList<EventMessage> tmp = incoming1;
        incoming1 = incoming2;
        incoming2 = tmp;
        return tmp;
    }

    private synchronized void waitForDeadline() {

        long t = deadline - System.currentTimeMillis();

        while (t > 0) {
            try {
                wait(t);
            } catch (Exception e) {
                // ignore
            }

            t = deadline - System.currentTimeMillis();
        }
    }

    private synchronized void determineDeadline() {

        currentDelay = currentDelay * 2;

        if (currentDelay > MAX_DELAY) {
            currentDelay = MAX_DELAY;
        }

        deadline += currentDelay;
    }

    private int attemptSend(LinkedList<EventMessage> l) {

        final int size = l.size();

        if (size == 0) {
            return 0;
        }

        for (int i = 0; i < size; i++) {
            EventMessage m = l.removeFirst();

            if (!parent.handleApplicationMessage(m, false)) {
                l.addLast(m);
            }
        }

        return (size - l.size());
    }

    public void run() {

        while (true) {

            waitForDeadline();

            // First try to send any old messages that are still pending.
            attemptSend(old);

            // Next, get any new messages we've obtained and try to send them.
            LinkedList<EventMessage> incoming = swap();
            attemptSend(incoming);

            if (incoming.size() > 0) {
                // If we have any new message left, they are now appended to old
                old.addAll(incoming);
                incoming.clear();
            }

            // Increment the delay and determine new deadline
            determineDeadline();
        }
    }
}
