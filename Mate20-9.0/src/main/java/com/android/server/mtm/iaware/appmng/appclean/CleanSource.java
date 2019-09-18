package com.android.server.mtm.iaware.appmng.appclean;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.pm.IPackageManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.rule.ListItem;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.rms.iaware.srms.AppCleanupDumpRadar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public abstract class CleanSource {
    private static final Comparator<AwareProcessBlockInfo> BLOCK_BY_PACKAGE = new Comparator<AwareProcessBlockInfo>() {
        public int compare(AwareProcessBlockInfo arg0, AwareProcessBlockInfo arg1) {
            int i = -1;
            if (arg0 == null) {
                return arg1 == null ? 0 : -1;
            }
            if (arg1 == null) {
                return 1;
            }
            if (arg0.mPackageName == null) {
                return arg1.mPackageName == null ? 0 : -1;
            }
            if (arg1.mPackageName == null) {
                return 1;
            }
            if (!arg1.mPackageName.equals(arg0.mPackageName)) {
                return arg0.mPackageName.compareTo(arg1.mPackageName);
            }
            if (arg0.mUid == arg1.mUid) {
                return 0;
            }
            if (arg0.mUid >= arg1.mUid) {
                i = 1;
            }
            return i;
        }
    };
    protected static final int FAKE_ADJ = 10001;
    protected static final int FAKE_PID = 99999;
    protected static final int FAKE_UID = 99999;
    private static final String TAG = "CleanSource";

    public void clean() {
    }

    public static List<AwareProcessBlockInfo> mergeBlockForMemory(List<AwareProcessBlockInfo> rawBlock, ArrayMap<String, ListItem> processList) {
        return mergeBlockInternal(rawBlock, true, null, processList);
    }

    public static List<AwareProcessBlockInfo> mergeBlock(List<AwareProcessBlockInfo> rawBlock) {
        return mergeBlockInternal(rawBlock, false, null, null);
    }

    public static List<AwareProcessBlockInfo> mergeBlock(List<AwareProcessBlockInfo> rawBlock, List<ProcessCleaner.CleanType> priority) {
        return mergeBlockInternal(rawBlock, false, priority, null);
    }

    private static void setProperFlag(AwareProcessBlockInfo curBlock, boolean forMemory) {
        if (ProcessCleaner.CleanType.FORCESTOP.equals(curBlock.mCleanType) || ProcessCleaner.CleanType.FORCESTOP_REMOVETASK.equals(curBlock.mCleanType)) {
            curBlock.mResCleanAllow = true;
        } else if (ProcessCleaner.CleanType.FORCESTOP_ALARM.equals(curBlock.mCleanType)) {
            curBlock.mResCleanAllow = true;
            curBlock.mCleanAlarm = true;
            curBlock.mIsNativeForceStop = false;
        } else if (forMemory && ProcessCleaner.CleanType.KILL_ALLOW_START.equals(curBlock.mCleanType) && curBlock.mProcessList != null) {
            curBlock.mResCleanAllow = false;
            curBlock.mIsNativeForceStop = false;
            for (AwareProcessInfo procInfo : curBlock.mProcessList) {
                if (procInfo != null) {
                    procInfo.mRestartFlag = true;
                }
            }
        }
    }

    private static List<AwareProcessBlockInfo> mergeBlockInternal(List<AwareProcessBlockInfo> rawBlock, boolean forMemory, List<ProcessCleaner.CleanType> priority, ArrayMap<String, ListItem> processList) {
        List<AwareProcessBlockInfo> list = rawBlock;
        boolean z = forMemory;
        ArrayMap<String, ListItem> arrayMap = processList;
        if (list == null || rawBlock.isEmpty()) {
            return list;
        }
        Collections.sort(list, BLOCK_BY_PACKAGE);
        List<AwareProcessBlockInfo> cleanBlock = new ArrayList<>();
        List<AwareProcessBlockInfo> protectedBlock = new ArrayList<>();
        List<AwareProcessBlockInfo> tempBlockInfoList = new ArrayList<>();
        Iterator<AwareProcessBlockInfo> iterator = rawBlock.iterator();
        AwareProcessBlockInfo lastBlock = null;
        while (true) {
            Iterator<AwareProcessBlockInfo> iterator2 = iterator;
            if (iterator2.hasNext()) {
                AwareProcessBlockInfo curBlock = iterator2.next();
                iterator2.remove();
                if (curBlock == null) {
                    AwareLog.e(TAG, "bad decide result curBlock == null");
                    return null;
                }
                setProperFlag(curBlock, z);
                if (lastBlock == null) {
                    if (!ProcessCleaner.CleanType.NONE.equals(curBlock.mCleanType)) {
                        checkProcInProcesslist(curBlock, arrayMap, tempBlockInfoList);
                    }
                    lastBlock = curBlock;
                } else if (lastBlock.mPackageName == null || !lastBlock.mPackageName.equals(curBlock.mPackageName) || UserHandle.getUserId(curBlock.mUid) != UserHandle.getUserId(lastBlock.mUid)) {
                    handleBlockWithProcessList(lastBlock, cleanBlock, protectedBlock, tempBlockInfoList, arrayMap, priority, z);
                    mergeBlockToList(lastBlock, cleanBlock, protectedBlock, z);
                    tempBlockInfoList.clear();
                    if (!ProcessCleaner.CleanType.NONE.equals(curBlock.mCleanType)) {
                        checkProcInProcesslist(curBlock, arrayMap, tempBlockInfoList);
                    }
                    lastBlock = curBlock;
                } else {
                    if (!ProcessCleaner.CleanType.NONE.equals(lastBlock.mCleanType) && !ProcessCleaner.CleanType.NONE.equals(curBlock.mCleanType)) {
                        checkProcInProcesslist(curBlock, arrayMap, tempBlockInfoList);
                    }
                    mergeBlockInfo(lastBlock, curBlock, priority);
                }
                iterator = iterator2;
            } else {
                handleBlockWithProcessList(lastBlock, cleanBlock, protectedBlock, tempBlockInfoList, arrayMap, priority, z);
                mergeBlockToList(lastBlock, cleanBlock, protectedBlock, z);
                list.addAll(protectedBlock);
                return cleanBlock;
            }
        }
    }

    private static void checkProcInProcesslist(AwareProcessBlockInfo block, ArrayMap<String, ListItem> processList, List<AwareProcessBlockInfo> blockInfoList) {
        if (block != null && block.mProcessList != null && processList != null && blockInfoList != null) {
            Iterator<AwareProcessInfo> iterator = block.mProcessList.iterator();
            while (iterator.hasNext()) {
                AwareProcessInfo procInfo = iterator.next();
                if (procInfo == null || procInfo.mProcInfo == null || procInfo.mProcInfo.mProcessName == null) {
                    iterator.remove();
                } else {
                    ListItem item = processList.get(procInfo.mProcInfo.mProcessName);
                    if (item != null && item.getPolicy() < block.mCleanType.ordinal()) {
                        AwareProcessBlockInfo tempBlock = new AwareProcessBlockInfo(block.mReason, block.mUid, procInfo, block.mCleanType.ordinal(), block.mDetailedReason);
                        blockInfoList.add(tempBlock);
                        iterator.remove();
                    }
                }
            }
            if (block.mProcessList.isEmpty()) {
                block.mProcessList = null;
            }
        }
    }

    private static void handleBlockWithProcessList(AwareProcessBlockInfo lastBlock, List<AwareProcessBlockInfo> cleanBlock, List<AwareProcessBlockInfo> protectedBlock, List<AwareProcessBlockInfo> blockInfoList, ArrayMap<String, ListItem> processList, List<ProcessCleaner.CleanType> priority, boolean forMemory) {
        if (lastBlock != null && blockInfoList != null && cleanBlock != null && processList != null && protectedBlock != null) {
            if (ProcessCleaner.CleanType.NONE.equals(lastBlock.mCleanType)) {
                for (AwareProcessBlockInfo block : blockInfoList) {
                    mergeBlockInfo(lastBlock, block, priority);
                }
                return;
            }
            boolean isChanged = mergeBlockWithProcessList(lastBlock, cleanBlock, protectedBlock, blockInfoList, processList, priority, forMemory);
            if (lastBlock.mProcessList != null) {
                if (isChanged && lastBlock.mCleanType.ordinal() > ProcessCleaner.CleanType.KILL_ALLOW_START.ordinal()) {
                    lastBlock.mCleanType = ProcessCleaner.CleanType.KILL_ALLOW_START;
                    StringBuilder sb = new StringBuilder();
                    String str = lastBlock.mReason;
                    sb.append(str.replaceFirst(RuleParserUtil.AppMngTag.POLICY.getDesc().toUpperCase() + ":\\d+", RuleParserUtil.AppMngTag.POLICY.getDesc().toUpperCase() + ":" + ProcessCleaner.CleanType.KILL_ALLOW_START.ordinal()));
                    sb.append(",");
                    sb.append(AppMngConstant.CleanReason.POLICY_DEGRADE.getCode());
                    lastBlock.mReason = sb.toString();
                    lastBlock.mDetailedReason.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(ProcessCleaner.CleanType.KILL_ALLOW_START.ordinal()));
                    setProperFlag(lastBlock, forMemory);
                    AwareLog.d(TAG, lastBlock.toString());
                }
            }
        }
    }

    private static boolean mergeBlockWithProcessList(AwareProcessBlockInfo lastBlock, List<AwareProcessBlockInfo> cleanBlock, List<AwareProcessBlockInfo> protectedBlock, List<AwareProcessBlockInfo> blockInfoList, ArrayMap<String, ListItem> processList, List<ProcessCleaner.CleanType> priority, boolean forMemory) {
        AwareProcessBlockInfo awareProcessBlockInfo = lastBlock;
        boolean z = forMemory;
        boolean isChanged = false;
        Iterator<AwareProcessBlockInfo> iterator = blockInfoList.iterator();
        while (iterator.hasNext()) {
            AwareProcessBlockInfo block = iterator.next();
            AwareProcessInfo procInfo = block.mProcessList.get(0);
            ListItem item = processList.get(procInfo.mProcInfo.mProcessName);
            if (item == null || (awareProcessBlockInfo.mProcessList != null && item.getPolicy() >= awareProcessBlockInfo.mCleanType.ordinal())) {
                List<AwareProcessBlockInfo> list = cleanBlock;
                List<AwareProcessBlockInfo> list2 = protectedBlock;
                mergeBlockInfo(awareProcessBlockInfo, block, priority);
            } else {
                isChanged = true;
                ArrayMap<String, Integer> detailedReason = new ArrayMap<>();
                detailedReason.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(item.getPolicy()));
                detailedReason.put("spec", Integer.valueOf(AppMngConstant.CleanReason.PROCESSLIST.ordinal()));
                AwareProcessBlockInfo tempBlock = new AwareProcessBlockInfo(AppMngConstant.CleanReason.PROCESSLIST.getCode(), procInfo.mProcInfo.mUid, procInfo, item.getPolicy(), detailedReason);
                tempBlock.mPackageName = procInfo.mProcInfo.mProcessName;
                setProperFlag(tempBlock, z);
                mergeBlockToList(tempBlock, cleanBlock, protectedBlock, z);
                AwareLog.d(TAG, tempBlock.toString());
                List<ProcessCleaner.CleanType> list3 = priority;
            }
            iterator.remove();
        }
        List<AwareProcessBlockInfo> list4 = cleanBlock;
        List<AwareProcessBlockInfo> list5 = protectedBlock;
        ArrayMap<String, ListItem> arrayMap = processList;
        List<ProcessCleaner.CleanType> list6 = priority;
        return isChanged;
    }

    private static void mergeBlockToList(AwareProcessBlockInfo procBlock, List<AwareProcessBlockInfo> cleanBlock, List<AwareProcessBlockInfo> protectedBlock, boolean forMemory) {
        if (procBlock != null && cleanBlock != null && protectedBlock != null) {
            if (!forMemory || !ProcessCleaner.CleanType.NONE.equals(procBlock.mCleanType)) {
                procBlock.mUpdateTime = SystemClock.elapsedRealtime();
                cleanBlock.add(procBlock);
            } else {
                protectedBlock.add(procBlock);
            }
        }
    }

    private static void mergeBlockInfo(AwareProcessBlockInfo lastBlock, AwareProcessBlockInfo curBlock, List<ProcessCleaner.CleanType> priority) {
        if (curBlock.mProcessList != null) {
            if (lastBlock.mProcessList == null) {
                lastBlock.mProcessList = new ArrayList();
            }
            if (curBlock.mProcessList != null) {
                lastBlock.mProcessList.addAll(curBlock.mProcessList);
                if (lastBlock.mMinAdj > curBlock.mMinAdj) {
                    lastBlock.mMinAdj = curBlock.mMinAdj;
                }
            }
            if (curBlock.mCleanType == null) {
                curBlock.mCleanType = ProcessCleaner.CleanType.NONE;
            }
            if (lastBlock.mCleanType == null) {
                lastBlock.mCleanType = ProcessCleaner.CleanType.NONE;
            }
            if (!lastBlock.mCleanType.equals(curBlock.mCleanType)) {
                if (priority != null) {
                    if (priority.indexOf(lastBlock.mCleanType) < priority.indexOf(curBlock.mCleanType)) {
                        lastBlock.mCleanType = curBlock.mCleanType;
                        lastBlock.mResCleanAllow = curBlock.mResCleanAllow;
                        lastBlock.mReason = curBlock.mReason;
                        lastBlock.mDetailedReason = curBlock.mDetailedReason;
                    }
                } else if (lastBlock.mCleanType.ordinal() > curBlock.mCleanType.ordinal()) {
                    lastBlock.mCleanType = curBlock.mCleanType;
                    lastBlock.mResCleanAllow = curBlock.mResCleanAllow;
                    lastBlock.mReason = curBlock.mReason;
                    lastBlock.mDetailedReason = curBlock.mDetailedReason;
                }
                if (lastBlock.mWeight < curBlock.mWeight) {
                    lastBlock.mWeight = curBlock.mWeight;
                }
            }
        }
    }

    protected static AwareProcessInfo getDeadAwareProcInfo(String packageName, int userId) {
        int packageUid = 99999;
        try {
            IPackageManager pm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
            if (pm != null) {
                packageUid = pm.getPackageUid(packageName, 8192, userId);
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "Failed to get PackageManagerService!");
        }
        ProcessInfo fakeProcInfo = new ProcessInfo(99999, packageUid);
        fakeProcInfo.mCurAdj = 10001;
        fakeProcInfo.mPackageName.add(packageName);
        return new AwareProcessInfo(99999, fakeProcInfo);
    }

    /* access modifiers changed from: protected */
    public void uploadToBigData(AppMngConstant.AppCleanSource source, AwareProcessBlockInfo info) {
        if (info != null) {
            AppCleanupDumpRadar.getInstance().updateCleanData(info.mPackageName, source, info.mDetailedReason);
        }
    }

    public void updateHistory(AppMngConstant.AppCleanSource source, AwareProcessBlockInfo info) {
        if (info != null) {
            DecisionMaker.getInstance().updateHistory(source, "pkg = " + info.mPackageName + ", uid = " + info.mUid + ", reason = " + info.mReason + ", type = " + info.mCleanType);
        }
    }
}
