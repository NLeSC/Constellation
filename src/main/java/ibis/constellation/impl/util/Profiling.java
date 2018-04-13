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

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import ibis.constellation.impl.TimerImpl;

public class Profiling implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private List<TimerImpl> timers;

    private final String hostId;

    private transient TimeSyncInfo syncInfo;

    private TimerImpl overallTimer;

    // This is the public interface to the rest of the framework.
    public Profiling(String hostId) {
        this.hostId = hostId;
        timers = new ArrayList<TimerImpl>();
    }

    public void setSyncInfo(TimeSyncInfo syncInfo) {
        this.syncInfo = syncInfo;
    }

    public synchronized void add(Profiling s) {
        this.timers.addAll(s.timers);
    }

    /**
     * Print the statistics. This is the entry point for the master in the conclusion phase process all statistics. The statistics
     * from all other nodes have already been added to this.
     */
    public void printProfile(String output) {

        PrintStream stream;

        if (output != null) {
            try {
                stream = new PrintStream(new BufferedOutputStream(new FileOutputStream(output + ".data")));
            } catch (FileNotFoundException e) {
                stream = System.out;
            }
        } else {
            stream = System.out;
        }

        normalize(syncInfo);

        TimerImpl timer = getAllTimers();
        timer.filterOverall();
        write(timer, stream, true);
        if (!stream.equals(System.out)) {
            stream.close();
        }
        if (output != null) {
            try {
                stream = new PrintStream(new BufferedOutputStream(new FileOutputStream(output + ".nothread.data")));
            } catch (FileNotFoundException e) {
                return;
            }
            write(timer, stream, false);
            stream.close();
        }
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
        TimerImpl timer = getAllTimers();

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

    private synchronized TimerImpl getAllTimers() {
        TimerImpl temp = new TimerImpl(hostId);
        for (TimerImpl t : timers) {
            temp.add(t);
        }
        return temp;
    }

    private void write(TimerImpl timer, PrintStream ps, boolean perThread) {
        if (ps.equals(System.out)) {
            ps.println("BEGIN PLOT DATA");
        }
        ps.print(timer.gnuPlotData(perThread));
        if (ps.equals(System.out)) {
            ps.println("END PLOT DATA");
        }
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
