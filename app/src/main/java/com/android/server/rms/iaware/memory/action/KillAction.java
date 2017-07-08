package com.android.server.rms.iaware.memory.action;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.memory.utils.PackageTracker;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.ArrayList;
import java.util.List;

public class KillAction extends Action {
    private static final int MAX_PROCESS_KILL_COUNT = 5;
    private static final String TAG = "AwareMem_Kill";
    private int mInvaildKillCount;
    private long mLastExecTime;
    private int mLastKillZeroCount;

    public KillAction(Context context) {
        super(context);
        this.mLastExecTime = 0;
        this.mLastKillZeroCount = 0;
        this.mInvaildKillCount = 0;
    }

    public int execute(Bundle extras) {
        if (extras == null) {
            AwareLog.e(TAG, "null extras!");
            return -1;
        }
        int maxKillCount = MAX_PROCESS_KILL_COUNT;
        long availableRam = MemoryReader.getInstance().getMemAvailable();
        if (availableRam <= 0) {
            AwareLog.e(TAG, "execute faild to read availableRam =" + availableRam);
            return -1;
        }
        boolean invaildKill = false;
        long normalWater = MemoryConstant.getMiddleWater();
        if (availableRam < normalWater) {
            maxKillCount = (int) ((((normalWater - availableRam) / MemoryConstant.APP_AVG_USS) + 1) + 5);
            invaildKill = true;
        }
        AwareAppMngSortPolicy policy = getAwareAppMngSortPolicy();
        if (policy == null) {
            AwareLog.w(TAG, "getAppMngSortPolicy null policy!");
            return -1;
        } else if (this.mInterrupt.get()) {
            this.mLastKillZeroCount++;
            this.mInvaildKillCount++;
            return -1;
        } else {
            int i;
            int killedCount = 0;
            List<AwareProcessBlockInfo> procGroups = MemoryUtils.getAppMngProcGroup(policy, 2);
            if (procGroups == null || procGroups.isEmpty()) {
                AwareLog.w(TAG, "empty group list!");
                this.mLastKillZeroCount++;
            } else {
                updateGroupList(procGroups);
                killedCount = execKillGroup(extras, procGroups, maxKillCount);
                this.mLastExecTime = SystemClock.elapsedRealtime();
                if (killedCount > 0) {
                    this.mLastKillZeroCount = 0;
                    if (killedCount >= MAX_PROCESS_KILL_COUNT) {
                        invaildKill = false;
                    }
                } else {
                    this.mLastKillZeroCount++;
                }
            }
            if (invaildKill) {
                this.mInvaildKillCount++;
            } else {
                this.mInvaildKillCount = 0;
            }
            AwareLog.i(TAG, "execute: killing count=" + killedCount);
            if (killedCount > 0) {
                i = 0;
            } else {
                i = -1;
            }
            return i;
        }
    }

    private AwareAppMngSortPolicy getAwareAppMngSortPolicy() {
        if (getInvaildKillCount() <= 3) {
            return MemoryUtils.getAppMngSortPolicy(3);
        }
        AwareLog.i(TAG, "request MEM_LEVEL1 grouplist");
        return MemoryUtils.getAppMngSortPolicy(3, 1);
    }

    public int getInvaildKillCount() {
        return this.mInvaildKillCount;
    }

    public void reset() {
        this.mLastExecTime = 0;
        this.mLastKillZeroCount = 0;
        this.mInvaildKillCount = 0;
    }

    public boolean canBeExecuted() {
        long availableRam = MemoryReader.getInstance().getMemAvailable();
        if (availableRam <= 0) {
            AwareLog.w(TAG, "kill action canBeExecuted read availableRam err!" + availableRam);
            return false;
        }
        long curStep = availableRam - MemoryConstant.getEmergencyMemory();
        long interval = ((curStep * curStep) * MemoryConstant.MIN_INTERVAL_OP_TIMEOUT) / ((100 * (3 * (MemoryConstant.getIdleMemory() - MemoryConstant.getEmergencyMemory()))) * 1024);
        if (interval <= SystemClock.elapsedRealtime() - this.mLastExecTime) {
            return true;
        }
        AwareLog.w(TAG, "kill action canBeExecuted waiting next operation, real interval=" + interval);
        return false;
    }

    public int getLastExecFailCount() {
        return this.mLastKillZeroCount;
    }

    private int execKillGroup(Bundle extras, List<AwareProcessBlockInfo> procGroups, int maxKillCount) {
        if (extras == null || procGroups == null || procGroups.size() < 1 || this.mContext == null) {
            AwareLog.w(TAG, "execKillGroup: null procGroups");
            return 0;
        }
        long reqMem = extras.getLong("reqMem");
        int position = procGroups.size() - 1;
        int killedPids = 0;
        while (reqMem > 0 && position >= 0) {
            if (this.mInterrupt.get()) {
                AwareLog.w(TAG, "execKillGroup: mInterrupt, return");
                break;
            }
            AwareProcessBlockInfo procGroup = (AwareProcessBlockInfo) procGroups.get(position);
            if (procGroup == null) {
                AwareLog.w(TAG, "execKillGroup: null procGroup");
                position--;
            } else {
                List<AwareProcessInfo> procs = procGroup.getProcessList();
                if (procs == null || procs.size() < 1) {
                    AwareLog.w(TAG, "execKillGroup: null process list. uid:" + procGroup.mUid + ", position:" + position);
                    position--;
                } else {
                    AwareProcessInfo currentProcess = (AwareProcessInfo) procs.get(0);
                    String str = currentProcess.mProcInfo != null ? currentProcess.mProcInfo.mProcessName : null;
                    long beginTime = System.currentTimeMillis();
                    List<Integer> pids = ProcessCleaner.getInstance(this.mContext).killProcessesSameUidExt(procGroup, this.mInterrupt, false, false);
                    int exeTime = (int) (System.currentTimeMillis() - beginTime);
                    if (pids != null) {
                        killedPids += pids.size();
                        insertDumpAndStatisticData(extras, procGroup, pids, beginTime, exeTime, reqMem);
                        reqMem -= ((long) pids.size()) * MemoryConstant.APP_AVG_USS;
                        EventTracker.getInstance().trackEvent(EventTracker.TRACK_TYPE_KILL, 0, 0, "uid:" + procGroup.mUid + ", proc:" + str);
                        PackageTracker.getInstance().trackKillEvent(procGroup.mUid, procs);
                        if (killedPids >= maxKillCount) {
                            AwareLog.i(TAG, "execKillGroup: Had killed process count=" + killedPids + ", return");
                            break;
                        }
                    }
                    EventTracker.getInstance().trackEvent(EventTracker.TRACK_TYPE_KILL, 0, 0, "uid:" + procGroup.mUid + ", proc:" + str + " fail ");
                    position--;
                }
            }
        }
        return killedPids;
    }

    private void updateGroupList(List<AwareProcessBlockInfo> procGroups) {
        if (PackageTracker.getInstance().isEnabled() && procGroups != null) {
            List<AwareProcessBlockInfo> highFrequencyProcGroups = new ArrayList();
            int oldSize = procGroups.size();
            for (int i = procGroups.size() - 1; i >= 0; i--) {
                List<AwareProcessInfo> processList = ((AwareProcessBlockInfo) procGroups.get(i)).mProcessList;
                int uid = ((AwareProcessBlockInfo) procGroups.get(i)).mUid;
                for (String packageName : getPackageList(processList)) {
                    if (uid < LifeCycleStateMachine.TIME_OUT_TIME && ((AwareProcessInfo) processList.get(0)).mProcInfo != null) {
                        String firstProcessname = ((AwareProcessInfo) processList.get(0)).mProcInfo.mProcessName;
                        if (PackageTracker.getInstance().isProcessCriticalFrequency(packageName, firstProcessname, uid)) {
                            AwareLog.w(TAG, "updateGroupList process: not kill critical frequent=" + firstProcessname + ", uid=" + uid);
                            procGroups.remove(i);
                            break;
                        } else if (PackageTracker.getInstance().isProcessHighFrequency(packageName, firstProcessname, uid)) {
                            AwareLog.w(TAG, "updateGroupList process: move high frequent=" + firstProcessname + ", uid=" + uid);
                            highFrequencyProcGroups.add((AwareProcessBlockInfo) procGroups.get(i));
                            procGroups.remove(i);
                            break;
                        }
                    } else if (uid < LifeCycleStateMachine.TIME_OUT_TIME) {
                        continue;
                    } else if (PackageTracker.getInstance().isPackageCriticalFrequency(packageName, uid)) {
                        AwareLog.w(TAG, "updateGroupList package: not kill critical frequent=" + packageName + ", uid=" + uid);
                        procGroups.remove(i);
                        break;
                    } else if (PackageTracker.getInstance().isPackageHighFrequency(packageName, uid)) {
                        AwareLog.w(TAG, "updateGroupList package: move high frequent=" + packageName + ", uid=" + uid);
                        highFrequencyProcGroups.add((AwareProcessBlockInfo) procGroups.get(i));
                        procGroups.remove(i);
                        break;
                    }
                }
            }
            for (AwareProcessBlockInfo info : highFrequencyProcGroups) {
                procGroups.add(0, info);
            }
            AwareLog.d(TAG, "updateGroupList: oldSize=" + oldSize + ", newSize=" + procGroups.size());
        }
    }

    private List<String> getPackageList(List<AwareProcessInfo> procInfoList) {
        List<String> packageList = new ArrayList();
        if (procInfoList == null) {
            return packageList;
        }
        for (AwareProcessInfo info : procInfoList) {
            if (!(info.mProcInfo == null || info.mProcInfo.mPackageName == null)) {
                for (String packageName : info.mProcInfo.mPackageName) {
                    if (!packageList.contains(packageName)) {
                        packageList.add(packageName);
                    }
                }
            }
        }
        return packageList;
    }

    private void insertDumpAndStatisticData(Bundle extras, AwareProcessBlockInfo procGroup, List<Integer> pids, long beginTime, int exeTime, long reqMem) {
        if (extras == null || procGroup == null || pids == null) {
            AwareLog.w(TAG, "insertDumpAndStatisticData: null procGroups");
            return;
        }
        List<AwareProcessInfo> procs = procGroup.getProcessList();
        String appName = extras.getString("appName");
        int event = extras.getInt("event");
        long timeStamp = extras.getLong("timeStamp");
        int cpuLoad = extras.getInt("cpuLoad");
        int effect = (pids.size() * 20480) / HwGlobalActionsData.FLAG_SILENTMODE_NORMAL;
        StringBuilder stringBuilder = new StringBuilder("[" + appName + "," + event + "," + timeStamp + "],[" + cpuLoad + "," + extras.getBoolean("cpuBusy") + "],[" + (reqMem / 1024) + "," + effect + "]");
        StringBuilder operationSB = new StringBuilder("Kill [");
        operationSB.append(procGroup.mUid).append(",");
        operationSB.append(getPackageList(procs)).append(",").append(pids).append("]");
        EventTracker.getInstance().insertDumpData(beginTime, operationSB.toString(), exeTime, stringBuilder.toString());
        EventTracker.getInstance().insertStatisticData("Kill", exeTime, effect);
    }
}
