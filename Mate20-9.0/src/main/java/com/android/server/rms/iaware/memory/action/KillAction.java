package com.android.server.rms.iaware.memory.action;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.rms.iaware.AwareLog;
import android.rms.iaware.memrepair.MemRepairPkgInfo;
import android.rms.iaware.memrepair.MemRepairProcInfo;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.rms.collector.ResourceCollector;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.memory.action.Action;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.memory.utils.PackageTracker;
import com.android.server.rms.iaware.srms.AppCleanupDumpRadar;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KillAction extends Action {
    private static final long ABNORM_KILL_INTERVAL = 600000;
    private static final int FREQUENCY_DEFAULT = 0;
    private static final int FREQUENCY_KILL = 3;
    private static final int FREQUENCY_MOVE = 1;
    private static final int FREQUENCY_PRIORITY = 2;
    private static final int MAX_PROCESS_KILL_COUNT = 5;
    private static final long MIN_KILL_GAP_MEMORY = 51200;
    private static final String TAG = "AwareMem_Kill";
    private final boolean MEM_EMERG_KILL = SystemProperties.getBoolean("persist.iaware.mem_emerg_kill", false);
    private int mInvaildKillCount = 0;
    private long mLastAbnormExec = 0;
    private long mLastExecTime = 0;
    private int mLastKillZeroCount = 0;
    private long mReqMem;

    public KillAction(Context context) {
        super(context);
    }

    public int execute(Bundle extras) {
        if (this.mContext == null) {
            AwareLog.e(TAG, "mContext = NULL!");
            return -1;
        } else if (extras == null) {
            AwareLog.e(TAG, "null extras!");
            return -1;
        } else {
            ProcessCleaner.getInstance(this.mContext).beginKillFast();
            int result = execKillAction(extras);
            ProcessCleaner.getInstance(this.mContext).endKillFast();
            return result;
        }
    }

    private int execKillAction(Bundle extras) {
        List<MemRepairPkgInfo> memRepairPkgInfoList;
        Bundle bundle = extras;
        int maxKillCount = 5;
        long availableRam = MemoryReader.getInstance().getMemAvailable();
        if (availableRam <= 0) {
            AwareLog.e(TAG, "execute faild to read availableRam =" + availableRam);
            return -1;
        }
        long normalWater = MemoryConstant.getMiddleWater();
        if (availableRam < normalWater) {
            maxKillCount = (int) (((long) 5) + 1 + ((normalWater - availableRam) / MemoryConstant.APP_AVG_USS));
        }
        int memCleanLevel = 0;
        if (this.mInvaildKillCount > 3 && AppMngConfig.getKillMoreFlag()) {
            memCleanLevel = 1;
        }
        AwareAppMngSortPolicy policy = getAwareAppMngSortPolicy(memCleanLevel);
        if (policy == null) {
            AwareLog.w(TAG, "getAppMngSortPolicy null policy!");
            return -1;
        } else if (this.mInterrupt.get()) {
            this.mLastKillZeroCount++;
            this.mInvaildKillCount++;
            return -1;
        } else {
            long j = normalWater;
            this.mReqMem = bundle.getLong("reqMem") + MIN_KILL_GAP_MEMORY;
            long targetReqMem = this.mReqMem;
            long criticalWater = MemoryConstant.getCriticalMemory();
            long emergWater = MemoryConstant.getEmergencyMemory();
            int killedCount = 0;
            long criticalWater2 = criticalWater;
            long now = SystemClock.elapsedRealtime();
            List<AwareProcessBlockInfo> procGroups = MemoryUtils.getAppMngProcGroup(policy, 2);
            if (procGroups == null || procGroups.isEmpty()) {
                List<AwareProcessBlockInfo> list = procGroups;
                AwareLog.w(TAG, "empty group list!");
            } else {
                updateGroupList(procGroups, 0);
                killedCount = execKillGroup(bundle, procGroups, maxKillCount, memCleanLevel);
                this.mLastExecTime = now;
                List<AwareProcessBlockInfo> list2 = procGroups;
            }
            StringBuilder sb = new StringBuilder();
            int i = memCleanLevel;
            sb.append("availableRam=");
            sb.append(availableRam);
            sb.append(",emergWater=");
            sb.append(emergWater);
            sb.append(",mReqMem=");
            long now2 = now;
            sb.append(this.mReqMem);
            AwareLog.i(TAG, sb.toString());
            long appMem = bundle.getLong("appMem");
            long emergReqMem = (targetReqMem - this.mReqMem) + availableRam;
            if (appMem > 0) {
                long j2 = targetReqMem;
                this.mReqMem = appMem - emergReqMem;
            } else {
                this.mReqMem = (MemoryConstant.getKillGapMemory() + emergWater) - emergReqMem;
            }
            if ((MemoryConstant.isCleanAllSwitch() || killedCount >= 5) && (!MemoryConstant.isCleanAllSwitch() || emergReqMem > MemoryConstant.getKillGapMemory() + emergWater)) {
                this.mInvaildKillCount = 0;
            } else {
                this.mInvaildKillCount++;
            }
            StringBuilder sb2 = new StringBuilder();
            sb2.append("mReqMem=");
            long emergReqMem2 = emergReqMem;
            sb2.append(this.mReqMem);
            sb2.append(",mInvaildKillCount=");
            sb2.append(this.mInvaildKillCount);
            sb2.append(",gap=");
            sb2.append(MemoryConstant.getKillGapMemory());
            AwareLog.i(TAG, sb2.toString());
            if (!MemoryConstant.isCleanAllSwitch() && this.MEM_EMERG_KILL && availableRam <= emergWater && maxKillCount > killedCount) {
                killedCount += excuteEmergKill(bundle, maxKillCount - killedCount, 2, 1);
            }
            boolean requestAppMem = appMem > 0 && this.mReqMem > 0;
            if (MemoryConstant.isCleanAllSwitch() && (emergReqMem2 < emergWater || requestAppMem)) {
                int freqType = 3;
                if (this.mLastKillZeroCount < 3) {
                    freqType = 2;
                }
                killedCount += excuteEmergKill(bundle, maxKillCount - killedCount, 4, freqType);
            }
            if (killedCount > 0) {
                this.mLastKillZeroCount = 0;
            } else {
                this.mLastKillZeroCount++;
            }
            StringBuilder sb3 = new StringBuilder();
            sb3.append("MEM_EMERG_KILL=");
            sb3.append(this.MEM_EMERG_KILL);
            sb3.append(";availableRam=");
            sb3.append(availableRam);
            sb3.append(";now=");
            int i2 = maxKillCount;
            long now3 = now2;
            sb3.append(now3);
            sb3.append(";mLastAbnormExec=");
            long j3 = appMem;
            sb3.append(this.mLastAbnormExec);
            AwareLog.d(TAG, sb3.toString());
            if (availableRam <= criticalWater2 && now3 - this.mLastAbnormExec > 600000) {
                AwareLog.d(TAG, "memRepairPkgInfoList=" + memRepairPkgInfoList);
                executeAbnormProcKill(memRepairPkgInfoList);
                this.mLastAbnormExec = now3;
            }
            AwareLog.i(TAG, "execute: killing count=" + killedCount);
            return killedCount > 0 ? 0 : -1;
        }
    }

    private int excuteEmergKill(Bundle extras, int maxKillCount, int memCleanLevel, int freqType) {
        AwareAppMngSortPolicy policy = getAwareAppMngSortPolicy(memCleanLevel);
        if (policy == null) {
            AwareLog.w(TAG, "getAppMngSortPolicy emerg level null policy!");
            return 0;
        }
        int killedCount = 0;
        long now = SystemClock.elapsedRealtime();
        List<AwareProcessBlockInfo> procGroups = MemoryUtils.getAppMngProcGroup(policy, 2);
        if (procGroups != null && !procGroups.isEmpty()) {
            updateGroupList(procGroups, freqType);
            killedCount = execKillGroup(extras, procGroups, maxKillCount, memCleanLevel);
            this.mLastExecTime = now;
        }
        AwareLog.i(TAG, "execute: killing emerg count=" + killedCount);
        return killedCount;
    }

    private void executeAbnormProcKill(List<MemRepairPkgInfo> memRepairPkgInfoList) {
        if (memRepairPkgInfoList == null) {
            AwareLog.d(TAG, "memRepairPkgInfoList is null");
            return;
        }
        AwareLog.d(TAG, "memRepairPkgInfoList size()=" + memRepairPkgInfoList.size());
        for (MemRepairPkgInfo memRepairPkgInfo : memRepairPkgInfoList) {
            if (!memRepairPkgInfo.getCanClean() || (8 & memRepairPkgInfo.getThresHoldType()) != 0) {
                AwareLog.d(TAG, "canClean=" + memRepairPkgInfo.getCanClean() + "ThresHoldType()=" + memRepairPkgInfo.getThresHoldType());
            } else {
                List<MemRepairProcInfo> memRepairProcInfoList = memRepairPkgInfo.getProcessList();
                if (memRepairProcInfoList != null) {
                    AwareLog.d(TAG, "memRepairProcInfoList.size()=" + memRepairProcInfoList.size());
                    for (MemRepairProcInfo memRepairProcInfo : memRepairProcInfoList) {
                        AwareLog.d(TAG, "proc name=" + memRepairProcInfo.getProcName() + ";pid=" + memRepairProcInfo.getPid());
                        ProcessCleaner.getInstance(this.mContext).killProcess(memRepairProcInfo.getPid(), true, "abnormProc");
                    }
                }
            }
        }
    }

    private AwareAppMngSortPolicy getAwareAppMngSortPolicy(int memCleanLevel) {
        AwareLog.i(TAG, "request grouplist level=" + memCleanLevel);
        return MemoryUtils.getAppMngSortPolicy(2, 3, memCleanLevel);
    }

    public void reset() {
        this.mLastExecTime = 0;
        this.mLastKillZeroCount = 0;
        this.mInvaildKillCount = 0;
    }

    public boolean canBeExecuted() {
        long availableRam = MemoryReader.getInstance().getMemAvailable();
        if (availableRam <= 0) {
            AwareLog.i(TAG, "canBeExecuted read availableRam err!" + availableRam);
            return false;
        }
        long maxStep = 3 * (MemoryConstant.getIdleMemory() - MemoryConstant.getEmergencyMemory());
        if (maxStep <= 0) {
            AwareLog.w(TAG, "Idle <= Emergency Memory! getIdleMemory=" + MemoryConstant.getIdleMemory() + ",getEmergencyMemory=" + MemoryConstant.getEmergencyMemory());
            return false;
        }
        long curStep = availableRam - MemoryConstant.getEmergencyMemory();
        long interval = ((curStep * curStep) * MemoryConstant.MIN_INTERVAL_OP_TIMEOUT) / ((100 * maxStep) * 1024);
        if (interval <= SystemClock.elapsedRealtime() - this.mLastExecTime) {
            return true;
        }
        AwareLog.i(TAG, "canBeExecuted waiting next operation, real interval=" + interval);
        return false;
    }

    public int getLastExecFailCount() {
        return this.mLastKillZeroCount;
    }

    private List<Integer> execChooseKill(AwareProcessBlockInfo procGroup, boolean needCheckAdj) {
        if (MemoryConstant.getCameraPreloadSwitch() == 1 && checkPreloadPackage(procGroup)) {
            return null;
        }
        if (MemoryConstant.isFastKillSwitch()) {
            AwareLog.d(TAG, "will fast kill, procGroupInfo: " + procGroup);
            return ProcessCleaner.getInstance(this.mContext).killProcessesSameUidFast(procGroup, this.mInterrupt, false, false, "LowMem", needCheckAdj);
        }
        return ProcessCleaner.getInstance(this.mContext).killProcessesSameUidExt(procGroup, this.mInterrupt, false, false, "LowMem", needCheckAdj);
    }

    private boolean checkPreloadPackage(AwareProcessBlockInfo procGroup) {
        if (procGroup == null) {
            return false;
        }
        String packageName = procGroup.mPackageName;
        List<AwareProcessInfo> processList = procGroup.getProcessList();
        if (packageName == null) {
            AwareLog.i(TAG, "checkPreloadPackage packageName is null");
            return false;
        } else if (processList == null) {
            AwareLog.i(TAG, "checkPreloadPackage processList is null");
            return false;
        } else {
            if (packageName.equals(MemoryConstant.CAMERA_PACKAGE_NAME) && processList.size() == 1) {
                AwareProcessInfo procInfo = processList.get(0);
                if (procInfo == null || procInfo.mProcInfo == null) {
                    AwareLog.w(TAG, "checkPreloadPackage  procInfo is null");
                    return false;
                }
                long[] outUss = new long[2];
                long pss = ResourceCollector.getPssFast(procInfo.mPid, outUss, null);
                if (pss <= 0) {
                    AwareLog.w(TAG, "checkPreloadPackage  pss = " + pss);
                    return false;
                }
                long uss = outUss[0] + (outUss[1] / 3);
                AwareLog.i(TAG, "checkPreloadPackage " + packageName + " uss=" + uss);
                if (uss < ((long) MemoryConstant.getCameraPreloadKillUss())) {
                    return true;
                }
            }
            return false;
        }
    }

    private int execKillGroup(Bundle extras, List<AwareProcessBlockInfo> procGroups, int maxKillCount, int memCleanLevel) {
        int killedPids;
        long beginTime;
        boolean needCheckAdj;
        int appUid;
        boolean z;
        int exeTime;
        long beginTime2;
        int position;
        int appUid2;
        boolean needCheckAdj2;
        boolean z2;
        int position2;
        List<Integer> pids;
        int exeTime2;
        Bundle bundle = extras;
        List<AwareProcessBlockInfo> list = procGroups;
        if (bundle == null || list == null || procGroups.size() < 1 || this.mContext == null) {
            int i = maxKillCount;
            List<AwareProcessBlockInfo> list2 = list;
            AwareLog.w(TAG, "execKillGroup: null procGroups");
            return 0;
        }
        long beginTime3 = 0;
        int exeTime3 = 0;
        int position3 = procGroups.size() - 1;
        int killedPids2 = 0;
        boolean needCheckAdj3 = memCleanLevel < 3;
        int appUid3 = bundle.getInt("appUid");
        int position4 = position3;
        while (true) {
            if (this.mReqMem <= 0 || position4 < 0) {
                beginTime = beginTime3;
                int i2 = exeTime3;
                int i3 = appUid3;
                boolean z3 = needCheckAdj3;
                int i4 = maxKillCount;
            } else if (this.mInterrupt.get()) {
                AwareLog.w(TAG, "execKillGroup: mInterrupt, return");
                beginTime = beginTime3;
                int i5 = exeTime3;
                int i6 = appUid3;
                boolean z4 = needCheckAdj3;
                int i7 = maxKillCount;
                break;
            } else {
                AwareProcessBlockInfo procGroup = list.get(position4);
                if (procGroup == null) {
                    AwareLog.w(TAG, "execKillGroup: null procGroup");
                    position4--;
                } else {
                    List<AwareProcessInfo> procs = procGroup.getProcessList();
                    if (procs == null) {
                        beginTime2 = beginTime3;
                        exeTime = exeTime3;
                        appUid2 = appUid3;
                        needCheckAdj2 = needCheckAdj3;
                        z2 = true;
                        int i8 = maxKillCount;
                    } else if (procs.size() < 1) {
                        beginTime2 = beginTime3;
                        exeTime = exeTime3;
                        z2 = true;
                        appUid2 = appUid3;
                        needCheckAdj2 = needCheckAdj3;
                        int i9 = maxKillCount;
                    } else {
                        Action.PkgMemHolder pkgMemHolder = new Action.PkgMemHolder(procGroup.mUid, procs);
                        AwareProcessInfo currentProcess = procs.get(0);
                        long beginTime4 = beginTime3;
                        String processName = currentProcess.mProcInfo != null ? currentProcess.mProcInfo.mProcessName : null;
                        if (appUid3 == procGroup.mUid) {
                            int exeTime4 = exeTime3;
                            StringBuilder sb = new StringBuilder();
                            AwareProcessInfo awareProcessInfo = currentProcess;
                            sb.append("execKillGroup: foreground ");
                            sb.append(processName);
                            AwareLog.i(TAG, sb.toString());
                            position4--;
                            beginTime3 = beginTime4;
                            exeTime3 = exeTime4;
                        } else {
                            AwareProcessInfo currentProcess2 = currentProcess;
                            long beginTime5 = System.currentTimeMillis();
                            List<Integer> pids2 = execChooseKill(procGroup, needCheckAdj3);
                            int exeTime5 = (int) (System.currentTimeMillis() - beginTime5);
                            if (pids2 != null) {
                                int killedPids3 = killedPids2 + pids2.size();
                                long killedMem = pkgMemHolder.getKilledMem(procGroup.mUid, pids2);
                                appUid = appUid3;
                                String processName2 = processName;
                                Bundle bundle2 = bundle;
                                exeTime2 = exeTime5;
                                AwareProcessInfo awareProcessInfo2 = currentProcess2;
                                killedPids = killedPids3;
                                pids = pids2;
                                Action.PkgMemHolder pkgMemHolder2 = pkgMemHolder;
                                z = true;
                                position2 = position4;
                                needCheckAdj = needCheckAdj3;
                                insertDumpAndStatisticData(bundle2, procGroup, pids2, beginTime5, exeTime2, this.mReqMem, killedMem);
                                this.mReqMem -= killedMem;
                                EventTracker.getInstance().trackEvent(1002, 0, 0, "uid:" + procGroup.mUid + ", proc:" + processName + ",weight:" + procGroup.mWeight);
                                PackageTracker.getInstance().trackKillEvent(procGroup.mUid, procs);
                                if (!MemoryConstant.isExactKillSwitch()) {
                                    if (killedPids >= maxKillCount) {
                                        AwareLog.i(TAG, "execKillGroup: Had killed process count=" + killedPids + ", return");
                                        int i10 = exeTime2;
                                        List<Integer> list3 = pids;
                                        position4 = position2;
                                        break;
                                    }
                                } else {
                                    int i11 = maxKillCount;
                                }
                                killedPids2 = killedPids;
                            } else {
                                int i12 = maxKillCount;
                                exeTime2 = exeTime5;
                                pids = pids2;
                                Action.PkgMemHolder pkgMemHolder3 = pkgMemHolder;
                                position2 = position4;
                                appUid = appUid3;
                                needCheckAdj = needCheckAdj3;
                                AwareProcessInfo awareProcessInfo3 = currentProcess2;
                                z = true;
                                EventTracker.getInstance().trackEvent(1002, 0, 0, "uid:" + procGroup.mUid + ", proc:" + processName + " fail ");
                            }
                            position = position2 - 1;
                            int i13 = memCleanLevel;
                            beginTime3 = beginTime5;
                            exeTime3 = exeTime2;
                            List<Integer> list4 = pids;
                            boolean z5 = z;
                            appUid3 = appUid;
                            needCheckAdj3 = needCheckAdj;
                            bundle = extras;
                            list = procGroups;
                        }
                    }
                    AwareLog.w(TAG, "execKillGroup: null process list. uid:" + procGroup.mUid + ", position:" + position4);
                    position = position4 + -1;
                    int i14 = memCleanLevel;
                    beginTime3 = beginTime2;
                    exeTime3 = exeTime;
                    boolean z52 = z;
                    appUid3 = appUid;
                    needCheckAdj3 = needCheckAdj;
                    bundle = extras;
                    list = procGroups;
                }
            }
        }
        beginTime = beginTime3;
        int i22 = exeTime3;
        int i32 = appUid3;
        boolean z32 = needCheckAdj3;
        int i42 = maxKillCount;
        killedPids = killedPids2;
        long j = beginTime;
        AppCleanupDumpRadar.getInstance().reportMemoryData(procGroups, position4);
        return killedPids;
    }

    private void updateGroupList(List<AwareProcessBlockInfo> procGroups, int freqType) {
        List<AwareProcessBlockInfo> list = procGroups;
        int i = freqType;
        if (!PackageTracker.getInstance().isEnabled() || list == null) {
        } else if (i != 3) {
            List<AwareProcessBlockInfo> highFrequencyProcGroups = new ArrayList<>();
            int oldSize = procGroups.size();
            int i2 = procGroups.size() - 1;
            while (true) {
                int i3 = 0;
                if (i2 < 0) {
                    break;
                }
                List<AwareProcessInfo> processList = list.get(i2).mProcessList;
                if (processList != null) {
                    int uid = list.get(i2).mUid;
                    Iterator<String> it = getPackageList(processList).iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        String packageName = it.next();
                        if (uid < 10000 && processList.get(i3) != null && processList.get(i3).mProcInfo != null) {
                            PackageTracker.KilledFrequency freq = PackageTracker.getInstance().getProcessKilledFrequency(packageName, processList.get(i3).mProcInfo.mProcessName, uid);
                            if (freq == PackageTracker.KilledFrequency.FREQUENCY_CRITICAL) {
                                AwareLog.w(TAG, "updateGroupList process: not kill critical frequent=" + firstProcessname + ", uid=" + uid);
                                if (i == 1) {
                                    highFrequencyProcGroups.add(list.get(i2));
                                }
                                list.remove(i2);
                            } else if (freq == PackageTracker.KilledFrequency.FREQUENCY_HIGH) {
                                if (i == 2) {
                                    AwareLog.w(TAG, "updateGroupList process: skip move high frequent=" + firstProcessname + ", uid=" + uid);
                                } else {
                                    AwareLog.w(TAG, "updateGroupList process: move high frequent=" + firstProcessname + ", uid=" + uid);
                                    highFrequencyProcGroups.add(list.get(i2));
                                    list.remove(i2);
                                }
                            }
                        } else if (uid >= 10000) {
                            PackageTracker.KilledFrequency freq2 = PackageTracker.getInstance().getPackageKilledFrequency(packageName, uid);
                            if (freq2 == PackageTracker.KilledFrequency.FREQUENCY_CRITICAL) {
                                AwareLog.w(TAG, "updateGroupList package: not kill critical frequent=" + packageName + ", uid=" + uid);
                                if (i == 1) {
                                    highFrequencyProcGroups.add(list.get(i2));
                                }
                                list.remove(i2);
                            } else if (freq2 == PackageTracker.KilledFrequency.FREQUENCY_HIGH) {
                                if (i == 2) {
                                    AwareLog.w(TAG, "updateGroupList package: skip move high frequent=" + packageName + ", uid=" + uid);
                                } else {
                                    AwareLog.w(TAG, "updateGroupList package: move high frequent=" + packageName + ", uid=" + uid);
                                    highFrequencyProcGroups.add(list.get(i2));
                                    list.remove(i2);
                                }
                            }
                        } else {
                            continue;
                        }
                        i3 = 0;
                    }
                }
                i2--;
            }
            for (AwareProcessBlockInfo info : highFrequencyProcGroups) {
                list.add(0, info);
            }
            AwareLog.d(TAG, "updateGroupList: oldSize=" + oldSize + ", newSize=" + procGroups.size());
        }
    }

    @SuppressLint({"PreferForInArrayList"})
    private List<String> getPackageList(List<AwareProcessInfo> procInfoList) {
        List<String> packageList = new ArrayList<>();
        if (procInfoList == null) {
            return packageList;
        }
        for (AwareProcessInfo info : procInfoList) {
            if (!(info.mProcInfo == null || info.mProcInfo.mPackageName == null)) {
                Iterator it = info.mProcInfo.mPackageName.iterator();
                while (it.hasNext()) {
                    String packageName = (String) it.next();
                    if (!packageList.contains(packageName)) {
                        packageList.add(packageName);
                    }
                }
            }
        }
        return packageList;
    }

    private void insertDumpAndStatisticData(Bundle extras, AwareProcessBlockInfo procGroup, List<Integer> pids, long beginTime, int exeTime, long reqMem, long killedMem) {
        Bundle bundle = extras;
        AwareProcessBlockInfo awareProcessBlockInfo = procGroup;
        List<Integer> list = pids;
        if (bundle == null || awareProcessBlockInfo == null || list == null) {
            int i = exeTime;
            long j = killedMem;
            AwareLog.w(TAG, "insertDumpAndStatisticData: null procGroups");
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
        StringBuilder operationSB = new StringBuilder("Kill [");
        operationSB.append(awareProcessBlockInfo.mUid);
        operationSB.append(",");
        List<String> packages = getPackageList(procs);
        operationSB.append(packages);
        List<String> list2 = packages;
        operationSB.append(",");
        operationSB.append(list);
        operationSB.append("]");
        EventTracker.getInstance().insertDumpData(beginTime, operationSB.toString(), exeTime, reasonSB.toString());
        EventTracker.getInstance().insertStatisticData("Kill", exeTime, effect);
    }
}
