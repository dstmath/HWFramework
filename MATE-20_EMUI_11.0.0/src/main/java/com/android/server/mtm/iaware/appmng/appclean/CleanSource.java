package com.android.server.mtm.iaware.appmng.appclean;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.rule.ListItem;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.rms.dualfwk.AwareMiddleware;
import com.android.server.rms.iaware.cpu.CpuVipThread;
import com.android.server.rms.iaware.feature.AppSceneMngFeature;
import com.android.server.rms.iaware.srms.AppCleanupDumpRadar;
import com.huawei.android.content.pm.IPackageManagerExt;
import com.huawei.android.os.ProcessExt;
import com.huawei.android.os.UserHandleEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class CleanSource {
    private static final int BIGGER = 1;
    private static final Comparator<AwareProcessBlockInfo> BLOCK_BY_PACKAGE = new Comparator<AwareProcessBlockInfo>() {
        /* class com.android.server.mtm.iaware.appmng.appclean.CleanSource.AnonymousClass1 */

        public int compare(AwareProcessBlockInfo arg0, AwareProcessBlockInfo arg1) {
            if (arg0 == null) {
                return arg1 == null ? 0 : -1;
            }
            if (arg1 == null) {
                return 1;
            }
            if (arg0.procPackageName == null) {
                return arg1.procPackageName == null ? 0 : -1;
            }
            if (arg1.procPackageName == null) {
                return 1;
            }
            if (!arg1.procPackageName.equals(arg0.procPackageName)) {
                return arg0.procPackageName.compareTo(arg1.procPackageName);
            }
            if (arg0.procUid == arg1.procUid) {
                return 0;
            }
            if (arg0.procUid < arg1.procUid) {
                return -1;
            }
            return 1;
        }
    };
    private static final String BLOCK_INFO_LIST = "blockInfoList";
    private static final String CLEAN_BLOCK_LIST = "cleanBlock";
    private static final int EQUAL = 0;
    protected static final int FAKE_ADJ = 10001;
    protected static final int FAKE_PID = 99999;
    protected static final int FAKE_UID = 99999;
    private static final String PROTECTED_BLOCK_LIST = "protectedBlock";
    private static final int SMALLER = -1;
    private static final String TAG = "CleanSource";

    public void clean() {
    }

    public static List<AwareProcessBlockInfo> mergeBlockForMemory(List<AwareProcessBlockInfo> rawBlock, ArrayMap<String, ListItem> processList) {
        if (rawBlock == null || rawBlock.isEmpty()) {
            return rawBlock;
        }
        return mergeBlockInternal(rawBlock, true, null, processList);
    }

    public static List<AwareProcessBlockInfo> mergeBlock(List<AwareProcessBlockInfo> rawBlock) {
        if (rawBlock == null || rawBlock.isEmpty()) {
            return rawBlock;
        }
        return mergeBlockInternal(rawBlock, false, null, null);
    }

    public static List<AwareProcessBlockInfo> mergeBlock(List<AwareProcessBlockInfo> rawBlock, List<ProcessCleaner.CleanType> priority) {
        if (rawBlock == null || rawBlock.isEmpty()) {
            return rawBlock;
        }
        return mergeBlockInternal(rawBlock, false, priority, null);
    }

    private static void setProperFlag(AwareProcessBlockInfo curBlock, boolean forMemory) {
        if (ProcessCleaner.CleanType.FORCE_STOP.equals(curBlock.procCleanType) || ProcessCleaner.CleanType.FORCE_STOP_REMOVE_TASK.equals(curBlock.procCleanType)) {
            curBlock.procResCleanAllow = true;
        } else if (ProcessCleaner.CleanType.FORCE_STOP_ALARM.equals(curBlock.procCleanType)) {
            curBlock.procResCleanAllow = true;
            curBlock.procCleanAlarm = true;
            curBlock.procIsNativeForceStop = false;
        } else if (forMemory && ProcessCleaner.CleanType.KILL_ALLOW_START.equals(curBlock.procCleanType) && curBlock.procProcessList != null) {
            curBlock.procResCleanAllow = false;
            curBlock.procIsNativeForceStop = false;
            for (AwareProcessInfo procInfo : curBlock.procProcessList) {
                if (procInfo != null) {
                    procInfo.procRestartFlag = true;
                }
            }
        }
    }

    private static List<AwareProcessBlockInfo> mergeBlockInternal(List<AwareProcessBlockInfo> rawBlock, boolean forMemory, List<ProcessCleaner.CleanType> priority, ArrayMap<String, ListItem> processList) {
        Collections.sort(rawBlock, BLOCK_BY_PACKAGE);
        AwareProcessBlockInfo lastBlock = null;
        Map<String, List<AwareProcessBlockInfo>> groupBy = new HashMap<>(3);
        groupBy.put(CLEAN_BLOCK_LIST, new ArrayList<>());
        groupBy.put(PROTECTED_BLOCK_LIST, new ArrayList<>());
        groupBy.put(BLOCK_INFO_LIST, new ArrayList<>());
        Iterator<AwareProcessBlockInfo> iterator = rawBlock.iterator();
        while (iterator.hasNext()) {
            AwareProcessBlockInfo curBlock = iterator.next();
            iterator.remove();
            if (curBlock == null) {
                AwareLog.e(TAG, "bad decide result curBlock == null");
                return null;
            }
            setProperFlag(curBlock, forMemory);
            if (lastBlock == null) {
                if (!ProcessCleaner.CleanType.NONE.equals(curBlock.procCleanType)) {
                    checkProcInProcesslist(curBlock, processList, groupBy.get(BLOCK_INFO_LIST));
                }
                lastBlock = curBlock;
            } else if (lastBlock.procPackageName == null || !lastBlock.procPackageName.equals(curBlock.procPackageName) || UserHandleEx.getUserId(curBlock.procUid) != UserHandleEx.getUserId(lastBlock.procUid)) {
                handleBlockWithProcessList(lastBlock, groupBy, processList, priority, forMemory);
                mergeBlockToList(lastBlock, groupBy.get(CLEAN_BLOCK_LIST), groupBy.get(PROTECTED_BLOCK_LIST), forMemory);
                groupBy.get(BLOCK_INFO_LIST).clear();
                if (!ProcessCleaner.CleanType.NONE.equals(curBlock.procCleanType)) {
                    checkProcInProcesslist(curBlock, processList, groupBy.get(BLOCK_INFO_LIST));
                }
                lastBlock = curBlock;
            } else {
                if (!ProcessCleaner.CleanType.NONE.equals(lastBlock.procCleanType) && !ProcessCleaner.CleanType.NONE.equals(curBlock.procCleanType)) {
                    checkProcInProcesslist(curBlock, processList, groupBy.get(BLOCK_INFO_LIST));
                } else if (AppSceneMngFeature.isEnable() && !ProcessCleaner.CleanType.NONE.equals(curBlock.procCleanType)) {
                    checkProcInProcesslist(curBlock, processList, groupBy.get(BLOCK_INFO_LIST));
                }
                mergeBlockInfo(lastBlock, curBlock, priority);
            }
        }
        handleBlockWithProcessList(lastBlock, groupBy, processList, priority, forMemory);
        mergeBlockToList(lastBlock, groupBy.get(CLEAN_BLOCK_LIST), groupBy.get(PROTECTED_BLOCK_LIST), forMemory);
        rawBlock.addAll(groupBy.get(PROTECTED_BLOCK_LIST));
        return groupBy.get(CLEAN_BLOCK_LIST);
    }

    private static boolean isProcessListSupportedCleanType(int cleanType) {
        if (cleanType == ProcessCleaner.CleanType.NONE.ordinal() || cleanType == ProcessCleaner.CleanType.KILL_ALLOW_START.ordinal()) {
            return true;
        }
        return false;
    }

    private static void checkProcInProcesslist(AwareProcessBlockInfo block, ArrayMap<String, ListItem> processList, List<AwareProcessBlockInfo> blockInfoList) {
        ListItem item;
        if (block != null && block.procProcessList != null && processList != null && blockInfoList != null) {
            Iterator<AwareProcessInfo> iterator = block.procProcessList.iterator();
            while (iterator.hasNext()) {
                AwareProcessInfo procInfo = iterator.next();
                if (procInfo == null || procInfo.procProcInfo == null || procInfo.procProcInfo.mProcessName == null) {
                    iterator.remove();
                } else if (procInfo.procListPolicy != 1 && (((item = processList.get(procInfo.procProcInfo.mProcessName)) == null || !AppSceneMngFeature.isEnable() || isProcessListSupportedCleanType(item.getPolicy())) && item != null && item.getPolicy() < block.procCleanType.ordinal())) {
                    blockInfoList.add(new AwareProcessBlockInfo(block.procReason, block.procUid, procInfo, block.procCleanType.ordinal(), block.procDetailedReason));
                    iterator.remove();
                }
            }
            if (block.procProcessList.isEmpty()) {
                block.procProcessList = null;
            }
        }
    }

    private static void handleBlockWithProcessList(AwareProcessBlockInfo lastBlock, Map<String, List<AwareProcessBlockInfo>> groupBy, ArrayMap<String, ListItem> processList, List<ProcessCleaner.CleanType> priority, boolean forMemory) {
        List<AwareProcessBlockInfo> cleanBlock = groupBy.get(CLEAN_BLOCK_LIST);
        List<AwareProcessBlockInfo> protectedBlock = groupBy.get(PROTECTED_BLOCK_LIST);
        List<AwareProcessBlockInfo> blockInfoList = groupBy.get(BLOCK_INFO_LIST);
        if (!(lastBlock == null || blockInfoList == null || cleanBlock == null || processList == null || protectedBlock == null)) {
            if (AppSceneMngFeature.isEnable() || !ProcessCleaner.CleanType.NONE.equals(lastBlock.procCleanType)) {
                boolean isChanged = mergeBlockWithProcessList(lastBlock, groupBy, processList, priority, forMemory);
                if (lastBlock.procProcessList != null) {
                    if (isChanged && lastBlock.procCleanType.ordinal() > ProcessCleaner.CleanType.KILL_ALLOW_START.ordinal()) {
                        lastBlock.procCleanType = ProcessCleaner.CleanType.KILL_ALLOW_START;
                        StringBuilder sb = new StringBuilder();
                        String str = lastBlock.procReason;
                        sb.append(str.replaceFirst(RuleParserUtil.AppMngTag.POLICY.getDesc().toUpperCase() + ":\\d+", RuleParserUtil.AppMngTag.POLICY.getDesc().toUpperCase() + ":" + ProcessCleaner.CleanType.KILL_ALLOW_START.ordinal()));
                        sb.append(",");
                        sb.append(AppMngConstant.CleanReason.POLICY_DEGRADE.getCode());
                        lastBlock.procReason = sb.toString();
                        lastBlock.procDetailedReason.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(ProcessCleaner.CleanType.KILL_ALLOW_START.ordinal()));
                        setProperFlag(lastBlock, forMemory);
                        AwareLog.d(TAG, lastBlock.toString());
                        return;
                    }
                    return;
                }
                return;
            }
            for (AwareProcessBlockInfo block : blockInfoList) {
                mergeBlockInfo(lastBlock, block, priority);
            }
        }
    }

    private static boolean mergeBlockWithProcessList(AwareProcessBlockInfo lastBlock, Map<String, List<AwareProcessBlockInfo>> groupBy, ArrayMap<String, ListItem> processList, List<ProcessCleaner.CleanType> priority, boolean forMemory) {
        boolean isChanged = false;
        Iterator<AwareProcessBlockInfo> iterator = groupBy.get(BLOCK_INFO_LIST).iterator();
        while (iterator.hasNext()) {
            AwareProcessBlockInfo block = iterator.next();
            AwareProcessInfo procInfo = block.procProcessList.get(0);
            ListItem item = processList.get(procInfo.procProcInfo.mProcessName);
            if (item == null || (lastBlock.procProcessList != null && item.getPolicy() == lastBlock.procCleanType.ordinal())) {
                mergeBlockInfo(lastBlock, block, priority);
            } else {
                isChanged = true;
                ArrayMap<String, Integer> detailedReason = new ArrayMap<>();
                detailedReason.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(item.getPolicy()));
                detailedReason.put("spec", Integer.valueOf(AppMngConstant.CleanReason.PROCESS_LIST.ordinal()));
                AwareProcessBlockInfo tempBlock = new AwareProcessBlockInfo(AppMngConstant.CleanReason.PROCESS_LIST.getCode(), procInfo.procProcInfo.mUid, procInfo, item.getPolicy(), detailedReason);
                tempBlock.procPackageName = procInfo.procProcInfo.mProcessName;
                setProperFlag(tempBlock, forMemory);
                mergeBlockToList(tempBlock, groupBy.get(CLEAN_BLOCK_LIST), groupBy.get(PROTECTED_BLOCK_LIST), forMemory);
                AwareLog.d(TAG, tempBlock.toString());
            }
            iterator.remove();
        }
        return isChanged;
    }

    private static void mergeBlockToList(AwareProcessBlockInfo procBlock, List<AwareProcessBlockInfo> cleanBlock, List<AwareProcessBlockInfo> protectedBlock, boolean forMemory) {
        if (procBlock != null && cleanBlock != null && protectedBlock != null) {
            if (!forMemory || !ProcessCleaner.CleanType.NONE.equals(procBlock.procCleanType)) {
                procBlock.procUpdateTime = SystemClock.elapsedRealtime();
                cleanBlock.add(procBlock);
                return;
            }
            protectedBlock.add(procBlock);
        }
    }

    private static void mergeBlockInfo(AwareProcessBlockInfo lastBlock, AwareProcessBlockInfo curBlock, List<ProcessCleaner.CleanType> priority) {
        if (curBlock.procProcessList != null) {
            if (lastBlock.procProcessList == null) {
                lastBlock.procProcessList = new ArrayList();
            }
            if (curBlock.procProcessList != null) {
                lastBlock.procProcessList.addAll(curBlock.procProcessList);
                if (lastBlock.procMinAdj > curBlock.procMinAdj) {
                    lastBlock.procMinAdj = curBlock.procMinAdj;
                }
            }
            if (curBlock.procCleanType == null) {
                curBlock.procCleanType = ProcessCleaner.CleanType.NONE;
            }
            if (lastBlock.procCleanType == null) {
                lastBlock.procCleanType = ProcessCleaner.CleanType.NONE;
            }
            if (!lastBlock.procCleanType.equals(curBlock.procCleanType)) {
                if (priority != null) {
                    if (priority.indexOf(lastBlock.procCleanType) < priority.indexOf(curBlock.procCleanType)) {
                        lastBlock.procCleanType = curBlock.procCleanType;
                        lastBlock.procResCleanAllow = curBlock.procResCleanAllow;
                        lastBlock.procReason = curBlock.procReason;
                        lastBlock.procDetailedReason = curBlock.procDetailedReason;
                    }
                } else if (lastBlock.procCleanType.ordinal() > curBlock.procCleanType.ordinal()) {
                    lastBlock.procCleanType = curBlock.procCleanType;
                    lastBlock.procResCleanAllow = curBlock.procResCleanAllow;
                    lastBlock.procReason = curBlock.procReason;
                    lastBlock.procDetailedReason = curBlock.procDetailedReason;
                } else if (lastBlock.procWeight < curBlock.procWeight) {
                    lastBlock.procWeight = curBlock.procWeight;
                    return;
                } else {
                    return;
                }
                if (lastBlock.procWeight < curBlock.procWeight) {
                    lastBlock.procWeight = curBlock.procWeight;
                }
            }
        }
    }

    protected static AwareProcessInfo getDeadAwareProcInfo(String packageName, int userId) {
        int packageUid = 99999;
        try {
            packageUid = IPackageManagerExt.getPackageUid(packageName, 8192, userId);
        } catch (RemoteException e) {
            AwareLog.e(TAG, "Failed to get PackageManagerService!");
        }
        ProcessInfo fakeProcInfo = new ProcessInfo(99999, packageUid);
        fakeProcInfo.mCurAdj = FAKE_ADJ;
        fakeProcInfo.mPackageName.add(packageName);
        return new AwareProcessInfo(99999, fakeProcInfo);
    }

    /* access modifiers changed from: protected */
    public void uploadToBigData(AppMngConstant.AppCleanSource source, AwareProcessBlockInfo info) {
        if (info != null) {
            AppCleanupDumpRadar.getInstance().updateCleanData(info.procPackageName, source, info.procDetailedReason);
        }
    }

    public void updateHistory(AppMngConstant.AppCleanSource source, AwareProcessBlockInfo info) {
        if (info != null) {
            StringBuilder history = new StringBuilder();
            history.append("pkg = ");
            history.append(info.procPackageName);
            history.append(", uid = ");
            history.append(info.procUid);
            history.append(", reason = ");
            history.append(info.procReason);
            history.append(", type = ");
            history.append(info.procCleanType);
            if (AwareMiddleware.getInstance().isZApp(info.procPackageName)) {
                history.append(", ");
                history.append("Z");
                history.append(" = ");
                history.append("1");
            }
            DecisionMaker.getInstance().updateHistory(source, history.toString());
        }
    }

    public static boolean setSchedPriority() {
        boolean rtSchedSet = false;
        try {
            ProcessExt.setThreadScheduler(Process.myTid(), 1073741825, 1);
            ProcessExt.setThreadGroupAndCpuset(Process.myTid(), 10);
            rtSchedSet = true;
        } catch (IllegalArgumentException e) {
            AwareLog.w(TAG, "setThreadScheduler failed");
        } catch (SecurityException e2) {
            AwareLog.w(TAG, "setThreadScheduler not allowed");
        }
        if (!rtSchedSet) {
            List<Integer> tids = new ArrayList<>(1);
            tids.add(Integer.valueOf(Process.myTid()));
            CpuVipThread.getInstance().setAppVipThread(Process.myPid(), tids, true, false);
        }
        return rtSchedSet;
    }

    public static void resetSchedPriority(boolean rtSchedSet) {
        if (rtSchedSet) {
            try {
                ProcessExt.setThreadScheduler(Process.myTid(), 0, 0);
                ProcessExt.setThreadGroupAndCpuset(Process.myTid(), 2);
            } catch (IllegalArgumentException e) {
                AwareLog.w(TAG, "resetThreadScheduler failed");
            } catch (SecurityException e2) {
                AwareLog.w(TAG, "resetThreadScheduler not allowed");
            }
        } else {
            List<Integer> tids = new ArrayList<>(1);
            tids.add(Integer.valueOf(Process.myTid()));
            CpuVipThread.getInstance().setAppVipThread(Process.myPid(), tids, false, false);
        }
    }
}
