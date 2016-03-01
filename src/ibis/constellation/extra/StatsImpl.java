package ibis.constellation.extra;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import ibis.constellation.CTimer;

public class StatsImpl extends ibis.constellation.Stats
        implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private List<CTimer> timers;

    private final String hostId;

    private transient TimeSyncInfo syncInfo;

    // This is the public interface to the rest of the framework.
    public StatsImpl(String hostId) {
        this.hostId = hostId;
        timers = new ArrayList<CTimer>();
    }

    public void setSyncInfo(TimeSyncInfo syncInfo) {
        this.syncInfo = syncInfo;
    }

    public synchronized void add(StatsImpl s) {
        this.timers.addAll(s.timers);
    }

    /**
     * Print the statistics. This is the entry point for the master in the
     * conclusion phase process all statistics. The statistics from all other
     * nodes have already been added to this.
     */
    public void printStats(PrintStream stream) {
        stream.print("\n-------------------------------");
        stream.print(" STATISTICS ");
        stream.println("-------------------------------");

        normalize(syncInfo);

        CTimer timer = getTotalMCTimer();
        timer.filterOverall();
        // System.out.println(timer.extensiveOutput());

        printActions(stream, timer);

        printDataTransfers(stream);

        printPlotData();
    }

    synchronized void addTimer(CTimer timer) {
        timers.add(timer);
    }

    private void printActions(PrintStream stream, CTimer timer) {
        List<CTimer> actionTimers = timer.groupByAction();

        for (CTimer t : actionTimers) {
            print(stream, t.getAction(), t);
        }
    }

    private void printDataTransfers(PrintStream stream) {
        CTimer timer = getTotalMCTimer();
        timer.onlyDataTransfers();

        List<CTimer> actionTimers = timer.groupByAction();

        for (CTimer t : actionTimers) {
            stream.println(t.dataTransferOutput());
        }
    }

    private synchronized void clean() {
        for (CTimer timer : timers) {
            timer.clean();
        }
    }

    private void normalize(TimeSyncInfo timeSyncInfo) {
        clean();
        CTimer timer = getTotalMCTimer();

        normalizeTimer(timer, timeSyncInfo);
    }

    private void normalizeTimer(CTimer timer, TimeSyncInfo timeSyncInfo) {
        timer.equalize(timeSyncInfo);
        long min = Long.MAX_VALUE;
        min = Math.min(timer.getMinimumTime(), min);
        timer.normalize(min);
    }

    private synchronized CTimer getTotalMCTimer() {
        CTimer temp = new CTimer(hostId);
        for (CTimer t : timers) {
            temp.add(t);
        }
        return temp;
    }

    private void write(CTimer timer, String fileName, boolean perThread) {
        PrintStream ps = null;
        try {
            ps = new PrintStream(fileName);
            ps.print(timer.gnuPlotData(perThread));
        } catch (FileNotFoundException e) {
            System.out.println(e);
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    private void printPlotData() {
        CTimer temp = getTotalMCTimer();
        temp.filterOverall();
        write(temp, "gantt.data", false);
        write(temp, "gantt-thread.data", true);
    }

    void print(PrintStream stream, String kind, CTimer t) {
        stream.printf("%-53s %3d %s %s\n", kind, t.nrTimes(), t.averageTime(),
                t.totalTime());
    }

    @Override
    public CTimer getTimer() {
        CTimer timer = new CTimer(hostId);
        addTimer(timer);
        return timer;
    }

    @Override
    public CTimer getTimer(String standardDevice, String standardThread,
            String standardAction) {
        CTimer timer = new CTimer(hostId, standardDevice, standardThread,
                standardAction);
        addTimer(timer);
        return timer;
    }
}
