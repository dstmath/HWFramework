package com.android.server.wm;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Looper;
import com.android.server.wm.ActivityStackSupervisor;
import com.huawei.android.app.IApplicationThreadEx;
import com.huawei.displayengine.IDisplayEngineService;
import java.util.ArrayList;
import java.util.Iterator;

public class ActivityStackSupervisorBridgeEx {
    private ActivityStackSupervisorBridge mActivityStackSupervisor;
    private RootActivityContainerEx mRootEx;

    public ActivityStackSupervisorBridgeEx(ActivityTaskManagerServiceEx service, Looper looper) {
        this.mActivityStackSupervisor = new ActivityStackSupervisorBridge(service.getActivityTaskManagerService(), looper);
        this.mActivityStackSupervisor.setActivityStackSupervisorEx(this);
        if (this.mActivityStackSupervisor.mRootActivityContainer != null) {
            this.mRootEx = new RootActivityContainerEx();
            this.mRootEx.setRootActivityContainer(this.mActivityStackSupervisor.mRootActivityContainer);
        }
    }

    public ActivityStackSupervisorBridge getActivityStackSupervisor() {
        return this.mActivityStackSupervisor;
    }

    public int getReportWindowStateChangedMsg() {
        return IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT;
    }

    /* access modifiers changed from: protected */
    public boolean startProcessOnExtDisplay(ActivityRecordEx activityRecordEx) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean startProcessOnExtDisplayEx(ActivityRecordEx activityRecordEx) {
        return this.mActivityStackSupervisor.startProcessOnExtDisplayEx(activityRecordEx);
    }

    public boolean isInVisibleStack(String pkg) {
        return false;
    }

    public ActivityStackSupervisor.ActivityStackSupervisorHandler getHandler() {
        return this.mActivityStackSupervisor.mHandler;
    }

    public boolean hasMessages(int what) {
        return getHandler().hasMessages(what);
    }

    public final boolean sendEmptyMessage(int what) {
        return getHandler().sendEmptyMessage(what);
    }

    /* access modifiers changed from: protected */
    public boolean keepStackResumed(ActivityStackEx stack) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean keepStackResumedEx(ActivityStackEx stack) {
        return this.mActivityStackSupervisor.keepStackResumedEx(stack);
    }

    public ArrayList<ActivityStackEx> getActivityStackEx(int displayNdx) {
        ActivityDisplay activityDisplay;
        ActivityStackSupervisorBridge activityStackSupervisorBridge = this.mActivityStackSupervisor;
        if (activityStackSupervisorBridge == null || activityStackSupervisorBridge.mRootActivityContainer == null || (activityDisplay = this.mActivityStackSupervisor.mRootActivityContainer.getChildAt(displayNdx)) == null) {
            return null;
        }
        ArrayList<ActivityStack> activityStacks = activityDisplay.mStacks;
        ArrayList<ActivityStackEx> activityStackExs = new ArrayList<>();
        if (activityStacks != null) {
            Iterator<ActivityStack> it = activityStacks.iterator();
            while (it.hasNext()) {
                ActivityStackEx activityStackEx = new ActivityStackEx();
                activityStackEx.setActivityStack(it.next());
                activityStackExs.add(activityStackEx);
            }
        }
        return activityStackExs;
    }

    /* access modifiers changed from: protected */
    public boolean isStackInVisible(ActivityStackEx stack) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isStackInVisibleEx(ActivityStackEx stack) {
        return this.mActivityStackSupervisor.isStackInVisibleEx(stack);
    }

    public void handlePCWindowStateChanged() {
    }

    public ResolveInfo resolveIntentEx(Intent intent, String resolvedType, int userId, int flags, int filterCallingUid) {
        return this.mActivityStackSupervisor.resolveIntentEx(intent, resolvedType, userId, flags, filterCallingUid);
    }

    /* access modifiers changed from: package-private */
    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int userId, int flags, int filterCallingUid) {
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean restoreRecentTaskLockedEx(TaskRecordEx task, ActivityOptions activityOptions, boolean isOnTop) {
        if (task == null || task.isEmpty()) {
            return false;
        }
        return this.mActivityStackSupervisor.restoreRecentTaskLockedEx(task, activityOptions, isOnTop);
    }

    /* access modifiers changed from: protected */
    public boolean restoreRecentTaskLocked(TaskRecordEx task, ActivityOptions activityOptions, boolean isOnTop) {
        return false;
    }

    public boolean shouldNotKillProcWhenRemoveTask(String pkg) {
        return false;
    }

    public void recognitionMaliciousApp(IApplicationThreadEx caller, Intent intent, int userId) {
    }

    public void scheduleReportPCWindowStateChangedLocked(TaskRecordEx task) {
    }

    /* access modifiers changed from: package-private */
    public boolean hasActivityInStackLocked(ActivityInfo activityInfo) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void uploadUnSupportSplitScreenAppPackageName(String pkgName) {
    }

    /* access modifiers changed from: protected */
    public ActivityStackEx getTargetSplitTopStack(ActivityStackEx current) {
        return null;
    }

    /* access modifiers changed from: protected */
    public ActivityStackEx getNextStackInSplitSecondary(ActivityStackEx current) {
        return null;
    }

    public RootActivityContainerEx getRootActivityContainerEx() {
        ActivityStackSupervisorBridge activityStackSupervisorBridge;
        if (this.mRootEx != null || (activityStackSupervisorBridge = this.mActivityStackSupervisor) == null || activityStackSupervisorBridge.mRootActivityContainer == null) {
            return this.mRootEx;
        }
        this.mRootEx = new RootActivityContainerEx();
        this.mRootEx.setRootActivityContainer(this.mActivityStackSupervisor.mRootActivityContainer);
        return this.mRootEx;
    }
}
