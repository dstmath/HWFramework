package com.android.server.mtm.iaware.appmng.appclean;

import android.app.ActivityManager;
import android.app.mtm.iaware.HwAppStartupSetting;
import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.app.mtm.iaware.appmng.IAppCleanCallback;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareAppCleanerForSm;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.SystemUnremoveUidCache;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.utils.AppStatusUtils;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.cpu.CpuCustBaseConfig;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.memrepair.SystemAppMemRepairMng;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HsmClean extends CleanSource {
    private static final boolean DEBUG = false;
    private static final int FORCESTOP_ORIDINARY = 2;
    private static final boolean IS_TV = "tv".equalsIgnoreCase(SystemPropertiesEx.get("ro.build.characteristics", MemoryConstant.MEM_SCENE_DEFAULT));
    private static final int KILL_ORIDINARY = 0;
    private static final Object LOCK = new Object();
    private static final int MAX_TASKS = 100;
    private static final int REMOVETASK_ORIDINARY = 1;
    private static final String TAG = "HSMClean";
    private static final int TASK_ID_INVALID = -1;
    private static volatile Boolean sIsExecutorThreadExist = false;
    private static LinkedList<AwareAppMngSortPolicy> sPolicyQueue = new LinkedList<>();
    private IAppCleanCallback mCallback;
    private Context mContext;
    private final HwActivityManagerService mHwAms = HwActivityManagerService.self();
    private AppCleanParam mParam;
    private SystemUnremoveUidCache mSystemUnremoveUidCache;

    public HsmClean(AppCleanParam param, IAppCleanCallback callback, Context context) {
        this.mParam = param;
        this.mContext = context;
        this.mCallback = callback;
        this.mSystemUnremoveUidCache = SystemUnremoveUidCache.getInstance(context);
    }

    @Override // com.android.server.mtm.iaware.appmng.appclean.CleanSource
    public void clean() {
        AppCleanParam appCleanParam = this.mParam;
        if (appCleanParam == null) {
            AwareLog.e(TAG, "mParam == null");
            return;
        }
        int action = appCleanParam.getAction();
        if (action == 0) {
            executeClean();
        } else if (action == 1) {
            getCleanList();
        }
    }

    private void executeClean() {
        reportSystemManagerCleanInBatch();
        List<AwareProcessBlockInfo> info = decideEnhanceClean(buildAwareProcBlockInfoList());
        Map<Integer, List<AwareProcessBlockInfo>> srcProcList = new ArrayMap<>();
        srcProcList.put(2, info);
        AwareAppMngSortPolicy policy = new AwareAppMngSortPolicy(this.mContext, srcProcList);
        synchronized (LOCK) {
            sPolicyQueue.add(policy);
            if (!sIsExecutorThreadExist.booleanValue()) {
                setExecutorThreadExist(true);
                Thread cleanExecutorThread = new Thread(new Runnable() {
                    /* class com.android.server.mtm.iaware.appmng.appclean.HsmClean.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        for (AwareAppMngSortPolicy policy = HsmClean.getNextPolicy(); policy != null; policy = HsmClean.getNextPolicy()) {
                            int killedNumber = AwareAppCleanerForSm.getInstance(HsmClean.this.mContext).execute(policy, null);
                            AwareLog.i(HsmClean.TAG, "appClean executor killed " + killedNumber + " processes for SM!");
                        }
                    }
                }, "AppCleanExecutor");
                cleanExecutorThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    /* class com.android.server.mtm.iaware.appmng.appclean.HsmClean.AnonymousClass2 */

                    @Override // java.lang.Thread.UncaughtExceptionHandler
                    public void uncaughtException(Thread thread, Throwable ex) {
                        AwareLog.e(HsmClean.TAG, "AppCleanExecutor was failed !!!");
                        HsmClean.setExecutorThreadExist(false);
                    }
                });
                cleanExecutorThread.start();
            }
        }
        int cnt = 0;
        for (AwareProcessBlockInfo infoItem : info) {
            if (!(infoItem.procCleanType == ProcessCleaner.CleanType.NONE || infoItem.procProcessList == null)) {
                cnt += infoItem.procProcessList.size();
            }
        }
        callbackAfterClean(policy, cnt);
    }

    /* access modifiers changed from: private */
    public static AwareAppMngSortPolicy getNextPolicy() {
        AwareAppMngSortPolicy policy = null;
        synchronized (LOCK) {
            if (!sPolicyQueue.isEmpty()) {
                policy = sPolicyQueue.remove();
            } else {
                setExecutorThreadExist(false);
            }
        }
        return policy;
    }

    /* access modifiers changed from: private */
    public static void setExecutorThreadExist(Boolean isExist) {
        sIsExecutorThreadExist = isExist;
    }

    private List<ActivityManager.RecentTaskInfo> getRecentTasks() {
        int currentUserId = AwareAppAssociate.getInstance().getCurUserId();
        List<ActivityManager.RecentTaskInfo> emptyList = new ArrayList<>();
        HwActivityManagerService hwActivityManagerService = this.mHwAms;
        if (hwActivityManagerService != null) {
            return hwActivityManagerService.getRecentTasksList(100, 2, currentUserId);
        }
        AwareLog.e(TAG, "Failed to get HwActivityManagerService");
        return emptyList;
    }

    private void updateTaskId(List<AwareProcessInfo> allAwareProcNeedProcess) {
        ArrayList<String> packageNames;
        if (allAwareProcNeedProcess == null) {
            AwareLog.e(TAG, "updateTaskId got null parameters!");
            return;
        }
        int currentUserId = AwareAppAssociate.getInstance().getCurUserId();
        for (AwareProcessInfo item : allAwareProcNeedProcess) {
            if (item != null && item.procProcInfo != null && (packageNames = item.procProcInfo.mPackageName) != null && !packageNames.isEmpty() && item.procTaskId == -1) {
                String destPackageName = packageNames.get(0);
                if (UserHandleEx.getUserId(item.procProcInfo.mUid) == currentUserId && destPackageName != null) {
                    Iterator<ActivityManager.RecentTaskInfo> iter = getRecentTasks().iterator();
                    while (true) {
                        if (!iter.hasNext()) {
                            break;
                        }
                        ActivityManager.RecentTaskInfo temp = iter.next();
                        Intent intent = new Intent(temp.baseIntent);
                        if (temp.origActivity != null) {
                            intent.setComponent(temp.origActivity);
                        }
                        if (intent.getComponent() != null && destPackageName.equals(intent.getComponent().getPackageName())) {
                            item.procTaskId = temp.persistentId;
                            break;
                        }
                    }
                }
            }
        }
    }

    private void callbackAfterClean(AwareAppMngSortPolicy policy, int killedCount) {
        List<AwareProcessBlockInfo> awareProcBlockList = policy.getAllowStopProcBlockList();
        if (awareProcBlockList == null) {
            AwareLog.e(TAG, "AwareAppMngSortPolicy is null!");
            return;
        }
        List<String> pkgList = new ArrayList<>();
        List<Integer> userIdList = new ArrayList<>();
        List<Integer> cleanTypeList = new ArrayList<>();
        for (AwareProcessBlockInfo item : awareProcBlockList) {
            if (!(item == null || item.procCleanType == ProcessCleaner.CleanType.NONE)) {
                pkgList.add(item.procPackageName);
                userIdList.add(Integer.valueOf(UserHandleEx.getUserId(item.procUid)));
                cleanTypeList.add(Integer.valueOf(item.procCleanType.ordinal()));
            }
        }
        AppCleanParam result = new AppCleanParam.Builder(this.mParam.getSource()).killedCount(killedCount).stringList(pkgList).intList(userIdList).intList2(cleanTypeList).build();
        IAppCleanCallback iAppCleanCallback = this.mCallback;
        if (iAppCleanCallback != null) {
            try {
                iAppCleanCallback.onCleanFinish(result);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "RemoteExcption e = " + e.toString());
            }
        }
        for (AwareProcessBlockInfo block : awareProcBlockList) {
            if (block != null) {
                AwareLog.i(TAG, "pkg = " + block.procPackageName + ", uid = " + block.procUid + ", policy = " + block.procCleanType + ", reason = " + block.procReason);
                if (block.procDetailedReason == null) {
                    block.procDetailedReason = new ArrayMap();
                }
                block.procDetailedReason.put(MemoryConstant.MEM_SYSTRIM_POLICY, Integer.valueOf(block.procCleanType.ordinal()));
                uploadToBigData(AppMngConstant.AppCleanSource.SYSTEM_MANAGER, block);
            }
        }
    }

    private void reportSystemManagerCleanInBatch() {
        List<AppCleanParam.AppCleanInfo> infoList = this.mParam.getAppCleanInfoList();
        if (infoList != null && infoList.size() > 1) {
            SystemAppMemRepairMng.getInstance().reportData(20031);
            AwareLog.d(TAG, "SMClean in batch appCleanInfoList.size()= " + infoList.size());
        }
    }

    private List<AwareProcessBlockInfo> buildAwareProcBlockInfoList() {
        List<AppCleanParam.AppCleanInfo> appCleanInfoList = this.mParam.getAppCleanInfoList();
        List<AwareProcessBlockInfo> info = new ArrayList<>();
        if (appCleanInfoList == null || appCleanInfoList.isEmpty()) {
            AwareLog.e(TAG, "got empty AppCleanInfoList for buildAwareProcBlockInfoList!");
            return info;
        }
        executeBuildAwareProcBlockInfoList(appCleanInfoList, info);
        return info;
    }

    private void executeBuildAwareProcBlockInfoList(List<AppCleanParam.AppCleanInfo> appCleanInfoList, List<AwareProcessBlockInfo> info) {
        ArrayList<AwareProcessInfo> allProcList = new ArrayList<>();
        boolean needUpdateTaskId = false;
        StringBuilder sb = new StringBuilder("appCleanInfos: ");
        for (AppCleanParam.AppCleanInfo appCleanInfo : appCleanInfoList) {
            if (appCleanInfo != null) {
                ArrayList<AwareProcessInfo> procList = AwareProcessInfo.getAwareProcInfosFromPackage(appCleanInfo.getPkgName(), appCleanInfo.getUserId().intValue());
                if (procList.isEmpty()) {
                    procList.add(getDeadAwareProcInfo(appCleanInfo.getPkgName(), appCleanInfo.getUserId().intValue()));
                }
                if (procList.get(0).procProcInfo != null) {
                    allProcList.addAll(procList);
                    AwareProcessBlockInfo item = new AwareProcessBlockInfo(procList.get(0).procProcInfo.mUid);
                    int cleanType = appCleanInfo.getCleanType().intValue();
                    if (cleanType == 0) {
                        item.procCleanType = ProcessCleaner.CleanType.KILL_ALLOW_START;
                    } else if (cleanType == 1) {
                        item.procCleanType = ProcessCleaner.CleanType.REMOVE_TASK;
                        if (appCleanInfo.getTaskId().intValue() == -1) {
                            needUpdateTaskId = true;
                        }
                    } else if (cleanType == 2) {
                        item.procCleanType = ProcessCleaner.CleanType.FORCE_STOP;
                    } else {
                        item.procCleanType = ProcessCleaner.CleanType.NONE;
                        AwareLog.e(TAG, "invalid clean type, don't clean. cleanType: " + cleanType);
                    }
                    item.procPackageName = appCleanInfo.getPkgName();
                    int size = procList.size();
                    for (int i = 0; i < size; i++) {
                        AwareProcessInfo awareProcInfo = procList.get(i);
                        if (cleanType == 1) {
                            awareProcInfo.procTaskId = appCleanInfo.getTaskId().intValue();
                        }
                        item.add(awareProcInfo);
                    }
                    info.add(item);
                    sb.append(appCleanInfo.getPkgName());
                    sb.append(",");
                    sb.append(appCleanInfo.getUserId());
                    sb.append(",");
                    sb.append(cleanType);
                    sb.append(",");
                    sb.append(appCleanInfo.getTaskId());
                    sb.append(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                }
            }
        }
        AwareLog.i(TAG, sb.toString());
        if (needUpdateTaskId) {
            updateTaskId(allProcList);
        }
    }

    private List<AwareProcessBlockInfo> decideEnhanceClean(List<AwareProcessBlockInfo> blockInfoList) {
        ArrayList<AwareProcessInfo> processList = new ArrayList<>();
        ArrayList<AwareProcessBlockInfo> resultList = new ArrayList<>();
        for (AwareProcessBlockInfo blockInfo : blockInfoList) {
            if (blockInfo.procCleanType != ProcessCleaner.CleanType.REMOVE_TASK || !shouldEnhance(blockInfo.procUid, blockInfo.procPackageName)) {
                resultList.add(blockInfo);
            } else {
                processList.addAll(blockInfo.procProcessList);
            }
        }
        List<AwareProcessBlockInfo> decisionBlockList = mergeBlock(DecisionMaker.getInstance().decideAll((List<AwareProcessInfo>) processList, 0, AppMngConstant.AppMngFeature.APP_CLEAN, (AppMngConstant.EnumWithDesc) AppMngConstant.AppCleanSource.SYSTEM_MANAGER));
        if (decisionBlockList != null) {
            for (AwareProcessBlockInfo blockInfo2 : decisionBlockList) {
                if (!(blockInfo2 == null || blockInfo2.procCleanType == ProcessCleaner.CleanType.FORCE_STOP_REMOVE_TASK)) {
                    blockInfo2.procCleanType = ProcessCleaner.CleanType.REMOVE_TASK;
                }
            }
            resultList.addAll(decisionBlockList);
        }
        return resultList;
    }

    private boolean needAppKeepAlv(String pkgName) {
        if (AppMngConfig.getAbroadFlag()) {
            return true;
        }
        AwareAppStartupPolicy appStart = AwareAppStartupPolicy.self();
        if (appStart == null) {
            return false;
        }
        return appStart.needAppKeepAlv(appStart.getAppStartupSetting(pkgName));
    }

    private boolean shouldEnhance(int uid, String pkgName) {
        HwAppStartupSetting setting;
        if (!AwareIntelligentRecg.getInstance().isAppMngEnhance() && needAppKeepAlv(pkgName)) {
            return false;
        }
        if (AppMngConfig.getAbroadFlag() && AppTypeRecoManager.getInstance().getAppWhereFrom(pkgName) != 0) {
            AwareLog.i(TAG, "shouldNotEnhance, abroad & not china top 3000 app, do not enhance: " + pkgName);
            return false;
        } else if (inAppStartBaseLine(uid, pkgName)) {
            return false;
        } else {
            if (AwareAppStartupPolicy.self() != null && (setting = AwareAppStartupPolicy.self().getAppStartupSetting(pkgName)) != null && setting.getModifier(0) == 2 && setting.getPolicy(3) == 1) {
                AwareLog.i(TAG, "shouldNotEnhance cust app and allow background: " + pkgName);
                return false;
            } else if (this.mHwAms.isPackageRunningOnPCMode(pkgName, uid)) {
                return false;
            } else {
                return true;
            }
        }
    }

    private boolean inAppStartBaseLine(int uid, String pkgName) {
        if (isSystemUnRemoveApp(uid)) {
            return true;
        }
        boolean allowSr = DecisionMaker.getInstance().getAppStartPolicy(pkgName, AppMngConstant.AppStartSource.SCHEDULE_RESTART) != 0;
        boolean allowAlarm = DecisionMaker.getInstance().getAppStartPolicy(pkgName, AppMngConstant.AppStartSource.ALARM) != 0;
        boolean isGmsApp = AwareIntelligentRecg.getInstance().isGmsApp(pkgName);
        if (!allowSr && !allowAlarm && !isGmsApp) {
            return false;
        }
        AwareLog.i(TAG, "inAppStartBaseLine: " + pkgName + ", allowSr: " + allowSr + ", allowAlarm: " + allowAlarm + ",isGmsApp: " + isGmsApp);
        return true;
    }

    private boolean isSystemUnRemoveApp(int uid) {
        int uid2 = UserHandleEx.getAppId(uid);
        if (uid2 > 0 && uid2 < 10000) {
            return true;
        }
        SystemUnremoveUidCache systemUnremoveUidCache = this.mSystemUnremoveUidCache;
        if (systemUnremoveUidCache == null || !systemUnremoveUidCache.checkUidExist(uid2)) {
            return false;
        }
        return true;
    }

    private void getCleanList() {
        List<AwareProcessInfo> allAwareProcNeedProcess = AppStatusUtils.getInstance().getAllProcNeedSort();
        if (allAwareProcNeedProcess == null || allAwareProcNeedProcess.isEmpty()) {
            AwareLog.e(TAG, "getAllProcNeedSort failed!");
            return;
        }
        List<AwareProcessBlockInfo> info = mergeBlock(DecisionMaker.getInstance().decideAll(allAwareProcNeedProcess, 1, AppMngConstant.AppMngFeature.APP_CLEAN, (AppMngConstant.EnumWithDesc) AppMngConstant.AppCleanSource.SYSTEM_MANAGER));
        if (info == null || info.isEmpty()) {
            AwareLog.e(TAG, "decideAll failed!");
            return;
        }
        List<AwareProcessBlockInfo> filteredAwareProcBlockInfoList = new ArrayList<>();
        for (AwareProcessBlockInfo infoItem : info) {
            if (infoItem != null) {
                if (infoItem.procCleanType == ProcessCleaner.CleanType.NONE) {
                    filteredAwareProcBlockInfoList.add(infoItem);
                } else if (!IS_TV && inAppStartBaseLine(infoItem.procUid, infoItem.procPackageName)) {
                    filteredAwareProcBlockInfoList.add(infoItem);
                }
            }
        }
        info.removeAll(filteredAwareProcBlockInfoList);
        Map<Integer, List<AwareProcessBlockInfo>> srcProcList = new ArrayMap<>();
        srcProcList.put(2, info);
        callbackBeforeClean(new AwareAppMngSortPolicy(this.mContext, srcProcList));
    }

    private void callbackBeforeClean(AwareAppMngSortPolicy policy) {
        List<AwareProcessBlockInfo> awareProcBlockList = policy.getAllowStopProcBlockList();
        if (awareProcBlockList == null) {
            AwareLog.e(TAG, "AwareAppMngSortPolicy is null!");
            return;
        }
        List<String> pkgList = new ArrayList<>();
        List<Integer> userIdList = new ArrayList<>();
        List<Integer> cleanTypeList = new ArrayList<>();
        for (AwareProcessBlockInfo item : awareProcBlockList) {
            if (item != null) {
                pkgList.add(item.procPackageName);
                userIdList.add(Integer.valueOf(UserHandleEx.getUserId(item.procUid)));
                cleanTypeList.add(Integer.valueOf(item.procCleanType.ordinal()));
            }
        }
        AppCleanParam result = new AppCleanParam.Builder(this.mParam.getSource()).stringList(pkgList).intList(userIdList).intList2(cleanTypeList).build();
        IAppCleanCallback iAppCleanCallback = this.mCallback;
        if (iAppCleanCallback != null) {
            try {
                iAppCleanCallback.onCleanFinish(result);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "RemoteExcption e = " + e.toString());
            }
        }
        for (AwareProcessBlockInfo block : awareProcBlockList) {
            if (block != null) {
                AwareLog.i(TAG, "pkg = " + block.procPackageName + ", uid = " + block.procUid + ", policy = " + block.procCleanType + ", reason = " + block.procReason);
                updateHistory(AppMngConstant.AppCleanSource.SYSTEM_MANAGER, block);
                uploadToBigData(AppMngConstant.AppCleanSource.SYSTEM_MANAGER, block);
            }
        }
    }
}
