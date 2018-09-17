package com.android.internal.os;

import android.os.FileUtils;
import android.os.Process;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.SettingsStringUtil;
import android.system.OsConstants;
import android.util.LogException;
import android.util.Slog;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.os.BatteryStatsImpl.Uid.Proc;
import com.android.internal.util.FastPrintWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import libcore.io.IoUtils;
import libcore.io.Libcore;

public class ProcessCpuTracker {
    private static final boolean DEBUG = false;
    private static final int[] LOAD_AVERAGE_FORMAT = new int[]{16416, 16416, 16416};
    private static final int[] PROCESS_FULL_STATS_FORMAT = new int[]{32, 4640, 32, 32, 32, 32, 32, 32, 32, 8224, 32, 8224, 32, 8224, 8224, 32, 32, 32, 32, 32, 32, 32, 8224};
    static final int PROCESS_FULL_STAT_MAJOR_FAULTS = 2;
    static final int PROCESS_FULL_STAT_MINOR_FAULTS = 1;
    static final int PROCESS_FULL_STAT_STIME = 4;
    static final int PROCESS_FULL_STAT_UTIME = 3;
    static final int PROCESS_FULL_STAT_VSIZE = 5;
    private static final int[] PROCESS_STATS_FORMAT = new int[]{32, MetricsEvent.DIALOG_WIFI_SKIP, 32, 32, 32, 32, 32, 32, 32, 8224, 32, 8224, 32, 8224, 8224};
    static final int PROCESS_STAT_MAJOR_FAULTS = 1;
    static final int PROCESS_STAT_MINOR_FAULTS = 0;
    static final int PROCESS_STAT_STIME = 3;
    static final int PROCESS_STAT_UTIME = 2;
    private static final int[] SYSTEM_CPU_FORMAT = new int[]{288, 8224, 8224, 8224, 8224, 8224, 8224, 8224};
    private static final String TAG = "ProcessCpuTracker";
    private static final boolean localLOGV = false;
    private static final Comparator<Stats> sLoadComparator = new Comparator<Stats>() {
        public final int compare(Stats sta, Stats stb) {
            int i = -1;
            int ta = sta.rel_utime + sta.rel_stime;
            int tb = stb.rel_utime + stb.rel_stime;
            if (ta != tb) {
                if (ta <= tb) {
                    i = 1;
                }
                return i;
            } else if (sta.added != stb.added) {
                if (!sta.added) {
                    i = 1;
                }
                return i;
            } else if (sta.removed == stb.removed) {
                return 0;
            } else {
                if (!sta.added) {
                    i = 1;
                }
                return i;
            }
        }
    };
    private long mBaseIdleTime;
    private long mBaseIoWaitTime;
    private long mBaseIrqTime;
    private long mBaseSoftIrqTime;
    private long mBaseSystemTime;
    private long mBaseUserTime;
    private byte[] mBuffer = new byte[4096];
    private int[] mCurPids;
    private int[] mCurThreadPids;
    private long mCurrentSampleRealTime;
    private long mCurrentSampleTime;
    private long mCurrentSampleWallTime;
    private boolean mFirst = true;
    private final boolean mIncludeThreads;
    private final long mJiffyMillis;
    private long mLastSampleRealTime;
    private long mLastSampleTime;
    private long mLastSampleWallTime;
    private float mLoad1 = 0.0f;
    private float mLoad15 = 0.0f;
    private float mLoad5 = 0.0f;
    private final float[] mLoadAverageData = new float[3];
    private final ArrayList<Stats> mProcStats = new ArrayList();
    private final long[] mProcessFullStatsData = new long[6];
    private final String[] mProcessFullStatsStringData = new String[6];
    private final long[] mProcessStatsData = new long[4];
    private int mRelIdleTime;
    private int mRelIoWaitTime;
    private int mRelIrqTime;
    private int mRelSoftIrqTime;
    private boolean mRelStatsAreGood;
    private int mRelSystemTime;
    private int mRelUserTime;
    private final long[] mSinglePidStatsData = new long[4];
    private final long[] mSystemCpuData = new long[7];
    private final ArrayList<Stats> mWorkingProcs = new ArrayList();
    private boolean mWorkingProcsSorted;

    public interface FilterStats {
        boolean needed(Stats stats);
    }

    public static class Stats {
        public boolean active;
        public boolean added;
        public String baseName;
        public long base_majfaults;
        public long base_minfaults;
        public long base_stime;
        public long base_uptime;
        public long base_utime;
        public Proc batteryStats;
        final String cmdlineFile;
        public boolean interesting;
        public String name;
        public int nameWidth;
        public final int pid;
        public int rel_majfaults;
        public int rel_minfaults;
        public int rel_stime;
        public long rel_uptime;
        public int rel_utime;
        public boolean removed;
        final String statFile;
        final ArrayList<Stats> threadStats;
        final String threadsDir;
        public final int uid;
        public long vsize;
        public boolean working;
        final ArrayList<Stats> workingThreads;

        Stats(int _pid, int parentPid, boolean includeThreads) {
            this.pid = _pid;
            if (parentPid < 0) {
                File procDir = new File("/proc", Integer.toString(this.pid));
                this.statFile = new File(procDir, "stat").toString();
                this.cmdlineFile = new File(procDir, "cmdline").toString();
                this.threadsDir = new File(procDir, "task").toString();
                if (includeThreads) {
                    this.threadStats = new ArrayList();
                    this.workingThreads = new ArrayList();
                } else {
                    this.threadStats = null;
                    this.workingThreads = null;
                }
            } else {
                this.statFile = new File(new File(new File(new File("/proc", Integer.toString(parentPid)), "task"), Integer.toString(this.pid)), "stat").toString();
                this.cmdlineFile = null;
                this.threadsDir = null;
                this.threadStats = null;
                this.workingThreads = null;
            }
            this.uid = FileUtils.getUid(this.statFile.toString());
        }
    }

    public ProcessCpuTracker(boolean includeThreads) {
        this.mIncludeThreads = includeThreads;
        this.mJiffyMillis = 1000 / Libcore.os.sysconf(OsConstants._SC_CLK_TCK);
    }

    public void onLoadChanged(float load1, float load5, float load15) {
    }

    public int onMeasureProcessName(String name) {
        return 0;
    }

    public void init() {
        this.mFirst = true;
        update();
    }

    public void update() {
        long nowUptime = SystemClock.uptimeMillis();
        long nowRealtime = SystemClock.elapsedRealtime();
        long nowWallTime = System.currentTimeMillis();
        long[] sysCpu = this.mSystemCpuData;
        if (Process.readProcFile("/proc/stat", SYSTEM_CPU_FORMAT, null, sysCpu, null)) {
            long usertime = (sysCpu[0] + sysCpu[1]) * this.mJiffyMillis;
            long systemtime = sysCpu[2] * this.mJiffyMillis;
            long idletime = sysCpu[3] * this.mJiffyMillis;
            long iowaittime = sysCpu[4] * this.mJiffyMillis;
            long irqtime = sysCpu[5] * this.mJiffyMillis;
            long softirqtime = sysCpu[6] * this.mJiffyMillis;
            this.mRelUserTime = (int) (usertime - this.mBaseUserTime);
            this.mRelSystemTime = (int) (systemtime - this.mBaseSystemTime);
            this.mRelIoWaitTime = (int) (iowaittime - this.mBaseIoWaitTime);
            this.mRelIrqTime = (int) (irqtime - this.mBaseIrqTime);
            this.mRelSoftIrqTime = (int) (softirqtime - this.mBaseSoftIrqTime);
            this.mRelIdleTime = (int) (idletime - this.mBaseIdleTime);
            this.mRelStatsAreGood = true;
            this.mBaseUserTime = usertime;
            this.mBaseSystemTime = systemtime;
            this.mBaseIoWaitTime = iowaittime;
            this.mBaseIrqTime = irqtime;
            this.mBaseSoftIrqTime = softirqtime;
            this.mBaseIdleTime = idletime;
        }
        this.mLastSampleTime = this.mCurrentSampleTime;
        this.mCurrentSampleTime = nowUptime;
        this.mLastSampleRealTime = this.mCurrentSampleRealTime;
        this.mCurrentSampleRealTime = nowRealtime;
        this.mLastSampleWallTime = this.mCurrentSampleWallTime;
        this.mCurrentSampleWallTime = nowWallTime;
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        try {
            this.mCurPids = collectStats("/proc", -1, this.mFirst, this.mCurPids, this.mProcStats);
            float[] loadAverages = this.mLoadAverageData;
            if (Process.readProcFile("/proc/loadavg", LOAD_AVERAGE_FORMAT, null, null, loadAverages)) {
                float load1 = loadAverages[0];
                float load5 = loadAverages[1];
                float load15 = loadAverages[2];
                if (!(load1 == this.mLoad1 && load5 == this.mLoad5 && load15 == this.mLoad15)) {
                    this.mLoad1 = load1;
                    this.mLoad5 = load5;
                    this.mLoad15 = load15;
                    onLoadChanged(load1, load5, load15);
                }
            }
            this.mWorkingProcsSorted = false;
            this.mFirst = false;
        } finally {
            StrictMode.setThreadPolicy(savedPolicy);
        }
    }

    private int[] collectStats(String statsFile, int parentPid, boolean first, int[] curPids, ArrayList<Stats> allProcs) {
        Stats st;
        int[] pids = Process.getPids(statsFile, curPids);
        int NP = pids == null ? 0 : pids.length;
        int NS = allProcs.size();
        int curStatsIndex = 0;
        int i = 0;
        while (i < NP) {
            int pid = pids[i];
            if (pid < 0) {
                NP = pid;
                break;
            }
            st = curStatsIndex < NS ? (Stats) allProcs.get(curStatsIndex) : null;
            long[] procStats;
            if (st != null && st.pid == pid) {
                st.added = false;
                st.working = false;
                curStatsIndex++;
                if (st.interesting) {
                    long uptime = SystemClock.uptimeMillis();
                    procStats = this.mProcessStatsData;
                    if (Process.readProcFile(st.statFile.toString(), PROCESS_STATS_FORMAT, null, procStats, null)) {
                        long minfaults = procStats[0];
                        long majfaults = procStats[1];
                        long utime = procStats[2] * this.mJiffyMillis;
                        long stime = procStats[3] * this.mJiffyMillis;
                        if (utime == st.base_utime && stime == st.base_stime) {
                            st.rel_utime = 0;
                            st.rel_stime = 0;
                            st.rel_minfaults = 0;
                            st.rel_majfaults = 0;
                            if (st.active) {
                                st.active = false;
                            }
                        } else {
                            if (!st.active) {
                                st.active = true;
                            }
                            if (parentPid < 0) {
                                getName(st, st.cmdlineFile);
                                if (st.threadStats != null) {
                                    this.mCurThreadPids = collectStats(st.threadsDir, pid, false, this.mCurThreadPids, st.threadStats);
                                }
                            }
                            st.rel_uptime = uptime - st.base_uptime;
                            st.base_uptime = uptime;
                            st.rel_utime = (int) (utime - st.base_utime);
                            st.rel_stime = (int) (stime - st.base_stime);
                            st.base_utime = utime;
                            st.base_stime = stime;
                            st.rel_minfaults = (int) (minfaults - st.base_minfaults);
                            st.rel_majfaults = (int) (majfaults - st.base_majfaults);
                            st.base_minfaults = minfaults;
                            st.base_majfaults = majfaults;
                            st.working = true;
                        }
                    }
                }
            } else if (st == null || st.pid > pid) {
                Stats stats = new Stats(pid, parentPid, this.mIncludeThreads);
                allProcs.add(curStatsIndex, stats);
                curStatsIndex++;
                NS++;
                String[] procStatsString = this.mProcessFullStatsStringData;
                procStats = this.mProcessFullStatsData;
                stats.base_uptime = SystemClock.uptimeMillis();
                if (Process.readProcFile(stats.statFile.toString(), PROCESS_FULL_STATS_FORMAT, procStatsString, procStats, null)) {
                    stats.vsize = procStats[5];
                    stats.interesting = true;
                    stats.baseName = procStatsString[0];
                    stats.base_minfaults = procStats[1];
                    stats.base_majfaults = procStats[2];
                    stats.base_utime = procStats[3] * this.mJiffyMillis;
                    stats.base_stime = procStats[4] * this.mJiffyMillis;
                } else {
                    Slog.w(TAG, "Skipping unknown process pid " + pid);
                    stats.baseName = MediaStore.UNKNOWN_STRING;
                    stats.base_stime = 0;
                    stats.base_utime = 0;
                    stats.base_majfaults = 0;
                    stats.base_minfaults = 0;
                }
                if (parentPid < 0) {
                    getName(stats, stats.cmdlineFile);
                    if (stats.threadStats != null) {
                        this.mCurThreadPids = collectStats(stats.threadsDir, pid, true, this.mCurThreadPids, stats.threadStats);
                    }
                } else if (stats.interesting) {
                    stats.name = stats.baseName;
                    stats.nameWidth = onMeasureProcessName(stats.name);
                }
                stats.rel_utime = 0;
                stats.rel_stime = 0;
                stats.rel_minfaults = 0;
                stats.rel_majfaults = 0;
                stats.added = true;
                if (!first && stats.interesting) {
                    stats.working = true;
                }
            } else {
                st.rel_utime = 0;
                st.rel_stime = 0;
                st.rel_minfaults = 0;
                st.rel_majfaults = 0;
                st.removed = true;
                st.working = true;
                allProcs.remove(curStatsIndex);
                NS--;
                i--;
            }
            i++;
        }
        while (curStatsIndex < NS) {
            st = (Stats) allProcs.get(curStatsIndex);
            st.rel_utime = 0;
            st.rel_stime = 0;
            st.rel_minfaults = 0;
            st.rel_majfaults = 0;
            st.removed = true;
            st.working = true;
            allProcs.remove(curStatsIndex);
            NS--;
        }
        return pids;
    }

    public long getCpuTimeForPid(int pid) {
        synchronized (this.mSinglePidStatsData) {
            String statFile = "/proc/" + pid + "/stat";
            long[] statsData = this.mSinglePidStatsData;
            if (Process.readProcFile(statFile, PROCESS_STATS_FORMAT, null, statsData, null)) {
                long j = this.mJiffyMillis * (statsData[2] + statsData[3]);
                return j;
            }
            return 0;
        }
    }

    public final int getLastUserTime() {
        return this.mRelUserTime;
    }

    public final int getLastSystemTime() {
        return this.mRelSystemTime;
    }

    public final int getLastIoWaitTime() {
        return this.mRelIoWaitTime;
    }

    public final int getLastIrqTime() {
        return this.mRelIrqTime;
    }

    public final int getLastSoftIrqTime() {
        return this.mRelSoftIrqTime;
    }

    public final int getLastIdleTime() {
        return this.mRelIdleTime;
    }

    public final boolean hasGoodLastStats() {
        return this.mRelStatsAreGood;
    }

    public final float getTotalCpuPercent() {
        int denom = ((this.mRelUserTime + this.mRelSystemTime) + this.mRelIrqTime) + this.mRelIdleTime;
        if (denom <= 0) {
            return 0.0f;
        }
        return (((float) ((this.mRelUserTime + this.mRelSystemTime) + this.mRelIrqTime)) * 100.0f) / ((float) denom);
    }

    final void buildWorkingProcs() {
        if (!this.mWorkingProcsSorted) {
            this.mWorkingProcs.clear();
            int N = this.mProcStats.size();
            for (int i = 0; i < N; i++) {
                Stats stats = (Stats) this.mProcStats.get(i);
                if (stats.working) {
                    this.mWorkingProcs.add(stats);
                    if (stats.threadStats != null && stats.threadStats.size() > 1) {
                        stats.workingThreads.clear();
                        int M = stats.threadStats.size();
                        for (int j = 0; j < M; j++) {
                            Stats tstats = (Stats) stats.threadStats.get(j);
                            if (tstats.working) {
                                stats.workingThreads.add(tstats);
                            }
                        }
                        Collections.sort(stats.workingThreads, sLoadComparator);
                    }
                }
            }
            Collections.sort(this.mWorkingProcs, sLoadComparator);
            this.mWorkingProcsSorted = true;
        }
    }

    public final int countStats() {
        return this.mProcStats.size();
    }

    public final Stats getStats(int index) {
        return (Stats) this.mProcStats.get(index);
    }

    public final List<Stats> getStats(FilterStats filter) {
        ArrayList<Stats> statses = new ArrayList(this.mProcStats.size());
        int N = this.mProcStats.size();
        for (int p = 0; p < N; p++) {
            Stats stats = (Stats) this.mProcStats.get(p);
            if (filter.needed(stats)) {
                statses.add(stats);
            }
        }
        return statses;
    }

    public final int countWorkingStats() {
        buildWorkingProcs();
        return this.mWorkingProcs.size();
    }

    public final Stats getWorkingStats(int index) {
        return (Stats) this.mWorkingProcs.get(index);
    }

    public final String printCurrentLoad() {
        Writer sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter(sw, false, 128);
        pw.print("Load: ");
        pw.print(this.mLoad1);
        pw.print(" / ");
        pw.print(this.mLoad5);
        pw.print(" / ");
        pw.println(this.mLoad15);
        pw.flush();
        return sw.toString();
    }

    public final String printCurrentState(long now) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        buildWorkingProcs();
        Writer sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter(sw, false, 1024);
        pw.print("CPU usage from ");
        if (now > this.mLastSampleTime) {
            pw.print(now - this.mLastSampleTime);
            pw.print("ms to ");
            pw.print(now - this.mCurrentSampleTime);
            pw.print("ms ago");
        } else {
            pw.print(this.mLastSampleTime - now);
            pw.print("ms to ");
            pw.print(this.mCurrentSampleTime - now);
            pw.print("ms later");
        }
        pw.print(" (");
        pw.print(simpleDateFormat.format(new Date(this.mLastSampleWallTime)));
        pw.print(" to ");
        pw.print(simpleDateFormat.format(new Date(this.mCurrentSampleWallTime)));
        pw.print(")");
        long sampleRealTime = this.mCurrentSampleRealTime - this.mLastSampleRealTime;
        long percAwake = sampleRealTime > 0 ? (100 * (this.mCurrentSampleTime - this.mLastSampleTime)) / sampleRealTime : 0;
        if (percAwake != 100) {
            pw.print(" with ");
            pw.print(percAwake);
            pw.print("% awake");
        }
        pw.println(SettingsStringUtil.DELIMITER);
        int totalTime = ((((this.mRelUserTime + this.mRelSystemTime) + this.mRelIoWaitTime) + this.mRelIrqTime) + this.mRelSoftIrqTime) + this.mRelIdleTime;
        int N = this.mWorkingProcs.size();
        for (int i = 0; i < N; i++) {
            Stats st = (Stats) this.mWorkingProcs.get(i);
            String str = st.added ? " +" : st.removed ? " -" : "  ";
            printProcessCPU(pw, str, st.pid, st.name, (int) st.rel_uptime, st.rel_utime, st.rel_stime, 0, 0, 0, st.rel_minfaults, st.rel_majfaults);
            if (!(st.removed || st.workingThreads == null)) {
                int M = st.workingThreads.size();
                for (int j = 0; j < M; j++) {
                    Stats tst = (Stats) st.workingThreads.get(j);
                    str = tst.added ? "   +" : tst.removed ? "   -" : "    ";
                    printProcessCPU(pw, str, tst.pid, tst.name, (int) st.rel_uptime, tst.rel_utime, tst.rel_stime, 0, 0, 0, 0, 0);
                }
            }
        }
        printProcessCPU(pw, LogException.NO_VALUE, -1, "TOTAL", totalTime, this.mRelUserTime, this.mRelSystemTime, this.mRelIoWaitTime, this.mRelIrqTime, this.mRelSoftIrqTime, 0, 0);
        pw.flush();
        return sw.toString();
    }

    private void printRatio(PrintWriter pw, long numerator, long denominator) {
        long thousands = (1000 * numerator) / denominator;
        long hundreds = thousands / 10;
        pw.print(hundreds);
        if (hundreds < 10) {
            long remainder = thousands - (hundreds * 10);
            if (remainder != 0) {
                pw.print('.');
                pw.print(remainder);
            }
        }
    }

    private void printProcessCPU(PrintWriter pw, String prefix, int pid, String label, int totalTime, int user, int system, int iowait, int irq, int softIrq, int minFaults, int majFaults) {
        pw.print(prefix);
        if (totalTime == 0) {
            totalTime = 1;
        }
        printRatio(pw, (long) ((((user + system) + iowait) + irq) + softIrq), (long) totalTime);
        pw.print("% ");
        if (pid >= 0) {
            pw.print(pid);
            pw.print("/");
        }
        pw.print(label);
        pw.print(": ");
        printRatio(pw, (long) user, (long) totalTime);
        pw.print("% user + ");
        printRatio(pw, (long) system, (long) totalTime);
        pw.print("% kernel");
        if (iowait > 0) {
            pw.print(" + ");
            printRatio(pw, (long) iowait, (long) totalTime);
            pw.print("% iowait");
        }
        if (irq > 0) {
            pw.print(" + ");
            printRatio(pw, (long) irq, (long) totalTime);
            pw.print("% irq");
        }
        if (softIrq > 0) {
            pw.print(" + ");
            printRatio(pw, (long) softIrq, (long) totalTime);
            pw.print("% softirq");
        }
        if (minFaults > 0 || majFaults > 0) {
            pw.print(" / faults:");
            if (minFaults > 0) {
                pw.print(" ");
                pw.print(minFaults);
                pw.print(" minor");
            }
            if (majFaults > 0) {
                pw.print(" ");
                pw.print(majFaults);
                pw.print(" major");
            }
        }
        pw.println();
    }

    private String readFile(String file, char endChar) {
        Throwable th;
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        FileInputStream is = null;
        try {
            FileInputStream is2 = new FileInputStream(file);
            try {
                int len = is2.read(this.mBuffer);
                is2.close();
                if (len > 0) {
                    int i = 0;
                    while (i < len && this.mBuffer[i] != endChar) {
                        i++;
                    }
                    String str = new String(this.mBuffer, 0, i);
                    IoUtils.closeQuietly(is2);
                    StrictMode.setThreadPolicy(savedPolicy);
                    return str;
                }
                IoUtils.closeQuietly(is2);
                StrictMode.setThreadPolicy(savedPolicy);
                is = is2;
                return null;
            } catch (FileNotFoundException e) {
                is = is2;
                IoUtils.closeQuietly(is);
                StrictMode.setThreadPolicy(savedPolicy);
                return null;
            } catch (IOException e2) {
                is = is2;
                IoUtils.closeQuietly(is);
                StrictMode.setThreadPolicy(savedPolicy);
                return null;
            } catch (Throwable th2) {
                th = th2;
                is = is2;
                IoUtils.closeQuietly(is);
                StrictMode.setThreadPolicy(savedPolicy);
                throw th;
            }
        } catch (FileNotFoundException e3) {
            IoUtils.closeQuietly(is);
            StrictMode.setThreadPolicy(savedPolicy);
            return null;
        } catch (IOException e4) {
            IoUtils.closeQuietly(is);
            StrictMode.setThreadPolicy(savedPolicy);
            return null;
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(is);
            StrictMode.setThreadPolicy(savedPolicy);
            throw th;
        }
    }

    private void getName(Stats st, String cmdlineFile) {
        String newName = st.name;
        if (st.name == null || st.name.equals("app_process") || st.name.equals("<pre-initialized>")) {
            String cmdName = readFile(cmdlineFile, 0);
            if (cmdName != null && cmdName.length() > 1) {
                newName = cmdName;
                int i = cmdName.lastIndexOf("/");
                if (i > 0 && i < cmdName.length() - 1) {
                    newName = cmdName.substring(i + 1);
                }
            }
            if (newName == null) {
                newName = st.baseName;
            }
        }
        if (st.name == null || (newName.equals(st.name) ^ 1) != 0) {
            st.name = newName;
            st.nameWidth = onMeasureProcessName(st.name);
        }
    }
}
