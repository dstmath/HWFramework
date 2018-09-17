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
import android.text.format.DateFormat;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.DebugUtils;
import android.util.PtmLog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.app.ProcessMap;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.telephony.PhoneConstants;
import com.huawei.hwperformance.HwPerformance;
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
import javax.microedition.khronos.opengles.GL10;

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
    public static final int[] ALL_MEM_ADJ = null;
    public static final int[] ALL_PROC_STATES = null;
    public static final int[] ALL_SCREEN_ADJ = null;
    public static final int[] BACKGROUND_PROC_STATES = null;
    static final int[] BAD_TABLE = null;
    public static long COMMIT_PERIOD = 0;
    public static long COMMIT_UPTIME_PERIOD = 0;
    public static final Creator<ProcessStats> CREATOR = null;
    static final boolean DEBUG = false;
    static final boolean DEBUG_PARCEL = false;
    public static final int FLAG_COMPLETE = 1;
    public static final int FLAG_SHUTDOWN = 2;
    public static final int FLAG_SYSPROPS = 4;
    private static final int MAGIC = 1347638356;
    public static final int[] NON_CACHED_PROC_STATES = null;
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
    private static final Pattern sPageTypeRegex = null;
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
        public final ArrayMap<String, ProcessState> mProcesses;
        public final ArrayMap<String, ServiceState> mServices;
        public final int mUid;

        public PackageState(String packageName, int uid) {
            this.mProcesses = new ArrayMap();
            this.mServices = new ArrayMap();
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
        public long[] processStatePss;
        public int[] processStateSamples;
        public long[] processStateTime;
        public double[] processStateWeight;
        final int[] screenStates;
        public double sysMemCachedWeight;
        public double sysMemFreeWeight;
        public double sysMemKernelWeight;
        public double sysMemNativeWeight;
        public int sysMemSamples;
        public long[] sysMemUsage;
        public double sysMemZRamWeight;
        public long totalTime;

        public TotalMemoryUseCollection(int[] _screenStates, int[] _memStates) {
            this.processStatePss = new long[ProcessStats.SYS_MEM_USAGE_NATIVE_AVERAGE];
            this.processStateWeight = new double[ProcessStats.SYS_MEM_USAGE_NATIVE_AVERAGE];
            this.processStateTime = new long[ProcessStats.SYS_MEM_USAGE_NATIVE_AVERAGE];
            this.processStateSamples = new int[ProcessStats.SYS_MEM_USAGE_NATIVE_AVERAGE];
            this.sysMemUsage = new long[ProcessStats.SYS_MEM_USAGE_COUNT];
            this.screenStates = _screenStates;
            this.memStates = _memStates;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.procstats.ProcessStats.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.app.procstats.ProcessStats.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.procstats.ProcessStats.<clinit>():void");
    }

    private java.lang.String readCommonString(android.os.Parcel r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.procstats.ProcessStats.readCommonString(android.os.Parcel, int):java.lang.String
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.procstats.ProcessStats.readCommonString(android.os.Parcel, int):java.lang.String");
    }

    private void readCompactedLongArray(android.os.Parcel r1, int r2, long[] r3, int r4) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.procstats.ProcessStats.readCompactedLongArray(android.os.Parcel, int, long[], int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.procstats.ProcessStats.readCompactedLongArray(android.os.Parcel, int, long[], int):void");
    }

    private void writeCommonString(android.os.Parcel r1, java.lang.String r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.procstats.ProcessStats.writeCommonString(android.os.Parcel, java.lang.String):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.procstats.ProcessStats.writeCommonString(android.os.Parcel, java.lang.String):void");
    }

    private void writeCompactedLongArray(android.os.Parcel r1, long[] r2, int r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.procstats.ProcessStats.writeCompactedLongArray(android.os.Parcel, long[], int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.procstats.ProcessStats.writeCompactedLongArray(android.os.Parcel, long[], int):void");
    }

    public ProcessStats(boolean running) {
        this.mPackages = new ProcessMap();
        this.mProcesses = new ProcessMap();
        this.mMemFactorDurations = new long[SYS_MEM_USAGE_ZRAM_AVERAGE];
        this.mMemFactor = STATE_NOTHING;
        this.mTableData = new SparseMappingTable();
        this.mSysMemUsageArgs = new long[SYS_MEM_USAGE_COUNT];
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
        this.mMemFactorDurations = new long[SYS_MEM_USAGE_ZRAM_AVERAGE];
        this.mMemFactor = STATE_NOTHING;
        this.mTableData = new SparseMappingTable();
        this.mSysMemUsageArgs = new long[SYS_MEM_USAGE_COUNT];
        this.mSysMemUsage = new SysMemUsageTable(this.mTableData);
        this.mPageTypeZones = new ArrayList();
        this.mPageTypeLabels = new ArrayList();
        this.mPageTypeSizes = new ArrayList();
        reset();
        readFromParcel(in);
    }

    public void add(ProcessStats other) {
        int ip;
        ProcessState thisProc;
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pkgMap = other.mPackages.getMap();
        for (ip = SYS_MEM_USAGE_SAMPLE_COUNT; ip < pkgMap.size(); ip += SYS_MEM_USAGE_CACHED_MINIMUM) {
            int iu;
            String pkgName = (String) pkgMap.keyAt(ip);
            SparseArray<SparseArray<PackageState>> uids = (SparseArray) pkgMap.valueAt(ip);
            for (iu = SYS_MEM_USAGE_SAMPLE_COUNT; iu < uids.size(); iu += SYS_MEM_USAGE_CACHED_MINIMUM) {
                int uid = uids.keyAt(iu);
                SparseArray<PackageState> versions = (SparseArray) uids.valueAt(iu);
                for (int iv = SYS_MEM_USAGE_SAMPLE_COUNT; iv < versions.size(); iv += SYS_MEM_USAGE_CACHED_MINIMUM) {
                    int vers = versions.keyAt(iv);
                    PackageState otherState = (PackageState) versions.valueAt(iv);
                    int NPROCS = otherState.mProcesses.size();
                    int NSRVS = otherState.mServices.size();
                    for (int iproc = SYS_MEM_USAGE_SAMPLE_COUNT; iproc < NPROCS; iproc += SYS_MEM_USAGE_CACHED_MINIMUM) {
                        ProcessState otherProc = (ProcessState) otherState.mProcesses.valueAt(iproc);
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
                    for (int isvc = SYS_MEM_USAGE_SAMPLE_COUNT; isvc < NSRVS; isvc += SYS_MEM_USAGE_CACHED_MINIMUM) {
                        ServiceState otherSvc = (ServiceState) otherState.mServices.valueAt(isvc);
                        getServiceStateLocked(pkgName, uid, vers, otherSvc.getProcessName(), otherSvc.getName()).add(otherSvc);
                    }
                }
            }
        }
        ArrayMap<String, SparseArray<ProcessState>> procMap = other.mProcesses.getMap();
        for (ip = SYS_MEM_USAGE_SAMPLE_COUNT; ip < procMap.size(); ip += SYS_MEM_USAGE_CACHED_MINIMUM) {
            SparseArray<ProcessState> uids2 = (SparseArray) procMap.valueAt(ip);
            for (iu = SYS_MEM_USAGE_SAMPLE_COUNT; iu < uids2.size(); iu += SYS_MEM_USAGE_CACHED_MINIMUM) {
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
        for (int i = SYS_MEM_USAGE_SAMPLE_COUNT; i < SYS_MEM_USAGE_ZRAM_AVERAGE; i += SYS_MEM_USAGE_CACHED_MINIMUM) {
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
        if (this.mMemFactor != STATE_NOTHING) {
            int state = this.mMemFactor * SYS_MEM_USAGE_NATIVE_AVERAGE;
            this.mSysMemUsageArgs[SYS_MEM_USAGE_SAMPLE_COUNT] = 1;
            for (int i = SYS_MEM_USAGE_SAMPLE_COUNT; i < SYS_MEM_USAGE_CACHED_MAXIMUM; i += SYS_MEM_USAGE_CACHED_MINIMUM) {
                this.mSysMemUsageArgs[i + SYS_MEM_USAGE_CACHED_MINIMUM] = cachedMem;
                this.mSysMemUsageArgs[i + SYS_MEM_USAGE_FREE_MINIMUM] = freeMem;
                this.mSysMemUsageArgs[i + SYS_MEM_USAGE_ZRAM_MINIMUM] = zramMem;
                this.mSysMemUsageArgs[i + SYS_MEM_USAGE_KERNEL_MINIMUM] = kernelMem;
                this.mSysMemUsageArgs[i + SYS_MEM_USAGE_NATIVE_MINIMUM] = nativeMem;
            }
            this.mSysMemUsage.mergeStats(state, this.mSysMemUsageArgs, SYS_MEM_USAGE_SAMPLE_COUNT);
        }
    }

    public void computeTotalMemoryUse(TotalMemoryUseCollection data, long now) {
        int i;
        data.totalTime = 0;
        for (i = SYS_MEM_USAGE_SAMPLE_COUNT; i < SYS_MEM_USAGE_NATIVE_AVERAGE; i += SYS_MEM_USAGE_CACHED_MINIMUM) {
            data.processStateWeight[i] = 0.0d;
            data.processStatePss[i] = 0;
            data.processStateTime[i] = 0;
            data.processStateSamples[i] = SYS_MEM_USAGE_SAMPLE_COUNT;
        }
        for (i = SYS_MEM_USAGE_SAMPLE_COUNT; i < SYS_MEM_USAGE_COUNT; i += SYS_MEM_USAGE_CACHED_MINIMUM) {
            data.sysMemUsage[i] = 0;
        }
        data.sysMemCachedWeight = 0.0d;
        data.sysMemFreeWeight = 0.0d;
        data.sysMemZRamWeight = 0.0d;
        data.sysMemKernelWeight = 0.0d;
        data.sysMemNativeWeight = 0.0d;
        data.sysMemSamples = SYS_MEM_USAGE_SAMPLE_COUNT;
        long[] totalMemUsage = this.mSysMemUsage.getTotalMemUsage();
        int is = SYS_MEM_USAGE_SAMPLE_COUNT;
        while (true) {
            int length = data.screenStates.length;
            if (is >= r0) {
                break;
            }
            int im = SYS_MEM_USAGE_SAMPLE_COUNT;
            while (true) {
                length = data.memStates.length;
                if (im >= r0) {
                    break;
                }
                int memBucket = data.screenStates[is] + data.memStates[im];
                int stateBucket = memBucket * SYS_MEM_USAGE_NATIVE_AVERAGE;
                long memTime = this.mMemFactorDurations[memBucket];
                length = this.mMemFactor;
                if (r0 == memBucket) {
                    memTime += now - this.mStartTime;
                }
                data.totalTime += memTime;
                int sysKey = this.mSysMemUsage.getKey((byte) stateBucket);
                long[] longs = totalMemUsage;
                int idx = SYS_MEM_USAGE_SAMPLE_COUNT;
                if (sysKey != STATE_NOTHING) {
                    long[] tmpLongs = this.mSysMemUsage.getArrayForKey(sysKey);
                    int tmpIndex = SparseMappingTable.getIndexFromKey(sysKey);
                    if (tmpLongs[tmpIndex + SYS_MEM_USAGE_SAMPLE_COUNT] >= 3) {
                        SysMemUsageTable.mergeSysMemUsage(data.sysMemUsage, SYS_MEM_USAGE_SAMPLE_COUNT, totalMemUsage, SYS_MEM_USAGE_SAMPLE_COUNT);
                        longs = tmpLongs;
                        idx = tmpIndex;
                    }
                }
                data.sysMemCachedWeight += ((double) longs[idx + SYS_MEM_USAGE_CACHED_AVERAGE]) * ((double) memTime);
                data.sysMemFreeWeight += ((double) longs[idx + SYS_MEM_USAGE_FREE_AVERAGE]) * ((double) memTime);
                data.sysMemZRamWeight += ((double) longs[idx + SYS_MEM_USAGE_ZRAM_AVERAGE]) * ((double) memTime);
                data.sysMemKernelWeight += ((double) longs[idx + SYS_MEM_USAGE_KERNEL_AVERAGE]) * ((double) memTime);
                data.sysMemNativeWeight += ((double) longs[idx + SYS_MEM_USAGE_NATIVE_AVERAGE]) * ((double) memTime);
                data.sysMemSamples = (int) (((long) data.sysMemSamples) + longs[idx + SYS_MEM_USAGE_SAMPLE_COUNT]);
                im += SYS_MEM_USAGE_CACHED_MINIMUM;
            }
            is += SYS_MEM_USAGE_CACHED_MINIMUM;
        }
        data.hasSwappedOutPss = this.mHasSwappedOutPss;
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        for (int iproc = SYS_MEM_USAGE_SAMPLE_COUNT; iproc < procMap.size(); iproc += SYS_MEM_USAGE_CACHED_MINIMUM) {
            SparseArray<ProcessState> uids = (SparseArray) procMap.valueAt(iproc);
            for (int iu = SYS_MEM_USAGE_SAMPLE_COUNT; iu < uids.size(); iu += SYS_MEM_USAGE_CACHED_MINIMUM) {
                ((ProcessState) uids.valueAt(iu)).aggregatePss(data, now);
            }
        }
    }

    public void reset() {
        resetCommon();
        this.mPackages.getMap().clear();
        this.mProcesses.getMap().clear();
        this.mMemFactor = STATE_NOTHING;
        this.mStartTime = 0;
    }

    public void resetSafely() {
        int ip;
        resetCommon();
        long now = SystemClock.uptimeMillis();
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        for (ip = procMap.size() + STATE_NOTHING; ip >= 0; ip += STATE_NOTHING) {
            int iu;
            SparseArray<ProcessState> uids = (SparseArray) procMap.valueAt(ip);
            for (iu = uids.size() + STATE_NOTHING; iu >= 0; iu += STATE_NOTHING) {
                ((ProcessState) uids.valueAt(iu)).tmpNumInUse = SYS_MEM_USAGE_SAMPLE_COUNT;
            }
        }
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        for (ip = pkgMap.size() + STATE_NOTHING; ip >= 0; ip += STATE_NOTHING) {
            SparseArray<SparseArray<PackageState>> uids2 = (SparseArray) pkgMap.valueAt(ip);
            for (iu = uids2.size() + STATE_NOTHING; iu >= 0; iu += STATE_NOTHING) {
                SparseArray<PackageState> vpkgs = (SparseArray) uids2.valueAt(iu);
                for (int iv = vpkgs.size() + STATE_NOTHING; iv >= 0; iv += STATE_NOTHING) {
                    PackageState pkgState = (PackageState) vpkgs.valueAt(iv);
                    for (int iproc = pkgState.mProcesses.size() + STATE_NOTHING; iproc >= 0; iproc += STATE_NOTHING) {
                        ProcessState ps = (ProcessState) pkgState.mProcesses.valueAt(iproc);
                        if (ps.isInUse()) {
                            ps.resetSafely(now);
                            ProcessState commonProcess = ps.getCommonProcess();
                            commonProcess.tmpNumInUse += SYS_MEM_USAGE_CACHED_MINIMUM;
                            ps.getCommonProcess().tmpFoundSubProc = ps;
                        } else {
                            ((ProcessState) pkgState.mProcesses.valueAt(iproc)).makeDead();
                            pkgState.mProcesses.removeAt(iproc);
                        }
                    }
                    for (int isvc = pkgState.mServices.size() + STATE_NOTHING; isvc >= 0; isvc += STATE_NOTHING) {
                        ServiceState ss = (ServiceState) pkgState.mServices.valueAt(isvc);
                        if (ss.isInUse()) {
                            ss.resetSafely(now);
                        } else {
                            pkgState.mServices.removeAt(isvc);
                        }
                    }
                    if (pkgState.mProcesses.size() <= 0) {
                        if (pkgState.mServices.size() <= 0) {
                            vpkgs.removeAt(iv);
                        }
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
        for (ip = procMap.size() + STATE_NOTHING; ip >= 0; ip += STATE_NOTHING) {
            uids = (SparseArray) procMap.valueAt(ip);
            for (iu = uids.size() + STATE_NOTHING; iu >= 0; iu += STATE_NOTHING) {
                ps = (ProcessState) uids.valueAt(iu);
                if (ps.isInUse() || ps.tmpNumInUse > 0) {
                    if (!ps.isActive() && ps.isMultiPackage()) {
                        int i = ps.tmpNumInUse;
                        if (r0 == SYS_MEM_USAGE_CACHED_MINIMUM) {
                            ps = ps.tmpFoundSubProc;
                            ps.makeStandalone();
                            uids.setValueAt(iu, ps);
                        }
                    }
                    ps.resetSafely(now);
                } else {
                    ps.makeDead();
                    uids.removeAt(iu);
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
        this.mFlags = SYS_MEM_USAGE_SAMPLE_COUNT;
        evaluateSystemProperties(true);
        updateFragmentation();
    }

    public boolean evaluateSystemProperties(boolean update) {
        boolean changed = DEBUG_PARCEL;
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

    public void updateFragmentation() {
        Throwable th;
        BufferedReader bufferedReader = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/pagetypeinfo"));
            try {
                Matcher matcher = sPageTypeRegex.matcher("");
                this.mPageTypeZones.clear();
                this.mPageTypeLabels.clear();
                this.mPageTypeSizes.clear();
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    matcher.reset(line);
                    if (matcher.matches()) {
                        Integer zone = Integer.valueOf(matcher.group(SYS_MEM_USAGE_CACHED_MINIMUM), SYS_MEM_USAGE_KERNEL_MINIMUM);
                        if (zone != null) {
                            this.mPageTypeZones.add(zone);
                            this.mPageTypeLabels.add(matcher.group(SYS_MEM_USAGE_CACHED_AVERAGE));
                            this.mPageTypeSizes.add(splitAndParseNumbers(matcher.group(SYS_MEM_USAGE_CACHED_MAXIMUM)));
                        }
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
            } catch (IOException e2) {
                bufferedReader = reader;
            } catch (Throwable th2) {
                th = th2;
                bufferedReader = reader;
            }
        } catch (IOException e3) {
            try {
                this.mPageTypeZones.clear();
                this.mPageTypeLabels.clear();
                this.mPageTypeSizes.clear();
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e4) {
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e5) {
                    }
                }
                throw th;
            }
        }
    }

    private static int[] splitAndParseNumbers(String s) {
        int i;
        boolean digit = DEBUG_PARCEL;
        int count = SYS_MEM_USAGE_SAMPLE_COUNT;
        int N = s.length();
        for (i = SYS_MEM_USAGE_SAMPLE_COUNT; i < N; i += SYS_MEM_USAGE_CACHED_MINIMUM) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                digit = DEBUG_PARCEL;
            } else if (!digit) {
                digit = true;
                count += SYS_MEM_USAGE_CACHED_MINIMUM;
            }
        }
        int[] result = new int[count];
        int val = SYS_MEM_USAGE_SAMPLE_COUNT;
        i = SYS_MEM_USAGE_SAMPLE_COUNT;
        int p = SYS_MEM_USAGE_SAMPLE_COUNT;
        while (i < N) {
            int p2;
            c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                if (digit) {
                    val = (val * SYS_MEM_USAGE_KERNEL_MINIMUM) + (c - 48);
                } else {
                    digit = true;
                    val = c - 48;
                }
                p2 = p;
            } else if (digit) {
                digit = DEBUG_PARCEL;
                p2 = p + SYS_MEM_USAGE_CACHED_MINIMUM;
                result[p] = val;
            } else {
                p2 = p;
            }
            i += SYS_MEM_USAGE_CACHED_MINIMUM;
            p = p2;
        }
        if (count > 0) {
            result[count + STATE_NOTHING] = val;
        }
        return result;
    }

    public int describeContents() {
        return SYS_MEM_USAGE_SAMPLE_COUNT;
    }

    public void writeToParcel(Parcel out, int flags) {
        writeToParcel(out, SystemClock.uptimeMillis(), flags);
    }

    public void writeToParcel(Parcel out, long now, int flags) {
        int ip;
        int iu;
        int iproc;
        out.writeInt(MAGIC);
        out.writeInt(PARCEL_VERSION);
        out.writeInt(SYS_MEM_USAGE_NATIVE_AVERAGE);
        out.writeInt(SYS_MEM_USAGE_ZRAM_AVERAGE);
        out.writeInt(SYS_MEM_USAGE_ZRAM_MINIMUM);
        out.writeInt(SYS_MEM_USAGE_COUNT);
        out.writeInt(HwPerformance.PERF_EVENT_RAW_REQ);
        this.mCommonStringToIndex = new ArrayMap(this.mProcesses.size());
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        int NPROC = procMap.size();
        for (ip = SYS_MEM_USAGE_SAMPLE_COUNT; ip < NPROC; ip += SYS_MEM_USAGE_CACHED_MINIMUM) {
            SparseArray<ProcessState> uids = (SparseArray) procMap.valueAt(ip);
            int NUID = uids.size();
            for (iu = SYS_MEM_USAGE_SAMPLE_COUNT; iu < NUID; iu += SYS_MEM_USAGE_CACHED_MINIMUM) {
                ((ProcessState) uids.valueAt(iu)).commitStateTime(now);
            }
        }
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        int NPKG = pkgMap.size();
        for (ip = SYS_MEM_USAGE_SAMPLE_COUNT; ip < NPKG; ip += SYS_MEM_USAGE_CACHED_MINIMUM) {
            SparseArray<SparseArray<PackageState>> uids2 = (SparseArray) pkgMap.valueAt(ip);
            NUID = uids2.size();
            for (iu = SYS_MEM_USAGE_SAMPLE_COUNT; iu < NUID; iu += SYS_MEM_USAGE_CACHED_MINIMUM) {
                int iv;
                SparseArray<PackageState> vpkgs = (SparseArray) uids2.valueAt(iu);
                int NVERS = vpkgs.size();
                for (iv = SYS_MEM_USAGE_SAMPLE_COUNT; iv < NVERS; iv += SYS_MEM_USAGE_CACHED_MINIMUM) {
                    int isvc;
                    PackageState pkgState = (PackageState) vpkgs.valueAt(iv);
                    int NPROCS = pkgState.mProcesses.size();
                    for (iproc = SYS_MEM_USAGE_SAMPLE_COUNT; iproc < NPROCS; iproc += SYS_MEM_USAGE_CACHED_MINIMUM) {
                        ProcessState proc = (ProcessState) pkgState.mProcesses.valueAt(iproc);
                        if (proc.getCommonProcess() != proc) {
                            proc.commitStateTime(now);
                        }
                    }
                    int NSRVS = pkgState.mServices.size();
                    for (isvc = SYS_MEM_USAGE_SAMPLE_COUNT; isvc < NSRVS; isvc += SYS_MEM_USAGE_CACHED_MINIMUM) {
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
        out.writeInt(this.mHasSwappedOutPss ? SYS_MEM_USAGE_CACHED_MINIMUM : SYS_MEM_USAGE_SAMPLE_COUNT);
        out.writeInt(this.mFlags);
        this.mTableData.writeToParcel(out);
        int i = this.mMemFactor;
        if (r0 != STATE_NOTHING) {
            long[] jArr = this.mMemFactorDurations;
            int i2 = this.mMemFactor;
            jArr[i2] = jArr[i2] + (now - this.mStartTime);
            this.mStartTime = now;
        }
        writeCompactedLongArray(out, this.mMemFactorDurations, this.mMemFactorDurations.length);
        this.mSysMemUsage.writeToParcel(out);
        out.writeInt(NPROC);
        for (ip = SYS_MEM_USAGE_SAMPLE_COUNT; ip < NPROC; ip += SYS_MEM_USAGE_CACHED_MINIMUM) {
            writeCommonString(out, (String) procMap.keyAt(ip));
            uids = (SparseArray) procMap.valueAt(ip);
            NUID = uids.size();
            out.writeInt(NUID);
            for (iu = SYS_MEM_USAGE_SAMPLE_COUNT; iu < NUID; iu += SYS_MEM_USAGE_CACHED_MINIMUM) {
                out.writeInt(uids.keyAt(iu));
                proc = (ProcessState) uids.valueAt(iu);
                writeCommonString(out, proc.getPackage());
                out.writeInt(proc.getVersion());
                proc.writeToParcel(out, now);
            }
        }
        out.writeInt(NPKG);
        for (ip = SYS_MEM_USAGE_SAMPLE_COUNT; ip < NPKG; ip += SYS_MEM_USAGE_CACHED_MINIMUM) {
            writeCommonString(out, (String) pkgMap.keyAt(ip));
            uids2 = (SparseArray) pkgMap.valueAt(ip);
            NUID = uids2.size();
            out.writeInt(NUID);
            for (iu = SYS_MEM_USAGE_SAMPLE_COUNT; iu < NUID; iu += SYS_MEM_USAGE_CACHED_MINIMUM) {
                out.writeInt(uids2.keyAt(iu));
                vpkgs = (SparseArray) uids2.valueAt(iu);
                NVERS = vpkgs.size();
                out.writeInt(NVERS);
                for (iv = SYS_MEM_USAGE_SAMPLE_COUNT; iv < NVERS; iv += SYS_MEM_USAGE_CACHED_MINIMUM) {
                    out.writeInt(vpkgs.keyAt(iv));
                    pkgState = (PackageState) vpkgs.valueAt(iv);
                    NPROCS = pkgState.mProcesses.size();
                    out.writeInt(NPROCS);
                    for (iproc = SYS_MEM_USAGE_SAMPLE_COUNT; iproc < NPROCS; iproc += SYS_MEM_USAGE_CACHED_MINIMUM) {
                        writeCommonString(out, (String) pkgState.mProcesses.keyAt(iproc));
                        proc = (ProcessState) pkgState.mProcesses.valueAt(iproc);
                        if (proc.getCommonProcess() == proc) {
                            out.writeInt(SYS_MEM_USAGE_SAMPLE_COUNT);
                        } else {
                            out.writeInt(SYS_MEM_USAGE_CACHED_MINIMUM);
                            proc.writeToParcel(out, now);
                        }
                    }
                    NSRVS = pkgState.mServices.size();
                    out.writeInt(NSRVS);
                    for (isvc = SYS_MEM_USAGE_SAMPLE_COUNT; isvc < NSRVS; isvc += SYS_MEM_USAGE_CACHED_MINIMUM) {
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
        for (int i3 = SYS_MEM_USAGE_SAMPLE_COUNT; i3 < NPAGETYPES; i3 += SYS_MEM_USAGE_CACHED_MINIMUM) {
            out.writeInt(((Integer) this.mPageTypeZones.get(i3)).intValue());
            out.writeString((String) this.mPageTypeLabels.get(i3));
            out.writeIntArray((int[]) this.mPageTypeSizes.get(i3));
        }
        this.mCommonStringToIndex = null;
    }

    private boolean readCheckedInt(Parcel in, int val, String what) {
        int got = in.readInt();
        if (got == val) {
            return true;
        }
        this.mReadError = "bad " + what + ": " + got;
        return DEBUG_PARCEL;
    }

    static byte[] readFully(InputStream stream, int[] outLen) throws IOException {
        int pos = SYS_MEM_USAGE_SAMPLE_COUNT;
        int initialAvail = stream.available();
        byte[] data = new byte[(initialAvail > 0 ? initialAvail + SYS_MEM_USAGE_CACHED_MINIMUM : GL10.GL_LIGHT0)];
        while (true) {
            int amt = stream.read(data, pos, data.length - pos);
            if (amt < 0) {
                outLen[SYS_MEM_USAGE_SAMPLE_COUNT] = pos;
                return data;
            }
            pos += amt;
            if (pos >= data.length) {
                byte[] newData = new byte[(pos + GL10.GL_LIGHT0)];
                System.arraycopy(data, SYS_MEM_USAGE_SAMPLE_COUNT, newData, SYS_MEM_USAGE_SAMPLE_COUNT, pos);
                data = newData;
            }
        }
    }

    public void read(InputStream stream) {
        try {
            int[] len = new int[SYS_MEM_USAGE_CACHED_MINIMUM];
            byte[] raw = readFully(stream, len);
            Parcel in = Parcel.obtain();
            in.unmarshall(raw, SYS_MEM_USAGE_SAMPLE_COUNT, len[SYS_MEM_USAGE_SAMPLE_COUNT]);
            in.setDataPosition(SYS_MEM_USAGE_SAMPLE_COUNT);
            stream.close();
            readFromParcel(in);
        } catch (IOException e) {
            this.mReadError = "caught exception: " + e;
        }
    }

    public void readFromParcel(Parcel in) {
        boolean hadData = this.mPackages.getMap().size() <= 0 ? this.mProcesses.getMap().size() > 0 ? true : DEBUG_PARCEL : true;
        if (hadData) {
            resetSafely();
        }
        if (readCheckedInt(in, MAGIC, "magic number")) {
            int version = in.readInt();
            if (version != PARCEL_VERSION) {
                this.mReadError = "bad version: " + version;
                return;
            }
            if (readCheckedInt(in, SYS_MEM_USAGE_NATIVE_AVERAGE, "state count")) {
                if (readCheckedInt(in, SYS_MEM_USAGE_ZRAM_AVERAGE, "adj count")) {
                    if (readCheckedInt(in, SYS_MEM_USAGE_ZRAM_MINIMUM, "pss count")) {
                        if (readCheckedInt(in, SYS_MEM_USAGE_COUNT, "sys mem usage count")) {
                            if (readCheckedInt(in, HwPerformance.PERF_EVENT_RAW_REQ, "longs size")) {
                                this.mIndexToCommonString = new ArrayList();
                                this.mTimePeriodStartClock = in.readLong();
                                buildTimePeriodStartClockStr();
                                this.mTimePeriodStartRealtime = in.readLong();
                                this.mTimePeriodEndRealtime = in.readLong();
                                this.mTimePeriodStartUptime = in.readLong();
                                this.mTimePeriodEndUptime = in.readLong();
                                this.mRuntime = in.readString();
                                this.mHasSwappedOutPss = in.readInt() != 0 ? true : DEBUG_PARCEL;
                                this.mFlags = in.readInt();
                                this.mTableData.readFromParcel(in);
                                readCompactedLongArray(in, version, this.mMemFactorDurations, this.mMemFactorDurations.length);
                                if (this.mSysMemUsage.readFromParcel(in)) {
                                    String procName;
                                    int NUID;
                                    int uid;
                                    String pkgName;
                                    int vers;
                                    ProcessState proc;
                                    int NPROC = in.readInt();
                                    if (NPROC < 0) {
                                        this.mReadError = "bad process count: " + NPROC;
                                        return;
                                    }
                                    while (NPROC > 0) {
                                        NPROC += STATE_NOTHING;
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
                                            NUID += STATE_NOTHING;
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
                                            } else if (!proc.readFromParcel(in, DEBUG_PARCEL)) {
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
                                        NPKG += STATE_NOTHING;
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
                                            NUID += STATE_NOTHING;
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
                                                NVERS += STATE_NOTHING;
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
                                                    NPROCS += STATE_NOTHING;
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
                                                            proc = new ProcessState(commonProc, pkgName, uid, vers, procName, null);
                                                            if (!proc.readFromParcel(in, true)) {
                                                                return;
                                                            }
                                                        } else if (!proc.readFromParcel(in, DEBUG_PARCEL)) {
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
                                                    NSRVS += STATE_NOTHING;
                                                    String serviceName = in.readString();
                                                    if (serviceName == null) {
                                                        this.mReadError = "bad package service name";
                                                        return;
                                                    }
                                                    String readCommonString = version > SYS_MEM_USAGE_ZRAM_MAXIMUM ? readCommonString(in, version) : null;
                                                    ServiceState serv = hadData ? (ServiceState) packageState.mServices.get(serviceName) : null;
                                                    if (serv == null) {
                                                        serv = new ServiceState(this, pkgName, serviceName, readCommonString, null);
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
                                    for (int i = SYS_MEM_USAGE_SAMPLE_COUNT; i < NPAGETYPES; i += SYS_MEM_USAGE_CACHED_MINIMUM) {
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
                    for (int i = commonPkgState.mServices.size() + STATE_NOTHING; i >= 0; i += STATE_NOTHING) {
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
        long totalTime = DumpUtils.dumpSingleTime(null, null, this.mMemFactorDurations, this.mMemFactor, this.mStartTime, now);
        boolean sepNeeded = DEBUG_PARCEL;
        if (this.mSysMemUsage.getKeyCount() > 0) {
            pw.println("System memory usage:");
            this.mSysMemUsage.dump(pw, "  ", ALL_SCREEN_ADJ, ALL_MEM_ADJ);
            sepNeeded = true;
        }
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        boolean printedHeader = DEBUG_PARCEL;
        for (ip = SYS_MEM_USAGE_SAMPLE_COUNT; ip < pkgMap.size(); ip += SYS_MEM_USAGE_CACHED_MINIMUM) {
            String pkgName = (String) pkgMap.keyAt(ip);
            SparseArray<SparseArray<PackageState>> uids = (SparseArray) pkgMap.valueAt(ip);
            for (iu = SYS_MEM_USAGE_SAMPLE_COUNT; iu < uids.size(); iu += SYS_MEM_USAGE_CACHED_MINIMUM) {
                int uid = uids.keyAt(iu);
                SparseArray<PackageState> vpkgs = (SparseArray) uids.valueAt(iu);
                for (int iv = SYS_MEM_USAGE_SAMPLE_COUNT; iv < vpkgs.size(); iv += SYS_MEM_USAGE_CACHED_MINIMUM) {
                    int iproc;
                    ProcessState proc;
                    int vers = vpkgs.keyAt(iv);
                    PackageState pkgState = (PackageState) vpkgs.valueAt(iv);
                    int NPROCS = pkgState.mProcesses.size();
                    int NSRVS = pkgState.mServices.size();
                    boolean pkgMatch = reqPackage != null ? reqPackage.equals(pkgName) : true;
                    if (!pkgMatch) {
                        boolean procMatch = DEBUG_PARCEL;
                        for (iproc = SYS_MEM_USAGE_SAMPLE_COUNT; iproc < NPROCS; iproc += SYS_MEM_USAGE_CACHED_MINIMUM) {
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
                        pw.println(":");
                    }
                    if (!dumpSummary || dumpAll) {
                        iproc = SYS_MEM_USAGE_SAMPLE_COUNT;
                        while (iproc < NPROCS) {
                            proc = (ProcessState) pkgState.mProcesses.valueAt(iproc);
                            if (!pkgMatch) {
                                if (!reqPackage.equals(proc.getName())) {
                                    iproc += SYS_MEM_USAGE_CACHED_MINIMUM;
                                }
                            }
                            if (!activeOnly || proc.isInUse()) {
                                pw.print("      Process ");
                                pw.print((String) pkgState.mProcesses.keyAt(iproc));
                                if (proc.getCommonProcess().isMultiPackage()) {
                                    pw.print(" (multi, ");
                                } else {
                                    pw.print(" (unique, ");
                                }
                                pw.print(proc.getDurationsBucketCount());
                                pw.print(" entries)");
                                pw.println(":");
                                proc.dumpProcessState(pw, "        ", ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES, now);
                                proc.dumpPss(pw, "        ", ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES);
                                proc.dumpInternalLocked(pw, "        ", dumpAll);
                                iproc += SYS_MEM_USAGE_CACHED_MINIMUM;
                            } else {
                                pw.print("      (Not active: ");
                                pw.print((String) pkgState.mProcesses.keyAt(iproc));
                                pw.println(")");
                                iproc += SYS_MEM_USAGE_CACHED_MINIMUM;
                            }
                        }
                    } else {
                        ArrayList<ProcessState> procs = new ArrayList();
                        iproc = SYS_MEM_USAGE_SAMPLE_COUNT;
                        while (iproc < NPROCS) {
                            proc = (ProcessState) pkgState.mProcesses.valueAt(iproc);
                            if (!pkgMatch) {
                                if (!reqPackage.equals(proc.getName())) {
                                    iproc += SYS_MEM_USAGE_CACHED_MINIMUM;
                                }
                            }
                            if (!activeOnly || proc.isInUse()) {
                                procs.add(proc);
                                iproc += SYS_MEM_USAGE_CACHED_MINIMUM;
                            } else {
                                iproc += SYS_MEM_USAGE_CACHED_MINIMUM;
                            }
                        }
                        DumpUtils.dumpProcessSummaryLocked(pw, "      ", procs, ALL_SCREEN_ADJ, ALL_MEM_ADJ, NON_CACHED_PROC_STATES, now, totalTime);
                    }
                    int isvc = SYS_MEM_USAGE_SAMPLE_COUNT;
                    while (isvc < NSRVS) {
                        ServiceState svc = (ServiceState) pkgState.mServices.valueAt(isvc);
                        if (!pkgMatch) {
                            if (!reqPackage.equals(svc.getProcessName())) {
                                isvc += SYS_MEM_USAGE_CACHED_MINIMUM;
                            }
                        }
                        if (!activeOnly || svc.isInUse()) {
                            if (dumpAll) {
                                pw.print("      Service ");
                            } else {
                                pw.print("      * ");
                            }
                            pw.print((String) pkgState.mServices.keyAt(isvc));
                            pw.println(":");
                            pw.print("        Process: ");
                            pw.println(svc.getProcessName());
                            svc.dumpStats(pw, "        ", "          ", "    ", now, totalTime, dumpSummary, dumpAll);
                            isvc += SYS_MEM_USAGE_CACHED_MINIMUM;
                        } else {
                            pw.print("      (Not active: ");
                            pw.print((String) pkgState.mServices.keyAt(isvc));
                            pw.println(")");
                            isvc += SYS_MEM_USAGE_CACHED_MINIMUM;
                        }
                    }
                }
            }
        }
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        printedHeader = DEBUG_PARCEL;
        int numShownProcs = SYS_MEM_USAGE_SAMPLE_COUNT;
        int numTotalProcs = SYS_MEM_USAGE_SAMPLE_COUNT;
        for (ip = SYS_MEM_USAGE_SAMPLE_COUNT; ip < procMap.size(); ip += SYS_MEM_USAGE_CACHED_MINIMUM) {
            String procName = (String) procMap.keyAt(ip);
            SparseArray<ProcessState> uids2 = (SparseArray) procMap.valueAt(ip);
            for (iu = SYS_MEM_USAGE_SAMPLE_COUNT; iu < uids2.size(); iu += SYS_MEM_USAGE_CACHED_MINIMUM) {
                uid = uids2.keyAt(iu);
                numTotalProcs += SYS_MEM_USAGE_CACHED_MINIMUM;
                proc = (ProcessState) uids2.valueAt(iu);
                if (!proc.hasAnyData() && proc.isMultiPackage()) {
                    if (!(reqPackage == null || reqPackage.equals(procName))) {
                        if (reqPackage.equals(proc.getPackage())) {
                        }
                    }
                    numShownProcs += SYS_MEM_USAGE_CACHED_MINIMUM;
                    if (sepNeeded) {
                        pw.println();
                    }
                    sepNeeded = true;
                    if (!printedHeader) {
                        pw.println("Multi-Package Common Processes:");
                        printedHeader = true;
                    }
                    if (!activeOnly || proc.isInUse()) {
                        pw.print("  * ");
                        pw.print(procName);
                        pw.print(" / ");
                        UserHandle.formatUid(pw, uid);
                        pw.print(" (");
                        pw.print(proc.getDurationsBucketCount());
                        pw.print(" entries)");
                        pw.println(":");
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
        for (int i = SYS_MEM_USAGE_SAMPLE_COUNT; i < NPAGETYPES; i += SYS_MEM_USAGE_CACHED_MINIMUM) {
            Object[] objArr = new Object[SYS_MEM_USAGE_CACHED_AVERAGE];
            objArr[SYS_MEM_USAGE_SAMPLE_COUNT] = this.mPageTypeZones.get(i);
            objArr[SYS_MEM_USAGE_CACHED_MINIMUM] = this.mPageTypeLabels.get(i);
            pw.format("Zone %3d  %14s ", objArr);
            int[] sizes = (int[]) this.mPageTypeSizes.get(i);
            int N = sizes == null ? SYS_MEM_USAGE_SAMPLE_COUNT : sizes.length;
            for (int j = SYS_MEM_USAGE_SAMPLE_COUNT; j < N; j += SYS_MEM_USAGE_CACHED_MINIMUM) {
                objArr = new Object[SYS_MEM_USAGE_CACHED_MINIMUM];
                objArr[SYS_MEM_USAGE_SAMPLE_COUNT] = Integer.valueOf(sizes[j]);
                pw.format("%6d", objArr);
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
        for (int i = SYS_MEM_USAGE_SAMPLE_COUNT; i < SYS_MEM_USAGE_NATIVE_AVERAGE; i += SYS_MEM_USAGE_CACHED_MINIMUM) {
            if (i != SYS_MEM_USAGE_ZRAM_MINIMUM) {
                totalPss = printMemoryCategory(pw, "  ", DumpUtils.STATE_NAMES[i], totalMemoryUseCollection.processStateWeight[i], totalMemoryUseCollection.totalTime, totalPss, totalMemoryUseCollection.processStateSamples[i]);
            }
        }
        totalPss = printMemoryCategory(pw, "  ", "Z-Ram  ", totalMemoryUseCollection.sysMemZRamWeight, totalMemoryUseCollection.totalTime, printMemoryCategory(pw, "  ", "Free   ", totalMemoryUseCollection.sysMemFreeWeight, totalMemoryUseCollection.totalTime, printMemoryCategory(pw, "  ", "Cached ", totalMemoryUseCollection.sysMemCachedWeight, totalMemoryUseCollection.totalTime, totalPss, totalMemoryUseCollection.sysMemSamples), totalMemoryUseCollection.sysMemSamples), totalMemoryUseCollection.sysMemSamples);
        pw.print("  TOTAL  : ");
        DebugUtils.printSizeValue(pw, totalPss);
        pw.println();
        printMemoryCategory(pw, "  ", DumpUtils.STATE_NAMES[SYS_MEM_USAGE_ZRAM_MINIMUM], totalMemoryUseCollection.processStateWeight[SYS_MEM_USAGE_ZRAM_MINIMUM], totalMemoryUseCollection.totalTime, totalPss, totalMemoryUseCollection.processStateSamples[SYS_MEM_USAGE_ZRAM_MINIMUM]);
        pw.println();
        pw.print("          Start time: ");
        pw.print(DateFormat.format((CharSequence) "yyyy-MM-dd HH:mm:ss", this.mTimePeriodStartClock));
        pw.println();
        pw.print("  Total elapsed time: ");
        TimeUtils.formatDuration((this.mRunning ? SystemClock.elapsedRealtime() : this.mTimePeriodEndRealtime) - this.mTimePeriodStartRealtime, pw);
        boolean partial = true;
        if ((this.mFlags & SYS_MEM_USAGE_CACHED_AVERAGE) != 0) {
            pw.print(" (shutdown)");
            partial = DEBUG_PARCEL;
        }
        if ((this.mFlags & SYS_MEM_USAGE_FREE_MINIMUM) != 0) {
            pw.print(" (sysprops)");
            partial = DEBUG_PARCEL;
        }
        if ((this.mFlags & SYS_MEM_USAGE_CACHED_MINIMUM) != 0) {
            pw.print(" (complete)");
            partial = DEBUG_PARCEL;
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
        ArraySet<ProcessState> foundProcs = new ArraySet();
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        for (int ip = SYS_MEM_USAGE_SAMPLE_COUNT; ip < pkgMap.size(); ip += SYS_MEM_USAGE_CACHED_MINIMUM) {
            String pkgName = (String) pkgMap.keyAt(ip);
            SparseArray<SparseArray<PackageState>> procs = (SparseArray) pkgMap.valueAt(ip);
            for (int iu = SYS_MEM_USAGE_SAMPLE_COUNT; iu < procs.size(); iu += SYS_MEM_USAGE_CACHED_MINIMUM) {
                SparseArray<PackageState> vpkgs = (SparseArray) procs.valueAt(iu);
                int NVERS = vpkgs.size();
                for (int iv = SYS_MEM_USAGE_SAMPLE_COUNT; iv < NVERS; iv += SYS_MEM_USAGE_CACHED_MINIMUM) {
                    PackageState state = (PackageState) vpkgs.valueAt(iv);
                    int NPROCS = state.mProcesses.size();
                    boolean equals = reqPackage != null ? reqPackage.equals(pkgName) : true;
                    int iproc = SYS_MEM_USAGE_SAMPLE_COUNT;
                    while (iproc < NPROCS) {
                        ProcessState proc = (ProcessState) state.mProcesses.valueAt(iproc);
                        if (!equals) {
                            if (!reqPackage.equals(proc.getName())) {
                                iproc += SYS_MEM_USAGE_CACHED_MINIMUM;
                            }
                        }
                        if (!activeOnly || proc.isInUse()) {
                            foundProcs.add(proc.getCommonProcess());
                            iproc += SYS_MEM_USAGE_CACHED_MINIMUM;
                        } else {
                            iproc += SYS_MEM_USAGE_CACHED_MINIMUM;
                        }
                    }
                }
            }
        }
        ArrayList<ProcessState> arrayList = new ArrayList(foundProcs.size());
        for (int i = SYS_MEM_USAGE_SAMPLE_COUNT; i < foundProcs.size(); i += SYS_MEM_USAGE_CACHED_MINIMUM) {
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
        int i;
        int j;
        long now = SystemClock.uptimeMillis();
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        pw.println("vers,5");
        pw.print("period,");
        pw.print(this.mTimePeriodStartClockStr);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(this.mTimePeriodStartRealtime);
        pw.print(PtmLog.PAIRE_DELIMETER);
        if (this.mRunning) {
            elapsedRealtime = SystemClock.elapsedRealtime();
        } else {
            elapsedRealtime = this.mTimePeriodEndRealtime;
        }
        pw.print(elapsedRealtime);
        boolean partial = true;
        if ((this.mFlags & SYS_MEM_USAGE_CACHED_AVERAGE) != 0) {
            pw.print(",shutdown");
            partial = DEBUG_PARCEL;
        }
        if ((this.mFlags & SYS_MEM_USAGE_FREE_MINIMUM) != 0) {
            pw.print(",sysprops");
            partial = DEBUG_PARCEL;
        }
        if ((this.mFlags & SYS_MEM_USAGE_CACHED_MINIMUM) != 0) {
            pw.print(",complete");
            partial = DEBUG_PARCEL;
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
        for (ip = SYS_MEM_USAGE_SAMPLE_COUNT; ip < pkgMap.size(); ip += SYS_MEM_USAGE_CACHED_MINIMUM) {
            int iu;
            String pkgName = (String) pkgMap.keyAt(ip);
            if (reqPackage == null || reqPackage.equals(pkgName)) {
                SparseArray<SparseArray<PackageState>> uids = (SparseArray) pkgMap.valueAt(ip);
                for (iu = SYS_MEM_USAGE_SAMPLE_COUNT; iu < uids.size(); iu += SYS_MEM_USAGE_CACHED_MINIMUM) {
                    int uid = uids.keyAt(iu);
                    SparseArray<PackageState> vpkgs = (SparseArray) uids.valueAt(iu);
                    for (int iv = SYS_MEM_USAGE_SAMPLE_COUNT; iv < vpkgs.size(); iv += SYS_MEM_USAGE_CACHED_MINIMUM) {
                        int vers = vpkgs.keyAt(iv);
                        PackageState pkgState = (PackageState) vpkgs.valueAt(iv);
                        int NPROCS = pkgState.mProcesses.size();
                        int NSRVS = pkgState.mServices.size();
                        for (int iproc = SYS_MEM_USAGE_SAMPLE_COUNT; iproc < NPROCS; iproc += SYS_MEM_USAGE_CACHED_MINIMUM) {
                            ((ProcessState) pkgState.mProcesses.valueAt(iproc)).dumpPackageProcCheckin(pw, pkgName, uid, vers, (String) pkgState.mProcesses.keyAt(iproc), now);
                        }
                        for (int isvc = SYS_MEM_USAGE_SAMPLE_COUNT; isvc < NSRVS; isvc += SYS_MEM_USAGE_CACHED_MINIMUM) {
                            ((ServiceState) pkgState.mServices.valueAt(isvc)).dumpTimesCheckin(pw, pkgName, uid, vers, DumpUtils.collapseString(pkgName, (String) pkgState.mServices.keyAt(isvc)), now);
                        }
                    }
                }
            }
        }
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        for (ip = SYS_MEM_USAGE_SAMPLE_COUNT; ip < procMap.size(); ip += SYS_MEM_USAGE_CACHED_MINIMUM) {
            String procName = (String) procMap.keyAt(ip);
            SparseArray<ProcessState> uids2 = (SparseArray) procMap.valueAt(ip);
            for (iu = SYS_MEM_USAGE_SAMPLE_COUNT; iu < uids2.size(); iu += SYS_MEM_USAGE_CACHED_MINIMUM) {
                ((ProcessState) uids2.valueAt(iu)).dumpProcCheckin(pw, procName, uids2.keyAt(iu), now);
            }
        }
        pw.print("total");
        DumpUtils.dumpAdjTimesCheckin(pw, PtmLog.PAIRE_DELIMETER, this.mMemFactorDurations, this.mMemFactor, this.mStartTime, now);
        pw.println();
        int sysMemUsageCount = this.mSysMemUsage.getKeyCount();
        if (sysMemUsageCount > 0) {
            pw.print("sysmemusage");
            for (i = SYS_MEM_USAGE_SAMPLE_COUNT; i < sysMemUsageCount; i += SYS_MEM_USAGE_CACHED_MINIMUM) {
                int key = this.mSysMemUsage.getKeyAt(i);
                int type = SparseMappingTable.getIdFromKey(key);
                pw.print(PtmLog.PAIRE_DELIMETER);
                DumpUtils.printProcStateTag(pw, type);
                for (j = SYS_MEM_USAGE_SAMPLE_COUNT; j < SYS_MEM_USAGE_COUNT; j += SYS_MEM_USAGE_CACHED_MINIMUM) {
                    if (j > SYS_MEM_USAGE_CACHED_MINIMUM) {
                        pw.print(":");
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
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(totalMemoryUseCollection.sysMemCachedWeight);
        pw.print(":");
        pw.print(totalMemoryUseCollection.sysMemSamples);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(totalMemoryUseCollection.sysMemFreeWeight);
        pw.print(":");
        pw.print(totalMemoryUseCollection.sysMemSamples);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(totalMemoryUseCollection.sysMemZRamWeight);
        pw.print(":");
        pw.print(totalMemoryUseCollection.sysMemSamples);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(totalMemoryUseCollection.sysMemKernelWeight);
        pw.print(":");
        pw.print(totalMemoryUseCollection.sysMemSamples);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(totalMemoryUseCollection.sysMemNativeWeight);
        pw.print(":");
        pw.print(totalMemoryUseCollection.sysMemSamples);
        for (i = SYS_MEM_USAGE_SAMPLE_COUNT; i < SYS_MEM_USAGE_NATIVE_AVERAGE; i += SYS_MEM_USAGE_CACHED_MINIMUM) {
            pw.print(PtmLog.PAIRE_DELIMETER);
            pw.print(totalMemoryUseCollection.processStateWeight[i]);
            pw.print(":");
            pw.print(totalMemoryUseCollection.processStateSamples[i]);
        }
        pw.println();
        int NPAGETYPES = this.mPageTypeLabels.size();
        for (i = SYS_MEM_USAGE_SAMPLE_COUNT; i < NPAGETYPES; i += SYS_MEM_USAGE_CACHED_MINIMUM) {
            pw.print("availablepages,");
            pw.print((String) this.mPageTypeLabels.get(i));
            pw.print(PtmLog.PAIRE_DELIMETER);
            pw.print(this.mPageTypeZones.get(i));
            pw.print(PtmLog.PAIRE_DELIMETER);
            int[] sizes = (int[]) this.mPageTypeSizes.get(i);
            int N = sizes == null ? SYS_MEM_USAGE_SAMPLE_COUNT : sizes.length;
            for (j = SYS_MEM_USAGE_SAMPLE_COUNT; j < N; j += SYS_MEM_USAGE_CACHED_MINIMUM) {
                if (j != 0) {
                    pw.print(PtmLog.PAIRE_DELIMETER);
                }
                pw.print(sizes[j]);
            }
            pw.println();
        }
    }
}
