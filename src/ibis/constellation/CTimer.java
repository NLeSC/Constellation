package ibis.constellation;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import ibis.constellation.extra.TimeSyncInfo;
import ibis.util.Timer;

public class CTimer implements java.io.Serializable {

    private static class TimerEvent implements java.io.Serializable {

        private static final long serialVersionUID = 1L;

        String node;
        String device;
        String thread;
        String action;

        long queued;
        long submitted;
        long start;
        long end;

        long nrBytes;

        public TimerEvent(String node, String device, String thread,
                String action, long queued, long submitted, long start,
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

        boolean isEmptyEvent() {
            return queued == 0 && submitted == 0 && start == 0 && end == 0;
        }

        boolean isOverallEvent() {
            return device.equals("java") && thread.equals("main")
                    && action.equals("overall");
        }

        boolean hasDataTransfers() {
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

        String toDataTransferString() {
            return String.format(
                    "%-8s  |  %-14s  |  start: %-6s  |  "
                            + "end: %-6s  |  duration: %-6s  | nrBytes: %4.3f MB  |  "
                            + "rate: %13s\n",
                    node, action, Timer.format(start / 1000.0),
                    Timer.format(end / 1000.0),
                    Timer.format((end - start) / 1000.0),
                    nrBytes / 1024.0 / 1024.0, getRateString());
        }

        @Override
        public String toString() {
            return String.format(
                    "%-8s  |  %-10s  |  %-22s  | "
                            + "%-14s  |  queued: %-6s  |  submitted: %-6s  |  "
                            + "start: %-6s  |  end: %-6s\n",
                    node, device, thread, // queueIndex,
                    action, Timer.format(queued / 1000.0),
                    Timer.format(submitted / 1000.0),
                    Timer.format(start / 1000.0), Timer.format(end / 1000.0));
        }
    }

    private static final long serialVersionUID = 1L;

    private ArrayList<TimerEvent> events;

    private final String hostId;
    private final String device;
    private final String thread;
    private final String action;

    public String getAction() {
        if (action == null) {
            TimerEvent event = events.get(0);
            return event.getAction();
        } else {
            return action;
        }
    }

    public String getNode() {
        return hostId;
    }

    public CTimer(String constellation) {
        this(constellation, null, null, null);
    }

    public CTimer(String constellation, String standardDevice,
            String standardThread, String standardAction) {
        this.events = new ArrayList<TimerEvent>();
        this.hostId = constellation;
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

    public int start() {
        int eventNo;
        TimerEvent event = new TimerEvent(getNode(), device, thread, action, 0,
                0, 0, 0);
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
        TimerEvent event = events.get(eventNo);
        event.nrBytes = nrBytes;
    }

    public void stop(int eventNo) {
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

    public List<CTimer> groupByAction() {
        Function<TimerEvent, String> f = new Function<TimerEvent, String>() {
            @Override
            public String apply(TimerEvent event) {
                return event.getAction();
            }
        };
        return groupBy(f);
    }

    List<CTimer> groupByDevice() {
        Function<TimerEvent, String> f = new Function<TimerEvent, String>() {
            @Override
            public String apply(TimerEvent event) {
                return event.getDevice();
            }
        };
        return groupBy(f);
    }

    List<CTimer> groupByNode() {
        Function<TimerEvent, String> f = new Function<TimerEvent, String>() {
            @Override
            public String apply(TimerEvent event) {
                return event.getNode();
            }
        };
        return groupBy(f);
    }

    private <T> List<CTimer> groupBy(Function<TimerEvent, T> f) {
        Multimap<T, TimerEvent> index = Multimaps.index(events, f);
        ArrayList<CTimer> timers = new ArrayList<CTimer>();
        for (T t : index.keySet()) {
            CTimer timer = new CTimer(hostId);
            timer.events.addAll(index.get(t));
            // Collections.sort(timer.events);
            timers.add(timer);
        }
        return timers;
    }

    public void add(CTimer mcTimer) {
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

    public int nrTimes() {
        return events.size();
    }

    private double toDoubleMicroSecondsFromNanos(long nanos) {
        return nanos / 1000.0;
    }

    public double totalTimeVal() {
        double total = 0.0;
        for (TimerEvent event : events) {
            total += toDoubleMicroSecondsFromNanos(event.time());
        }
        return total;
    }

    public double averageTimeVal() {
        return totalTimeVal() / nrTimes();
    }

    public String averageTime() {
        return Timer.format(averageTimeVal());
    }

    public String totalTime() {
        return Timer.format(totalTimeVal());
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

    public String extensiveOutput() {
        StringBuffer sb = new StringBuffer();
        for (TimerEvent event : events) {
            sb.append(event);
        }
        return sb.toString();
    }

    public void append(StringBuffer sb, String node, String device,
            String thread, long start, long end, String action,
            boolean perThread) {
        sb.append(String.format("%s %s %s\t%f\t%f\t%s\n", node, device,
                perThread ? thread : "nothread", start / 1e6, end / 1e6,
                action));
    }

    private boolean isSmallSteal(TimerEvent event) {
        // event is assumed to be a steal event
        return event.getEnd() - event.getStart() < 200000;
    }

    private boolean isTooEarlySteal(TimerEvent event, long startExecute) {
        return event.getStart() < startExecute;
        // event is assumed to be a steal event
    }

    private boolean isUninterestingSteal(TimerEvent event, long startExecute) {
        return event.getAction().equals("steal") && (isSmallSteal(event)
                || isTooEarlySteal(event, startExecute));
    }

    private long getStartTimeExecute() {
        long startExecute = Long.MAX_VALUE;
        for (TimerEvent event : events) {
            if (event.getAction().equals("execute")) {
                if (event.getStart() < startExecute) {
                    startExecute = event.getStart();
                }
            }
        }
        return startExecute;
    }

    private TimerEvent getOverallEvent() {
        for (TimerEvent event : events) {
            if (event.isOverallEvent()) {
                return event;
            }
        }
        return null;
    }

    /**
     * Filters all events that are not within the 'overall' frame.
     *
     * We filter 10% before the overallStartTime and 10% after to make up for
     * imprecision in synchronizing between nodes.
     */
    public void filterOverall() {

        ArrayList<TimerEvent> filtered = new ArrayList<TimerEvent>();

        for (TimerEvent event : events) {
            if (event.getStart() >= 0 && event.getEnd() > event.getStart()) {
                filtered.add(event);
            } else {
                System.out.println("Filtered out event: " + event);
            }

        }

        this.events = filtered;

        TimerEvent overallEvent = getOverallEvent();
        if (overallEvent == null)
            return;

        long startTime = overallEvent.getStart();
        long time = overallEvent.time();
        long startFilter = Math.max(startTime - (long) (0.1 * time), 0);
        long endFilter = time + (long) (2 * 0.1 * time); // because of the
                                                         // normalize below.

        normalize(startFilter);

        filtered = new ArrayList<TimerEvent>();

        for (TimerEvent event : events) {
            if (event.getStart() >= 0 && event.getEnd() < endFilter) {
                filtered.add(event);
            } else {
                System.out.println("Filtered out event: " + event);
            }

        }
        this.events = filtered;
    }

    public void filterSteals() {
        ArrayList<TimerEvent> filtered = new ArrayList<TimerEvent>();
        long startExecute = getStartTimeExecute();
        for (TimerEvent e : events) {
            if (!isUninterestingSteal(e, startExecute)) {
                filtered.add(e);
            }
        }
        this.events = filtered;
    }

    public String gnuPlotData(boolean perThread) {
        StringBuffer sb = new StringBuffer();
        for (TimerEvent event : events) {
            if (event.getEnd() > 0) {
                append(sb, event.getNode(), event.getDevice(),
                        event.getThread(), event.getStart(), event.getEnd(),
                        event.getAction(), perThread);
            }
        }
        return sb.toString();
    }

    public void add(String nickName, String thread, String action, long l,
            long m, long n, long o) {
        add(new TimerEvent(getNode(), nickName, thread, action, l, m, n, o));

    }
}
