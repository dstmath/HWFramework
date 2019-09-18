package com.android.internal.app.procstats;

import android.os.Parcel;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.DebugUtils;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.util.proto.ProtoUtils;
import com.android.internal.app.procstats.ProcessStats;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.widget.LockPatternUtils;
import java.io.PrintWriter;
import java.lang.annotation.RCUnownedRef;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public final class ProcessState {
    public static final Comparator<ProcessState> COMPARATOR = new Comparator<ProcessState>() {
        public int compare(ProcessState lhs, ProcessState rhs) {
            if (lhs.mTmpTotalTime < rhs.mTmpTotalTime) {
                return -1;
            }
            if (lhs.mTmpTotalTime > rhs.mTmpTotalTime) {
                return 1;
            }
            return 0;
        }
    };
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_PARCEL = false;
    private static final int[] PROCESS_STATE_TO_STATE = {0, 0, 1, 2, 2, 2, 3, 3, 4, 5, 7, 1, 8, 9, 10, 11, 12, 11, 13};
    private static final String TAG = "ProcessStats";
    private boolean mActive;
    private long mAvgCachedKillPss;
    private ProcessState mCommonProcess;
    private int mCurState = -1;
    private boolean mDead;
    private final DurationsTable mDurations;
    private int mLastPssState = -1;
    private long mLastPssTime;
    private long mMaxCachedKillPss;
    private long mMinCachedKillPss;
    private boolean mMultiPackage;
    private final String mName;
    private int mNumActiveServices;
    private int mNumCachedKill;
    private int mNumExcessiveCpu;
    private int mNumStartedServices;
    private final String mPackage;
    private final PssTable mPssTable;
    private long mStartTime;
    @RCUnownedRef
    private final ProcessStats mStats;
    /* access modifiers changed from: private */
    public long mTmpTotalTime;
    private final int mUid;
    private final long mVersion;
    public ProcessState tmpFoundSubProc;
    public int tmpNumInUse;

    static class PssAggr {
        long pss = 0;
        long samples = 0;

        PssAggr() {
        }

        /* access modifiers changed from: package-private */
        public void add(long newPss, long newSamples) {
            this.pss = ((long) ((((double) this.pss) * ((double) this.samples)) + (((double) newPss) * ((double) newSamples)))) / (this.samples + newSamples);
            this.samples += newSamples;
        }
    }

    public ProcessState(ProcessStats processStats, String pkg, int uid, long vers, String name) {
        this.mStats = processStats;
        this.mName = name;
        this.mCommonProcess = this;
        this.mPackage = pkg;
        this.mUid = uid;
        this.mVersion = vers;
        this.mDurations = new DurationsTable(processStats.mTableData);
        this.mPssTable = new PssTable(processStats.mTableData);
    }

    public ProcessState(ProcessState commonProcess, String pkg, int uid, long vers, String name, long now) {
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
        pnew.mPssTable.copyFrom(this.mPssTable, 10);
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

    public long getVersion() {
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
            Slog.w("ProcessStats", "ProcessState dead: name=" + this.mName + " pkg=" + this.mPackage + " uid=" + this.mUid + " common.name=" + this.mCommonProcess.mName);
        }
    }

    public void writeToParcel(Parcel out, long now) {
        out.writeInt(this.mMultiPackage ? 1 : 0);
        this.mDurations.writeToParcel(out);
        this.mPssTable.writeToParcel(out);
        out.writeInt(0);
        out.writeInt(this.mNumExcessiveCpu);
        out.writeInt(this.mNumCachedKill);
        if (this.mNumCachedKill > 0) {
            out.writeLong(this.mMinCachedKillPss);
            out.writeLong(this.mAvgCachedKillPss);
            out.writeLong(this.mMaxCachedKillPss);
        }
    }

    public boolean readFromParcel(Parcel in, boolean fully) {
        boolean multiPackage = in.readInt() != 0;
        if (fully) {
            this.mMultiPackage = multiPackage;
        }
        if (!this.mDurations.readFromParcel(in) || !this.mPssTable.readFromParcel(in)) {
            return false;
        }
        in.readInt();
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
        this.mActive = false;
    }

    public boolean isInUse() {
        return this.mActive || this.mNumActiveServices > 0 || this.mNumStartedServices > 0 || this.mCurState != -1;
    }

    public boolean isActive() {
        return this.mActive;
    }

    public boolean hasAnyData() {
        return (this.mDurations.getKeyCount() == 0 && this.mCurState == -1 && this.mPssTable.getKeyCount() == 0) ? false : true;
    }

    public void setState(int state, int memFactor, long now, ArrayMap<String, ProcessStats.ProcessStateHolder> pkgList) {
        int state2;
        if (state == 19) {
            Slog.i("ProcessStats", "setState state not existent, error!");
            return;
        }
        if (state < 0) {
            state2 = this.mNumStartedServices > 0 ? 6 + (memFactor * 14) : -1;
        } else {
            state2 = PROCESS_STATE_TO_STATE[state] + (memFactor * 14);
        }
        this.mCommonProcess.setState(state2, now);
        if (this.mCommonProcess.mMultiPackage && pkgList != null) {
            for (int ip = pkgList.size() - 1; ip >= 0; ip--) {
                pullFixedProc(pkgList, ip).setState(state2, now);
            }
        }
    }

    public void setState(int state, long now) {
        ensureNotDead();
        if (!this.mDead && this.mCurState != state) {
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
            Slog.wtfStack("ProcessStats", "Proc active services underrun: pkg=" + this.mPackage + " uid=" + this.mUid + " proc=" + this.mName + " service=" + serviceName);
            this.mNumActiveServices = 0;
        }
    }

    public void incStartedServices(int memFactor, long now, String serviceName) {
        if (this.mCommonProcess != this) {
            this.mCommonProcess.incStartedServices(memFactor, now, serviceName);
        }
        this.mNumStartedServices++;
        if (this.mNumStartedServices == 1 && this.mCurState == -1) {
            setState(6 + (memFactor * 14), now);
        }
    }

    public void decStartedServices(int memFactor, long now, String serviceName) {
        if (this.mCommonProcess != this) {
            this.mCommonProcess.decStartedServices(memFactor, now, serviceName);
        }
        this.mNumStartedServices--;
        if (this.mNumStartedServices == 0 && this.mCurState % 14 == 6) {
            setState(-1, now);
        } else if (this.mNumStartedServices < 0) {
            Slog.wtfStack("ProcessStats", "Proc started services underrun: pkg=" + this.mPackage + " uid=" + this.mUid + " name=" + this.mName);
            this.mNumStartedServices = 0;
        }
    }

    public void addPss(long pss, long uss, long rss, boolean always, int type, long duration, ArrayMap<String, ProcessStats.ProcessStateHolder> pkgList) {
        ArrayMap<String, ProcessStats.ProcessStateHolder> arrayMap = pkgList;
        ensureNotDead();
        switch (type) {
            case 0:
                this.mStats.mInternalSinglePssCount++;
                this.mStats.mInternalSinglePssTime += duration;
                break;
            case 1:
                this.mStats.mInternalAllMemPssCount++;
                this.mStats.mInternalAllMemPssTime += duration;
                break;
            case 2:
                this.mStats.mInternalAllPollPssCount++;
                this.mStats.mInternalAllPollPssTime += duration;
                break;
            case 3:
                this.mStats.mExternalPssCount++;
                this.mStats.mExternalPssTime += duration;
                break;
            case 4:
                this.mStats.mExternalSlowPssCount++;
                this.mStats.mExternalSlowPssTime += duration;
                break;
        }
        if (always || this.mLastPssState != this.mCurState || SystemClock.uptimeMillis() >= this.mLastPssTime + LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS) {
            this.mLastPssState = this.mCurState;
            this.mLastPssTime = SystemClock.uptimeMillis();
            if (this.mCurState != -1) {
                this.mCommonProcess.mPssTable.mergeStats(this.mCurState, 1, pss, pss, pss, uss, uss, uss, rss, rss, rss);
                if (this.mCommonProcess.mMultiPackage && arrayMap != null) {
                    for (int ip = pkgList.size() - 1; ip >= 0; ip--) {
                        pullFixedProc(arrayMap, ip).mPssTable.mergeStats(this.mCurState, 1, pss, pss, pss, uss, uss, uss, rss, rss, rss);
                    }
                }
            }
        }
    }

    public void reportExcessiveCpu(ArrayMap<String, ProcessStats.ProcessStateHolder> pkgList) {
        ensureNotDead();
        this.mCommonProcess.mNumExcessiveCpu++;
        if (this.mCommonProcess.mMultiPackage) {
            for (int ip = pkgList.size() - 1; ip >= 0; ip--) {
                pullFixedProc(pkgList, ip).mNumExcessiveCpu++;
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

    public void reportCachedKill(ArrayMap<String, ProcessStats.ProcessStateHolder> pkgList, long pss) {
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
        LongSparseArray<ProcessStats.PackageState> vpkg = this.mStats.mPackages.get(pkgName, this.mUid);
        if (vpkg != null) {
            ProcessStats.PackageState pkg = vpkg.get(this.mVersion);
            if (pkg != null) {
                ProcessState proc = pkg.mProcesses.get(this.mName);
                if (proc != null) {
                    return proc;
                }
                throw new IllegalStateException("Didn't create per-package process " + this.mName + " in pkg " + pkgName + " / " + this.mUid + " vers " + this.mVersion);
            }
            throw new IllegalStateException("Didn't find package " + pkgName + " / " + this.mUid + " vers " + this.mVersion);
        }
        throw new IllegalStateException("Didn't find package " + pkgName + " / " + this.mUid);
    }

    private ProcessState pullFixedProc(ArrayMap<String, ProcessStats.ProcessStateHolder> pkgList, int index) {
        ProcessStats.ProcessStateHolder holder = pkgList.valueAt(index);
        ProcessState proc = holder.state;
        if (this.mDead && proc.mCommonProcess != proc) {
            Log.wtf("ProcessStats", "Pulling dead proc: name=" + this.mName + " pkg=" + this.mPackage + " uid=" + this.mUid + " common.name=" + this.mCommonProcess.mName);
            proc = this.mStats.getProcessStateLocked(proc.mPackage, proc.mUid, proc.mVersion, proc.mName);
        }
        if (proc.mMultiPackage) {
            LongSparseArray<ProcessStats.PackageState> vpkg = this.mStats.mPackages.get(pkgList.keyAt(index), proc.mUid);
            if (vpkg != null) {
                ProcessStats.PackageState pkg = vpkg.get(proc.mVersion);
                if (pkg != null) {
                    String savedName = proc.mName;
                    proc = pkg.mProcesses.get(proc.mName);
                    if (proc != null) {
                        holder.state = proc;
                    } else {
                        throw new IllegalStateException("Didn't create per-package process " + savedName + " in pkg " + pkg.mPackageName + "/" + pkg.mUid);
                    }
                } else {
                    throw new IllegalStateException("No existing package " + pkgList.keyAt(index) + "/" + proc.mUid + " for multi-proc " + proc.mName + " version " + proc.mVersion);
                }
            } else {
                throw new IllegalStateException("No existing package " + pkgList.keyAt(index) + "/" + proc.mUid + " for multi-proc " + proc.mName);
            }
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

    public long getPssRssMinimum(int state) {
        return this.mPssTable.getValueForId((byte) state, 7);
    }

    public long getPssRssAverage(int state) {
        return this.mPssTable.getValueForId((byte) state, 8);
    }

    public long getPssRssMaximum(int state) {
        return this.mPssTable.getValueForId((byte) state, 9);
    }

    public void aggregatePss(ProcessStats.TotalMemoryUseCollection data, long now) {
        long time;
        long avg;
        long samples;
        ProcessState processState = this;
        ProcessStats.TotalMemoryUseCollection totalMemoryUseCollection = data;
        PssAggr fgPss = new PssAggr();
        PssAggr bgPss = new PssAggr();
        PssAggr cachedPss = new PssAggr();
        boolean havePss = false;
        for (int i = 0; i < processState.mDurations.getKeyCount(); i++) {
            int type = SparseMappingTable.getIdFromKey(processState.mDurations.getKeyAt(i));
            int procState = type % 14;
            boolean havePss2 = havePss;
            long samples2 = processState.getPssSampleCount(type);
            if (samples2 > 0) {
                long avg2 = processState.getPssAverage(type);
                havePss2 = true;
                if (procState <= 2) {
                    fgPss.add(avg2, samples2);
                } else if (procState <= 7) {
                    bgPss.add(avg2, samples2);
                } else {
                    cachedPss.add(avg2, samples2);
                }
            }
            havePss = havePss2;
        }
        if (havePss) {
            boolean fgHasBg = false;
            boolean fgHasCached = false;
            boolean bgHasCached = false;
            if (fgPss.samples < 3 && bgPss.samples > 0) {
                fgHasBg = true;
                fgPss.add(bgPss.pss, bgPss.samples);
            }
            if (fgPss.samples < 3 && cachedPss.samples > 0) {
                fgHasCached = true;
                fgPss.add(cachedPss.pss, cachedPss.samples);
            }
            if (bgPss.samples < 3 && cachedPss.samples > 0) {
                bgHasCached = true;
                bgPss.add(cachedPss.pss, cachedPss.samples);
            }
            if (bgPss.samples < 3 && !fgHasBg && fgPss.samples > 0) {
                bgPss.add(fgPss.pss, fgPss.samples);
            }
            if (cachedPss.samples < 3 && !bgHasCached && bgPss.samples > 0) {
                cachedPss.add(bgPss.pss, bgPss.samples);
            }
            if (cachedPss.samples < 3 && !fgHasCached && fgPss.samples > 0) {
                cachedPss.add(fgPss.pss, fgPss.samples);
            }
            int i2 = 0;
            while (true) {
                int i3 = i2;
                if (i3 < processState.mDurations.getKeyCount()) {
                    int key = processState.mDurations.getKeyAt(i3);
                    byte idFromKey = SparseMappingTable.getIdFromKey(key);
                    long time2 = processState.mDurations.getValue(key);
                    if (processState.mCurState == idFromKey) {
                        time2 += now - processState.mStartTime;
                    }
                    int procState2 = idFromKey % 14;
                    long[] jArr = totalMemoryUseCollection.processStateTime;
                    jArr[procState2] = jArr[procState2] + time2;
                    long samples3 = processState.getPssSampleCount(idFromKey);
                    if (samples3 > 0) {
                        time = time2;
                        samples = samples3;
                        avg = processState.getPssAverage(idFromKey);
                    } else if (procState2 <= 2) {
                        time = time2;
                        samples = fgPss.samples;
                        avg = fgPss.pss;
                    } else {
                        time = time2;
                        if (procState2 <= 7) {
                            avg = bgPss.pss;
                            samples = bgPss.samples;
                        } else {
                            samples = cachedPss.samples;
                            avg = cachedPss.pss;
                        }
                    }
                    PssAggr fgPss2 = fgPss;
                    double newAvg = ((((double) totalMemoryUseCollection.processStatePss[procState2]) * ((double) totalMemoryUseCollection.processStateSamples[procState2])) + (((double) avg) * ((double) samples))) / ((double) (((long) totalMemoryUseCollection.processStateSamples[procState2]) + samples));
                    totalMemoryUseCollection.processStatePss[procState2] = (long) newAvg;
                    int[] iArr = totalMemoryUseCollection.processStateSamples;
                    iArr[procState2] = (int) (((long) iArr[procState2]) + samples);
                    double[] dArr = totalMemoryUseCollection.processStateWeight;
                    double d = newAvg;
                    int i4 = key;
                    byte b = idFromKey;
                    dArr[procState2] = dArr[procState2] + (((double) avg) * ((double) time));
                    i2 = i3 + 1;
                    fgPss = fgPss2;
                    bgPss = bgPss;
                    cachedPss = cachedPss;
                    fgHasBg = fgHasBg;
                    fgHasCached = fgHasCached;
                    bgHasCached = bgHasCached;
                    processState = this;
                    totalMemoryUseCollection = data;
                } else {
                    PssAggr pssAggr = bgPss;
                    PssAggr pssAggr2 = cachedPss;
                    boolean z = fgHasBg;
                    boolean z2 = fgHasCached;
                    boolean z3 = bgHasCached;
                    return;
                }
            }
        }
    }

    public long computeProcessTimeLocked(int[] screenStates, int[] memStates, int[] procStates, long now) {
        long totalTime = 0;
        for (int is = 0; is < screenStates.length; is++) {
            int im = 0;
            while (im < memStates.length) {
                long totalTime2 = totalTime;
                for (int i : procStates) {
                    totalTime2 += getDuration(((screenStates[is] + memStates[im]) * 14) + i, now);
                }
                im++;
                totalTime = totalTime2;
            }
        }
        this.mTmpTotalTime = totalTime;
        return totalTime;
    }

    public void dumpSummary(PrintWriter pw, String prefix, int[] screenStates, int[] memStates, int[] procStates, long now, long totalTime) {
        PrintWriter printWriter = pw;
        pw.print(prefix);
        printWriter.print("* ");
        printWriter.print(this.mName);
        printWriter.print(" / ");
        UserHandle.formatUid(printWriter, this.mUid);
        printWriter.print(" / v");
        printWriter.print(this.mVersion);
        printWriter.println(":");
        PrintWriter printWriter2 = printWriter;
        String str = prefix;
        int[] iArr = screenStates;
        int[] iArr2 = memStates;
        long j = now;
        long j2 = totalTime;
        dumpProcessSummaryDetails(printWriter2, str, "         TOTAL: ", iArr, iArr2, procStates, j, j2, true);
        dumpProcessSummaryDetails(printWriter2, str, "    Persistent: ", iArr, iArr2, new int[]{0}, j, j2, true);
        dumpProcessSummaryDetails(printWriter2, str, "           Top: ", iArr, iArr2, new int[]{1}, j, j2, true);
        dumpProcessSummaryDetails(printWriter2, str, "        Imp Fg: ", iArr, iArr2, new int[]{2}, j, j2, true);
        dumpProcessSummaryDetails(printWriter2, str, "        Imp Bg: ", iArr, iArr2, new int[]{3}, j, j2, true);
        dumpProcessSummaryDetails(printWriter2, str, "        Backup: ", iArr, iArr2, new int[]{4}, j, j2, true);
        dumpProcessSummaryDetails(printWriter2, str, "     Heavy Wgt: ", iArr, iArr2, new int[]{8}, j, j2, true);
        dumpProcessSummaryDetails(printWriter2, str, "       Service: ", iArr, iArr2, new int[]{5}, j, j2, true);
        dumpProcessSummaryDetails(printWriter2, str, "    Service Rs: ", iArr, iArr2, new int[]{6}, j, j2, true);
        dumpProcessSummaryDetails(printWriter2, str, "      Receiver: ", iArr, iArr2, new int[]{7}, j, j2, true);
        dumpProcessSummaryDetails(printWriter2, str, "         Heavy: ", iArr, iArr2, new int[]{9}, j, j2, true);
        dumpProcessSummaryDetails(printWriter2, str, "        (Home): ", iArr, iArr2, new int[]{9}, j, j2, true);
        dumpProcessSummaryDetails(printWriter2, str, "    (Last Act): ", iArr, iArr2, new int[]{10}, j, j2, true);
        dumpProcessSummaryDetails(printWriter2, str, "      (Cached): ", iArr, iArr2, new int[]{11, 12, 13}, j, j2, true);
    }

    public void dumpProcessState(PrintWriter pw, String prefix, int[] screenStates, int[] memStates, int[] procStates, long now) {
        int i;
        String running;
        ProcessState processState = this;
        PrintWriter printWriter = pw;
        int[] iArr = screenStates;
        int[] iArr2 = memStates;
        int[] iArr3 = procStates;
        int printedScreen = -1;
        long totalTime = 0;
        int is = 0;
        while (is < iArr.length) {
            int printedMem = -1;
            long totalTime2 = totalTime;
            int im = 0;
            while (im < iArr2.length) {
                int ip = 0;
                while (ip < iArr3.length) {
                    int iscreen = iArr[is];
                    int imem = iArr2[im];
                    int bucket = ((iscreen + imem) * 14) + iArr3[ip];
                    long time = processState.mDurations.getValueForId((byte) bucket);
                    String running2 = "";
                    if (processState.mCurState == bucket) {
                        running = " (running)";
                    } else {
                        running = running2;
                    }
                    if (time != 0) {
                        pw.print(prefix);
                        int i2 = bucket;
                        if (iArr.length > 1) {
                            DumpUtils.printScreenLabel(printWriter, printedScreen != iscreen ? iscreen : -1);
                            printedScreen = iscreen;
                        }
                        if (iArr2.length > 1) {
                            DumpUtils.printMemLabel(printWriter, printedMem != imem ? imem : -1, '/');
                            printedMem = imem;
                        }
                        printWriter.print(DumpUtils.STATE_NAMES[iArr3[ip]]);
                        printWriter.print(": ");
                        TimeUtils.formatDuration(time, printWriter);
                        printWriter.println(running);
                        totalTime2 += time;
                    }
                    ip++;
                    processState = this;
                }
                im++;
                processState = this;
            }
            is++;
            totalTime = totalTime2;
            processState = this;
        }
        if (totalTime != 0) {
            pw.print(prefix);
            if (iArr.length > 1) {
                i = -1;
                DumpUtils.printScreenLabel(printWriter, -1);
            } else {
                i = -1;
            }
            if (iArr2.length > 1) {
                DumpUtils.printMemLabel(printWriter, i, '/');
            }
            printWriter.print("TOTAL  : ");
            TimeUtils.formatDuration(totalTime, printWriter);
            pw.println();
        }
    }

    public void dumpPss(PrintWriter pw, String prefix, int[] screenStates, int[] memStates, int[] procStates) {
        PrintWriter printWriter = pw;
        int[] iArr = screenStates;
        int[] iArr2 = memStates;
        int[] iArr3 = procStates;
        int printedScreen = -1;
        boolean printedScreen2 = false;
        int is = 0;
        while (is < iArr.length) {
            int printedMem = -1;
            int printedScreen3 = printedScreen;
            boolean printedScreen4 = printedScreen2;
            int im = 0;
            while (im < iArr2.length) {
                int printedMem2 = printedMem;
                boolean printedHeader = printedScreen4;
                int ip = 0;
                while (ip < iArr3.length) {
                    int iscreen = iArr[is];
                    int imem = iArr2[im];
                    int bucket = ((iscreen + imem) * 14) + iArr3[ip];
                    long count = getPssSampleCount(bucket);
                    if (count > 0) {
                        if (!printedHeader) {
                            pw.print(prefix);
                            boolean z = printedHeader;
                            printWriter.print("PSS/USS (");
                            printWriter.print(this.mPssTable.getKeyCount());
                            printWriter.println(" entries):");
                            printedHeader = true;
                        } else {
                            boolean z2 = printedHeader;
                        }
                        pw.print(prefix);
                        boolean printedHeader2 = printedHeader;
                        printWriter.print("  ");
                        if (iArr.length > 1) {
                            DumpUtils.printScreenLabel(printWriter, printedScreen3 != iscreen ? iscreen : -1);
                            printedScreen3 = iscreen;
                        }
                        if (iArr2.length > 1) {
                            DumpUtils.printMemLabel(printWriter, printedMem2 != imem ? imem : -1, '/');
                            printedMem2 = imem;
                        }
                        printWriter.print(DumpUtils.STATE_NAMES[iArr3[ip]]);
                        printWriter.print(": ");
                        printWriter.print(count);
                        printWriter.print(" samples ");
                        DebugUtils.printSizeValue(printWriter, getPssMinimum(bucket) * 1024);
                        printWriter.print(" ");
                        DebugUtils.printSizeValue(printWriter, getPssAverage(bucket) * 1024);
                        printWriter.print(" ");
                        DebugUtils.printSizeValue(printWriter, getPssMaximum(bucket) * 1024);
                        printWriter.print(" / ");
                        DebugUtils.printSizeValue(printWriter, getPssUssMinimum(bucket) * 1024);
                        printWriter.print(" ");
                        DebugUtils.printSizeValue(printWriter, getPssUssAverage(bucket) * 1024);
                        printWriter.print(" ");
                        DebugUtils.printSizeValue(printWriter, getPssUssMaximum(bucket) * 1024);
                        printWriter.print(" / ");
                        DebugUtils.printSizeValue(printWriter, getPssRssMinimum(bucket) * 1024);
                        printWriter.print(" ");
                        DebugUtils.printSizeValue(printWriter, getPssRssAverage(bucket) * 1024);
                        printWriter.print(" ");
                        DebugUtils.printSizeValue(printWriter, getPssRssMaximum(bucket) * 1024);
                        pw.println();
                        printedHeader = printedHeader2;
                    } else {
                        boolean z3 = printedHeader;
                    }
                    ip++;
                    iArr = screenStates;
                    iArr2 = memStates;
                }
                boolean printedHeader3 = printedHeader;
                im++;
                printedMem = printedMem2;
                printedScreen4 = printedHeader3;
                iArr = screenStates;
                iArr2 = memStates;
            }
            is++;
            printedScreen2 = printedScreen4;
            printedScreen = printedScreen3;
            iArr = screenStates;
            iArr2 = memStates;
        }
        if (this.mNumExcessiveCpu != 0) {
            pw.print(prefix);
            printWriter.print("Killed for excessive CPU use: ");
            printWriter.print(this.mNumExcessiveCpu);
            printWriter.println(" times");
        }
        if (this.mNumCachedKill != 0) {
            pw.print(prefix);
            printWriter.print("Killed from cached state: ");
            printWriter.print(this.mNumCachedKill);
            printWriter.print(" times from pss ");
            DebugUtils.printSizeValue(printWriter, this.mMinCachedKillPss * 1024);
            printWriter.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
            DebugUtils.printSizeValue(printWriter, this.mAvgCachedKillPss * 1024);
            printWriter.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
            DebugUtils.printSizeValue(printWriter, this.mMaxCachedKillPss * 1024);
            pw.println();
        }
    }

    private void dumpProcessSummaryDetails(PrintWriter pw, String prefix, String label, int[] screenStates, int[] memStates, int[] procStates, long now, long totalTime, boolean full) {
        PrintWriter printWriter = pw;
        String str = label;
        long j = totalTime;
        ProcessStats.ProcessDataCollection totals = new ProcessStats.ProcessDataCollection(screenStates, memStates, procStates);
        computeProcessData(totals, now);
        if ((((double) totals.totalTime) / ((double) j)) * 100.0d >= 0.005d || totals.numPss != 0) {
            if (prefix != null) {
                pw.print(prefix);
            }
            if (str != null) {
                printWriter.print(str);
            }
            totals.print(printWriter, j, full);
            if (prefix != null) {
                pw.println();
                return;
            }
            return;
        }
        boolean z = full;
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

    public void computeProcessData(ProcessStats.ProcessDataCollection data, long now) {
        int ip;
        int im;
        long j;
        int is;
        long avgPss;
        long maxUss;
        long minRss;
        ProcessStats.ProcessDataCollection processDataCollection = data;
        long j2 = 0;
        processDataCollection.totalTime = 0;
        processDataCollection.maxRss = 0;
        processDataCollection.avgRss = 0;
        processDataCollection.minRss = 0;
        processDataCollection.maxUss = 0;
        processDataCollection.avgUss = 0;
        processDataCollection.minUss = 0;
        processDataCollection.maxPss = 0;
        processDataCollection.avgPss = 0;
        processDataCollection.minPss = 0;
        processDataCollection.numPss = 0;
        int is2 = 0;
        while (is2 < processDataCollection.screenStates.length) {
            int im2 = 0;
            while (im2 < processDataCollection.memStates.length) {
                int ip2 = 0;
                while (ip2 < processDataCollection.procStates.length) {
                    int bucket = ((processDataCollection.screenStates[is2] + processDataCollection.memStates[im2]) * 14) + processDataCollection.procStates[ip2];
                    processDataCollection.totalTime += getDuration(bucket, now);
                    long samples = getPssSampleCount(bucket);
                    if (samples > j2) {
                        long minPss = getPssMinimum(bucket);
                        is = is2;
                        long avgPss2 = getPssAverage(bucket);
                        long maxPss = getPssMaximum(bucket);
                        long minUss = getPssUssMinimum(bucket);
                        im = im2;
                        ip = ip2;
                        long avgUss = getPssUssAverage(bucket);
                        long samples2 = samples;
                        long maxUss2 = getPssUssMaximum(bucket);
                        long minRss2 = getPssRssMinimum(bucket);
                        long avgRss = getPssRssAverage(bucket);
                        int i = bucket;
                        long maxRss = getPssRssMaximum(bucket);
                        j = 0;
                        if (processDataCollection.numPss == 0) {
                            processDataCollection.minPss = minPss;
                            processDataCollection.avgPss = avgPss2;
                            processDataCollection.maxPss = maxPss;
                            processDataCollection.minUss = minUss;
                            processDataCollection.avgUss = avgUss;
                            long maxUss3 = maxUss2;
                            processDataCollection.maxUss = maxUss3;
                            long maxUss4 = maxUss3;
                            long minRss3 = minRss2;
                            processDataCollection.minRss = minRss3;
                            long minRss4 = minRss3;
                            long minRss5 = avgRss;
                            processDataCollection.avgRss = minRss5;
                            long avgRss2 = minRss5;
                            long maxRss2 = maxRss;
                            processDataCollection.maxRss = maxRss2;
                            long j3 = maxPss;
                            long j4 = avgPss2;
                            long maxPss2 = maxRss2;
                            long j5 = minPss;
                            avgPss = samples2;
                            long j6 = maxUss4;
                            long j7 = minRss4;
                            long j8 = avgRss2;
                            long maxRss3 = avgUss;
                        } else {
                            long maxUss5 = maxUss2;
                            long minRss6 = minRss2;
                            long avgRss3 = avgRss;
                            long maxRss4 = maxRss;
                            if (minPss < processDataCollection.minPss) {
                                processDataCollection.minPss = minPss;
                            }
                            long j9 = minPss;
                            double d = (double) avgPss2;
                            long j10 = avgPss2;
                            long avgUss2 = avgUss;
                            avgPss = samples2;
                            processDataCollection.avgPss = (long) (((((double) processDataCollection.avgPss) * ((double) processDataCollection.numPss)) + (d * ((double) avgPss))) / ((double) (processDataCollection.numPss + avgPss)));
                            if (maxPss > processDataCollection.maxPss) {
                                processDataCollection.maxPss = maxPss;
                            }
                            if (minUss < processDataCollection.minUss) {
                                processDataCollection.minUss = minUss;
                            }
                            long j11 = maxPss;
                            processDataCollection.avgUss = (long) (((((double) processDataCollection.avgUss) * ((double) processDataCollection.numPss)) + (((double) avgUss2) * ((double) avgPss))) / ((double) (processDataCollection.numPss + avgPss)));
                            if (maxUss5 > processDataCollection.maxUss) {
                                maxUss = maxUss5;
                                processDataCollection.maxUss = maxUss;
                            } else {
                                maxUss = maxUss5;
                            }
                            if (minRss6 < processDataCollection.minRss) {
                                minRss = minRss6;
                                processDataCollection.minRss = minRss;
                            } else {
                                minRss = minRss6;
                            }
                            long j12 = maxUss;
                            long j13 = minRss;
                            long avgRss4 = avgRss3;
                            long j14 = avgRss4;
                            processDataCollection.avgRss = (long) (((((double) processDataCollection.avgRss) * ((double) processDataCollection.numPss)) + (((double) avgRss4) * ((double) avgPss))) / ((double) (processDataCollection.numPss + avgPss)));
                            if (maxRss4 > processDataCollection.maxRss) {
                                processDataCollection.maxRss = maxRss4;
                            } else {
                                long j15 = maxRss4;
                            }
                        }
                        processDataCollection.numPss += avgPss;
                    } else {
                        j = j2;
                        is = is2;
                        im = im2;
                        ip = ip2;
                    }
                    ip2 = ip + 1;
                    is2 = is;
                    j2 = j;
                    im2 = im;
                }
                long j16 = j2;
                int i2 = is2;
                im2++;
            }
            long j17 = j2;
            is2++;
        }
    }

    public void dumpCsv(PrintWriter pw, boolean sepScreenStates, int[] screenStates, boolean sepMemStates, int[] memStates, boolean sepProcStates, int[] procStates, long now) {
        int NSS;
        int NSS2;
        int iss;
        PrintWriter printWriter = pw;
        int[] iArr = screenStates;
        int[] iArr2 = memStates;
        int[] iArr3 = procStates;
        int NSS3 = sepScreenStates ? iArr.length : 1;
        int NMS = sepMemStates ? iArr2.length : 1;
        int NPS = sepProcStates ? iArr3.length : 1;
        int iss2 = 0;
        while (iss2 < NSS3) {
            int ims = 0;
            while (ims < NMS) {
                int ips = 0;
                while (ips < NPS) {
                    int vsscreen = sepScreenStates ? iArr[iss2] : 0;
                    int vsmem = sepMemStates ? iArr2[ims] : 0;
                    int vsproc = sepProcStates ? iArr3[ips] : 0;
                    int NSA = sepScreenStates ? 1 : iArr.length;
                    int NMA = sepMemStates ? 1 : iArr2.length;
                    if (sepProcStates) {
                        NSS = NSS3;
                        NSS2 = 1;
                    } else {
                        NSS = NSS3;
                        NSS2 = iArr3.length;
                    }
                    int NMS2 = NMS;
                    int NPS2 = NPS;
                    long totalTime = 0;
                    int isa = 0;
                    while (true) {
                        iss = iss2;
                        int isa2 = isa;
                        if (isa2 >= NSA) {
                            break;
                        }
                        long totalTime2 = totalTime;
                        int ima = 0;
                        while (ima < NMA) {
                            int ipa = 0;
                            while (ipa < NSS2) {
                                totalTime2 += getDuration(((vsscreen + (sepScreenStates ? 0 : iArr[isa2]) + vsmem + (sepMemStates ? 0 : iArr2[ima])) * 14) + vsproc + (sepProcStates ? 0 : iArr3[ipa]), now);
                                ipa++;
                                iArr = screenStates;
                                iArr2 = memStates;
                            }
                            long j = now;
                            ima++;
                            iArr = screenStates;
                            iArr2 = memStates;
                        }
                        long j2 = now;
                        int isa3 = isa2 + 1;
                        totalTime = totalTime2;
                        iss2 = iss;
                        iArr = screenStates;
                        iArr2 = memStates;
                        isa = isa3;
                    }
                    long j3 = now;
                    printWriter.print("\t");
                    printWriter.print(totalTime);
                    ips++;
                    NSS3 = NSS;
                    NMS = NMS2;
                    NPS = NPS2;
                    iss2 = iss;
                    iArr = screenStates;
                    iArr2 = memStates;
                }
                long j4 = now;
                int i = NSS3;
                int i2 = NMS;
                int i3 = NPS;
                int i4 = iss2;
                ims++;
                iArr = screenStates;
                iArr2 = memStates;
            }
            long j5 = now;
            int i5 = NSS3;
            int i6 = NMS;
            int i7 = NPS;
            iss2++;
            iArr = screenStates;
            iArr2 = memStates;
        }
        long j6 = now;
        int i8 = NSS3;
        int i9 = NMS;
        int i10 = NPS;
    }

    public void dumpPackageProcCheckin(PrintWriter pw, String pkgName, int uid, long vers, String itemName, long now) {
        pw.print("pkgproc,");
        pw.print(pkgName);
        pw.print(",");
        pw.print(uid);
        pw.print(",");
        pw.print(vers);
        pw.print(",");
        pw.print(DumpUtils.collapseString(pkgName, itemName));
        dumpAllStateCheckin(pw, now);
        pw.println();
        if (this.mPssTable.getKeyCount() > 0) {
            pw.print("pkgpss,");
            pw.print(pkgName);
            pw.print(",");
            pw.print(uid);
            pw.print(",");
            pw.print(vers);
            pw.print(",");
            pw.print(DumpUtils.collapseString(pkgName, itemName));
            dumpAllPssCheckin(pw);
            pw.println();
        }
        if (this.mNumExcessiveCpu > 0 || this.mNumCachedKill > 0) {
            pw.print("pkgkills,");
            pw.print(pkgName);
            pw.print(",");
            pw.print(uid);
            pw.print(",");
            pw.print(vers);
            pw.print(",");
            pw.print(DumpUtils.collapseString(pkgName, itemName));
            pw.print(",");
            pw.print("0");
            pw.print(",");
            pw.print(this.mNumExcessiveCpu);
            pw.print(",");
            pw.print(this.mNumCachedKill);
            pw.print(",");
            pw.print(this.mMinCachedKillPss);
            pw.print(":");
            pw.print(this.mAvgCachedKillPss);
            pw.print(":");
            pw.print(this.mMaxCachedKillPss);
            pw.println();
        }
    }

    public void dumpProcCheckin(PrintWriter pw, String procName, int uid, long now) {
        if (this.mDurations.getKeyCount() > 0) {
            pw.print("proc,");
            pw.print(procName);
            pw.print(",");
            pw.print(uid);
            dumpAllStateCheckin(pw, now);
            pw.println();
        }
        if (this.mPssTable.getKeyCount() > 0) {
            pw.print("pss,");
            pw.print(procName);
            pw.print(",");
            pw.print(uid);
            dumpAllPssCheckin(pw);
            pw.println();
        }
        if (this.mNumExcessiveCpu > 0 || this.mNumCachedKill > 0) {
            pw.print("kills,");
            pw.print(procName);
            pw.print(",");
            pw.print(uid);
            pw.print(",");
            pw.print("0");
            pw.print(",");
            pw.print(this.mNumExcessiveCpu);
            pw.print(",");
            pw.print(this.mNumCachedKill);
            pw.print(",");
            pw.print(this.mMinCachedKillPss);
            pw.print(":");
            pw.print(this.mAvgCachedKillPss);
            pw.print(":");
            pw.print(this.mMaxCachedKillPss);
            pw.println();
        }
    }

    public void dumpAllStateCheckin(PrintWriter pw, long now) {
        boolean didCurState = false;
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
            pw.print(',');
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
            pw.print(':');
            pw.print(this.mPssTable.getValue(key, 7));
            pw.print(':');
            pw.print(this.mPssTable.getValue(key, 8));
            pw.print(':');
            pw.print(this.mPssTable.getValue(key, 9));
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("ProcessState{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" ");
        sb.append(this.mName);
        sb.append("/");
        sb.append(this.mUid);
        sb.append(" pkg=");
        sb.append(this.mPackage);
        if (this.mMultiPackage) {
            sb.append(" (multi)");
        }
        if (this.mCommonProcess != this) {
            sb.append(" (sub)");
        }
        sb.append("}");
        return sb.toString();
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId, String procName, int uid, long now) {
        long j;
        int i;
        Map<Integer, Long> durationByState;
        Map<Integer, Long> durationByState2;
        ProtoOutputStream protoOutputStream = proto;
        long token = proto.start(fieldId);
        protoOutputStream.write(1138166333441L, procName);
        protoOutputStream.write(1120986464258L, uid);
        if (this.mNumExcessiveCpu > 0 || this.mNumCachedKill > 0) {
            long killToken = protoOutputStream.start(1146756268035L);
            protoOutputStream.write(1120986464257L, this.mNumExcessiveCpu);
            protoOutputStream.write(1120986464258L, this.mNumCachedKill);
            ProtoUtils.toAggStatsProto(protoOutputStream, 1146756268035L, this.mMinCachedKillPss, this.mAvgCachedKillPss, this.mMaxCachedKillPss);
            protoOutputStream.end(killToken);
        }
        Map<Integer, Long> durationByState3 = new HashMap<>();
        boolean didCurState = false;
        for (int i2 = 0; i2 < this.mDurations.getKeyCount(); i2++) {
            int key = this.mDurations.getKeyAt(i2);
            int type = SparseMappingTable.getIdFromKey(key);
            long time = this.mDurations.getValue(key);
            if (this.mCurState == type) {
                durationByState2 = durationByState3;
                time += now - this.mStartTime;
                didCurState = true;
            } else {
                durationByState2 = durationByState3;
            }
            durationByState3 = durationByState2;
            durationByState3.put(Integer.valueOf(type), Long.valueOf(time));
        }
        if (!didCurState && this.mCurState != -1) {
            durationByState3.put(Integer.valueOf(this.mCurState), Long.valueOf(now - this.mStartTime));
        }
        int i3 = 0;
        while (true) {
            int i4 = i3;
            j = 2246267895813L;
            if (i4 >= this.mPssTable.getKeyCount()) {
                break;
            }
            int key2 = this.mPssTable.getKeyAt(i4);
            int idFromKey = SparseMappingTable.getIdFromKey(key2);
            if (!durationByState3.containsKey(Integer.valueOf(idFromKey))) {
                i = i4;
                durationByState = durationByState3;
            } else {
                int i5 = idFromKey;
                long stateToken = protoOutputStream.start(2246267895813L);
                i = i4;
                DumpUtils.printProcStateTagProto(protoOutputStream, 1159641169921L, 1159641169922L, 1159641169923L, i5);
                int type2 = i5;
                long duration = durationByState3.get(Integer.valueOf(type2)).longValue();
                durationByState3.remove(Integer.valueOf(type2));
                protoOutputStream.write(1112396529668L, duration);
                int key3 = key2;
                protoOutputStream.write(1120986464261L, this.mPssTable.getValue(key3, 0));
                long j2 = duration;
                durationByState = durationByState3;
                int i6 = type2;
                int key4 = key3;
                ProtoUtils.toAggStatsProto(protoOutputStream, 1146756268038L, this.mPssTable.getValue(key3, 1), this.mPssTable.getValue(key3, 2), this.mPssTable.getValue(key3, 3));
                ProtoUtils.toAggStatsProto(protoOutputStream, 1146756268039L, this.mPssTable.getValue(key4, 4), this.mPssTable.getValue(key4, 5), this.mPssTable.getValue(key4, 6));
                ProtoUtils.toAggStatsProto(protoOutputStream, 1146756268040L, this.mPssTable.getValue(key4, 7), this.mPssTable.getValue(key4, 8), this.mPssTable.getValue(key4, 9));
                protoOutputStream.end(stateToken);
            }
            i3 = i + 1;
            durationByState3 = durationByState;
        }
        for (Map.Entry<Integer, Long> entry : durationByState3.entrySet()) {
            long stateToken2 = protoOutputStream.start(j);
            DumpUtils.printProcStateTagProto(protoOutputStream, 1159641169921L, 1159641169922L, 1159641169923L, entry.getKey().intValue());
            protoOutputStream.write(1112396529668L, entry.getValue().longValue());
            protoOutputStream.end(stateToken2);
            j = j;
        }
        protoOutputStream.end(token);
    }
}
