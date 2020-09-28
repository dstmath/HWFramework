package com.android.internal.app.procstats;

import android.content.ComponentName;
import android.os.Debug;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SettingsStringUtil;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.DebugUtils;
import android.util.LongSparseArray;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import com.android.internal.app.ProcessMap;
import com.android.internal.app.procstats.AssociationState;
import com.android.internal.content.NativeLibraryHelper;
import dalvik.system.VMRuntime;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProcessStats implements Parcelable {
    public static final int ADD_PSS_EXTERNAL = 3;
    public static final int ADD_PSS_EXTERNAL_SLOW = 4;
    public static final int ADD_PSS_INTERNAL_ALL_MEM = 1;
    public static final int ADD_PSS_INTERNAL_ALL_POLL = 2;
    public static final int ADD_PSS_INTERNAL_SINGLE = 0;
    public static final int ADJ_COUNT = 8;
    public static final int ADJ_MEM_FACTOR_COUNT = 4;
    public static final int ADJ_MEM_FACTOR_CRITICAL = 3;
    public static final int ADJ_MEM_FACTOR_LOW = 2;
    public static final int ADJ_MEM_FACTOR_MODERATE = 1;
    public static final int ADJ_MEM_FACTOR_NORMAL = 0;
    public static final int ADJ_NOTHING = -1;
    public static final int ADJ_SCREEN_MOD = 4;
    public static final int ADJ_SCREEN_OFF = 0;
    public static final int ADJ_SCREEN_ON = 4;
    public static final int[] ALL_MEM_ADJ = {0, 1, 2, 3};
    public static final int[] ALL_PROC_STATES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
    public static final int[] ALL_SCREEN_ADJ = {0, 4};
    public static final int[] BACKGROUND_PROC_STATES = {2, 3, 4, 8, 5, 6, 7};
    static final int[] BAD_TABLE = new int[0];
    public static long COMMIT_PERIOD = 10800000;
    public static long COMMIT_UPTIME_PERIOD = 3600000;
    public static final Parcelable.Creator<ProcessStats> CREATOR = new Parcelable.Creator<ProcessStats>() {
        /* class com.android.internal.app.procstats.ProcessStats.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ProcessStats createFromParcel(Parcel in) {
            return new ProcessStats(in);
        }

        @Override // android.os.Parcelable.Creator
        public ProcessStats[] newArray(int size) {
            return new ProcessStats[size];
        }
    };
    static final boolean DEBUG = false;
    static final boolean DEBUG_PARCEL = false;
    public static final int FLAG_COMPLETE = 1;
    public static final int FLAG_SHUTDOWN = 2;
    public static final int FLAG_SYSPROPS = 4;
    private static final long INVERSE_PROC_STATE_WARNING_MIN_INTERVAL_MS = 10000;
    private static final int MAGIC = 1347638356;
    public static final int[] NON_CACHED_PROC_STATES = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    public static final int[] OPTIONS = {1, 2, 4, 8, 14, 15};
    public static final String[] OPTIONS_STR = {"proc", "pkg-proc", "pkg-svc", "pkg-asc", "pkg-all", "all"};
    private static final int PARCEL_VERSION = 36;
    public static final int PSS_AVERAGE = 2;
    public static final int PSS_COUNT = 10;
    public static final int PSS_MAXIMUM = 3;
    public static final int PSS_MINIMUM = 1;
    public static final int PSS_RSS_AVERAGE = 8;
    public static final int PSS_RSS_MAXIMUM = 9;
    public static final int PSS_RSS_MINIMUM = 7;
    public static final int PSS_SAMPLE_COUNT = 0;
    public static final int PSS_USS_AVERAGE = 5;
    public static final int PSS_USS_MAXIMUM = 6;
    public static final int PSS_USS_MINIMUM = 4;
    public static final int REPORT_ALL = 15;
    public static final int REPORT_PKG_ASC_STATS = 8;
    public static final int REPORT_PKG_PROC_STATS = 2;
    public static final int REPORT_PKG_STATS = 14;
    public static final int REPORT_PKG_SVC_STATS = 4;
    public static final int REPORT_PROC_STATS = 1;
    public static final String SERVICE_NAME = "procstats";
    public static final int STATE_BACKUP = 4;
    public static final int STATE_CACHED_ACTIVITY = 11;
    public static final int STATE_CACHED_ACTIVITY_CLIENT = 12;
    public static final int STATE_CACHED_EMPTY = 13;
    public static final int STATE_COUNT = 14;
    public static final int STATE_HEAVY_WEIGHT = 8;
    public static final int STATE_HOME = 9;
    public static final int STATE_IMPORTANT_BACKGROUND = 3;
    public static final int STATE_IMPORTANT_FOREGROUND = 2;
    public static final int STATE_LAST_ACTIVITY = 10;
    public static final int STATE_NOTHING = -1;
    public static final int STATE_PERSISTENT = 0;
    public static final int STATE_RECEIVER = 7;
    public static final int STATE_SERVICE = 5;
    public static final int STATE_SERVICE_RESTARTING = 6;
    public static final int STATE_TOP = 1;
    public static final int SYS_MEM_USAGE_CACHED_AVERAGE = 2;
    public static final int SYS_MEM_USAGE_CACHED_MAXIMUM = 3;
    public static final int SYS_MEM_USAGE_CACHED_MINIMUM = 1;
    public static final int SYS_MEM_USAGE_COUNT = 16;
    public static final int SYS_MEM_USAGE_FREE_AVERAGE = 5;
    public static final int SYS_MEM_USAGE_FREE_MAXIMUM = 6;
    public static final int SYS_MEM_USAGE_FREE_MINIMUM = 4;
    public static final int SYS_MEM_USAGE_KERNEL_AVERAGE = 11;
    public static final int SYS_MEM_USAGE_KERNEL_MAXIMUM = 12;
    public static final int SYS_MEM_USAGE_KERNEL_MINIMUM = 10;
    public static final int SYS_MEM_USAGE_NATIVE_AVERAGE = 14;
    public static final int SYS_MEM_USAGE_NATIVE_MAXIMUM = 15;
    public static final int SYS_MEM_USAGE_NATIVE_MINIMUM = 13;
    public static final int SYS_MEM_USAGE_SAMPLE_COUNT = 0;
    public static final int SYS_MEM_USAGE_ZRAM_AVERAGE = 8;
    public static final int SYS_MEM_USAGE_ZRAM_MAXIMUM = 9;
    public static final int SYS_MEM_USAGE_ZRAM_MINIMUM = 7;
    public static final String TAG = "ProcessStats";
    private static final Pattern sPageTypeRegex = Pattern.compile("^Node\\s+(\\d+),.* zone\\s+(\\w+),.* type\\s+(\\w+)\\s+([\\s\\d]+?)\\s*$");
    ArrayMap<String, Integer> mCommonStringToIndex;
    public long mExternalPssCount;
    public long mExternalPssTime;
    public long mExternalSlowPssCount;
    public long mExternalSlowPssTime;
    public int mFlags;
    boolean mHasSwappedOutPss;
    ArrayList<String> mIndexToCommonString;
    public long mInternalAllMemPssCount;
    public long mInternalAllMemPssTime;
    public long mInternalAllPollPssCount;
    public long mInternalAllPollPssTime;
    public long mInternalSinglePssCount;
    public long mInternalSinglePssTime;
    public int mMemFactor;
    public final long[] mMemFactorDurations;
    private long mNextInverseProcStateWarningUptime;
    public final ProcessMap<LongSparseArray<PackageState>> mPackages;
    private final ArrayList<String> mPageTypeLabels;
    private final ArrayList<Integer> mPageTypeNodes;
    private final ArrayList<int[]> mPageTypeSizes;
    private final ArrayList<String> mPageTypeZones;
    public final ProcessMap<ProcessState> mProcesses;
    public String mReadError;
    boolean mRunning;
    String mRuntime;
    private int mSkippedInverseProcStateWarningCount;
    public long mStartTime;
    public final SysMemUsageTable mSysMemUsage;
    public final long[] mSysMemUsageArgs;
    public final SparseMappingTable mTableData;
    public long mTimePeriodEndRealtime;
    public long mTimePeriodEndUptime;
    public long mTimePeriodStartClock;
    public String mTimePeriodStartClockStr;
    public long mTimePeriodStartRealtime;
    public long mTimePeriodStartUptime;
    public final ArrayList<AssociationState.SourceState> mTrackingAssociations;

    public ProcessStats(boolean running) {
        this.mPackages = new ProcessMap<>();
        this.mProcesses = new ProcessMap<>();
        this.mTrackingAssociations = new ArrayList<>();
        this.mMemFactorDurations = new long[8];
        this.mMemFactor = -1;
        this.mTableData = new SparseMappingTable();
        this.mSysMemUsageArgs = new long[16];
        this.mSysMemUsage = new SysMemUsageTable(this.mTableData);
        this.mPageTypeNodes = new ArrayList<>();
        this.mPageTypeZones = new ArrayList<>();
        this.mPageTypeLabels = new ArrayList<>();
        this.mPageTypeSizes = new ArrayList<>();
        this.mRunning = running;
        reset();
        if (running) {
            Debug.MemoryInfo info = new Debug.MemoryInfo();
            Debug.getMemoryInfo(Process.myPid(), info);
            this.mHasSwappedOutPss = info.hasSwappedOutPss();
        }
    }

    public ProcessStats(Parcel in) {
        this.mPackages = new ProcessMap<>();
        this.mProcesses = new ProcessMap<>();
        this.mTrackingAssociations = new ArrayList<>();
        this.mMemFactorDurations = new long[8];
        this.mMemFactor = -1;
        this.mTableData = new SparseMappingTable();
        this.mSysMemUsageArgs = new long[16];
        this.mSysMemUsage = new SysMemUsageTable(this.mTableData);
        this.mPageTypeNodes = new ArrayList<>();
        this.mPageTypeZones = new ArrayList<>();
        this.mPageTypeLabels = new ArrayList<>();
        this.mPageTypeSizes = new ArrayList<>();
        reset();
        readFromParcel(in);
    }

    public void add(ProcessStats other) {
        ArrayMap<String, SparseArray<ProcessState>> procMap;
        ProcessState thisProc;
        int NPROCS;
        SparseArray<LongSparseArray<PackageState>> uids;
        LongSparseArray<PackageState> versions;
        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap;
        int NASCS;
        int NSRVS;
        PackageState otherState;
        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap2 = other.mPackages.getMap();
        for (int ip = 0; ip < pkgMap2.size(); ip++) {
            String pkgName = pkgMap2.keyAt(ip);
            SparseArray<LongSparseArray<PackageState>> uids2 = pkgMap2.valueAt(ip);
            for (int iu = 0; iu < uids2.size(); iu++) {
                int uid = uids2.keyAt(iu);
                LongSparseArray<PackageState> versions2 = uids2.valueAt(iu);
                int iv = 0;
                while (iv < versions2.size()) {
                    long vers = versions2.keyAt(iv);
                    PackageState otherState2 = versions2.valueAt(iv);
                    int NPROCS2 = otherState2.mProcesses.size();
                    int NSRVS2 = otherState2.mServices.size();
                    int NASCS2 = otherState2.mAssociations.size();
                    int iproc = 0;
                    while (iproc < NPROCS2) {
                        ProcessState otherProc = otherState2.mProcesses.valueAt(iproc);
                        if (otherProc.getCommonProcess() != otherProc) {
                            pkgMap = pkgMap2;
                            uids = uids2;
                            NSRVS = NSRVS2;
                            versions = versions2;
                            NASCS = NASCS2;
                            NPROCS = NPROCS2;
                            otherState = otherState2;
                            ProcessState thisProc2 = getProcessStateLocked(pkgName, uid, vers, otherProc.getName());
                            if (thisProc2.getCommonProcess() == thisProc2) {
                                thisProc2.setMultiPackage(true);
                                long now = SystemClock.uptimeMillis();
                                vers = vers;
                                PackageState pkgState = getPackageStateLocked(pkgName, uid, vers);
                                thisProc2 = thisProc2.clone(now);
                                pkgState.mProcesses.put(thisProc2.getName(), thisProc2);
                            } else {
                                vers = vers;
                            }
                            thisProc2.add(otherProc);
                        } else {
                            NPROCS = NPROCS2;
                            otherState = otherState2;
                            uids = uids2;
                            NSRVS = NSRVS2;
                            versions = versions2;
                            NASCS = NASCS2;
                            pkgMap = pkgMap2;
                        }
                        iproc++;
                        otherState2 = otherState;
                        NSRVS2 = NSRVS;
                        NASCS2 = NASCS;
                        pkgMap2 = pkgMap;
                        versions2 = versions;
                        uids2 = uids;
                        NPROCS2 = NPROCS;
                    }
                    int isvc = 0;
                    for (int NSRVS3 = NSRVS2; isvc < NSRVS3; NSRVS3 = NSRVS3) {
                        ServiceState otherSvc = otherState2.mServices.valueAt(isvc);
                        getServiceStateLocked(pkgName, uid, vers, otherSvc.getProcessName(), otherSvc.getName()).add(otherSvc);
                        isvc++;
                    }
                    for (int iasc = 0; iasc < NASCS2; iasc++) {
                        AssociationState otherAsc = otherState2.mAssociations.valueAt(iasc);
                        getAssociationStateLocked(pkgName, uid, vers, otherAsc.getProcessName(), otherAsc.getName()).add(otherAsc);
                    }
                    iv++;
                    pkgMap2 = pkgMap2;
                    versions2 = versions2;
                    uids2 = uids2;
                }
            }
        }
        ArrayMap<String, SparseArray<ProcessState>> procMap2 = other.mProcesses.getMap();
        for (int ip2 = 0; ip2 < procMap2.size(); ip2++) {
            SparseArray<ProcessState> uids3 = procMap2.valueAt(ip2);
            int iu2 = 0;
            while (iu2 < uids3.size()) {
                int uid2 = uids3.keyAt(iu2);
                ProcessState otherProc2 = uids3.valueAt(iu2);
                String name = otherProc2.getName();
                String pkg = otherProc2.getPackage();
                long vers2 = otherProc2.getVersion();
                ProcessState thisProc3 = this.mProcesses.get(name, uid2);
                if (thisProc3 == null) {
                    procMap = procMap2;
                    thisProc = new ProcessState(this, pkg, uid2, vers2, name);
                    this.mProcesses.put(name, uid2, thisProc);
                    PackageState thisState = getPackageStateLocked(pkg, uid2, vers2);
                    if (!thisState.mProcesses.containsKey(name)) {
                        thisState.mProcesses.put(name, thisProc);
                    }
                } else {
                    procMap = procMap2;
                    thisProc = thisProc3;
                }
                thisProc.add(otherProc2);
                iu2++;
                procMap2 = procMap;
            }
        }
        for (int i = 0; i < 8; i++) {
            long[] jArr = this.mMemFactorDurations;
            jArr[i] = jArr[i] + other.mMemFactorDurations[i];
        }
        this.mSysMemUsage.mergeStats(other.mSysMemUsage);
        long j = other.mTimePeriodStartClock;
        if (j < this.mTimePeriodStartClock) {
            this.mTimePeriodStartClock = j;
            this.mTimePeriodStartClockStr = other.mTimePeriodStartClockStr;
        }
        this.mTimePeriodEndRealtime += other.mTimePeriodEndRealtime - other.mTimePeriodStartRealtime;
        this.mTimePeriodEndUptime += other.mTimePeriodEndUptime - other.mTimePeriodStartUptime;
        this.mInternalSinglePssCount += other.mInternalSinglePssCount;
        this.mInternalSinglePssTime += other.mInternalSinglePssTime;
        this.mInternalAllMemPssCount += other.mInternalAllMemPssCount;
        this.mInternalAllMemPssTime += other.mInternalAllMemPssTime;
        this.mInternalAllPollPssCount += other.mInternalAllPollPssCount;
        this.mInternalAllPollPssTime += other.mInternalAllPollPssTime;
        this.mExternalPssCount += other.mExternalPssCount;
        this.mExternalPssTime += other.mExternalPssTime;
        this.mExternalSlowPssCount += other.mExternalSlowPssCount;
        this.mExternalSlowPssTime += other.mExternalSlowPssTime;
        this.mHasSwappedOutPss |= other.mHasSwappedOutPss;
    }

    public void addSysMemUsage(long cachedMem, long freeMem, long zramMem, long kernelMem, long nativeMem) {
        int i = this.mMemFactor;
        if (i != -1) {
            int state = i * 14;
            this.mSysMemUsageArgs[0] = 1;
            for (int i2 = 0; i2 < 3; i2++) {
                long[] jArr = this.mSysMemUsageArgs;
                jArr[i2 + 1] = cachedMem;
                jArr[i2 + 4] = freeMem;
                jArr[i2 + 7] = zramMem;
                jArr[i2 + 10] = kernelMem;
                jArr[i2 + 13] = nativeMem;
            }
            this.mSysMemUsage.mergeStats(state, this.mSysMemUsageArgs, 0);
        }
    }

    public void computeTotalMemoryUse(TotalMemoryUseCollection data, long now) {
        int i;
        long[] totalMemUsage;
        long j = now;
        data.totalTime = 0;
        int i2 = 0;
        while (true) {
            i = 0;
            if (i2 >= 14) {
                break;
            }
            data.processStateWeight[i2] = 0.0d;
            data.processStatePss[i2] = 0;
            data.processStateTime[i2] = 0;
            data.processStateSamples[i2] = 0;
            i2++;
        }
        for (int i3 = 0; i3 < 16; i3++) {
            data.sysMemUsage[i3] = 0;
        }
        data.sysMemCachedWeight = 0.0d;
        data.sysMemFreeWeight = 0.0d;
        data.sysMemZRamWeight = 0.0d;
        data.sysMemKernelWeight = 0.0d;
        data.sysMemNativeWeight = 0.0d;
        data.sysMemSamples = 0;
        long[] totalMemUsage2 = this.mSysMemUsage.getTotalMemUsage();
        int is = 0;
        while (is < data.screenStates.length) {
            int im = 0;
            while (im < data.memStates.length) {
                int memBucket = data.screenStates[is] + data.memStates[im];
                int stateBucket = memBucket * 14;
                long memTime = this.mMemFactorDurations[memBucket];
                if (this.mMemFactor == memBucket) {
                    memTime += j - this.mStartTime;
                }
                data.totalTime += memTime;
                int sysKey = this.mSysMemUsage.getKey((byte) stateBucket);
                long[] longs = totalMemUsage2;
                int idx = 0;
                if (sysKey != -1) {
                    long[] tmpLongs = this.mSysMemUsage.getArrayForKey(sysKey);
                    int tmpIndex = SparseMappingTable.getIndexFromKey(sysKey);
                    if (tmpLongs[tmpIndex + 0] >= 3) {
                        totalMemUsage = totalMemUsage2;
                        SysMemUsageTable.mergeSysMemUsage(data.sysMemUsage, i, longs, 0);
                        longs = tmpLongs;
                        idx = tmpIndex;
                    } else {
                        totalMemUsage = totalMemUsage2;
                    }
                } else {
                    totalMemUsage = totalMemUsage2;
                }
                data.sysMemCachedWeight += ((double) longs[idx + 2]) * ((double) memTime);
                data.sysMemFreeWeight += ((double) longs[idx + 5]) * ((double) memTime);
                data.sysMemZRamWeight += ((double) longs[idx + 8]) * ((double) memTime);
                data.sysMemKernelWeight += ((double) longs[idx + 11]) * ((double) memTime);
                data.sysMemNativeWeight += ((double) longs[idx + 14]) * ((double) memTime);
                data.sysMemSamples = (int) (((long) data.sysMemSamples) + longs[idx + 0]);
                im++;
                j = now;
                totalMemUsage2 = totalMemUsage;
                is = is;
                i = 0;
            }
            is++;
            j = now;
            i = 0;
        }
        data.hasSwappedOutPss = this.mHasSwappedOutPss;
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        for (int iproc = 0; iproc < procMap.size(); iproc++) {
            SparseArray<ProcessState> uids = procMap.valueAt(iproc);
            for (int iu = 0; iu < uids.size(); iu++) {
                uids.valueAt(iu).aggregatePss(data, now);
            }
        }
    }

    public void reset() {
        resetCommon();
        this.mPackages.getMap().clear();
        this.mProcesses.getMap().clear();
        this.mMemFactor = -1;
        this.mStartTime = 0;
    }

    public void resetSafely() {
        resetCommon();
        long now = SystemClock.uptimeMillis();
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        for (int ip = procMap.size() - 1; ip >= 0; ip--) {
            SparseArray<ProcessState> uids = procMap.valueAt(ip);
            for (int iu = uids.size() - 1; iu >= 0; iu--) {
                uids.valueAt(iu).tmpNumInUse = 0;
            }
        }
        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        for (int ip2 = pkgMap.size() - 1; ip2 >= 0; ip2--) {
            SparseArray<LongSparseArray<PackageState>> uids2 = pkgMap.valueAt(ip2);
            for (int iu2 = uids2.size() - 1; iu2 >= 0; iu2--) {
                LongSparseArray<PackageState> vpkgs = uids2.valueAt(iu2);
                for (int iv = vpkgs.size() - 1; iv >= 0; iv--) {
                    PackageState pkgState = vpkgs.valueAt(iv);
                    for (int iproc = pkgState.mProcesses.size() - 1; iproc >= 0; iproc--) {
                        ProcessState ps = pkgState.mProcesses.valueAt(iproc);
                        if (ps.isInUse()) {
                            ps.resetSafely(now);
                            ps.getCommonProcess().tmpNumInUse++;
                            ps.getCommonProcess().tmpFoundSubProc = ps;
                        } else {
                            pkgState.mProcesses.valueAt(iproc).makeDead();
                            pkgState.mProcesses.removeAt(iproc);
                        }
                    }
                    for (int isvc = pkgState.mServices.size() - 1; isvc >= 0; isvc--) {
                        ServiceState ss = pkgState.mServices.valueAt(isvc);
                        if (ss.isInUse()) {
                            ss.resetSafely(now);
                        } else {
                            pkgState.mServices.removeAt(isvc);
                        }
                    }
                    for (int iasc = pkgState.mAssociations.size() - 1; iasc >= 0; iasc--) {
                        AssociationState as = pkgState.mAssociations.valueAt(iasc);
                        if (as.isInUse()) {
                            as.resetSafely(now);
                        } else {
                            pkgState.mAssociations.removeAt(iasc);
                        }
                    }
                    if (pkgState.mProcesses.size() <= 0 && pkgState.mServices.size() <= 0 && pkgState.mAssociations.size() <= 0) {
                        vpkgs.removeAt(iv);
                    }
                }
                if (vpkgs.size() <= 0) {
                    uids2.removeAt(iu2);
                }
            }
            if (uids2.size() <= 0) {
                pkgMap.removeAt(ip2);
            }
        }
        for (int ip3 = procMap.size() - 1; ip3 >= 0; ip3--) {
            SparseArray<ProcessState> uids3 = procMap.valueAt(ip3);
            for (int iu3 = uids3.size() - 1; iu3 >= 0; iu3--) {
                ProcessState ps2 = uids3.valueAt(iu3);
                if (!ps2.isInUse() && ps2.tmpNumInUse <= 0) {
                    ps2.makeDead();
                    uids3.removeAt(iu3);
                } else if (ps2.isActive() || !ps2.isMultiPackage() || ps2.tmpNumInUse != 1) {
                    ps2.resetSafely(now);
                } else {
                    ProcessState ps3 = ps2.tmpFoundSubProc;
                    ps3.makeStandalone();
                    uids3.setValueAt(iu3, ps3);
                }
            }
            if (uids3.size() <= 0) {
                procMap.removeAt(ip3);
            }
        }
        this.mStartTime = now;
    }

    private void resetCommon() {
        this.mTimePeriodStartClock = System.currentTimeMillis();
        buildTimePeriodStartClockStr();
        long elapsedRealtime = SystemClock.elapsedRealtime();
        this.mTimePeriodEndRealtime = elapsedRealtime;
        this.mTimePeriodStartRealtime = elapsedRealtime;
        long uptimeMillis = SystemClock.uptimeMillis();
        this.mTimePeriodEndUptime = uptimeMillis;
        this.mTimePeriodStartUptime = uptimeMillis;
        this.mInternalSinglePssCount = 0;
        this.mInternalSinglePssTime = 0;
        this.mInternalAllMemPssCount = 0;
        this.mInternalAllMemPssTime = 0;
        this.mInternalAllPollPssCount = 0;
        this.mInternalAllPollPssTime = 0;
        this.mExternalPssCount = 0;
        this.mExternalPssTime = 0;
        this.mExternalSlowPssCount = 0;
        this.mExternalSlowPssTime = 0;
        this.mTableData.reset();
        Arrays.fill(this.mMemFactorDurations, 0L);
        this.mSysMemUsage.resetTable();
        this.mStartTime = 0;
        this.mReadError = null;
        this.mFlags = 0;
        evaluateSystemProperties(true);
        updateFragmentation();
    }

    public boolean evaluateSystemProperties(boolean update) {
        boolean changed = false;
        String runtime = SystemProperties.get("persist.sys.dalvik.vm.lib.2", VMRuntime.getRuntime().vmLibrary());
        if (!Objects.equals(runtime, this.mRuntime)) {
            changed = true;
            if (update) {
                this.mRuntime = runtime;
            }
        }
        return changed;
    }

    private void buildTimePeriodStartClockStr() {
        this.mTimePeriodStartClockStr = DateFormat.format("yyyy-MM-dd-HH-mm-ss", this.mTimePeriodStartClock).toString();
    }

    public void updateFragmentation() {
        Integer node;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader("/proc/pagetypeinfo"));
            Matcher matcher = sPageTypeRegex.matcher("");
            this.mPageTypeNodes.clear();
            this.mPageTypeZones.clear();
            this.mPageTypeLabels.clear();
            this.mPageTypeSizes.clear();
            while (true) {
                String line = reader2.readLine();
                if (line == null) {
                    try {
                        reader2.close();
                        return;
                    } catch (IOException e) {
                        return;
                    }
                } else {
                    matcher.reset(line);
                    if (matcher.matches() && (node = Integer.valueOf(matcher.group(1), 10)) != null) {
                        this.mPageTypeNodes.add(node);
                        this.mPageTypeZones.add(matcher.group(2));
                        this.mPageTypeLabels.add(matcher.group(3));
                        this.mPageTypeSizes.add(splitAndParseNumbers(matcher.group(4)));
                    }
                }
            }
        } catch (IOException e2) {
            this.mPageTypeNodes.clear();
            this.mPageTypeZones.clear();
            this.mPageTypeLabels.clear();
            this.mPageTypeSizes.clear();
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e3) {
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e4) {
                }
            }
            throw th;
        }
    }

    /* JADX INFO: Multiple debug info for r3v2 int[]: [D('i' int), D('result' int[])] */
    private static int[] splitAndParseNumbers(String s) {
        boolean digit = false;
        int count = 0;
        int N = s.length();
        for (int i = 0; i < N; i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                digit = false;
            } else if (!digit) {
                digit = true;
                count++;
            }
        }
        int[] result = new int[count];
        int p = 0;
        int val = 0;
        for (int i2 = 0; i2 < N; i2++) {
            char c2 = s.charAt(i2);
            if (c2 < '0' || c2 > '9') {
                if (digit) {
                    digit = false;
                    result[p] = val;
                    p++;
                }
            } else if (!digit) {
                digit = true;
                val = c2 - '0';
            } else {
                val = (val * 10) + (c2 - '0');
            }
        }
        if (count > 0) {
            result[count - 1] = val;
        }
        return result;
    }

    private void writeCompactedLongArray(Parcel out, long[] array, int num) {
        for (int i = 0; i < num; i++) {
            long val = array[i];
            if (val < 0) {
                Slog.w(TAG, "Time val negative: " + val);
                val = 0;
            }
            if (val <= 2147483647L) {
                out.writeInt((int) val);
            } else {
                out.writeInt(~((int) (2147483647L & (val >> 32))));
                out.writeInt((int) (4294967295L & val));
            }
        }
    }

    private void readCompactedLongArray(Parcel in, int version, long[] array, int num) {
        if (version <= 10) {
            in.readLongArray(array);
            return;
        }
        int alen = array.length;
        if (num <= alen) {
            int i = 0;
            while (i < num) {
                int val = in.readInt();
                if (val >= 0) {
                    array[i] = (long) val;
                } else {
                    array[i] = (((long) (~val)) << 32) | ((long) in.readInt());
                }
                i++;
            }
            while (i < alen) {
                array[i] = 0;
                i++;
            }
            return;
        }
        throw new RuntimeException("bad array lengths: got " + num + " array is " + alen);
    }

    /* access modifiers changed from: package-private */
    public void writeCommonString(Parcel out, String name) {
        Integer index = this.mCommonStringToIndex.get(name);
        if (index != null) {
            out.writeInt(index.intValue());
            return;
        }
        Integer index2 = Integer.valueOf(this.mCommonStringToIndex.size());
        this.mCommonStringToIndex.put(name, index2);
        out.writeInt(~index2.intValue());
        out.writeString(name);
    }

    /* access modifiers changed from: package-private */
    public String readCommonString(Parcel in, int version) {
        if (version <= 9) {
            return in.readString();
        }
        int index = in.readInt();
        if (index >= 0) {
            return this.mIndexToCommonString.get(index);
        }
        int index2 = ~index;
        String name = in.readString();
        while (this.mIndexToCommonString.size() <= index2) {
            this.mIndexToCommonString.add(null);
        }
        this.mIndexToCommonString.set(index2, name);
        return name;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        writeToParcel(out, SystemClock.uptimeMillis(), flags);
    }

    public void writeToParcel(Parcel out, long now, int flags) {
        out.writeInt(MAGIC);
        out.writeInt(36);
        out.writeInt(14);
        out.writeInt(8);
        out.writeInt(10);
        out.writeInt(16);
        out.writeInt(4096);
        this.mCommonStringToIndex = new ArrayMap<>(this.mProcesses.size());
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        int NPROC = procMap.size();
        for (int ip = 0; ip < NPROC; ip++) {
            SparseArray<ProcessState> uids = procMap.valueAt(ip);
            int NUID = uids.size();
            for (int iu = 0; iu < NUID; iu++) {
                uids.valueAt(iu).commitStateTime(now);
            }
        }
        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        int NPKG = pkgMap.size();
        for (int ip2 = 0; ip2 < NPKG; ip2++) {
            SparseArray<LongSparseArray<PackageState>> uids2 = pkgMap.valueAt(ip2);
            int NUID2 = uids2.size();
            for (int iu2 = 0; iu2 < NUID2; iu2++) {
                LongSparseArray<PackageState> vpkgs = uids2.valueAt(iu2);
                int NVERS = vpkgs.size();
                int iv = 0;
                while (iv < NVERS) {
                    PackageState pkgState = vpkgs.valueAt(iv);
                    int NPROCS = pkgState.mProcesses.size();
                    int iproc = 0;
                    while (iproc < NPROCS) {
                        ProcessState proc = pkgState.mProcesses.valueAt(iproc);
                        if (proc.getCommonProcess() != proc) {
                            proc.commitStateTime(now);
                        }
                        iproc++;
                        NPROCS = NPROCS;
                        vpkgs = vpkgs;
                    }
                    int NSRVS = pkgState.mServices.size();
                    for (int isvc = 0; isvc < NSRVS; isvc++) {
                        pkgState.mServices.valueAt(isvc).commitStateTime(now);
                    }
                    int NASCS = pkgState.mAssociations.size();
                    int iasc = 0;
                    while (iasc < NASCS) {
                        pkgState.mAssociations.valueAt(iasc).commitStateTime(now);
                        iasc++;
                        NSRVS = NSRVS;
                    }
                    iv++;
                    uids2 = uids2;
                    NUID2 = NUID2;
                    vpkgs = vpkgs;
                }
            }
        }
        out.writeLong(this.mTimePeriodStartClock);
        out.writeLong(this.mTimePeriodStartRealtime);
        out.writeLong(this.mTimePeriodEndRealtime);
        out.writeLong(this.mTimePeriodStartUptime);
        out.writeLong(this.mTimePeriodEndUptime);
        out.writeLong(this.mInternalSinglePssCount);
        out.writeLong(this.mInternalSinglePssTime);
        out.writeLong(this.mInternalAllMemPssCount);
        out.writeLong(this.mInternalAllMemPssTime);
        out.writeLong(this.mInternalAllPollPssCount);
        out.writeLong(this.mInternalAllPollPssTime);
        out.writeLong(this.mExternalPssCount);
        out.writeLong(this.mExternalPssTime);
        out.writeLong(this.mExternalSlowPssCount);
        out.writeLong(this.mExternalSlowPssTime);
        out.writeString(this.mRuntime);
        out.writeInt(this.mHasSwappedOutPss ? 1 : 0);
        out.writeInt(this.mFlags);
        this.mTableData.writeToParcel(out);
        int i = this.mMemFactor;
        if (i != -1) {
            long[] jArr = this.mMemFactorDurations;
            jArr[i] = jArr[i] + (now - this.mStartTime);
            this.mStartTime = now;
        }
        long[] jArr2 = this.mMemFactorDurations;
        writeCompactedLongArray(out, jArr2, jArr2.length);
        this.mSysMemUsage.writeToParcel(out);
        out.writeInt(NPROC);
        for (int ip3 = 0; ip3 < NPROC; ip3++) {
            writeCommonString(out, procMap.keyAt(ip3));
            SparseArray<ProcessState> uids3 = procMap.valueAt(ip3);
            int NUID3 = uids3.size();
            out.writeInt(NUID3);
            for (int iu3 = 0; iu3 < NUID3; iu3++) {
                out.writeInt(uids3.keyAt(iu3));
                ProcessState proc2 = uids3.valueAt(iu3);
                writeCommonString(out, proc2.getPackage());
                out.writeLong(proc2.getVersion());
                proc2.writeToParcel(out, now);
            }
        }
        out.writeInt(NPKG);
        for (int ip4 = 0; ip4 < NPKG; ip4++) {
            writeCommonString(out, pkgMap.keyAt(ip4));
            SparseArray<LongSparseArray<PackageState>> uids4 = pkgMap.valueAt(ip4);
            int NUID4 = uids4.size();
            out.writeInt(NUID4);
            for (int iu4 = 0; iu4 < NUID4; iu4++) {
                out.writeInt(uids4.keyAt(iu4));
                LongSparseArray<PackageState> vpkgs2 = uids4.valueAt(iu4);
                int NVERS2 = vpkgs2.size();
                out.writeInt(NVERS2);
                int iv2 = 0;
                while (iv2 < NVERS2) {
                    out.writeLong(vpkgs2.keyAt(iv2));
                    PackageState pkgState2 = vpkgs2.valueAt(iv2);
                    int NPROCS2 = pkgState2.mProcesses.size();
                    out.writeInt(NPROCS2);
                    int iproc2 = 0;
                    while (iproc2 < NPROCS2) {
                        writeCommonString(out, pkgState2.mProcesses.keyAt(iproc2));
                        ProcessState proc3 = pkgState2.mProcesses.valueAt(iproc2);
                        if (proc3.getCommonProcess() == proc3) {
                            out.writeInt(0);
                        } else {
                            out.writeInt(1);
                            proc3.writeToParcel(out, now);
                        }
                        iproc2++;
                        NPROCS2 = NPROCS2;
                        NPKG = NPKG;
                    }
                    int NSRVS2 = pkgState2.mServices.size();
                    out.writeInt(NSRVS2);
                    int isvc2 = 0;
                    while (isvc2 < NSRVS2) {
                        out.writeString(pkgState2.mServices.keyAt(isvc2));
                        ServiceState svc = pkgState2.mServices.valueAt(isvc2);
                        writeCommonString(out, svc.getProcessName());
                        svc.writeToParcel(out, now);
                        isvc2++;
                        NSRVS2 = NSRVS2;
                    }
                    int NASCS2 = pkgState2.mAssociations.size();
                    out.writeInt(NASCS2);
                    int iasc2 = 0;
                    while (iasc2 < NASCS2) {
                        writeCommonString(out, pkgState2.mAssociations.keyAt(iasc2));
                        AssociationState asc = pkgState2.mAssociations.valueAt(iasc2);
                        writeCommonString(out, asc.getProcessName());
                        asc.writeToParcel(this, out, now);
                        iasc2++;
                        pkgState2 = pkgState2;
                    }
                    iv2++;
                    procMap = procMap;
                    NPROC = NPROC;
                    pkgMap = pkgMap;
                    NPKG = NPKG;
                }
            }
        }
        int NPAGETYPES = this.mPageTypeLabels.size();
        out.writeInt(NPAGETYPES);
        for (int i2 = 0; i2 < NPAGETYPES; i2++) {
            out.writeInt(this.mPageTypeNodes.get(i2).intValue());
            out.writeString(this.mPageTypeZones.get(i2));
            out.writeString(this.mPageTypeLabels.get(i2));
            out.writeIntArray(this.mPageTypeSizes.get(i2));
        }
        this.mCommonStringToIndex = null;
    }

    private boolean readCheckedInt(Parcel in, int val, String what) {
        int got = in.readInt();
        if (got == val) {
            return true;
        }
        this.mReadError = "bad " + what + ": " + got;
        return false;
    }

    static byte[] readFully(InputStream stream, int[] outLen) throws IOException {
        int pos = 0;
        int initialAvail = stream.available();
        byte[] data = new byte[(initialAvail > 0 ? initialAvail + 1 : 16384)];
        while (true) {
            int amt = stream.read(data, pos, data.length - pos);
            if (amt < 0) {
                outLen[0] = pos;
                return data;
            }
            pos += amt;
            if (pos >= data.length) {
                byte[] newData = new byte[(pos + 16384)];
                System.arraycopy(data, 0, newData, 0, pos);
                data = newData;
            }
        }
    }

    public void read(InputStream stream) {
        try {
            int[] len = new int[1];
            byte[] raw = readFully(stream, len);
            Parcel in = Parcel.obtain();
            in.unmarshall(raw, 0, len[0]);
            in.setDataPosition(0);
            stream.close();
            readFromParcel(in);
        } catch (IOException e) {
            this.mReadError = "caught exception: " + e;
        }
    }

    public void readFromParcel(Parcel in) {
        LongSparseArray<PackageState> vpkg;
        int uid;
        String associationName;
        AssociationState asc;
        int NSRVS;
        String serviceName;
        long vers;
        PackageState pkgState;
        ServiceState serv;
        int NPROC;
        String procName;
        int uid2;
        boolean hadData = this.mPackages.getMap().size() > 0 || this.mProcesses.getMap().size() > 0;
        if (hadData) {
            resetSafely();
        }
        if (readCheckedInt(in, MAGIC, "magic number")) {
            int version = in.readInt();
            if (version != 36) {
                this.mReadError = "bad version: " + version;
            } else if (readCheckedInt(in, 14, "state count") && readCheckedInt(in, 8, "adj count") && readCheckedInt(in, 10, "pss count") && readCheckedInt(in, 16, "sys mem usage count") && readCheckedInt(in, 4096, "longs size")) {
                this.mIndexToCommonString = new ArrayList<>();
                this.mTimePeriodStartClock = in.readLong();
                buildTimePeriodStartClockStr();
                this.mTimePeriodStartRealtime = in.readLong();
                this.mTimePeriodEndRealtime = in.readLong();
                this.mTimePeriodStartUptime = in.readLong();
                this.mTimePeriodEndUptime = in.readLong();
                this.mInternalSinglePssCount = in.readLong();
                this.mInternalSinglePssTime = in.readLong();
                this.mInternalAllMemPssCount = in.readLong();
                this.mInternalAllMemPssTime = in.readLong();
                this.mInternalAllPollPssCount = in.readLong();
                this.mInternalAllPollPssTime = in.readLong();
                this.mExternalPssCount = in.readLong();
                this.mExternalPssTime = in.readLong();
                this.mExternalSlowPssCount = in.readLong();
                this.mExternalSlowPssTime = in.readLong();
                this.mRuntime = in.readString();
                this.mHasSwappedOutPss = in.readInt() != 0;
                this.mFlags = in.readInt();
                this.mTableData.readFromParcel(in);
                long[] jArr = this.mMemFactorDurations;
                readCompactedLongArray(in, version, jArr, jArr.length);
                if (this.mSysMemUsage.readFromParcel(in)) {
                    int NPROC2 = in.readInt();
                    if (NPROC2 < 0) {
                        this.mReadError = "bad process count: " + NPROC2;
                        return;
                    }
                    int NPROC3 = NPROC2;
                    while (NPROC3 > 0) {
                        int NPROC4 = NPROC3 - 1;
                        String procName2 = readCommonString(in, version);
                        if (procName2 == null) {
                            this.mReadError = "bad process name";
                            return;
                        }
                        int NUID = in.readInt();
                        if (NUID < 0) {
                            this.mReadError = "bad uid count: " + NUID;
                            return;
                        }
                        while (NUID > 0) {
                            int NUID2 = NUID - 1;
                            int uid3 = in.readInt();
                            if (uid3 < 0) {
                                this.mReadError = "bad uid: " + uid3;
                                return;
                            }
                            String pkgName = readCommonString(in, version);
                            if (pkgName == null) {
                                this.mReadError = "bad process package name";
                                return;
                            }
                            long vers2 = in.readLong();
                            ProcessState proc = hadData ? this.mProcesses.get(procName2, uid3) : null;
                            if (proc == null) {
                                uid2 = uid3;
                                procName = procName2;
                                proc = new ProcessState(this, pkgName, uid3, vers2, procName2);
                                if (!proc.readFromParcel(in, true)) {
                                    return;
                                }
                            } else if (proc.readFromParcel(in, false)) {
                                uid2 = uid3;
                                procName = procName2;
                            } else {
                                return;
                            }
                            this.mProcesses.put(procName, uid2, proc);
                            procName2 = procName;
                            NUID = NUID2;
                        }
                        NPROC3 = NPROC4;
                    }
                    int NUID3 = in.readInt();
                    if (NUID3 < 0) {
                        this.mReadError = "bad package count: " + NUID3;
                        return;
                    }
                    while (NUID3 > 0) {
                        int NPKG = NUID3 - 1;
                        String pkgName2 = readCommonString(in, version);
                        if (pkgName2 == null) {
                            this.mReadError = "bad package name";
                            return;
                        }
                        int NVERS = in.readInt();
                        if (NVERS < 0) {
                            this.mReadError = "bad uid count: " + NVERS;
                            return;
                        }
                        while (NVERS > 0) {
                            int NUID4 = NVERS - 1;
                            int uid4 = in.readInt();
                            if (uid4 < 0) {
                                this.mReadError = "bad uid: " + uid4;
                                return;
                            }
                            int NVERS2 = in.readInt();
                            if (NVERS2 < 0) {
                                this.mReadError = "bad versions count: " + NVERS2;
                                return;
                            }
                            while (NVERS2 > 0) {
                                int NVERS3 = NVERS2 - 1;
                                long vers3 = in.readLong();
                                int uid5 = uid4;
                                PackageState pkgState2 = new PackageState(this, pkgName2, uid4, vers3);
                                LongSparseArray<PackageState> vpkg2 = this.mPackages.get(pkgName2, uid5);
                                if (vpkg2 == null) {
                                    LongSparseArray<PackageState> vpkg3 = new LongSparseArray<>();
                                    this.mPackages.put(pkgName2, uid5, vpkg3);
                                    vpkg = vpkg3;
                                } else {
                                    vpkg = vpkg2;
                                }
                                long vers4 = vers3;
                                vpkg.put(vers4, pkgState2);
                                int NPROCS = in.readInt();
                                if (NPROCS < 0) {
                                    this.mReadError = "bad package process count: " + NPROCS;
                                    return;
                                }
                                int NPROCS2 = NPROCS;
                                while (NPROCS2 > 0) {
                                    NPROCS2--;
                                    String procName3 = readCommonString(in, version);
                                    if (procName3 == null) {
                                        this.mReadError = "bad package process name";
                                        return;
                                    }
                                    int hasProc = in.readInt();
                                    ProcessState commonProc = this.mProcesses.get(procName3, uid5);
                                    if (commonProc == null) {
                                        this.mReadError = "no common proc: " + procName3;
                                        return;
                                    }
                                    if (hasProc != 0) {
                                        ProcessState proc2 = hadData ? pkgState2.mProcesses.get(procName3) : null;
                                        if (proc2 != null) {
                                            NPROC = NPROC3;
                                            if (!proc2.readFromParcel(in, false)) {
                                                return;
                                            }
                                        } else {
                                            NPROC = NPROC3;
                                            proc2 = new ProcessState(commonProc, pkgName2, uid5, vers4, procName3, 0);
                                            if (!proc2.readFromParcel(in, true)) {
                                                return;
                                            }
                                        }
                                        pkgState2.mProcesses.put(procName3, proc2);
                                    } else {
                                        NPROC = NPROC3;
                                        pkgState2.mProcesses.put(procName3, commonProc);
                                    }
                                    vpkg = vpkg;
                                    NPROC3 = NPROC;
                                }
                                int NSRVS2 = in.readInt();
                                if (NSRVS2 < 0) {
                                    this.mReadError = "bad package service count: " + NSRVS2;
                                    return;
                                }
                                for (int NSRVS3 = NSRVS2; NSRVS3 > 0; NSRVS3 = NSRVS) {
                                    int NSRVS4 = NSRVS3 - 1;
                                    String serviceName2 = in.readString();
                                    if (serviceName2 == null) {
                                        this.mReadError = "bad package service name";
                                        return;
                                    }
                                    String processName = version > 9 ? readCommonString(in, version) : null;
                                    ServiceState serv2 = hadData ? pkgState2.mServices.get(serviceName2) : null;
                                    if (serv2 == null) {
                                        vers = vers4;
                                        serviceName = serviceName2;
                                        NSRVS = NSRVS4;
                                        pkgState = pkgState2;
                                        serv = new ServiceState(this, pkgName2, serviceName2, processName, null);
                                    } else {
                                        vers = vers4;
                                        serviceName = serviceName2;
                                        NSRVS = NSRVS4;
                                        pkgState = pkgState2;
                                        serv = serv2;
                                    }
                                    if (serv.readFromParcel(in)) {
                                        pkgState.mServices.put(serviceName, serv);
                                        pkgState2 = pkgState;
                                        vers4 = vers;
                                    } else {
                                        return;
                                    }
                                }
                                int NASCS = in.readInt();
                                if (NASCS < 0) {
                                    this.mReadError = "bad package association count: " + NASCS;
                                    return;
                                }
                                while (NASCS > 0) {
                                    int NASCS2 = NASCS - 1;
                                    String associationName2 = readCommonString(in, version);
                                    if (associationName2 == null) {
                                        this.mReadError = "bad package association name";
                                        return;
                                    }
                                    String processName2 = readCommonString(in, version);
                                    AssociationState asc2 = hadData ? pkgState2.mAssociations.get(associationName2) : null;
                                    if (asc2 == null) {
                                        uid = uid5;
                                        associationName = associationName2;
                                        asc = new AssociationState(this, pkgState2, associationName2, processName2, null);
                                    } else {
                                        uid = uid5;
                                        associationName = associationName2;
                                        asc = asc2;
                                    }
                                    String errorMsg = asc.readFromParcel(this, in, version);
                                    if (errorMsg != null) {
                                        this.mReadError = errorMsg;
                                        return;
                                    }
                                    pkgState2.mAssociations.put(associationName, asc);
                                    NASCS = NASCS2;
                                    uid5 = uid;
                                }
                                pkgName2 = pkgName2;
                                NVERS2 = NVERS3;
                                uid4 = uid5;
                                NPROC3 = NPROC3;
                            }
                            NVERS = NUID4;
                        }
                        NUID3 = NPKG;
                    }
                    int NPAGETYPES = in.readInt();
                    this.mPageTypeNodes.clear();
                    this.mPageTypeNodes.ensureCapacity(NPAGETYPES);
                    this.mPageTypeZones.clear();
                    this.mPageTypeZones.ensureCapacity(NPAGETYPES);
                    this.mPageTypeLabels.clear();
                    this.mPageTypeLabels.ensureCapacity(NPAGETYPES);
                    this.mPageTypeSizes.clear();
                    this.mPageTypeSizes.ensureCapacity(NPAGETYPES);
                    for (int i = 0; i < NPAGETYPES; i++) {
                        this.mPageTypeNodes.add(Integer.valueOf(in.readInt()));
                        this.mPageTypeZones.add(in.readString());
                        this.mPageTypeLabels.add(in.readString());
                        this.mPageTypeSizes.add(in.createIntArray());
                    }
                    this.mIndexToCommonString = null;
                }
            }
        }
    }

    public PackageState getPackageStateLocked(String packageName, int uid, long vers) {
        LongSparseArray<PackageState> vpkg = this.mPackages.get(packageName, uid);
        if (vpkg == null) {
            vpkg = new LongSparseArray<>();
            this.mPackages.put(packageName, uid, vpkg);
        }
        PackageState as = vpkg.get(vers);
        if (as != null) {
            return as;
        }
        PackageState as2 = new PackageState(this, packageName, uid, vers);
        vpkg.put(vers, as2);
        return as2;
    }

    public ProcessState getProcessStateLocked(String packageName, int uid, long vers, String processName) {
        return getProcessStateLocked(getPackageStateLocked(packageName, uid, vers), processName);
    }

    public ProcessState getProcessStateLocked(PackageState pkgState, String processName) {
        ProcessState commonProc;
        String str;
        ProcessState ps;
        ProcessState ps2 = pkgState.mProcesses.get(processName);
        if (ps2 != null) {
            return ps2;
        }
        ProcessState commonProc2 = this.mProcesses.get(processName, pkgState.mUid);
        if (commonProc2 == null) {
            commonProc = new ProcessState(this, pkgState.mPackageName, pkgState.mUid, pkgState.mVersionCode, processName);
            this.mProcesses.put(processName, pkgState.mUid, commonProc);
        } else {
            commonProc = commonProc2;
        }
        if (commonProc.isMultiPackage()) {
            str = processName;
            ps = new ProcessState(commonProc, pkgState.mPackageName, pkgState.mUid, pkgState.mVersionCode, processName, SystemClock.uptimeMillis());
        } else if (!pkgState.mPackageName.equals(commonProc.getPackage()) || pkgState.mVersionCode != commonProc.getVersion()) {
            commonProc.setMultiPackage(true);
            long now = SystemClock.uptimeMillis();
            PackageState commonPkgState = getPackageStateLocked(commonProc.getPackage(), pkgState.mUid, commonProc.getVersion());
            if (commonPkgState != null) {
                ProcessState cloned = commonProc.clone(now);
                commonPkgState.mProcesses.put(commonProc.getName(), cloned);
                for (int i = commonPkgState.mServices.size() - 1; i >= 0; i--) {
                    ServiceState ss = commonPkgState.mServices.valueAt(i);
                    if (ss.getProcess() == commonProc) {
                        ss.setProcess(cloned);
                    }
                }
                for (int i2 = commonPkgState.mAssociations.size() - 1; i2 >= 0; i2--) {
                    AssociationState as = commonPkgState.mAssociations.valueAt(i2);
                    if (as.getProcess() == commonProc) {
                        as.setProcess(cloned);
                    }
                }
            } else {
                Slog.w(TAG, "Cloning proc state: no package state " + commonProc.getPackage() + "/" + pkgState.mUid + " for proc " + commonProc.getName());
            }
            str = processName;
            ps = new ProcessState(commonProc, pkgState.mPackageName, pkgState.mUid, pkgState.mVersionCode, processName, now);
        } else {
            ps = commonProc;
            str = processName;
        }
        pkgState.mProcesses.put(str, ps);
        return ps;
    }

    public ServiceState getServiceStateLocked(String packageName, int uid, long vers, String processName, String className) {
        PackageState as = getPackageStateLocked(packageName, uid, vers);
        ServiceState ss = as.mServices.get(className);
        if (ss != null) {
            return ss;
        }
        ServiceState ss2 = new ServiceState(this, packageName, className, processName, processName != null ? getProcessStateLocked(packageName, uid, vers, processName) : null);
        as.mServices.put(className, ss2);
        return ss2;
    }

    public AssociationState getAssociationStateLocked(String packageName, int uid, long vers, String processName, String className) {
        PackageState pkgs = getPackageStateLocked(packageName, uid, vers);
        AssociationState as = pkgs.mAssociations.get(className);
        if (as != null) {
            return as;
        }
        AssociationState as2 = new AssociationState(this, pkgs, className, processName, processName != null ? getProcessStateLocked(packageName, uid, vers, processName) : null);
        pkgs.mAssociations.put(className, as2);
        return as2;
    }

    public void updateTrackingAssociationsLocked(int curSeq, long now) {
        for (int i = this.mTrackingAssociations.size() - 1; i >= 0; i--) {
            AssociationState.SourceState act = this.mTrackingAssociations.get(i);
            if (act.mProcStateSeq != curSeq || act.mProcState >= 9) {
                act.stopActive(now);
                act.mInTrackingList = false;
                act.mProcState = -1;
                this.mTrackingAssociations.remove(i);
            } else {
                ProcessState proc = act.getAssociationState().getProcess();
                if (proc != null) {
                    int procState = proc.getCombinedState() % 14;
                    if (act.mProcState == procState) {
                        act.startActive(now);
                    } else {
                        act.stopActive(now);
                        if (act.mProcState < procState) {
                            long nowUptime = SystemClock.uptimeMillis();
                            if (this.mNextInverseProcStateWarningUptime > nowUptime) {
                                this.mSkippedInverseProcStateWarningCount++;
                            } else {
                                Slog.w(TAG, "Tracking association " + act + " whose proc state " + act.mProcState + " is better than process " + proc + " proc state " + procState + " (" + this.mSkippedInverseProcStateWarningCount + " skipped)");
                                this.mSkippedInverseProcStateWarningCount = 0;
                                this.mNextInverseProcStateWarningUptime = 10000 + nowUptime;
                            }
                        }
                    }
                } else {
                    Slog.wtf(TAG, "Tracking association without process: " + act + " in " + act.getAssociationState());
                }
            }
        }
    }

    public void dumpLocked(PrintWriter pw, String reqPackage, long now, boolean dumpSummary, boolean dumpDetails, boolean dumpAll, boolean activeOnly, int section) {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        boolean sepNeeded;
        boolean z;
        String str6;
        String str7;
        ProcessStats processStats;
        PrintWriter printWriter;
        SparseArray<ProcessState> uids;
        String str8;
        String str9;
        String str10;
        String procName;
        int iu;
        String str11;
        String str12;
        String str13;
        boolean printedHeader;
        boolean printedHeader2;
        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap;
        String str14;
        String str15;
        String str16;
        String str17;
        int uid;
        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap2;
        String pkgName;
        int iu2;
        int iv;
        String str18;
        boolean z2;
        String str19;
        String str20;
        boolean printedHeader3;
        boolean sepNeeded2;
        int NASCS;
        boolean printedHeader4;
        boolean sepNeeded3;
        int NSRVS;
        PackageState pkgState;
        String str21;
        String str22;
        String str23;
        String str24;
        PackageState pkgState2;
        int iasc;
        int NASCS2;
        String str25;
        String str26;
        String str27;
        int isvc;
        String str28;
        PackageState pkgState3;
        int NSRVS2;
        String str29;
        String str30;
        PackageState pkgState4;
        int NPROCS;
        PackageState pkgState5;
        String str31;
        String str32;
        String pkgName2;
        ArrayList<ProcessState> procs;
        boolean z3 = dumpAll;
        long totalTime = DumpUtils.dumpSingleTime(null, null, this.mMemFactorDurations, this.mMemFactor, this.mStartTime, now);
        boolean sepNeeded4 = false;
        if (this.mSysMemUsage.getKeyCount() > 0) {
            pw.println("System memory usage:");
            this.mSysMemUsage.dump(pw, "  ", ALL_SCREEN_ADJ, ALL_MEM_ADJ);
            sepNeeded4 = true;
        }
        boolean printedHeader5 = false;
        int i = section & 14;
        String str33 = "      (Not active: ";
        String str34 = "        ";
        String str35 = " entries)";
        String str36 = " / ";
        String str37 = "  * ";
        String str38 = ")";
        String str39 = SettingsStringUtil.DELIMITER;
        if (i != 0) {
            ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap3 = this.mPackages.getMap();
            sepNeeded = sepNeeded4;
            int ip = 0;
            while (true) {
                printedHeader2 = printedHeader5;
                if (ip >= pkgMap3.size()) {
                    break;
                }
                String pkgName3 = pkgMap3.keyAt(ip);
                SparseArray<LongSparseArray<PackageState>> uids2 = pkgMap3.valueAt(ip);
                int iu3 = 0;
                while (true) {
                    pkgMap = pkgMap3;
                    if (iu3 >= uids2.size()) {
                        break;
                    }
                    int uid2 = uids2.keyAt(iu3);
                    LongSparseArray<PackageState> vpkgs = uids2.valueAt(iu3);
                    int iv2 = 0;
                    while (iv2 < vpkgs.size()) {
                        long vers = vpkgs.keyAt(iv2);
                        PackageState pkgState6 = vpkgs.valueAt(iv2);
                        int NPROCS2 = pkgState6.mProcesses.size();
                        int NSRVS3 = pkgState6.mServices.size();
                        int NASCS3 = pkgState6.mAssociations.size();
                        boolean pkgMatch = reqPackage == null || reqPackage.equals(pkgName3);
                        boolean onlyAssociations = false;
                        if (!pkgMatch) {
                            boolean procMatch = false;
                            iu2 = iu3;
                            int iproc = 0;
                            while (true) {
                                if (iproc >= NPROCS2) {
                                    iv = iv2;
                                    break;
                                }
                                iv = iv2;
                                if (reqPackage.equals(pkgState6.mProcesses.valueAt(iproc).getName())) {
                                    procMatch = true;
                                    break;
                                } else {
                                    iproc++;
                                    iv2 = iv;
                                }
                            }
                            if (!procMatch) {
                                int iasc2 = 0;
                                while (true) {
                                    if (iasc2 >= NASCS3) {
                                        break;
                                    } else if (pkgState6.mAssociations.valueAt(iasc2).hasProcessOrPackage(reqPackage)) {
                                        onlyAssociations = true;
                                        break;
                                    } else {
                                        iasc2++;
                                    }
                                }
                                if (!onlyAssociations) {
                                    z2 = dumpAll;
                                    pkgName = pkgName3;
                                    uid = uid2;
                                    str19 = str39;
                                    str17 = str37;
                                    pkgMap2 = pkgMap;
                                    str20 = str38;
                                    str14 = str33;
                                    str15 = str34;
                                    str16 = str35;
                                    str18 = str36;
                                    str38 = str20;
                                    str39 = str19;
                                    z3 = z2;
                                    vpkgs = vpkgs;
                                    str36 = str18;
                                    ip = ip;
                                    pkgName3 = pkgName;
                                    pkgMap = pkgMap2;
                                    uid2 = uid;
                                    str37 = str17;
                                    str35 = str16;
                                    str34 = str15;
                                    str33 = str14;
                                    iv2 = iv + 1;
                                    iu3 = iu2;
                                }
                            }
                        } else {
                            iu2 = iu3;
                            iv = iv2;
                        }
                        if (NPROCS2 > 0 || NSRVS3 > 0 || NASCS3 > 0) {
                            if (!printedHeader2) {
                                if (sepNeeded) {
                                    pw.println();
                                }
                                pw.println("Per-Package Stats:");
                                printedHeader2 = true;
                                sepNeeded = true;
                            }
                            pw.print(str37);
                            pw.print(pkgName3);
                            pw.print(str36);
                            UserHandle.formatUid(pw, uid2);
                            pw.print(" / v");
                            pw.print(vers);
                            pw.println(str39);
                            sepNeeded2 = sepNeeded;
                            printedHeader3 = printedHeader2;
                        } else {
                            sepNeeded2 = sepNeeded;
                            printedHeader3 = printedHeader2;
                        }
                        if ((section & 2) == 0 || onlyAssociations) {
                            z2 = dumpAll;
                            NASCS = NASCS3;
                            sepNeeded3 = sepNeeded2;
                            printedHeader4 = printedHeader3;
                            pkgName = pkgName3;
                            uid = uid2;
                            str17 = str37;
                            NSRVS = NSRVS3;
                            pkgMap2 = pkgMap;
                            str20 = str38;
                            str22 = str33;
                            str23 = str35;
                            str19 = str39;
                            str18 = str36;
                            str21 = str34;
                            pkgState = pkgState6;
                        } else {
                            if (!dumpSummary) {
                                NASCS = NASCS3;
                                sepNeeded3 = sepNeeded2;
                                printedHeader4 = printedHeader3;
                                pkgName = pkgName3;
                                uid = uid2;
                                pkgState4 = pkgState6;
                                str17 = str37;
                                NSRVS = NSRVS3;
                                pkgMap2 = pkgMap;
                                str20 = str38;
                                str22 = str33;
                                str29 = str34;
                                str23 = str35;
                                str30 = str39;
                                str18 = str36;
                            } else if (dumpAll) {
                                NASCS = NASCS3;
                                sepNeeded3 = sepNeeded2;
                                printedHeader4 = printedHeader3;
                                pkgName = pkgName3;
                                uid = uid2;
                                pkgState4 = pkgState6;
                                str17 = str37;
                                NSRVS = NSRVS3;
                                pkgMap2 = pkgMap;
                                str20 = str38;
                                str22 = str33;
                                str29 = str34;
                                str23 = str35;
                                str30 = str39;
                                str18 = str36;
                            } else {
                                ArrayList<ProcessState> procs2 = new ArrayList<>();
                                sepNeeded3 = sepNeeded2;
                                int iproc2 = 0;
                                while (iproc2 < NPROCS2) {
                                    ProcessState proc = pkgState6.mProcesses.valueAt(iproc2);
                                    if (!pkgMatch) {
                                        pkgName2 = pkgName3;
                                        if (!reqPackage.equals(proc.getName())) {
                                            procs = procs2;
                                            iproc2++;
                                            procs2 = procs;
                                            printedHeader3 = printedHeader3;
                                            pkgName3 = pkgName2;
                                        }
                                    } else {
                                        pkgName2 = pkgName3;
                                    }
                                    if (!activeOnly || proc.isInUse()) {
                                        procs = procs2;
                                        procs.add(proc);
                                        iproc2++;
                                        procs2 = procs;
                                        printedHeader3 = printedHeader3;
                                        pkgName3 = pkgName2;
                                    } else {
                                        procs = procs2;
                                        iproc2++;
                                        procs2 = procs;
                                        printedHeader3 = printedHeader3;
                                        pkgName3 = pkgName2;
                                    }
                                }
                                printedHeader4 = printedHeader3;
                                pkgName = pkgName3;
                                uid = uid2;
                                pkgMap2 = pkgMap;
                                NASCS = NASCS3;
                                str20 = str38;
                                str17 = str37;
                                str18 = str36;
                                str23 = str35;
                                NSRVS = NSRVS3;
                                str22 = str33;
                                DumpUtils.dumpProcessSummaryLocked(pw, "      ", "Prc ", procs2, ALL_SCREEN_ADJ, ALL_MEM_ADJ, NON_CACHED_PROC_STATES, now, totalTime);
                                pkgState = pkgState6;
                                str21 = str34;
                                z2 = dumpAll;
                                str19 = str39;
                            }
                            int iproc3 = 0;
                            while (iproc3 < NPROCS2) {
                                ProcessState proc2 = pkgState4.mProcesses.valueAt(iproc3);
                                if (!pkgMatch && !reqPackage.equals(proc2.getName())) {
                                    pkgState5 = pkgState4;
                                    str31 = str29;
                                    NPROCS = NPROCS2;
                                    str32 = str30;
                                } else if (!activeOnly || proc2.isInUse()) {
                                    pw.print("      Process ");
                                    pw.print(pkgState4.mProcesses.keyAt(iproc3));
                                    if (proc2.getCommonProcess().isMultiPackage()) {
                                        pw.print(" (multi, ");
                                    } else {
                                        pw.print(" (unique, ");
                                    }
                                    pw.print(proc2.getDurationsBucketCount());
                                    pw.print(str23);
                                    pw.println(str30);
                                    NPROCS = NPROCS2;
                                    str32 = str30;
                                    proc2.dumpProcessState(pw, "        ", ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES, now);
                                    proc2.dumpPss(pw, "        ", ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES, now);
                                    pkgState5 = pkgState4;
                                    str31 = str29;
                                    proc2.dumpInternalLocked(pw, str31, dumpAll);
                                } else {
                                    pw.print(str22);
                                    pw.print(pkgState4.mProcesses.keyAt(iproc3));
                                    pw.println(str20);
                                    pkgState5 = pkgState4;
                                    str31 = str29;
                                    NPROCS = NPROCS2;
                                    str32 = str30;
                                }
                                iproc3++;
                                str29 = str31;
                                pkgState4 = pkgState5;
                                str30 = str32;
                                NPROCS2 = NPROCS;
                            }
                            pkgState = pkgState4;
                            str21 = str29;
                            z2 = dumpAll;
                            str19 = str30;
                        }
                        String str40 = "        Process: ";
                        if ((section & 4) == 0 || onlyAssociations) {
                            str16 = str23;
                            str14 = str22;
                            str15 = str21;
                            pkgState2 = pkgState;
                            str24 = str40;
                        } else {
                            int isvc2 = 0;
                            while (isvc2 < NSRVS) {
                                ServiceState svc = pkgState.mServices.valueAt(isvc2);
                                if (!pkgMatch && !reqPackage.equals(svc.getProcessName())) {
                                    str27 = str23;
                                    str25 = str22;
                                    NSRVS2 = NSRVS;
                                    str26 = str21;
                                    isvc = isvc2;
                                    pkgState3 = pkgState;
                                    str28 = str40;
                                } else if (!activeOnly || svc.isInUse()) {
                                    if (z2) {
                                        pw.print("      Service ");
                                    } else {
                                        pw.print("      * Svc ");
                                    }
                                    pw.print(pkgState.mServices.keyAt(isvc2));
                                    pw.println(str19);
                                    pw.print(str40);
                                    pw.println(svc.getProcessName());
                                    NSRVS2 = NSRVS;
                                    str26 = str21;
                                    isvc = isvc2;
                                    str27 = str23;
                                    str25 = str22;
                                    pkgState3 = pkgState;
                                    str28 = str40;
                                    svc.dumpStats(pw, "        ", "          ", "    ", now, totalTime, dumpSummary, dumpAll);
                                } else {
                                    pw.print("      (Not active service: ");
                                    pw.print(pkgState.mServices.keyAt(isvc2));
                                    pw.println(str20);
                                    str27 = str23;
                                    str25 = str22;
                                    NSRVS2 = NSRVS;
                                    str26 = str21;
                                    isvc = isvc2;
                                    pkgState3 = pkgState;
                                    str28 = str40;
                                }
                                isvc2 = isvc + 1;
                                pkgState = pkgState3;
                                str40 = str28;
                                str23 = str27;
                                str21 = str26;
                                str22 = str25;
                            }
                            str16 = str23;
                            str14 = str22;
                            str15 = str21;
                            pkgState2 = pkgState;
                            str24 = str40;
                        }
                        if ((section & 8) != 0) {
                            int iasc3 = 0;
                            while (iasc3 < NASCS) {
                                AssociationState asc = pkgState2.mAssociations.valueAt(iasc3);
                                if (!pkgMatch && !reqPackage.equals(asc.getProcessName())) {
                                    if (!onlyAssociations) {
                                        iasc = iasc3;
                                        NASCS2 = NASCS;
                                    } else if (!asc.hasProcessOrPackage(reqPackage)) {
                                        iasc = iasc3;
                                        NASCS2 = NASCS;
                                    }
                                    iasc3 = iasc + 1;
                                }
                                if (!activeOnly || asc.isInUse()) {
                                    if (z2) {
                                        pw.print("      Association ");
                                    } else {
                                        pw.print("      * Asc ");
                                    }
                                    pw.print(pkgState2.mAssociations.keyAt(iasc3));
                                    pw.println(str19);
                                    pw.print(str24);
                                    pw.println(asc.getProcessName());
                                    iasc = iasc3;
                                    NASCS2 = NASCS;
                                    asc.dumpStats(pw, "        ", "          ", "    ", now, totalTime, onlyAssociations ? reqPackage : null, dumpDetails, dumpAll);
                                    iasc3 = iasc + 1;
                                } else {
                                    pw.print("      (Not active association: ");
                                    pw.print(pkgState2.mAssociations.keyAt(iasc3));
                                    pw.println(str20);
                                    iasc = iasc3;
                                    NASCS2 = NASCS;
                                    iasc3 = iasc + 1;
                                }
                            }
                        }
                        sepNeeded = sepNeeded3;
                        printedHeader2 = printedHeader4;
                        str38 = str20;
                        str39 = str19;
                        z3 = z2;
                        vpkgs = vpkgs;
                        str36 = str18;
                        ip = ip;
                        pkgName3 = pkgName;
                        pkgMap = pkgMap2;
                        uid2 = uid;
                        str37 = str17;
                        str35 = str16;
                        str34 = str15;
                        str33 = str14;
                        iv2 = iv + 1;
                        iu3 = iu2;
                    }
                    iu3++;
                    str38 = str38;
                    uids2 = uids2;
                    ip = ip;
                    pkgMap3 = pkgMap;
                }
                ip++;
                printedHeader5 = printedHeader2;
                pkgMap3 = pkgMap;
            }
            z = z3;
            str6 = str39;
            str7 = str38;
            str4 = str37;
            str5 = str36;
            str3 = str35;
            str = str33;
            str2 = str34;
            printedHeader5 = printedHeader2;
        } else {
            z = z3;
            str6 = str39;
            str7 = str38;
            str4 = str37;
            str5 = str36;
            str3 = str35;
            str = str33;
            str2 = str34;
            sepNeeded = sepNeeded4;
        }
        if ((section & 1) != 0) {
            processStats = this;
            ArrayMap<String, SparseArray<ProcessState>> procMap = processStats.mProcesses.getMap();
            int numShownProcs = 0;
            int numTotalProcs = 0;
            boolean printedHeader6 = false;
            int ip2 = 0;
            while (ip2 < procMap.size()) {
                String procName2 = procMap.keyAt(ip2);
                SparseArray<ProcessState> uids3 = procMap.valueAt(ip2);
                int iu4 = 0;
                while (iu4 < uids3.size()) {
                    int uid3 = uids3.keyAt(iu4);
                    int numTotalProcs2 = numTotalProcs + 1;
                    ProcessState proc3 = uids3.valueAt(iu4);
                    if (proc3.hasAnyData() && proc3.isMultiPackage() && (reqPackage == null || reqPackage.equals(procName2) || reqPackage.equals(proc3.getPackage()))) {
                        int numShownProcs2 = numShownProcs + 1;
                        if (sepNeeded) {
                            pw.println();
                        }
                        sepNeeded = true;
                        if (!printedHeader6) {
                            pw.println("Multi-Package Common Processes:");
                            printedHeader = true;
                        } else {
                            printedHeader = printedHeader6;
                        }
                        if (!activeOnly || proc3.isInUse()) {
                            pw.print(str4);
                            pw.print(procName2);
                            pw.print(str5);
                            UserHandle.formatUid(pw, uid3);
                            str11 = str7;
                            pw.print(" (");
                            pw.print(proc3.getDurationsBucketCount());
                            str13 = str3;
                            pw.print(str13);
                            pw.println(str6);
                            str10 = str5;
                            str9 = str4;
                            str8 = str;
                            iu = iu4;
                            procName = procName2;
                            uids = uids3;
                            proc3.dumpProcessState(pw, "        ", ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES, now);
                            proc3.dumpPss(pw, "        ", ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES, now);
                            str12 = str2;
                            proc3.dumpInternalLocked(pw, str12, z);
                        } else {
                            pw.print(str);
                            pw.print(procName2);
                            pw.println(str7);
                            str11 = str7;
                            str8 = str;
                            iu = iu4;
                            procName = procName2;
                            uids = uids3;
                            str10 = str5;
                            str9 = str4;
                            str13 = str3;
                            str12 = str2;
                        }
                        numShownProcs = numShownProcs2;
                        printedHeader6 = printedHeader;
                    } else {
                        str11 = str7;
                        iu = iu4;
                        procName = procName2;
                        uids = uids3;
                        str10 = str5;
                        str9 = str4;
                        str13 = str3;
                        str12 = str2;
                        str8 = str;
                    }
                    iu4 = iu + 1;
                    str3 = str13;
                    str2 = str12;
                    numTotalProcs = numTotalProcs2;
                    str7 = str11;
                    procName2 = procName;
                    str5 = str10;
                    str4 = str9;
                    str = str8;
                    uids3 = uids;
                }
                ip2++;
                str7 = str7;
            }
            pw.print("  Total procs: ");
            pw.print(numShownProcs);
            pw.print(" shown of ");
            pw.print(numTotalProcs);
            pw.println(" total");
        } else {
            processStats = this;
        }
        if (z) {
            if (sepNeeded) {
                pw.println();
            }
            sepNeeded = true;
            if (processStats.mTrackingAssociations.size() > 0) {
                pw.println();
                pw.println("Tracking associations:");
                for (int i2 = 0; i2 < processStats.mTrackingAssociations.size(); i2++) {
                    AssociationState.SourceState src = processStats.mTrackingAssociations.get(i2);
                    AssociationState asc2 = src.getAssociationState();
                    pw.print("  #");
                    pw.print(i2);
                    pw.print(": ");
                    pw.print(asc2.getProcessName());
                    pw.print("/");
                    UserHandle.formatUid(pw, asc2.getUid());
                    pw.print(" <- ");
                    pw.print(src.getProcessName());
                    pw.print("/");
                    UserHandle.formatUid(pw, src.getUid());
                    pw.println(str6);
                    pw.print("    Tracking for: ");
                    TimeUtils.formatDuration(now - src.mTrackingUptime, pw);
                    pw.println();
                    pw.print("    Component: ");
                    pw.print(new ComponentName(asc2.getPackage(), asc2.getName()).flattenToShortString());
                    pw.println();
                    pw.print("    Proc state: ");
                    if (src.mProcState != -1) {
                        pw.print(DumpUtils.STATE_NAMES[src.mProcState]);
                    } else {
                        pw.print("--");
                    }
                    pw.print(" #");
                    pw.println(src.mProcStateSeq);
                    pw.print("    Process: ");
                    pw.println(asc2.getProcess());
                    if (src.mActiveCount > 0) {
                        pw.print("    Active count ");
                        pw.print(src.mActiveCount);
                        pw.print(": ");
                        asc2.dumpActiveDurationSummary(pw, src, totalTime, now, dumpAll);
                        pw.println();
                    }
                }
            }
        }
        if (sepNeeded) {
            pw.println();
        }
        if (dumpSummary) {
            pw.println("Process summary:");
            printWriter = pw;
            dumpSummaryLocked(pw, reqPackage, now, activeOnly);
        } else {
            printWriter = pw;
            processStats.dumpTotalsLocked(printWriter, now);
        }
        if (dumpAll) {
            pw.println();
            printWriter.println("Internal state:");
            printWriter.print("  mRunning=");
            printWriter.println(processStats.mRunning);
        }
        if (reqPackage == null) {
            dumpFragmentationLocked(pw);
        }
    }

    public void dumpSummaryLocked(PrintWriter pw, String reqPackage, long now, boolean activeOnly) {
        dumpFilteredSummaryLocked(pw, null, "  ", null, ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES, NON_CACHED_PROC_STATES, now, DumpUtils.dumpSingleTime(null, null, this.mMemFactorDurations, this.mMemFactor, this.mStartTime, now), reqPackage, activeOnly);
        pw.println();
        dumpTotalsLocked(pw, now);
    }

    private void dumpFragmentationLocked(PrintWriter pw) {
        pw.println();
        pw.println("Available pages by page size:");
        int NPAGETYPES = this.mPageTypeLabels.size();
        for (int i = 0; i < NPAGETYPES; i++) {
            pw.format("Node %3d Zone %7s  %14s ", this.mPageTypeNodes.get(i), this.mPageTypeZones.get(i), this.mPageTypeLabels.get(i));
            int[] sizes = this.mPageTypeSizes.get(i);
            int N = sizes == null ? 0 : sizes.length;
            for (int j = 0; j < N; j++) {
                pw.format("%6d", Integer.valueOf(sizes[j]));
            }
            pw.println();
        }
    }

    /* access modifiers changed from: package-private */
    public long printMemoryCategory(PrintWriter pw, String prefix, String label, double memWeight, long totalTime, long curTotalMem, int samples) {
        if (memWeight == 0.0d) {
            return curTotalMem;
        }
        long mem = (long) ((1024.0d * memWeight) / ((double) totalTime));
        pw.print(prefix);
        pw.print(label);
        pw.print(": ");
        DebugUtils.printSizeValue(pw, mem);
        pw.print(" (");
        pw.print(samples);
        pw.print(" samples)");
        pw.println();
        return curTotalMem + mem;
    }

    /* access modifiers changed from: package-private */
    public void dumpTotalsLocked(PrintWriter pw, long now) {
        int i;
        pw.println("Run time Stats:");
        DumpUtils.dumpSingleTime(pw, "  ", this.mMemFactorDurations, this.mMemFactor, this.mStartTime, now);
        pw.println();
        pw.println("Memory usage:");
        TotalMemoryUseCollection totalMem = new TotalMemoryUseCollection(ALL_SCREEN_ADJ, ALL_MEM_ADJ);
        computeTotalMemoryUse(totalMem, now);
        long totalPss = printMemoryCategory(pw, "  ", "Native ", totalMem.sysMemNativeWeight, totalMem.totalTime, printMemoryCategory(pw, "  ", "Kernel ", totalMem.sysMemKernelWeight, totalMem.totalTime, 0, totalMem.sysMemSamples), totalMem.sysMemSamples);
        int i2 = 0;
        while (i2 < 14) {
            if (i2 != 6) {
                i = i2;
                totalPss = printMemoryCategory(pw, "  ", DumpUtils.STATE_NAMES[i2], totalMem.processStateWeight[i2], totalMem.totalTime, totalPss, totalMem.processStateSamples[i2]);
            } else {
                i = i2;
            }
            i2 = i + 1;
        }
        long totalPss2 = printMemoryCategory(pw, "  ", "Z-Ram  ", totalMem.sysMemZRamWeight, totalMem.totalTime, printMemoryCategory(pw, "  ", "Free   ", totalMem.sysMemFreeWeight, totalMem.totalTime, printMemoryCategory(pw, "  ", "Cached ", totalMem.sysMemCachedWeight, totalMem.totalTime, totalPss, totalMem.sysMemSamples), totalMem.sysMemSamples), totalMem.sysMemSamples);
        pw.print("  TOTAL  : ");
        DebugUtils.printSizeValue(pw, totalPss2);
        pw.println();
        printMemoryCategory(pw, "  ", DumpUtils.STATE_NAMES[6], totalMem.processStateWeight[6], totalMem.totalTime, totalPss2, totalMem.processStateSamples[6]);
        pw.println();
        pw.println("PSS collection stats:");
        pw.print("  Internal Single: ");
        pw.print(this.mInternalSinglePssCount);
        pw.print("x over ");
        TimeUtils.formatDuration(this.mInternalSinglePssTime, pw);
        pw.println();
        pw.print("  Internal All Procs (Memory Change): ");
        pw.print(this.mInternalAllMemPssCount);
        pw.print("x over ");
        TimeUtils.formatDuration(this.mInternalAllMemPssTime, pw);
        pw.println();
        pw.print("  Internal All Procs (Polling): ");
        pw.print(this.mInternalAllPollPssCount);
        pw.print("x over ");
        TimeUtils.formatDuration(this.mInternalAllPollPssTime, pw);
        pw.println();
        pw.print("  External: ");
        pw.print(this.mExternalPssCount);
        pw.print("x over ");
        TimeUtils.formatDuration(this.mExternalPssTime, pw);
        pw.println();
        pw.print("  External Slow: ");
        pw.print(this.mExternalSlowPssCount);
        pw.print("x over ");
        TimeUtils.formatDuration(this.mExternalSlowPssTime, pw);
        pw.println();
        pw.println();
        pw.print("          Start time: ");
        pw.print(DateFormat.format("yyyy-MM-dd HH:mm:ss", this.mTimePeriodStartClock));
        pw.println();
        pw.print("        Total uptime: ");
        TimeUtils.formatDuration((this.mRunning ? SystemClock.uptimeMillis() : this.mTimePeriodEndUptime) - this.mTimePeriodStartUptime, pw);
        pw.println();
        pw.print("  Total elapsed time: ");
        TimeUtils.formatDuration((this.mRunning ? SystemClock.elapsedRealtime() : this.mTimePeriodEndRealtime) - this.mTimePeriodStartRealtime, pw);
        boolean partial = true;
        if ((this.mFlags & 2) != 0) {
            pw.print(" (shutdown)");
            partial = false;
        }
        if ((this.mFlags & 4) != 0) {
            pw.print(" (sysprops)");
            partial = false;
        }
        if ((this.mFlags & 1) != 0) {
            pw.print(" (complete)");
            partial = false;
        }
        if (partial) {
            pw.print(" (partial)");
        }
        if (this.mHasSwappedOutPss) {
            pw.print(" (swapped-out-pss)");
        }
        pw.print(' ');
        pw.print(this.mRuntime);
        pw.println();
    }

    /* access modifiers changed from: package-private */
    public void dumpFilteredSummaryLocked(PrintWriter pw, String header, String prefix, String prcLabel, int[] screenStates, int[] memStates, int[] procStates, int[] sortProcStates, long now, long totalTime, String reqPackage, boolean activeOnly) {
        ArrayList<ProcessState> procs = collectProcessesLocked(screenStates, memStates, procStates, sortProcStates, now, reqPackage, activeOnly);
        if (procs.size() > 0) {
            if (header != null) {
                pw.println();
                pw.println(header);
            }
            DumpUtils.dumpProcessSummaryLocked(pw, prefix, prcLabel, procs, screenStates, memStates, sortProcStates, now, totalTime);
        }
    }

    public ArrayList<ProcessState> collectProcessesLocked(int[] screenStates, int[] memStates, int[] procStates, int[] sortProcStates, long now, String reqPackage, boolean activeOnly) {
        ArraySet<ProcessState> foundProcs = new ArraySet<>();
        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        for (int ip = 0; ip < pkgMap.size(); ip++) {
            String pkgName = pkgMap.keyAt(ip);
            SparseArray<LongSparseArray<PackageState>> procs = pkgMap.valueAt(ip);
            for (int iu = 0; iu < procs.size(); iu++) {
                LongSparseArray<PackageState> vpkgs = procs.valueAt(iu);
                int NVERS = vpkgs.size();
                for (int iv = 0; iv < NVERS; iv++) {
                    PackageState state = vpkgs.valueAt(iv);
                    int NPROCS = state.mProcesses.size();
                    boolean pkgMatch = reqPackage == null || reqPackage.equals(pkgName);
                    for (int iproc = 0; iproc < NPROCS; iproc++) {
                        ProcessState proc = state.mProcesses.valueAt(iproc);
                        if ((pkgMatch || reqPackage.equals(proc.getName())) && (!activeOnly || proc.isInUse())) {
                            foundProcs.add(proc.getCommonProcess());
                        }
                    }
                }
            }
        }
        ArrayList<ProcessState> outProcs = new ArrayList<>(foundProcs.size());
        for (int i = 0; i < foundProcs.size(); i++) {
            ProcessState proc2 = foundProcs.valueAt(i);
            if (proc2.computeProcessTimeLocked(screenStates, memStates, procStates, now) > 0) {
                outProcs.add(proc2);
                if (procStates != sortProcStates) {
                    proc2.computeProcessTimeLocked(screenStates, memStates, sortProcStates, now);
                }
            }
        }
        Collections.sort(outProcs, ProcessState.COMPARATOR);
        return outProcs;
    }

    public void dumpCheckinLocked(PrintWriter pw, String reqPackage, int section) {
        boolean partial;
        String str;
        ProcessStats processStats;
        String str2;
        int iu;
        SparseArray<LongSparseArray<PackageState>> uids;
        LongSparseArray<PackageState> vpkgs;
        String str3;
        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap;
        int ip;
        int NASCS;
        PackageState pkgState;
        String pkgName;
        int NSRVS;
        String str4 = reqPackage;
        long now = SystemClock.uptimeMillis();
        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap2 = this.mPackages.getMap();
        pw.println("vers,5");
        pw.print("period,");
        pw.print(this.mTimePeriodStartClockStr);
        String str5 = SmsManager.REGEX_PREFIX_DELIMITER;
        pw.print(str5);
        pw.print(this.mTimePeriodStartRealtime);
        pw.print(str5);
        pw.print(this.mRunning ? SystemClock.elapsedRealtime() : this.mTimePeriodEndRealtime);
        boolean partial2 = true;
        if ((this.mFlags & 2) != 0) {
            pw.print(",shutdown");
            partial2 = false;
        }
        if ((this.mFlags & 4) != 0) {
            pw.print(",sysprops");
            partial2 = false;
        }
        if ((this.mFlags & 1) != 0) {
            pw.print(",complete");
            partial = false;
        } else {
            partial = partial2;
        }
        if (partial) {
            pw.print(",partial");
        }
        if (this.mHasSwappedOutPss) {
            pw.print(",swapped-out-pss");
        }
        pw.println();
        pw.print("config,");
        pw.println(this.mRuntime);
        if ((section & 14) != 0) {
            int ip2 = 0;
            while (ip2 < pkgMap2.size()) {
                String pkgName2 = pkgMap2.keyAt(ip2);
                if (str4 == null || str4.equals(pkgName2)) {
                    SparseArray<LongSparseArray<PackageState>> uids2 = pkgMap2.valueAt(ip2);
                    int iu2 = 0;
                    while (iu2 < uids2.size()) {
                        int uid = uids2.keyAt(iu2);
                        LongSparseArray<PackageState> vpkgs2 = uids2.valueAt(iu2);
                        int iv = 0;
                        while (iv < vpkgs2.size()) {
                            long vers = vpkgs2.keyAt(iv);
                            PackageState pkgState2 = vpkgs2.valueAt(iv);
                            int NPROCS = pkgState2.mProcesses.size();
                            int NSRVS2 = pkgState2.mServices.size();
                            int NASCS2 = pkgState2.mAssociations.size();
                            if ((section & 2) != 0) {
                                int iproc = 0;
                                while (iproc < NPROCS) {
                                    pkgState2.mProcesses.valueAt(iproc).dumpPackageProcCheckin(pw, pkgName2, uid, vers, pkgState2.mProcesses.keyAt(iproc), now);
                                    iproc++;
                                    NSRVS2 = NSRVS2;
                                    pkgName2 = pkgName2;
                                    pkgState2 = pkgState2;
                                    NASCS2 = NASCS2;
                                    ip2 = ip2;
                                    NPROCS = NPROCS;
                                    pkgMap2 = pkgMap2;
                                    str5 = str5;
                                    vpkgs2 = vpkgs2;
                                    uids2 = uids2;
                                    iu2 = iu2;
                                }
                                vpkgs = vpkgs2;
                                uids = uids2;
                                iu = iu2;
                                pkgName = pkgName2;
                                NSRVS = NSRVS2;
                                ip = ip2;
                                pkgMap = pkgMap2;
                                str3 = str5;
                                pkgState = pkgState2;
                                NASCS = NASCS2;
                            } else {
                                vpkgs = vpkgs2;
                                uids = uids2;
                                iu = iu2;
                                pkgName = pkgName2;
                                NSRVS = NSRVS2;
                                ip = ip2;
                                pkgMap = pkgMap2;
                                str3 = str5;
                                pkgState = pkgState2;
                                NASCS = NASCS2;
                            }
                            if ((section & 4) != 0) {
                                for (int isvc = 0; isvc < NSRVS; isvc++) {
                                    pkgState.mServices.valueAt(isvc).dumpTimesCheckin(pw, pkgName, uid, vers, DumpUtils.collapseString(pkgName, pkgState.mServices.keyAt(isvc)), now);
                                }
                            }
                            if ((section & 8) != 0) {
                                for (int iasc = 0; iasc < NASCS; iasc++) {
                                    pkgState.mAssociations.valueAt(iasc).dumpTimesCheckin(pw, pkgName, uid, vers, DumpUtils.collapseString(pkgName, pkgState.mAssociations.keyAt(iasc)), now);
                                }
                            }
                            iv++;
                            pkgName2 = pkgName;
                            ip2 = ip;
                            pkgMap2 = pkgMap;
                            str5 = str3;
                            vpkgs2 = vpkgs;
                            uids2 = uids;
                            iu2 = iu;
                        }
                        iu2++;
                    }
                }
                ip2++;
                str4 = reqPackage;
                pkgMap2 = pkgMap2;
                str5 = str5;
            }
            str = str5;
        } else {
            str = str5;
        }
        if ((section & 1) != 0) {
            processStats = this;
            ArrayMap<String, SparseArray<ProcessState>> procMap = processStats.mProcesses.getMap();
            for (int ip3 = 0; ip3 < procMap.size(); ip3++) {
                String procName = procMap.keyAt(ip3);
                SparseArray<ProcessState> uids3 = procMap.valueAt(ip3);
                for (int iu3 = 0; iu3 < uids3.size(); iu3++) {
                    uids3.valueAt(iu3).dumpProcCheckin(pw, procName, uids3.keyAt(iu3), now);
                }
            }
        } else {
            processStats = this;
        }
        pw.print("total");
        DumpUtils.dumpAdjTimesCheckin(pw, SmsManager.REGEX_PREFIX_DELIMITER, processStats.mMemFactorDurations, processStats.mMemFactor, processStats.mStartTime, now);
        pw.println();
        int sysMemUsageCount = processStats.mSysMemUsage.getKeyCount();
        if (sysMemUsageCount > 0) {
            pw.print("sysmemusage");
            int i = 0;
            while (i < sysMemUsageCount) {
                int key = processStats.mSysMemUsage.getKeyAt(i);
                int type = SparseMappingTable.getIdFromKey(key);
                pw.print(str);
                DumpUtils.printProcStateTag(pw, type);
                for (int j = 0; j < 16; j++) {
                    if (j > 1) {
                        pw.print(SettingsStringUtil.DELIMITER);
                    }
                    pw.print(processStats.mSysMemUsage.getValue(key, j));
                }
                i++;
                str = str;
            }
            str2 = str;
        } else {
            str2 = str;
        }
        pw.println();
        TotalMemoryUseCollection totalMem = new TotalMemoryUseCollection(ALL_SCREEN_ADJ, ALL_MEM_ADJ);
        processStats.computeTotalMemoryUse(totalMem, now);
        pw.print("weights,");
        pw.print(totalMem.totalTime);
        pw.print(str2);
        pw.print(totalMem.sysMemCachedWeight);
        pw.print(SettingsStringUtil.DELIMITER);
        pw.print(totalMem.sysMemSamples);
        pw.print(str2);
        pw.print(totalMem.sysMemFreeWeight);
        pw.print(SettingsStringUtil.DELIMITER);
        pw.print(totalMem.sysMemSamples);
        pw.print(str2);
        pw.print(totalMem.sysMemZRamWeight);
        pw.print(SettingsStringUtil.DELIMITER);
        pw.print(totalMem.sysMemSamples);
        pw.print(str2);
        pw.print(totalMem.sysMemKernelWeight);
        pw.print(SettingsStringUtil.DELIMITER);
        pw.print(totalMem.sysMemSamples);
        pw.print(str2);
        pw.print(totalMem.sysMemNativeWeight);
        pw.print(SettingsStringUtil.DELIMITER);
        pw.print(totalMem.sysMemSamples);
        for (int i2 = 0; i2 < 14; i2++) {
            pw.print(str2);
            pw.print(totalMem.processStateWeight[i2]);
            pw.print(SettingsStringUtil.DELIMITER);
            pw.print(totalMem.processStateSamples[i2]);
        }
        pw.println();
        int NPAGETYPES = processStats.mPageTypeLabels.size();
        for (int i3 = 0; i3 < NPAGETYPES; i3++) {
            pw.print("availablepages,");
            pw.print(processStats.mPageTypeLabels.get(i3));
            pw.print(str2);
            pw.print(processStats.mPageTypeZones.get(i3));
            pw.print(str2);
            int[] sizes = processStats.mPageTypeSizes.get(i3);
            int N = sizes == null ? 0 : sizes.length;
            for (int j2 = 0; j2 < N; j2++) {
                if (j2 != 0) {
                    pw.print(str2);
                }
                pw.print(sizes[j2]);
            }
            pw.println();
        }
    }

    public void writeToProto(ProtoOutputStream proto, long now, int section) {
        boolean partial;
        proto.write(1112396529665L, this.mTimePeriodStartRealtime);
        proto.write(1112396529666L, this.mRunning ? SystemClock.elapsedRealtime() : this.mTimePeriodEndRealtime);
        proto.write(1112396529667L, this.mTimePeriodStartUptime);
        proto.write(1112396529668L, this.mTimePeriodEndUptime);
        proto.write(1138166333445L, this.mRuntime);
        proto.write(1133871366150L, this.mHasSwappedOutPss);
        boolean partial2 = true;
        if ((this.mFlags & 2) != 0) {
            proto.write(2259152797703L, 3);
            partial2 = false;
        }
        if ((this.mFlags & 4) != 0) {
            proto.write(2259152797703L, 4);
            partial2 = false;
        }
        if ((this.mFlags & 1) != 0) {
            proto.write(2259152797703L, 1);
            partial = false;
        } else {
            partial = partial2;
        }
        if (partial) {
            proto.write(2259152797703L, 2);
        }
        int NPAGETYPES = this.mPageTypeLabels.size();
        for (int i = 0; i < NPAGETYPES; i++) {
            long token = proto.start(2246267895818L);
            proto.write(1120986464257L, this.mPageTypeNodes.get(i).intValue());
            proto.write(1138166333442L, this.mPageTypeZones.get(i));
            proto.write(1138166333443L, this.mPageTypeLabels.get(i));
            int[] sizes = this.mPageTypeSizes.get(i);
            int N = sizes == null ? 0 : sizes.length;
            for (int j = 0; j < N; j++) {
                proto.write(2220498092036L, sizes[j]);
            }
            proto.end(token);
        }
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        if ((section & 1) != 0) {
            for (int ip = 0; ip < procMap.size(); ip++) {
                String procName = procMap.keyAt(ip);
                SparseArray<ProcessState> uids = procMap.valueAt(ip);
                for (int iu = 0; iu < uids.size(); iu++) {
                    uids.valueAt(iu).writeToProto(proto, 2246267895816L, procName, uids.keyAt(iu), now);
                }
            }
        }
        if ((section & 14) != 0) {
            ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
            for (int ip2 = 0; ip2 < pkgMap.size(); ip2++) {
                SparseArray<LongSparseArray<PackageState>> uids2 = pkgMap.valueAt(ip2);
                for (int iu2 = 0; iu2 < uids2.size(); iu2++) {
                    int iv = 0;
                    for (LongSparseArray<PackageState> vers = uids2.valueAt(iu2); iv < vers.size(); vers = vers) {
                        vers.valueAt(iv).writeToProto(proto, 2246267895817L, now, section);
                        iv++;
                    }
                }
            }
        }
    }

    public static final class ProcessStateHolder {
        public final long appVersion;
        public PackageState pkg;
        public ProcessState state;

        public ProcessStateHolder(long _appVersion) {
            this.appVersion = _appVersion;
        }
    }

    public static final class PackageState {
        public final ArrayMap<String, AssociationState> mAssociations = new ArrayMap<>();
        public final String mPackageName;
        public final ProcessStats mProcessStats;
        public final ArrayMap<String, ProcessState> mProcesses = new ArrayMap<>();
        public final ArrayMap<String, ServiceState> mServices = new ArrayMap<>();
        public final int mUid;
        public final long mVersionCode;

        public PackageState(ProcessStats procStats, String packageName, int uid, long versionCode) {
            this.mProcessStats = procStats;
            this.mUid = uid;
            this.mPackageName = packageName;
            this.mVersionCode = versionCode;
        }

        public AssociationState getAssociationStateLocked(ProcessState proc, String className) {
            AssociationState as = this.mAssociations.get(className);
            if (as != null) {
                if (proc != null) {
                    as.setProcess(proc);
                }
                return as;
            }
            AssociationState as2 = new AssociationState(this.mProcessStats, this, className, proc.getName(), proc);
            this.mAssociations.put(className, as2);
            return as2;
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId, long now, int section) {
            long token = proto.start(fieldId);
            proto.write(1138166333441L, this.mPackageName);
            proto.write(1120986464258L, this.mUid);
            proto.write(1112396529667L, this.mVersionCode);
            if ((section & 2) != 0) {
                for (int ip = 0; ip < this.mProcesses.size(); ip++) {
                    this.mProcesses.valueAt(ip).writeToProto(proto, 2246267895812L, this.mProcesses.keyAt(ip), this.mUid, now);
                }
            }
            if ((section & 4) != 0) {
                for (int is = 0; is < this.mServices.size(); is++) {
                    this.mServices.valueAt(is).writeToProto(proto, 2246267895813L, now);
                }
            }
            if ((section & 8) != 0) {
                for (int ia = 0; ia < this.mAssociations.size(); ia++) {
                    this.mAssociations.valueAt(ia).writeToProto(proto, 2246267895814L, now);
                }
            }
            proto.end(token);
        }
    }

    public static final class ProcessDataCollection {
        public long avgPss;
        public long avgRss;
        public long avgUss;
        public long maxPss;
        public long maxRss;
        public long maxUss;
        final int[] memStates;
        public long minPss;
        public long minRss;
        public long minUss;
        public long numPss;
        final int[] procStates;
        final int[] screenStates;
        public long totalTime;

        public ProcessDataCollection(int[] _screenStates, int[] _memStates, int[] _procStates) {
            this.screenStates = _screenStates;
            this.memStates = _memStates;
            this.procStates = _procStates;
        }

        /* access modifiers changed from: package-private */
        public void print(PrintWriter pw, long overallTime, boolean full) {
            if (this.totalTime > overallTime) {
                pw.print("*");
            }
            DumpUtils.printPercent(pw, ((double) this.totalTime) / ((double) overallTime));
            if (this.numPss > 0) {
                pw.print(" (");
                DebugUtils.printSizeValue(pw, this.minPss * 1024);
                pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                DebugUtils.printSizeValue(pw, this.avgPss * 1024);
                pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                DebugUtils.printSizeValue(pw, this.maxPss * 1024);
                pw.print("/");
                DebugUtils.printSizeValue(pw, this.minUss * 1024);
                pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                DebugUtils.printSizeValue(pw, this.avgUss * 1024);
                pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                DebugUtils.printSizeValue(pw, this.maxUss * 1024);
                pw.print("/");
                DebugUtils.printSizeValue(pw, this.minRss * 1024);
                pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                DebugUtils.printSizeValue(pw, this.avgRss * 1024);
                pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                DebugUtils.printSizeValue(pw, this.maxRss * 1024);
                if (full) {
                    pw.print(" over ");
                    pw.print(this.numPss);
                }
                pw.print(")");
            }
        }
    }

    public static class TotalMemoryUseCollection {
        public boolean hasSwappedOutPss;
        final int[] memStates;
        public long[] processStatePss = new long[14];
        public int[] processStateSamples = new int[14];
        public long[] processStateTime = new long[14];
        public double[] processStateWeight = new double[14];
        final int[] screenStates;
        public double sysMemCachedWeight;
        public double sysMemFreeWeight;
        public double sysMemKernelWeight;
        public double sysMemNativeWeight;
        public int sysMemSamples;
        public long[] sysMemUsage = new long[16];
        public double sysMemZRamWeight;
        public long totalTime;

        public TotalMemoryUseCollection(int[] _screenStates, int[] _memStates) {
            this.screenStates = _screenStates;
            this.memStates = _memStates;
        }
    }
}
