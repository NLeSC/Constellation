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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import ibis.constellation.impl.util.TimeSyncInfo;

public class TimerImpl implements java.io.Serializable, ibis.constellation.Timer {

    private static class TimerEvent implements java.io.Serializable, Comparable<TimerEvent> {

        private static final long serialVersionUID = 1L;

        public String node;
        public String device;
        public String thread;
        public String action;

        public long queued;
        public long submitted;
        public long start;
        public long end;

        public long nrBytes;

        public TimerEvent(String node, String device, String thread, String action, long queued, long submitted, long start,
                long end) {

            this.node = node;
            this.device = device;
            this.thread = thread;
            this.action = action;
            this.queued = queued;
            this.submitted = submitted;
            this.start = start;
            this.end = end;
            this.nrBytes = 0;
        }

        public String getNode() {
            return node;
        }

        public String getDevice() {
            return device;
        }

        public String getAction() {
            return action;
        }

        public long getQueued() {
            return queued;
        }

        public long getSubmitted() {
            return submitted;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }

        public String getThread() {
            return thread;
        }

        public void normalize(long min) {
            queued -= min;
            submitted -= min;
            start -= min;
            end -= min;
        }

        public long time() {
            return end - start;
        }

        public void empty() {
            queued = 0;
            submitted = 0;
            start = 0;
            end = 0;
        }

        public boolean isEmptyEvent() {
            return queued == 0 && submitted == 0 && start == 0 && end == 0;
        }

        public boolean isOverallEvent() {
            return device.equals("java") && thread.equals("main") && action.equals("overall");
        }

        public boolean hasDataTransfers() {
            return nrBytes > 0;
        }

        private double getRate() {
            double nrMBs = nrBytes / 1024.0 / 1024.0;
            double duration = (end - start) / 1e9;
            return nrMBs / duration;
        }

        private String getRateString() {
            return String.format("%4.3f MB/s", getRate());
        }

        public String toDataTransferString() {
            return String.format(
                    "%-8s  |  %-14s  |  start: %-6s  |  " + "end: %-6s  |  duration: %-6s  | nrBytes: %4.3f MB  |  "
                            + "rate: %13s\n",
                    node, action, ibis.util.Timer.format(start / 1000.0), ibis.util.Timer.format(end / 1000.0),
                    ibis.util.Timer.format((end - start) / 1000.0), nrBytes / 1024.0 / 1024.0, getRateString());
        }

        @Override
        public String toString() {
            return String.format(
                    "%-8s  |  %-10s  |  %-22s  | " + "%-14s  |  queued: %-6s  |  submitted: %-6s  |  "
                            + "start: %-6s  |  end: %-6s\n",
                    node, device, thread, // queueIndex,
                    action, ibis.util.Timer.format(queued / 1000.0), ibis.util.Timer.format(submitted / 1000.0),
                    ibis.util.Timer.format(start / 1000.0), ibis.util.Timer.format(end / 1000.0));
        }

        @Override
        public int compareTo(TimerEvent that) {
            int result = this.node.compareTo(that.node);
            if (result == 0) {
                result = this.thread.compareTo(that.thread);
            }
            if (result == 0) {
                result = this.device.compareTo(that.device);
            }
            if (result == 0) {
                if (this.time() < that.time()) {
                    result = 1;
                } else if (this.time() == that.time()) {
                    result = 0;
                } else {
                    result = -1;
                }
            }

            return result;
        }
    }

    private static final long serialVersionUID = 1L;

    private ArrayList<TimerEvent> events;

    private final String hostId;
    private final String device;
    private final String thread;
    private final String action;

    public String getAction() {
        if (action == null && events.size() > 0) {
            TimerEvent event = events.get(0);
            return event.getAction();
        }
        return action;
    }

    public String getNode() {
        return hostId;
    }

    public TimerImpl(String nodeId) {
        this(nodeId, null, null, null);
    }

    public TimerImpl(String nodeId, String standardDevice, String standardThread, String standardAction) {
        this.events = new ArrayList<TimerEvent>();
        this.hostId = nodeId;
        this.device = standardDevice;
        this.thread = standardThread;
        this.action = standardAction;
    }

    public void equalize(TimeSyncInfo timeSyncInfo) {
        for (TimerEvent e : events) {
            long offsetToMaster = timeSyncInfo.getOffsetToMaster(e.node);
            e.queued += offsetToMaster;
            e.submitted += offsetToMaster;
            e.start += offsetToMaster;
            e.end += offsetToMaster;
        }
    }

    public synchronized void cancel(int evt) {
        if (evt == events.size() - 1) {
            events.remove(evt);
        } else {
            events.get(evt).empty();
        }
    }

    @Override
    public int start() {
        int eventNo;
        TimerEvent event = new TimerEvent(getNode(), device, thread, action, 0, 0, 0, 0);
        synchronized (this) {
            eventNo = events.size();
            events.add(event);
        }
        event.queued = event.submitted = event.start = System.nanoTime();
        return eventNo;
    }

    public int start(String action) {
        int eventNo = start();
        TimerEvent event = events.get(eventNo);
        event.action = action;
        return eventNo;
    }

    public void addBytes(long nrBytes, int eventNo) {
        if (eventNo < 0 || eventNo >= events.size()) {
            return;
        }
        TimerEvent event = events.get(eventNo);
        event.nrBytes = nrBytes;
    }

    @Override
    public void stop(int eventNo) {
        if (eventNo < 0 || eventNo >= events.size()) {
            return;
        }
        TimerEvent event = events.get(eventNo);
        event.end = System.nanoTime();
    }

    private void add(TimerEvent event) {
        this.events.add(event);
    }

    public void onlyDataTransfers() {
        ArrayList<TimerEvent> filtered = new ArrayList<TimerEvent>();
        for (TimerEvent e : events) {
            if (e != null && e.hasDataTransfers()) {
                filtered.add(e);
            }
        }
        this.events = filtered;
    }

    public List<TimerImpl> groupByAction() {
        Function<TimerEvent, String> f = new Function<TimerEvent, String>() {
            @Override
            public String apply(TimerEvent event) {
                return event.getAction();
            }
        };
        return groupBy(f);
    }

    //    List<TimerImpl> groupByDevice() {
    //        Function<TimerEvent, String> f = new Function<TimerEvent, String>() {
    //            @Override
    //            public String apply(TimerEvent event) {
    //                return event.getDevice();
    //            }
    //        };
    //        return groupBy(f);
    //    }
    //
    //    List<TimerImpl> groupByNode() {
    //        Function<TimerEvent, String> f = new Function<TimerEvent, String>() {
    //            @Override
    //            public String apply(TimerEvent event) {
    //                return event.getNode();
    //            }
    //        };
    //        return groupBy(f);
    //    }

    private <T> List<TimerImpl> groupBy(Function<TimerEvent, T> f) {
        Multimap<T, TimerEvent> index = Multimaps.index(events, f);
        ArrayList<TimerImpl> timers = new ArrayList<TimerImpl>();
        for (T t : index.keySet()) {
            TimerImpl timer = new TimerImpl(hostId);
            timer.events.addAll(index.get(t));
            // Collections.sort(timer.events);
            timers.add(timer);
        }
        return timers;
    }

    public void add(TimerImpl mcTimer) {
        this.events.addAll(mcTimer.events);
    }

    public void clean() {
        ArrayList<TimerEvent> cleaned = new ArrayList<TimerEvent>();
        for (TimerEvent e : events) {
            if (e != null && !e.isEmptyEvent()) {
                cleaned.add(e);
            }
        }
        this.events = cleaned;
    }

    @Override
    public int nrTimes() {
        return events.size();
    }

    private double toDoubleMicroSecondsFromNanos(long nanos) {
        return nanos / 1000.0;
    }

    @Override
    public double totalTimeVal() {
        double total = 0.0;
        for (TimerEvent event : events) {
            total += toDoubleMicroSecondsFromNanos(event.time());
        }
        return total;
    }

    @Override
    public double averageTimeVal() {
        return totalTimeVal() / nrTimes();
    }

    public String averageTime() {
        return ibis.util.Timer.format(averageTimeVal());
    }

    public String totalTime() {
        return ibis.util.Timer.format(totalTimeVal());
    }

    public long getMinimumTime() {
        long min = Long.MAX_VALUE;
        for (TimerEvent event : events) {
            min = Math.min(event.getQueued(), min);
        }
        return min;
    }

    public void normalize(long min) {
        for (TimerEvent event : events) {
            event.normalize(min);
        }
    }

    public String dataTransferOutput() {
        StringBuffer sb = new StringBuffer();
        for (TimerEvent event : events) {
            sb.append(event.toDataTransferString());
        }
        return sb.toString();
    }

    public void append(StringBuffer sb, String node, String device, String thread, long start, long end, String action,
            boolean perThread) {
        sb.append(String.format("%s %s %s\t%f\t%f\t%s\n", node, device, perThread ? thread : "nothread", start / 1e6, end / 1e6,
                action));
    }

    private TimerEvent getOverallEvent() {
        for (TimerEvent event : events) {
            if (event.isOverallEvent()) {
                return event;
            }
        }
        return null;
    }

    private ArrayList<TimerEvent> getFiltered(ArrayList<TimerEvent> events, long endFilter) {
        ArrayList<TimerEvent> filtered = new ArrayList<TimerEvent>();

        for (TimerEvent event : events) {
            if (event.getStart() >= 0 && event.getEnd() > event.getStart() && event.getEnd() < endFilter) {
                filtered.add(event);
                //            } else {
                //                System.out.println("Filtered out event: " + event);
            }

        }

        return filtered;
    }

    /**
     * Filters all events that are not within the 'overall' frame.
     *
     * We filter 10% before the overallStartTime and 10% after to make up for imprecision in synchronizing between nodes.
     */
    public void filterOverall() {

        TimerEvent overallEvent = getOverallEvent();
        if (overallEvent == null) {
            return;
        }

        long startTime = overallEvent.getStart();
        long time = overallEvent.time();
        long startFilter = Math.max(startTime - (long) (0.1 * time), 0);
        long endFilter = time + (long) (2 * 0.1 * time); // because of the
                                                         // normalize below.

        normalize(startFilter);

        ArrayList<TimerEvent> filtered = getFiltered(events, endFilter);
        this.events = filtered;
    }

    public String gnuPlotData(boolean perThread) {
        StringBuffer sb = new StringBuffer();
        if (perThread) {
            Collections.sort(events);
        }
        for (TimerEvent event : events) {
            if (event.getEnd() > 0) {
                append(sb, event.getNode(), event.getDevice(), event.getThread(), event.getStart(), event.getEnd(),
                        event.getAction(), perThread);
            }
        }
        return sb.toString();
    }

    @Override
    public void add(long start, long end) {
        add(new TimerEvent(getNode(), device, thread, action, start, start, start, end));
    }

    @Override
    public void add(String nickName, String thread, String action, long l, long m, long n, long o) {
        add(new TimerEvent(getNode(), nickName, thread, action, l, m, n, o));
    }

    private synchronized void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream stream) throws ClassNotFoundException, IOException {
        stream.defaultReadObject();
    }
}
