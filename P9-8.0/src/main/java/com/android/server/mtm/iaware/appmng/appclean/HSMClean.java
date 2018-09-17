package com.android.server.mtm.iaware.appmng.appclean;

import android.app.ActivityManager.RecentTaskInfo;
import android.app.mtm.iaware.HwAppStartupSetting;
import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.AppCleanParam.AppCleanInfo;
import android.app.mtm.iaware.appmng.AppCleanParam.Builder;
import android.app.mtm.iaware.appmng.AppMngConstant.AppCleanSource;
import android.app.mtm.iaware.appmng.AppMngConstant.AppMngFeature;
import android.app.mtm.iaware.appmng.AppMngConstant.AppStartSource;
import android.app.mtm.iaware.appmng.IAppCleanCallback;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.UserHandle;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareAppCleanerForSM;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.SystemUnremoveUidCache;
import com.android.server.mtm.taskstatus.ProcessCleaner.CleanType;
import com.android.server.mtm.utils.AppStatusUtils;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HSMClean extends CleanSource {
    private static final boolean DEBUG = false;
    private static final int FORCESTOP_ORIDINARY = 2;
    private static final int KILL_ORIDINARY = 0;
    private static final int MAX_TASKS = 100;
    private static final int REMOVETASK_ORIDINARY = 1;
    private static final String TAG = "HSMClean";
    private static final int TASK_ID_INVALID = -1;
    private static volatile Boolean isExecutorThreadExist = Boolean.valueOf(false);
    private static LinkedList<AwareAppMngSortPolicy> policyQueue = new LinkedList();
    private IAppCleanCallback mCallback;
    private Context mContext;
    private final HwActivityManagerService mHwAMS = HwActivityManagerService.self();
    private AppCleanParam mParam;
    private SystemUnremoveUidCache mSystemUnremoveUidCache;

    public HSMClean(AppCleanParam param, IAppCleanCallback callback, Context context) {
        this.mParam = param;
        this.mContext = context;
        this.mCallback = callback;
        this.mSystemUnremoveUidCache = SystemUnremoveUidCache.getInstance(context);
    }

    public void clean() {
        if (this.mParam == null) {
            AwareLog.e(TAG, "null == mParam");
            return;
        }
        switch (this.mParam.getAction()) {
            case 0:
                executeClean();
                break;
            case 1:
                getCleanList();
                break;
        }
    }

    private void executeClean() {
        List<AwareProcessBlockInfo> info = decideEnhanceClean(buildAwareProcBlockInfoList());
        Map<Integer, List<AwareProcessBlockInfo>> srcProcList = new HashMap();
        srcProcList.put(Integer.valueOf(2), info);
        AwareAppMngSortPolicy policy = new AwareAppMngSortPolicy(this.mContext, srcProcList);
        synchronized (HSMClean.class) {
            policyQueue.add(policy);
            if (!isExecutorThreadExist.booleanValue()) {
                setExecutorThreadExist(Boolean.valueOf(true));
                new Thread(new Runnable() {
                    public void run() {
                        for (AwareAppMngSortPolicy policy = HSMClean.getNextPolicy(); policy != null; policy = HSMClean.getNextPolicy()) {
                            AwareLog.i(HSMClean.TAG, "appClean executor killed " + AwareAppCleanerForSM.getInstance(HSMClean.this.mContext).execute(policy, null) + " processes for SM!");
                        }
                    }
                }, "AppCleanExecutor").start();
            }
        }
        int cnt = 0;
        for (AwareProcessBlockInfo infoItem : info) {
            if (!(infoItem.mCleanType == CleanType.NONE || infoItem.mProcessList == null)) {
                cnt += infoItem.mProcessList.size();
            }
        }
        callbackAfterClean(policy, cnt);
    }

    private static AwareAppMngSortPolicy getNextPolicy() {
        AwareAppMngSortPolicy awareAppMngSortPolicy = null;
        synchronized (HSMClean.class) {
            if (policyQueue.size() > 0) {
                awareAppMngSortPolicy = (AwareAppMngSortPolicy) policyQueue.remove();
            } else {
                setExecutorThreadExist(Boolean.valueOf(false));
            }
        }
        return awareAppMngSortPolicy;
    }

    private static void setExecutorThreadExist(Boolean isExist) {
        isExecutorThreadExist = isExist;
    }

    private List<RecentTaskInfo> getRecentTasks() {
        int currentUserId = AwareAppAssociate.getInstance().getCurUserId();
        List<RecentTaskInfo> emptyList = new ArrayList();
        if (this.mHwAMS != null) {
            return this.mHwAMS.getRecentTasks(100, 62, currentUserId).getList();
        }
        AwareLog.e(TAG, "Failed to get HwActivityManagerService");
        return emptyList;
    }

    private void updateTaskId(List<AwareProcessInfo> allAwareProcNeedProcess) {
        if (allAwareProcNeedProcess == null) {
            AwareLog.e(TAG, "updateTaskId got null parameters!");
            return;
        }
        List<RecentTaskInfo> recentTasks = getRecentTasks();
        int currentUserId = AwareAppAssociate.getInstance().getCurUserId();
        for (AwareProcessInfo item : allAwareProcNeedProcess) {
            if (item != null && item.mProcInfo != null) {
                ArrayList<String> packageNames = item.mProcInfo.mPackageName;
                if (packageNames != null && !packageNames.isEmpty() && item.mTaskId == -1) {
                    String destPackageName = (String) packageNames.get(0);
                    if (UserHandle.getUserId(item.mProcInfo.mUid) == currentUserId && destPackageName != null) {
                        for (RecentTaskInfo t : recentTasks) {
                            Intent intent = new Intent(t.baseIntent);
                            if (t.origActivity != null) {
                                intent.setComponent(t.origActivity);
                            }
                            if (intent.getComponent() != null && destPackageName.equals(intent.getComponent().getPackageName())) {
                                item.mTaskId = t.persistentId;
                                break;
                            }
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
        List<String> pkgList = new ArrayList();
        List<Integer> userIdList = new ArrayList();
        List<Integer> cleanTypeList = new ArrayList();
        for (AwareProcessBlockInfo item : awareProcBlockList) {
            if (!(item == null || item.mCleanType == CleanType.NONE)) {
                pkgList.add(item.mPackageName);
                userIdList.add(Integer.valueOf(UserHandle.getUserId(item.mUid)));
                cleanTypeList.add(Integer.valueOf(item.mCleanType.ordinal()));
            }
        }
        AppCleanParam result = new Builder(this.mParam.getSource()).killedCount(killedCount).stringList(pkgList).intList(userIdList).intList2(cleanTypeList).build();
        if (this.mCallback != null) {
            try {
                this.mCallback.onCleanFinish(result);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "RemoteExcption e = " + e.toString());
            }
        }
        for (AwareProcessBlockInfo block : awareProcBlockList) {
            if (block != null) {
                AwareLog.i(TAG, "pkg = " + block.mPackageName + ", uid = " + block.mUid + ", policy = " + block.mCleanType + ", reason = " + block.mReason);
                if (block.mDetailedReason == null) {
                    block.mDetailedReason = new HashMap();
                }
                block.mDetailedReason.put("policy", Integer.valueOf(block.mCleanType.ordinal()));
                uploadToBigData(AppCleanSource.SYSTEM_MANAGER, block);
            }
        }
    }

    private List<AwareProcessBlockInfo> buildAwareProcBlockInfoList() {
        List<AppCleanInfo> appCleanInfoList = this.mParam.getAppCleanInfoList();
        List<AwareProcessBlockInfo> info = new ArrayList();
        if (appCleanInfoList == null || appCleanInfoList.isEmpty()) {
            AwareLog.e(TAG, "got empty AppCleanInfoList for buildAwareProcBlockInfoList!");
            return info;
        }
        ArrayList<AwareProcessInfo> allProcList = new ArrayList();
        boolean needUpdateTaskId = false;
        StringBuilder sb = new StringBuilder("appCleanInfos: ");
        for (AppCleanInfo appCleanInfo : appCleanInfoList) {
            if (appCleanInfo != null) {
                ArrayList<AwareProcessInfo> proclist = AwareProcessInfo.getAwareProcInfosFromPackage(appCleanInfo.getPkgName(), appCleanInfo.getUserId().intValue());
                if (proclist.isEmpty()) {
                    proclist.add(CleanSource.getDeadAwareProcInfo(appCleanInfo.getPkgName(), appCleanInfo.getUserId().intValue()));
                }
                if (((AwareProcessInfo) proclist.get(0)).mProcInfo != null) {
                    allProcList.addAll(proclist);
                    AwareProcessBlockInfo item = new AwareProcessBlockInfo(((AwareProcessInfo) proclist.get(0)).mProcInfo.mUid);
                    int cleanType = appCleanInfo.getCleanType().intValue();
                    if (cleanType == 0) {
                        item.mCleanType = CleanType.KILL_ALLOW_START;
                    } else if (1 == cleanType) {
                        item.mCleanType = CleanType.REMOVETASK;
                        if (appCleanInfo.getTaskId().intValue() == -1) {
                            needUpdateTaskId = true;
                        }
                    } else if (2 == cleanType) {
                        item.mCleanType = CleanType.FORCESTOP;
                    } else {
                        item.mCleanType = CleanType.NONE;
                        AwareLog.e(TAG, "invalid clean type, don't clean. cleanType: " + cleanType);
                    }
                    item.mPackageName = appCleanInfo.getPkgName();
                    int size = proclist.size();
                    for (int i = 0; i < size; i++) {
                        AwareProcessInfo awareProcInfo = (AwareProcessInfo) proclist.get(i);
                        if (1 == cleanType) {
                            awareProcInfo.mTaskId = appCleanInfo.getTaskId().intValue();
                        }
                        item.add(awareProcInfo);
                    }
                    info.add(item);
                    sb.append(appCleanInfo.getPkgName()).append(",").append(appCleanInfo.getUserId()).append(",").append(cleanType).append(",").append(appCleanInfo.getTaskId()).append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                }
            }
        }
        AwareLog.i(TAG, sb.toString());
        if (needUpdateTaskId) {
            updateTaskId(allProcList);
        }
        return info;
    }

    private List<AwareProcessBlockInfo> decideEnhanceClean(List<AwareProcessBlockInfo> blockInfoList) {
        ArrayList<AwareProcessInfo> processList = new ArrayList();
        ArrayList<AwareProcessBlockInfo> resultList = new ArrayList();
        for (AwareProcessBlockInfo blockInfo : blockInfoList) {
            if (blockInfo.mCleanType == CleanType.REMOVETASK && shouldEnhance(blockInfo.mUid, blockInfo.mPackageName)) {
                processList.addAll(blockInfo.mProcessList);
            } else {
                resultList.add(blockInfo);
            }
        }
        List<AwareProcessBlockInfo> decisionBlockList = CleanSource.mergeBlock(DecisionMaker.getInstance().decideAll(processList, 0, AppMngFeature.APP_CLEAN, AppCleanSource.SYSTEM_MANAGER));
        if (decisionBlockList != null) {
            for (AwareProcessBlockInfo blockInfo2 : decisionBlockList) {
                if (!(blockInfo2 == null || blockInfo2.mCleanType == CleanType.FORCESTOP_REMOVETASK)) {
                    blockInfo2.mCleanType = CleanType.REMOVETASK;
                }
            }
            resultList.addAll(decisionBlockList);
        }
        return resultList;
    }

    private boolean shouldEnhance(int uid, String pkgName) {
        if (AppMngConfig.getAbroadFlag() && AppTypeRecoManager.getInstance().getAppWhereFrom(pkgName) != 0) {
            AwareLog.i(TAG, "shouldNotEnhance, abroad & not china top 3000 app, do not enhance: " + pkgName);
            return false;
        } else if (inAppStartBaseLine(uid, pkgName)) {
            return false;
        } else {
            if (AwareAppStartupPolicy.self() != null) {
                HwAppStartupSetting setting = AwareAppStartupPolicy.self().getAppStartupSetting(pkgName);
                if (setting != null && setting.getModifier(0) == 2 && setting.getPolicy(3) == 1) {
                    AwareLog.i(TAG, "shouldNotEnhance cust app and allow background: " + pkgName);
                    return false;
                }
            }
            return !this.mHwAMS.isPackageRunningOnPCMode(pkgName, uid);
        }
    }

    private boolean inAppStartBaseLine(int uid, String pkgName) {
        if (isSystemUnRemoveApp(uid)) {
            return true;
        }
        boolean allowSR = DecisionMaker.getInstance().getAppStartPolicy(pkgName, AppStartSource.SCHEDULE_RESTART) != 0;
        boolean allowAlarm = DecisionMaker.getInstance().getAppStartPolicy(pkgName, AppStartSource.ALARM) != 0;
        if (!allowSR && !allowAlarm) {
            return false;
        }
        AwareLog.i(TAG, "inAppStartBaseLine: " + pkgName + ", allowSR: " + allowSR + ", allowAlarm: " + allowAlarm);
        return true;
    }

    private boolean isSystemUnRemoveApp(int uid) {
        uid = UserHandle.getAppId(uid);
        if (uid <= 0 || uid >= 10000) {
            return this.mSystemUnremoveUidCache != null && this.mSystemUnremoveUidCache.checkUidExist(uid);
        } else {
            return true;
        }
    }

    private void getCleanList() {
        List<AwareProcessInfo> allAwareProcNeedProcess = AppStatusUtils.getInstance().getAllProcNeedSort();
        if (allAwareProcNeedProcess == null || allAwareProcNeedProcess.isEmpty()) {
            AwareLog.e(TAG, "getAllProcNeedSort failed!");
            return;
        }
        List<AwareProcessBlockInfo> info = CleanSource.mergeBlock(DecisionMaker.getInstance().decideAll(allAwareProcNeedProcess, 1, AppMngFeature.APP_CLEAN, AppCleanSource.SYSTEM_MANAGER));
        if (info == null || info.isEmpty()) {
            AwareLog.e(TAG, "decideAll failed!");
            return;
        }
        List<AwareProcessBlockInfo> filteredAwareProcBlockInfoList = new ArrayList();
        for (AwareProcessBlockInfo infoItem : info) {
            if (infoItem != null) {
                if (infoItem.mCleanType == CleanType.NONE) {
                    filteredAwareProcBlockInfoList.add(infoItem);
                } else if (inAppStartBaseLine(infoItem.mUid, infoItem.mPackageName)) {
                    filteredAwareProcBlockInfoList.add(infoItem);
                }
            }
        }
        info.removeAll(filteredAwareProcBlockInfoList);
        Map<Integer, List<AwareProcessBlockInfo>> srcProcList = new HashMap();
        srcProcList.put(Integer.valueOf(2), info);
        callbackBeforeClean(new AwareAppMngSortPolicy(this.mContext, srcProcList));
    }

    private void callbackBeforeClean(AwareAppMngSortPolicy policy) {
        List<AwareProcessBlockInfo> awareProcBlockList = policy.getAllowStopProcBlockList();
        if (awareProcBlockList == null) {
            AwareLog.e(TAG, "AwareAppMngSortPolicy is null!");
            return;
        }
        List<String> pkgList = new ArrayList();
        List<Integer> userIdList = new ArrayList();
        List<Integer> cleanTypeList = new ArrayList();
        for (AwareProcessBlockInfo item : awareProcBlockList) {
            if (item != null) {
                pkgList.add(item.mPackageName);
                userIdList.add(Integer.valueOf(UserHandle.getUserId(item.mUid)));
                cleanTypeList.add(Integer.valueOf(item.mCleanType.ordinal()));
            }
        }
        AppCleanParam result = new Builder(this.mParam.getSource()).stringList(pkgList).intList(userIdList).intList2(cleanTypeList).build();
        if (this.mCallback != null) {
            try {
                this.mCallback.onCleanFinish(result);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "RemoteExcption e = " + e.toString());
            }
        }
        for (AwareProcessBlockInfo block : awareProcBlockList) {
            if (block != null) {
                AwareLog.i(TAG, "pkg = " + block.mPackageName + ", uid = " + block.mUid + ", policy = " + block.mCleanType + ", reason = " + block.mReason);
                updateHistory(AppCleanSource.SYSTEM_MANAGER, block);
                uploadToBigData(AppCleanSource.SYSTEM_MANAGER, block);
            }
        }
    }
}
