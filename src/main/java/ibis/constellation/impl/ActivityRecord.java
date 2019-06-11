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
package ibis.constellation.impl;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.AbstractContext;
import ibis.constellation.Activity;
import ibis.constellation.Constellation;
import ibis.constellation.Event;
import ibis.constellation.impl.util.CircularBuffer;
import ibis.constellation.util.ByteBuffers;

public class ActivityRecord implements Serializable, ByteBuffers {

    private static final Logger logger = LoggerFactory.getLogger(ActivityRecord.class);
    private static final long serialVersionUID = 6938326535791839797L;

    private static final int INITIALIZING = 1;
    private static final int SUSPENDED = 2;
    private static final int RUNNABLE = 3;
    private static final int FINISHING = 4;
    private static final int DONE = 5;
    private static final int ERROR = Integer.MAX_VALUE;

    private final Activity activity;
    private final ActivityIdentifierImpl identifier;

    private final AbstractContext context;

    private final boolean mayBeStolen;

    private final CircularBuffer<EventWrapper> queue;
    private int state = INITIALIZING;

    private boolean stolen = false;
    private boolean relocated = false;
    private boolean remote = false;

    private static class EventWrapper implements ByteBuffers, Serializable {

        private static final long serialVersionUID = 1051677223714686496L;

        public final Event event;

        public EventWrapper(Event event) {
            this.event = event;
        }

        @Override
        public void pushByteBuffers(List<ByteBuffer> list) {
            Object tmp = event.getData();

            if (tmp != null && tmp instanceof ByteBuffers) {
                ((ByteBuffers) tmp).pushByteBuffers(list);
            }
        }

        @Override
        public void popByteBuffers(List<ByteBuffer> list) {
            Object tmp = event.getData();

            if (tmp != null && tmp instanceof ByteBuffers) {
                ((ByteBuffers) tmp).popByteBuffers(list);
            }
        }
    }

    ActivityRecord(Activity activity, ActivityIdentifierImpl id) {
        this.activity = activity;
        this.identifier = id;
        this.context = activity.getContext();
        this.mayBeStolen = activity.mayBeStolen();

        if (activity.expectsEvents()) {
            queue = new CircularBuffer<EventWrapper>(4);
        } else {
            queue = null;
        }
    }

    public void enqueue(Event e) {

        if (queue == null) {
            throw new IllegalStateException("Activity does not expect events");
        }

        if (e == null) {
            throw new IllegalArgumentException("Event may not be null");
        }

        if (state >= FINISHING) {
            throw new IllegalStateException(
                    "Cannot deliver an event to a finished activity! " + activity + " (event from " + e.getSource() + ")");
        }

        queue.insertLast(new EventWrapper(e));
    }

    public Event dequeue() {

        if (queue == null) {
            throw new IllegalStateException("Activity does not expect events");
        }

        if (queue.size() == 0) {
            return null;
        }

        return queue.removeFirst().event;
    }

    public int pendingEvents() {

        if (queue == null) {
            throw new IllegalStateException("Activity does not expect events");
        }

        return queue.size();
    }

    public ActivityIdentifierImpl identifier() {
        return identifier;
    }

    public boolean isRunnable() {
        return (state == RUNNABLE);
    }

    public boolean isFinishing() {
        return state == FINISHING;
    }

    public boolean isStolen() {
        return stolen;
    }

    public void setStolen(boolean value) {
        stolen = value;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean value) {
        remote = value;
    }

    public void setRelocated(boolean value) {
        relocated = value;
    }

    public boolean isRelocated() {
        return relocated;
    }

    public boolean isRestrictedToLocal() {
        return !mayBeStolen;
    }

    public boolean isDone() {
        return (state == DONE || state == ERROR);
    }

    public boolean isError() {
        return (state == ERROR);
    }

    public boolean isFresh() {
        return (state == INITIALIZING);
    }

    public boolean needsToRun() {
        return (state == INITIALIZING || state == RUNNABLE || state == FINISHING);
    }

    public boolean setRunnable() {

        if (state == RUNNABLE || state == INITIALIZING) {
            // it's already runnable
            return false;
        }

        if (state == SUSPENDED) {
            // it's runnable now
            state = RUNNABLE;
            return true;
        }

        // It cannot be made runnable
        throw new IllegalStateException("INTERNAL ERROR: activity cannot be made runnable!");
    }

    private final void runStateMachine(Constellation c) {
        try {
            int nextState;

            switch (state) {

            case INITIALIZING:
                nextState = activity.initialize(c);

                if (nextState == Activity.SUSPEND) {
                    if (pendingEvents() > 0) {
                        state = RUNNABLE;
                    } else {
                        state = SUSPENDED;
                    }
                } else if (nextState == Activity.FINISH) {
                    // TODO: handle pending event here ?? Exception or warning ?
                    state = FINISHING;
                } else {
                    throw new IllegalStateException("Activity did not suspend or finish!");
                }
                break;

            case RUNNABLE:

                Event e = dequeue();

                if (e == null) {
                    throw new IllegalStateException("INTERNAL ERROR: Runnable activity has no pending events!");
                }

                nextState = activity.process(c, e);

                if (nextState == Activity.SUSPEND) {
                    // We only suspend the job if there are no pending events.
                    if (pendingEvents() > 0) {
                        state = RUNNABLE;
                    } else {
                        state = SUSPENDED;
                    }
                } else if (nextState == Activity.FINISH) {
                    // TODO: handle pending event here ?? Exception or warning ?
                    state = FINISHING;
                } else {
                    throw new IllegalStateException("Activity did not suspend or finish!");
                }

                break;

            case FINISHING:
                activity.cleanup(c);
                state = DONE;
                break;

            case DONE:
                throw new IllegalStateException("INTERNAL ERROR: Running activity that is already done");

            case ERROR:
                throw new IllegalStateException("INTERNAL ERROR: Running activity that is in an error state!");

            default:
                throw new IllegalStateException("INTERNAL ERROR: Running activity with unknown state!");
            }

        } catch (Throwable e) {
            logger.error("Activity failed: ", e);
            state = ERROR;
        }

    }

    public void run(Constellation c) {
        runStateMachine(c);
    }

    private String getStateAsString() {

        switch (state) {

        case INITIALIZING:
            return "initializing";
        case SUSPENDED:
            return "suspended";
        case RUNNABLE:
            return "runnable";
        case FINISHING:
            return "finishing";
        case DONE:
            return "done";
        case ERROR:
            return "error";
        default:
            return "unknown";
        }
    }

    @Override
    public String toString() {
        return activity + " STATE: " + getStateAsString() + " " + "event queue size = " + (queue == null ? 0 : queue.size());
    }

    public AbstractContext getContext() {
        return context;
    }

    @Override
    public void pushByteBuffers(List<ByteBuffer> list) {
        if (queue != null) {
            queue.pushByteBuffers(list);
        }
        if (activity != null && activity instanceof ByteBuffers) {
            ((ByteBuffers) activity).pushByteBuffers(list);
        }
    }

    @Override
    public void popByteBuffers(List<ByteBuffer> list) {
        if (queue != null) {
            queue.popByteBuffers(list);
        }
        if (activity != null && activity instanceof ByteBuffers) {
            ((ByteBuffers) activity).popByteBuffers(list);
        }
    }

    //    public Activity getActivity() {
    //        return activity;
    //    }
}
