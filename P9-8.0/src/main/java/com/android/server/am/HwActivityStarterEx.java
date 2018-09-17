package com.android.server.am;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.iawareperf.UniPerf;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.huawei.pgmng.log.LogPower;
import com.huawei.server.am.IHwActivityStarterEx;
import java.util.ArrayList;

public class HwActivityStarterEx implements IHwActivityStarterEx {
    final ActivityManagerService mService;

    public HwActivityStarterEx(ActivityManagerService service) {
        this.mService = service;
    }

    public void effectiveIawareToLaunchApp(Intent targetIntent, ActivityInfo targetAInfo, String curActivityPkName) {
        if (targetIntent != null && targetAInfo != null) {
            String strPkg = "";
            if (targetIntent.getComponent() != null) {
                strPkg = targetIntent.getComponent().getPackageName();
            }
            if (!isAppHotStart(targetIntent, targetAInfo, this.mService.getRecentTasks())) {
                this.mService.notifyAppEventToIaware(3, strPkg);
            }
            if (curActivityPkName == null || !(curActivityPkName.equals(strPkg) || ("com.android.systemui".equals(strPkg) ^ 1) == 0)) {
                UniPerf.getInstance().uniPerfEvent(4099, "", new int[0]);
                this.mService.notifyAppEventToIaware(1, strPkg);
                LogPower.push(CPUFeature.MSG_SET_FG_CGROUP, strPkg);
                return;
            }
            UniPerf.getInstance().uniPerfEvent(4098, "", new int[0]);
            this.mService.notifyAppEventToIaware(2, strPkg);
        }
    }

    private boolean isAppHotStart(Intent targetIntent, ActivityInfo targetAInfo, ArrayList<TaskRecord> recentTasks) {
        if (!(targetIntent == null || targetAInfo == null || recentTasks == null)) {
            boolean z;
            if (!"android.intent.action.MAIN".equals(targetIntent.getAction()) || targetIntent.getCategories() == null) {
                z = false;
            } else {
                z = targetIntent.getCategories().contains("android.intent.category.LAUNCHER");
            }
            if (!z) {
                return true;
            }
            ComponentName cls = targetIntent.getComponent();
            int taskSize = recentTasks.size();
            int i = 0;
            while (i < taskSize) {
                TaskRecord task = (TaskRecord) recentTasks.get(i);
                Intent taskIntent = task.intent;
                Intent affinityIntent = task.affinityIntent;
                if ((task.rootAffinity != null && task.rootAffinity.equals(targetAInfo.taskAffinity)) || ((taskIntent != null && taskIntent.getComponent() != null && taskIntent.getComponent().compareTo(cls) == 0) || (affinityIntent != null && affinityIntent.getComponent() != null && affinityIntent.getComponent().compareTo(cls) == 0))) {
                    return task.mActivities.size() > 0;
                } else {
                    i++;
                }
            }
        }
        return false;
    }
}
