package ibis.constellation;

import ibis.constellation.extra.TimeSyncInfo;
import ibis.util.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class CTimer implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private static final AtomicInteger nrQueues = new AtomicInteger();

    private ArrayList<TimerEvent> events;

    private final String hostId;
    private String standardDevice;
    private String standardThread;
    private String standardAction;
    private int standardQueue;

    // not sure
    public static int getNextQueue() {
        return nrQueues.getAndIncrement();
    }

    public int getQueue() {
        return standardQueue;
    }

    public String getAction() {
        if (standardAction == null) {
            TimerEvent event = events.get(0);
            return event.getAction();
        } else {
            return standardAction;
        }
    }

    public String getNode() {
        return hostId;
    }

    public CTimer(String constellation) {
        events = new ArrayList<TimerEvent>();
        this.hostId = constellation;
        this.standardThread = null;
        this.standardAction = null;
        this.standardQueue = getNextQueue();
    }

    public CTimer(String constellation, String standardDevice,
            String standardThread, String standardAction) {
        this.events = new ArrayList<TimerEvent>();
        this.hostId = constellation;
        this.standardDevice = standardDevice;
        this.standardThread = standardThread;
        this.standardAction = standardAction;
        this.standardQueue = getNextQueue();
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
        TimerEvent event = new TimerEvent(getNode(), standardDevice,
                standardThread, standardQueue, standardAction, 0, 0, 0, 0);
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

    public void add(TimerEvent event) {
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
            String thread, int queue, long start, long end, String action,
            boolean perThread) {
        sb.append(String.format("%s %s %s\t%f\t%f\t%s\n", node, device,
                perThread ? thread : "queue" + queue, start / 1e6, end / 1e6,
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
     * imprecisions in synchronizing between nodes.
     */
    public void filterOverall() {
        TimerEvent overallEvent = getOverallEvent();
        if (overallEvent == null)
            return;

        long startTime = overallEvent.getStart();
        long time = overallEvent.time();
        long startFilter = Math.max(startTime - (long) (0.1 * time), 0);
        long endFilter = time + (long) (2 * 0.1 * time); // because of the
                                                         // normalize below.

        normalize(startFilter);

        ArrayList<TimerEvent> filtered = new ArrayList<TimerEvent>();
        // System.out.println("Filter: end = " +
        // Timer.format(endFilter/1000.0));
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
            /*
             * append(sb, event.getNode(), event.getQueue(), event.getQueued(),
             * event.getQueued() + 3000, "queuing for " + event.getAction());
             * append(sb, event.getNode(), event.getQueue(),
             * event.getSubmitted(), event.getSubmitted() + 3000,
             * "submitting for " + event.getAction());
             */
            if (event.getEnd() > 0) {
                append(sb, event.getNode(), event.getDevice(),
                        event.getThread(), event.getQueue(), event.getStart(),
                        event.getEnd(), event.getAction(), perThread);
            }
        }
        return sb.toString();
    }
}
