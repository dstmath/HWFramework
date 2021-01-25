package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.ActivityThread;
import android.app.IApplicationThread;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.ResolveInfo;
import android.freeform.HwFreeFormUtils;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Slog;
import com.huawei.hiai.awareness.AwarenessConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class HwActivityStackSupervisor extends ActivityStackSupervisorBridgeEx {
    private static final int CRASH_INTERVAL_THRESHOLD = 60000;
    private static final int CRASH_TIMES_THRESHOLD = 3;
    private static final String SPLIT_SCREEN_APP_NAME = "splitscreen.SplitScreenAppActivity";
    private static final String STACK_DIVIDER_APP_NAME = "stackdivider.ForcedResizableInfoActivity";
    private static final String TAG = "HwAss";
    private ActivityTaskManagerServiceEx mAtmsEx;
    private int mCrashTimes;
    private long mFirstLaunchTime;
    private String mLastHomePkg;
    private final ArrayList<ActivityRecordEx> mWindowStateChangedActivities = new ArrayList<>();

    public HwActivityStackSupervisor(ActivityTaskManagerServiceEx service, Looper looper) {
        super(service, looper);
        this.mAtmsEx = service;
    }

    private boolean isUninstallableApk(String pkgName, Intent homeIntent, int userId) {
        ResolveInfo homeInfo;
        ComponentInfo ci;
        if (pkgName == null || "android".equals(pkgName) || (homeInfo = resolveIntent(homeIntent, homeIntent.resolveTypeIfNeeded(this.mAtmsEx.getContext().getContentResolver()), userId, 786432, Binder.getCallingUid())) == null || (ci = homeInfo.getComponentInfo()) == null) {
            return false;
        }
        if (((ci.applicationInfo.flags & 1) != 0) || !pkgName.equals(ci.packageName)) {
            return false;
        }
        return true;
    }

    public void recognitionMaliciousApp(IApplicationThread caller, Intent intent, int userId) {
        Intent homeIntent;
        if (caller == null && intent != null && (homeIntent = this.mAtmsEx.getHomeIntent()) != null && homeIntent.getCategories() != null) {
            String action = intent.getAction();
            Set<String> categories = intent.getCategories();
            boolean isCategoryContainsHome = true;
            boolean isActionEqualsHome = action != null && action.equals(homeIntent.getAction());
            if (categories == null || !categories.containsAll(homeIntent.getCategories())) {
                isCategoryContainsHome = false;
            }
            if (isActionEqualsHome && isCategoryContainsHome) {
                ComponentName cmp = intent.getComponent();
                String strPkg = cmp != null ? cmp.getPackageName() : null;
                if (strPkg != null && !HwDeviceManager.disallowOp(26, strPkg) && isUninstallableApk(strPkg, homeIntent, userId) && (intent.getHwFlags() & AwarenessConstants.TRAVEL_HELPER_DATA_CHANGE_ACTION) == 0) {
                    updateCrashInfo(strPkg);
                }
            }
        }
    }

    private void updateCrashInfo(String strPkg) {
        if (strPkg.equals(this.mLastHomePkg)) {
            this.mCrashTimes++;
            long now = SystemClock.uptimeMillis();
            if (this.mCrashTimes < 3) {
                return;
            }
            if (now - this.mFirstLaunchTime < 60000) {
                try {
                    ActivityThread.getPackageManager().clearPackagePreferredActivities(strPkg);
                } catch (RemoteException e) {
                    Slog.e(TAG, " Update crash info fail.");
                }
                this.mAtmsEx.showUninstallLauncherDialog(strPkg);
                this.mLastHomePkg = null;
                this.mCrashTimes = 0;
                return;
            }
            this.mCrashTimes = 1;
            this.mFirstLaunchTime = now;
            return;
        }
        this.mLastHomePkg = strPkg;
        this.mCrashTimes = 0;
        this.mFirstLaunchTime = SystemClock.uptimeMillis();
    }

    /* access modifiers changed from: package-private */
    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int userId, int flags, int filterCallingUid) {
        int callingUserId = UserHandle.getUserId(Binder.getCallingUid());
        if (userId == callingUserId && userId == 0) {
            return resolveIntentEx(intent, resolvedType, userId, flags, filterCallingUid);
        }
        if (!this.mAtmsEx.isSameGroupForClone(callingUserId, userId)) {
            return resolveIntentEx(intent, resolvedType, userId, flags, filterCallingUid);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            return resolveIntentEx(intent, resolvedType, userId, flags, filterCallingUid);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private ArrayList<ActivityRecordEx> getWindowStateChangedActivities() {
        return this.mWindowStateChangedActivities;
    }

    private boolean isActivityInStack(TaskRecordEx task, String pkgName, String processName) {
        ArrayList<ActivityRecordEx> activities;
        int numActivities;
        if (task == null || pkgName == null || processName == null || (numActivities = (activities = task.getActivityRecordExs()).size()) <= 0) {
            return false;
        }
        for (int activityNdx = numActivities - 1; activityNdx >= 0; activityNdx--) {
            ActivityRecordEx record = activities.get(activityNdx);
            if (!(record == null || record.isEmpty() || record.isFinishing() || record.isDestroyState() || record.isAppNull() || !pkgName.equals(record.getPackageName()) || !processName.equals(record.getAppName()))) {
                return true;
            }
        }
        return false;
    }

    public void scheduleReportPCWindowStateChangedLocked(TaskRecordEx task) {
        if (!(task == null || task.isEmpty())) {
            for (int i = task.getActivityRecordExs().size() - 1; i >= 0; i--) {
                ActivityRecordEx record = (ActivityRecordEx) task.getActivityRecordExs().get(i);
                if (record.isWindowProcessControllerNotNull()) {
                    getWindowStateChangedActivities().add(record);
                }
            }
            if (!hasMessages(getReportWindowStateChangedMsg())) {
                sendEmptyMessage(getReportWindowStateChangedMsg());
            }
        }
    }

    public void handlePCWindowStateChanged() {
        synchronized (this.mAtmsEx.getGlobalLock()) {
            List<ActivityRecordEx> activityRecordExes = getWindowStateChangedActivities();
            for (int i = activityRecordExes.size() - 1; i >= 0; i--) {
                activityRecordExes.remove(i).schedulePCWindowStateChangedEx();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean restoreRecentTaskLocked(TaskRecordEx task, ActivityOptions activityOptions, boolean isTop) {
        if ((HwPCUtils.isPcCastModeInServer() || this.mAtmsEx.isVrMode()) && (task == null || task.isEmpty())) {
            return false;
        }
        return restoreRecentTaskLockedEx(task, activityOptions, isTop);
    }

    /* access modifiers changed from: protected */
    public boolean keepStackResumed(ActivityStackEx stack) {
        if (HwPCUtils.isPcCastModeInServer() && stack != null && !stack.isActivityStackNull()) {
            if (HwPCUtils.isPcDynamicStack(stack.getStackId()) && stack.shouldBeVisible((ActivityRecordEx) null)) {
                return true;
            }
            if (getRootActivityContainerEx() != null && getRootActivityContainerEx().getTopDisplayFocusedStack() != null && HwPCUtils.isPcDynamicStack(getRootActivityContainerEx().getTopDisplayFocusedStack().getStackId()) && !HwPCUtils.isPcDynamicStack(stack.getStackId())) {
                return true;
            }
        }
        return keepStackResumedEx(stack);
    }

    /* access modifiers changed from: protected */
    public boolean isStackInVisible(ActivityStackEx stack) {
        if (stack != null && !stack.isActivityStackNull() && HwPCUtils.isExtDynamicStack(stack.getStackId()) && stack.shouldBeVisible((ActivityRecordEx) null)) {
            return true;
        }
        if (!HwFreeFormUtils.isFreeFormEnable() || stack == null || stack.isActivityStackNull() || !stack.inFreeformWindowingMode() || stack.shouldBeVisible((ActivityRecordEx) null)) {
            return isStackInVisibleEx(stack);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean startProcessOnExtDisplay(ActivityRecordEx record) {
        return startProcessOnExtDisplayEx(record);
    }

    public boolean shouldNotKillProcWhenRemoveTask(String pkg) {
        if (!"com.tencent.mm".equals(pkg) || SystemProperties.getBoolean("hw.app.smart_cleaning", false)) {
            return false;
        }
        Slog.d(TAG, "cleanUpRemovedTaskLocked, do not kill process : " + pkg);
        return true;
    }

    public boolean isInVisibleStack(String pkg) {
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean hasActivityInStackLocked(ActivityInfo activityInfo) {
        if (activityInfo == null) {
            return false;
        }
        String affinity = activityInfo.taskAffinity;
        String pkgName = activityInfo.packageName;
        String processName = activityInfo.processName;
        if (affinity == null || pkgName == null || processName == null || activityInfo.applicationInfo == null) {
            return false;
        }
        int userId = UserHandle.getUserId(activityInfo.applicationInfo.uid);
        for (int displayNdx = getRootActivityContainerEx().getChildCount() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStackEx> stacks = getActivityStackEx(displayNdx);
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ArrayList<TaskRecordEx> allTasks = stacks.get(stackNdx).getAllTaskRecordExs();
                for (int taskNdx = allTasks.size() - 1; taskNdx >= 0; taskNdx--) {
                    TaskRecordEx task = allTasks.get(taskNdx);
                    if (!(task == null || task.isEmpty() || task.getUserId() != userId || task.getAffinity() == null || !task.getAffinity().equals(affinity))) {
                        return isActivityInStack(task, pkgName, processName);
                    }
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void uploadUnSupportSplitScreenAppPackageName(String pkgName) {
        Flog.bdReport(991310174, "pkgName", pkgName);
    }

    /* access modifiers changed from: protected */
    public ActivityStackEx getTargetSplitTopStack(ActivityStackEx current) {
        ActivityStackEx topPrimaryStack;
        if (current.getWindowingMode() == 3) {
            ActivityStackEx topSecondaryStack = current.getDisplay().getTopStackInWindowingMode(4);
            ActivityRecordEx topSecondaryActivityRecord = null;
            if (topSecondaryStack != null) {
                topSecondaryActivityRecord = topSecondaryStack.topRunningActivityLocked();
            }
            if (topSecondaryActivityRecord != null && !topSecondaryActivityRecord.toString().contains(SPLIT_SCREEN_APP_NAME)) {
                return topSecondaryStack;
            }
            ActivityStackEx homeStack = current.getDisplay().getStackEx(4, 2);
            if (homeStack != null) {
                return homeStack;
            }
            return null;
        } else if (current.getWindowingMode() != 4 || current.getActivityType() != 1) {
            return null;
        } else {
            ActivityStackEx nextTargetStack = getNextStackInSplitSecondary(current);
            ActivityRecordEx nextTargetRecord = null;
            if (nextTargetStack != null) {
                nextTargetRecord = nextTargetStack.topRunningActivityLocked();
            }
            if (nextTargetRecord == null || nextTargetRecord.getInfo() == null || nextTargetRecord.getInfo().name == null || !nextTargetRecord.getInfo().name.contains(SPLIT_SCREEN_APP_NAME) || (topPrimaryStack = current.getDisplay().getTopStackInWindowingMode(3)) == null) {
                return null;
            }
            return topPrimaryStack;
        }
    }

    /* access modifiers changed from: protected */
    public ActivityStackEx getNextStackInSplitSecondary(ActivityStackEx current) {
        ArrayList<ActivityStackEx> stacks = current.getDisplay().getStackExs();
        boolean isNext = false;
        for (int i = stacks.size() - 1; i >= 0; i--) {
            ActivityStackEx targetStack = stacks.get(i);
            if (isNext) {
                ActivityRecordEx targetRecord = targetStack.topRunningActivityLocked();
                if ((targetRecord == null || targetRecord.getInfo() == null || targetRecord.getInfo().name == null || !targetRecord.getInfo().name.contains(STACK_DIVIDER_APP_NAME)) && targetStack.getWindowingMode() == 4) {
                    return targetStack;
                }
            } else if (targetStack.getStackId() == current.getStackId()) {
                isNext = true;
            }
        }
        return null;
    }
}
