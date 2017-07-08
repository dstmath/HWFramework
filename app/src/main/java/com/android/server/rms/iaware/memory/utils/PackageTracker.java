package com.android.server.rms.iaware.memory.utils;

import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.text.TextUtils;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import java.util.List;

public final class PackageTracker {
    private static int INDEX_KILL_AVG = 0;
    private static int INDEX_KILL_CUR = 0;
    private static int INDEX_KILL_MAX = 0;
    private static int INDEX_KILL_MINUTE = 0;
    static final long INTERVAL_DAY = 86400000;
    static final long INTERVAL_HOUR = 9000000;
    static final long INTERVAL_MINUTE = 30000;
    static final long INTERVAL_TRIGGER = 1800000;
    private static final String TAG = "AwareMem_PkgTracker";
    static final int[][] mKillThresHold = null;
    private static final Object mLock = null;
    private static PackageTracker sPackageTracker;
    private boolean mDebug;
    private boolean mEnable;
    private PackageStats mPackageStats;
    private long mlastTriggerTime;

    public enum KilledFrequency {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.memory.utils.PackageTracker.KilledFrequency.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.memory.utils.PackageTracker.KilledFrequency.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.memory.utils.PackageTracker.KilledFrequency.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.memory.utils.PackageTracker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.memory.utils.PackageTracker.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.memory.utils.PackageTracker.<clinit>():void");
    }

    public PackageTracker() {
        this.mDebug = false;
        this.mEnable = true;
        this.mPackageStats = new PackageStats();
        this.mlastTriggerTime = SystemClock.elapsedRealtime();
    }

    public static PackageTracker getInstance() {
        PackageTracker packageTracker;
        synchronized (mLock) {
            if (sPackageTracker == null) {
                sPackageTracker = new PackageTracker();
            }
            packageTracker = sPackageTracker;
        }
        return packageTracker;
    }

    public boolean isEnabled() {
        return this.mEnable;
    }

    public void trackKillEvent(int uid, List<AwareProcessInfo> processList) {
        if (getInstance().isEnabled()) {
            long timeStamp = SystemClock.uptimeMillis();
            for (AwareProcessInfo info : processList) {
                if (!(info.mProcInfo == null || info.mProcInfo.mPackageName == null)) {
                    for (String packageName : info.mProcInfo.mPackageName) {
                        getInstance().addKillRecord(packageName, uid, info.mProcInfo.mProcessName, timeStamp);
                    }
                }
            }
        }
    }

    public void addKillRecord(String packageName, int uid, String processName, long timeStamp) {
        addRecord(2, MemoryConstant.MEM_POLICY_KILLACTION, packageName, uid, processName, timeStamp);
    }

    public void addStartRecord(String reason, String packageName, int uid, String processName, long timeStamp) {
        int recordValue = 0;
        if ("restart".equalsIgnoreCase(reason)) {
            recordValue = 1;
        }
        addRecord(recordValue, reason, packageName, uid, processName, timeStamp);
    }

    public void addExitRecord(String reason, String packageName, int uid, String processName, long timeStamp) {
        int recordValue = -1;
        if ("died".equalsIgnoreCase(reason)) {
            recordValue = 3;
        }
        if ("crash".equalsIgnoreCase(reason)) {
            recordValue = 4;
        }
        if ("anr".equalsIgnoreCase(reason)) {
            recordValue = 5;
        }
        if (recordValue > -1) {
            addRecord(recordValue, reason, packageName, uid, processName, timeStamp);
        }
    }

    private void addRecord(int recordValue, String reason, String packageName, int uid, String processName, long timeStamp) {
        if (!this.mEnable) {
            return;
        }
        if (TextUtils.isEmpty(packageName)) {
            AwareLog.w(TAG, "addRecord, empty packageName, uid=" + uid + ", packageName=" + packageName);
            return;
        }
        long startTime;
        synchronized (mLock) {
            update(false);
            startTime = SystemClock.elapsedRealtime();
            long totalCount = this.mPackageStats.addRecord(recordValue, packageName, uid, processName, timeStamp);
            if (this.mDebug) {
                AwareLog.d(TAG, "addRecord, reason=" + reason + ", totalCount=" + totalCount + ", packageName=" + packageName + ", processName=" + processName);
            }
        }
        checkTime(startTime, " addRecord, reason=" + reason + ", packageName=" + packageName + ", processName=" + processName);
    }

    private void update(boolean quickUpdated) {
        long nowTime = SystemClock.elapsedRealtime();
        if ((quickUpdated ? INTERVAL_MINUTE : INTERVAL_TRIGGER) <= nowTime - this.mlastTriggerTime) {
            this.mPackageStats.cleanRange(SystemClock.uptimeMillis(), INTERVAL_HOUR);
            checkTime(nowTime, " cleanRange");
            this.mPackageStats.calcMax();
            checkTime(nowTime, " calcMax");
            this.mlastTriggerTime = nowTime;
        }
    }

    public boolean isPackageCriticalFrequency(String packageName, int uid) {
        return getPackageKilledFrequency(packageName, uid) == KilledFrequency.FREQUENCY_CRITICAL;
    }

    public boolean isPackageHighFrequency(String packageName, int uid) {
        return getPackageKilledFrequency(packageName, uid) == KilledFrequency.FREQUENCY_HIGH;
    }

    private KilledFrequency getPackageKilledFrequency(String packageName, int uid) {
        if (!this.mEnable) {
            return KilledFrequency.FREQUENCY_NORMAL;
        }
        synchronized (mLock) {
            PackageState state = this.mPackageStats.getPackageState(packageName, uid);
            if (state == null) {
                return KilledFrequency.FREQUENCY_NORMAL;
            }
            if (this.mDebug) {
                AwareLog.d(TAG, "getPackageKilledFrequency:" + state.mPackageName + ", uid:" + state.mUid);
            }
            update(true);
            long now = SystemClock.uptimeMillis();
            int[] array = new int[]{2, 1, 0};
            for (int index : array) {
                KilledFrequency frequency = compareRecordThreshold(state, index, mKillThresHold[index], now, state.mProcesses.size());
                if (frequency != KilledFrequency.FREQUENCY_NORMAL) {
                    return frequency;
                }
            }
            return KilledFrequency.FREQUENCY_NORMAL;
        }
    }

    public boolean isProcessCriticalFrequency(String packageName, String processName, int uid) {
        return getProcessKilledFrequency(packageName, processName, uid) == KilledFrequency.FREQUENCY_CRITICAL;
    }

    public boolean isProcessHighFrequency(String packageName, String processName, int uid) {
        return getProcessKilledFrequency(packageName, processName, uid) == KilledFrequency.FREQUENCY_HIGH;
    }

    private KilledFrequency getProcessKilledFrequency(String packageName, String processName, int uid) {
        if (!this.mEnable) {
            return KilledFrequency.FREQUENCY_NORMAL;
        }
        synchronized (mLock) {
            PackageState state = this.mPackageStats.getPackageState(packageName, uid);
            if (state == null) {
                return KilledFrequency.FREQUENCY_NORMAL;
            }
            ProcessState processState = (ProcessState) state.mProcesses.get(processName);
            if (processState == null) {
                return KilledFrequency.FREQUENCY_NORMAL;
            }
            if (this.mDebug) {
                AwareLog.d(TAG, "getProcessKilledFrequency:" + processName + " " + state.mPackageName + ", uid:" + state.mUid);
            }
            update(true);
            long now = SystemClock.uptimeMillis();
            int[] array = new int[]{2, 1, 0};
            for (int index : array) {
                KilledFrequency frequency = compareRecordThreshold(processState.getRecordTable(), index, mKillThresHold[index], now, 1);
                if (frequency != KilledFrequency.FREQUENCY_NORMAL) {
                    return frequency;
                }
            }
            return KilledFrequency.FREQUENCY_NORMAL;
        }
    }

    private KilledFrequency compareRecordThreshold(RecordTable state, int record, int[] threshold, long now, int processSize) {
        if (processSize <= 0) {
            processSize = 1;
        }
        List<Long> timeList = state.mRecordTimeStamps[record].mTimeStamps;
        int criticalCount = 0;
        for (int i = timeList.size() - 1; i >= 0; i--) {
            if (((long) state.getScaleTimes()) * INTERVAL_MINUTE < now - ((Long) timeList.get(i)).longValue()) {
                break;
            }
            criticalCount++;
        }
        criticalCount /= processSize;
        if (threshold[INDEX_KILL_MINUTE] <= criticalCount) {
            if (this.mDebug) {
                AwareLog.d(TAG, "compareThresHold critical, index=" + record + ", critical Count=" + criticalCount + ", timeScale=" + state.getScaleTimes());
            }
            state.updateScaleTimes();
            return KilledFrequency.FREQUENCY_CRITICAL;
        }
        int curCount = timeList.size() / processSize;
        if (threshold[INDEX_KILL_CUR] <= curCount) {
            if (this.mDebug) {
                AwareLog.d(TAG, "compareThresHold high, index=" + record + ", curCount=" + curCount);
            }
            return KilledFrequency.FREQUENCY_HIGH;
        }
        long firstTime = state.mFirstTimes[record];
        long totalCount = state.mTotalRecordCounts[record];
        long avgCount = totalCount / ((long) processSize);
        if (INTERVAL_DAY < now - firstTime) {
            avgCount = (INTERVAL_DAY * totalCount) / (now - firstTime);
            if (((long) threshold[INDEX_KILL_AVG]) <= avgCount) {
                if (this.mDebug) {
                    AwareLog.d(TAG, "compareThresHold high, index=" + record + ", avgCount=" + avgCount);
                }
                return KilledFrequency.FREQUENCY_HIGH;
            }
        }
        long maxCount = state.mMaxRecordCounts[record];
        if (maxCount == 0) {
            maxCount = state.mDayRecordCounts[record];
        }
        maxCount /= (long) processSize;
        if (((long) threshold[INDEX_KILL_MAX]) <= maxCount) {
            if (this.mDebug) {
                AwareLog.d(TAG, "compareThresHold high, index=" + record + ", maxCount=" + maxCount);
            }
            return KilledFrequency.FREQUENCY_HIGH;
        }
        if (this.mDebug) {
            AwareLog.d(TAG, "compareThresHold normal, index=" + record + ", curCount=" + curCount + ", avgCount=" + avgCount + ", maxCount=" + maxCount + ", criticalCount=" + criticalCount + ", processSize=" + processSize);
        }
        return KilledFrequency.FREQUENCY_NORMAL;
    }

    private void checkTime(long startTime, String action) {
        long now = SystemClock.elapsedRealtime();
        if (now - startTime > 10) {
            AwareLog.w(TAG, "[" + action + "] takes too much time:" + (now - startTime));
        }
    }
}
