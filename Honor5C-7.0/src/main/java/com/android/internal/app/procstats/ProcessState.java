package com.android.internal.app.procstats;

import android.os.Parcel;
import android.os.SystemClock;
import android.os.UserHandle;
import android.telephony.PhoneNumberUtils;
import android.util.ArrayMap;
import android.util.DebugUtils;
import android.util.Log;
import android.util.PtmLog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.app.procstats.ProcessStats.PackageState;
import com.android.internal.app.procstats.ProcessStats.ProcessDataCollection;
import com.android.internal.app.procstats.ProcessStats.ProcessStateHolder;
import com.android.internal.app.procstats.ProcessStats.TotalMemoryUseCollection;
import com.android.internal.content.NativeLibraryHelper;
import com.huawei.pgmng.log.LogPower;
import java.io.PrintWriter;
import java.util.Comparator;

public final class ProcessState {
    public static final Comparator<ProcessState> COMPARATOR = null;
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_PARCEL = false;
    private static final int[] PROCESS_STATE_TO_STATE = null;
    private static final String TAG = "ProcessStats";
    private boolean mActive;
    private long mAvgCachedKillPss;
    private ProcessState mCommonProcess;
    private int mCurState;
    private boolean mDead;
    private final DurationsTable mDurations;
    private int mLastPssState;
    private long mLastPssTime;
    private long mMaxCachedKillPss;
    private long mMinCachedKillPss;
    private boolean mMultiPackage;
    private final String mName;
    private int mNumActiveServices;
    private int mNumCachedKill;
    private int mNumExcessiveCpu;
    private int mNumExcessiveWake;
    private int mNumStartedServices;
    private final String mPackage;
    private final PssTable mPssTable;
    private long mStartTime;
    private final ProcessStats mStats;
    private long mTmpTotalTime;
    private final int mUid;
    private final int mVersion;
    public ProcessState tmpFoundSubProc;
    public int tmpNumInUse;

    static class PssAggr {
        long pss;
        long samples;

        PssAggr() {
            this.pss = 0;
            this.samples = 0;
        }

        void add(long newPss, long newSamples) {
            this.pss = ((long) ((((double) this.pss) * ((double) this.samples)) + (((double) newPss) * ((double) newSamples)))) / (this.samples + newSamples);
            this.samples += newSamples;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.procstats.ProcessState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.app.procstats.ProcessState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.procstats.ProcessState.<clinit>():void");
    }

    public ProcessState(ProcessStats processStats, String pkg, int uid, int vers, String name) {
        this.mCurState = -1;
        this.mLastPssState = -1;
        this.mStats = processStats;
        this.mName = name;
        this.mCommonProcess = this;
        this.mPackage = pkg;
        this.mUid = uid;
        this.mVersion = vers;
        this.mDurations = new DurationsTable(processStats.mTableData);
        this.mPssTable = new PssTable(processStats.mTableData);
    }

    public ProcessState(ProcessState commonProcess, String pkg, int uid, int vers, String name, long now) {
        this.mCurState = -1;
        this.mLastPssState = -1;
        this.mStats = commonProcess.mStats;
        this.mName = name;
        this.mCommonProcess = commonProcess;
        this.mPackage = pkg;
        this.mUid = uid;
        this.mVersion = vers;
        this.mCurState = commonProcess.mCurState;
        this.mStartTime = now;
        this.mDurations = new DurationsTable(commonProcess.mStats.mTableData);
        this.mPssTable = new PssTable(commonProcess.mStats.mTableData);
    }

    public ProcessState clone(long now) {
        ProcessState pnew = new ProcessState(this, this.mPackage, this.mUid, this.mVersion, this.mName, now);
        pnew.mDurations.addDurations(this.mDurations);
        pnew.mPssTable.copyFrom(this.mPssTable, 7);
        pnew.mNumExcessiveWake = this.mNumExcessiveWake;
        pnew.mNumExcessiveCpu = this.mNumExcessiveCpu;
        pnew.mNumCachedKill = this.mNumCachedKill;
        pnew.mMinCachedKillPss = this.mMinCachedKillPss;
        pnew.mAvgCachedKillPss = this.mAvgCachedKillPss;
        pnew.mMaxCachedKillPss = this.mMaxCachedKillPss;
        pnew.mActive = this.mActive;
        pnew.mNumActiveServices = this.mNumActiveServices;
        pnew.mNumStartedServices = this.mNumStartedServices;
        return pnew;
    }

    public String getName() {
        return this.mName;
    }

    public ProcessState getCommonProcess() {
        return this.mCommonProcess;
    }

    public void makeStandalone() {
        this.mCommonProcess = this;
    }

    public String getPackage() {
        return this.mPackage;
    }

    public int getUid() {
        return this.mUid;
    }

    public int getVersion() {
        return this.mVersion;
    }

    public boolean isMultiPackage() {
        return this.mMultiPackage;
    }

    public void setMultiPackage(boolean val) {
        this.mMultiPackage = val;
    }

    public int getDurationsBucketCount() {
        return this.mDurations.getKeyCount();
    }

    public void add(ProcessState other) {
        this.mDurations.addDurations(other.mDurations);
        this.mPssTable.mergeStats(other.mPssTable);
        this.mNumExcessiveWake += other.mNumExcessiveWake;
        this.mNumExcessiveCpu += other.mNumExcessiveCpu;
        if (other.mNumCachedKill > 0) {
            addCachedKill(other.mNumCachedKill, other.mMinCachedKillPss, other.mAvgCachedKillPss, other.mMaxCachedKillPss);
        }
    }

    public void resetSafely(long now) {
        this.mDurations.resetTable();
        this.mPssTable.resetTable();
        this.mStartTime = now;
        this.mLastPssState = -1;
        this.mLastPssTime = 0;
        this.mNumExcessiveWake = 0;
        this.mNumExcessiveCpu = 0;
        this.mNumCachedKill = 0;
        this.mMaxCachedKillPss = 0;
        this.mAvgCachedKillPss = 0;
        this.mMinCachedKillPss = 0;
    }

    public void makeDead() {
        this.mDead = true;
    }

    private void ensureNotDead() {
        if (this.mDead) {
            Slog.w(TAG, "ProcessState dead: name=" + this.mName + " pkg=" + this.mPackage + " uid=" + this.mUid + " common.name=" + this.mCommonProcess.mName);
        }
    }

    public void writeToParcel(Parcel out, long now) {
        int i = 0;
        if (this.mMultiPackage) {
            i = 1;
        }
        out.writeInt(i);
        this.mDurations.writeToParcel(out);
        this.mPssTable.writeToParcel(out);
        out.writeInt(this.mNumExcessiveWake);
        out.writeInt(this.mNumExcessiveCpu);
        out.writeInt(this.mNumCachedKill);
        if (this.mNumCachedKill > 0) {
            out.writeLong(this.mMinCachedKillPss);
            out.writeLong(this.mAvgCachedKillPss);
            out.writeLong(this.mMaxCachedKillPss);
        }
    }

    public boolean readFromParcel(Parcel in, boolean fully) {
        boolean multiPackage = in.readInt() != 0 ? true : DEBUG_PARCEL;
        if (fully) {
            this.mMultiPackage = multiPackage;
        }
        if (!this.mDurations.readFromParcel(in) || !this.mPssTable.readFromParcel(in)) {
            return DEBUG_PARCEL;
        }
        this.mNumExcessiveWake = in.readInt();
        this.mNumExcessiveCpu = in.readInt();
        this.mNumCachedKill = in.readInt();
        if (this.mNumCachedKill > 0) {
            this.mMinCachedKillPss = in.readLong();
            this.mAvgCachedKillPss = in.readLong();
            this.mMaxCachedKillPss = in.readLong();
        } else {
            this.mMaxCachedKillPss = 0;
            this.mAvgCachedKillPss = 0;
            this.mMinCachedKillPss = 0;
        }
        return true;
    }

    public void makeActive() {
        ensureNotDead();
        this.mActive = true;
    }

    public void makeInactive() {
        this.mActive = DEBUG_PARCEL;
    }

    public boolean isInUse() {
        if (this.mActive || this.mNumActiveServices > 0 || this.mNumStartedServices > 0 || this.mCurState != -1) {
            return true;
        }
        return DEBUG_PARCEL;
    }

    public boolean isActive() {
        return this.mActive;
    }

    public boolean hasAnyData() {
        if (this.mDurations.getKeyCount() == 0 && this.mCurState == -1 && this.mPssTable.getKeyCount() == 0) {
            return DEBUG_PARCEL;
        }
        return true;
    }

    public void setState(int state, int memFactor, long now, ArrayMap<String, ProcessStateHolder> pkgList) {
        state = state < 0 ? this.mNumStartedServices > 0 ? (memFactor * 14) + 7 : -1 : PROCESS_STATE_TO_STATE[state] + (memFactor * 14);
        this.mCommonProcess.setState(state, now);
        if (this.mCommonProcess.mMultiPackage && pkgList != null) {
            for (int ip = pkgList.size() - 1; ip >= 0; ip--) {
                pullFixedProc(pkgList, ip).setState(state, now);
            }
        }
    }

    public void setState(int state, long now) {
        ensureNotDead();
        if (this.mCurState != state) {
            commitStateTime(now);
            this.mCurState = state;
        }
    }

    public void commitStateTime(long now) {
        if (this.mCurState != -1) {
            long dur = now - this.mStartTime;
            if (dur > 0) {
                this.mDurations.addDuration(this.mCurState, dur);
            }
        }
        this.mStartTime = now;
    }

    public void incActiveServices(String serviceName) {
        if (this.mCommonProcess != this) {
            this.mCommonProcess.incActiveServices(serviceName);
        }
        this.mNumActiveServices++;
    }

    public void decActiveServices(String serviceName) {
        if (this.mCommonProcess != this) {
            this.mCommonProcess.decActiveServices(serviceName);
        }
        this.mNumActiveServices--;
        if (this.mNumActiveServices < 0) {
            Slog.wtfStack(TAG, "Proc active services underrun: pkg=" + this.mPackage + " uid=" + this.mUid + " proc=" + this.mName + " service=" + serviceName);
            this.mNumActiveServices = 0;
        }
    }

    public void incStartedServices(int memFactor, long now, String serviceName) {
        if (this.mCommonProcess != this) {
            this.mCommonProcess.incStartedServices(memFactor, now, serviceName);
        }
        this.mNumStartedServices++;
        if (this.mNumStartedServices == 1 && this.mCurState == -1) {
            setState((memFactor * 14) + 7, now);
        }
    }

    public void decStartedServices(int memFactor, long now, String serviceName) {
        if (this.mCommonProcess != this) {
            this.mCommonProcess.decStartedServices(memFactor, now, serviceName);
        }
        this.mNumStartedServices--;
        if (this.mNumStartedServices == 0 && this.mCurState % 14 == 7) {
            setState(-1, now);
        } else if (this.mNumStartedServices < 0) {
            Slog.wtfStack(TAG, "Proc started services underrun: pkg=" + this.mPackage + " uid=" + this.mUid + " name=" + this.mName);
            this.mNumStartedServices = 0;
        }
    }

    public void addPss(long pss, long uss, boolean always, ArrayMap<String, ProcessStateHolder> pkgList) {
        ensureNotDead();
        if (always || this.mLastPssState != this.mCurState || SystemClock.uptimeMillis() >= this.mLastPssTime + 30000) {
            this.mLastPssState = this.mCurState;
            this.mLastPssTime = SystemClock.uptimeMillis();
            if (this.mCurState != -1) {
                this.mCommonProcess.mPssTable.mergeStats(this.mCurState, 1, pss, pss, pss, uss, uss, uss);
                if (this.mCommonProcess.mMultiPackage && pkgList != null) {
                    for (int ip = pkgList.size() - 1; ip >= 0; ip--) {
                        pullFixedProc(pkgList, ip).mPssTable.mergeStats(this.mCurState, 1, pss, pss, pss, uss, uss, uss);
                    }
                }
            }
        }
    }

    public void reportExcessiveWake(ArrayMap<String, ProcessStateHolder> pkgList) {
        ensureNotDead();
        ProcessState processState = this.mCommonProcess;
        processState.mNumExcessiveWake++;
        if (this.mCommonProcess.mMultiPackage) {
            for (int ip = pkgList.size() - 1; ip >= 0; ip--) {
                processState = pullFixedProc(pkgList, ip);
                processState.mNumExcessiveWake++;
            }
        }
    }

    public void reportExcessiveCpu(ArrayMap<String, ProcessStateHolder> pkgList) {
        ensureNotDead();
        ProcessState processState = this.mCommonProcess;
        processState.mNumExcessiveCpu++;
        if (this.mCommonProcess.mMultiPackage) {
            for (int ip = pkgList.size() - 1; ip >= 0; ip--) {
                processState = pullFixedProc(pkgList, ip);
                processState.mNumExcessiveCpu++;
            }
        }
    }

    private void addCachedKill(int num, long minPss, long avgPss, long maxPss) {
        if (this.mNumCachedKill <= 0) {
            this.mNumCachedKill = num;
            this.mMinCachedKillPss = minPss;
            this.mAvgCachedKillPss = avgPss;
            this.mMaxCachedKillPss = maxPss;
            return;
        }
        if (minPss < this.mMinCachedKillPss) {
            this.mMinCachedKillPss = minPss;
        }
        if (maxPss > this.mMaxCachedKillPss) {
            this.mMaxCachedKillPss = maxPss;
        }
        this.mAvgCachedKillPss = (long) (((((double) this.mAvgCachedKillPss) * ((double) this.mNumCachedKill)) + ((double) avgPss)) / ((double) (this.mNumCachedKill + num)));
        this.mNumCachedKill += num;
    }

    public void reportCachedKill(ArrayMap<String, ProcessStateHolder> pkgList, long pss) {
        ensureNotDead();
        this.mCommonProcess.addCachedKill(1, pss, pss, pss);
        if (this.mCommonProcess.mMultiPackage) {
            for (int ip = pkgList.size() - 1; ip >= 0; ip--) {
                pullFixedProc(pkgList, ip).addCachedKill(1, pss, pss, pss);
            }
        }
    }

    public ProcessState pullFixedProc(String pkgName) {
        if (!this.mMultiPackage) {
            return this;
        }
        SparseArray<PackageState> vpkg = (SparseArray) this.mStats.mPackages.get(pkgName, this.mUid);
        if (vpkg == null) {
            throw new IllegalStateException("Didn't find package " + pkgName + " / " + this.mUid);
        }
        PackageState pkg = (PackageState) vpkg.get(this.mVersion);
        if (pkg == null) {
            throw new IllegalStateException("Didn't find package " + pkgName + " / " + this.mUid + " vers " + this.mVersion);
        }
        ProcessState proc = (ProcessState) pkg.mProcesses.get(this.mName);
        if (proc != null) {
            return proc;
        }
        throw new IllegalStateException("Didn't create per-package process " + this.mName + " in pkg " + pkgName + " / " + this.mUid + " vers " + this.mVersion);
    }

    private ProcessState pullFixedProc(ArrayMap<String, ProcessStateHolder> pkgList, int index) {
        ProcessStateHolder holder = (ProcessStateHolder) pkgList.valueAt(index);
        ProcessState proc = holder.state;
        if (this.mDead && proc.mCommonProcess != proc) {
            Log.wtf(TAG, "Pulling dead proc: name=" + this.mName + " pkg=" + this.mPackage + " uid=" + this.mUid + " common.name=" + this.mCommonProcess.mName);
            proc = this.mStats.getProcessStateLocked(proc.mPackage, proc.mUid, proc.mVersion, proc.mName);
        }
        if (proc.mMultiPackage) {
            SparseArray<PackageState> vpkg = (SparseArray) this.mStats.mPackages.get((String) pkgList.keyAt(index), proc.mUid);
            if (vpkg == null) {
                throw new IllegalStateException("No existing package " + ((String) pkgList.keyAt(index)) + "/" + proc.mUid + " for multi-proc " + proc.mName);
            }
            PackageState pkg = (PackageState) vpkg.get(proc.mVersion);
            if (pkg == null) {
                throw new IllegalStateException("No existing package " + ((String) pkgList.keyAt(index)) + "/" + proc.mUid + " for multi-proc " + proc.mName + " version " + proc.mVersion);
            }
            String savedName = proc.mName;
            proc = (ProcessState) pkg.mProcesses.get(proc.mName);
            if (proc == null) {
                throw new IllegalStateException("Didn't create per-package process " + savedName + " in pkg " + pkg.mPackageName + "/" + pkg.mUid);
            }
            holder.state = proc;
        }
        return proc;
    }

    public long getDuration(int state, long now) {
        long time = this.mDurations.getValueForId((byte) state);
        if (this.mCurState == state) {
            return time + (now - this.mStartTime);
        }
        return time;
    }

    public long getPssSampleCount(int state) {
        return this.mPssTable.getValueForId((byte) state, 0);
    }

    public long getPssMinimum(int state) {
        return this.mPssTable.getValueForId((byte) state, 1);
    }

    public long getPssAverage(int state) {
        return this.mPssTable.getValueForId((byte) state, 2);
    }

    public long getPssMaximum(int state) {
        return this.mPssTable.getValueForId((byte) state, 3);
    }

    public long getPssUssMinimum(int state) {
        return this.mPssTable.getValueForId((byte) state, 4);
    }

    public long getPssUssAverage(int state) {
        return this.mPssTable.getValueForId((byte) state, 5);
    }

    public long getPssUssMaximum(int state) {
        return this.mPssTable.getValueForId((byte) state, 6);
    }

    public void aggregatePss(TotalMemoryUseCollection data, long now) {
        PssAggr fgPss = new PssAggr();
        PssAggr bgPss = new PssAggr();
        PssAggr cachedPss = new PssAggr();
        boolean havePss = DEBUG_PARCEL;
        int i = 0;
        while (true) {
            if (i >= this.mDurations.getKeyCount()) {
                break;
            }
            long avg;
            int type = SparseMappingTable.getIdFromKey(this.mDurations.getKeyAt(i));
            int procState = type % 14;
            long samples = getPssSampleCount(type);
            if (samples > 0) {
                avg = getPssAverage(type);
                havePss = true;
                if (procState <= 2) {
                    fgPss.add(avg, samples);
                } else if (procState <= 8) {
                    bgPss.add(avg, samples);
                } else {
                    cachedPss.add(avg, samples);
                }
            }
            i++;
        }
        if (havePss) {
            boolean fgHasBg = DEBUG_PARCEL;
            boolean fgHasCached = DEBUG_PARCEL;
            boolean bgHasCached = DEBUG_PARCEL;
            if (fgPss.samples < 3) {
                if (bgPss.samples > 0) {
                    fgHasBg = true;
                    fgPss.add(bgPss.pss, bgPss.samples);
                }
            }
            if (fgPss.samples < 3) {
                if (cachedPss.samples > 0) {
                    fgHasCached = true;
                    fgPss.add(cachedPss.pss, cachedPss.samples);
                }
            }
            if (bgPss.samples < 3) {
                if (cachedPss.samples > 0) {
                    bgHasCached = true;
                    bgPss.add(cachedPss.pss, cachedPss.samples);
                }
            }
            if (bgPss.samples < 3 && !fgHasBg) {
                if (fgPss.samples > 0) {
                    bgPss.add(fgPss.pss, fgPss.samples);
                }
            }
            if (cachedPss.samples < 3 && !bgHasCached) {
                if (bgPss.samples > 0) {
                    cachedPss.add(bgPss.pss, bgPss.samples);
                }
            }
            if (cachedPss.samples < 3 && !fgHasCached) {
                if (fgPss.samples > 0) {
                    cachedPss.add(fgPss.pss, fgPss.samples);
                }
            }
            i = 0;
            while (true) {
                if (i < this.mDurations.getKeyCount()) {
                    int key = this.mDurations.getKeyAt(i);
                    type = SparseMappingTable.getIdFromKey(key);
                    long time = this.mDurations.getValue(key);
                    int i2 = this.mCurState;
                    if (r0 == type) {
                        time += now - this.mStartTime;
                    }
                    procState = type % 14;
                    long[] jArr = data.processStateTime;
                    jArr[procState] = jArr[procState] + time;
                    samples = getPssSampleCount(type);
                    if (samples > 0) {
                        avg = getPssAverage(type);
                    } else if (procState <= 2) {
                        samples = fgPss.samples;
                        avg = fgPss.pss;
                    } else if (procState <= 8) {
                        samples = bgPss.samples;
                        avg = bgPss.pss;
                    } else {
                        samples = cachedPss.samples;
                        avg = cachedPss.pss;
                    }
                    double d = (double) data.processStatePss[procState];
                    double d2 = (double) data.processStateSamples[procState];
                    d2 = (double) avg;
                    double d3 = (double) samples;
                    double newAvg = ((r0 * r0) + (r0 * r0)) / ((double) (((long) data.processStateSamples[procState]) + samples));
                    data.processStatePss[procState] = (long) newAvg;
                    int[] iArr = data.processStateSamples;
                    iArr[procState] = (int) (((long) iArr[procState]) + samples);
                    double[] dArr = data.processStateWeight;
                    dArr[procState] = dArr[procState] + (((double) avg) * ((double) time));
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    public long computeProcessTimeLocked(int[] screenStates, int[] memStates, int[] procStates, long now) {
        long totalTime = 0;
        for (int i : screenStates) {
            for (int i2 : memStates) {
                for (int i22 : procStates) {
                    totalTime += getDuration(((i + i22) * 14) + i22, now);
                }
            }
        }
        this.mTmpTotalTime = totalTime;
        return totalTime;
    }

    public void dumpSummary(PrintWriter pw, String prefix, int[] screenStates, int[] memStates, int[] procStates, long now, long totalTime) {
        pw.print(prefix);
        pw.print("* ");
        pw.print(this.mName);
        pw.print(" / ");
        UserHandle.formatUid(pw, this.mUid);
        pw.print(" / v");
        pw.print(this.mVersion);
        pw.println(":");
        dumpProcessSummaryDetails(pw, prefix, "         TOTAL: ", screenStates, memStates, procStates, now, totalTime, true);
        PrintWriter printWriter = pw;
        String str = prefix;
        dumpProcessSummaryDetails(printWriter, str, "    Persistent: ", screenStates, memStates, new int[]{0}, now, totalTime, true);
        printWriter = pw;
        str = prefix;
        dumpProcessSummaryDetails(printWriter, str, "           Top: ", screenStates, memStates, new int[]{1}, now, totalTime, true);
        printWriter = pw;
        str = prefix;
        dumpProcessSummaryDetails(printWriter, str, "        Imp Fg: ", screenStates, memStates, new int[]{2}, now, totalTime, true);
        printWriter = pw;
        str = prefix;
        dumpProcessSummaryDetails(printWriter, str, "        Imp Bg: ", screenStates, memStates, new int[]{3}, now, totalTime, true);
        printWriter = pw;
        str = prefix;
        dumpProcessSummaryDetails(printWriter, str, "        Backup: ", screenStates, memStates, new int[]{4}, now, totalTime, true);
        printWriter = pw;
        str = prefix;
        dumpProcessSummaryDetails(printWriter, str, "     Heavy Wgt: ", screenStates, memStates, new int[]{5}, now, totalTime, true);
        printWriter = pw;
        str = prefix;
        dumpProcessSummaryDetails(printWriter, str, "       Service: ", screenStates, memStates, new int[]{6}, now, totalTime, true);
        printWriter = pw;
        str = prefix;
        dumpProcessSummaryDetails(printWriter, str, "    Service Rs: ", screenStates, memStates, new int[]{7}, now, totalTime, true);
        printWriter = pw;
        str = prefix;
        dumpProcessSummaryDetails(printWriter, str, "      Receiver: ", screenStates, memStates, new int[]{8}, now, totalTime, true);
        printWriter = pw;
        str = prefix;
        dumpProcessSummaryDetails(printWriter, str, "        (Home): ", screenStates, memStates, new int[]{9}, now, totalTime, true);
        printWriter = pw;
        str = prefix;
        dumpProcessSummaryDetails(printWriter, str, "    (Last Act): ", screenStates, memStates, new int[]{10}, now, totalTime, true);
        dumpProcessSummaryDetails(pw, prefix, "      (Cached): ", screenStates, memStates, new int[]{11, 12, 13}, now, totalTime, true);
    }

    public void dumpProcessState(PrintWriter pw, String prefix, int[] screenStates, int[] memStates, int[] procStates, long now) {
        long totalTime = 0;
        int printedScreen = -1;
        for (int iscreen : screenStates) {
            int printedMem = -1;
            for (int imem : memStates) {
                for (int ip = 0; ip < procStates.length; ip++) {
                    int bucket = ((iscreen + imem) * 14) + procStates[ip];
                    long time = this.mDurations.getValueForId((byte) bucket);
                    String running = "";
                    if (this.mCurState == bucket) {
                        running = " (running)";
                    }
                    if (time != 0) {
                        pw.print(prefix);
                        if (screenStates.length > 1) {
                            int i;
                            if (printedScreen != iscreen) {
                                i = iscreen;
                            } else {
                                i = -1;
                            }
                            DumpUtils.printScreenLabel(pw, i);
                            printedScreen = iscreen;
                        }
                        if (memStates.length > 1) {
                            DumpUtils.printMemLabel(pw, printedMem != imem ? imem : -1, '/');
                            printedMem = imem;
                        }
                        pw.print(DumpUtils.STATE_NAMES[procStates[ip]]);
                        pw.print(": ");
                        TimeUtils.formatDuration(time, pw);
                        pw.println(running);
                        totalTime += time;
                    }
                }
            }
        }
        if (totalTime != 0) {
            pw.print(prefix);
            if (screenStates.length > 1) {
                DumpUtils.printScreenLabel(pw, -1);
            }
            if (memStates.length > 1) {
                DumpUtils.printMemLabel(pw, -1, '/');
            }
            pw.print("TOTAL  : ");
            TimeUtils.formatDuration(totalTime, pw);
            pw.println();
        }
    }

    public void dumpPss(PrintWriter pw, String prefix, int[] screenStates, int[] memStates, int[] procStates) {
        boolean printedHeader = DEBUG_PARCEL;
        int printedScreen = -1;
        for (int iscreen : screenStates) {
            int printedMem = -1;
            for (int imem : memStates) {
                for (int ip = 0; ip < procStates.length; ip++) {
                    int bucket = ((iscreen + imem) * 14) + procStates[ip];
                    long count = getPssSampleCount(bucket);
                    if (count > 0) {
                        if (!printedHeader) {
                            pw.print(prefix);
                            pw.print("PSS/USS (");
                            pw.print(this.mPssTable.getKeyCount());
                            pw.println(" entries):");
                            printedHeader = true;
                        }
                        pw.print(prefix);
                        pw.print("  ");
                        if (screenStates.length > 1) {
                            DumpUtils.printScreenLabel(pw, printedScreen != iscreen ? iscreen : -1);
                            printedScreen = iscreen;
                        }
                        if (memStates.length > 1) {
                            DumpUtils.printMemLabel(pw, printedMem != imem ? imem : -1, '/');
                            printedMem = imem;
                        }
                        pw.print(DumpUtils.STATE_NAMES[procStates[ip]]);
                        pw.print(": ");
                        pw.print(count);
                        pw.print(" samples ");
                        DebugUtils.printSizeValue(pw, getPssMinimum(bucket) * 1024);
                        pw.print(" ");
                        DebugUtils.printSizeValue(pw, getPssAverage(bucket) * 1024);
                        pw.print(" ");
                        DebugUtils.printSizeValue(pw, getPssMaximum(bucket) * 1024);
                        pw.print(" / ");
                        DebugUtils.printSizeValue(pw, getPssUssMinimum(bucket) * 1024);
                        pw.print(" ");
                        DebugUtils.printSizeValue(pw, getPssUssAverage(bucket) * 1024);
                        pw.print(" ");
                        DebugUtils.printSizeValue(pw, getPssUssMaximum(bucket) * 1024);
                        pw.println();
                    }
                }
            }
        }
        if (this.mNumExcessiveWake != 0) {
            pw.print(prefix);
            pw.print("Killed for excessive wake locks: ");
            pw.print(this.mNumExcessiveWake);
            pw.println(" times");
        }
        if (this.mNumExcessiveCpu != 0) {
            pw.print(prefix);
            pw.print("Killed for excessive CPU use: ");
            pw.print(this.mNumExcessiveCpu);
            pw.println(" times");
        }
        if (this.mNumCachedKill != 0) {
            pw.print(prefix);
            pw.print("Killed from cached state: ");
            pw.print(this.mNumCachedKill);
            pw.print(" times from pss ");
            DebugUtils.printSizeValue(pw, this.mMinCachedKillPss * 1024);
            pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
            DebugUtils.printSizeValue(pw, this.mAvgCachedKillPss * 1024);
            pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
            DebugUtils.printSizeValue(pw, this.mMaxCachedKillPss * 1024);
            pw.println();
        }
    }

    private void dumpProcessSummaryDetails(PrintWriter pw, String prefix, String label, int[] screenStates, int[] memStates, int[] procStates, long now, long totalTime, boolean full) {
        ProcessDataCollection totals = new ProcessDataCollection(screenStates, memStates, procStates);
        computeProcessData(totals, now);
        if ((((double) totals.totalTime) / ((double) totalTime)) * 100.0d >= 0.005d || totals.numPss != 0) {
            if (prefix != null) {
                pw.print(prefix);
            }
            if (label != null) {
                pw.print(label);
            }
            totals.print(pw, totalTime, full);
            if (prefix != null) {
                pw.println();
            }
        }
    }

    public void dumpInternalLocked(PrintWriter pw, String prefix, boolean dumpAll) {
        if (dumpAll) {
            pw.print(prefix);
            pw.print("myID=");
            pw.print(Integer.toHexString(System.identityHashCode(this)));
            pw.print(" mCommonProcess=");
            pw.print(Integer.toHexString(System.identityHashCode(this.mCommonProcess)));
            pw.print(" mPackage=");
            pw.println(this.mPackage);
            if (this.mMultiPackage) {
                pw.print(prefix);
                pw.print("mMultiPackage=");
                pw.println(this.mMultiPackage);
            }
            if (this != this.mCommonProcess) {
                pw.print(prefix);
                pw.print("Common Proc: ");
                pw.print(this.mCommonProcess.mName);
                pw.print("/");
                pw.print(this.mCommonProcess.mUid);
                pw.print(" pkg=");
                pw.println(this.mCommonProcess.mPackage);
            }
        }
        if (this.mActive) {
            pw.print(prefix);
            pw.print("mActive=");
            pw.println(this.mActive);
        }
        if (this.mDead) {
            pw.print(prefix);
            pw.print("mDead=");
            pw.println(this.mDead);
        }
        if (this.mNumActiveServices != 0 || this.mNumStartedServices != 0) {
            pw.print(prefix);
            pw.print("mNumActiveServices=");
            pw.print(this.mNumActiveServices);
            pw.print(" mNumStartedServices=");
            pw.println(this.mNumStartedServices);
        }
    }

    public void computeProcessData(ProcessDataCollection data, long now) {
        data.totalTime = 0;
        data.maxUss = 0;
        data.avgUss = 0;
        data.minUss = 0;
        data.maxPss = 0;
        data.avgPss = 0;
        data.minPss = 0;
        data.numPss = 0;
        int is = 0;
        while (true) {
            int length = data.screenStates.length;
            if (is < r0) {
                int im = 0;
                while (true) {
                    length = data.memStates.length;
                    if (im >= r0) {
                        break;
                    }
                    int ip = 0;
                    while (true) {
                        length = data.procStates.length;
                        if (ip >= r0) {
                            break;
                        }
                        int bucket = ((data.screenStates[is] + data.memStates[im]) * 14) + data.procStates[ip];
                        data.totalTime += getDuration(bucket, now);
                        long samples = getPssSampleCount(bucket);
                        if (samples > 0) {
                            long minPss = getPssMinimum(bucket);
                            long avgPss = getPssAverage(bucket);
                            long maxPss = getPssMaximum(bucket);
                            long minUss = getPssUssMinimum(bucket);
                            long avgUss = getPssUssAverage(bucket);
                            long maxUss = getPssUssMaximum(bucket);
                            if (data.numPss == 0) {
                                data.minPss = minPss;
                                data.avgPss = avgPss;
                                data.maxPss = maxPss;
                                data.minUss = minUss;
                                data.avgUss = avgUss;
                                data.maxUss = maxUss;
                            } else {
                                if (minPss < data.minPss) {
                                    data.minPss = minPss;
                                }
                                double d = (double) data.avgPss;
                                double d2 = (double) data.numPss;
                                d2 = (double) avgPss;
                                double d3 = (double) samples;
                                data.avgPss = (long) (((r0 * r0) + (r0 * r0)) / ((double) (data.numPss + samples)));
                                if (maxPss > data.maxPss) {
                                    data.maxPss = maxPss;
                                }
                                if (minUss < data.minUss) {
                                    data.minUss = minUss;
                                }
                                d = (double) data.avgUss;
                                d2 = (double) data.numPss;
                                d2 = (double) avgUss;
                                d3 = (double) samples;
                                data.avgUss = (long) (((r0 * r0) + (r0 * r0)) / ((double) (data.numPss + samples)));
                                if (maxUss > data.maxUss) {
                                    data.maxUss = maxUss;
                                }
                            }
                            data.numPss += samples;
                        }
                        ip++;
                    }
                    im++;
                }
                is++;
            } else {
                return;
            }
        }
    }

    public void dumpCsv(PrintWriter pw, boolean sepScreenStates, int[] screenStates, boolean sepMemStates, int[] memStates, boolean sepProcStates, int[] procStates, long now) {
        int NSS = sepScreenStates ? screenStates.length : 1;
        int NMS = sepMemStates ? memStates.length : 1;
        int NPS = sepProcStates ? procStates.length : 1;
        int iss = 0;
        while (iss < NSS) {
            int ims = 0;
            while (ims < NMS) {
                int ips = 0;
                while (ips < NPS) {
                    int vsscreen = sepScreenStates ? screenStates[iss] : 0;
                    int vsmem = sepMemStates ? memStates[ims] : 0;
                    int vsproc = sepProcStates ? procStates[ips] : 0;
                    int NSA = sepScreenStates ? 1 : screenStates.length;
                    int NMA = sepMemStates ? 1 : memStates.length;
                    int NPA = sepProcStates ? 1 : procStates.length;
                    long totalTime = 0;
                    int isa = 0;
                    while (isa < NSA) {
                        int ima = 0;
                        while (ima < NMA) {
                            int ipa = 0;
                            while (ipa < NPA) {
                                int vascreen = sepScreenStates ? 0 : screenStates[isa];
                                totalTime += getDuration((((((vsscreen + vascreen) + vsmem) + (sepMemStates ? 0 : memStates[ima])) * 14) + vsproc) + (sepProcStates ? 0 : procStates[ipa]), now);
                                ipa++;
                            }
                            ima++;
                        }
                        isa++;
                    }
                    pw.print("\t");
                    pw.print(totalTime);
                    ips++;
                }
                ims++;
            }
            iss++;
        }
    }

    public void dumpPackageProcCheckin(PrintWriter pw, String pkgName, int uid, int vers, String itemName, long now) {
        pw.print("pkgproc,");
        pw.print(pkgName);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(uid);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(vers);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(DumpUtils.collapseString(pkgName, itemName));
        dumpAllStateCheckin(pw, now);
        pw.println();
        if (this.mPssTable.getKeyCount() > 0) {
            pw.print("pkgpss,");
            pw.print(pkgName);
            pw.print(PtmLog.PAIRE_DELIMETER);
            pw.print(uid);
            pw.print(PtmLog.PAIRE_DELIMETER);
            pw.print(vers);
            pw.print(PtmLog.PAIRE_DELIMETER);
            pw.print(DumpUtils.collapseString(pkgName, itemName));
            dumpAllPssCheckin(pw);
            pw.println();
        }
        if (this.mNumExcessiveWake <= 0 && this.mNumExcessiveCpu <= 0) {
            if (this.mNumCachedKill <= 0) {
                return;
            }
        }
        pw.print("pkgkills,");
        pw.print(pkgName);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(uid);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(vers);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(DumpUtils.collapseString(pkgName, itemName));
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(this.mNumExcessiveWake);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(this.mNumExcessiveCpu);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(this.mNumCachedKill);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(this.mMinCachedKillPss);
        pw.print(":");
        pw.print(this.mAvgCachedKillPss);
        pw.print(":");
        pw.print(this.mMaxCachedKillPss);
        pw.println();
    }

    public void dumpProcCheckin(PrintWriter pw, String procName, int uid, long now) {
        if (this.mDurations.getKeyCount() > 0) {
            pw.print("proc,");
            pw.print(procName);
            pw.print(PtmLog.PAIRE_DELIMETER);
            pw.print(uid);
            dumpAllStateCheckin(pw, now);
            pw.println();
        }
        if (this.mPssTable.getKeyCount() > 0) {
            pw.print("pss,");
            pw.print(procName);
            pw.print(PtmLog.PAIRE_DELIMETER);
            pw.print(uid);
            dumpAllPssCheckin(pw);
            pw.println();
        }
        if (this.mNumExcessiveWake <= 0 && this.mNumExcessiveCpu <= 0) {
            if (this.mNumCachedKill <= 0) {
                return;
            }
        }
        pw.print("kills,");
        pw.print(procName);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(uid);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(this.mNumExcessiveWake);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(this.mNumExcessiveCpu);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(this.mNumCachedKill);
        pw.print(PtmLog.PAIRE_DELIMETER);
        pw.print(this.mMinCachedKillPss);
        pw.print(":");
        pw.print(this.mAvgCachedKillPss);
        pw.print(":");
        pw.print(this.mMaxCachedKillPss);
        pw.println();
    }

    public void dumpAllStateCheckin(PrintWriter pw, long now) {
        boolean didCurState = DEBUG_PARCEL;
        for (int i = 0; i < this.mDurations.getKeyCount(); i++) {
            int key = this.mDurations.getKeyAt(i);
            int type = SparseMappingTable.getIdFromKey(key);
            long time = this.mDurations.getValue(key);
            if (this.mCurState == type) {
                didCurState = true;
                time += now - this.mStartTime;
            }
            DumpUtils.printProcStateTagAndValue(pw, type, time);
        }
        if (!didCurState && this.mCurState != -1) {
            DumpUtils.printProcStateTagAndValue(pw, this.mCurState, now - this.mStartTime);
        }
    }

    public void dumpAllPssCheckin(PrintWriter pw) {
        int N = this.mPssTable.getKeyCount();
        for (int i = 0; i < N; i++) {
            int key = this.mPssTable.getKeyAt(i);
            int type = SparseMappingTable.getIdFromKey(key);
            pw.print(PhoneNumberUtils.PAUSE);
            DumpUtils.printProcStateTag(pw, type);
            pw.print(':');
            pw.print(this.mPssTable.getValue(key, 0));
            pw.print(':');
            pw.print(this.mPssTable.getValue(key, 1));
            pw.print(':');
            pw.print(this.mPssTable.getValue(key, 2));
            pw.print(':');
            pw.print(this.mPssTable.getValue(key, 3));
            pw.print(':');
            pw.print(this.mPssTable.getValue(key, 4));
            pw.print(':');
            pw.print(this.mPssTable.getValue(key, 5));
            pw.print(':');
            pw.print(this.mPssTable.getValue(key, 6));
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(LogPower.START_CHG_ROTATION);
        sb.append("ProcessState{").append(Integer.toHexString(System.identityHashCode(this))).append(" ").append(this.mName).append("/").append(this.mUid).append(" pkg=").append(this.mPackage);
        if (this.mMultiPackage) {
            sb.append(" (multi)");
        }
        if (this.mCommonProcess != this) {
            sb.append(" (sub)");
        }
        sb.append("}");
        return sb.toString();
    }
}
