package com.android.server.rms.iaware.memory.action;

import android.content.Context;
import android.os.Bundle;
import android.rms.iaware.AwareLog;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.memory.action.Action;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.memory.utils.PackageTracker;
import com.android.server.rms.iaware.srms.AppCleanupDumpRadar;
import com.huawei.android.os.SystemPropertiesEx;
import java.util.List;

public class QuickKillAction extends Action {
    private static final String TAG = "AwareMem_QuickKill";
    private final boolean mIsMemEmergKill = SystemPropertiesEx.getBoolean("persist.iaware.mem_emerg_kill", false);
    private AwareAppMngSortPolicy mPolicy;
    private long mReqMem = 0;

    public QuickKillAction(Context context) {
        super(context);
    }

    @Override // com.android.server.rms.iaware.memory.action.Action
    public int execute(Bundle extras) {
        if (extras == null) {
            AwareLog.e(TAG, "execQuickKillGroup: null extras");
            return -1;
        }
        this.mReqMem = extras.getLong("reqMem");
        if (this.mReqMem <= 0) {
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "QuickKillAction exit cause reqMem is negative: " + this.mReqMem);
            }
            return -1;
        }
        ProcessCleaner.getInstance(this.mContext).beginKillFast();
        int result = execQuickKillGroup(extras);
        ProcessCleaner.getInstance(this.mContext).endKillFast();
        return result;
    }

    private List<AwareProcessBlockInfo> getAwareAppMngProcGroup(int memCleanLevel) {
        AwareLog.i(TAG, "request grouplist level=" + memCleanLevel);
        this.mPolicy = MemoryUtils.getAppMngSortPolicy(2, 3, memCleanLevel);
        AwareAppMngSortPolicy awareAppMngSortPolicy = this.mPolicy;
        if (awareAppMngSortPolicy == null) {
            AwareLog.w(TAG, "getAppMngSortPolicy null policy!");
            return null;
        }
        List<AwareProcessBlockInfo> procGroups = MemoryUtils.getAppMngProcGroup(awareAppMngSortPolicy, 2);
        MemoryUtils.sortByTimeIfNeed(procGroups, memCleanLevel);
        return procGroups;
    }

    @Override // com.android.server.rms.iaware.memory.action.Action
    public void reset() {
    }

    private List<Integer> execChooseKill(AwareProcessBlockInfo procGroup, boolean isNeedCheckAdj) {
        AwareIntelligentRecg.getInstance().reportAbnormalClean(procGroup);
        if (MemoryConstant.isFastQuickKillSwitch()) {
            return ProcessCleaner.getInstance(this.mContext).killProcessesSameUidFast(procGroup, this.mInterrupt, "LowMemQuick", new boolean[]{true, true, isNeedCheckAdj});
        }
        return ProcessCleaner.getInstance(this.mContext).killProcessesSameUidExt(procGroup, true, "LowMemQuick", isNeedCheckAdj);
    }

    private int execKillProcess(Bundle extras, List<AwareProcessBlockInfo> procGroups, int memCleanLevel) {
        int i = 0;
        if (procGroups == null) {
            AwareLog.w(TAG, "execKillProcess parameter procGroups NULL");
            return 0;
        }
        int killedNum = 0;
        int i2 = 1;
        int position = procGroups.size() - 1;
        String processName = null;
        int appUid = extras.getInt("appUid");
        boolean isNeedCheckAdj = memCleanLevel < 3;
        while (this.mReqMem > 0 && position >= 0) {
            AwareProcessBlockInfo procGroup = procGroups.get(position);
            if (procGroup == null) {
                position--;
            } else {
                List<AwareProcessInfo> procs = procGroup.getProcessList();
                if (procs == null || procs.size() < i2) {
                    AwareLog.w(TAG, "execKillProcess: null process list. uid:" + procGroup.procUid + ", position:" + position);
                    position += -1;
                    i = 0;
                    i2 = 1;
                } else {
                    AwareProcessInfo currentProcess = procs.get(i);
                    if (!(currentProcess == null || currentProcess.procProcInfo == null)) {
                        processName = currentProcess.procProcInfo.mProcessName;
                    }
                    if (appUid == procGroup.procUid) {
                        if (AwareLog.getDebugLogSwitch()) {
                            AwareLog.d(TAG, "execKillProcess: uid " + procGroup.procUid + processName + " is launching,should not be killed, position:" + position);
                        }
                        position--;
                        i = 0;
                        i2 = 1;
                    } else {
                        killedNum += executeKill(extras, procGroup, procs, processName, isNeedCheckAdj);
                        position--;
                        i = 0;
                        i2 = 1;
                    }
                }
            }
        }
        AppCleanupDumpRadar.getInstance().reportMemoryData(procGroups, position);
        return killedNum;
    }

    private int decideQuickKillLevel() {
        if (MemoryConstant.isCleanAllSwitch() && this.mReqMem > 0) {
            return 5;
        }
        if (MemoryConstant.isCleanAllSwitch() || !this.mIsMemEmergKill || this.mReqMem <= 0) {
            return 0;
        }
        return 3;
    }

    private int executeKill(Bundle extras, AwareProcessBlockInfo procGroup, List<AwareProcessInfo> procs, String processName, boolean isNeedCheckAdj) {
        Action.PkgMemHolder pkgMemHolder = new Action.PkgMemHolder(procGroup.procUid, procs);
        long beginTime = System.currentTimeMillis();
        List<Integer> pids = execChooseKill(procGroup, isNeedCheckAdj);
        int exeTime = (int) (System.currentTimeMillis() - beginTime);
        if (pids != null) {
            int killedNum = 0 + pids.size();
            long killedMem = pkgMemHolder.getKilledMem(procGroup.procUid, pids);
            insertDumpAndStatisticData(extras, procGroup, pids, new KilledInfo(beginTime, exeTime, this.mReqMem, killedMem));
            this.mReqMem -= killedMem;
            EventTracker.getInstance().trackEvent(1002, 0, 0, "QuickKill,uid:" + procGroup.procUid + ", proc:" + processName + ",weight:" + procGroup.procWeight);
            PackageTracker.getInstance().trackKillEvent(procGroup.procUid, procs);
            return killedNum;
        }
        EventTracker.getInstance().trackEvent(1002, 0, 0, "QuickKill,uid:" + procGroup.procUid + ", proc:" + processName + " failed!");
        return 0;
    }

    private int execQuickKillGroup(Bundle extras) {
        if (this.mContext == null) {
            AwareLog.e(TAG, "execQuickKillGroup: mContext = NULL!");
            return -1;
        }
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "requestMem:" + this.mReqMem);
        }
        int memCleanLevel = decideQuickKillLevel();
        int totalKilledNum = 0;
        List<AwareProcessBlockInfo> procGroups = getAwareAppMngProcGroup(memCleanLevel);
        if (procGroups != null) {
            int killedNum = execKillProcess(extras, procGroups, memCleanLevel);
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "getprocGroups level" + memCleanLevel + ":" + procGroups.size() + " killed num:" + killedNum);
            }
            totalKilledNum = 0 + killedNum;
        }
        if (this.mReqMem > 0 && memCleanLevel == 0) {
            AwareLog.d(TAG, "get getProcessActionGroup1");
            memCleanLevel = 1;
            List<AwareProcessBlockInfo> level1ProcGroups = getAwareAppMngProcGroup(1);
            if (level1ProcGroups != null) {
                int killedNum2 = execKillProcess(extras, level1ProcGroups, 1);
                if (AwareLog.getDebugLogSwitch()) {
                    AwareLog.d(TAG, "getprocGroups level1:" + level1ProcGroups.size() + " killed num:" + killedNum2);
                }
                totalKilledNum += killedNum2;
            }
        }
        AwareLog.i(TAG, "killing level" + memCleanLevel + " count=" + totalKilledNum);
        return 0;
    }

    /* access modifiers changed from: private */
    public class KilledInfo {
        long mBeginTime;
        int mExeTime;
        long mKilledMem;
        long mReqMem;

        KilledInfo(long beginTime, int exeTime, long reqMem, long killedMem) {
            this.mBeginTime = beginTime;
            this.mExeTime = exeTime;
            this.mKilledMem = killedMem;
            this.mReqMem = reqMem;
        }
    }

    private void insertDumpAndStatisticData(Bundle extras, AwareProcessBlockInfo procGroup, List<Integer> pids, KilledInfo killedInfo) {
        if (extras == null || procGroup == null || pids == null) {
            AwareLog.e(TAG, "insertDumpAndStatisticData: null procGroups");
            return;
        }
        List<AwareProcessInfo> procs = procGroup.getProcessList();
        String appName = extras.getString("appName");
        int event = extras.getInt("event");
        long timeStamp = extras.getLong("timeStamp");
        int cpuLoad = extras.getInt("cpuLoad");
        boolean isCpuBusy = extras.getBoolean("cpuBusy");
        int effect = ((int) killedInfo.mKilledMem) / 1024;
        StringBuilder sb = new StringBuilder("[");
        sb.append(appName);
        sb.append(",");
        sb.append(event);
        sb.append(",");
        sb.append(timeStamp);
        sb.append("],[");
        sb.append(cpuLoad);
        sb.append(",");
        sb.append(isCpuBusy);
        sb.append("],[");
        sb.append(killedInfo.mReqMem / 1024);
        sb.append(",");
        sb.append(effect);
        StringBuilder reasonSb = sb.append("]");
        StringBuilder operationSb = new StringBuilder("QuickKill [");
        operationSb.append(procGroup.procUid);
        operationSb.append(",");
        for (AwareProcessInfo info : procs) {
            if (info.procProcInfo == null) {
                procs = procs;
            } else if (info.procProcInfo.mPackageName == null) {
                procs = procs;
            } else {
                operationSb.append(info.procProcInfo.mPackageName);
                operationSb.append(",");
                procs = procs;
            }
        }
        operationSb.append(pids);
        operationSb.append("]");
        EventTracker.getInstance().insertDumpData(killedInfo.mBeginTime, operationSb.toString(), killedInfo.mExeTime, reasonSb.toString());
        EventTracker.getInstance().insertStatisticData("QuickKill", killedInfo.mExeTime, effect);
    }
}
