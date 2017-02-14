package ibis.constellation.impl.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import ibis.constellation.impl.TimerImpl;

public class Stats implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private List<TimerImpl> timers;

    private final String hostId;

    private transient TimeSyncInfo syncInfo;

    private TimerImpl overallTimer;

    // This is the public interface to the rest of the framework.
    public Stats(String hostId) {
        this.hostId = hostId;
        timers = new ArrayList<TimerImpl>();
    }

    public void setSyncInfo(TimeSyncInfo syncInfo) {
        this.syncInfo = syncInfo;
    }

    public synchronized void add(Stats s) {
        this.timers.addAll(s.timers);
    }

    /**
     * Print the statistics. This is the entry point for the master in the conclusion phase process all statistics. The statistics
     * from all other nodes have already been added to this.
     */
    public void printStats(PrintStream stream) {
        stream.print("\n-------------------------------");
        stream.print(" STATISTICS ");
        stream.println("-------------------------------");

        normalize(syncInfo);

        TimerImpl timer = getTotalMCTimer();
        timer.filterOverall();
        printPlotData(stream);
    }

    private synchronized void addTimer(TimerImpl timer) {
        timers.add(timer);
    }

    private synchronized void clean() {
        for (TimerImpl timer : timers) {
            timer.clean();
        }
    }

    private void normalize(TimeSyncInfo timeSyncInfo) {
        clean();
        TimerImpl timer = getTotalMCTimer();

        normalizeTimer(timer, timeSyncInfo);
    }

    private void normalizeTimer(TimerImpl timer, TimeSyncInfo timeSyncInfo) {
        if (timeSyncInfo != null) {
            timer.equalize(timeSyncInfo);
        }
        long min = Long.MAX_VALUE;
        min = Math.min(timer.getMinimumTime(), min);
        timer.normalize(min);
    }

    private synchronized TimerImpl getTotalMCTimer() {
        TimerImpl temp = new TimerImpl(hostId);
        for (TimerImpl t : timers) {
            temp.add(t);
        }
        return temp;
    }

    private void write(TimerImpl timer, PrintStream ps, boolean perThread) {
        ps.println("BEGIN PLOT DATA");
        ps.print(timer.gnuPlotData(perThread));
        ps.println("END PLOT DATA");
    }

    private void printPlotData(PrintStream stream) {
        TimerImpl temp = getTotalMCTimer();
        temp.filterOverall();
        // write(temp, "gantt.data", false);
        // write(temp, "gantt-thread.data", true);
        write(temp, stream, true);
    }

    public void print(PrintStream stream, String kind, TimerImpl t) {
        stream.printf("%-53s %3d %s %s\n", kind, t.nrTimes(), t.averageTime(), t.totalTime());
    }

    public TimerImpl getTimer() {
        TimerImpl timer = new TimerImpl(hostId);
        addTimer(timer);
        return timer;
    }

    public TimerImpl getTimer(String standardDevice, String standardThread, String standardAction) {
        TimerImpl timer = new TimerImpl(hostId, standardDevice, standardThread, standardAction);
        addTimer(timer);
        return timer;
    }

    public synchronized TimerImpl getOverallTimer() {
        if (overallTimer == null) {
            overallTimer = getTimer("java", "main", "overall");
        }
        return overallTimer;

    }
}
