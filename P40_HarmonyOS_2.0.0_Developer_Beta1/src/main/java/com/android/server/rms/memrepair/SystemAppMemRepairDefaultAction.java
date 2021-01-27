package com.android.server.rms.memrepair;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.os.Bundle;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appclean.CleanSource;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.rms.iaware.memory.policy.SystemTrimPolicy;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.huawei.android.app.ActivityManagerEx;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SystemAppMemRepairDefaultAction {
    private static final String TAG = "SysMemRepair";
    private String mAction;
    private final Map<String, Long> mConfigThreshold = new ArrayMap();

    public SystemAppMemRepairDefaultAction(String action) {
        this.mAction = action;
    }

    public void updateThreshold(String name, long threshold) {
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, this.mAction + ":updateThreshold name=" + name + ", threshold=" + threshold);
        }
        synchronized (this.mConfigThreshold) {
            this.mConfigThreshold.put(name, Long.valueOf(threshold));
        }
    }

    public int excute(Context context, Bundle extras, AtomicBoolean interrupted) {
        AwareLog.i(TAG, "excute entry now!");
        List<AwareProcessInfo> procsGroups = MemoryUtils.getAppMngSortPolicyForSystemTrim();
        if (procsGroups == null) {
            AwareLog.w(TAG, this.mAction + ":getAppMngSortPolicyForSystemTrim is null!");
            return -1;
        }
        List<AwareProcessInfo> overThresholdProcs = SystemTrimPolicy.getInstance().getProcOverThreshold(procsGroups, this.mConfigThreshold);
        if (overThresholdProcs == null || overThresholdProcs.isEmpty()) {
            AwareLog.i(TAG, this.mAction + ":no app proc over threshold");
            return -1;
        }
        List<AwareProcessBlockInfo> procInfos = CleanSource.mergeBlockForMemory(DecisionMaker.getInstance().decideAll(overThresholdProcs, 0, AppMngConstant.AppMngFeature.APP_CLEAN, (AppMngConstant.EnumWithDesc) AppMngConstant.AppCleanSource.SYSTEM_MEMORY_REPAIR), DecisionMaker.getInstance().getProcessList(AppMngConstant.AppMngFeature.APP_CLEAN, AppMngConstant.AppCleanSource.SYSTEM_MEMORY_REPAIR));
        if (procInfos == null || procInfos.isEmpty()) {
            AwareLog.i(TAG, this.mAction + ":no app proc need to mem repair");
            return -1;
        }
        doMemoryRepair(checkFloatWindowScene(procInfos, context), extras, context, interrupted);
        return 0;
    }

    public void doMemoryRepair(List<AwareProcessBlockInfo> procInfos, Bundle extras, Context context, AtomicBoolean interrupted) {
        if (procInfos == null || procInfos.isEmpty()) {
            AwareLog.i(TAG, this.mAction + ":no procs need to mem repair");
        } else if (extras == null || context == null || interrupted == null) {
            AwareLog.w(TAG, this.mAction + ":some params is null");
        } else {
            int appUid = extras.getInt("appUid");
            for (AwareProcessBlockInfo procGroup : procInfos) {
                if (interrupted.get()) {
                    AwareLog.w(TAG, this.mAction + ":execKill: mInterrupt, return");
                    return;
                } else if (procGroup == null) {
                    AwareLog.w(TAG, this.mAction + ":execKill: null procGroup");
                } else {
                    List<AwareProcessInfo> procs = procGroup.getProcessList();
                    if (procs == null || procs.isEmpty()) {
                        AwareLog.w(TAG, this.mAction + ":execKill: null process list. uid:" + procGroup.procUid);
                    } else {
                        AwareProcessInfo currentProcess = procs.get(0);
                        String processName = currentProcess.procProcInfo != null ? currentProcess.procProcInfo.mProcessName : null;
                        if (appUid == procGroup.procUid) {
                            AwareLog.i(TAG, this.mAction + ":execKill: foreground " + processName);
                        } else {
                            AwareLog.d(TAG, this.mAction + ":procs=" + procGroup);
                            if (procGroup.procCleanType == ProcessCleaner.CleanType.NONE) {
                                AwareLog.d(TAG, this.mAction + ":execKill: app not kill, reason=" + procGroup.procReason);
                            } else {
                                ProcessCleaner.getInstance(context).killProcessesSameUidExt(procGroup, interrupted, this.mAction, new boolean[]{false, false, true});
                                SysMemMngBigData.getInstance().fillSysMemBigData(procGroup, null, 1001);
                            }
                        }
                    }
                }
            }
        }
    }

    private List<AwareProcessBlockInfo> checkFloatWindowScene(List<AwareProcessBlockInfo> procInfos, Context context) {
        if (procInfos == null || procInfos.size() == 0) {
            AwareLog.d(TAG, "checkFloatWindowScene procInfos null");
            return new ArrayList(0);
        }
        List<String> pkgNames = new ArrayList<>();
        for (AwareProcessBlockInfo procInfo : procInfos) {
            pkgNames.add(procInfo.procPackageName);
        }
        Map<String, Boolean> forbidCleanApps = ActivityManagerEx.getAppUserAwarenessState(ActivityManagerEx.getDisplayId(context), pkgNames);
        if (forbidCleanApps == null || forbidCleanApps.size() == 0) {
            return procInfos;
        }
        List<AwareProcessBlockInfo> excludeProcInfos = new ArrayList<>();
        for (AwareProcessBlockInfo procInfo2 : procInfos) {
            if (forbidCleanApps.containsKey(procInfo2.procPackageName)) {
                excludeProcInfos.add(procInfo2);
                if (AwareLog.getDebugLogSwitch()) {
                    AwareLog.d(TAG, procInfo2.procPackageName + " is exclude from kill for floatwin mode");
                }
            }
        }
        procInfos.removeAll(excludeProcInfos);
        return procInfos;
    }
}
