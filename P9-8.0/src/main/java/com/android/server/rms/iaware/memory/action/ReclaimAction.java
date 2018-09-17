package com.android.server.rms.iaware.memory.action;

import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.net.HwNetworkStatsService;
import com.android.server.rms.iaware.hiber.AppHibernateTask;
import com.android.server.rms.iaware.hiber.bean.HiberAppInfo;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import java.util.ArrayList;
import java.util.List;

public class ReclaimAction extends Action {
    private static final int BASE_RECLAIM_GAP = 2000;
    private static final int DEFAULT_RECLAIM_RATE = 30;
    private static final int MAX_RECLAIM_CNT = 3;
    private static final int MAX_RECLAIM_GAP = 1800000;
    private static final int MAX_RECLAIM_TIME = 1000;
    private static final int MIN_RECLAIM_UID = 10000;
    private static final String TAG = "AwareMem_Reclaim";
    private int mEmptyLoopCount;
    private long mLastReclaimTime;
    private long mReclaimGap;

    private static class ReclaimState {
        public long mBeginTime;
        public String mReasonCommon;
        public int mReclaimedProc;
        public long mRequestMemory;

        ReclaimState(Bundle extras) {
            String appName = extras.getString("appName");
            if (appName != null && appName.length() > 64) {
                appName = appName.substring(0, 63);
            }
            int event = extras.getInt("event");
            long timeStamp = extras.getLong("timeStamp");
            int cpuLoad = extras.getInt("cpuLoad");
            boolean cpuBusy = extras.getBoolean("cpuBusy");
            StringBuffer buffer = new StringBuffer(128);
            buffer.append("[");
            buffer.append(appName).append(",");
            buffer.append(event).append(",");
            buffer.append(timeStamp).append("],[");
            buffer.append(cpuLoad).append(",");
            buffer.append(cpuBusy).append("],[");
            this.mReasonCommon = buffer.toString();
            this.mRequestMemory = extras.getLong("reqMem");
            this.mReclaimedProc = 0;
            this.mBeginTime = System.currentTimeMillis();
        }
    }

    public ReclaimAction(Context context) {
        super(context);
        reset();
    }

    public boolean reqInterrupt(Bundle extras) {
        this.mInterrupt.set(true);
        return true;
    }

    private List<AwareProcessInfo> generateCompressList(List<AwareProcessBlockInfo> procsGroups) {
        if (procsGroups == null || procsGroups.isEmpty()) {
            AwareLog.w(TAG, "generateCompressList procsGroups error!");
            return null;
        }
        List<AwareProcessInfo> compressList = new ArrayList();
        ArrayMap<Integer, HiberAppInfo> historyMap = AppHibernateTask.getInstance().getRelaimedRecord();
        for (AwareProcessBlockInfo blockInfo : procsGroups) {
            if (blockInfo != null && blockInfo.mUid >= 10000) {
                List<AwareProcessInfo> processList = blockInfo.getProcessList();
                if (processList != null) {
                    for (AwareProcessInfo proc : processList) {
                        if (!(proc == null || proc.mProcInfo == null || Process.myPid() == proc.mPid)) {
                            ProcessInfo currentProcInfo = proc.mProcInfo;
                            if (!(currentProcInfo.mProcessName == null || currentProcInfo.mProcessName.contains("launcher") || currentProcInfo.mPackageName == null || 1 != currentProcInfo.mPackageName.size())) {
                                HiberAppInfo historyHiberAppInfo = historyMap == null ? null : (HiberAppInfo) historyMap.get(Integer.valueOf(currentProcInfo.mPid));
                                if (historyHiberAppInfo == null || currentProcInfo.mUid != historyHiberAppInfo.mUid || !currentProcInfo.mProcessName.equals(historyHiberAppInfo.mProcessName) || SystemClock.uptimeMillis() - historyHiberAppInfo.mReclaimTime >= AwareAppMngSort.PREVIOUS_APP_DIRCACTIVITY_DECAYTIME) {
                                    compressList.add(proc);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (compressList.isEmpty()) {
            compressList = null;
        }
        return compressList;
    }

    public int execute(Bundle extras) {
        if (extras == null) {
            AwareLog.w(TAG, "null extras!");
            return -1;
        }
        ReclaimState state = new ReclaimState(extras);
        AwareAppMngSortPolicy policy = MemoryUtils.getAppMngSortPolicy(1, 3);
        if (policy == null) {
            AwareLog.w(TAG, "getAppMngSortPolicy null policy!");
            return -1;
        }
        if (3 == reclaimProcessGroup(policy, 2, state)) {
            reclaimProcessGroup(policy, 1, state);
        }
        if (this.mInterrupt.get()) {
            return 0;
        }
        int i;
        updateReclaimGap(state.mReclaimedProc);
        this.mLastReclaimTime = SystemClock.elapsedRealtime();
        if (state.mReclaimedProc == 0) {
            i = this.mEmptyLoopCount + 1;
        } else {
            i = 0;
        }
        this.mEmptyLoopCount = i;
        return 0;
    }

    private int reclaimProcessGroup(AwareAppMngSortPolicy policy, int groupId, ReclaimState state) {
        List<AwareProcessInfo> compressList = generateCompressList(MemoryUtils.getAppMngProcGroup(policy, groupId));
        if (compressList == null || compressList.isEmpty()) {
            AwareLog.i(TAG, "reclaim process group " + groupId + " err!");
            return 3;
        }
        for (AwareProcessInfo proc : compressList) {
            if (this.mInterrupt.get()) {
                AwareLog.d(TAG, "Interrupted, return");
                return -1;
            } else if (proc == null || proc.mProcInfo == null) {
                AwareLog.w(TAG, "proc error skip!");
            } else {
                String procName = proc.mProcInfo.mProcessName;
                long uss = MemoryReader.getPssForPid(proc.mPid);
                if (uss <= 0) {
                    AwareLog.w(TAG, "getPssForPid error skip! procName=" + procName);
                } else {
                    long beginTime = System.currentTimeMillis();
                    int ret = AppHibernateTask.getInstance().reclaimApp(proc);
                    long endTime = System.currentTimeMillis();
                    if (ret < 0) {
                        AwareLog.d(TAG, "call hiber reclaimApp error skip! procName=" + procName);
                    } else {
                        long effect = (70 * uss) / 100;
                        int exeTime = (int) (endTime - beginTime);
                        EventTracker.getInstance().insertDumpData(beginTime, "Reclaim [" + proc.mPid + "," + procName + "]", exeTime, state.mReasonCommon + (state.mRequestMemory / 1024) + "," + (effect / 1024) + "]");
                        EventTracker.getInstance().insertStatisticData("Reclaim", exeTime, (int) (effect / 1024));
                        state.mReclaimedProc++;
                        state.mRequestMemory -= effect;
                        AwareLog.d(TAG, "reclaimed " + procName + "(" + proc.mPid + ") get " + effect + " kb memory");
                        if (1000 <= endTime - state.mBeginTime) {
                            return 0;
                        }
                        if (state.mRequestMemory <= 0 || 3 <= state.mReclaimedProc) {
                            return 0;
                        }
                    }
                }
            }
        }
        return 3;
    }

    public void reset() {
        this.mLastReclaimTime = 0;
        this.mEmptyLoopCount = 0;
        this.mReclaimGap = 2000;
    }

    public boolean canBeExecuted() {
        if (!AppHibernateTask.getInstance().isAppHiberEnabled()) {
            AwareLog.i(TAG, "canBeExecuted hibernation is not running!");
            this.mReclaimGap = HwNetworkStatsService.UPLOAD_INTERVAL;
            return false;
        } else if (SystemClock.elapsedRealtime() - this.mLastReclaimTime < this.mReclaimGap) {
            AwareLog.i(TAG, "canBeExecuted waiting next operation, interval=" + this.mReclaimGap);
            return false;
        } else if (MemoryReader.isZramOK()) {
            return true;
        } else {
            AwareLog.i(TAG, "canBeExecuted no zram space!");
            updateReclaimGap(0);
            return false;
        }
    }

    public int getLastExecFailCount() {
        return this.mEmptyLoopCount;
    }

    private void updateReclaimGap(int nReclaimed) {
        if (nReclaimed == 0) {
            long j;
            this.mReclaimGap *= 2;
            if (this.mReclaimGap > HwNetworkStatsService.UPLOAD_INTERVAL) {
                j = HwNetworkStatsService.UPLOAD_INTERVAL;
            } else {
                j = this.mReclaimGap;
            }
            this.mReclaimGap = j;
        } else {
            long availableRam = MemoryReader.getInstance().getMemAvailable();
            if (availableRam > 0) {
                long maxStep = MemoryConstant.getIdleMemory() - MemoryConstant.getCriticalMemory();
                if (maxStep <= 0) {
                    AwareLog.w(TAG, "Idle <= Emergency Memory! getIdleMemory=" + MemoryConstant.getIdleMemory() + ",getEmergencyMemory=" + MemoryConstant.getEmergencyMemory());
                    return;
                }
                long interval = (8000 * (availableRam - MemoryConstant.getCriticalMemory())) / maxStep;
                if (interval < 0) {
                    interval = 0;
                }
                this.mReclaimGap = 2000 + interval;
            } else {
                this.mReclaimGap = 2000;
            }
        }
    }
}
