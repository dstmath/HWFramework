package com.android.server.rms.iaware.memory.action;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.rms.iaware.AwareLog;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.rms.iaware.memory.action.Action;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.memory.utils.PackageTracker;
import com.android.server.rms.iaware.srms.AppCleanupDumpRadar;
import java.util.List;

public class QuickKillAction extends Action {
    private static final String TAG = "AwareMem_QuickKill";
    private final boolean MEM_EMERG_KILL = SystemProperties.getBoolean("persist.iaware.mem_emerg_kill", false);
    private AwareAppMngSortPolicy mPolicy;
    private long mReqMem = 0;

    public QuickKillAction(Context context) {
        super(context);
    }

    public int execute(Bundle extras) {
        if (extras == null) {
            AwareLog.e(TAG, "execQuickKillGroup: null extras");
            return -1;
        }
        this.mReqMem = extras.getLong("reqMem");
        if (this.mReqMem <= 0) {
            AwareLog.d(TAG, "QuickKillAction exit cause reqMem is negative: " + this.mReqMem);
            return -1;
        }
        ProcessCleaner.getInstance(this.mContext).beginKillFast();
        int result = execQuickKillGroup(extras, getAwareAppMngProcGroup(0));
        ProcessCleaner.getInstance(this.mContext).endKillFast();
        return result;
    }

    private List<AwareProcessBlockInfo> getAwareAppMngProcGroup(int memCleanLevel) {
        AwareLog.i(TAG, "grouplist level=" + memCleanLevel);
        this.mPolicy = MemoryUtils.getAppMngSortPolicy(2, 3, memCleanLevel);
        if (this.mPolicy != null) {
            return MemoryUtils.getAppMngProcGroup(this.mPolicy, 2);
        }
        AwareLog.w(TAG, "getAppMngSortPolicy null policy!");
        return null;
    }

    public void reset() {
    }

    private List<Integer> execChooseKill(AwareProcessBlockInfo procGroup, boolean needCheckAdj) {
        if (!MemoryConstant.isFastQuickKillSwitch()) {
            return ProcessCleaner.getInstance(this.mContext).killProcessesSameUidExt(procGroup, true, "LowMemQuick", needCheckAdj);
        }
        return ProcessCleaner.getInstance(this.mContext).killProcessesSameUidFast(procGroup, this.mInterrupt, true, true, "LowMemQuick", needCheckAdj);
    }

    private int execKillProcess(Bundle extras, List<AwareProcessBlockInfo> procGroups, int memCleanLevel) {
        boolean z;
        long beginTime;
        boolean needCheckAdj;
        int appUid;
        int position;
        int position2;
        boolean needCheckAdj2;
        boolean z2;
        int exeTime;
        List<Integer> pids;
        boolean needCheckAdj3;
        int appUid2;
        String processName;
        int position3;
        List<AwareProcessBlockInfo> list = procGroups;
        if (list == null) {
            AwareLog.w(TAG, "execKillProcess parameter procGroups NULL");
            return 0;
        }
        int killedNum = 0;
        long beginTime2 = 0;
        int position4 = procGroups.size() - 1;
        String processName2 = null;
        int appUid3 = extras.getInt("appUid");
        boolean needCheckAdj4 = memCleanLevel < 3;
        int position5 = position4;
        while (this.mReqMem > 0 && position5 >= 0) {
            AwareProcessBlockInfo procGroup = list.get(position5);
            if (procGroup == null) {
                position5--;
            } else {
                List<AwareProcessInfo> procs = procGroup.getProcessList();
                if (procs == null) {
                    beginTime = beginTime2;
                    position = position5;
                    appUid = appUid3;
                    needCheckAdj = needCheckAdj4;
                    z2 = true;
                } else if (procs.size() < 1) {
                    beginTime = beginTime2;
                    position = position5;
                    z2 = true;
                    appUid = appUid3;
                    needCheckAdj = needCheckAdj4;
                } else {
                    AwareProcessInfo currentProcess = procs.get(0);
                    if (!(currentProcess == null || currentProcess.mProcInfo == null)) {
                        processName2 = currentProcess.mProcInfo.mProcessName;
                    }
                    if (appUid3 == procGroup.mUid) {
                        AwareLog.i(TAG, "execKillProcess: uid " + procGroup.mUid + processName2 + " is launching,should not be killed, position:" + position5);
                        position5 += -1;
                        beginTime2 = beginTime2;
                        int i = memCleanLevel;
                    } else {
                        Action.PkgMemHolder pkgMemHolder = new Action.PkgMemHolder(procGroup.mUid, procs);
                        long beginTime3 = System.currentTimeMillis();
                        List<Integer> pids2 = execChooseKill(procGroup, needCheckAdj4);
                        int exeTime2 = (int) (System.currentTimeMillis() - beginTime3);
                        if (pids2 != null) {
                            int killedNum2 = killedNum + pids2.size();
                            long killedMem = pkgMemHolder.getKilledMem(procGroup.mUid, pids2);
                            pids = pids2;
                            exeTime = exeTime2;
                            AwareProcessInfo awareProcessInfo = currentProcess;
                            processName = processName2;
                            position3 = position5;
                            Action.PkgMemHolder pkgMemHolder2 = pkgMemHolder;
                            z = true;
                            appUid2 = appUid3;
                            needCheckAdj3 = needCheckAdj4;
                            insertDumpAndStatisticData(extras, procGroup, pids2, beginTime3, exeTime, this.mReqMem, killedMem);
                            this.mReqMem -= killedMem;
                            EventTracker.getInstance().trackEvent(1002, 0, 0, "QuickKill,uid:" + procGroup.mUid + ", proc:" + processName + ",weight:" + procGroup.mWeight);
                            PackageTracker.getInstance().trackKillEvent(procGroup.mUid, procs);
                            killedNum = killedNum2;
                        } else {
                            pids = pids2;
                            exeTime = exeTime2;
                            position3 = position5;
                            Action.PkgMemHolder pkgMemHolder3 = pkgMemHolder;
                            appUid2 = appUid3;
                            needCheckAdj3 = needCheckAdj4;
                            AwareProcessInfo awareProcessInfo2 = currentProcess;
                            z = true;
                            processName = processName2;
                            EventTracker.getInstance().trackEvent(1002, 0, 0, "QuickKill,uid:" + procGroup.mUid + ", proc:" + processName + " failed!");
                        }
                        position2 = position3 - 1;
                        Bundle bundle = extras;
                        processName2 = processName;
                        appUid3 = appUid2;
                        needCheckAdj2 = needCheckAdj3;
                        beginTime2 = beginTime3;
                        List<Integer> list2 = pids;
                        int i2 = exeTime;
                        boolean z3 = z;
                        list = procGroups;
                        int i3 = memCleanLevel;
                    }
                }
                AwareLog.w(TAG, "execKillProcess: null process list. uid:" + procGroup.mUid + ", position:" + position);
                position2 = position + -1;
                Bundle bundle2 = extras;
                appUid3 = appUid;
                needCheckAdj2 = needCheckAdj;
                beginTime2 = beginTime;
                boolean z32 = z;
                list = procGroups;
                int i32 = memCleanLevel;
            }
            Bundle bundle3 = extras;
        }
        long j = beginTime2;
        int i4 = appUid3;
        boolean z4 = needCheckAdj4;
        AppCleanupDumpRadar.getInstance().reportMemoryData(procGroups, position5);
        return killedNum;
    }

    private int execQuickKillGroup(Bundle extras, List<AwareProcessBlockInfo> procGroups) {
        if (this.mContext == null) {
            AwareLog.e(TAG, "execQuickKillGroup: mContext = NULL!");
            return -1;
        }
        AwareLog.d(TAG, "requestMem:" + this.mReqMem);
        int totalKilledNum = 0;
        if (procGroups != null) {
            int killedNum = execKillProcess(extras, procGroups, 0);
            AwareLog.d(TAG, "getProcessActionGroup:" + procGroups.size() + " killed num:" + killedNum);
            totalKilledNum = 0 + killedNum;
        }
        if (this.mReqMem > 0) {
            AwareLog.d(TAG, "get getProcessActionGroup1");
            List<AwareProcessBlockInfo> procGroupsLevel1 = getAwareAppMngProcGroup(1);
            if (procGroupsLevel1 != null) {
                int killedNum2 = execKillProcess(extras, procGroupsLevel1, 1);
                AwareLog.d(TAG, "getProcessActionGroup1:" + procGroupsLevel1.size() + " killed num:" + killedNum2);
                totalKilledNum += killedNum2;
            }
        }
        if (!MemoryConstant.isCleanAllSwitch() && this.MEM_EMERG_KILL && this.mReqMem > 0) {
            AwareLog.d(TAG, "get getProcessActionGroup2");
            List<AwareProcessBlockInfo> procGroupsLevel2 = getAwareAppMngProcGroup(2);
            if (procGroupsLevel2 != null) {
                int killedNum3 = execKillProcess(extras, procGroupsLevel2, 2);
                AwareLog.d(TAG, "getProcessActionGroup2:" + procGroupsLevel2.size() + " killed num:" + killedNum3);
                totalKilledNum += killedNum3;
            }
        }
        if (!MemoryConstant.isCleanAllSwitch() && this.MEM_EMERG_KILL && this.mReqMem > 0) {
            AwareLog.d(TAG, "get getProcessActionGroup3");
            List<AwareProcessBlockInfo> procGroupsLevel3 = getAwareAppMngProcGroup(3);
            if (procGroupsLevel3 != null) {
                int killedNum4 = execKillProcess(extras, procGroupsLevel3, 3);
                AwareLog.d(TAG, "getProcessActionGroup3:" + procGroupsLevel3.size() + " killed num:" + killedNum4);
                totalKilledNum += killedNum4;
            }
        }
        if (MemoryConstant.isCleanAllSwitch() && this.mReqMem > 0) {
            AwareLog.d(TAG, "get getProcessActionGroup5");
            List<AwareProcessBlockInfo> procGroupsLevel5 = getAwareAppMngProcGroup(5);
            if (procGroupsLevel5 != null) {
                int killedNum5 = execKillProcess(extras, procGroupsLevel5, 5);
                AwareLog.d(TAG, "getProcessActionGroup5:" + procGroupsLevel5.size() + " killed num:" + killedNum5);
                totalKilledNum += killedNum5;
            }
        }
        AwareLog.i(TAG, "killing count:" + totalKilledNum);
        return 0;
    }

    private void insertDumpAndStatisticData(Bundle extras, AwareProcessBlockInfo procGroup, List<Integer> pids, long beginTime, int exeTime, long reqMem, long killedMem) {
        Bundle bundle = extras;
        AwareProcessBlockInfo awareProcessBlockInfo = procGroup;
        List<Integer> list = pids;
        if (bundle == null || awareProcessBlockInfo == null || list == null) {
            int i = exeTime;
            long j = killedMem;
            AwareLog.e(TAG, "insertDumpAndStatisticData: null procGroups");
            return;
        }
        List<AwareProcessInfo> procs = procGroup.getProcessList();
        String appName = bundle.getString("appName");
        int event = bundle.getInt("event");
        long timeStamp = bundle.getLong("timeStamp");
        int cpuLoad = bundle.getInt("cpuLoad");
        boolean cpuBusy = bundle.getBoolean("cpuBusy");
        int effect = ((int) killedMem) / 1024;
        StringBuilder sb = new StringBuilder("[");
        sb.append(appName);
        sb.append(",");
        sb.append(event);
        sb.append(",");
        sb.append(timeStamp);
        sb.append("],[");
        sb.append(cpuLoad);
        sb.append(",");
        sb.append(cpuBusy);
        sb.append("],[");
        sb.append(reqMem / 1024);
        sb.append(",");
        sb.append(effect);
        StringBuilder reasonSB = sb.append("]");
        StringBuilder operationSB = new StringBuilder("QuickKill [");
        operationSB.append(awareProcessBlockInfo.mUid);
        operationSB.append(",");
        for (AwareProcessInfo info : procs) {
            if (!(info.mProcInfo == null || info.mProcInfo.mPackageName == null)) {
                operationSB.append(info.mProcInfo.mPackageName);
                operationSB.append(",");
            }
            Bundle bundle2 = extras;
            AwareProcessBlockInfo awareProcessBlockInfo2 = procGroup;
        }
        operationSB.append(list);
        operationSB.append("]");
        EventTracker.getInstance().insertDumpData(beginTime, operationSB.toString(), exeTime, reasonSB.toString());
        EventTracker.getInstance().insertStatisticData("QuickKill", exeTime, effect);
    }
}
