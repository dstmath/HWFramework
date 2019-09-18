package com.android.server.rms.iaware.memory.utils;

import android.annotation.SuppressLint;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.text.TextUtils;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.rms.iaware.memory.utils.PackageStats;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class PackageTracker {
    private static int INDEX_KILL_AVG = 3;
    private static int INDEX_KILL_CUR = 2;
    private static int INDEX_KILL_MAX = 4;
    private static int INDEX_KILL_MINUTE = 1;
    static final long INTERVAL_DAY = 86400000;
    static final long INTERVAL_HOUR = 9000000;
    static final long INTERVAL_MINUTE = 30000;
    static final long INTERVAL_TRIGGER = 1800000;
    private static final String TAG = "AwareMem_PkgTracker";
    static final int[][] mKillThresHold = {new int[]{0, 2, 5, 30, 45}, new int[]{0, 2, 5, 30, 40}, new int[]{0, 2, 5, 20, 30}, new int[]{1, 2, 5, 20, 30}, new int[]{1, 2, 5, 20, 30}, new int[]{1, 2, 5, 20, 30}};
    private static final Object mLock = new Object();
    private static PackageTracker sPackageTracker;
    private boolean mDebug = false;
    private boolean mEnable = true;
    private PackageStats mPackageStats = new PackageStats();
    private long mlastTriggerTime = SystemClock.elapsedRealtime();

    public enum KilledFrequency {
        FREQUENCY_NORMAL,
        FREQUENCY_HIGH,
        FREQUENCY_CRITICAL
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

    @SuppressLint({"PreferForInArrayList"})
    public void trackKillEvent(int uid, List<AwareProcessInfo> processList) {
        if (isEnabled()) {
            List<String> pkgList = new ArrayList<>();
            long timeStamp = SystemClock.uptimeMillis();
            for (AwareProcessInfo info : processList) {
                if (!(info.mProcInfo == null || info.mProcInfo.mPackageName == null)) {
                    Iterator it = info.mProcInfo.mPackageName.iterator();
                    while (it.hasNext()) {
                        String packageName = (String) it.next();
                        if (this.mDebug) {
                            AwareLog.d(TAG, "trackKillEvent:" + packageName + ", uid:" + uid + ", process=" + info.mProcInfo.mProcessName);
                        }
                        if (!pkgList.contains(packageName)) {
                            pkgList.add(packageName);
                        }
                        addKillRecord(packageName, uid, info.mProcInfo.mProcessName, timeStamp);
                    }
                }
            }
            addPkgRecord(pkgList, uid, timeStamp);
        }
    }

    private void addPkgRecord(List<String> pkgList, int uid, long timeStamp) {
        for (String packageName : pkgList) {
            if (TextUtils.isEmpty(packageName)) {
                AwareLog.w(TAG, "addRecord, empty packageName, uid=" + uid + ", packageName=" + packageName);
            } else {
                long totalCount = addPkgRecordLocked(2, packageName, uid, timeStamp);
                if (this.mDebug) {
                    AwareLog.d(TAG, "addPkgRecord, reason=kill, totalCount=" + totalCount + ", packageName=" + packageName);
                }
            }
        }
    }

    private long addPkgRecordLocked(int recordValue, String packageName, int uid, long timeStamp) {
        long addPkgRecord;
        if (TextUtils.isEmpty(packageName)) {
            return 0;
        }
        synchronized (mLock) {
            addPkgRecord = this.mPackageStats.addPkgRecord(recordValue, packageName, uid, timeStamp);
        }
        return addPkgRecord;
    }

    private void addKillRecord(String packageName, int uid, String processName, long timeStamp) {
        addProcRecord(2, MemoryConstant.MEM_POLICY_KILLACTION, packageName, uid, processName, timeStamp);
    }

    public void addStartRecord(String reason, String packageName, int uid, String processName, long timeStamp) {
        if (isEnabled()) {
            int recordValue = 0;
            if ("restart".equalsIgnoreCase(reason)) {
                recordValue = 1;
            }
            int i = recordValue;
            addProcRecord(i, reason, packageName, uid, processName, timeStamp);
            long totalCount = addPkgRecordLocked(i, packageName, uid, timeStamp);
            if (this.mDebug) {
                AwareLog.d(TAG, "addPkgRecord, reason=start, totalCount=" + totalCount + ", packageName=" + packageName);
            }
        }
    }

    public void addExitRecord(String reason, String packageName, int uid, String processName, long timeStamp) {
        if (isEnabled()) {
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
                int i = recordValue;
                addProcRecord(i, reason, packageName, uid, processName, timeStamp);
                long totalCount = addPkgRecordLocked(i, packageName, uid, timeStamp);
                if (this.mDebug) {
                    AwareLog.d(TAG, "addPkgRecord, reason=exit, totalCount=" + totalCount + ", packageName=" + packageName);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x007f, code lost:
        checkTime(r14, " addProcRecord, reason=" + r2 + ", packageName=" + r10 + ", processName=" + r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x00a3, code lost:
        return;
     */
    private void addProcRecord(int recordValue, String reason, String packageName, int uid, String processName, long timeStamp) {
        String str = reason;
        String str2 = packageName;
        String str3 = processName;
        if (TextUtils.isEmpty(packageName)) {
            AwareLog.w(TAG, "addProcRecord, empty packageName, uid=" + uid + ", packageName=" + str2);
            return;
        }
        int i = uid;
        synchronized (mLock) {
            try {
                update(false);
                long startTime = SystemClock.elapsedRealtime();
                try {
                    long totalCount = this.mPackageStats.addProcRecord(recordValue, str2, i, str3, timeStamp);
                    if (this.mDebug) {
                        AwareLog.d(TAG, "addProcRecord, reason=" + str + ", totalCount=" + totalCount + ", packageName=" + str2 + ", processName=" + str3);
                    }
                } catch (Throwable th) {
                    th = th;
                    long j = startTime;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    private void update(boolean quickUpdated) {
        long nowTime = SystemClock.elapsedRealtime();
        if ((quickUpdated ? 30000 : 1800000) <= nowTime - this.mlastTriggerTime) {
            this.mPackageStats.cleanRange(SystemClock.uptimeMillis(), INTERVAL_HOUR);
            checkTime(nowTime, " cleanRange");
            this.mPackageStats.calcMax();
            checkTime(nowTime, " calcMax");
            this.mlastTriggerTime = nowTime;
        }
    }

    public KilledFrequency getPackageKilledFrequency(String packageName, int uid) {
        if (!this.mEnable) {
            return KilledFrequency.FREQUENCY_NORMAL;
        }
        synchronized (mLock) {
            PackageStats.PackageState state = this.mPackageStats.getPackageState(packageName, uid);
            if (state == null) {
                KilledFrequency killedFrequency = KilledFrequency.FREQUENCY_NORMAL;
                return killedFrequency;
            }
            if (this.mDebug) {
                AwareLog.d(TAG, "killed pkg:" + state.mPackageName + ", uid:" + state.mUid + ", size:" + state.mProcesses.size());
            }
            update(true);
            long now = SystemClock.uptimeMillis();
            int[] array = {2, 1, 0};
            int i = 0;
            while (true) {
                int i2 = i;
                if (i2 >= array.length) {
                    return KilledFrequency.FREQUENCY_NORMAL;
                }
                int index = array[i2];
                KilledFrequency frequency = compareRecordThreshold(state, index, mKillThresHold[index], now, 0);
                if (frequency != KilledFrequency.FREQUENCY_NORMAL) {
                    return frequency;
                }
                i = i2 + 1;
            }
        }
    }

    public KilledFrequency getProcessKilledFrequency(String packageName, String processName, int uid) {
        String str = processName;
        if (!this.mEnable) {
            return KilledFrequency.FREQUENCY_NORMAL;
        }
        synchronized (mLock) {
            try {
                try {
                    PackageStats.PackageState state = this.mPackageStats.getPackageState(packageName, uid);
                    if (state == null) {
                        KilledFrequency killedFrequency = KilledFrequency.FREQUENCY_NORMAL;
                        return killedFrequency;
                    }
                    PackageStats.ProcessState processState = state.mProcesses.get(str);
                    if (processState == null) {
                        KilledFrequency killedFrequency2 = KilledFrequency.FREQUENCY_NORMAL;
                        return killedFrequency2;
                    }
                    if (this.mDebug) {
                        AwareLog.d(TAG, "getProcessKilledFrequency:" + str + " " + state.mPackageName + ", uid:" + state.mUid);
                    }
                    update(true);
                    long now = SystemClock.uptimeMillis();
                    int[] array = {2, 1, 0};
                    int i = 0;
                    while (true) {
                        int i2 = i;
                        if (i2 >= array.length) {
                            return KilledFrequency.FREQUENCY_NORMAL;
                        }
                        int index = array[i2];
                        KilledFrequency frequency = compareRecordThreshold(processState.getRecordTable(), index, mKillThresHold[index], now, 1);
                        if (frequency != KilledFrequency.FREQUENCY_NORMAL) {
                            return frequency;
                        }
                        i = i2 + 1;
                    }
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                String str2 = packageName;
                int i3 = uid;
                throw th;
            }
        }
    }

    private KilledFrequency compareRecordThreshold(RecordTable state, int record, int[] threshold, long now, int processSize) {
        RecordTable recordTable = state;
        int i = record;
        int processSize2 = processSize > 0 ? processSize : 1;
        List<Long> list = recordTable.mRecordTimeStamps[i].mTimeStamps;
        int criticalCount = 0;
        int i2 = list.size() - 1;
        while (true) {
            int i3 = i2;
            if (i3 < 0 || 30000 * ((long) state.getScaleTimes()) < now - list.get(i3).longValue()) {
            } else {
                criticalCount++;
                i2 = i3 - 1;
            }
        }
        if (threshold[INDEX_KILL_MINUTE] <= criticalCount / processSize2) {
            if (this.mDebug) {
                AwareLog.d(TAG, "compareThresHold critical, index=" + i + ", critical Count=" + criticalCount + ", timeScale=" + state.getScaleTimes());
            }
            state.updateScaleTimes();
            return KilledFrequency.FREQUENCY_CRITICAL;
        }
        if (threshold[INDEX_KILL_CUR] <= list.size() / processSize2) {
            if (this.mDebug) {
                AwareLog.d(TAG, "compareThresHold high, index=" + i + ", curCount=" + curCount);
            }
            return KilledFrequency.FREQUENCY_HIGH;
        }
        long firstTime = recordTable.mFirstTimes[i];
        long totalCount = recordTable.mTotalRecordCounts[i];
        long avgCount = totalCount / ((long) processSize2);
        if (86400000 < now - firstTime) {
            avgCount = (86400000 * totalCount) / (now - firstTime);
            if (((long) threshold[INDEX_KILL_AVG]) <= avgCount) {
                if (this.mDebug) {
                    StringBuilder sb = new StringBuilder();
                    ArrayList<Long> arrayList = list;
                    sb.append("compareThresHold high, index=");
                    sb.append(i);
                    sb.append(", avgCount=");
                    sb.append(avgCount);
                    AwareLog.d(TAG, sb.toString());
                } else {
                    List<Long> timeList = list;
                }
                return KilledFrequency.FREQUENCY_HIGH;
            }
        }
        List<Long> timeList2 = list;
        long maxCount = recordTable.mMaxRecordCounts[i];
        long j = firstTime;
        if (((long) threshold[INDEX_KILL_MAX]) <= (maxCount == 0 ? recordTable.mDayRecordCounts[i] : maxCount) / ((long) processSize2)) {
            if (this.mDebug) {
                AwareLog.d(TAG, "compareThresHold high, index=" + i + ", maxCount=" + maxCount);
            }
            return KilledFrequency.FREQUENCY_HIGH;
        }
        if (this.mDebug) {
            AwareLog.d(TAG, "compareThresHold normal, index=" + i + ", curCount=" + curCount + ", avgCount=" + avgCount + ", maxCount=" + maxCount + ", criticalCount=" + criticalCount + ", processSize=" + processSize2);
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
