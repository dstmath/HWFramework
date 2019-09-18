package com.android.internal.app.procstats;

import android.os.Debug;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
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
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.telephony.PhoneConstants;
import com.android.server.job.JobStatusDumpProto;
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
        public ProcessStats createFromParcel(Parcel in) {
            return new ProcessStats(in);
        }

        public ProcessStats[] newArray(int size) {
            return new ProcessStats[size];
        }
    };
    static final boolean DEBUG = false;
    static final boolean DEBUG_PARCEL = false;
    public static final int FLAG_COMPLETE = 1;
    public static final int FLAG_SHUTDOWN = 2;
    public static final int FLAG_SYSPROPS = 4;
    private static final int MAGIC = 1347638356;
    public static final int[] NON_CACHED_PROC_STATES = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    private static final int PARCEL_VERSION = 27;
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
    private static final Pattern sPageTypeRegex = Pattern.compile("^Node\\s+(\\d+),.*. type\\s+(\\w+)\\s+([\\s\\d]+?)\\s*$");
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
    public int mMemFactor = -1;
    public final long[] mMemFactorDurations = new long[8];
    public final ProcessMap<LongSparseArray<PackageState>> mPackages = new ProcessMap<>();
    private final ArrayList<String> mPageTypeLabels = new ArrayList<>();
    private final ArrayList<int[]> mPageTypeSizes = new ArrayList<>();
    private final ArrayList<Integer> mPageTypeZones = new ArrayList<>();
    public final ProcessMap<ProcessState> mProcesses = new ProcessMap<>();
    public String mReadError;
    boolean mRunning;
    String mRuntime;
    public long mStartTime;
    public final SysMemUsageTable mSysMemUsage = new SysMemUsageTable(this.mTableData);
    public final long[] mSysMemUsageArgs = new long[16];
    public final SparseMappingTable mTableData = new SparseMappingTable();
    public long mTimePeriodEndRealtime;
    public long mTimePeriodEndUptime;
    public long mTimePeriodStartClock;
    public String mTimePeriodStartClockStr;
    public long mTimePeriodStartRealtime;
    public long mTimePeriodStartUptime;

    public static final class PackageState {
        public final String mPackageName;
        public final ArrayMap<String, ProcessState> mProcesses = new ArrayMap<>();
        public final ArrayMap<String, ServiceState> mServices = new ArrayMap<>();
        public final int mUid;

        public PackageState(String packageName, int uid) {
            this.mUid = uid;
            this.mPackageName = packageName;
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
                pw.print(PhoneConstants.APN_TYPE_ALL);
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

    public static final class ProcessStateHolder {
        public final long appVersion;
        public ProcessState state;

        public ProcessStateHolder(long _appVersion) {
            this.appVersion = _appVersion;
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

    public ProcessStats(boolean running) {
        this.mRunning = running;
        reset();
        if (running) {
            Debug.MemoryInfo info = new Debug.MemoryInfo();
            Debug.getMemoryInfo(Process.myPid(), info);
            this.mHasSwappedOutPss = info.hasSwappedOutPss();
        }
    }

    public ProcessStats(Parcel in) {
        reset();
        readFromParcel(in);
    }

    public void add(ProcessStats other) {
        ArrayMap<String, SparseArray<ProcessState>> procMap;
        ProcessState thisProc;
        SparseArray<LongSparseArray<PackageState>> uids;
        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap;
        LongSparseArray<PackageState> versions;
        int NSRVS;
        int NPROCS;
        PackageState otherState;
        int NSRVS2;
        ProcessStats processStats = other;
        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap2 = processStats.mPackages.getMap();
        int ip = 0;
        while (true) {
            int ip2 = ip;
            if (ip2 >= pkgMap2.size()) {
                break;
            }
            String pkgName = pkgMap2.keyAt(ip2);
            SparseArray<LongSparseArray<PackageState>> uids2 = pkgMap2.valueAt(ip2);
            int iu = 0;
            while (true) {
                int iu2 = iu;
                if (iu2 >= uids2.size()) {
                    break;
                }
                int uid = uids2.keyAt(iu2);
                LongSparseArray<PackageState> versions2 = uids2.valueAt(iu2);
                int iv = 0;
                while (true) {
                    int iv2 = iv;
                    if (iv2 >= versions2.size()) {
                        break;
                    }
                    long vers = versions2.keyAt(iv2);
                    PackageState otherState2 = versions2.valueAt(iv2);
                    int NPROCS2 = otherState2.mProcesses.size();
                    int NSRVS3 = otherState2.mServices.size();
                    int iproc = 0;
                    while (true) {
                        int iproc2 = iproc;
                        if (iproc2 >= NPROCS2) {
                            break;
                        }
                        int NSRVS4 = NSRVS3;
                        ProcessState otherProc = otherState2.mProcesses.valueAt(iproc2);
                        int NPROCS3 = NPROCS2;
                        if (otherProc.getCommonProcess() != otherProc) {
                            versions = versions2;
                            ProcessState otherProc2 = otherProc;
                            pkgMap = pkgMap2;
                            NPROCS = NPROCS3;
                            NSRVS2 = NSRVS4;
                            uids = uids2;
                            otherState = otherState2;
                            long vers2 = vers;
                            NSRVS = iv2;
                            ProcessState thisProc2 = getProcessStateLocked(pkgName, uid, vers, otherProc.getName());
                            if (thisProc2.getCommonProcess() == thisProc2) {
                                thisProc2.setMultiPackage(true);
                                long now = SystemClock.uptimeMillis();
                                vers = vers2;
                                PackageState pkgState = getPackageStateLocked(pkgName, uid, vers);
                                thisProc2 = thisProc2.clone(now);
                                long j = now;
                                pkgState.mProcesses.put(thisProc2.getName(), thisProc2);
                            } else {
                                vers = vers2;
                            }
                            thisProc2.add(otherProc2);
                        } else {
                            versions = versions2;
                            pkgMap = pkgMap2;
                            uids = uids2;
                            NSRVS2 = NSRVS4;
                            NPROCS = NPROCS3;
                            otherState = otherState2;
                            NSRVS = iv2;
                        }
                        iproc = iproc2 + 1;
                        NSRVS3 = NSRVS2;
                        otherState2 = otherState;
                        NPROCS2 = NPROCS;
                        iv2 = NSRVS;
                        versions2 = versions;
                        pkgMap2 = pkgMap;
                        uids2 = uids;
                    }
                    int iv3 = iv2;
                    LongSparseArray<PackageState> versions3 = versions2;
                    ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap3 = pkgMap2;
                    SparseArray<LongSparseArray<PackageState>> uids3 = uids2;
                    int NSRVS5 = NSRVS3;
                    PackageState otherState3 = otherState2;
                    int isvc = 0;
                    while (true) {
                        int isvc2 = isvc;
                        if (isvc2 >= NSRVS5) {
                            break;
                        }
                        ServiceState otherSvc = otherState3.mServices.valueAt(isvc2);
                        long j2 = vers;
                        int NSRVS6 = NSRVS5;
                        LongSparseArray<PackageState> longSparseArray = versions3;
                        getServiceStateLocked(pkgName, uid, vers, otherSvc.getProcessName(), otherSvc.getName()).add(otherSvc);
                        isvc = isvc2 + 1;
                        NSRVS5 = NSRVS6;
                    }
                    iv = iv3 + 1;
                    versions2 = versions3;
                    pkgMap2 = pkgMap3;
                    uids2 = uids3;
                }
                SparseArray<LongSparseArray<PackageState>> sparseArray = uids2;
                iu = iu2 + 1;
            }
            ip = ip2 + 1;
        }
        ArrayMap<String, SparseArray<ProcessState>> procMap2 = processStats.mProcesses.getMap();
        int ip3 = 0;
        while (true) {
            int ip4 = ip3;
            if (ip4 >= procMap2.size()) {
                break;
            }
            SparseArray<ProcessState> uids4 = procMap2.valueAt(ip4);
            int iu3 = 0;
            while (true) {
                int iu4 = iu3;
                if (iu4 >= uids4.size()) {
                    break;
                }
                int uid2 = uids4.keyAt(iu4);
                ProcessState otherProc3 = uids4.valueAt(iu4);
                String name = otherProc3.getName();
                String pkg = otherProc3.getPackage();
                long vers3 = otherProc3.getVersion();
                ProcessState thisProc3 = this.mProcesses.get(name, uid2);
                if (thisProc3 == null) {
                    procMap = procMap2;
                    thisProc = new ProcessState(this, pkg, uid2, vers3, name);
                    this.mProcesses.put(name, uid2, thisProc);
                    PackageState thisState = getPackageStateLocked(pkg, uid2, vers3);
                    if (!thisState.mProcesses.containsKey(name)) {
                        thisState.mProcesses.put(name, thisProc);
                    }
                } else {
                    procMap = procMap2;
                    String str = pkg;
                    thisProc = thisProc3;
                }
                thisProc.add(otherProc3);
                iu3 = iu4 + 1;
                procMap2 = procMap;
            }
            ip3 = ip4 + 1;
        }
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= 8) {
                break;
            }
            long[] jArr = this.mMemFactorDurations;
            jArr[i2] = jArr[i2] + processStats.mMemFactorDurations[i2];
            i = i2 + 1;
        }
        this.mSysMemUsage.mergeStats(processStats.mSysMemUsage);
        if (processStats.mTimePeriodStartClock < this.mTimePeriodStartClock) {
            this.mTimePeriodStartClock = processStats.mTimePeriodStartClock;
            this.mTimePeriodStartClockStr = processStats.mTimePeriodStartClockStr;
        }
        this.mTimePeriodEndRealtime += processStats.mTimePeriodEndRealtime - processStats.mTimePeriodStartRealtime;
        this.mTimePeriodEndUptime += processStats.mTimePeriodEndUptime - processStats.mTimePeriodStartUptime;
        this.mInternalSinglePssCount += processStats.mInternalSinglePssCount;
        this.mInternalSinglePssTime += processStats.mInternalSinglePssTime;
        this.mInternalAllMemPssCount += processStats.mInternalAllMemPssCount;
        this.mInternalAllMemPssTime += processStats.mInternalAllMemPssTime;
        this.mInternalAllPollPssCount += processStats.mInternalAllPollPssCount;
        this.mInternalAllPollPssTime += processStats.mInternalAllPollPssTime;
        this.mExternalPssCount += processStats.mExternalPssCount;
        this.mExternalPssTime += processStats.mExternalPssTime;
        this.mExternalSlowPssCount += processStats.mExternalSlowPssCount;
        this.mExternalSlowPssTime += processStats.mExternalSlowPssTime;
        this.mHasSwappedOutPss |= processStats.mHasSwappedOutPss;
    }

    public void addSysMemUsage(long cachedMem, long freeMem, long zramMem, long kernelMem, long nativeMem) {
        if (this.mMemFactor != -1) {
            int state = this.mMemFactor * 14;
            this.mSysMemUsageArgs[0] = 1;
            for (int i = 0; i < 3; i++) {
                this.mSysMemUsageArgs[1 + i] = cachedMem;
                this.mSysMemUsageArgs[4 + i] = freeMem;
                this.mSysMemUsageArgs[7 + i] = zramMem;
                this.mSysMemUsageArgs[10 + i] = kernelMem;
                this.mSysMemUsageArgs[13 + i] = nativeMem;
            }
            this.mSysMemUsage.mergeStats(state, this.mSysMemUsageArgs, 0);
        }
    }

    public void computeTotalMemoryUse(TotalMemoryUseCollection data, long now) {
        long[] totalMemUsage;
        TotalMemoryUseCollection totalMemoryUseCollection = data;
        long j = now;
        totalMemoryUseCollection.totalTime = 0;
        int i = 0;
        for (int i2 = 0; i2 < 14; i2++) {
            totalMemoryUseCollection.processStateWeight[i2] = 0.0d;
            totalMemoryUseCollection.processStatePss[i2] = 0;
            totalMemoryUseCollection.processStateTime[i2] = 0;
            totalMemoryUseCollection.processStateSamples[i2] = 0;
        }
        for (int i3 = 0; i3 < 16; i3++) {
            totalMemoryUseCollection.sysMemUsage[i3] = 0;
        }
        totalMemoryUseCollection.sysMemCachedWeight = 0.0d;
        totalMemoryUseCollection.sysMemFreeWeight = 0.0d;
        totalMemoryUseCollection.sysMemZRamWeight = 0.0d;
        totalMemoryUseCollection.sysMemKernelWeight = 0.0d;
        totalMemoryUseCollection.sysMemNativeWeight = 0.0d;
        totalMemoryUseCollection.sysMemSamples = 0;
        long[] totalMemUsage2 = this.mSysMemUsage.getTotalMemUsage();
        int is = 0;
        while (is < totalMemoryUseCollection.screenStates.length) {
            int im = i;
            while (im < totalMemoryUseCollection.memStates.length) {
                int memBucket = totalMemoryUseCollection.screenStates[is] + totalMemoryUseCollection.memStates[im];
                int stateBucket = memBucket * 14;
                long memTime = this.mMemFactorDurations[memBucket];
                if (this.mMemFactor == memBucket) {
                    memTime += j - this.mStartTime;
                }
                totalMemoryUseCollection.totalTime += memTime;
                int sysKey = this.mSysMemUsage.getKey((byte) stateBucket);
                long[] longs = totalMemUsage2;
                int idx = 0;
                if (sysKey != -1) {
                    long[] tmpLongs = this.mSysMemUsage.getArrayForKey(sysKey);
                    int tmpIndex = SparseMappingTable.getIndexFromKey(sysKey);
                    if (tmpLongs[tmpIndex + 0] >= 3) {
                        totalMemUsage = totalMemUsage2;
                        SysMemUsageTable.mergeSysMemUsage(totalMemoryUseCollection.sysMemUsage, i, longs, 0);
                        longs = tmpLongs;
                        idx = tmpIndex;
                        int i4 = memBucket;
                        int i5 = stateBucket;
                        totalMemoryUseCollection.sysMemCachedWeight += ((double) longs[idx + 2]) * ((double) memTime);
                        totalMemoryUseCollection.sysMemFreeWeight += ((double) longs[idx + 5]) * ((double) memTime);
                        totalMemoryUseCollection.sysMemZRamWeight += ((double) longs[idx + 8]) * ((double) memTime);
                        totalMemoryUseCollection.sysMemKernelWeight += ((double) longs[idx + 11]) * ((double) memTime);
                        totalMemoryUseCollection.sysMemNativeWeight += ((double) longs[idx + 14]) * ((double) memTime);
                        totalMemoryUseCollection.sysMemSamples = (int) (((long) totalMemoryUseCollection.sysMemSamples) + longs[idx + 0]);
                        im++;
                        totalMemUsage2 = totalMemUsage;
                        j = now;
                        i = 0;
                    }
                }
                totalMemUsage = totalMemUsage2;
                int i42 = memBucket;
                int i52 = stateBucket;
                totalMemoryUseCollection.sysMemCachedWeight += ((double) longs[idx + 2]) * ((double) memTime);
                totalMemoryUseCollection.sysMemFreeWeight += ((double) longs[idx + 5]) * ((double) memTime);
                totalMemoryUseCollection.sysMemZRamWeight += ((double) longs[idx + 8]) * ((double) memTime);
                totalMemoryUseCollection.sysMemKernelWeight += ((double) longs[idx + 11]) * ((double) memTime);
                totalMemoryUseCollection.sysMemNativeWeight += ((double) longs[idx + 14]) * ((double) memTime);
                totalMemoryUseCollection.sysMemSamples = (int) (((long) totalMemoryUseCollection.sysMemSamples) + longs[idx + 0]);
                im++;
                totalMemUsage2 = totalMemUsage;
                j = now;
                i = 0;
            }
            is++;
            j = now;
            i = 0;
        }
        totalMemoryUseCollection.hasSwappedOutPss = this.mHasSwappedOutPss;
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        for (int iproc = 0; iproc < procMap.size(); iproc++) {
            SparseArray<ProcessState> uids = procMap.valueAt(iproc);
            for (int iu = 0; iu < uids.size(); iu++) {
                uids.valueAt(iu).aggregatePss(totalMemoryUseCollection, now);
            }
            long j2 = now;
        }
        long j3 = now;
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
                    if (pkgState.mProcesses.size() <= 0 && pkgState.mServices.size() <= 0) {
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
        Arrays.fill(this.mMemFactorDurations, 0);
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
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/pagetypeinfo"));
            Matcher matcher = sPageTypeRegex.matcher("");
            this.mPageTypeZones.clear();
            this.mPageTypeLabels.clear();
            this.mPageTypeSizes.clear();
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    try {
                        break;
                    } catch (IOException e) {
                    }
                } else {
                    matcher.reset(line);
                    if (matcher.matches()) {
                        Integer zone = Integer.valueOf(matcher.group(1), 10);
                        if (zone != null) {
                            this.mPageTypeZones.add(zone);
                            this.mPageTypeLabels.add(matcher.group(2));
                            this.mPageTypeSizes.add(splitAndParseNumbers(matcher.group(3)));
                        }
                    }
                }
            }
            reader.close();
        } catch (IOException e2) {
            this.mPageTypeZones.clear();
            this.mPageTypeLabels.clear();
            this.mPageTypeSizes.clear();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                }
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e4) {
                }
            }
            throw th;
        }
    }

    private static int[] splitAndParseNumbers(String s) {
        int count = 0;
        int N = s.length();
        boolean digit = false;
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

    private void writeCommonString(Parcel out, String name) {
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

    private String readCommonString(Parcel in, int version) {
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        writeToParcel(out, SystemClock.uptimeMillis(), flags);
    }

    public void writeToParcel(Parcel out, long now, int flags) {
        int NUID;
        Parcel parcel = out;
        long j = now;
        parcel.writeInt(MAGIC);
        parcel.writeInt(27);
        parcel.writeInt(14);
        parcel.writeInt(8);
        parcel.writeInt(10);
        parcel.writeInt(16);
        parcel.writeInt(4096);
        this.mCommonStringToIndex = new ArrayMap<>(this.mProcesses.size());
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        int NPROC = procMap.size();
        for (int ip = 0; ip < NPROC; ip++) {
            SparseArray<ProcessState> uids = procMap.valueAt(ip);
            int NUID2 = uids.size();
            for (int iu = 0; iu < NUID2; iu++) {
                uids.valueAt(iu).commitStateTime(j);
            }
        }
        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        int NPKG = pkgMap.size();
        for (int ip2 = 0; ip2 < NPKG; ip2++) {
            SparseArray<LongSparseArray<PackageState>> uids2 = pkgMap.valueAt(ip2);
            int iproc = uids2.size();
            for (int iu2 = 0; iu2 < iproc; iu2++) {
                LongSparseArray<PackageState> vpkgs = uids2.valueAt(iu2);
                int NVERS = vpkgs.size();
                int iv = 0;
                while (iv < NVERS) {
                    PackageState pkgState = vpkgs.valueAt(iv);
                    SparseArray<LongSparseArray<PackageState>> uids3 = uids2;
                    int NPROCS = pkgState.mProcesses.size();
                    int iproc2 = 0;
                    while (true) {
                        NUID = iproc;
                        int NUID3 = iproc2;
                        if (NUID3 >= NPROCS) {
                            break;
                        }
                        int NPROCS2 = NPROCS;
                        ProcessState proc = pkgState.mProcesses.valueAt(NUID3);
                        LongSparseArray<PackageState> vpkgs2 = vpkgs;
                        if (proc.getCommonProcess() != proc) {
                            proc.commitStateTime(j);
                        }
                        iproc2 = NUID3 + 1;
                        iproc = NUID;
                        NPROCS = NPROCS2;
                        vpkgs = vpkgs2;
                    }
                    LongSparseArray<PackageState> vpkgs3 = vpkgs;
                    int NSRVS = pkgState.mServices.size();
                    for (int isvc = 0; isvc < NSRVS; isvc++) {
                        pkgState.mServices.valueAt(isvc).commitStateTime(j);
                    }
                    iv++;
                    uids2 = uids3;
                    iproc = NUID;
                    vpkgs = vpkgs3;
                }
                int i = iproc;
            }
        }
        parcel.writeLong(this.mTimePeriodStartClock);
        parcel.writeLong(this.mTimePeriodStartRealtime);
        parcel.writeLong(this.mTimePeriodEndRealtime);
        parcel.writeLong(this.mTimePeriodStartUptime);
        parcel.writeLong(this.mTimePeriodEndUptime);
        parcel.writeLong(this.mInternalSinglePssCount);
        parcel.writeLong(this.mInternalSinglePssTime);
        parcel.writeLong(this.mInternalAllMemPssCount);
        parcel.writeLong(this.mInternalAllMemPssTime);
        parcel.writeLong(this.mInternalAllPollPssCount);
        parcel.writeLong(this.mInternalAllPollPssTime);
        parcel.writeLong(this.mExternalPssCount);
        parcel.writeLong(this.mExternalPssTime);
        parcel.writeLong(this.mExternalSlowPssCount);
        parcel.writeLong(this.mExternalSlowPssTime);
        parcel.writeString(this.mRuntime);
        parcel.writeInt(this.mHasSwappedOutPss ? 1 : 0);
        parcel.writeInt(this.mFlags);
        this.mTableData.writeToParcel(parcel);
        if (this.mMemFactor != -1) {
            long[] jArr = this.mMemFactorDurations;
            int i2 = this.mMemFactor;
            jArr[i2] = jArr[i2] + (j - this.mStartTime);
            this.mStartTime = j;
        }
        writeCompactedLongArray(parcel, this.mMemFactorDurations, this.mMemFactorDurations.length);
        this.mSysMemUsage.writeToParcel(parcel);
        parcel.writeInt(NPROC);
        for (int ip3 = 0; ip3 < NPROC; ip3++) {
            writeCommonString(parcel, procMap.keyAt(ip3));
            SparseArray<ProcessState> uids4 = procMap.valueAt(ip3);
            int NUID4 = uids4.size();
            parcel.writeInt(NUID4);
            for (int iu3 = 0; iu3 < NUID4; iu3++) {
                parcel.writeInt(uids4.keyAt(iu3));
                ProcessState proc2 = uids4.valueAt(iu3);
                writeCommonString(parcel, proc2.getPackage());
                parcel.writeLong(proc2.getVersion());
                proc2.writeToParcel(parcel, j);
            }
        }
        parcel.writeInt(NPKG);
        for (int ip4 = 0; ip4 < NPKG; ip4++) {
            writeCommonString(parcel, pkgMap.keyAt(ip4));
            SparseArray<LongSparseArray<PackageState>> uids5 = pkgMap.valueAt(ip4);
            int NUID5 = uids5.size();
            parcel.writeInt(NUID5);
            for (int iu4 = 0; iu4 < NUID5; iu4++) {
                parcel.writeInt(uids5.keyAt(iu4));
                LongSparseArray<PackageState> vpkgs4 = uids5.valueAt(iu4);
                int NVERS2 = vpkgs4.size();
                parcel.writeInt(NVERS2);
                int iv2 = 0;
                while (iv2 < NVERS2) {
                    ArrayMap<String, SparseArray<ProcessState>> procMap2 = procMap;
                    int NPROC2 = NPROC;
                    parcel.writeLong(vpkgs4.keyAt(iv2));
                    PackageState pkgState2 = vpkgs4.valueAt(iv2);
                    int NPROCS3 = pkgState2.mProcesses.size();
                    parcel.writeInt(NPROCS3);
                    int iproc3 = 0;
                    while (iproc3 < NPROCS3) {
                        int NPROCS4 = NPROCS3;
                        writeCommonString(parcel, pkgState2.mProcesses.keyAt(iproc3));
                        ProcessState proc3 = pkgState2.mProcesses.valueAt(iproc3);
                        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap2 = pkgMap;
                        if (proc3.getCommonProcess() == proc3) {
                            parcel.writeInt(0);
                        } else {
                            parcel.writeInt(1);
                            proc3.writeToParcel(parcel, j);
                        }
                        iproc3++;
                        NPROCS3 = NPROCS4;
                        pkgMap = pkgMap2;
                    }
                    ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap3 = pkgMap;
                    int NSRVS2 = pkgState2.mServices.size();
                    parcel.writeInt(NSRVS2);
                    int isvc2 = 0;
                    while (isvc2 < NSRVS2) {
                        parcel.writeString(pkgState2.mServices.keyAt(isvc2));
                        ServiceState svc = pkgState2.mServices.valueAt(isvc2);
                        writeCommonString(parcel, svc.getProcessName());
                        svc.writeToParcel(parcel, j);
                        isvc2++;
                        pkgState2 = pkgState2;
                    }
                    iv2++;
                    procMap = procMap2;
                    NPROC = NPROC2;
                    pkgMap = pkgMap3;
                }
                int i3 = NPROC;
                ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> arrayMap = pkgMap;
            }
            int i4 = NPROC;
            ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> arrayMap2 = pkgMap;
        }
        int i5 = NPROC;
        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> arrayMap3 = pkgMap;
        int NPAGETYPES = this.mPageTypeLabels.size();
        parcel.writeInt(NPAGETYPES);
        int i6 = 0;
        while (true) {
            int i7 = i6;
            if (i7 < NPAGETYPES) {
                parcel.writeInt(this.mPageTypeZones.get(i7).intValue());
                parcel.writeString(this.mPageTypeLabels.get(i7));
                parcel.writeIntArray(this.mPageTypeSizes.get(i7));
                i6 = i7 + 1;
            } else {
                this.mCommonStringToIndex = null;
                return;
            }
        }
    }

    private boolean readCheckedInt(Parcel in, int val, String what) {
        int readInt = in.readInt();
        int got = readInt;
        if (readInt == val) {
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
        PackageState pkgState;
        int uid;
        long vers;
        LongSparseArray<PackageState> vpkg;
        ServiceState serv;
        int uid2;
        Parcel parcel = in;
        boolean z = false;
        boolean hadData = this.mPackages.getMap().size() > 0 || this.mProcesses.getMap().size() > 0;
        if (hadData) {
            resetSafely();
        }
        if (readCheckedInt(parcel, MAGIC, "magic number")) {
            int version = in.readInt();
            if (version != 27) {
                this.mReadError = "bad version: " + version;
            } else if (readCheckedInt(parcel, 14, "state count") && readCheckedInt(parcel, 8, "adj count") && readCheckedInt(parcel, 10, "pss count") && readCheckedInt(parcel, 16, "sys mem usage count") && readCheckedInt(parcel, 4096, "longs size")) {
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
                this.mTableData.readFromParcel(parcel);
                readCompactedLongArray(parcel, version, this.mMemFactorDurations, this.mMemFactorDurations.length);
                if (this.mSysMemUsage.readFromParcel(parcel)) {
                    int NPROC = in.readInt();
                    if (NPROC < 0) {
                        this.mReadError = "bad process count: " + NPROC;
                        return;
                    }
                    int NPROC2 = NPROC;
                    while (NPROC2 > 0) {
                        int NPROC3 = NPROC2 - 1;
                        String procName = readCommonString(parcel, version);
                        if (procName == null) {
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
                            String pkgName = readCommonString(parcel, version);
                            if (pkgName == null) {
                                this.mReadError = "bad process package name";
                                return;
                            }
                            long vers2 = in.readLong();
                            ProcessState proc = hadData ? this.mProcesses.get(procName, uid3) : null;
                            if (proc == null) {
                                ProcessState processState = proc;
                                uid2 = uid3;
                                ProcessState processState2 = new ProcessState(this, pkgName, uid3, vers2, procName);
                                proc = processState2;
                                if (!proc.readFromParcel(parcel, true)) {
                                    return;
                                }
                            } else if (proc.readFromParcel(parcel, false)) {
                                uid2 = uid3;
                            } else {
                                return;
                            }
                            this.mProcesses.put(procName, uid2, proc);
                            NUID = NUID2;
                        }
                        NPROC2 = NPROC3;
                    }
                    int NPKG = in.readInt();
                    if (NPKG < 0) {
                        this.mReadError = "bad package count: " + NPKG;
                        return;
                    }
                    while (NPKG > 0) {
                        int NPKG2 = NPKG - 1;
                        String pkgName2 = readCommonString(parcel, version);
                        if (pkgName2 == null) {
                            this.mReadError = "bad package name";
                            return;
                        }
                        int NUID3 = in.readInt();
                        if (NUID3 < 0) {
                            this.mReadError = "bad uid count: " + NUID3;
                            return;
                        }
                        while (NUID3 > 0) {
                            int NUID4 = NUID3 - 1;
                            int uid4 = in.readInt();
                            if (uid4 < 0) {
                                this.mReadError = "bad uid: " + uid4;
                                return;
                            }
                            int NVERS = in.readInt();
                            if (NVERS < 0) {
                                this.mReadError = "bad versions count: " + NVERS;
                                return;
                            }
                            while (NVERS > 0) {
                                int NVERS2 = NVERS - 1;
                                long vers3 = in.readLong();
                                PackageState pkgState2 = new PackageState(pkgName2, uid4);
                                LongSparseArray<PackageState> vpkg2 = this.mPackages.get(pkgName2, uid4);
                                if (vpkg2 == null) {
                                    vpkg2 = new LongSparseArray<>();
                                    this.mPackages.put(pkgName2, uid4, vpkg2);
                                }
                                vpkg2.put(vers3, pkgState2);
                                int NPROCS = in.readInt();
                                if (NPROCS < 0) {
                                    this.mReadError = "bad package process count: " + NPROCS;
                                    return;
                                }
                                int NPROCS2 = NPROCS;
                                while (NPROCS2 > 0) {
                                    NPROCS2--;
                                    String procName2 = readCommonString(parcel, version);
                                    if (procName2 == null) {
                                        this.mReadError = "bad package process name";
                                        return;
                                    }
                                    int hasProc = in.readInt();
                                    ProcessState commonProc = this.mProcesses.get(procName2, uid4);
                                    if (commonProc == null) {
                                        StringBuilder sb = new StringBuilder();
                                        LongSparseArray<PackageState> longSparseArray = vpkg2;
                                        sb.append("no common proc: ");
                                        sb.append(procName2);
                                        this.mReadError = sb.toString();
                                        return;
                                    }
                                    LongSparseArray<PackageState> vpkg3 = vpkg2;
                                    if (hasProc != 0) {
                                        ProcessState proc2 = hadData ? pkgState2.mProcesses.get(procName2) : null;
                                        if (proc2 == null) {
                                            ProcessState processState3 = new ProcessState(commonProc, pkgName2, uid4, vers3, procName2, 0);
                                            proc2 = processState3;
                                            if (!proc2.readFromParcel(parcel, true)) {
                                                return;
                                            }
                                        } else if (!proc2.readFromParcel(parcel, z)) {
                                            return;
                                        }
                                        pkgState2.mProcesses.put(procName2, proc2);
                                    } else {
                                        pkgState2.mProcesses.put(procName2, commonProc);
                                    }
                                    vpkg2 = vpkg3;
                                    z = false;
                                }
                                LongSparseArray<PackageState> vpkg4 = vpkg2;
                                int NSRVS = in.readInt();
                                if (NSRVS < 0) {
                                    this.mReadError = "bad package service count: " + NSRVS;
                                    return;
                                }
                                while (NSRVS > 0) {
                                    int NSRVS2 = NSRVS - 1;
                                    String serviceName = in.readString();
                                    if (serviceName == null) {
                                        this.mReadError = "bad package service name";
                                        return;
                                    }
                                    String processName = version > 9 ? readCommonString(parcel, version) : null;
                                    ServiceState serv2 = hadData ? pkgState2.mServices.get(serviceName) : null;
                                    if (serv2 == null) {
                                        vpkg = vpkg4;
                                        pkgState = pkgState2;
                                        vers = vers3;
                                        uid = uid4;
                                        ServiceState serv3 = new ServiceState(this, pkgName2, serviceName, processName, null);
                                        serv = serv3;
                                    } else {
                                        pkgState = pkgState2;
                                        vers = vers3;
                                        uid = uid4;
                                        vpkg = vpkg4;
                                        serv = serv2;
                                    }
                                    if (serv.readFromParcel(parcel)) {
                                        pkgState2 = pkgState;
                                        pkgState2.mServices.put(serviceName, serv);
                                        NSRVS = NSRVS2;
                                        vpkg4 = vpkg;
                                        vers3 = vers;
                                        uid4 = uid;
                                    } else {
                                        return;
                                    }
                                }
                                NVERS = NVERS2;
                                z = false;
                            }
                            NUID3 = NUID4;
                            z = false;
                        }
                        NPKG = NPKG2;
                        z = false;
                    }
                    int NPAGETYPES = in.readInt();
                    this.mPageTypeZones.clear();
                    this.mPageTypeZones.ensureCapacity(NPAGETYPES);
                    this.mPageTypeLabels.clear();
                    this.mPageTypeLabels.ensureCapacity(NPAGETYPES);
                    this.mPageTypeSizes.clear();
                    this.mPageTypeSizes.ensureCapacity(NPAGETYPES);
                    int i = 0;
                    while (true) {
                        int i2 = i;
                        if (i2 < NPAGETYPES) {
                            this.mPageTypeZones.add(Integer.valueOf(in.readInt()));
                            this.mPageTypeLabels.add(in.readString());
                            this.mPageTypeSizes.add(in.createIntArray());
                            i = i2 + 1;
                        } else {
                            this.mIndexToCommonString = null;
                            return;
                        }
                    }
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
        PackageState as2 = new PackageState(packageName, uid);
        vpkg.put(vers, as2);
        return as2;
    }

    public ProcessState getProcessStateLocked(String packageName, int uid, long vers, String processName) {
        ProcessState commonProc;
        PackageState pkgState;
        ProcessState ps;
        int i = uid;
        String str = processName;
        PackageState pkgState2 = getPackageStateLocked(packageName, uid, vers);
        ProcessState ps2 = pkgState2.mProcesses.get(str);
        if (ps2 != null) {
            return ps2;
        }
        ProcessState commonProc2 = this.mProcesses.get(str, i);
        if (commonProc2 == null) {
            commonProc = new ProcessState(this, packageName, i, vers, str);
            this.mProcesses.put(str, i, commonProc);
        } else {
            commonProc = commonProc2;
        }
        if (!commonProc.isMultiPackage()) {
            String str2 = packageName;
            if (!str2.equals(commonProc.getPackage()) || vers != commonProc.getVersion()) {
                commonProc.setMultiPackage(true);
                long now = SystemClock.uptimeMillis();
                PackageState commonPkgState = getPackageStateLocked(commonProc.getPackage(), i, commonProc.getVersion());
                if (commonPkgState != null) {
                    ProcessState cloned = commonProc.clone(now);
                    commonPkgState.mProcesses.put(commonProc.getName(), cloned);
                    int i2 = commonPkgState.mServices.size() - 1;
                    while (true) {
                        int i3 = i2;
                        if (i3 < 0) {
                            break;
                        }
                        ServiceState ss = commonPkgState.mServices.valueAt(i3);
                        if (ss.getProcess() == commonProc) {
                            ss.setProcess(cloned);
                        }
                        i2 = i3 - 1;
                    }
                } else {
                    Slog.w(TAG, "Cloning proc state: no package state " + commonProc.getPackage() + "/" + i + " for proc " + commonProc.getName());
                }
                pkgState = pkgState2;
                PackageState packageState = commonPkgState;
                ps = new ProcessState(commonProc, str2, i, vers, processName, now);
            } else {
                ps = commonProc;
                pkgState = pkgState2;
            }
        } else {
            pkgState = pkgState2;
            ps = new ProcessState(commonProc, packageName, uid, vers, processName, SystemClock.uptimeMillis());
        }
        pkgState.mProcesses.put(processName, ps);
        return ps;
    }

    public ServiceState getServiceStateLocked(String packageName, int uid, long vers, String processName, String className) {
        PackageState as = getPackageStateLocked(packageName, uid, vers);
        ServiceState ss = as.mServices.get(className);
        if (ss != null) {
            return ss;
        }
        ServiceState serviceState = new ServiceState(this, packageName, className, processName, processName != null ? getProcessStateLocked(packageName, uid, vers, processName) : null);
        ServiceState ss2 = serviceState;
        as.mServices.put(className, ss2);
        return ss2;
    }

    /* JADX WARNING: Removed duplicated region for block: B:76:0x023f  */
    public void dumpLocked(PrintWriter pw, String reqPackage, long now, boolean dumpSummary, boolean dumpAll, boolean activeOnly) {
        PrintWriter printWriter;
        int iu;
        int uid;
        String procName;
        SparseArray<ProcessState> uids;
        PrintWriter printWriter2;
        boolean z;
        boolean printedHeader;
        String pkgName;
        int uid2;
        SparseArray<LongSparseArray<PackageState>> uids2;
        LongSparseArray<PackageState> vpkgs;
        int iv;
        int NSRVS;
        PackageState pkgState;
        boolean z2;
        String str;
        PrintWriter printWriter3;
        int isvc;
        int NSRVS2;
        PrintWriter printWriter4;
        String str2;
        boolean z3;
        PackageState pkgState2;
        int NPROCS;
        PackageState pkgState3;
        long vers;
        int NSRVS3;
        PackageState pkgState4;
        String str3;
        PrintWriter printWriter5;
        PrintWriter printWriter6 = pw;
        String str4 = reqPackage;
        boolean z4 = dumpAll;
        long totalTime = DumpUtils.dumpSingleTime(null, null, this.mMemFactorDurations, this.mMemFactor, this.mStartTime, now);
        boolean sepNeeded = false;
        if (this.mSysMemUsage.getKeyCount() > 0) {
            printWriter6.println("System memory usage:");
            this.mSysMemUsage.dump(printWriter6, "  ", ALL_SCREEN_ADJ, ALL_MEM_ADJ);
            sepNeeded = true;
        }
        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        boolean printedHeader2 = false;
        boolean printedHeader3 = sepNeeded;
        int ip = 0;
        while (ip < pkgMap.size()) {
            String pkgName2 = pkgMap.keyAt(ip);
            SparseArray<LongSparseArray<PackageState>> uids3 = pkgMap.valueAt(ip);
            boolean printedHeader4 = printedHeader3;
            boolean sepNeeded2 = printedHeader2;
            int iu2 = 0;
            while (iu2 < uids3.size()) {
                int uid3 = uids3.keyAt(iu2);
                LongSparseArray<PackageState> vpkgs2 = uids3.valueAt(iu2);
                boolean sepNeeded3 = printedHeader4;
                boolean printedHeader5 = sepNeeded2;
                int iv2 = 0;
                while (true) {
                    int iproc = iv2;
                    if (iproc >= vpkgs2.size()) {
                        break;
                    }
                    int iu3 = iu2;
                    ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap2 = pkgMap;
                    long vers2 = vpkgs2.keyAt(iproc);
                    PackageState pkgState5 = vpkgs2.valueAt(iproc);
                    int NPROCS2 = pkgState5.mProcesses.size();
                    int ip2 = ip;
                    int NSRVS4 = pkgState5.mServices.size();
                    boolean pkgMatch = str4 == null || str4.equals(pkgName2);
                    if (!pkgMatch) {
                        boolean procMatch = false;
                        int iproc2 = 0;
                        while (true) {
                            iv = iproc;
                            int iv3 = iproc2;
                            if (iv3 >= NPROCS2) {
                                vpkgs = vpkgs2;
                                uids2 = uids3;
                                break;
                            }
                            vpkgs = vpkgs2;
                            uids2 = uids3;
                            if (str4.equals(pkgState5.mProcesses.valueAt(iv3).getName())) {
                                procMatch = true;
                                break;
                            }
                            iproc2 = iv3 + 1;
                            iproc = iv;
                            vpkgs2 = vpkgs;
                            uids3 = uids2;
                        }
                        if (!procMatch) {
                            uid2 = uid3;
                            pkgName = pkgName2;
                            iv2 = iv + 1;
                            pkgMap = pkgMap2;
                            iu2 = iu3;
                            ip = ip2;
                            vpkgs2 = vpkgs;
                            uids3 = uids2;
                            uid3 = uid2;
                            pkgName2 = pkgName;
                        }
                    } else {
                        iv = iproc;
                        vpkgs = vpkgs2;
                        uids2 = uids3;
                    }
                    if (NPROCS2 > 0 || NSRVS4 > 0) {
                        if (!printedHeader5) {
                            if (sepNeeded3) {
                                pw.println();
                            }
                            printWriter6.println("Per-Package Stats:");
                            printedHeader5 = true;
                            sepNeeded3 = true;
                        }
                        printWriter6.print("  * ");
                        printWriter6.print(pkgName2);
                        printWriter6.print(" / ");
                        UserHandle.formatUid(printWriter6, uid3);
                        printWriter6.print(" / v");
                        printWriter6.print(vers2);
                        printWriter6.println(":");
                    }
                    boolean printedHeader6 = printedHeader5;
                    boolean sepNeeded4 = sepNeeded3;
                    if (!dumpSummary) {
                        NPROCS = NPROCS2;
                        pkgState3 = pkgState5;
                        uid2 = uid3;
                        pkgName = pkgName2;
                    } else if (z4) {
                        NPROCS = NPROCS2;
                        pkgState3 = pkgState5;
                        uid2 = uid3;
                        pkgName = pkgName2;
                    } else {
                        ArrayList<ProcessState> procs = new ArrayList<>();
                        for (int iproc3 = 0; iproc3 < NPROCS2; iproc3++) {
                            ProcessState proc = pkgState5.mProcesses.valueAt(iproc3);
                            if ((pkgMatch || str4.equals(proc.getName())) && (!activeOnly || proc.isInUse())) {
                                procs.add(proc);
                            }
                        }
                        int i = NPROCS2;
                        ArrayList<ProcessState> arrayList = procs;
                        uid2 = uid3;
                        pkgName = pkgName2;
                        DumpUtils.dumpProcessSummaryLocked(printWriter6, "      ", procs, ALL_SCREEN_ADJ, ALL_MEM_ADJ, NON_CACHED_PROC_STATES, now, totalTime);
                        NSRVS = NSRVS4;
                        long j = vers2;
                        pkgState = pkgState5;
                        str = str4;
                        printWriter3 = printWriter6;
                        z2 = dumpAll;
                        isvc = 0;
                        while (isvc < NSRVS) {
                            ServiceState svc = pkgState.mServices.valueAt(isvc);
                            if (pkgMatch || str.equals(svc.getProcessName())) {
                                if (!activeOnly || svc.isInUse()) {
                                    if (z2) {
                                        printWriter3.print("      Service ");
                                    } else {
                                        printWriter3.print("      * ");
                                    }
                                    printWriter3.print(pkgState.mServices.keyAt(isvc));
                                    printWriter3.println(":");
                                    printWriter3.print("        Process: ");
                                    printWriter3.println(svc.getProcessName());
                                    printWriter4 = printWriter3;
                                    str2 = str;
                                    z3 = z2;
                                    pkgState2 = pkgState;
                                    NSRVS2 = NSRVS;
                                    svc.dumpStats(printWriter3, "        ", "          ", "    ", now, totalTime, dumpSummary, z3);
                                    isvc++;
                                    pkgState = pkgState2;
                                    z2 = z3;
                                    str = str2;
                                    printWriter3 = printWriter4;
                                    NSRVS = NSRVS2;
                                } else {
                                    printWriter3.print("      (Not active: ");
                                    printWriter3.print(pkgState.mServices.keyAt(isvc));
                                    printWriter3.println(")");
                                }
                            }
                            printWriter4 = printWriter3;
                            str2 = str;
                            z3 = z2;
                            pkgState2 = pkgState;
                            NSRVS2 = NSRVS;
                            isvc++;
                            pkgState = pkgState2;
                            z2 = z3;
                            str = str2;
                            printWriter3 = printWriter4;
                            NSRVS = NSRVS2;
                        }
                        printWriter6 = printWriter3;
                        str4 = str;
                        z4 = z2;
                        sepNeeded3 = sepNeeded4;
                        printedHeader5 = printedHeader6;
                        iv2 = iv + 1;
                        pkgMap = pkgMap2;
                        iu2 = iu3;
                        ip = ip2;
                        vpkgs2 = vpkgs;
                        uids3 = uids2;
                        uid3 = uid2;
                        pkgName2 = pkgName;
                    }
                    int iproc4 = 0;
                    while (true) {
                        int iproc5 = iproc4;
                        if (iproc5 >= NPROCS) {
                            break;
                        }
                        ProcessState proc2 = pkgState3.mProcesses.valueAt(iproc5);
                        if (pkgMatch || str4.equals(proc2.getName())) {
                            if (!activeOnly || proc2.isInUse()) {
                                printWriter6.print("      Process ");
                                printWriter6.print(pkgState3.mProcesses.keyAt(iproc5));
                                if (proc2.getCommonProcess().isMultiPackage()) {
                                    printWriter6.print(" (multi, ");
                                } else {
                                    printWriter6.print(" (unique, ");
                                }
                                printWriter6.print(proc2.getDurationsBucketCount());
                                printWriter6.print(" entries)");
                                printWriter6.println(":");
                                ProcessState proc3 = proc2;
                                proc2.dumpProcessState(printWriter6, "        ", ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES, now);
                                NSRVS3 = NSRVS4;
                                vers = vers2;
                                pkgState4 = pkgState3;
                                str3 = str4;
                                printWriter5 = printWriter6;
                                proc3.dumpPss(printWriter6, "        ", ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES);
                                proc3.dumpInternalLocked(printWriter5, "        ", dumpAll);
                                iproc4 = iproc5 + 1;
                                printWriter6 = printWriter5;
                                str4 = str3;
                                pkgState3 = pkgState4;
                                NSRVS4 = NSRVS3;
                                vers2 = vers;
                            } else {
                                printWriter6.print("      (Not active: ");
                                printWriter6.print(pkgState3.mProcesses.keyAt(iproc5));
                                printWriter6.println(")");
                            }
                        }
                        NSRVS3 = NSRVS4;
                        vers = vers2;
                        pkgState4 = pkgState3;
                        str3 = str4;
                        printWriter5 = printWriter6;
                        boolean z5 = dumpAll;
                        iproc4 = iproc5 + 1;
                        printWriter6 = printWriter5;
                        str4 = str3;
                        pkgState3 = pkgState4;
                        NSRVS4 = NSRVS3;
                        vers2 = vers;
                    }
                    NSRVS = NSRVS4;
                    long j2 = vers2;
                    pkgState = pkgState3;
                    str = str4;
                    printWriter3 = printWriter6;
                    z2 = dumpAll;
                    isvc = 0;
                    while (isvc < NSRVS) {
                    }
                    printWriter6 = printWriter3;
                    str4 = str;
                    z4 = z2;
                    sepNeeded3 = sepNeeded4;
                    printedHeader5 = printedHeader6;
                    iv2 = iv + 1;
                    pkgMap = pkgMap2;
                    iu2 = iu3;
                    ip = ip2;
                    vpkgs2 = vpkgs;
                    uids3 = uids2;
                    uid3 = uid2;
                    pkgName2 = pkgName;
                }
                ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> arrayMap = pkgMap;
                SparseArray<LongSparseArray<PackageState>> sparseArray = uids3;
                String str5 = pkgName2;
                iu2++;
                sepNeeded2 = printedHeader5;
                printedHeader4 = sepNeeded3;
            }
            ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> arrayMap2 = pkgMap;
            ip++;
            printedHeader2 = sepNeeded2;
            printedHeader3 = printedHeader4;
        }
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        boolean numShownProcs = false;
        int numTotalProcs = 0;
        boolean sepNeeded5 = printedHeader3;
        int ip3 = 0;
        int numShownProcs2 = 0;
        while (true) {
            int ip4 = ip3;
            if (ip4 >= procMap.size()) {
                break;
            }
            String procName2 = procMap.keyAt(ip4);
            SparseArray<ProcessState> uids4 = procMap.valueAt(ip4);
            boolean printedHeader7 = numShownProcs;
            int numShownProcs3 = numShownProcs2;
            int numShownProcs4 = 0;
            while (true) {
                int iu4 = numShownProcs4;
                if (iu4 >= uids4.size()) {
                    break;
                }
                int uid4 = uids4.keyAt(iu4);
                int numTotalProcs2 = numTotalProcs + 1;
                ProcessState proc4 = uids4.valueAt(iu4);
                if (!proc4.hasAnyData() && proc4.isMultiPackage() && (str4 == null || str4.equals(procName2) || str4.equals(proc4.getPackage()))) {
                    int numShownProcs5 = numShownProcs3 + 1;
                    if (sepNeeded5) {
                        pw.println();
                    }
                    if (!printedHeader7) {
                        printWriter6.println("Multi-Package Common Processes:");
                        printedHeader = true;
                    } else {
                        printedHeader = printedHeader7;
                    }
                    if (!activeOnly || proc4.isInUse()) {
                        printWriter6.print("  * ");
                        printWriter6.print(procName2);
                        printWriter6.print(" / ");
                        UserHandle.formatUid(printWriter6, uid4);
                        printWriter6.print(" (");
                        printWriter6.print(proc4.getDurationsBucketCount());
                        printWriter6.print(" entries)");
                        printWriter6.println(":");
                        ProcessState proc5 = proc4;
                        iu = iu4;
                        int i2 = uid4;
                        proc4.dumpProcessState(printWriter6, "        ", ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES, now);
                        uids = uids4;
                        procName = procName2;
                        uid = ip4;
                        z = z4;
                        printWriter2 = printWriter6;
                        proc5.dumpPss(printWriter6, "        ", ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES);
                        proc5.dumpInternalLocked(printWriter2, "        ", z);
                    } else {
                        printWriter6.print("      (Not active: ");
                        printWriter6.print(procName2);
                        printWriter6.println(")");
                        uids = uids4;
                        uid = ip4;
                        z = z4;
                        printWriter2 = printWriter6;
                        iu = iu4;
                        procName = procName2;
                    }
                    numShownProcs3 = numShownProcs5;
                    sepNeeded5 = true;
                    printedHeader7 = printedHeader;
                } else {
                    uids = uids4;
                    uid = ip4;
                    z = z4;
                    printWriter2 = printWriter6;
                    iu = iu4;
                    procName = procName2;
                }
                str4 = reqPackage;
                z4 = z;
                printWriter6 = printWriter2;
                procName2 = procName;
                ip4 = uid;
                numTotalProcs = numTotalProcs2;
                numShownProcs4 = iu + 1;
                uids4 = uids;
            }
            boolean z6 = z4;
            ip3 = ip4 + 1;
            str4 = reqPackage;
            numShownProcs2 = numShownProcs3;
            numShownProcs = printedHeader7;
        }
        boolean printedHeader8 = z4;
        if (printedHeader8) {
            pw.println();
            printWriter6.print("  Total procs: ");
            printWriter6.print(numShownProcs2);
            printWriter6.print(" shown of ");
            printWriter6.print(numTotalProcs);
            printWriter6.println(" total");
        }
        if (sepNeeded5) {
            pw.println();
        }
        if (dumpSummary) {
            printWriter6.println("Summary:");
            printWriter = printWriter6;
            dumpSummaryLocked(printWriter6, reqPackage, now, activeOnly);
            long j3 = now;
        } else {
            printWriter = printWriter6;
            dumpTotalsLocked(printWriter, now);
        }
        if (printedHeader8) {
            pw.println();
            printWriter.println("Internal state:");
            printWriter.print("  mRunning=");
            printWriter.println(this.mRunning);
        }
        dumpFragmentationLocked(pw);
    }

    public void dumpSummaryLocked(PrintWriter pw, String reqPackage, long now, boolean activeOnly) {
        PrintWriter printWriter = pw;
        dumpFilteredSummaryLocked(printWriter, null, "  ", ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES, NON_CACHED_PROC_STATES, now, DumpUtils.dumpSingleTime(null, null, this.mMemFactorDurations, this.mMemFactor, this.mStartTime, now), reqPackage, activeOnly);
        pw.println();
        dumpTotalsLocked(pw, now);
    }

    private void dumpFragmentationLocked(PrintWriter pw) {
        pw.println();
        pw.println("Available pages by page size:");
        int NPAGETYPES = this.mPageTypeLabels.size();
        for (int i = 0; i < NPAGETYPES; i++) {
            pw.format("Zone %3d  %14s ", new Object[]{this.mPageTypeZones.get(i), this.mPageTypeLabels.get(i)});
            int[] sizes = this.mPageTypeSizes.get(i);
            int N = sizes == null ? 0 : sizes.length;
            for (int j = 0; j < N; j++) {
                pw.format("%6d", new Object[]{Integer.valueOf(sizes[j])});
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
        PrintWriter printWriter = pw;
        printWriter.println("Run time Stats:");
        DumpUtils.dumpSingleTime(printWriter, "  ", this.mMemFactorDurations, this.mMemFactor, this.mStartTime, now);
        pw.println();
        printWriter.println("Memory usage:");
        TotalMemoryUseCollection totalMem = new TotalMemoryUseCollection(ALL_SCREEN_ADJ, ALL_MEM_ADJ);
        computeTotalMemoryUse(totalMem, now);
        PrintWriter printWriter2 = printWriter;
        int i2 = 0;
        long totalPss = printMemoryCategory(printWriter2, "  ", "Native ", totalMem.sysMemNativeWeight, totalMem.totalTime, printMemoryCategory(printWriter2, "  ", "Kernel ", totalMem.sysMemKernelWeight, totalMem.totalTime, 0, totalMem.sysMemSamples), totalMem.sysMemSamples);
        while (true) {
            int i3 = i2;
            if (i3 >= 14) {
                break;
            }
            if (i3 != 6) {
                i = i3;
                totalPss = printMemoryCategory(printWriter, "  ", DumpUtils.STATE_NAMES[i3], totalMem.processStateWeight[i3], totalMem.totalTime, totalPss, totalMem.processStateSamples[i3]);
            } else {
                i = i3;
            }
            i2 = i + 1;
        }
        PrintWriter printWriter3 = printWriter;
        long totalPss2 = printMemoryCategory(printWriter3, "  ", "Z-Ram  ", totalMem.sysMemZRamWeight, totalMem.totalTime, printMemoryCategory(printWriter3, "  ", "Free   ", totalMem.sysMemFreeWeight, totalMem.totalTime, printMemoryCategory(printWriter3, "  ", "Cached ", totalMem.sysMemCachedWeight, totalMem.totalTime, totalPss, totalMem.sysMemSamples), totalMem.sysMemSamples), totalMem.sysMemSamples);
        printWriter.print("  TOTAL  : ");
        DebugUtils.printSizeValue(printWriter, totalPss2);
        pw.println();
        long j = totalPss2;
        printMemoryCategory(printWriter3, "  ", DumpUtils.STATE_NAMES[6], totalMem.processStateWeight[6], totalMem.totalTime, totalPss2, totalMem.processStateSamples[6]);
        pw.println();
        printWriter.println("PSS collection stats:");
        printWriter.print("  Internal Single: ");
        printWriter.print(this.mInternalSinglePssCount);
        printWriter.print("x over ");
        TimeUtils.formatDuration(this.mInternalSinglePssTime, printWriter);
        pw.println();
        printWriter.print("  Internal All Procs (Memory Change): ");
        printWriter.print(this.mInternalAllMemPssCount);
        printWriter.print("x over ");
        TimeUtils.formatDuration(this.mInternalAllMemPssTime, printWriter);
        pw.println();
        printWriter.print("  Internal All Procs (Polling): ");
        printWriter.print(this.mInternalAllPollPssCount);
        printWriter.print("x over ");
        TimeUtils.formatDuration(this.mInternalAllPollPssTime, printWriter);
        pw.println();
        printWriter.print("  External: ");
        printWriter.print(this.mExternalPssCount);
        printWriter.print("x over ");
        TimeUtils.formatDuration(this.mExternalPssTime, printWriter);
        pw.println();
        printWriter.print("  External Slow: ");
        printWriter.print(this.mExternalSlowPssCount);
        printWriter.print("x over ");
        TimeUtils.formatDuration(this.mExternalSlowPssTime, printWriter);
        pw.println();
        pw.println();
        printWriter.print("          Start time: ");
        printWriter.print(DateFormat.format("yyyy-MM-dd HH:mm:ss", this.mTimePeriodStartClock));
        pw.println();
        printWriter.print("        Total uptime: ");
        TimeUtils.formatDuration((this.mRunning ? SystemClock.uptimeMillis() : this.mTimePeriodEndUptime) - this.mTimePeriodStartUptime, printWriter);
        pw.println();
        printWriter.print("  Total elapsed time: ");
        TimeUtils.formatDuration((this.mRunning ? SystemClock.elapsedRealtime() : this.mTimePeriodEndRealtime) - this.mTimePeriodStartRealtime, printWriter);
        boolean partial = true;
        if ((this.mFlags & 2) != 0) {
            printWriter.print(" (shutdown)");
            partial = false;
        }
        if ((this.mFlags & 4) != 0) {
            printWriter.print(" (sysprops)");
            partial = false;
        }
        if ((this.mFlags & 1) != 0) {
            printWriter.print(" (complete)");
            partial = false;
        }
        if (partial) {
            printWriter.print(" (partial)");
        }
        if (this.mHasSwappedOutPss) {
            printWriter.print(" (swapped-out-pss)");
        }
        printWriter.print(' ');
        printWriter.print(this.mRuntime);
        pw.println();
    }

    /* access modifiers changed from: package-private */
    public void dumpFilteredSummaryLocked(PrintWriter pw, String header, String prefix, int[] screenStates, int[] memStates, int[] procStates, int[] sortProcStates, long now, long totalTime, String reqPackage, boolean activeOnly) {
        ArrayList<ProcessState> procs = collectProcessesLocked(screenStates, memStates, procStates, sortProcStates, now, reqPackage, activeOnly);
        if (procs.size() > 0) {
            if (header != null) {
                pw.println();
                pw.println(header);
            }
            DumpUtils.dumpProcessSummaryLocked(pw, prefix, procs, screenStates, memStates, sortProcStates, now, totalTime);
        }
    }

    public ArrayList<ProcessState> collectProcessesLocked(int[] screenStates, int[] memStates, int[] procStates, int[] sortProcStates, long now, String reqPackage, boolean activeOnly) {
        String str = reqPackage;
        ArraySet<ProcessState> foundProcs = new ArraySet<>();
        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        int ip = 0;
        while (ip < pkgMap.size()) {
            String pkgName = pkgMap.keyAt(ip);
            SparseArray<LongSparseArray<PackageState>> procs = pkgMap.valueAt(ip);
            int iu = 0;
            while (iu < procs.size()) {
                LongSparseArray<PackageState> vpkgs = procs.valueAt(iu);
                int NVERS = vpkgs.size();
                int iv = 0;
                while (iv < NVERS) {
                    PackageState state = vpkgs.valueAt(iv);
                    int NPROCS = state.mProcesses.size();
                    boolean pkgMatch = str == null || str.equals(pkgName);
                    int iproc = 0;
                    while (iproc < NPROCS) {
                        ProcessState proc = state.mProcesses.valueAt(iproc);
                        if ((pkgMatch || str.equals(proc.getName())) && (!activeOnly || proc.isInUse())) {
                            foundProcs.add(proc.getCommonProcess());
                        }
                        iproc++;
                        str = reqPackage;
                    }
                    iv++;
                    str = reqPackage;
                }
                iu++;
                str = reqPackage;
            }
            ip++;
            str = reqPackage;
        }
        ArrayList<ProcessState> outProcs = new ArrayList<>(foundProcs.size());
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < foundProcs.size()) {
                ProcessState proc2 = foundProcs.valueAt(i2);
                if (proc2.computeProcessTimeLocked(screenStates, memStates, procStates, now) > 0) {
                    outProcs.add(proc2);
                    int[] iArr = sortProcStates;
                    if (procStates != iArr) {
                        proc2.computeProcessTimeLocked(screenStates, memStates, iArr, now);
                    }
                } else {
                    int[] iArr2 = procStates;
                    int[] iArr3 = sortProcStates;
                }
                i = i2 + 1;
            } else {
                int[] iArr4 = procStates;
                int[] iArr5 = sortProcStates;
                Collections.sort(outProcs, ProcessState.COMPARATOR);
                return outProcs;
            }
        }
    }

    public void dumpCheckinLocked(PrintWriter pw, String reqPackage) {
        PrintWriter printWriter = pw;
        String str = reqPackage;
        long now = SystemClock.uptimeMillis();
        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        printWriter.println("vers,5");
        printWriter.print("period,");
        printWriter.print(this.mTimePeriodStartClockStr);
        printWriter.print(",");
        printWriter.print(this.mTimePeriodStartRealtime);
        printWriter.print(",");
        printWriter.print(this.mRunning ? SystemClock.elapsedRealtime() : this.mTimePeriodEndRealtime);
        boolean partial = true;
        if ((this.mFlags & 2) != 0) {
            printWriter.print(",shutdown");
            partial = false;
        }
        if ((this.mFlags & 4) != 0) {
            printWriter.print(",sysprops");
            partial = false;
        }
        if ((this.mFlags & 1) != 0) {
            printWriter.print(",complete");
            partial = false;
        }
        if (partial) {
            printWriter.print(",partial");
        }
        if (this.mHasSwappedOutPss) {
            printWriter.print(",swapped-out-pss");
        }
        pw.println();
        printWriter.print("config,");
        printWriter.println(this.mRuntime);
        int ip = 0;
        while (true) {
            int ip2 = ip;
            if (ip2 >= pkgMap.size()) {
                break;
            }
            String pkgName = pkgMap.keyAt(ip2);
            if (str == null || str.equals(pkgName)) {
                SparseArray<LongSparseArray<PackageState>> uids = pkgMap.valueAt(ip2);
                int iu = 0;
                while (true) {
                    int iu2 = iu;
                    if (iu2 >= uids.size()) {
                        break;
                    }
                    int uid = uids.keyAt(iu2);
                    LongSparseArray<PackageState> vpkgs = uids.valueAt(iu2);
                    int isvc = 0;
                    while (true) {
                        int iv = isvc;
                        if (iv >= vpkgs.size()) {
                            break;
                        }
                        long vers = vpkgs.keyAt(iv);
                        PackageState pkgState = vpkgs.valueAt(iv);
                        int NPROCS = pkgState.mProcesses.size();
                        int NSRVS = pkgState.mServices.size();
                        int iproc = 0;
                        while (true) {
                            int iproc2 = iproc;
                            if (iproc2 >= NPROCS) {
                                break;
                            }
                            pkgState.mProcesses.valueAt(iproc2).dumpPackageProcCheckin(printWriter, pkgName, uid, vers, pkgState.mProcesses.keyAt(iproc2), now);
                            iproc = iproc2 + 1;
                            pkgName = pkgName;
                            NSRVS = NSRVS;
                            pkgState = pkgState;
                            ip2 = ip2;
                            NPROCS = NPROCS;
                            pkgMap = pkgMap;
                            iv = iv;
                            iu2 = iu2;
                            vpkgs = vpkgs;
                            uids = uids;
                            String str2 = reqPackage;
                        }
                        int NSRVS2 = NSRVS;
                        int i = NPROCS;
                        int iv2 = iv;
                        int iu3 = iu2;
                        LongSparseArray<PackageState> vpkgs2 = vpkgs;
                        SparseArray<LongSparseArray<PackageState>> uids2 = uids;
                        int ip3 = ip2;
                        String pkgName2 = pkgName;
                        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> pkgMap2 = pkgMap;
                        PackageState pkgState2 = pkgState;
                        int isvc2 = 0;
                        while (true) {
                            int isvc3 = isvc2;
                            if (isvc3 >= NSRVS2) {
                                break;
                            }
                            pkgState2.mServices.valueAt(isvc3).dumpTimesCheckin(printWriter, pkgName2, uid, vers, DumpUtils.collapseString(pkgName2, pkgState2.mServices.keyAt(isvc3)), now);
                            isvc2 = isvc3 + 1;
                        }
                        isvc = iv2 + 1;
                        pkgName = pkgName2;
                        ip2 = ip3;
                        pkgMap = pkgMap2;
                        iu2 = iu3;
                        vpkgs = vpkgs2;
                        uids = uids2;
                        String str3 = reqPackage;
                    }
                    SparseArray<LongSparseArray<PackageState>> sparseArray = uids;
                    int i2 = ip2;
                    String str4 = pkgName;
                    ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> arrayMap = pkgMap;
                    iu = iu2 + 1;
                    String str5 = reqPackage;
                }
            }
            ip = ip2 + 1;
            pkgMap = pkgMap;
            str = reqPackage;
        }
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        int iu4 = 0;
        while (true) {
            int ip4 = iu4;
            if (ip4 >= procMap.size()) {
                break;
            }
            String procName = procMap.keyAt(ip4);
            SparseArray<ProcessState> uids3 = procMap.valueAt(ip4);
            int iu5 = 0;
            while (true) {
                int iu6 = iu5;
                if (iu6 >= uids3.size()) {
                    break;
                }
                uids3.valueAt(iu6).dumpProcCheckin(printWriter, procName, uids3.keyAt(iu6), now);
                iu5 = iu6 + 1;
            }
            iu4 = ip4 + 1;
        }
        printWriter.print("total");
        DumpUtils.dumpAdjTimesCheckin(printWriter, ",", this.mMemFactorDurations, this.mMemFactor, this.mStartTime, now);
        pw.println();
        int sysMemUsageCount = this.mSysMemUsage.getKeyCount();
        if (sysMemUsageCount > 0) {
            printWriter.print("sysmemusage");
            for (int i3 = 0; i3 < sysMemUsageCount; i3++) {
                int key = this.mSysMemUsage.getKeyAt(i3);
                int type = SparseMappingTable.getIdFromKey(key);
                printWriter.print(",");
                DumpUtils.printProcStateTag(printWriter, type);
                for (int j = 0; j < 16; j++) {
                    if (j > 1) {
                        printWriter.print(":");
                    }
                    printWriter.print(this.mSysMemUsage.getValue(key, j));
                }
            }
        }
        pw.println();
        TotalMemoryUseCollection totalMem = new TotalMemoryUseCollection(ALL_SCREEN_ADJ, ALL_MEM_ADJ);
        computeTotalMemoryUse(totalMem, now);
        printWriter.print("weights,");
        printWriter.print(totalMem.totalTime);
        printWriter.print(",");
        printWriter.print(totalMem.sysMemCachedWeight);
        printWriter.print(":");
        printWriter.print(totalMem.sysMemSamples);
        printWriter.print(",");
        printWriter.print(totalMem.sysMemFreeWeight);
        printWriter.print(":");
        printWriter.print(totalMem.sysMemSamples);
        printWriter.print(",");
        printWriter.print(totalMem.sysMemZRamWeight);
        printWriter.print(":");
        printWriter.print(totalMem.sysMemSamples);
        printWriter.print(",");
        printWriter.print(totalMem.sysMemKernelWeight);
        printWriter.print(":");
        printWriter.print(totalMem.sysMemSamples);
        printWriter.print(",");
        printWriter.print(totalMem.sysMemNativeWeight);
        printWriter.print(":");
        printWriter.print(totalMem.sysMemSamples);
        for (int i4 = 0; i4 < 14; i4++) {
            printWriter.print(",");
            printWriter.print(totalMem.processStateWeight[i4]);
            printWriter.print(":");
            printWriter.print(totalMem.processStateSamples[i4]);
        }
        pw.println();
        int NPAGETYPES = this.mPageTypeLabels.size();
        for (int i5 = 0; i5 < NPAGETYPES; i5++) {
            printWriter.print("availablepages,");
            printWriter.print(this.mPageTypeLabels.get(i5));
            printWriter.print(",");
            printWriter.print(this.mPageTypeZones.get(i5));
            printWriter.print(",");
            int[] sizes = this.mPageTypeSizes.get(i5);
            int N = sizes == null ? 0 : sizes.length;
            for (int j2 = 0; j2 < N; j2++) {
                if (j2 != 0) {
                    printWriter.print(",");
                }
                printWriter.print(sizes[j2]);
            }
            pw.println();
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId, long now) {
        ProtoOutputStream protoOutputStream = proto;
        ArrayMap<String, SparseArray<LongSparseArray<PackageState>>> map = this.mPackages.getMap();
        long token = proto.start(fieldId);
        protoOutputStream.write(1112396529665L, this.mTimePeriodStartRealtime);
        protoOutputStream.write(1112396529666L, this.mRunning ? SystemClock.elapsedRealtime() : this.mTimePeriodEndRealtime);
        protoOutputStream.write(1112396529667L, this.mTimePeriodStartUptime);
        protoOutputStream.write(1112396529668L, this.mTimePeriodEndUptime);
        protoOutputStream.write(1138166333445L, this.mRuntime);
        protoOutputStream.write(1133871366150L, this.mHasSwappedOutPss);
        boolean partial = true;
        if ((this.mFlags & 2) != 0) {
            protoOutputStream.write(JobStatusDumpProto.REQUIRED_CONSTRAINTS, 3);
            partial = false;
        }
        if ((this.mFlags & 4) != 0) {
            protoOutputStream.write(JobStatusDumpProto.REQUIRED_CONSTRAINTS, 4);
            partial = false;
        }
        if ((this.mFlags & 1) != 0) {
            protoOutputStream.write(JobStatusDumpProto.REQUIRED_CONSTRAINTS, 1);
            partial = false;
        }
        if (partial) {
            protoOutputStream.write(JobStatusDumpProto.REQUIRED_CONSTRAINTS, 2);
        }
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        int ip = 0;
        while (true) {
            int ip2 = ip;
            if (ip2 < procMap.size()) {
                String procName = procMap.keyAt(ip2);
                SparseArray<ProcessState> uids = procMap.valueAt(ip2);
                int iu = 0;
                while (true) {
                    int iu2 = iu;
                    if (iu2 >= uids.size()) {
                        break;
                    }
                    uids.valueAt(iu2).writeToProto(protoOutputStream, 2246267895816L, procName, uids.keyAt(iu2), now);
                    iu = iu2 + 1;
                    ip2 = ip2;
                    uids = uids;
                }
                ip = ip2 + 1;
            } else {
                protoOutputStream.end(token);
                return;
            }
        }
    }
}
