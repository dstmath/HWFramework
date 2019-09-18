package com.android.server.rms.iaware.memory.action;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.am.ProcessRecord;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.net.HwNetworkStatsService;
import com.android.server.rms.iaware.hiber.AppHibernateTask;
import com.android.server.rms.iaware.hiber.bean.HiberAppInfo;
import com.android.server.rms.iaware.memory.data.handle.DataAppHandle;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import java.util.ArrayList;
import java.util.List;

public class ReclaimAction extends Action {
    private static final int BASE_RECLAIM_GAP = 2000;
    private static final int DEFAULT_RECLAIM_RATE = 30;
    private static final int ENHANCE_RECLAIM_GAP = 600000;
    private static final int MAX_RECLAIM_CNT = 3;
    private static final int MAX_RECLAIM_GAP = 1800000;
    private static final int MAX_RECLAIM_TIME = 1000;
    private static final int MIN_RECLAIM_UID = 10000;
    private static final String TAG = "AwareMem_Reclaim";
    private int mEmptyLoopCount = 0;
    private long mLastReclaimTime = 0;
    private long mReclaimGap = 2000;

    private static class ReclaimRunnable implements Runnable {
        private ProcessRecord cameraApp;

        public ReclaimRunnable(ProcessRecord app) {
            this.cameraApp = app;
        }

        public void run() {
            if (this.cameraApp == null || this.cameraApp.pid <= 0) {
                return;
            }
            if (DataAppHandle.getInstance().createBundleFromAppInfo().getInt("appUid") == this.cameraApp.uid) {
                AwareLog.d(ReclaimAction.TAG, "do not reclaim foregroud app!");
            } else {
                MemoryUtils.reclaimProcessAll(this.cameraApp.pid, false);
            }
        }
    }

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
            buffer.append(appName);
            buffer.append(",");
            buffer.append(event);
            buffer.append(",");
            buffer.append(timeStamp);
            buffer.append("],[");
            buffer.append(cpuLoad);
            buffer.append(",");
            buffer.append(cpuBusy);
            buffer.append("],[");
            this.mReasonCommon = buffer.toString();
            this.mRequestMemory = extras.getLong("reqMem");
            this.mReclaimedProc = 0;
            this.mBeginTime = System.currentTimeMillis();
        }
    }

    public ReclaimAction(Context context) {
        super(context);
    }

    public boolean reqInterrupt(Bundle extras) {
        this.mInterrupt.set(true);
        return true;
    }

    private List<AwareProcessInfo> generateCompressList(List<AwareProcessBlockInfo> procsGroups) {
        int repeatReclaimTime;
        if (procsGroups == null || procsGroups.isEmpty()) {
            AwareLog.w(TAG, "generateCompressList procsGroups error!");
            return null;
        }
        List<AwareProcessInfo> compressList = new ArrayList<>();
        ArrayMap<Integer, HiberAppInfo> historyMap = AppHibernateTask.getInstance().getRelaimedRecord();
        boolean reclaimEnhanceSwitch = MemoryConstant.getReclaimEnhanceSwitch();
        if (reclaimEnhanceSwitch) {
            repeatReclaimTime = 300000;
        } else {
            repeatReclaimTime = 600000;
        }
        for (AwareProcessBlockInfo blockInfo : procsGroups) {
            if (blockInfo != null && (reclaimEnhanceSwitch || blockInfo.mUid >= 10000)) {
                List<AwareProcessInfo> processList = blockInfo.getProcessList();
                if (processList != null) {
                    for (AwareProcessInfo proc : processList) {
                        if (!(proc == null || proc.mProcInfo == null || Process.myPid() == proc.mPid)) {
                            ProcessInfo currentProcInfo = proc.mProcInfo;
                            if (currentProcInfo.mProcessName != null && !currentProcInfo.mProcessName.contains("launcher") && currentProcInfo.mPackageName != null && 1 == currentProcInfo.mPackageName.size()) {
                                HiberAppInfo historyHiberAppInfo = historyMap == null ? null : historyMap.get(Integer.valueOf(currentProcInfo.mPid));
                                if (historyHiberAppInfo == null || currentProcInfo.mUid != historyHiberAppInfo.mUid || !currentProcInfo.mProcessName.equals(historyHiberAppInfo.mProcessName) || SystemClock.uptimeMillis() - historyHiberAppInfo.mReclaimTime >= ((long) repeatReclaimTime)) {
                                    compressList.add(proc);
                                }
                            }
                        }
                    }
                }
            }
        }
        return compressList.isEmpty() ? null : compressList;
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
        int ret = reclaimProcessGroup(policy, 2, state);
        if (3 == ret) {
            ret = reclaimProcessGroup(policy, 1, state);
        }
        if (MemoryConstant.getReclaimEnhanceSwitch() && 3 == ret) {
            int ret2 = reclaimProcessGroup(policy, 0, state);
        }
        if (this.mInterrupt.get()) {
            return 0;
        }
        updateReclaimGap(state.mReclaimedProc);
        this.mLastReclaimTime = SystemClock.elapsedRealtime();
        this.mEmptyLoopCount = state.mReclaimedProc == 0 ? this.mEmptyLoopCount + 1 : 0;
        return 0;
    }

    private int reclaimProcessGroup(AwareAppMngSortPolicy policy, int groupId, ReclaimState state) {
        List<AwareProcessInfo> compressList;
        List<AwareProcessBlockInfo> procsGroups;
        int ret;
        ReclaimAction reclaimAction = this;
        ReclaimState reclaimState = state;
        int ret2 = 0;
        List<AwareProcessBlockInfo> procsGroups2 = MemoryUtils.getAppMngProcGroup(policy, groupId);
        List<AwareProcessInfo> compressList2 = reclaimAction.generateCompressList(procsGroups2);
        if (compressList2 == null) {
            List<AwareProcessInfo> list = compressList2;
        } else if (compressList2.isEmpty()) {
            List<AwareProcessBlockInfo> list2 = procsGroups2;
            List<AwareProcessInfo> list3 = compressList2;
        } else {
            for (AwareProcessInfo proc : compressList2) {
                if (reclaimAction.mInterrupt.get()) {
                    AwareLog.d(TAG, "Interrupted, return");
                    return -1;
                }
                if (proc == null) {
                    ret = ret2;
                    procsGroups = procsGroups2;
                    compressList = compressList2;
                } else if (proc.mProcInfo == null) {
                    ret = ret2;
                    procsGroups = procsGroups2;
                    compressList = compressList2;
                } else {
                    String procName = proc.mProcInfo.mProcessName;
                    long uss = MemoryReader.getPssForPid(proc.mPid);
                    if (uss <= 0) {
                        AwareLog.w(TAG, "getPssForPid error skip! procName=" + procName);
                        ret2 = ret2;
                    } else {
                        long beginTime = System.currentTimeMillis();
                        int ret3 = AppHibernateTask.getInstance().reclaimApp(proc);
                        long endTime = System.currentTimeMillis();
                        if (ret3 < 0) {
                            AwareLog.d(TAG, "call hiber reclaimApp error skip! procName=" + procName);
                            ret2 = ret3;
                        } else {
                            int ret4 = ret3;
                            String operation = "Reclaim [" + proc.mPid + "," + procName + "]";
                            long effect = (70 * uss) / 100;
                            StringBuilder sb = new StringBuilder();
                            sb.append(reclaimState.mReasonCommon);
                            procsGroups = procsGroups2;
                            sb.append(reclaimState.mRequestMemory / 1024);
                            sb.append(",");
                            sb.append(effect / 1024);
                            sb.append("]");
                            String reason = sb.toString();
                            int exeTime = (int) (endTime - beginTime);
                            EventTracker.getInstance().insertDumpData(beginTime, operation, exeTime, reason);
                            compressList = compressList2;
                            EventTracker.getInstance().insertStatisticData("Reclaim", exeTime, (int) (effect / 1024));
                            reclaimState.mReclaimedProc++;
                            reclaimState.mRequestMemory -= effect;
                            if (!MemoryConstant.isKernCompressEnable()) {
                                AwareLog.d(TAG, "reclaimed " + procName + "(" + proc.mPid + ") get " + effect + " kb memory");
                            }
                            if (1000 <= endTime - reclaimState.mBeginTime || reclaimState.mRequestMemory <= 0 || 3 <= reclaimState.mReclaimedProc) {
                                return 0;
                            }
                            String str = operation;
                            String str2 = reason;
                            ret2 = ret4;
                            procsGroups2 = procsGroups;
                            compressList2 = compressList;
                        }
                    }
                    reclaimAction = this;
                }
                AwareLog.w(TAG, "proc error skip!");
                ret2 = ret;
                procsGroups2 = procsGroups;
                compressList2 = compressList;
                reclaimAction = this;
            }
            int i = ret2;
            List<AwareProcessBlockInfo> list4 = procsGroups2;
            List<AwareProcessInfo> list5 = compressList2;
            return 3;
        }
        AwareLog.i(TAG, "reclaim process group " + groupId + " err!");
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
        }
        if (MemoryConstant.getReclaimEnhanceSwitch() && this.mReclaimGap > AwareAppMngSort.PREVIOUS_APP_DIRCACTIVITY_DECAYTIME) {
            this.mReclaimGap = AwareAppMngSort.PREVIOUS_APP_DIRCACTIVITY_DECAYTIME;
        }
        if (SystemClock.elapsedRealtime() - this.mLastReclaimTime < this.mReclaimGap) {
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
            this.mReclaimGap *= 2;
            long j = this.mReclaimGap;
            long j2 = HwNetworkStatsService.UPLOAD_INTERVAL;
            if (j <= HwNetworkStatsService.UPLOAD_INTERVAL) {
                j2 = this.mReclaimGap;
            }
            this.mReclaimGap = j2;
        } else {
            long availableRam = MemoryReader.getInstance().getMemAvailable();
            long interval = 0;
            if (availableRam > 0) {
                long maxStep = MemoryConstant.getIdleMemory() - MemoryConstant.getCriticalMemory();
                if (maxStep <= 0) {
                    AwareLog.w(TAG, "Idle <= Emergency Memory! getIdleMemory=" + MemoryConstant.getIdleMemory() + ",getEmergencyMemory=" + MemoryConstant.getEmergencyMemory());
                    return;
                }
                long interval2 = (8000 * (availableRam - MemoryConstant.getCriticalMemory())) / maxStep;
                if (interval2 >= 0) {
                    interval = interval2;
                }
                this.mReclaimGap = 2000 + interval;
            } else {
                this.mReclaimGap = 2000;
            }
        }
    }

    public static void reclaimProcessAll(Handler handler, ProcessRecord app) {
        handler.postDelayed(new ReclaimRunnable(app), (long) MemoryConstant.getCameraPreloadReclaimDelay());
    }
}
