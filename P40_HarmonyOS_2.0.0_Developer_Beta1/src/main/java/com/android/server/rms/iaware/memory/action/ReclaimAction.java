package com.android.server.rms.iaware.memory.action;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.am.ProcessRecordEx;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.rms.iaware.hiber.AppHibernateTask;
import com.android.server.rms.iaware.hiber.bean.HiberAppInfo;
import com.android.server.rms.iaware.memory.data.handle.DataAppHandle;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReclaimAction extends Action {
    private static final int BASE_RECLAIM_GAP = 2000;
    private static final int DEFAULT_RECLAIM_RATE = 30;
    private static final int DOUBLE_RECLAIM_GAP = 2;
    private static final int MAX_RECLAIM_CNT = 3;
    private static final int MAX_RECLAIM_GAP = 1800000;
    private static final int MAX_RECLAIM_TIME = 1000;
    private static final int MIN_RECLAIM_UID = 10000;
    private static final int RECLAIM_STATE_SB_SIZE = 128;
    private static final String TAG = "AwareMem_Reclaim";
    private int mEmptyLoopCount = 0;
    private long mLastReclaimTime = 0;
    private long mReclaimGap = 2000;

    public ReclaimAction(Context context) {
        super(context);
    }

    /* access modifiers changed from: private */
    public static class ReclaimState {
        private long mBeginTime;
        private String mReasonCommon;
        private int mReclaimedProc;
        private long mRequestMemory;

        static /* synthetic */ int access$008(ReclaimState x0) {
            int i = x0.mReclaimedProc;
            x0.mReclaimedProc = i + 1;
            return i;
        }

        ReclaimState(Bundle extras) {
            if (extras != null) {
                String appName = extras.getString("appName");
                if (appName != null && appName.length() > 64) {
                    appName = appName.substring(0, 63);
                }
                StringBuffer buffer = new StringBuffer(128);
                buffer.append("[");
                buffer.append(appName);
                buffer.append(",");
                int event = extras.getInt("event");
                long timeStamp = extras.getLong("timeStamp");
                int cpuLoad = extras.getInt("cpuLoad");
                boolean isCpuBusy = extras.getBoolean("cpuBusy");
                buffer.append(event);
                buffer.append(",");
                buffer.append(timeStamp);
                buffer.append("],[");
                buffer.append(cpuLoad);
                buffer.append(",");
                buffer.append(isCpuBusy);
                buffer.append("],[");
                this.mReasonCommon = buffer.toString();
                this.mRequestMemory = extras.getLong("reqMem");
                this.mReclaimedProc = 0;
                this.mBeginTime = System.currentTimeMillis();
            }
        }
    }

    @Override // com.android.server.rms.iaware.memory.action.Action
    public boolean reqInterrupt(Bundle extras) {
        this.mInterrupt.set(true);
        return true;
    }

    private List<AwareProcessInfo> generateCompressList(List<AwareProcessBlockInfo> procsGroups) {
        List<AwareProcessInfo> processList;
        if (procsGroups == null || procsGroups.isEmpty()) {
            AwareLog.w(TAG, "generateCompressList procsGroups error!");
            return new ArrayList();
        }
        List<AwareProcessInfo> compressList = new ArrayList<>();
        ArrayMap<Integer, HiberAppInfo> historyMap = AppHibernateTask.getInstance().getRelaimedRecord();
        for (AwareProcessBlockInfo blockInfo : procsGroups) {
            if (!(blockInfo == null || blockInfo.procUid < 10000 || (processList = blockInfo.getProcessList()) == null)) {
                Iterator<AwareProcessInfo> it = processList.iterator();
                while (it.hasNext()) {
                    AwareProcessInfo proc = it.next();
                    if (!(proc == null || proc.procProcInfo == null || Process.myPid() == proc.procPid)) {
                        ProcessInfo currentProcInfo = proc.procProcInfo;
                        if (currentProcInfo.mProcessName != null && !currentProcInfo.mProcessName.contains("launcher") && currentProcInfo.mPackageName != null && currentProcInfo.mPackageName.size() == 1) {
                            if (canBeReclaimed(historyMap == null ? null : historyMap.get(Integer.valueOf(currentProcInfo.mPid)), currentProcInfo)) {
                                compressList.add(proc);
                            }
                        }
                    }
                }
            }
        }
        return compressList;
    }

    private boolean canBeReclaimed(HiberAppInfo historyHiberAppInfo, ProcessInfo currentProcInfo) {
        return historyHiberAppInfo == null || currentProcInfo.mUid != historyHiberAppInfo.uid || !currentProcInfo.mProcessName.equals(historyHiberAppInfo.processName) || SystemClock.uptimeMillis() - historyHiberAppInfo.reclaimTime >= AwareAppMngSort.PREVIOUS_APP_DIRCACTIVITY_DECAYTIME;
    }

    @Override // com.android.server.rms.iaware.memory.action.Action
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
        if (reclaimProcessGroup(policy, 2, state) == 3) {
            reclaimProcessGroup(policy, 1, state);
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
        ReclaimAction reclaimAction = this;
        List<AwareProcessInfo> compressList = reclaimAction.generateCompressList(MemoryUtils.getAppMngProcGroup(policy, groupId));
        if (compressList == null) {
            return 3;
        }
        if (compressList.isEmpty()) {
            return 3;
        }
        Iterator<AwareProcessInfo> it = compressList.iterator();
        while (it.hasNext()) {
            AwareProcessInfo proc = it.next();
            if (reclaimAction.mInterrupt.get()) {
                return -1;
            }
            if (proc == null) {
                reclaimAction = this;
            } else if (proc.procProcInfo == null) {
                continue;
            } else {
                String procName = proc.procProcInfo.mProcessName;
                long uss = MemoryReader.getPssForPid(proc.procPid);
                if (uss <= 0) {
                    continue;
                } else {
                    long beginTime = System.currentTimeMillis();
                    int ret = AppHibernateTask.getInstance().reclaimApp(proc);
                    long endTime = System.currentTimeMillis();
                    if (ret >= 0) {
                        long effect = (70 * uss) / 100;
                        int exeTime = (int) (endTime - beginTime);
                        EventTracker.getInstance().insertDumpData(beginTime, "Reclaim [" + proc.procPid + "," + procName + "]", exeTime, state.mReasonCommon + (state.mRequestMemory / 1024) + "," + (effect / 1024) + "]");
                        EventTracker.getInstance().insertStatisticData("Reclaim", exeTime, (int) (effect / 1024));
                        ReclaimState.access$008(state);
                        state.mRequestMemory = state.mRequestMemory - effect;
                        if (!MemoryConstant.isKernCompressEnable() && AwareLog.getDebugLogSwitch()) {
                            AwareLog.d(TAG, "reclaimed " + procName + "(" + proc.procPid + ") get " + effect + " kb memory");
                        }
                        if (endTime - state.mBeginTime >= 1000 || state.mRequestMemory <= 0 || state.mReclaimedProc >= 3) {
                            return 0;
                        }
                        reclaimAction = this;
                        it = it;
                    } else if (AwareLog.getDebugLogSwitch()) {
                        AwareLog.d(TAG, "call hiber reclaimApp error skip! procName=" + procName);
                    }
                }
            }
        }
        return 3;
    }

    @Override // com.android.server.rms.iaware.memory.action.Action
    public void reset() {
        this.mLastReclaimTime = 0;
        this.mEmptyLoopCount = 0;
        this.mReclaimGap = 2000;
    }

    @Override // com.android.server.rms.iaware.memory.action.Action
    public boolean canBeExecuted() {
        if (!AppHibernateTask.getInstance().isAppHiberEnabled()) {
            AwareLog.i(TAG, "canBeExecuted hibernation is not running!");
            this.mReclaimGap = 1800000;
            return false;
        } else if (SystemClock.elapsedRealtime() - this.mLastReclaimTime < this.mReclaimGap) {
            AwareLog.i(TAG, "canBeExecuted waiting next operation, interval=" + this.mReclaimGap);
            return false;
        } else if (MemoryReader.isZramOk()) {
            return true;
        } else {
            AwareLog.i(TAG, "canBeExecuted no zram space!");
            updateReclaimGap(0);
            return false;
        }
    }

    @Override // com.android.server.rms.iaware.memory.action.Action
    public int getLastExecFailCount() {
        return this.mEmptyLoopCount;
    }

    private void updateReclaimGap(int numReclaimed) {
        if (numReclaimed == 0) {
            this.mReclaimGap *= 2;
            long j = this.mReclaimGap;
            if (j > 1800000) {
                j = 1800000;
            }
            this.mReclaimGap = j;
            return;
        }
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
            return;
        }
        this.mReclaimGap = 2000;
    }

    public static void reclaimProcessAll(Handler handler, ProcessRecordEx app) {
        handler.postDelayed(new ReclaimRunnable(app), (long) MemoryConstant.getCameraPreloadReclaimDelay());
    }

    private static class ReclaimRunnable implements Runnable {
        private ProcessRecordEx cameraApp;

        ReclaimRunnable(ProcessRecordEx app) {
            this.cameraApp = app;
        }

        @Override // java.lang.Runnable
        public void run() {
            Bundle extras;
            ProcessRecordEx processRecordEx = this.cameraApp;
            if (processRecordEx != null && !processRecordEx.isProcessRecordNull() && this.cameraApp.getPid() > 0 && (extras = DataAppHandle.getInstance().createBundleFromAppInfo()) != null) {
                if (extras.getInt("appUid") == this.cameraApp.getUid()) {
                    AwareLog.d(ReclaimAction.TAG, "do not reclaim foregroud app!");
                } else {
                    MemoryUtils.reclaimProcessAll(this.cameraApp.getPid(), false);
                }
            }
        }
    }
}
