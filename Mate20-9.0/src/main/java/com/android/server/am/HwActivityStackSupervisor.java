package com.android.server.am;

import android.app.ActivityOptions;
import android.app.ActivityThread;
import android.app.IApplicationThread;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.ResolveInfo;
import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.pc.IHwPCManager;
import android.provider.Settings;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import android.rms.iaware.DataContract;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.vrsystem.IVRSystemServiceManager;
import android.widget.Toast;
import com.android.server.HwServiceFactory;
import com.android.server.UiThread;
import com.android.server.am.ActivityStack;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.huawei.android.statistical.StatisticalUtils;
import com.huawei.displayengine.IDisplayEngineService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class HwActivityStackSupervisor extends ActivityStackSupervisor {
    private static final String ACTION_CONFIRM_APPLOCK_CREDENTIAL = "huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER";
    private static final String ACTION_CONFIRM_APPLOCK_PACKAGENAME = "com.huawei.systemmanager";
    private static final int CRASH_INTERVAL_THRESHOLD = 60000;
    private static final int CRASH_TIMES_THRESHOLD = 3;
    private static final String EXTRA_USER_HANDLE_HWEX = "android.intent.extra.user_handle_hwex";
    private static final String SPLIT_SCREEN_APP_NAME = "splitscreen.SplitScreenAppActivity";
    private static final String STACK_DIVIDER_APP_NAME = "stackdivider.ForcedResizableInfoActivity";
    private int mCrashTimes;
    private long mFirstLaunchTime;
    private String mLastHomePkg;
    private int mNextPcFreeStackId = 1000000008;
    private int mNextVrFreeStackId = 0;
    /* access modifiers changed from: private */
    public Toast mToast = null;
    private IVRSystemServiceManager mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
    private final ArrayList<ActivityRecord> mWindowStateChangedActivities = new ArrayList<>();

    public HwActivityStackSupervisor(ActivityManagerService service, Looper looper) {
        super(service, looper);
        IVRSystemServiceManager iVRSystemServiceManager = this.mVrMananger;
        this.mNextVrFreeStackId = 1100000000;
    }

    private boolean isUninstallableApk(String pkgName, Intent homeIntent, int userId) {
        if (pkgName == null || "android".equals(pkgName)) {
            return false;
        }
        try {
            ResolveInfo homeInfo = resolveIntent(homeIntent, homeIntent.resolveTypeIfNeeded(this.mService.mContext.getContentResolver()), userId, 786432, Binder.getCallingUid());
            if (homeInfo == null) {
                return false;
            }
            ComponentInfo ci = homeInfo.getComponentInfo();
            if (ci != null && (ci.applicationInfo.flags & 1) == 0 && pkgName.equals(ci.packageName)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void recognitionMaliciousApp(IApplicationThread caller, Intent intent, int userId) {
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
                String strPkg2 = strPkg;
                if (strPkg2 != null) {
                    if (HwDeviceManager.disallowOp(26, strPkg2)) {
                        Slog.i("ActivityManager", strPkg2 + " is a ignored frequent relaunch app set by MDM!");
                        return;
                    }
                    if (isUninstallableApk(strPkg2, homeIntent, userId)) {
                        if (strPkg2.equals(this.mLastHomePkg)) {
                            this.mCrashTimes++;
                            long now = SystemClock.uptimeMillis();
                            if (this.mCrashTimes >= 3) {
                                if (now - this.mFirstLaunchTime < AppHibernateCst.DELAY_ONE_MINS) {
                                    try {
                                        ActivityThread.getPackageManager().clearPackagePreferredActivities(strPkg2);
                                    } catch (RemoteException e) {
                                    }
                                    this.mService.showUninstallLauncherDialog(strPkg2);
                                    this.mLastHomePkg = null;
                                    this.mCrashTimes = 0;
                                } else {
                                    this.mCrashTimes = 1;
                                    this.mFirstLaunchTime = now;
                                }
                            }
                        } else {
                            this.mLastHomePkg = strPkg2;
                            this.mCrashTimes = 0;
                            this.mFirstLaunchTime = SystemClock.uptimeMillis();
                        }
                    }
                }
                return;
            }
        }
        int i = userId;
    }

    /* access modifiers changed from: package-private */
    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int userId, int flags, int filterCallingUid) {
        int callingUserId = UserHandle.getUserId(Binder.getCallingUid());
        if (userId == callingUserId && userId == 0) {
            return HwActivityStackSupervisor.super.resolveIntent(intent, resolvedType, userId, flags, filterCallingUid);
        }
        if (!this.mService.mUserController.mInjector.getUserManagerInternal().isSameGroupForClone(callingUserId, userId)) {
            return HwActivityStackSupervisor.super.resolveIntent(intent, resolvedType, userId, flags, filterCallingUid);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            return HwActivityStackSupervisor.super.resolveIntent(intent, resolvedType, userId, flags, filterCallingUid);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private final void noteActivityDisplayedEnd(String activityName, int uid, int pid, long time) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null && resManager.isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RES_APP)) && this.mService.mSystemReady) {
            DataContract.Apps.Builder builder = DataContract.Apps.builder();
            builder.addEvent(85013);
            builder.addActivityDisplayedInfoWithUid(activityName, uid, pid, time);
            CollectData appsData = builder.build();
            long id = Binder.clearCallingIdentity();
            resManager.reportData(appsData);
            Binder.restoreCallingIdentity(id);
            Slog.d("ActivityManager", "EVENT_APP_ACTIVITY_DISPLAYED_FINISH reportData: " + activityName + " " + pid + " " + time);
        }
    }

    /* access modifiers changed from: package-private */
    public void reportActivityLaunchedLocked(boolean timeout, ActivityRecord r, long thisTime, long totalTime) {
        if (!(timeout || r == null || r.app == null || r.shortComponentName == null || r.app.pid <= 0)) {
            noteActivityDisplayedEnd(r.shortComponentName, r.app.uid, r.app.pid, thisTime);
        }
        HwActivityStackSupervisor.super.reportActivityLaunchedLocked(timeout, r, thisTime, totalTime);
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

    public void handlePCWindowStateChanged() {
        synchronized (this.mService) {
            List<ActivityRecord> list = getWindowStateChangedActivities();
            for (int i = list.size() - 1; i >= 0; i--) {
                HwActivityRecord remove = list.remove(i);
                if (remove instanceof HwActivityRecord) {
                    remove.schedulePCWindowStateChanged();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getNextVrStackId() {
        while (true) {
            int i = this.mNextVrFreeStackId;
            IVRSystemServiceManager iVRSystemServiceManager = this.mVrMananger;
            if (i >= 1100000000 && getStack(this.mNextVrFreeStackId) == null) {
                return this.mNextVrFreeStackId;
            }
            this.mNextVrFreeStackId++;
        }
    }

    /* access modifiers changed from: protected */
    public int getNextPcStackId() {
        while (true) {
            int i = this.mNextPcFreeStackId;
            IVRSystemServiceManager iVRSystemServiceManager = this.mVrMananger;
            if (i >= 1100000000) {
                this.mNextPcFreeStackId = 1000000008;
            }
            if (this.mNextPcFreeStackId >= 1000000008 && getStack(this.mNextPcFreeStackId) == null) {
                return this.mNextPcFreeStackId;
            }
            this.mNextPcFreeStackId++;
        }
    }

    /* access modifiers changed from: protected */
    public ActivityStack getValidLaunchStackOnDisplay(int displayId, ActivityRecord r) {
        ActivityDisplay activityDisplay = (ActivityDisplay) this.mActivityDisplays.get(displayId);
        if (activityDisplay == null) {
            throw new IllegalArgumentException("Display with displayId=" + displayId + " not found.");
        } else if (this.mVrMananger.isValidVRDisplayId(displayId)) {
            Slog.i("ActivityManager", "vr getValidLaunchStackOnDisplay displayid is: " + displayId);
            return HwServiceFactory.createActivityStack(activityDisplay, getNextVrStackId(), this, r.getWindowingMode(), r.getActivityType(), true);
        } else if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayId)) {
            return HwServiceFactory.createActivityStack(activityDisplay, getNextPcStackId(), this, 10, 1, true);
        } else {
            if (HwPCUtils.isPcCastModeInServer() && displayId == 0) {
                HwPCUtils.log("ActivityManager", " create full screen stack because the stack is null when r is from pc");
                activityDisplay.getOrCreateStack(1, 0, true);
            }
            return HwActivityStackSupervisor.super.getValidLaunchStackOnDisplay(displayId, r);
        }
    }

    /* access modifiers changed from: protected */
    public boolean restoreRecentTaskLocked(TaskRecord task, ActivityOptions aOptions, boolean onTop) {
        if ((HwPCUtils.isPcCastModeInServer() || this.mVrMananger.isVRDeviceConnected()) && task == null) {
            return false;
        }
        return HwActivityStackSupervisor.super.restoreRecentTaskLocked(task, aOptions, onTop);
    }

    /* access modifiers changed from: protected */
    public void handleDisplayRemoved(int displayId) {
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayId)) {
            synchronized (this.mService) {
                ActivityDisplay activityDisplay = (ActivityDisplay) this.mActivityDisplays.get(displayId);
                if (activityDisplay != null) {
                    int size = activityDisplay.getChildCount();
                    ArrayList<ActivityStack> stacks = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        stacks.add(activityDisplay.getChildAt(i));
                    }
                    onDisplayRemoved(stacks);
                    activityDisplay.remove();
                    this.mActivityDisplays.remove(displayId);
                    if (HwPCUtils.enabledInPad()) {
                        applySleepTokensLocked(false);
                    }
                }
            }
        } else if (this.mVrMananger.isValidVRDisplayId(displayId)) {
            synchronized (this.mService) {
                ActivityDisplay activityDisplay2 = (ActivityDisplay) this.mActivityDisplays.get(displayId);
                if (activityDisplay2 != null) {
                    int size2 = activityDisplay2.getChildCount();
                    ArrayList<ActivityStack> stacks2 = new ArrayList<>();
                    for (int i2 = 0; i2 < size2; i2++) {
                        stacks2.add(activityDisplay2.getChildAt(i2));
                    }
                    onVRdisplayRemoved(activityDisplay2, stacks2, displayId);
                }
            }
        } else {
            HwActivityStackSupervisor.super.handleDisplayRemoved(displayId);
        }
    }

    /* access modifiers changed from: protected */
    public boolean keepStackResumed(ActivityStack stack) {
        if (HwPCUtils.isPcCastModeInServer() && stack != null) {
            if (HwPCUtils.isPcDynamicStack(stack.mStackId) && stack.shouldBeVisible(null)) {
                return true;
            }
            if (HwPCUtils.isPcDynamicStack(this.mFocusedStack.mStackId) && !HwPCUtils.isPcDynamicStack(stack.mStackId)) {
                return true;
            }
        }
        return HwActivityStackSupervisor.super.keepStackResumed(stack);
    }

    /* access modifiers changed from: protected */
    public boolean isStackInVisible(ActivityStack stack) {
        if (stack != null && HwPCUtils.isExtDynamicStack(stack.mStackId) && stack.shouldBeVisible(null)) {
            return true;
        }
        if (!HwFreeFormUtils.isFreeFormEnable() || stack == null || !stack.inFreeformWindowingMode() || stack.shouldBeVisible(null)) {
            return HwActivityStackSupervisor.super.isStackInVisible(stack);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void showToast(final int displayId) {
        final Context context;
        if (HwPCUtils.isPcCastModeInServer()) {
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
                            Toast unused = HwActivityStackSupervisor.this.mToast = Toast.makeText(context, context.getResources().getString(33685970), 0);
                        } else {
                            Toast unused2 = HwActivityStackSupervisor.this.mToast = Toast.makeText(context, context.getString(33685971), 0);
                        }
                        if (HwActivityStackSupervisor.this.mToast != null) {
                            HwActivityStackSupervisor.this.mToast.show();
                        }
                    }
                });
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean startProcessOnExtDisplay(ActivityRecord r) {
        ActivityRecord activityRecord = r;
        if (r.getStack() == null || !HwPCUtils.isExtDynamicStack(r.getStack().mStackId) || r.getTask() == null || r.getTask().getLaunchBounds() == null) {
            return HwActivityStackSupervisor.super.startProcessOnExtDisplay(r);
        }
        String[] args = null;
        Rect launchBounds = r.getTask().getLaunchBounds();
        if (launchBounds != null) {
            args = new String[]{String.valueOf(r.getDisplayId()), String.valueOf(launchBounds.width()), String.valueOf(launchBounds.height())};
        }
        this.mService.startProcessLocked(activityRecord.processName, activityRecord.info.applicationInfo, true, 0, "activity", activityRecord.intent.getComponent(), false, false, 0, true, null, null, args, null);
        return true;
    }

    public boolean shouldNotKillProcWhenRemoveTask(String pkg) {
        if (!"com.tencent.mm".equals(pkg)) {
            return false;
        }
        Slog.d("ActivityManager", " cleanUpRemovedTaskLocked, do not kill process : " + pkg);
        return true;
    }

    public void onDisplayRemoved(ArrayList<ActivityStack> stacks) {
        ArrayList<Intent> mIntentList = new ArrayList<>();
        for (int i = stacks.size() - 1; i >= 0; i--) {
            stacks.get(i).mForceHidden = true;
        }
        ArrayList<ProcessRecord> procs = getPCProcessRecordList();
        while (!stacks.isEmpty()) {
            ActivityStack stack = stacks.get(0);
            TaskRecord tr = stack.topTask();
            if (tr != null) {
                if (tr.intent != null && !stack.inPinnedWindowingMode()) {
                    mIntentList.add(tr.intent);
                }
                if (tr instanceof HwTaskRecord) {
                    removeProcessesActivityNotFinished(((HwTaskRecord) tr).getActivities(), procs);
                }
                stack.moveTaskToBackLocked(stack.getStackId());
                stack.finishAllActivitiesLocked(true);
            } else {
                stack.finishAllActivitiesLocked(true);
            }
            stacks.remove(stack);
        }
        killPCProcessesLocked(procs);
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.saveAppIntent(mIntentList);
            } catch (RemoteException e) {
                HwPCUtils.log("ActivityManager", "fail to saveAppIntent on display removed");
            }
        }
        if (this.mService instanceof HwActivityManagerService) {
            this.mService.mHwAMSEx.getPkgDisplayMaps().clear();
        }
    }

    public void onVRdisplayRemoved(ActivityDisplay display, ArrayList<ActivityStack> stacks, int displayId) {
        Slog.i("ActivityManager", "onVRdisplayRemoved");
        if (stacks == null) {
            Slog.w("ActivityManager", "vr activitystack is null");
            return;
        }
        while (!stacks.isEmpty()) {
            ActivityStack stack = stacks.get(0);
            moveTasksToFullscreenStackLocked(stack, false);
            stacks.remove(stack);
        }
        display.remove();
        this.mActivityDisplays.remove(displayId);
        resumeFocusedStackTopActivityLocked();
    }

    /* access modifiers changed from: protected */
    public boolean resumeAppLockActivityIfNeeded(ActivityStack stack, ActivityOptions targetOptions) {
        if (this.mCurrentUser != 0) {
            return false;
        }
        ActivityRecord r = stack.topRunningActivityLocked();
        if (r == null || r.isState(ActivityStack.ActivityState.RESUMED) || !isKeyguardDismiss() || !isAppInLockList(r.packageName, r.userId)) {
            return false;
        }
        Intent newIntent = new Intent(ACTION_CONFIRM_APPLOCK_CREDENTIAL);
        newIntent.putExtra("android.intent.extra.user_handle_hwex", r.userId);
        newIntent.setFlags(109051904);
        newIntent.setPackage("com.huawei.systemmanager");
        newIntent.putExtra("android.intent.extra.TASK_ID", r.task.taskId);
        newIntent.putExtra("android.intent.extra.PACKAGE_NAME", r.packageName);
        ActivityOptions options = ActivityOptions.makeBasic();
        options.setLaunchTaskId(r.task.taskId);
        this.mService.mContext.startActivity(newIntent, options.toBundle());
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isAppInLockList(String pgkName, int userId) {
        if (pgkName != null && Settings.Secure.getInt(this.mService.mContext.getContentResolver(), "app_lock_func_status", 0) == 1) {
            if ((Settings.Secure.getStringForUser(this.mService.mContext.getContentResolver(), "app_lock_list", userId) + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER).contains(pgkName + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                return true;
            }
        }
        return false;
    }

    private boolean isKeyguardDismiss() {
        return !getKeyguardController().isKeyguardLocked() && !this.mWindowManager.isPendingLock();
    }

    public boolean isInVisibleStack(String pkg) {
        if (pkg == null || pkg.isEmpty()) {
            return false;
        }
        int size = this.mActivityDisplays.size();
        for (int displayNdx = 0; displayNdx < size; displayNdx++) {
            ActivityDisplay activityDisplay = (ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = activityDisplay.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                HwActivityStack hwStack = activityDisplay.getChildAt(stackNdx);
                if ((hwStack instanceof HwActivityStack) && hwStack.isVisibleLocked(pkg, true)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean notKillProcessWhenRemoveTask(ProcessRecord processRecord) {
        boolean z = true;
        if (this.mService.mContext == null) {
            return true;
        }
        if (Settings.System.getInt(this.mService.mContext.getContentResolver(), "not_kill_process_when_remove_task", 1) != 1) {
            z = false;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean hasActivityInStackLocked(ActivityInfo aInfo) {
        HwActivityStackSupervisor hwActivityStackSupervisor = this;
        ActivityInfo activityInfo = aInfo;
        boolean z = false;
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
        int i = 1;
        int displayNdx = hwActivityStackSupervisor.mActivityDisplays.size() - 1;
        while (displayNdx >= 0) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) hwActivityStackSupervisor.mActivityDisplays.valueAt(displayNdx)).mStacks;
            int stackNdx = stacks.size() - i;
            while (stackNdx >= 0) {
                ArrayList<TaskRecord> allTasks = stacks.get(stackNdx).getAllTasks();
                int taskNdx = allTasks.size() - i;
                while (taskNdx >= 0) {
                    TaskRecord task = allTasks.get(taskNdx);
                    if (task == null || task.userId != userId || task.affinity == null || !task.affinity.equals(affinity)) {
                        taskNdx--;
                        i = i;
                        z = false;
                    } else {
                        ArrayList<ActivityRecord> activities = task.mActivities;
                        int numActivities = activities.size();
                        if (numActivities <= 0) {
                            return z;
                        }
                        int activityNdx = numActivities - 1;
                        while (true) {
                            int activityNdx2 = activityNdx;
                            if (activityNdx2 < 0) {
                                return false;
                            }
                            ActivityRecord r = activities.get(activityNdx2);
                            if (!r.finishing && !r.isState(ActivityStack.ActivityState.DESTROYED) && r.app != null && pkgName.equals(r.packageName) && processName.equals(r.app.processName)) {
                                return true;
                            }
                            activityNdx = activityNdx2 - 1;
                        }
                    }
                }
                int i2 = i;
                stackNdx--;
                z = false;
            }
            int i3 = i;
            displayNdx--;
            hwActivityStackSupervisor = this;
            z = false;
        }
        return false;
    }

    private int getSpecialTaskId(boolean isDefaultDisplay, ActivityStack stack, String pkgName, boolean invisibleAlso) {
        TaskRecord tr;
        if (pkgName != null && !"".equals(pkgName)) {
            ArrayList<TaskRecord> tasks = stack.getAllTasks();
            for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                TaskRecord tr2 = tasks.get(taskNdx);
                if (tr2 != null && (invisibleAlso || tr2.isVisible())) {
                    int arIdx = 0;
                    ActivityRecord[] candicatedArs = {tr2.topRunningActivityLocked(), tr2.getRootActivity()};
                    while (true) {
                        int arIdx2 = arIdx;
                        if (arIdx2 >= candicatedArs.length) {
                            continue;
                            break;
                        }
                        ActivityRecord ar = candicatedArs[arIdx2];
                        HwPCUtils.log("ActivityManager", "getSpecialTaskId ar = " + ar + ", tr.isVisible() = " + tr2.isVisible());
                        if (ar != null && ar.packageName != null && ar.packageName.equals(pkgName)) {
                            return tr2.taskId;
                        }
                        arIdx = arIdx2 + 1;
                    }
                }
            }
        } else if (!isDefaultDisplay) {
            TaskRecord tr3 = stack.topTask();
            if (tr3 != null && (invisibleAlso || tr3.isVisible())) {
                HwPCUtils.log("ActivityManager", "getSpecialTaskId tr.taskId = " + tr3.taskId);
                return tr3.taskId;
            }
        } else {
            ArrayList<TaskRecord> tasks2 = stack.getAllTasks();
            int taskNdx2 = tasks2.size() - 1;
            while (true) {
                int taskNdx3 = taskNdx2;
                if (taskNdx3 < 0) {
                    break;
                }
                tr = tasks2.get(taskNdx3);
                if (tr == null || (!invisibleAlso && !tr.isVisible())) {
                    taskNdx2 = taskNdx3 - 1;
                }
            }
            return tr.taskId;
        }
        return -1;
    }

    public int getTopTaskIdInDisplay(int displayId, String pkgName, boolean invisibleAlso) {
        int N = this.mActivityDisplays.size();
        HwPCUtils.log("ActivityManager", "getTopTaskIdInDisplay displayId = " + displayId + ", N = " + N + ", pkgName = " + pkgName);
        if (displayId < 0) {
            return -1;
        }
        ActivityDisplay activityDisplay = getActivityDisplay(displayId);
        if (activityDisplay == null) {
            HwPCUtils.log("ActivityManager", "getTopTaskIdInDisplay activityDisplay not exist");
            return -1;
        }
        for (int stackNdx = activityDisplay.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            int taskId = getSpecialTaskId(displayId == 0, activityDisplay.getChildAt(stackNdx), pkgName, invisibleAlso);
            if (taskId != -1) {
                return taskId;
            }
        }
        return -1;
    }

    public Rect getPCTopTaskBounds(int displayId) {
        int N = this.mActivityDisplays.size();
        HwPCUtils.log("ActivityManager", "getPCTopTaskBounds displayId = " + displayId + ", N = " + N);
        if (displayId < 0) {
            return null;
        }
        ActivityDisplay activityDisplay = getActivityDisplay(displayId);
        if (activityDisplay == null) {
            HwPCUtils.log("ActivityManager", "getPCTopTaskBounds activityDisplay not exist");
            return null;
        }
        Rect rect = new Rect();
        int stackNdx = activityDisplay.getChildCount() - 1;
        while (stackNdx >= 0) {
            TaskRecord tr = activityDisplay.getChildAt(stackNdx).topTask();
            if (tr == null || !tr.isVisible()) {
                stackNdx--;
            } else {
                tr.getWindowContainerBounds(rect);
                HwPCUtils.log("ActivityManager", "getTaskIdInPCDisplayLocked tr.taskId = " + tr.taskId + ", rect = " + rect);
                return rect;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void uploadUnSupportSplitScreenAppPackageName(String pkgName) {
        if (this.mService.mContext != null) {
            Context context = this.mService.mContext;
            StatisticalUtils.reporte(context, 174, "{ Launcher fail, pkgName:" + pkgName + " }");
        }
    }

    /* access modifiers changed from: protected */
    public ActivityStack getTargetSplitTopStack(ActivityStack current) {
        if (current.getWindowingMode() == 3) {
            ActivityStack topSecondaryStack = current.getDisplay().getTopStackInWindowingMode(4);
            ActivityRecord topSecondaryActivityRecord = null;
            if (topSecondaryStack != null) {
                topSecondaryActivityRecord = topSecondaryStack.topRunningActivityLocked();
            }
            if (topSecondaryActivityRecord != null && !topSecondaryActivityRecord.toString().contains(SPLIT_SCREEN_APP_NAME)) {
                return topSecondaryStack;
            }
            ActivityStack homeStack = current.getDisplay().getStack(4, 2);
            if (homeStack != null) {
                return homeStack;
            }
            return null;
        } else if (current.getWindowingMode() != 4 || current.getActivityType() != 1) {
            return null;
        } else {
            ActivityStack nextTargetStack = getNextStackInSplitSecondary(current);
            ActivityRecord nextTargetRecord = null;
            if (nextTargetStack != null) {
                nextTargetRecord = nextTargetStack.topRunningActivityLocked();
            }
            if (nextTargetRecord == null || !nextTargetRecord.info.name.contains(SPLIT_SCREEN_APP_NAME)) {
                return null;
            }
            ActivityStack topPrimaryStack = current.getDisplay().getTopStackInWindowingMode(3);
            if (topPrimaryStack != null) {
                return topPrimaryStack;
            }
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public ActivityStack getNextStackInSplitSecondary(ActivityStack current) {
        ArrayList<ActivityStack> mStacks = current.getDisplay().mStacks;
        boolean returnNext = false;
        for (int i = mStacks.size() - 1; i >= 0; i--) {
            ActivityStack targetStack = mStacks.get(i);
            if (!returnNext) {
                if (targetStack.getStackId() == current.getStackId()) {
                    returnNext = true;
                }
            } else if (!targetStack.topRunningActivityLocked().info.name.contains(STACK_DIVIDER_APP_NAME) && targetStack.getWindowingMode() == 4) {
                return targetStack;
            }
        }
        return null;
    }

    private ArrayList<ProcessRecord> getPCProcessRecordList() {
        ArrayList<ProcessRecord> procs = new ArrayList<>();
        int NP = this.mService.mProcessNames.getMap().size();
        for (int ip = 0; ip < NP; ip++) {
            SparseArray<ProcessRecord> apps = (SparseArray) this.mService.mProcessNames.getMap().valueAt(ip);
            int NA = apps.size();
            for (int ia = 0; ia < NA; ia++) {
                ProcessRecord proc = apps.valueAt(ia);
                if (!(proc == this.mService.mHomeProcess || proc.mDisplayId == 0)) {
                    procs.add(proc);
                }
            }
        }
        return procs;
    }

    private void killPCProcessesLocked(ArrayList<ProcessRecord> procs) {
        int NU = procs.size();
        for (int iu = 0; iu < NU; iu++) {
            ProcessRecord pr = procs.get(iu);
            pr.kill("HwPCUtils#DisplayRemoved", true);
            HwPCUtils.log("ActivityManager", "killPCProcessesLocked: pr = " + pr);
        }
    }

    private void removeProcessesActivityNotFinished(ArrayList<ActivityRecord> activities, ArrayList<ProcessRecord> procs) {
        for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
            ActivityRecord r = activities.get(activityNdx);
            int NP = procs.size();
            int i = 0;
            while (true) {
                if (i >= NP) {
                    break;
                } else if (procs.get(i).processName.equals(r.processName)) {
                    procs.remove(i);
                    break;
                } else {
                    i++;
                }
            }
        }
    }
}
