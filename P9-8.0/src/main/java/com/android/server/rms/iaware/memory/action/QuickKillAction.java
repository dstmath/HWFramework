package com.android.server.rms.iaware.memory.action;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.rms.iaware.AwareLog;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessCleaner;
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
        if (this.mReqMem > 0) {
            return execQuickKillGroup(extras, getAwareAppMngProcGroup(0));
        }
        AwareLog.d(TAG, "QuickKillAction exit cause reqMem is negative: " + this.mReqMem);
        return -1;
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

    private int execKillProcess(Bundle extras, int needKillNum, List<AwareProcessBlockInfo> procGroups) {
        if (procGroups == null) {
            AwareLog.w(TAG, "execKillProcess parameter procGroups NULL");
            return needKillNum;
        }
        long reqMem = this.mReqMem;
        int position = procGroups.size() - 1;
        String processName = null;
        int appUid = extras.getInt("appUid");
        while (needKillNum > 0 && position >= 0) {
            AwareProcessBlockInfo procGroup = (AwareProcessBlockInfo) procGroups.get(position);
            if (procGroup == null) {
                position--;
            } else {
                List<AwareProcessInfo> procs = procGroup.getProcessList();
                if (procs == null || procs.size() < 1) {
                    AwareLog.w(TAG, "execKillProcess: null process list. uid:" + procGroup.mUid + ", position:" + position);
                    position--;
                } else {
                    AwareProcessInfo currentProcess = (AwareProcessInfo) procs.get(0);
                    if (!(currentProcess == null || currentProcess.mProcInfo == null)) {
                        processName = currentProcess.mProcInfo.mProcessName;
                    }
                    if (appUid == procGroup.mUid) {
                        AwareLog.i(TAG, "execKillProcess: uid " + procGroup.mUid + processName + " is launching,should not be killed, position:" + position);
                        position--;
                    } else {
                        long beginTime = System.currentTimeMillis();
                        List<Integer> pids = ProcessCleaner.getInstance(this.mContext).killProcessesSameUidExt(procGroup, true, "LowMemQuick");
                        int exeTime = (int) (System.currentTimeMillis() - beginTime);
                        if (pids != null) {
                            needKillNum -= pids.size();
                            insertDumpAndStatisticData(extras, procGroup, pids, beginTime, exeTime, reqMem);
                            reqMem -= ((long) pids.size()) * MemoryConstant.APP_AVG_USS;
                            EventTracker.getInstance().trackEvent(1002, 0, 0, "QuickKill,uid:" + procGroup.mUid + ", processName:" + processName);
                            PackageTracker.getInstance().trackKillEvent(procGroup.mUid, procs);
                        } else {
                            EventTracker.getInstance().trackEvent(1002, 0, 0, "QuickKill,uid:" + procGroup.mUid + ", processName:" + processName + " failed!");
                        }
                        position--;
                    }
                }
            }
        }
        AppCleanupDumpRadar.getInstance().reportMemoryData(procGroups, position);
        return needKillNum;
    }

    private int execQuickKillGroup(Bundle extras, List<AwareProcessBlockInfo> procGroups) {
        int needKillNum = (int) ((this.mReqMem / MemoryConstant.APP_AVG_USS) + 4);
        if (this.mContext == null) {
            AwareLog.e(TAG, "execQuickKillGroup: mContext = NULL!");
            return -1;
        } else if (procGroups == null || procGroups.size() < 1) {
            AwareLog.d(TAG, "getProcessActionGroup:null!");
            return -1;
        } else {
            AwareLog.d(TAG, "getProcessActionGroup:" + procGroups.size() + " needKillNum:" + needKillNum);
            needKillNum = execKillProcess(extras, needKillNum, procGroups);
            releaseSnapshots(1);
            if (needKillNum > 0) {
                AwareLog.d(TAG, "get getProcessActionGroup1");
                List<AwareProcessBlockInfo> procGroupsLevel1 = getAwareAppMngProcGroup(1);
                if (procGroupsLevel1 != null) {
                    needKillNum = execKillProcess(extras, needKillNum, procGroupsLevel1);
                    AwareLog.d(TAG, "getProcessActionGroup1:" + procGroupsLevel1.size() + " needKillNum:" + needKillNum);
                }
            }
            if (this.MEM_EMERG_KILL && needKillNum > 0) {
                AwareLog.d(TAG, "get getProcessActionGroup2");
                List<AwareProcessBlockInfo> procGroupsLevel2 = getAwareAppMngProcGroup(2);
                if (procGroupsLevel2 != null) {
                    needKillNum = execKillProcess(extras, needKillNum, procGroupsLevel2);
                    AwareLog.d(TAG, "getProcessActionGroup2:" + procGroupsLevel2.size() + " needKillNum:" + needKillNum);
                }
            }
            if (this.MEM_EMERG_KILL && needKillNum > 0) {
                AwareLog.d(TAG, "get getProcessActionGroup3");
                List<AwareProcessBlockInfo> procGroupsLevel3 = getAwareAppMngProcGroup(3);
                if (procGroupsLevel3 != null) {
                    AwareLog.d(TAG, "getProcessActionGroup3:" + procGroupsLevel3.size() + " needKillNum:" + execKillProcess(extras, needKillNum, procGroupsLevel3));
                }
            }
            return 0;
        }
    }

    private void insertDumpAndStatisticData(Bundle extras, AwareProcessBlockInfo procGroup, List<Integer> pids, long beginTime, int exeTime, long reqMem) {
        if (extras == null || procGroup == null || pids == null) {
            AwareLog.e(TAG, "insertDumpAndStatisticData: null procGroups");
            return;
        }
        List<AwareProcessInfo> procs = procGroup.getProcessList();
        String appName = extras.getString("appName");
        int event = extras.getInt("event");
        long timeStamp = extras.getLong("timeStamp");
        int cpuLoad = extras.getInt("cpuLoad");
        int effect = (pids.size() * 20480) / 1024;
        StringBuilder reasonSB = new StringBuilder("[").append(appName).append(",").append(event).append(",").append(timeStamp).append("],[").append(cpuLoad).append(",").append(extras.getBoolean("cpuBusy")).append("],[").append(reqMem / 1024).append(",").append(effect).append("]");
        StringBuilder operationSB = new StringBuilder("QuickKill [");
        operationSB.append(procGroup.mUid).append(",");
        for (AwareProcessInfo info : procs) {
            if (!(info.mProcInfo == null || info.mProcInfo.mPackageName == null)) {
                operationSB.append(info.mProcInfo.mPackageName).append(",");
            }
        }
        operationSB.append(pids).append("]");
        EventTracker.getInstance().insertDumpData(beginTime, operationSB.toString(), exeTime, reasonSB.toString());
        EventTracker.getInstance().insertStatisticData("QuickKill", exeTime, effect);
    }
}
