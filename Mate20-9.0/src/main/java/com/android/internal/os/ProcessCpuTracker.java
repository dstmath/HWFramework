package com.android.internal.os;

import android.os.FileUtils;
import android.os.Process;
import android.os.StrictMode;
import android.os.SystemClock;
import android.system.Os;
import android.system.OsConstants;
import android.util.Slog;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.os.BatteryStatsImpl;
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

public class ProcessCpuTracker {
    private static final boolean DEBUG = false;
    private static final int[] LOAD_AVERAGE_FORMAT = {16416, 16416, 16416};
    private static final int[] PROCESS_FULL_STATS_FORMAT = {32, 4640, 32, 32, 32, 32, 32, 32, 32, 8224, 32, 8224, 32, 8224, 8224, 32, 32, 32, 32, 32, 32, 32, 8224};
    static final int PROCESS_FULL_STAT_MAJOR_FAULTS = 2;
    static final int PROCESS_FULL_STAT_MINOR_FAULTS = 1;
    static final int PROCESS_FULL_STAT_STIME = 4;
    static final int PROCESS_FULL_STAT_UTIME = 3;
    static final int PROCESS_FULL_STAT_VSIZE = 5;
    private static final int[] PROCESS_STATS_FORMAT = {32, MetricsProto.MetricsEvent.DIALOG_WIFI_SKIP, 32, 32, 32, 32, 32, 32, 32, 8224, 32, 8224, 32, 8224, 8224};
    static final int PROCESS_STAT_MAJOR_FAULTS = 1;
    static final int PROCESS_STAT_MINOR_FAULTS = 0;
    static final int PROCESS_STAT_STIME = 3;
    static final int PROCESS_STAT_UTIME = 2;
    private static final int[] SYSTEM_CPU_FORMAT = {MetricsProto.MetricsEvent.OVERVIEW_SELECT_TIMEOUT, 8224, 8224, 8224, 8224, 8224, 8224, 8224};
    private static final String TAG = "ProcessCpuTracker";
    private static final boolean localLOGV = false;
    private static final Comparator<Stats> sLoadComparator = new Comparator<Stats>() {
        public final int compare(Stats sta, Stats stb) {
            int ta = sta.rel_utime + sta.rel_stime;
            int tb = stb.rel_utime + stb.rel_stime;
            int i = 1;
            if (ta != tb) {
                if (ta > tb) {
                    i = -1;
                }
                return i;
            } else if (sta.added != stb.added) {
                if (sta.added) {
                    i = -1;
                }
                return i;
            } else if (sta.removed == stb.removed) {
                return 0;
            } else {
                if (sta.added) {
                    i = -1;
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
    private final ArrayList<Stats> mProcStats = new ArrayList<>();
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
    private final ArrayList<Stats> mWorkingProcs = new ArrayList<>();
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
        public BatteryStatsImpl.Uid.Proc batteryStats;
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
                    this.threadStats = new ArrayList<>();
                    this.workingThreads = new ArrayList<>();
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
        this.mJiffyMillis = 1000 / Os.sysconf(OsConstants._SC_CLK_TCK);
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

    /* JADX INFO: finally extract failed */
    public void update() {
        long nowUptime;
        long nowRealtime;
        long nowWallTime;
        long nowUptime2 = SystemClock.uptimeMillis();
        long nowRealtime2 = SystemClock.elapsedRealtime();
        long nowWallTime2 = System.currentTimeMillis();
        long[] sysCpu = this.mSystemCpuData;
        boolean z = true;
        if (Process.readProcFile("/proc/stat", SYSTEM_CPU_FORMAT, null, sysCpu, null)) {
            long usertime = (sysCpu[0] + sysCpu[1]) * this.mJiffyMillis;
            long systemtime = sysCpu[2] * this.mJiffyMillis;
            nowWallTime = nowWallTime2;
            long idletime = sysCpu[3] * this.mJiffyMillis;
            nowRealtime = nowRealtime2;
            long iowaittime = sysCpu[4] * this.mJiffyMillis;
            nowUptime = nowUptime2;
            long irqtime = sysCpu[5] * this.mJiffyMillis;
            long[] jArr = sysCpu;
            long softirqtime = sysCpu[6] * this.mJiffyMillis;
            this.mRelUserTime = (int) (usertime - this.mBaseUserTime);
            this.mRelSystemTime = (int) (systemtime - this.mBaseSystemTime);
            this.mRelIoWaitTime = (int) (iowaittime - this.mBaseIoWaitTime);
            this.mRelIrqTime = (int) (irqtime - this.mBaseIrqTime);
            this.mRelSoftIrqTime = (int) (softirqtime - this.mBaseSoftIrqTime);
            this.mRelIdleTime = (int) (idletime - this.mBaseIdleTime);
            z = true;
            this.mRelStatsAreGood = true;
            this.mBaseUserTime = usertime;
            this.mBaseSystemTime = systemtime;
            this.mBaseIoWaitTime = iowaittime;
            this.mBaseIrqTime = irqtime;
            this.mBaseSoftIrqTime = softirqtime;
            this.mBaseIdleTime = idletime;
        } else {
            nowUptime = nowUptime2;
            nowRealtime = nowRealtime2;
            nowWallTime = nowWallTime2;
            long[] jArr2 = sysCpu;
        }
        this.mLastSampleTime = this.mCurrentSampleTime;
        this.mCurrentSampleTime = nowUptime;
        this.mLastSampleRealTime = this.mCurrentSampleRealTime;
        this.mCurrentSampleRealTime = nowRealtime;
        this.mLastSampleWallTime = this.mCurrentSampleWallTime;
        this.mCurrentSampleWallTime = nowWallTime;
        StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        try {
            char c = z;
            this.mCurPids = collectStats("/proc", -1, this.mFirst, this.mCurPids, this.mProcStats);
            StrictMode.setThreadPolicy(savedPolicy);
            float[] loadAverages = this.mLoadAverageData;
            if (Process.readProcFile("/proc/loadavg", LOAD_AVERAGE_FORMAT, null, null, loadAverages)) {
                float load1 = loadAverages[0];
                float load5 = loadAverages[c];
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
        } catch (Throwable th) {
            StrictMode.setThreadPolicy(savedPolicy);
            throw th;
        }
    }

    private int[] collectStats(String statsFile, int parentPid, boolean first, int[] curPids, ArrayList<Stats> allProcs) {
        int[] pids;
        boolean z;
        int[] pids2;
        int i;
        int NP;
        int i2;
        int pid;
        ArrayList<Stats> arrayList;
        int NS;
        boolean z2;
        long minfaults;
        long majfaults;
        Stats st;
        boolean z3;
        long majfaults2;
        long uptime;
        int[] pids3;
        int i3 = parentPid;
        ArrayList<Stats> arrayList2 = allProcs;
        int[] pids4 = Process.getPids(statsFile, curPids);
        boolean z4 = false;
        int NP2 = pids4 == null ? 0 : pids4.length;
        int curStatsIndex = 0;
        int NS2 = allProcs.size();
        int NS3 = 0;
        while (true) {
            int i4 = NS3;
            if (i4 >= NP2) {
                int i5 = i3;
                pids = pids4;
                int i6 = NP2;
                int i7 = NS2;
                z = true;
                break;
            }
            int pid2 = pids4[i4];
            if (pid2 < 0) {
                int NP3 = pid2;
                int i8 = i3;
                pids = pids4;
                z = true;
                break;
            }
            Stats st2 = curStatsIndex < NS2 ? arrayList2.get(curStatsIndex) : null;
            if (st2 == null || st2.pid != pid2) {
                int pid3 = pid2;
                pids2 = pids4;
                NP = NP2;
                int NS4 = NS2;
                i = i4;
                Stats st3 = st2;
                if (st3 != null) {
                    pid = pid3;
                    if (st3.pid > pid) {
                        arrayList = allProcs;
                    } else {
                        st3.rel_utime = 0;
                        st3.rel_stime = 0;
                        st3.rel_minfaults = 0;
                        st3.rel_majfaults = 0;
                        st3.removed = true;
                        st3.working = true;
                        arrayList2 = allProcs;
                        arrayList2.remove(curStatsIndex);
                        NS2 = NS4 - 1;
                        i--;
                    }
                } else {
                    pid = pid3;
                    arrayList = allProcs;
                }
                i2 = parentPid;
                Stats st4 = new Stats(pid, i2, this.mIncludeThreads);
                arrayList2.add(curStatsIndex, st4);
                int curStatsIndex2 = curStatsIndex + 1;
                NS2 = NS4 + 1;
                String[] procStatsString = this.mProcessFullStatsStringData;
                long[] procStats = this.mProcessFullStatsData;
                st4.base_uptime = SystemClock.uptimeMillis();
                String path = st4.statFile.toString();
                if (Process.readProcFile(path, PROCESS_FULL_STATS_FORMAT, procStatsString, procStats, null)) {
                    st4.vsize = procStats[5];
                    st4.interesting = true;
                    st4.baseName = procStatsString[0];
                    st4.base_minfaults = procStats[1];
                    st4.base_majfaults = procStats[2];
                    st4.base_utime = procStats[3] * this.mJiffyMillis;
                    st4.base_stime = procStats[4] * this.mJiffyMillis;
                } else {
                    Slog.w(TAG, "Skipping unknown process pid " + pid);
                    st4.baseName = "<unknown>";
                    st4.base_stime = 0;
                    st4.base_utime = 0;
                    st4.base_majfaults = 0;
                    st4.base_minfaults = 0;
                }
                if (i2 < 0) {
                    getName(st4, st4.cmdlineFile);
                    if (st4.threadStats != null) {
                        String str = path;
                        this.mCurThreadPids = collectStats(st4.threadsDir, pid, true, this.mCurThreadPids, st4.threadStats);
                    }
                } else {
                    if (st4.interesting) {
                        st4.name = st4.baseName;
                        st4.nameWidth = onMeasureProcessName(st4.name);
                    }
                }
                st4.rel_utime = 0;
                st4.rel_stime = 0;
                st4.rel_minfaults = 0;
                st4.rel_majfaults = 0;
                st4.added = true;
                if (!first && st4.interesting) {
                    st4.working = true;
                }
                curStatsIndex = curStatsIndex2;
                NS3 = i + 1;
                i3 = i2;
                NP2 = NP;
                pids4 = pids2;
                String str2 = statsFile;
                int[] iArr = curPids;
                z4 = false;
            } else {
                st2.added = z4;
                st2.working = z4;
                int curStatsIndex3 = curStatsIndex + 1;
                if (st2.interesting != 0) {
                    long uptime2 = SystemClock.uptimeMillis();
                    long[] procStats2 = this.mProcessStatsData;
                    long uptime3 = uptime2;
                    if (!Process.readProcFile(st2.statFile.toString(), PROCESS_STATS_FORMAT, null, procStats2, null)) {
                        pids2 = pids4;
                        NP = NP2;
                        NS = NS2;
                        i = i4;
                    } else {
                        long minfaults2 = procStats2[0];
                        long majfaults3 = procStats2[1];
                        long minfaults3 = minfaults2;
                        long utime = procStats2[2] * this.mJiffyMillis;
                        NP = NP2;
                        long stime = this.mJiffyMillis * procStats2[3];
                        NS = NS2;
                        i = i4;
                        if (utime == st2.base_utime && stime == st2.base_stime) {
                            st2.rel_utime = 0;
                            st2.rel_stime = 0;
                            st2.rel_minfaults = 0;
                            st2.rel_majfaults = 0;
                            if (st2.active) {
                                st2.active = false;
                            }
                            pids2 = pids4;
                        } else {
                            if (!st2.active) {
                                z2 = true;
                                st2.active = true;
                            } else {
                                z2 = true;
                            }
                            if (i3 < 0) {
                                getName(st2, st2.cmdlineFile);
                                if (st2.threadStats != null) {
                                    majfaults = majfaults3;
                                    uptime = uptime3;
                                    minfaults = minfaults3;
                                    majfaults2 = utime;
                                    st = st2;
                                    int i9 = pid2;
                                    long[] jArr = procStats2;
                                    pids3 = pids4;
                                    z3 = true;
                                    this.mCurThreadPids = collectStats(st2.threadsDir, pid2, false, this.mCurThreadPids, st2.threadStats);
                                    st.rel_uptime = uptime - st.base_uptime;
                                    st.base_uptime = uptime;
                                    st.rel_utime = (int) (majfaults2 - st.base_utime);
                                    st.rel_stime = (int) (stime - st.base_stime);
                                    st.base_utime = majfaults2;
                                    st.base_stime = stime;
                                    st.rel_minfaults = (int) (minfaults - st.base_minfaults);
                                    st.rel_majfaults = (int) (majfaults - st.base_majfaults);
                                    st.base_minfaults = minfaults;
                                    st.base_majfaults = majfaults;
                                    st.working = z3;
                                }
                            }
                            st = st2;
                            int i10 = pid2;
                            long[] jArr2 = procStats2;
                            majfaults = majfaults3;
                            pids3 = pids4;
                            z3 = z2;
                            uptime = uptime3;
                            minfaults = minfaults3;
                            majfaults2 = utime;
                            st.rel_uptime = uptime - st.base_uptime;
                            st.base_uptime = uptime;
                            st.rel_utime = (int) (majfaults2 - st.base_utime);
                            st.rel_stime = (int) (stime - st.base_stime);
                            st.base_utime = majfaults2;
                            st.base_stime = stime;
                            st.rel_minfaults = (int) (minfaults - st.base_minfaults);
                            st.rel_majfaults = (int) (majfaults - st.base_majfaults);
                            st.base_minfaults = minfaults;
                            st.base_majfaults = majfaults;
                            st.working = z3;
                        }
                    }
                } else {
                    pids2 = pids4;
                    NP = NP2;
                    NS = NS2;
                    i = i4;
                }
                curStatsIndex = curStatsIndex3;
                NS2 = NS;
                arrayList2 = allProcs;
            }
            i2 = parentPid;
            NS3 = i + 1;
            i3 = i2;
            NP2 = NP;
            pids4 = pids2;
            String str22 = statsFile;
            int[] iArr2 = curPids;
            z4 = false;
        }
        while (curStatsIndex < NS2) {
            Stats st5 = arrayList2.get(curStatsIndex);
            st5.rel_utime = 0;
            st5.rel_stime = 0;
            st5.rel_minfaults = 0;
            st5.rel_majfaults = 0;
            st5.removed = z;
            st5.working = z;
            arrayList2.remove(curStatsIndex);
            NS2--;
        }
        return pids;
    }

    public long getCpuTimeForPid(int pid) {
        synchronized (this.mSinglePidStatsData) {
            String statFile = "/proc/" + pid + "/stat";
            long[] statsData = this.mSinglePidStatsData;
            if (!Process.readProcFile(statFile, PROCESS_STATS_FORMAT, null, statsData, null)) {
                return 0;
            }
            long j = this.mJiffyMillis * (statsData[2] + statsData[3]);
            return j;
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
        int denom = this.mRelUserTime + this.mRelSystemTime + this.mRelIrqTime + this.mRelIdleTime;
        if (denom <= 0) {
            return 0.0f;
        }
        return (((float) ((this.mRelUserTime + this.mRelSystemTime) + this.mRelIrqTime)) * 100.0f) / ((float) denom);
    }

    /* access modifiers changed from: package-private */
    public final void buildWorkingProcs() {
        if (!this.mWorkingProcsSorted) {
            this.mWorkingProcs.clear();
            int N = this.mProcStats.size();
            for (int i = 0; i < N; i++) {
                Stats stats = this.mProcStats.get(i);
                if (stats.working) {
                    this.mWorkingProcs.add(stats);
                    if (stats.threadStats != null && stats.threadStats.size() > 1) {
                        stats.workingThreads.clear();
                        int M = stats.threadStats.size();
                        for (int j = 0; j < M; j++) {
                            Stats tstats = stats.threadStats.get(j);
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
        return this.mProcStats.get(index);
    }

    public final List<Stats> getStats(FilterStats filter) {
        ArrayList<Stats> statses = new ArrayList<>(this.mProcStats.size());
        int N = this.mProcStats.size();
        for (int p = 0; p < N; p++) {
            Stats stats = this.mProcStats.get(p);
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
        return this.mWorkingProcs.get(index);
    }

    public final String printCurrentLoad() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter((Writer) sw, false, 128);
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        buildWorkingProcs();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter((Writer) sw, false, 1024);
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
        pw.print(sdf.format(new Date(this.mLastSampleWallTime)));
        pw.print(" to ");
        pw.print(sdf.format(new Date(this.mCurrentSampleWallTime)));
        pw.print(")");
        long sampleTime = this.mCurrentSampleTime - this.mLastSampleTime;
        long sampleRealTime = this.mCurrentSampleRealTime - this.mLastSampleRealTime;
        long j = 0;
        if (sampleRealTime > 0) {
            j = (sampleTime * 100) / sampleRealTime;
        }
        long percAwake = j;
        if (percAwake != 100) {
            pw.print(" with ");
            pw.print(percAwake);
            pw.print("% awake");
        }
        pw.println(":");
        int totalTime = this.mRelUserTime + this.mRelSystemTime + this.mRelIoWaitTime + this.mRelIrqTime + this.mRelSoftIrqTime + this.mRelIdleTime;
        int N = this.mWorkingProcs.size();
        int j2 = 0;
        while (true) {
            int i = j2;
            if (i < N) {
                Stats st = this.mWorkingProcs.get(i);
                long percAwake2 = percAwake;
                int i2 = i;
                int N2 = N;
                PrintWriter pw2 = pw;
                StringWriter sw2 = sw;
                SimpleDateFormat sdf2 = sdf;
                printProcessCPU(pw, st.added ? " +" : st.removed ? " -" : "  ", st.pid, st.name, (int) st.rel_uptime, st.rel_utime, st.rel_stime, 0, 0, 0, st.rel_minfaults, st.rel_majfaults);
                Stats st2 = st;
                if (!st2.removed && st2.workingThreads != null) {
                    int M = st2.workingThreads.size();
                    int j3 = 0;
                    while (true) {
                        int j4 = j3;
                        if (j4 >= M) {
                            break;
                        }
                        Stats tst = st2.workingThreads.get(j4);
                        Stats stats = tst;
                        printProcessCPU(pw2, tst.added ? "   +" : tst.removed ? "   -" : "    ", tst.pid, tst.name, (int) st2.rel_uptime, tst.rel_utime, tst.rel_stime, 0, 0, 0, 0, 0);
                        j3 = j4 + 1;
                        M = M;
                        st2 = st2;
                    }
                }
                j2 = i2 + 1;
                sdf = sdf2;
                percAwake = percAwake2;
                N = N2;
                pw = pw2;
                sw = sw2;
            } else {
                long j5 = percAwake;
                PrintWriter pw3 = pw;
                SimpleDateFormat simpleDateFormat = sdf;
                printProcessCPU(pw3, "", -1, "TOTAL", totalTime, this.mRelUserTime, this.mRelSystemTime, this.mRelIoWaitTime, this.mRelIrqTime, this.mRelSoftIrqTime, 0, 0);
                pw3.flush();
                return sw.toString();
            }
        }
    }

    private void printRatio(PrintWriter pw, long numerator, long denominator) {
        long thousands = (1000 * numerator) / denominator;
        long hundreds = thousands / 10;
        pw.print(hundreds);
        if (hundreds < 10) {
            long remainder = thousands - (10 * hundreds);
            if (remainder != 0) {
                pw.print('.');
                pw.print(remainder);
            }
        }
    }

    private void printProcessCPU(PrintWriter pw, String prefix, int pid, String label, int totalTime, int user, int system, int iowait, int irq, int softIrq, int minFaults, int majFaults) {
        PrintWriter printWriter = pw;
        int i = pid;
        int i2 = user;
        int i3 = system;
        int i4 = iowait;
        int i5 = irq;
        int i6 = softIrq;
        int i7 = minFaults;
        int i8 = majFaults;
        pw.print(prefix);
        int totalTime2 = totalTime == 0 ? 1 : totalTime;
        printRatio(printWriter, (long) (i2 + i3 + i4 + i5 + i6), (long) totalTime2);
        printWriter.print("% ");
        if (i >= 0) {
            printWriter.print(i);
            printWriter.print("/");
        }
        printWriter.print(label);
        printWriter.print(": ");
        PrintWriter printWriter2 = printWriter;
        printRatio(printWriter2, (long) i2, (long) totalTime2);
        printWriter.print("% user + ");
        printRatio(printWriter2, (long) i3, (long) totalTime2);
        printWriter.print("% kernel");
        if (i4 > 0) {
            printWriter.print(" + ");
            printRatio(printWriter, (long) i4, (long) totalTime2);
            printWriter.print("% iowait");
        }
        if (i5 > 0) {
            printWriter.print(" + ");
            printRatio(printWriter, (long) i5, (long) totalTime2);
            printWriter.print("% irq");
        }
        if (i6 > 0) {
            printWriter.print(" + ");
            printRatio(printWriter, (long) i6, (long) totalTime2);
            printWriter.print("% softirq");
        }
        if (i7 > 0 || i8 > 0) {
            printWriter.print(" / faults:");
            if (i7 > 0) {
                printWriter.print(" ");
                printWriter.print(i7);
                printWriter.print(" minor");
            }
            if (i8 > 0) {
                printWriter.print(" ");
                printWriter.print(i8);
                printWriter.print(" major");
            }
        }
        pw.println();
    }

    private String readFile(String file, char endChar) {
        StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            int len = is.read(this.mBuffer);
            is.close();
            if (len > 0) {
                int i = 0;
                while (true) {
                    if (i >= len) {
                        break;
                    } else if (this.mBuffer[i] == endChar) {
                        break;
                    } else {
                        i++;
                    }
                }
                String str = new String(this.mBuffer, 0, i);
                IoUtils.closeQuietly(is);
                StrictMode.setThreadPolicy(savedPolicy);
                return str;
            }
        } catch (FileNotFoundException | IOException e) {
        } catch (Throwable th) {
            IoUtils.closeQuietly(null);
            StrictMode.setThreadPolicy(savedPolicy);
            throw th;
        }
        IoUtils.closeQuietly(is);
        StrictMode.setThreadPolicy(savedPolicy);
        return null;
    }

    private void getName(Stats st, String cmdlineFile) {
        String newName = st.name;
        if (st.name == null || st.name.equals("app_process") || st.name.equals("<pre-initialized>")) {
            String cmdName = readFile(cmdlineFile, 0);
            if (cmdName != null && cmdName.length() > 1) {
                newName = cmdName;
                int i = newName.lastIndexOf("/");
                if (i > 0 && i < newName.length() - 1) {
                    newName = newName.substring(i + 1);
                }
            }
            if (newName == null) {
                newName = st.baseName;
            }
        }
        if (st.name == null || !newName.equals(st.name)) {
            st.name = newName;
            st.nameWidth = onMeasureProcessName(st.name);
        }
    }
}
