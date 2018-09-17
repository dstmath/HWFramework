package com.android.server.am;

import android.app.ActivityOptions;
import android.app.ActivityThread;
import android.app.IApplicationThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.pc.IHwPCManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.CollectData;
import android.rms.iaware.DataContract.Apps;
import android.rms.iaware.DataContract.Apps.Builder;
import android.util.HwPCUtils;
import android.util.Slog;
import android.widget.Toast;
import com.android.server.UiThread;
import com.android.server.am.ActivityStack.ActivityState;
import com.android.server.am.ActivityStackSupervisor.ActivityDisplay;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.huawei.displayengine.IDisplayEngineService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class HwActivityStackSupervisor extends ActivityStackSupervisor {
    private static final String ACTION_CONFIRM_APPLOCK_CREDENTIAL = "huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER";
    private static final String ACTION_CONFIRM_APPLOCK_PACKAGENAME = "com.huawei.systemmanager";
    private static final int CRASH_INTERVAL_THRESHOLD = 60000;
    private static final int CRASH_TIMES_THRESHOLD = 3;
    private int mCrashTimes;
    private long mFirstLaunchTime;
    private String mLastHomePkg;
    private int mNextPcFreeStackId = IDisplayEngineService.DE_ACTION_PG_CAMERA_FRONT;
    private Toast mToast = null;
    private final ArrayList<ActivityRecord> mWindowStateChangedActivities = new ArrayList();

    public HwActivityStackSupervisor(ActivityManagerService service, Looper looper) {
        super(service, looper);
    }

    private boolean isUninstallableApk(String pkgName) {
        if (pkgName == null || "android".equals(pkgName)) {
            return false;
        }
        try {
            PackageInfo pInfo = this.mService.mContext.getPackageManager().getPackageInfo(pkgName, 0);
            if (pInfo == null || (pInfo.applicationInfo.flags & 1) == 0) {
                return true;
            }
            return false;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public void recognitionMaliciousApp(IApplicationThread caller, Intent intent) {
        if (caller == null) {
            Intent homeIntent = this.mService.getHomeIntent();
            String action = intent.getAction();
            Set<String> category = intent.getCategories();
            if (action != null && action.equals(homeIntent.getAction()) && category != null && category.containsAll(homeIntent.getCategories())) {
                ComponentName cmp = intent.getComponent();
                String strPkg = null;
                if (cmp != null) {
                    strPkg = cmp.getPackageName();
                }
                if (strPkg != null) {
                    if (HwDeviceManager.disallowOp(26, strPkg)) {
                        Slog.i(TAG, strPkg + " is a ignored frequent relaunch app set by MDM!");
                    } else if (isUninstallableApk(strPkg)) {
                        if (strPkg.equals(this.mLastHomePkg)) {
                            this.mCrashTimes++;
                            long now = SystemClock.uptimeMillis();
                            if (this.mCrashTimes >= 3) {
                                if (now - this.mFirstLaunchTime < AppHibernateCst.DELAY_ONE_MINS) {
                                    try {
                                        ActivityThread.getPackageManager().clearPackagePreferredActivities(strPkg);
                                    } catch (RemoteException e) {
                                    }
                                    this.mService.showUninstallLauncherDialog(strPkg);
                                    this.mLastHomePkg = null;
                                    this.mCrashTimes = 0;
                                } else {
                                    this.mCrashTimes = 1;
                                    this.mFirstLaunchTime = now;
                                }
                            }
                        } else {
                            this.mLastHomePkg = strPkg;
                            this.mCrashTimes = 0;
                            this.mFirstLaunchTime = SystemClock.uptimeMillis();
                        }
                    }
                }
            }
        }
    }

    ResolveInfo resolveIntent(Intent intent, String resolvedType, int userId, int flags) {
        int callingUserId = UserHandle.getUserId(Binder.getCallingUid());
        if (userId == callingUserId && userId == 0) {
            return super.resolveIntent(intent, resolvedType, userId, flags);
        }
        if (!this.mService.mUserController.mInjector.getUserManagerInternal().isSameGroupForClone(callingUserId, userId)) {
            return super.resolveIntent(intent, resolvedType, userId, flags);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            ResolveInfo resolveIntent = super.resolveIntent(intent, resolvedType, userId, flags);
            return resolveIntent;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private final void noteActivityDisplayedEnd(String activityName, int pid, long time) {
        if (AwareConstant.CURRENT_USER_TYPE == 3) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RES_APP)) && this.mService.mSystemReady) {
                Builder builder = Apps.builder();
                builder.addEvent(85013);
                builder.addActivityDisplayedInfo(activityName, pid, time);
                CollectData appsData = builder.build();
                long id = Binder.clearCallingIdentity();
                resManager.reportData(appsData);
                Binder.restoreCallingIdentity(id);
                Slog.d(TAG, "EVENT_APP_ACTIVITY_DISPLAYED_FINISH reportData: " + activityName + " " + pid + " " + time);
            }
        }
    }

    void reportActivityLaunchedLocked(boolean timeout, ActivityRecord r, long thisTime, long totalTime) {
        if (!(timeout || r == null || r.app == null || r.shortComponentName == null || r.app.pid <= 0)) {
            noteActivityDisplayedEnd(r.shortComponentName, r.app.pid, thisTime);
        }
        super.reportActivityLaunchedLocked(timeout, r, thisTime, totalTime);
    }

    public ArrayList<ActivityRecord> getWindowStateChangedActivities() {
        return this.mWindowStateChangedActivities;
    }

    public void scheduleReportPCWindowStateChangedLocked(TaskRecord task) {
        for (int i = task.mActivities.size() - 1; i >= 0; i--) {
            ActivityRecord r = (ActivityRecord) task.mActivities.get(i);
            if (!(r.app == null || r.app.thread == null)) {
                getWindowStateChangedActivities().add(r);
            }
        }
        if (!this.mHandler.hasMessages(IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT)) {
            this.mHandler.sendEmptyMessage(IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT);
        }
    }

    public boolean isFrontOfStackList(ActivityStack stack, List<ActivityStack> stackList) {
        if (stack == null) {
            return false;
        }
        if (HwPCUtils.isPcDynamicStack(stack.getStackId())) {
            return true;
        }
        return super.isFrontOfStackList(stack, stackList);
    }

    public void handlePCWindowStateChanged() {
        synchronized (this.mService) {
            List<ActivityRecord> list = getWindowStateChangedActivities();
            for (int i = list.size() - 1; i >= 0; i--) {
                ActivityRecord r = (ActivityRecord) list.remove(i);
                if (r instanceof HwActivityRecord) {
                    ((HwActivityRecord) r).schedulePCWindowStateChanged();
                }
            }
        }
    }

    protected int getNextPcStackId() {
        while (true) {
            if (this.mNextPcFreeStackId >= IDisplayEngineService.DE_ACTION_PG_CAMERA_FRONT && getStack(this.mNextPcFreeStackId) == null) {
                return this.mNextPcFreeStackId;
            }
            this.mNextPcFreeStackId++;
        }
    }

    protected ActivityStack getValidLaunchStackOnDisplay(int displayId, ActivityRecord r) {
        if (((ActivityDisplay) this.mActivityDisplays.get(displayId)) == null) {
            throw new IllegalArgumentException("Display with displayId=" + displayId + " not found.");
        }
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayId)) {
            int newDynamicStackId = getNextPcStackId();
            if (this.mService.mActivityStarter.isValidLaunchStackId(newDynamicStackId, displayId, r)) {
                return createStackOnDisplay(newDynamicStackId, displayId, true);
            }
        }
        return super.getValidLaunchStackOnDisplay(displayId, r);
    }

    protected boolean restoreRecentTaskLocked(TaskRecord task, int stackId) {
        if (HwPCUtils.isPcCastModeInServer() && task == null) {
            return false;
        }
        return super.restoreRecentTaskLocked(task, stackId);
    }

    protected void handleDisplayRemoved(int displayId) {
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayId)) {
            synchronized (this.mService) {
                ActivityDisplay activityDisplay = (ActivityDisplay) this.mActivityDisplays.get(displayId);
                if (activityDisplay != null) {
                    onDisplayRemoved(activityDisplay.mStacks);
                    this.mActivityDisplays.remove(displayId);
                    this.mWindowManager.onDisplayRemoved(displayId);
                }
            }
            return;
        }
        super.handleDisplayRemoved(displayId);
    }

    protected boolean keepStackResumed(ActivityStack stack) {
        if (HwPCUtils.isPcCastModeInServer() && stack != null) {
            if (HwPCUtils.isPcDynamicStack(stack.mStackId) && stack.shouldBeVisible(null) == 1) {
                return true;
            }
            if (HwPCUtils.isPcDynamicStack(this.mFocusedStack.mStackId) && (HwPCUtils.isPcDynamicStack(stack.mStackId) ^ 1) != 0) {
                return true;
            }
        }
        return super.keepStackResumed(stack);
    }

    protected boolean isStackInVisible(ActivityStack stack) {
        if (stack != null && HwPCUtils.isPcDynamicStack(stack.mStackId) && stack.shouldBeVisible(null) == 0) {
            return true;
        }
        return super.isStackInVisible(stack);
    }

    protected void showToast(final int displayId) {
        if (HwPCUtils.isPcCastModeInServer()) {
            Context context;
            if (HwPCUtils.isValidExtDisplayId(displayId)) {
                context = HwPCUtils.getDisplayContext(this.mService.mContext, displayId);
            } else {
                context = this.mService.mContext;
            }
            if (context != null) {
                UiThread.getHandler().post(new Runnable() {
                    public void run() {
                        if (HwActivityStackSupervisor.this.mToast != null) {
                            HwActivityStackSupervisor.this.mToast.cancel();
                        }
                        if (HwPCUtils.isValidExtDisplayId(displayId)) {
                            HwActivityStackSupervisor.this.mToast = Toast.makeText(context, context.getResources().getString(33685970), 0);
                        } else {
                            HwActivityStackSupervisor.this.mToast = Toast.makeText(context, context.getString(33685971), 0);
                        }
                        if (HwActivityStackSupervisor.this.mToast != null) {
                            HwActivityStackSupervisor.this.mToast.show();
                        }
                    }
                });
            }
        }
    }

    protected boolean startProcessOnExtDisplay(ActivityRecord r) {
        if (r.getStack() == null || !HwPCUtils.isPcDynamicStack(r.getStack().mStackId) || r.getTask() == null || r.getTask().getLaunchBounds() == null) {
            return super.startProcessOnExtDisplay(r);
        }
        String[] strArr = null;
        if (r.getTask().getLaunchBounds() != null) {
            strArr = new String[]{String.valueOf(r.getDisplayId()), String.valueOf(r.getTask().getLaunchBounds().width()), String.valueOf(r.getTask().getLaunchBounds().height())};
        }
        this.mService.startProcessLocked(r.processName, r.info.applicationInfo, true, 0, "activity", r.intent.getComponent(), false, false, 0, true, null, null, strArr, null);
        return true;
    }

    public boolean shouldNotKillProcWhenRemoveTask(String pkg) {
        if (!"com.tencent.mm".equals(pkg)) {
            return false;
        }
        Slog.d(TAG, " cleanUpRemovedTaskLocked, do not kill process : " + pkg);
        return true;
    }

    public void onDisplayRemoved(ArrayList<ActivityStack> stacks) {
        ArrayList<Intent> mIntentList = new ArrayList();
        for (int i = stacks.size() - 1; i >= 0; i--) {
            ((ActivityStack) stacks.get(i)).mForceHidden = true;
        }
        while (!stacks.isEmpty()) {
            ActivityStack stack = (ActivityStack) stacks.get(0);
            moveStackToDisplayLocked(stack.mStackId, 0, false);
            TaskRecord tr = stack.topTask();
            if (tr != null) {
                if (tr.intent != null) {
                    mIntentList.add(tr.intent);
                }
                stack.moveTaskToBackLocked(stack.getStackId());
                stack.finishAllActivitiesLocked(true);
            } else {
                stack.finishAllActivitiesLocked(true);
            }
        }
        if (HwPCUtils.enabledInPad() && (this.mService.mActivityStarter instanceof HwActivityStarter)) {
            ((HwActivityStarter) this.mService.mActivityStarter).killAllPCProcessesLocked();
        }
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.saveAppIntent(mIntentList);
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "fail to saveAppIntent on display removed");
            }
        }
        if (this.mService instanceof HwActivityManagerService) {
            ((HwActivityManagerService) this.mService).mPkgDisplayMaps.clear();
        }
    }

    protected boolean resumeAppLockActivityIfNeeded(ActivityStack stack, ActivityOptions targetOptions) {
        if (this.mCurrentUser != 0) {
            return false;
        }
        ActivityRecord r = stack.topRunningActivityLocked();
        if (r == null || r.state == ActivityState.RESUMED || !isKeyguardDismiss() || !isAppInLockList(r.packageName)) {
            return false;
        }
        Intent newIntent = new Intent(ACTION_CONFIRM_APPLOCK_CREDENTIAL);
        newIntent.setFlags(109051904);
        newIntent.setPackage(ACTION_CONFIRM_APPLOCK_PACKAGENAME);
        newIntent.putExtra("android.intent.extra.TASK_ID", r.task.taskId);
        newIntent.putExtra("android.intent.extra.PACKAGE_NAME", r.packageName);
        if (HwPCUtils.isPcDynamicStack(stack.getStackId())) {
            ActivityOptions options = ActivityOptions.makeBasic();
            options.setLaunchTaskId(r.task.taskId);
            this.mService.mContext.startActivity(newIntent, options.toBundle());
        } else {
            this.mService.mContext.startActivity(newIntent);
        }
        return true;
    }

    protected boolean isAppInLockList(String pgkName) {
        return pgkName != null && Secure.getInt(this.mService.mContext.getContentResolver(), "app_lock_func_status", 0) == 1 && (Secure.getString(this.mService.mContext.getContentResolver(), "app_lock_list") + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER).contains(pgkName + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
    }

    private boolean isKeyguardDismiss() {
        return this.mKeyguardController.isKeyguardLocked() ^ 1;
    }

    protected boolean notKillProcessWhenRemoveTask() {
        boolean z = true;
        if (this.mService.mContext == null) {
            return true;
        }
        if (System.getInt(this.mService.mContext.getContentResolver(), "not_kill_process_when_remove_task", 1) != 1) {
            z = false;
        }
        return z;
    }
}
