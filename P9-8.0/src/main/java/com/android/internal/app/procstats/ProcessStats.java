package com.android.internal.app.procstats;

import android.os.Debug;
import android.os.Debug.MemoryInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SettingsStringUtil;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.DebugUtils;
import android.util.LogException;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.app.ProcessMap;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.telephony.PhoneConstants;
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
    public static final int[] ALL_MEM_ADJ = new int[]{0, 1, 2, 3};
    public static final int[] ALL_PROC_STATES = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
    public static final int[] ALL_SCREEN_ADJ = new int[]{0, 4};
    public static final int[] BACKGROUND_PROC_STATES = new int[]{2, 3, 4, 5, 6, 7, 8};
    static final int[] BAD_TABLE = new int[0];
    public static long COMMIT_PERIOD = 10800000;
    public static long COMMIT_UPTIME_PERIOD = DateUtils.HOUR_IN_MILLIS;
    public static final Creator<ProcessStats> CREATOR = new Creator<ProcessStats>() {
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
    public static final int[] NON_CACHED_PROC_STATES = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
    private static final int PARCEL_VERSION = 21;
    public static final int PSS_AVERAGE = 2;
    public static final int PSS_COUNT = 7;
    public static final int PSS_MAXIMUM = 3;
    public static final int PSS_MINIMUM = 1;
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
    public static final int STATE_HEAVY_WEIGHT = 5;
    public static final int STATE_HOME = 9;
    public static final int STATE_IMPORTANT_BACKGROUND = 3;
    public static final int STATE_IMPORTANT_FOREGROUND = 2;
    public static final int STATE_LAST_ACTIVITY = 10;
    public static final int STATE_NOTHING = -1;
    public static final int STATE_PERSISTENT = 0;
    public static final int STATE_RECEIVER = 8;
    public static final int STATE_SERVICE = 6;
    public static final int STATE_SERVICE_RESTARTING = 7;
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
    public int mFlags;
    boolean mHasSwappedOutPss;
    ArrayList<String> mIndexToCommonString;
    public int mMemFactor;
    public final long[] mMemFactorDurations;
    public final ProcessMap<SparseArray<PackageState>> mPackages;
    private final ArrayList<String> mPageTypeLabels;
    private final ArrayList<int[]> mPageTypeSizes;
    private final ArrayList<Integer> mPageTypeZones;
    public final ProcessMap<ProcessState> mProcesses;
    public String mReadError;
    boolean mRunning;
    String mRuntime;
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

    public static final class PackageState {
        public final String mPackageName;
        public final ArrayMap<String, ProcessState> mProcesses = new ArrayMap();
        public final ArrayMap<String, ServiceState> mServices = new ArrayMap();
        public final int mUid;

        public PackageState(String packageName, int uid) {
            this.mUid = uid;
            this.mPackageName = packageName;
        }
    }

    public static final class ProcessDataCollection {
        public long avgPss;
        public long avgUss;
        public long maxPss;
        public long maxUss;
        final int[] memStates;
        public long minPss;
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

        void print(PrintWriter pw, long overallTime, boolean full) {
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
                if (full) {
                    pw.print(" over ");
                    pw.print(this.numPss);
                }
                pw.print(")");
            }
        }
    }

    public static final class ProcessStateHolder {
        public final int appVersion;
        public ProcessState state;

        public ProcessStateHolder(int _appVersion) {
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
        this.mPackages = new ProcessMap();
        this.mProcesses = new ProcessMap();
        this.mMemFactorDurations = new long[8];
        this.mMemFactor = -1;
        this.mTableData = new SparseMappingTable();
        this.mSysMemUsageArgs = new long[16];
        this.mSysMemUsage = new SysMemUsageTable(this.mTableData);
        this.mPageTypeZones = new ArrayList();
        this.mPageTypeLabels = new ArrayList();
        this.mPageTypeSizes = new ArrayList();
        this.mRunning = running;
        reset();
        if (running) {
            MemoryInfo info = new MemoryInfo();
            Debug.getMemoryInfo(Process.myPid(), info);
            this.mHasSwappedOutPss = info.hasSwappedOutPss();
        }
    }

    public ProcessStats(Parcel in) {
        this.mPackages = new ProcessMap();
        this.mProcesses = new ProcessMap();
        this.mMemFactorDurations = new long[8];
        this.mMemFactor = -1;
        this.mTableData = new SparseMappingTable();
        this.mSysMemUsageArgs = new long[16];
        this.mSysMemUsage = new SysMemUsageTable(this.mTableData);
        this.mPageTypeZones = new ArrayList();
        this.mPageTypeLabels = new ArrayList();
        this.mPageTypeSizes = new ArrayList();
        reset();
        readFromParcel(in);
    }

    public void add(ProcessStats other) {
        int ip;
        int iu;
        int uid;
        int vers;
        ProcessState otherProc;
        ProcessState thisProc;
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pkgMap = other.mPackages.getMap();
        for (ip = 0; ip < pkgMap.size(); ip++) {
            String pkgName = (String) pkgMap.keyAt(ip);
            SparseArray<SparseArray<PackageState>> uids = (SparseArray) pkgMap.valueAt(ip);
            for (iu = 0; iu < uids.size(); iu++) {
                uid = uids.keyAt(iu);
                SparseArray<PackageState> versions = (SparseArray) uids.valueAt(iu);
                for (int iv = 0; iv < versions.size(); iv++) {
                    vers = versions.keyAt(iv);
                    PackageState otherState = (PackageState) versions.valueAt(iv);
                    int NPROCS = otherState.mProcesses.size();
                    int NSRVS = otherState.mServices.size();
                    for (int iproc = 0; iproc < NPROCS; iproc++) {
                        otherProc = (ProcessState) otherState.mProcesses.valueAt(iproc);
                        if (otherProc.getCommonProcess() != otherProc) {
                            thisProc = getProcessStateLocked(pkgName, uid, vers, otherProc.getName());
                            if (thisProc.getCommonProcess() == thisProc) {
                                thisProc.setMultiPackage(true);
                                long now = SystemClock.uptimeMillis();
                                PackageState pkgState = getPackageStateLocked(pkgName, uid, vers);
                                thisProc = thisProc.clone(now);
                                pkgState.mProcesses.put(thisProc.getName(), thisProc);
                            }
                            thisProc.add(otherProc);
                        }
                    }
                    for (int isvc = 0; isvc < NSRVS; isvc++) {
                        ServiceState otherSvc = (ServiceState) otherState.mServices.valueAt(isvc);
                        getServiceStateLocked(pkgName, uid, vers, otherSvc.getProcessName(), otherSvc.getName()).add(otherSvc);
                    }
                }
            }
        }
        ArrayMap<String, SparseArray<ProcessState>> procMap = other.mProcesses.getMap();
        for (ip = 0; ip < procMap.size(); ip++) {
            SparseArray<ProcessState> uids2 = (SparseArray) procMap.valueAt(ip);
            for (iu = 0; iu < uids2.size(); iu++) {
                uid = uids2.keyAt(iu);
                otherProc = (ProcessState) uids2.valueAt(iu);
                String name = otherProc.getName();
                String pkg = otherProc.getPackage();
                vers = otherProc.getVersion();
                thisProc = (ProcessState) this.mProcesses.get(name, uid);
                if (thisProc == null) {
                    thisProc = new ProcessState(this, pkg, uid, vers, name);
                    this.mProcesses.put(name, uid, thisProc);
                    PackageState thisState = getPackageStateLocked(pkg, uid, vers);
                    if (!thisState.mProcesses.containsKey(name)) {
                        thisState.mProcesses.put(name, thisProc);
                    }
                }
                thisProc.add(otherProc);
            }
        }
        for (int i = 0; i < 8; i++) {
            long[] jArr = this.mMemFactorDurations;
            jArr[i] = jArr[i] + other.mMemFactorDurations[i];
        }
        this.mSysMemUsage.mergeStats(other.mSysMemUsage);
        if (other.mTimePeriodStartClock < this.mTimePeriodStartClock) {
            this.mTimePeriodStartClock = other.mTimePeriodStartClock;
            this.mTimePeriodStartClockStr = other.mTimePeriodStartClockStr;
        }
        this.mTimePeriodEndRealtime += other.mTimePeriodEndRealtime - other.mTimePeriodStartRealtime;
        this.mTimePeriodEndUptime += other.mTimePeriodEndUptime - other.mTimePeriodStartUptime;
        this.mHasSwappedOutPss |= other.mHasSwappedOutPss;
    }

    public void addSysMemUsage(long cachedMem, long freeMem, long zramMem, long kernelMem, long nativeMem) {
        if (this.mMemFactor != -1) {
            int state = this.mMemFactor * 14;
            this.mSysMemUsageArgs[0] = 1;
            for (int i = 0; i < 3; i++) {
                this.mSysMemUsageArgs[i + 1] = cachedMem;
                this.mSysMemUsageArgs[i + 4] = freeMem;
                this.mSysMemUsageArgs[i + 7] = zramMem;
                this.mSysMemUsageArgs[i + 10] = kernelMem;
                this.mSysMemUsageArgs[i + 13] = nativeMem;
            }
            this.mSysMemUsage.mergeStats(state, this.mSysMemUsageArgs, 0);
        }
    }

    public void computeTotalMemoryUse(TotalMemoryUseCollection data, long now) {
        int i;
        data.totalTime = 0;
        for (i = 0; i < 14; i++) {
            data.processStateWeight[i] = 0.0d;
            data.processStatePss[i] = 0;
            data.processStateTime[i] = 0;
            data.processStateSamples[i] = 0;
        }
        for (i = 0; i < 16; i++) {
            data.sysMemUsage[i] = 0;
        }
        data.sysMemCachedWeight = 0.0d;
        data.sysMemFreeWeight = 0.0d;
        data.sysMemZRamWeight = 0.0d;
        data.sysMemKernelWeight = 0.0d;
        data.sysMemNativeWeight = 0.0d;
        data.sysMemSamples = 0;
        long[] totalMemUsage = this.mSysMemUsage.getTotalMemUsage();
        for (int i2 : data.screenStates) {
            for (int i3 : data.memStates) {
                int memBucket = i2 + i3;
                int stateBucket = memBucket * 14;
                long memTime = this.mMemFactorDurations[memBucket];
                if (this.mMemFactor == memBucket) {
                    memTime += now - this.mStartTime;
                }
                data.totalTime += memTime;
                int sysKey = this.mSysMemUsage.getKey((byte) stateBucket);
                long[] longs = totalMemUsage;
                int idx = 0;
                if (sysKey != -1) {
                    long[] tmpLongs = this.mSysMemUsage.getArrayForKey(sysKey);
                    int tmpIndex = SparseMappingTable.getIndexFromKey(sysKey);
                    if (tmpLongs[tmpIndex + 0] >= 3) {
                        SysMemUsageTable.mergeSysMemUsage(data.sysMemUsage, 0, totalMemUsage, 0);
                        longs = tmpLongs;
                        idx = tmpIndex;
                    }
                }
                data.sysMemCachedWeight += ((double) longs[idx + 2]) * ((double) memTime);
                data.sysMemFreeWeight += ((double) longs[idx + 5]) * ((double) memTime);
                data.sysMemZRamWeight += ((double) longs[idx + 8]) * ((double) memTime);
                data.sysMemKernelWeight += ((double) longs[idx + 11]) * ((double) memTime);
                data.sysMemNativeWeight += ((double) longs[idx + 14]) * ((double) memTime);
                data.sysMemSamples = (int) (((long) data.sysMemSamples) + longs[idx + 0]);
            }
        }
        data.hasSwappedOutPss = this.mHasSwappedOutPss;
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        for (int iproc = 0; iproc < procMap.size(); iproc++) {
            SparseArray<ProcessState> uids = (SparseArray) procMap.valueAt(iproc);
            for (int iu = 0; iu < uids.size(); iu++) {
                ((ProcessState) uids.valueAt(iu)).aggregatePss(data, now);
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
        int ip;
        SparseArray<ProcessState> uids;
        int iu;
        ProcessState ps;
        resetCommon();
        long now = SystemClock.uptimeMillis();
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        for (ip = procMap.size() - 1; ip >= 0; ip--) {
            uids = (SparseArray) procMap.valueAt(ip);
            for (iu = uids.size() - 1; iu >= 0; iu--) {
                ((ProcessState) uids.valueAt(iu)).tmpNumInUse = 0;
            }
        }
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        for (ip = pkgMap.size() - 1; ip >= 0; ip--) {
            SparseArray<SparseArray<PackageState>> uids2 = (SparseArray) pkgMap.valueAt(ip);
            for (iu = uids2.size() - 1; iu >= 0; iu--) {
                SparseArray<PackageState> vpkgs = (SparseArray) uids2.valueAt(iu);
                for (int iv = vpkgs.size() - 1; iv >= 0; iv--) {
                    PackageState pkgState = (PackageState) vpkgs.valueAt(iv);
                    for (int iproc = pkgState.mProcesses.size() - 1; iproc >= 0; iproc--) {
                        ps = (ProcessState) pkgState.mProcesses.valueAt(iproc);
                        if (ps.isInUse()) {
                            ps.resetSafely(now);
                            ProcessState commonProcess = ps.getCommonProcess();
                            commonProcess.tmpNumInUse++;
                            ps.getCommonProcess().tmpFoundSubProc = ps;
                        } else {
                            ((ProcessState) pkgState.mProcesses.valueAt(iproc)).makeDead();
                            pkgState.mProcesses.removeAt(iproc);
                        }
                    }
                    for (int isvc = pkgState.mServices.size() - 1; isvc >= 0; isvc--) {
                        ServiceState ss = (ServiceState) pkgState.mServices.valueAt(isvc);
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
                    uids2.removeAt(iu);
                }
            }
            if (uids2.size() <= 0) {
                pkgMap.removeAt(ip);
            }
        }
        for (ip = procMap.size() - 1; ip >= 0; ip--) {
            uids = (SparseArray) procMap.valueAt(ip);
            for (iu = uids.size() - 1; iu >= 0; iu--) {
                ps = (ProcessState) uids.valueAt(iu);
                if (!ps.isInUse() && ps.tmpNumInUse <= 0) {
                    ps.makeDead();
                    uids.removeAt(iu);
                } else if (!ps.isActive() && ps.isMultiPackage() && ps.tmpNumInUse == 1) {
                    ps = ps.tmpFoundSubProc;
                    ps.makeStandalone();
                    uids.setValueAt(iu, ps);
                } else {
                    ps.resetSafely(now);
                }
            }
            if (uids.size() <= 0) {
                procMap.removeAt(ip);
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
        elapsedRealtime = SystemClock.uptimeMillis();
        this.mTimePeriodEndUptime = elapsedRealtime;
        this.mTimePeriodStartUptime = elapsedRealtime;
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
        this.mTimePeriodStartClockStr = DateFormat.format((CharSequence) "yyyy-MM-dd-HH-mm-ss", this.mTimePeriodStartClock).toString();
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0079 A:{SYNTHETIC, Splitter: B:23:0x0079} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0084 A:{SYNTHETIC, Splitter: B:30:0x0084} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateFragmentation() {
        Throwable th;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader("/proc/pagetypeinfo"));
            try {
                Matcher matcher = sPageTypeRegex.matcher(LogException.NO_VALUE);
                this.mPageTypeZones.clear();
                this.mPageTypeLabels.clear();
                this.mPageTypeSizes.clear();
                while (true) {
                    String line = reader2.readLine();
                    if (line == null) {
                        break;
                    }
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
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e) {
                    }
                }
            } catch (IOException e2) {
                reader = reader2;
                try {
                    this.mPageTypeZones.clear();
                    this.mPageTypeLabels.clear();
                    this.mPageTypeSizes.clear();
                    if (reader != null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e3) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                reader = reader2;
                if (reader != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            this.mPageTypeZones.clear();
            this.mPageTypeLabels.clear();
            this.mPageTypeSizes.clear();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e5) {
                }
            }
        }
    }

    private static int[] splitAndParseNumbers(String s) {
        int i;
        char c;
        boolean digit = false;
        int count = 0;
        int N = s.length();
        for (i = 0; i < N; i++) {
            c = s.charAt(i);
            if (c < '0' || c > '9') {
                digit = false;
            } else if (!digit) {
                digit = true;
                count++;
            }
        }
        int[] result = new int[count];
        int val = 0;
        i = 0;
        int p = 0;
        while (i < N) {
            int p2;
            c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                if (digit) {
                    val = (val * 10) + (c - 48);
                } else {
                    digit = true;
                    val = c - 48;
                }
                p2 = p;
            } else if (digit) {
                digit = false;
                p2 = p + 1;
                result[p] = val;
            } else {
                p2 = p;
            }
            i++;
            p = p2;
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
                int bottom = (int) (4294967295L & val);
                out.writeInt(~((int) ((val >> 32) & 2147483647L)));
                out.writeInt(bottom);
            }
        }
    }

    private void readCompactedLongArray(Parcel in, int version, long[] array, int num) {
        if (version <= 10) {
            in.readLongArray(array);
            return;
        }
        int alen = array.length;
        if (num > alen) {
            throw new RuntimeException("bad array lengths: got " + num + " array is " + alen);
        }
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
    }

    private void writeCommonString(Parcel out, String name) {
        Integer index = (Integer) this.mCommonStringToIndex.get(name);
        if (index != null) {
            out.writeInt(index.intValue());
            return;
        }
        index = Integer.valueOf(this.mCommonStringToIndex.size());
        this.mCommonStringToIndex.put(name, index);
        out.writeInt(~index.intValue());
        out.writeString(name);
    }

    private String readCommonString(Parcel in, int version) {
        if (version <= 9) {
            return in.readString();
        }
        int index = in.readInt();
        if (index >= 0) {
            return (String) this.mIndexToCommonString.get(index);
        }
        index = ~index;
        String name = in.readString();
        while (this.mIndexToCommonString.size() <= index) {
            this.mIndexToCommonString.add(null);
        }
        this.mIndexToCommonString.set(index, name);
        return name;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        writeToParcel(out, SystemClock.uptimeMillis(), flags);
    }

    public void writeToParcel(Parcel out, long now, int flags) {
        int ip;
        SparseArray<ProcessState> uids;
        int NUID;
        int iu;
        SparseArray<SparseArray<PackageState>> uids2;
        SparseArray<PackageState> vpkgs;
        int NVERS;
        int iv;
        PackageState pkgState;
        int NPROCS;
        int iproc;
        ProcessState proc;
        int NSRVS;
        int isvc;
        out.writeInt(MAGIC);
        out.writeInt(21);
        out.writeInt(14);
        out.writeInt(8);
        out.writeInt(7);
        out.writeInt(16);
        out.writeInt(4096);
        this.mCommonStringToIndex = new ArrayMap(this.mProcesses.size());
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        int NPROC = procMap.size();
        for (ip = 0; ip < NPROC; ip++) {
            uids = (SparseArray) procMap.valueAt(ip);
            NUID = uids.size();
            for (iu = 0; iu < NUID; iu++) {
                ((ProcessState) uids.valueAt(iu)).commitStateTime(now);
            }
        }
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        int NPKG = pkgMap.size();
        for (ip = 0; ip < NPKG; ip++) {
            uids2 = (SparseArray) pkgMap.valueAt(ip);
            NUID = uids2.size();
            for (iu = 0; iu < NUID; iu++) {
                vpkgs = (SparseArray) uids2.valueAt(iu);
                NVERS = vpkgs.size();
                for (iv = 0; iv < NVERS; iv++) {
                    pkgState = (PackageState) vpkgs.valueAt(iv);
                    NPROCS = pkgState.mProcesses.size();
                    for (iproc = 0; iproc < NPROCS; iproc++) {
                        proc = (ProcessState) pkgState.mProcesses.valueAt(iproc);
                        if (proc.getCommonProcess() != proc) {
                            proc.commitStateTime(now);
                        }
                    }
                    NSRVS = pkgState.mServices.size();
                    for (isvc = 0; isvc < NSRVS; isvc++) {
                        ((ServiceState) pkgState.mServices.valueAt(isvc)).commitStateTime(now);
                    }
                }
            }
        }
        out.writeLong(this.mTimePeriodStartClock);
        out.writeLong(this.mTimePeriodStartRealtime);
        out.writeLong(this.mTimePeriodEndRealtime);
        out.writeLong(this.mTimePeriodStartUptime);
        out.writeLong(this.mTimePeriodEndUptime);
        out.writeString(this.mRuntime);
        out.writeInt(this.mHasSwappedOutPss ? 1 : 0);
        out.writeInt(this.mFlags);
        this.mTableData.writeToParcel(out);
        if (this.mMemFactor != -1) {
            long[] jArr = this.mMemFactorDurations;
            int i = this.mMemFactor;
            jArr[i] = jArr[i] + (now - this.mStartTime);
            this.mStartTime = now;
        }
        writeCompactedLongArray(out, this.mMemFactorDurations, this.mMemFactorDurations.length);
        this.mSysMemUsage.writeToParcel(out);
        out.writeInt(NPROC);
        for (ip = 0; ip < NPROC; ip++) {
            writeCommonString(out, (String) procMap.keyAt(ip));
            uids = (SparseArray) procMap.valueAt(ip);
            NUID = uids.size();
            out.writeInt(NUID);
            for (iu = 0; iu < NUID; iu++) {
                out.writeInt(uids.keyAt(iu));
                proc = (ProcessState) uids.valueAt(iu);
                writeCommonString(out, proc.getPackage());
                out.writeInt(proc.getVersion());
                proc.writeToParcel(out, now);
            }
        }
        out.writeInt(NPKG);
        for (ip = 0; ip < NPKG; ip++) {
            writeCommonString(out, (String) pkgMap.keyAt(ip));
            uids2 = (SparseArray) pkgMap.valueAt(ip);
            NUID = uids2.size();
            out.writeInt(NUID);
            for (iu = 0; iu < NUID; iu++) {
                out.writeInt(uids2.keyAt(iu));
                vpkgs = (SparseArray) uids2.valueAt(iu);
                NVERS = vpkgs.size();
                out.writeInt(NVERS);
                for (iv = 0; iv < NVERS; iv++) {
                    out.writeInt(vpkgs.keyAt(iv));
                    pkgState = (PackageState) vpkgs.valueAt(iv);
                    NPROCS = pkgState.mProcesses.size();
                    out.writeInt(NPROCS);
                    for (iproc = 0; iproc < NPROCS; iproc++) {
                        writeCommonString(out, (String) pkgState.mProcesses.keyAt(iproc));
                        proc = (ProcessState) pkgState.mProcesses.valueAt(iproc);
                        if (proc.getCommonProcess() == proc) {
                            out.writeInt(0);
                        } else {
                            out.writeInt(1);
                            proc.writeToParcel(out, now);
                        }
                    }
                    NSRVS = pkgState.mServices.size();
                    out.writeInt(NSRVS);
                    for (isvc = 0; isvc < NSRVS; isvc++) {
                        out.writeString((String) pkgState.mServices.keyAt(isvc));
                        ServiceState svc = (ServiceState) pkgState.mServices.valueAt(isvc);
                        writeCommonString(out, svc.getProcessName());
                        svc.writeToParcel(out, now);
                    }
                }
            }
        }
        int NPAGETYPES = this.mPageTypeLabels.size();
        out.writeInt(NPAGETYPES);
        for (int i2 = 0; i2 < NPAGETYPES; i2++) {
            out.writeInt(((Integer) this.mPageTypeZones.get(i2)).intValue());
            out.writeString((String) this.mPageTypeLabels.get(i2));
            out.writeIntArray((int[]) this.mPageTypeSizes.get(i2));
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
        boolean hadData = this.mPackages.getMap().size() <= 0 ? this.mProcesses.getMap().size() > 0 : true;
        if (hadData) {
            resetSafely();
        }
        if (readCheckedInt(in, MAGIC, "magic number")) {
            int version = in.readInt();
            if (version != 21) {
                this.mReadError = "bad version: " + version;
                return;
            }
            if (readCheckedInt(in, 14, "state count")) {
                if (readCheckedInt(in, 8, "adj count")) {
                    if (readCheckedInt(in, 7, "pss count")) {
                        if (readCheckedInt(in, 16, "sys mem usage count")) {
                            if (readCheckedInt(in, 4096, "longs size")) {
                                this.mIndexToCommonString = new ArrayList();
                                this.mTimePeriodStartClock = in.readLong();
                                buildTimePeriodStartClockStr();
                                this.mTimePeriodStartRealtime = in.readLong();
                                this.mTimePeriodEndRealtime = in.readLong();
                                this.mTimePeriodStartUptime = in.readLong();
                                this.mTimePeriodEndUptime = in.readLong();
                                this.mRuntime = in.readString();
                                this.mHasSwappedOutPss = in.readInt() != 0;
                                this.mFlags = in.readInt();
                                this.mTableData.readFromParcel(in);
                                readCompactedLongArray(in, version, this.mMemFactorDurations, this.mMemFactorDurations.length);
                                if (this.mSysMemUsage.readFromParcel(in)) {
                                    String procName;
                                    int NUID;
                                    int uid;
                                    String pkgName;
                                    int vers;
                                    Object proc;
                                    int NPROC = in.readInt();
                                    if (NPROC < 0) {
                                        this.mReadError = "bad process count: " + NPROC;
                                        return;
                                    }
                                    while (NPROC > 0) {
                                        NPROC--;
                                        procName = readCommonString(in, version);
                                        if (procName == null) {
                                            this.mReadError = "bad process name";
                                            return;
                                        }
                                        NUID = in.readInt();
                                        if (NUID < 0) {
                                            this.mReadError = "bad uid count: " + NUID;
                                            return;
                                        }
                                        while (NUID > 0) {
                                            NUID--;
                                            uid = in.readInt();
                                            if (uid < 0) {
                                                this.mReadError = "bad uid: " + uid;
                                                return;
                                            }
                                            pkgName = readCommonString(in, version);
                                            if (pkgName == null) {
                                                this.mReadError = "bad process package name";
                                                return;
                                            }
                                            vers = in.readInt();
                                            proc = hadData ? (ProcessState) this.mProcesses.get(procName, uid) : null;
                                            if (proc == null) {
                                                proc = new ProcessState(this, pkgName, uid, vers, procName);
                                                if (!proc.readFromParcel(in, true)) {
                                                    return;
                                                }
                                            } else if (!proc.readFromParcel(in, false)) {
                                                return;
                                            }
                                            this.mProcesses.put(procName, uid, proc);
                                        }
                                    }
                                    int NPKG = in.readInt();
                                    if (NPKG < 0) {
                                        this.mReadError = "bad package count: " + NPKG;
                                        return;
                                    }
                                    while (NPKG > 0) {
                                        NPKG--;
                                        pkgName = readCommonString(in, version);
                                        if (pkgName == null) {
                                            this.mReadError = "bad package name";
                                            return;
                                        }
                                        NUID = in.readInt();
                                        if (NUID < 0) {
                                            this.mReadError = "bad uid count: " + NUID;
                                            return;
                                        }
                                        while (NUID > 0) {
                                            NUID--;
                                            uid = in.readInt();
                                            if (uid < 0) {
                                                this.mReadError = "bad uid: " + uid;
                                                return;
                                            }
                                            int NVERS = in.readInt();
                                            if (NVERS < 0) {
                                                this.mReadError = "bad versions count: " + NVERS;
                                                return;
                                            }
                                            while (NVERS > 0) {
                                                NVERS--;
                                                vers = in.readInt();
                                                PackageState packageState = new PackageState(pkgName, uid);
                                                SparseArray<PackageState> vpkg = (SparseArray) this.mPackages.get(pkgName, uid);
                                                if (vpkg == null) {
                                                    vpkg = new SparseArray();
                                                    this.mPackages.put(pkgName, uid, vpkg);
                                                }
                                                vpkg.put(vers, packageState);
                                                int NPROCS = in.readInt();
                                                if (NPROCS < 0) {
                                                    this.mReadError = "bad package process count: " + NPROCS;
                                                    return;
                                                }
                                                while (NPROCS > 0) {
                                                    NPROCS--;
                                                    procName = readCommonString(in, version);
                                                    if (procName == null) {
                                                        this.mReadError = "bad package process name";
                                                        return;
                                                    }
                                                    int hasProc = in.readInt();
                                                    ProcessState commonProc = (ProcessState) this.mProcesses.get(procName, uid);
                                                    if (commonProc == null) {
                                                        this.mReadError = "no common proc: " + procName;
                                                        return;
                                                    } else if (hasProc != 0) {
                                                        proc = hadData ? (ProcessState) packageState.mProcesses.get(procName) : null;
                                                        if (proc == null) {
                                                            proc = new ProcessState(commonProc, pkgName, uid, vers, procName, 0);
                                                            if (!proc.readFromParcel(in, true)) {
                                                                return;
                                                            }
                                                        } else if (!proc.readFromParcel(in, false)) {
                                                            return;
                                                        }
                                                        packageState.mProcesses.put(procName, proc);
                                                    } else {
                                                        packageState.mProcesses.put(procName, commonProc);
                                                    }
                                                }
                                                int NSRVS = in.readInt();
                                                if (NSRVS < 0) {
                                                    this.mReadError = "bad package service count: " + NSRVS;
                                                    return;
                                                }
                                                while (NSRVS > 0) {
                                                    NSRVS--;
                                                    String serviceName = in.readString();
                                                    if (serviceName == null) {
                                                        this.mReadError = "bad package service name";
                                                        return;
                                                    }
                                                    String processName = version > 9 ? readCommonString(in, version) : null;
                                                    ServiceState serv = hadData ? (ServiceState) packageState.mServices.get(serviceName) : null;
                                                    if (serv == null) {
                                                        serv = new ServiceState(this, pkgName, serviceName, processName, null);
                                                    }
                                                    if (serv.readFromParcel(in)) {
                                                        packageState.mServices.put(serviceName, serv);
                                                    } else {
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    int NPAGETYPES = in.readInt();
                                    this.mPageTypeZones.clear();
                                    this.mPageTypeZones.ensureCapacity(NPAGETYPES);
                                    this.mPageTypeLabels.clear();
                                    this.mPageTypeLabels.ensureCapacity(NPAGETYPES);
                                    this.mPageTypeSizes.clear();
                                    this.mPageTypeSizes.ensureCapacity(NPAGETYPES);
                                    for (int i = 0; i < NPAGETYPES; i++) {
                                        this.mPageTypeZones.add(Integer.valueOf(in.readInt()));
                                        this.mPageTypeLabels.add(in.readString());
                                        this.mPageTypeSizes.add(in.createIntArray());
                                    }
                                    this.mIndexToCommonString = null;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public PackageState getPackageStateLocked(String packageName, int uid, int vers) {
        SparseArray<PackageState> vpkg = (SparseArray) this.mPackages.get(packageName, uid);
        if (vpkg == null) {
            vpkg = new SparseArray();
            this.mPackages.put(packageName, uid, vpkg);
        }
        PackageState as = (PackageState) vpkg.get(vers);
        if (as != null) {
            return as;
        }
        as = new PackageState(packageName, uid);
        vpkg.put(vers, as);
        return as;
    }

    public ProcessState getProcessStateLocked(String packageName, int uid, int vers, String processName) {
        PackageState pkgState = getPackageStateLocked(packageName, uid, vers);
        ProcessState ps = (ProcessState) pkgState.mProcesses.get(processName);
        if (ps != null) {
            return ps;
        }
        ProcessState commonProc = (ProcessState) this.mProcesses.get(processName, uid);
        if (commonProc == null) {
            commonProc = new ProcessState(this, packageName, uid, vers, processName);
            this.mProcesses.put(processName, uid, commonProc);
        }
        if (commonProc.isMultiPackage()) {
            ProcessState processState = new ProcessState(commonProc, packageName, uid, vers, processName, SystemClock.uptimeMillis());
        } else {
            if (packageName.equals(commonProc.getPackage()) && vers == commonProc.getVersion()) {
                ps = commonProc;
            } else {
                commonProc.setMultiPackage(true);
                long now = SystemClock.uptimeMillis();
                PackageState commonPkgState = getPackageStateLocked(commonProc.getPackage(), uid, commonProc.getVersion());
                if (commonPkgState != null) {
                    ProcessState cloned = commonProc.clone(now);
                    commonPkgState.mProcesses.put(commonProc.getName(), cloned);
                    for (int i = commonPkgState.mServices.size() - 1; i >= 0; i--) {
                        ServiceState ss = (ServiceState) commonPkgState.mServices.valueAt(i);
                        if (ss.getProcess() == commonProc) {
                            ss.setProcess(cloned);
                        }
                    }
                } else {
                    Slog.w(TAG, "Cloning proc state: no package state " + commonProc.getPackage() + "/" + uid + " for proc " + commonProc.getName());
                }
                ps = new ProcessState(commonProc, packageName, uid, vers, processName, now);
            }
        }
        pkgState.mProcesses.put(processName, ps);
        return ps;
    }

    public ServiceState getServiceStateLocked(String packageName, int uid, int vers, String processName, String className) {
        PackageState as = getPackageStateLocked(packageName, uid, vers);
        ServiceState ss = (ServiceState) as.mServices.get(className);
        if (ss != null) {
            return ss;
        }
        ss = new ServiceState(this, packageName, className, processName, processName != null ? getProcessStateLocked(packageName, uid, vers, processName) : null);
        as.mServices.put(className, ss);
        return ss;
    }

    public void dumpLocked(PrintWriter pw, String reqPackage, long now, boolean dumpSummary, boolean dumpAll, boolean activeOnly) {
        int ip;
        int iu;
        int uid;
        ProcessState proc;
        long totalTime = DumpUtils.dumpSingleTime(null, null, this.mMemFactorDurations, this.mMemFactor, this.mStartTime, now);
        boolean sepNeeded = false;
        if (this.mSysMemUsage.getKeyCount() > 0) {
            pw.println("System memory usage:");
            this.mSysMemUsage.dump(pw, "  ", ALL_SCREEN_ADJ, ALL_MEM_ADJ);
            sepNeeded = true;
        }
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        boolean printedHeader = false;
        for (ip = 0; ip < pkgMap.size(); ip++) {
            String pkgName = (String) pkgMap.keyAt(ip);
            SparseArray<SparseArray<PackageState>> uids = (SparseArray) pkgMap.valueAt(ip);
            for (iu = 0; iu < uids.size(); iu++) {
                uid = uids.keyAt(iu);
                SparseArray<PackageState> vpkgs = (SparseArray) uids.valueAt(iu);
                for (int iv = 0; iv < vpkgs.size(); iv++) {
                    int iproc;
                    int vers = vpkgs.keyAt(iv);
                    PackageState pkgState = (PackageState) vpkgs.valueAt(iv);
                    int NPROCS = pkgState.mProcesses.size();
                    int NSRVS = pkgState.mServices.size();
                    boolean pkgMatch = reqPackage != null ? reqPackage.equals(pkgName) : true;
                    if (!pkgMatch) {
                        boolean procMatch = false;
                        for (iproc = 0; iproc < NPROCS; iproc++) {
                            if (reqPackage.equals(((ProcessState) pkgState.mProcesses.valueAt(iproc)).getName())) {
                                procMatch = true;
                                break;
                            }
                        }
                        if (!procMatch) {
                        }
                    }
                    if (NPROCS > 0 || NSRVS > 0) {
                        if (!printedHeader) {
                            if (sepNeeded) {
                                pw.println();
                            }
                            pw.println("Per-Package Stats:");
                            printedHeader = true;
                            sepNeeded = true;
                        }
                        pw.print("  * ");
                        pw.print(pkgName);
                        pw.print(" / ");
                        UserHandle.formatUid(pw, uid);
                        pw.print(" / v");
                        pw.print(vers);
                        pw.println(SettingsStringUtil.DELIMITER);
                    }
                    if (!dumpSummary || dumpAll) {
                        iproc = 0;
                        while (iproc < NPROCS) {
                            proc = (ProcessState) pkgState.mProcesses.valueAt(iproc);
                            if (!pkgMatch) {
                                if ((reqPackage.equals(proc.getName()) ^ 1) != 0) {
                                    iproc++;
                                }
                            }
                            if (!activeOnly || (proc.isInUse() ^ 1) == 0) {
                                pw.print("      Process ");
                                pw.print((String) pkgState.mProcesses.keyAt(iproc));
                                if (proc.getCommonProcess().isMultiPackage()) {
                                    pw.print(" (multi, ");
                                } else {
                                    pw.print(" (unique, ");
                                }
                                pw.print(proc.getDurationsBucketCount());
                                pw.print(" entries)");
                                pw.println(SettingsStringUtil.DELIMITER);
                                proc.dumpProcessState(pw, "        ", ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES, now);
                                proc.dumpPss(pw, "        ", ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES);
                                proc.dumpInternalLocked(pw, "        ", dumpAll);
                                iproc++;
                            } else {
                                pw.print("      (Not active: ");
                                pw.print((String) pkgState.mProcesses.keyAt(iproc));
                                pw.println(")");
                                iproc++;
                            }
                        }
                    } else {
                        ArrayList<ProcessState> procs = new ArrayList();
                        iproc = 0;
                        while (iproc < NPROCS) {
                            proc = (ProcessState) pkgState.mProcesses.valueAt(iproc);
                            if (!pkgMatch) {
                                if ((reqPackage.equals(proc.getName()) ^ 1) != 0) {
                                    iproc++;
                                }
                            }
                            if (!activeOnly || (proc.isInUse() ^ 1) == 0) {
                                procs.add(proc);
                                iproc++;
                            } else {
                                iproc++;
                            }
                        }
                        DumpUtils.dumpProcessSummaryLocked(pw, "      ", procs, ALL_SCREEN_ADJ, ALL_MEM_ADJ, NON_CACHED_PROC_STATES, now, totalTime);
                    }
                    int isvc = 0;
                    while (isvc < NSRVS) {
                        ServiceState svc = (ServiceState) pkgState.mServices.valueAt(isvc);
                        if (!pkgMatch) {
                            if ((reqPackage.equals(svc.getProcessName()) ^ 1) != 0) {
                                isvc++;
                            }
                        }
                        if (!activeOnly || (svc.isInUse() ^ 1) == 0) {
                            if (dumpAll) {
                                pw.print("      Service ");
                            } else {
                                pw.print("      * ");
                            }
                            pw.print((String) pkgState.mServices.keyAt(isvc));
                            pw.println(SettingsStringUtil.DELIMITER);
                            pw.print("        Process: ");
                            pw.println(svc.getProcessName());
                            svc.dumpStats(pw, "        ", "          ", "    ", now, totalTime, dumpSummary, dumpAll);
                            isvc++;
                        } else {
                            pw.print("      (Not active: ");
                            pw.print((String) pkgState.mServices.keyAt(isvc));
                            pw.println(")");
                            isvc++;
                        }
                    }
                }
            }
        }
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        printedHeader = false;
        int numShownProcs = 0;
        int numTotalProcs = 0;
        for (ip = 0; ip < procMap.size(); ip++) {
            String procName = (String) procMap.keyAt(ip);
            SparseArray<ProcessState> uids2 = (SparseArray) procMap.valueAt(ip);
            for (iu = 0; iu < uids2.size(); iu++) {
                uid = uids2.keyAt(iu);
                numTotalProcs++;
                proc = (ProcessState) uids2.valueAt(iu);
                if (!proc.hasAnyData() && proc.isMultiPackage()) {
                    if (!(reqPackage == null || (reqPackage.equals(procName) ^ 1) == 0)) {
                        if ((reqPackage.equals(proc.getPackage()) ^ 1) != 0) {
                        }
                    }
                    numShownProcs++;
                    if (sepNeeded) {
                        pw.println();
                    }
                    sepNeeded = true;
                    if (!printedHeader) {
                        pw.println("Multi-Package Common Processes:");
                        printedHeader = true;
                    }
                    if (!activeOnly || (proc.isInUse() ^ 1) == 0) {
                        pw.print("  * ");
                        pw.print(procName);
                        pw.print(" / ");
                        UserHandle.formatUid(pw, uid);
                        pw.print(" (");
                        pw.print(proc.getDurationsBucketCount());
                        pw.print(" entries)");
                        pw.println(SettingsStringUtil.DELIMITER);
                        proc.dumpProcessState(pw, "        ", ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES, now);
                        proc.dumpPss(pw, "        ", ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES);
                        proc.dumpInternalLocked(pw, "        ", dumpAll);
                    } else {
                        pw.print("      (Not active: ");
                        pw.print(procName);
                        pw.println(")");
                    }
                }
            }
        }
        if (dumpAll) {
            pw.println();
            pw.print("  Total procs: ");
            pw.print(numShownProcs);
            pw.print(" shown of ");
            pw.print(numTotalProcs);
            pw.println(" total");
        }
        if (sepNeeded) {
            pw.println();
        }
        if (dumpSummary) {
            pw.println("Summary:");
            dumpSummaryLocked(pw, reqPackage, now, activeOnly);
        } else {
            dumpTotalsLocked(pw, now);
        }
        if (dumpAll) {
            pw.println();
            pw.println("Internal state:");
            pw.print("  mRunning=");
            pw.println(this.mRunning);
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
            int[] sizes = (int[]) this.mPageTypeSizes.get(i);
            int N = sizes == null ? 0 : sizes.length;
            for (int j = 0; j < N; j++) {
                pw.format("%6d", new Object[]{Integer.valueOf(sizes[j])});
            }
            pw.println();
        }
    }

    long printMemoryCategory(PrintWriter pw, String prefix, String label, double memWeight, long totalTime, long curTotalMem, int samples) {
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

    void dumpTotalsLocked(PrintWriter pw, long now) {
        pw.println("Run time Stats:");
        DumpUtils.dumpSingleTime(pw, "  ", this.mMemFactorDurations, this.mMemFactor, this.mStartTime, now);
        pw.println();
        pw.println("Memory usage:");
        TotalMemoryUseCollection totalMemoryUseCollection = new TotalMemoryUseCollection(ALL_SCREEN_ADJ, ALL_MEM_ADJ);
        computeTotalMemoryUse(totalMemoryUseCollection, now);
        long totalPss = printMemoryCategory(pw, "  ", "Native ", totalMemoryUseCollection.sysMemNativeWeight, totalMemoryUseCollection.totalTime, printMemoryCategory(pw, "  ", "Kernel ", totalMemoryUseCollection.sysMemKernelWeight, totalMemoryUseCollection.totalTime, 0, totalMemoryUseCollection.sysMemSamples), totalMemoryUseCollection.sysMemSamples);
        for (int i = 0; i < 14; i++) {
            if (i != 7) {
                totalPss = printMemoryCategory(pw, "  ", DumpUtils.STATE_NAMES[i], totalMemoryUseCollection.processStateWeight[i], totalMemoryUseCollection.totalTime, totalPss, totalMemoryUseCollection.processStateSamples[i]);
            }
        }
        totalPss = printMemoryCategory(pw, "  ", "Z-Ram  ", totalMemoryUseCollection.sysMemZRamWeight, totalMemoryUseCollection.totalTime, printMemoryCategory(pw, "  ", "Free   ", totalMemoryUseCollection.sysMemFreeWeight, totalMemoryUseCollection.totalTime, printMemoryCategory(pw, "  ", "Cached ", totalMemoryUseCollection.sysMemCachedWeight, totalMemoryUseCollection.totalTime, totalPss, totalMemoryUseCollection.sysMemSamples), totalMemoryUseCollection.sysMemSamples), totalMemoryUseCollection.sysMemSamples);
        pw.print("  TOTAL  : ");
        DebugUtils.printSizeValue(pw, totalPss);
        pw.println();
        printMemoryCategory(pw, "  ", DumpUtils.STATE_NAMES[7], totalMemoryUseCollection.processStateWeight[7], totalMemoryUseCollection.totalTime, totalPss, totalMemoryUseCollection.processStateSamples[7]);
        pw.println();
        pw.print("          Start time: ");
        pw.print(DateFormat.format((CharSequence) "yyyy-MM-dd HH:mm:ss", this.mTimePeriodStartClock));
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

    void dumpFilteredSummaryLocked(PrintWriter pw, String header, String prefix, int[] screenStates, int[] memStates, int[] procStates, int[] sortProcStates, long now, long totalTime, String reqPackage, boolean activeOnly) {
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
        ProcessState proc;
        ArraySet<ProcessState> foundProcs = new ArraySet();
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        for (int ip = 0; ip < pkgMap.size(); ip++) {
            String pkgName = (String) pkgMap.keyAt(ip);
            SparseArray<SparseArray<PackageState>> procs = (SparseArray) pkgMap.valueAt(ip);
            for (int iu = 0; iu < procs.size(); iu++) {
                SparseArray<PackageState> vpkgs = (SparseArray) procs.valueAt(iu);
                int NVERS = vpkgs.size();
                for (int iv = 0; iv < NVERS; iv++) {
                    PackageState state = (PackageState) vpkgs.valueAt(iv);
                    int NPROCS = state.mProcesses.size();
                    boolean pkgMatch = reqPackage != null ? reqPackage.equals(pkgName) : true;
                    int iproc = 0;
                    while (iproc < NPROCS) {
                        proc = (ProcessState) state.mProcesses.valueAt(iproc);
                        if (!pkgMatch) {
                            if ((reqPackage.equals(proc.getName()) ^ 1) != 0) {
                                iproc++;
                            }
                        }
                        if (!activeOnly || (proc.isInUse() ^ 1) == 0) {
                            foundProcs.add(proc.getCommonProcess());
                            iproc++;
                        } else {
                            iproc++;
                        }
                    }
                }
            }
        }
        ArrayList<ProcessState> arrayList = new ArrayList(foundProcs.size());
        for (int i = 0; i < foundProcs.size(); i++) {
            proc = (ProcessState) foundProcs.valueAt(i);
            if (proc.computeProcessTimeLocked(screenStates, memStates, procStates, now) > 0) {
                arrayList.add(proc);
                if (procStates != sortProcStates) {
                    proc.computeProcessTimeLocked(screenStates, memStates, sortProcStates, now);
                }
            }
        }
        Collections.sort(arrayList, ProcessState.COMPARATOR);
        return arrayList;
    }

    public void dumpCheckinLocked(PrintWriter pw, String reqPackage) {
        long elapsedRealtime;
        int ip;
        int iu;
        int i;
        int j;
        long now = SystemClock.uptimeMillis();
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        pw.println("vers,5");
        pw.print("period,");
        pw.print(this.mTimePeriodStartClockStr);
        pw.print(",");
        pw.print(this.mTimePeriodStartRealtime);
        pw.print(",");
        if (this.mRunning) {
            elapsedRealtime = SystemClock.elapsedRealtime();
        } else {
            elapsedRealtime = this.mTimePeriodEndRealtime;
        }
        pw.print(elapsedRealtime);
        boolean partial = true;
        if ((this.mFlags & 2) != 0) {
            pw.print(",shutdown");
            partial = false;
        }
        if ((this.mFlags & 4) != 0) {
            pw.print(",sysprops");
            partial = false;
        }
        if ((this.mFlags & 1) != 0) {
            pw.print(",complete");
            partial = false;
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
        for (ip = 0; ip < pkgMap.size(); ip++) {
            String pkgName = (String) pkgMap.keyAt(ip);
            if (reqPackage == null || (reqPackage.equals(pkgName) ^ 1) == 0) {
                SparseArray<SparseArray<PackageState>> uids = (SparseArray) pkgMap.valueAt(ip);
                for (iu = 0; iu < uids.size(); iu++) {
                    int uid = uids.keyAt(iu);
                    SparseArray<PackageState> vpkgs = (SparseArray) uids.valueAt(iu);
                    for (int iv = 0; iv < vpkgs.size(); iv++) {
                        int vers = vpkgs.keyAt(iv);
                        PackageState pkgState = (PackageState) vpkgs.valueAt(iv);
                        int NPROCS = pkgState.mProcesses.size();
                        int NSRVS = pkgState.mServices.size();
                        for (int iproc = 0; iproc < NPROCS; iproc++) {
                            ((ProcessState) pkgState.mProcesses.valueAt(iproc)).dumpPackageProcCheckin(pw, pkgName, uid, vers, (String) pkgState.mProcesses.keyAt(iproc), now);
                        }
                        for (int isvc = 0; isvc < NSRVS; isvc++) {
                            ((ServiceState) pkgState.mServices.valueAt(isvc)).dumpTimesCheckin(pw, pkgName, uid, vers, DumpUtils.collapseString(pkgName, (String) pkgState.mServices.keyAt(isvc)), now);
                        }
                    }
                }
            }
        }
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        for (ip = 0; ip < procMap.size(); ip++) {
            String procName = (String) procMap.keyAt(ip);
            SparseArray<ProcessState> uids2 = (SparseArray) procMap.valueAt(ip);
            for (iu = 0; iu < uids2.size(); iu++) {
                ((ProcessState) uids2.valueAt(iu)).dumpProcCheckin(pw, procName, uids2.keyAt(iu), now);
            }
        }
        pw.print("total");
        DumpUtils.dumpAdjTimesCheckin(pw, ",", this.mMemFactorDurations, this.mMemFactor, this.mStartTime, now);
        pw.println();
        int sysMemUsageCount = this.mSysMemUsage.getKeyCount();
        if (sysMemUsageCount > 0) {
            pw.print("sysmemusage");
            for (i = 0; i < sysMemUsageCount; i++) {
                int key = this.mSysMemUsage.getKeyAt(i);
                int type = SparseMappingTable.getIdFromKey(key);
                pw.print(",");
                DumpUtils.printProcStateTag(pw, type);
                for (j = 0; j < 16; j++) {
                    if (j > 1) {
                        pw.print(SettingsStringUtil.DELIMITER);
                    }
                    pw.print(this.mSysMemUsage.getValue(key, j));
                }
            }
        }
        pw.println();
        TotalMemoryUseCollection totalMemoryUseCollection = new TotalMemoryUseCollection(ALL_SCREEN_ADJ, ALL_MEM_ADJ);
        computeTotalMemoryUse(totalMemoryUseCollection, now);
        pw.print("weights,");
        pw.print(totalMemoryUseCollection.totalTime);
        pw.print(",");
        pw.print(totalMemoryUseCollection.sysMemCachedWeight);
        pw.print(SettingsStringUtil.DELIMITER);
        pw.print(totalMemoryUseCollection.sysMemSamples);
        pw.print(",");
        pw.print(totalMemoryUseCollection.sysMemFreeWeight);
        pw.print(SettingsStringUtil.DELIMITER);
        pw.print(totalMemoryUseCollection.sysMemSamples);
        pw.print(",");
        pw.print(totalMemoryUseCollection.sysMemZRamWeight);
        pw.print(SettingsStringUtil.DELIMITER);
        pw.print(totalMemoryUseCollection.sysMemSamples);
        pw.print(",");
        pw.print(totalMemoryUseCollection.sysMemKernelWeight);
        pw.print(SettingsStringUtil.DELIMITER);
        pw.print(totalMemoryUseCollection.sysMemSamples);
        pw.print(",");
        pw.print(totalMemoryUseCollection.sysMemNativeWeight);
        pw.print(SettingsStringUtil.DELIMITER);
        pw.print(totalMemoryUseCollection.sysMemSamples);
        for (i = 0; i < 14; i++) {
            pw.print(",");
            pw.print(totalMemoryUseCollection.processStateWeight[i]);
            pw.print(SettingsStringUtil.DELIMITER);
            pw.print(totalMemoryUseCollection.processStateSamples[i]);
        }
        pw.println();
        int NPAGETYPES = this.mPageTypeLabels.size();
        for (i = 0; i < NPAGETYPES; i++) {
            pw.print("availablepages,");
            pw.print((String) this.mPageTypeLabels.get(i));
            pw.print(",");
            pw.print(this.mPageTypeZones.get(i));
            pw.print(",");
            int[] sizes = (int[]) this.mPageTypeSizes.get(i);
            int N = sizes == null ? 0 : sizes.length;
            for (j = 0; j < N; j++) {
                if (j != 0) {
                    pw.print(",");
                }
                pw.print(sizes[j]);
            }
            pw.println();
        }
    }
}
