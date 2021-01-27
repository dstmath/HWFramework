package com.android.server.rms.iaware.memory.action;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.rms.iaware.memrepair.MemRepairPkgInfo;
import android.rms.iaware.memrepair.MemRepairProcInfo;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.collector.ResourceCollector;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.memory.action.Action;
import com.android.server.rms.iaware.memory.policy.CachedMemoryCleanPolicy;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.memory.utils.PackageTracker;
import com.android.server.rms.iaware.srms.AppCleanupDumpRadar;
import com.android.server.rms.memrepair.MemRepairPolicy;
import com.android.server.rms.memrepair.ProcStateStatisData;
import com.android.server.rms.memrepair.SystemAppMemRepairMng;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class KillAction extends Action {
    private static final long ABNORM_KILL_INTERVAL = 600000;
    private static final int FREQUENCY_DEFAULT = 0;
    private static final int FREQUENCY_KILL = 3;
    private static final int FREQUENCY_PRIORITY = 2;
    private static final int MAX_PROCESS_KILL_COUNT = 5;
    private static final long MIN_KILL_GAP_MEMORY = 51200;
    private static final long NO_KILL_LOG_PRINT_INTERVAL = 60000;
    private static final long SECOND_MS_FACTOR = 1000;
    private static final String TAG = "AwareMem_Kill";
    private long mEmergReqMem = 0;
    private int mInvaildKillCount = 0;
    private long mLastAbnormExec = 0;
    private long mLastExecTime = 0;
    private int mLastKillZeroCount = 0;
    private long mLastPrintLog = 0;
    private final boolean mMemEmergKill = SystemPropertiesEx.getBoolean("persist.iaware.mem_emerg_kill", false);
    private long mReqMem = 0;

    public KillAction(Context context) {
        super(context);
    }

    @Override // com.android.server.rms.iaware.memory.action.Action
    public int execute(Bundle extras) {
        if (this.mContext == null) {
            AwareLog.w(TAG, "mContext = NULL!");
            return -1;
        } else if (extras == null) {
            AwareLog.w(TAG, "null extras!");
            return -1;
        } else {
            int result = executeKillAction(extras);
            if (result == -1) {
                printAppMemInfo();
            }
            return result;
        }
    }

    private int executeKillAction(Bundle extras) {
        ProcessCleaner.getInstance(this.mContext).beginKillFast();
        int result = execKillAction(extras);
        ProcessCleaner.getInstance(this.mContext).endKillFast();
        return result;
    }

    private int executeKillPolicy(Bundle extras, AwareAppMngSortPolicy policy, long availableRam, int maxKillCount, int memCleanLevel) {
        boolean isAbnorm;
        int killedCount = 0;
        long now = SystemClock.elapsedRealtime();
        List<AwareProcessBlockInfo> procGroups = MemoryUtils.getAppMngProcGroup(policy, 2);
        int freqType = decideKillFreqType(memCleanLevel);
        long reqMemBeforeKill = this.mReqMem;
        if (procGroups == null || procGroups.isEmpty()) {
            AwareLog.w(TAG, "empty group list!");
        } else {
            updateGroupList(procGroups, freqType);
            MemoryUtils.sortByImportRecentIfNeed(procGroups, memCleanLevel);
            int killedCount2 = execKillGroup(extras, procGroups, maxKillCount, memCleanLevel);
            SystemAppMemRepairMng.getInstance().reportData(20029);
            this.mLastExecTime = now;
            killedCount = killedCount2;
        }
        if (memCleanLevel != 0 || killedCount >= 5) {
            isAbnorm = true;
            this.mInvaildKillCount = 0;
        } else {
            isAbnorm = true;
            this.mInvaildKillCount++;
        }
        updateLastKillZeroCount(killedCount);
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "mMemEmergKill = " + this.mMemEmergKill + "; availableRam = " + availableRam + "; now = " + now + "; mLastAbnormExec = " + this.mLastAbnormExec);
        }
        long criticalWater = MemoryConstant.getCriticalMemory();
        if (!MemoryConstant.isCleanAllSwitch() || availableRam > criticalWater || now - this.mLastAbnormExec <= 600000) {
            isAbnorm = false;
        }
        if (isAbnorm) {
            List<MemRepairPkgInfo> memRepairPkgInfoList = MemRepairPolicy.getInstance().getMemRepairPolicy(2);
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "memRepairPkgInfoList = " + memRepairPkgInfoList);
            }
            executeAbnormProcKill(memRepairPkgInfoList);
            this.mLastAbnormExec = now;
        }
        AwareLog.i(TAG, "execute: killing level " + memCleanLevel + ", count = " + killedCount + ",getUss = " + (reqMemBeforeKill - this.mReqMem));
        return killedCount;
    }

    private int execKillAction(Bundle extras) {
        int maxKillCount;
        long availableRam = MemoryReader.getInstance().getMemAvailable();
        if (availableRam <= 0) {
            AwareLog.e(TAG, "execute faild to read availableRam = " + availableRam);
            return -1;
        } else if (this.mInterrupt.get()) {
            this.mLastKillZeroCount++;
            this.mInvaildKillCount++;
            return -1;
        } else {
            int memCleanLevel = decideKillLevel(availableRam, extras);
            if (checkForClearCached(extras)) {
                return 0;
            }
            AwareAppMngSortPolicy policy = getAwareAppMngSortPolicy(memCleanLevel);
            if (policy == null) {
                AwareLog.w(TAG, "getAppMngSortPolicy null policy!");
                return -1;
            }
            long normalWater = MemoryConstant.getMiddleWater();
            if (availableRam < normalWater) {
                maxKillCount = (int) (((long) 5) + ((normalWater - availableRam) / MemoryConstant.APP_AVG_USS) + 1);
            } else {
                maxKillCount = 5;
            }
            if (executeKillPolicy(extras, policy, availableRam, maxKillCount, memCleanLevel) > 0) {
                return 0;
            }
            return -1;
        }
    }

    private boolean checkForClearCached(Bundle extras) {
        if (!CachedMemoryCleanPolicy.getInstance().isCachedMemoryEnable()) {
            return false;
        }
        execKillCachedAction(extras);
        if (this.mReqMem > 0) {
            return false;
        }
        if (!CachedMemoryCleanPolicy.getInstance().needPrintLog()) {
            return true;
        }
        AwareLog.i(TAG, " Cached clean is enough!");
        return true;
    }

    private void updateLastKillZeroCount(int killedCount) {
        if (killedCount > 0) {
            this.mLastKillZeroCount = 0;
        } else {
            this.mLastKillZeroCount++;
        }
    }

    private void executeAbnormProcKill(List<MemRepairPkgInfo> memRepairPkgInfoList) {
        if (memRepairPkgInfoList == null) {
            AwareLog.d(TAG, "memRepairPkgInfoList is null");
            return;
        }
        for (MemRepairPkgInfo memRepairPkgInfo : memRepairPkgInfoList) {
            if (memRepairPkgInfo.getCanClean() && (memRepairPkgInfo.getThresHoldType() & 8) == 0) {
                List<MemRepairProcInfo> memRepairProcInfoList = memRepairPkgInfo.getProcessList();
                if (memRepairProcInfoList != null) {
                    if (AwareLog.getDebugLogSwitch()) {
                        AwareLog.d(TAG, "memRepairProcInfoList.size() = " + memRepairProcInfoList.size());
                    }
                    printKilledPkgRecent(memRepairPkgInfo.getPkgName());
                    for (MemRepairProcInfo memRepairProcInfo : memRepairProcInfoList) {
                        if (AwareLog.getDebugLogSwitch()) {
                            AwareLog.d(TAG, "proc name = " + memRepairProcInfo.getProcName() + "; pid = " + memRepairProcInfo.getPid());
                        }
                        ProcessCleaner.getInstance(this.mContext).killProcess(memRepairProcInfo.getPid(), true, "abnormProc");
                    }
                }
            } else if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "canClean = " + memRepairPkgInfo.getCanClean() + "ThresHoldType() = " + memRepairPkgInfo.getThresHoldType());
            }
        }
    }

    private int decideKillLevel(long availableRam, Bundle extras) {
        int memCleanLevel;
        long appMem = extras.getLong("appMem");
        long emergWater = MemoryConstant.getEmergencyMemory();
        boolean isFixedKillSize = extras.getBoolean("isFixedKillSize");
        boolean sysPressureHigh = extras.getBoolean("sysPressHigh");
        this.mReqMem = extras.getLong("reqMem") + (isFixedKillSize ? 0 : MIN_KILL_GAP_MEMORY);
        if (appMem > 0) {
            this.mEmergReqMem = appMem - availableRam;
        } else {
            this.mEmergReqMem = (MemoryConstant.getKillGapMemory() + emergWater) - availableRam;
        }
        if (!isFixedKillSize && sysPressureHigh) {
            AwareLog.i(TAG, "system pressure is high, kill " + MemoryConstant.getConfigEnhancedKillSize() + "KB more");
            this.mEmergReqMem = this.mEmergReqMem + MemoryConstant.getConfigEnhancedKillSize();
            this.mReqMem = this.mReqMem + MemoryConstant.getConfigEnhancedKillSize();
        }
        boolean isRequestAppMem = appMem > 0 && this.mEmergReqMem > 0;
        if (!MemoryConstant.isCleanAllSwitch() && this.mMemEmergKill && availableRam <= emergWater) {
            memCleanLevel = 2;
        } else if (MemoryConstant.isCleanAllSwitch() && (availableRam < emergWater || isRequestAppMem)) {
            memCleanLevel = 4;
        } else if (this.mInvaildKillCount <= 3 || !AppMngConfig.getKillMoreFlag()) {
            memCleanLevel = 0;
        } else {
            memCleanLevel = 1;
        }
        AwareLog.i(TAG, "mReqMem = " + this.mReqMem + ", mEmergReqMem = " + this.mEmergReqMem + ", mInvaildKillCount = " + this.mInvaildKillCount + ", availableRam = " + availableRam + ", emergWater = " + emergWater + ", gap = " + MemoryConstant.getKillGapMemory());
        return memCleanLevel;
    }

    private int decideKillFreqType(int memCleanLevel) {
        if (memCleanLevel != 4) {
            return 0;
        }
        int freqType = 3;
        if (this.mLastKillZeroCount < 3) {
            freqType = 2;
        }
        return freqType;
    }

    private AwareAppMngSortPolicy getAwareAppMngSortPolicy(int memCleanLevel) {
        AwareLog.i(TAG, "request grouplist level = " + memCleanLevel);
        return MemoryUtils.getAppMngSortPolicy(2, 3, memCleanLevel);
    }

    @Override // com.android.server.rms.iaware.memory.action.Action
    public void reset() {
        this.mLastExecTime = 0;
        this.mLastKillZeroCount = 0;
        this.mInvaildKillCount = 0;
    }

    @Override // com.android.server.rms.iaware.memory.action.Action
    public boolean canBeExecuted() {
        long availableRam = MemoryReader.getInstance().getMemAvailable();
        if (availableRam <= 0) {
            AwareLog.e(TAG, "canBeExecuted read availableRam err!" + availableRam);
            return false;
        }
        long maxStep = (MemoryConstant.getIdleMemory() - MemoryConstant.getEmergencyMemory()) * 3;
        if (maxStep <= 0) {
            AwareLog.w(TAG, "Idle <= Emergency Memory! getIdleMemory = " + MemoryConstant.getIdleMemory() + ", getEmergencyMemory = " + MemoryConstant.getEmergencyMemory());
            return false;
        }
        long curStep = availableRam - MemoryConstant.getEmergencyMemory();
        long interval = ((curStep * curStep) * MemoryConstant.MIN_INTERVAL_OP_TIMEOUT) / ((100 * maxStep) * 1024);
        if (interval <= SystemClock.elapsedRealtime() - this.mLastExecTime) {
            return true;
        }
        AwareLog.i(TAG, "canBeExecuted waiting next operation, real interval = " + interval);
        return false;
    }

    @Override // com.android.server.rms.iaware.memory.action.Action
    public int getLastExecFailCount() {
        return this.mLastKillZeroCount;
    }

    private List<Integer> execChooseKill(AwareProcessBlockInfo procGroup, boolean needCheckAdj) {
        List<Integer> emptyPidList = new ArrayList<>();
        if (MemoryConstant.getCameraPreloadSwitch() == 1 && checkPreloadPackage(procGroup)) {
            return emptyPidList;
        }
        boolean[] params = {false, false, needCheckAdj};
        AwareIntelligentRecg.getInstance().reportAbnormalClean(procGroup);
        if (MemoryConstant.isFastKillSwitch()) {
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "will fast kill, procGroupInfo: " + procGroup);
            }
            List<Integer> res = ProcessCleaner.getInstance(this.mContext).killProcessesSameUidFast(procGroup, this.mInterrupt, "LowMem", params);
            return res == null ? emptyPidList : res;
        }
        List<Integer> res2 = ProcessCleaner.getInstance(this.mContext).killProcessesSameUidExt(procGroup, this.mInterrupt, "LowMem", params);
        return res2 == null ? emptyPidList : res2;
    }

    private boolean checkPreloadPackage(AwareProcessBlockInfo procGroup) {
        if (procGroup == null) {
            return false;
        }
        String packageName = procGroup.procPackageName;
        List<AwareProcessInfo> processList = procGroup.getProcessList();
        if (packageName == null) {
            AwareLog.w(TAG, "checkPreloadPackage packageName is null");
            return false;
        } else if (processList == null) {
            AwareLog.w(TAG, "checkPreloadPackage processList is null");
            return false;
        } else {
            if ("com.huawei.camera".equals(packageName) && processList.size() == 1) {
                AwareProcessInfo procInfo = processList.get(0);
                if (procInfo == null || procInfo.procProcInfo == null) {
                    AwareLog.w(TAG, "checkPreloadPackage procInfo is null");
                    return false;
                }
                long[] outUss = new long[2];
                if (ResourceCollector.getPss(procInfo.procPid, outUss, (long[]) null) > 0 && outUss[0] + (outUss[1] / 3) < ((long) MemoryConstant.getCameraPreloadKillUss())) {
                    return true;
                }
            }
            return false;
        }
    }

    /* JADX INFO: Multiple debug info for r12v6 'pids'  java.util.List<java.lang.Integer>: [D('isLegalParam' boolean), D('pids' java.util.List<java.lang.Integer>)] */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x010e, code lost:
        r15 = r7;
     */
    private int execKillGroup(Bundle extras, List<AwareProcessBlockInfo> procGroups, int maxNum, int memLevel) {
        int position;
        boolean isNeedCheckAdj;
        boolean isLegalParam;
        int appUid;
        AwareProcessBlockInfo procGroup;
        List<Integer> pids;
        boolean z = false;
        boolean isLegalParam2 = extras == null || procGroups == null || this.mContext == null;
        if (!isLegalParam2) {
            if (!procGroups.isEmpty()) {
                int position2 = procGroups.size() - 1;
                int killedPids = 0;
                long reqMem = this.mReqMem;
                if (memLevel < 3) {
                    z = true;
                }
                boolean isNeedCheckAdj2 = z;
                int appUid2 = extras.getInt("appUid");
                boolean isLevelListEmpty = false;
                long totalKilledMem = 0;
                long reqMem2 = reqMem;
                int position3 = position2;
                while (true) {
                    if (reqMem2 <= 0 || position3 < 0) {
                        break;
                    } else if (this.mInterrupt.get()) {
                        AwareLog.w(TAG, "execKillGroup: mInterrupt, return");
                        position = position3;
                        break;
                    } else {
                        AwareProcessBlockInfo procGroup2 = procGroups.get(position3);
                        if (!checkProcGroup(procGroup2, appUid2, position3)) {
                            position3--;
                        } else {
                            Action.PkgMemHolder pkgMemHolder = new Action.PkgMemHolder(procGroup2.procUid, procGroup2.getProcessList());
                            printKilledPkgRecent(procGroup2.procPackageName);
                            long beginTime = System.currentTimeMillis();
                            List<Integer> pids2 = execChooseKill(procGroup2, isNeedCheckAdj2);
                            int exeTime = (int) (System.currentTimeMillis() - beginTime);
                            if (!pids2.isEmpty()) {
                                int killedPids2 = killedPids + pids2.size();
                                long killedMem = pkgMemHolder.getKilledMem(procGroup2.procUid, pids2);
                                isLegalParam = isLegalParam2;
                                pids = pids2;
                                procGroup = procGroup2;
                                appUid = appUid2;
                                isNeedCheckAdj = isNeedCheckAdj2;
                                position = position3;
                                insertDumpAndStatisticData(extras, procGroup, pids, new KilledInfo(beginTime, exeTime, this.mReqMem, killedMem));
                                totalKilledMem += killedMem;
                                reqMem2 -= killedMem;
                                if (isLevelListEmpty || procGroup.procWeight == -1) {
                                    killedPids = killedPids2;
                                } else {
                                    reqMem2 -= this.mReqMem - this.mEmergReqMem;
                                    isLevelListEmpty = true;
                                    killedPids = killedPids2;
                                }
                            } else {
                                procGroup = procGroup2;
                                appUid = appUid2;
                                isLegalParam = isLegalParam2;
                                isNeedCheckAdj = isNeedCheckAdj2;
                                pids = pids2;
                                position = position3;
                            }
                            if (trackKillEvent(pids, procGroup, killedPids, maxNum)) {
                                break;
                            }
                            position3 = position - 1;
                            appUid2 = appUid;
                            isLegalParam2 = isLegalParam;
                            isNeedCheckAdj2 = isNeedCheckAdj;
                        }
                    }
                }
                this.mReqMem -= totalKilledMem;
                this.mEmergReqMem -= totalKilledMem;
                AppCleanupDumpRadar.getInstance().reportMemoryData(procGroups, position);
                return killedPids;
            }
        }
        AwareLog.w(TAG, "execKillGroup: null procGroups");
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x006b  */
    private boolean trackKillEvent(List<Integer> pids, AwareProcessBlockInfo procGroup, int killedPids, int maxKillCount) {
        boolean isKilled;
        List<AwareProcessInfo> procs = procGroup.getProcessList();
        AwareProcessInfo currentProcess = procs.get(0);
        String processName = currentProcess.procProcInfo != null ? currentProcess.procProcInfo.mProcessName : null;
        if (!pids.isEmpty()) {
            EventTracker.getInstance().trackEvent(1002, 0, 0, "uid: " + procGroup.procUid + ", proc: " + processName + ", weight:" + procGroup.procWeight);
            PackageTracker.getInstance().trackKillEvent(procGroup.procUid, procs);
            if (!MemoryConstant.isExactKillSwitch()) {
                if (killedPids >= maxKillCount) {
                    isKilled = true;
                    if (isKilled) {
                        AwareLog.i(TAG, "execKillGroup: Had killed process count = " + killedPids + ", return");
                        return true;
                    }
                }
            }
            isKilled = false;
            if (isKilled) {
            }
        } else {
            EventTracker.getInstance().trackEvent(1002, 0, 0, "uid: " + procGroup.procUid + ", proc: " + processName + " fail ");
        }
        return false;
    }

    private boolean checkProcGroup(AwareProcessBlockInfo procGroup, int appUid, int position) {
        if (procGroup == null) {
            AwareLog.w(TAG, "execKillGroup: null procGroup");
            return false;
        }
        List<AwareProcessInfo> procs = procGroup.getProcessList();
        if (procs == null || procs.size() < 1) {
            AwareLog.w(TAG, "execKillGroup: null process list. uid: " + procGroup.procUid + ", position: " + position);
            return false;
        }
        AwareProcessInfo currentProcess = procs.get(0);
        String processName = currentProcess.procProcInfo != null ? currentProcess.procProcInfo.mProcessName : null;
        if (appUid != procGroup.procUid) {
            return true;
        }
        AwareLog.i(TAG, "execKillGroup: foreground processName: " + processName);
        return false;
    }

    private PackageTracker.KilledFrequency getKilledFrequency(int uid, String packageName, List<AwareProcessInfo> processList) {
        PackageTracker.KilledFrequency freq = PackageTracker.KilledFrequency.FREQUENCY_NORMAL;
        if (uid >= 10000) {
            return PackageTracker.getInstance().getPackageKilledFrequency(packageName, uid);
        }
        if (processList.get(0) == null || processList.get(0).procProcInfo == null) {
            AwareLog.w(TAG, "first application in process list is null");
            return freq;
        }
        return PackageTracker.getInstance().getProcessKilledFrequency(packageName, processList.get(0).procProcInfo.mProcessName, uid);
    }

    private boolean execFrequencyPolicy(PackageTracker.KilledFrequency freq, int freqType, List<AwareProcessBlockInfo> highFrequencyProcGroups, List<AwareProcessBlockInfo> procGroups, int pos) {
        String packageName = procGroups.get(pos).procPackageName;
        int i = AnonymousClass1.$SwitchMap$com$android$server$rms$iaware$memory$utils$PackageTracker$KilledFrequency[freq.ordinal()];
        if (i == 1) {
            AwareLog.i(TAG, "not kill " + freq + "," + packageName);
            procGroups.remove(pos);
            return true;
        } else if (i != 2) {
            return i != 3 ? false : false;
        } else {
            if (freqType == 2) {
                AwareLog.i(TAG, "skip " + freq + "," + packageName);
                return true;
            }
            AwareLog.i(TAG, "move " + freq + "," + packageName);
            highFrequencyProcGroups.add(procGroups.get(pos));
            procGroups.remove(pos);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.rms.iaware.memory.action.KillAction$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$rms$iaware$memory$utils$PackageTracker$KilledFrequency = new int[PackageTracker.KilledFrequency.values().length];

        static {
            try {
                $SwitchMap$com$android$server$rms$iaware$memory$utils$PackageTracker$KilledFrequency[PackageTracker.KilledFrequency.FREQUENCY_CRITICAL.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$memory$utils$PackageTracker$KilledFrequency[PackageTracker.KilledFrequency.FREQUENCY_HIGH.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$memory$utils$PackageTracker$KilledFrequency[PackageTracker.KilledFrequency.FREQUENCY_NORMAL.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0020 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0021  */
    private void updateGroupList(List<AwareProcessBlockInfo> procGroups, int freqType) {
        boolean isLegal;
        if (PackageTracker.getInstance().isEnabled() && procGroups != null) {
            if (freqType != 3) {
                isLegal = false;
                if (isLegal) {
                    List<AwareProcessBlockInfo> highFrequencyProcGroups = new ArrayList<>();
                    if (AwareLog.getDebugLogSwitch()) {
                        AwareLog.d(TAG, "before killAction, oldSize = " + procGroups.size());
                    }
                    int i = procGroups.size() - 1;
                    while (i >= 0) {
                        List<AwareProcessInfo> processList = procGroups.get(i).procProcessList;
                        if (processList != null) {
                            int uid = procGroups.get(i).procUid;
                            Iterator<String> it = getPackageList(processList).iterator();
                            while (it.hasNext() && !execFrequencyPolicy(getKilledFrequency(uid, it.next(), processList), freqType, highFrequencyProcGroups, procGroups, i)) {
                            }
                        }
                        i--;
                    }
                    for (AwareProcessBlockInfo info : highFrequencyProcGroups) {
                        procGroups.add(0, info);
                    }
                    if (AwareLog.getDebugLogSwitch()) {
                        AwareLog.d(TAG, "after killAction, newSize = " + procGroups.size());
                        return;
                    }
                    return;
                }
                return;
            }
        }
        isLegal = true;
        if (isLegal) {
        }
    }

    @SuppressLint({"PreferForInArrayList"})
    private List<String> getPackageList(List<AwareProcessInfo> procInfoList) {
        List<String> packageList = new ArrayList<>();
        if (procInfoList == null) {
            return packageList;
        }
        for (AwareProcessInfo info : procInfoList) {
            if (!(info.procProcInfo == null || info.procProcInfo.mPackageName == null)) {
                Iterator it = info.procProcInfo.mPackageName.iterator();
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

    /* access modifiers changed from: private */
    public class KilledInfo {
        long mBeginTime = 0;
        int mExeTime = 0;
        long mKilledMem = 0;
        long mReqMem = 0;

        KilledInfo(long beginTime, int exeTime, long reqMem, long killedMem) {
            this.mBeginTime = beginTime;
            this.mExeTime = exeTime;
            this.mReqMem = reqMem;
            this.mKilledMem = killedMem;
        }
    }

    private void insertDumpAndStatisticData(Bundle extras, AwareProcessBlockInfo procGroup, List<Integer> pids, KilledInfo killedInfo) {
        if (extras != null && procGroup != null) {
            if (pids != null) {
                List<AwareProcessInfo> procs = procGroup.getProcessList();
                String appName = extras.getString("appName");
                int event = extras.getInt("event");
                long timeStamp = extras.getLong("timeStamp");
                int cpuLoad = extras.getInt("cpuLoad");
                boolean cpuBusy = extras.getBoolean("cpuBusy");
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
                sb.append(cpuBusy);
                sb.append("],[");
                sb.append(killedInfo.mReqMem / 1024);
                sb.append(",");
                sb.append(effect);
                StringBuilder reasonSb = sb.append("]");
                EventTracker.getInstance().insertDumpData(killedInfo.mBeginTime, "Kill [" + procGroup.procUid + "," + getPackageList(procs) + "," + pids + "]", killedInfo.mExeTime, reasonSb.toString());
                EventTracker.getInstance().insertStatisticData("Kill", killedInfo.mExeTime, effect);
                return;
            }
        }
        AwareLog.w(TAG, "insertDumpAndStatisticData: null procGroups");
    }

    private void printKilledPkgRecent(String pkgName) {
        if (pkgName != null) {
            AwareUserHabit habit = AwareUserHabit.getInstance();
            if (habit == null) {
                AwareLog.w(TAG, "AwareUserHabit is null");
                return;
            }
            LinkedHashMap<String, Long> lru = habit.getLruCache();
            if (lru != null) {
                Long recent = lru.get(pkgName);
                if (recent == null) {
                    AwareLog.d(TAG, "pkgName: " + pkgName + "; no last recent use time");
                    return;
                }
                AwareLog.i(TAG, "pkgName: " + pkgName + "; last recent use time: " + String.valueOf(recent) + "; backgroundTime: " + String.valueOf((SystemClock.elapsedRealtime() - recent.longValue()) / 1000) + "s");
            }
        }
    }

    private void printAppMemInfo() {
        long now = SystemClock.elapsedRealtime();
        if (now - this.mLastPrintLog >= 60000) {
            try {
                AwareLog.i(TAG, "begin print app memInfo when nothing to kill");
                List<ActivityManager.RunningAppProcessInfo> procs = ActivityManagerEx.getRunningAppProcesses();
                if (procs == null) {
                    return;
                }
                if (procs.size() >= 1) {
                    ProcStateStatisData handle = ProcStateStatisData.getInstance();
                    for (ActivityManager.RunningAppProcessInfo proc : procs) {
                        if (proc != null) {
                            long uss = handle.getProcUss(proc.uid, proc.pid);
                            AwareLog.i(TAG, "alive processName: " + proc.processName + ", pid: " + proc.pid + ", uss: " + uss);
                        }
                    }
                    AwareLog.i(TAG, "end print app memInfo when nothing to kill");
                    this.mLastPrintLog = now;
                }
            } catch (RemoteException e) {
                AwareLog.e(TAG, "am.getRunningAppProcess() failed");
            }
        }
    }

    private void execKillCachedAction(Bundle extras) {
        AwareAppMngSortPolicy policy;
        if (CachedMemoryCleanPolicy.getInstance().needPrintLog()) {
            AwareLog.i(TAG, " requst mem : " + this.mReqMem);
        }
        if (!this.mInterrupt.get() && (policy = MemoryUtils.getCachedCleanPolicy()) != null) {
            execKillCacheGroup(extras, MemoryUtils.getAppMngProcGroup(policy, 2));
        }
    }

    private int getBeginIndex(List<AwareProcessBlockInfo> procGroups) {
        if (CachedMemoryCleanPolicy.getInstance().needPrintLog()) {
            AwareLog.i(TAG, " Begin cached clean kill, kill size : " + procGroups.size());
        }
        return procGroups.size() - 1;
    }

    private void execKillCacheGroup(Bundle extras, List<AwareProcessBlockInfo> procGroups) {
        AwareProcessInfo procInfo;
        List<AwareProcessBlockInfo> list = procGroups;
        if (this.mContext != null && list != null) {
            if (!procGroups.isEmpty()) {
                int index = getBeginIndex(list);
                long reqMem = this.mReqMem;
                long totalKilledMem = 0;
                while (true) {
                    if (reqMem <= 0 || index < 0) {
                        break;
                    } else if (this.mInterrupt.get()) {
                        AwareLog.d(TAG, "execKillCacheGroup: mInterrupt, return");
                        break;
                    } else {
                        AwareProcessBlockInfo procBlockInfo = list.get(index);
                        if (!checkProcGroup(procBlockInfo, extras.getInt("appUid"), index)) {
                            index--;
                        } else {
                            List<AwareProcessInfo> procProcessList = procBlockInfo.procProcessList;
                            if (procProcessList == null || procProcessList.isEmpty() || (procInfo = procProcessList.get(0)) == null || procInfo.procProcInfo == null) {
                                list = procGroups;
                                reqMem = reqMem;
                            } else {
                                CachedMemoryCleanPolicy.getInstance().updateCachedMemoryCleanRecord(procBlockInfo.procUid, procBlockInfo.procPackageName, procInfo.procProcInfo.mProcessName, true);
                                Action.PkgMemHolder pkgMemHolder = new Action.PkgMemHolder(procBlockInfo.procUid, procBlockInfo.getProcessList());
                                List<Integer> pids = chooseKillForCached(procBlockInfo, procInfo.procPid);
                                if (pids != null) {
                                    long killedMem = pkgMemHolder.getKilledMem(procBlockInfo.procUid, pids);
                                    totalKilledMem += killedMem;
                                    reqMem -= killedMem;
                                } else {
                                    CachedMemoryCleanPolicy.getInstance().updateCachedMemoryCleanRecord(procBlockInfo.procUid, procBlockInfo.procPackageName, procInfo.procProcInfo.mProcessName, false);
                                    reqMem = reqMem;
                                }
                                index--;
                                list = procGroups;
                            }
                        }
                    }
                }
                if (CachedMemoryCleanPolicy.getInstance().needPrintLog()) {
                    AwareLog.i(TAG, " Cached clean kill total : " + totalKilledMem);
                }
                updateMemInfo(totalKilledMem);
            }
        }
    }

    private List<Integer> chooseKillForCached(AwareProcessBlockInfo procGroup, int pid) {
        List<Integer> pids = new ArrayList<>();
        if (MemoryConstant.isFastKillSwitch()) {
            if (CachedMemoryCleanPolicy.getInstance().needPrintLog()) {
                AwareLog.i(TAG, " chooseKillForCached use quick kill!");
            }
            List<Integer> res = ProcessCleaner.getInstance(this.mContext).killProcessesSameUidFast(procGroup, this.mInterrupt, "CachedClean", new boolean[]{false, false, false});
            return res == null ? pids : res;
        }
        if (CachedMemoryCleanPolicy.getInstance().needPrintLog()) {
            AwareLog.i(TAG, " chooseKillForCached use ori kill!");
        }
        if (ProcessCleaner.getInstance().killProcess(pid, true, "CachedClean")) {
            pids.add(Integer.valueOf(pid));
        }
        return pids;
    }

    private void updateMemInfo(long totalKilledMem) {
        this.mReqMem -= totalKilledMem;
        this.mEmergReqMem -= totalKilledMem;
    }
}
