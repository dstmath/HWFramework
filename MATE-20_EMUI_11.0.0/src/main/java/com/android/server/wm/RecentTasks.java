package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.WindowManagerPolicyConstants;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.wm.RecentTasks;
import com.google.android.collect.Sets;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RecentTasks {
    private static final int DEFAULT_INITIAL_CAPACITY = 5;
    private static final long FREEZE_TASK_LIST_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(5);
    private static final boolean IS_HW_MULTIWINDOW_SUPPORTED = SystemProperties.getBoolean("ro.config.hw_multiwindow_optimization", false);
    private static final ActivityInfo NO_ACTIVITY_INFO_TOKEN = new ActivityInfo();
    private static final ApplicationInfo NO_APPLICATION_INFO_TOKEN = new ApplicationInfo();
    private static final String TAG = "ActivityTaskManager";
    private static final String TAG_RECENTS = "ActivityTaskManager";
    private static final String TAG_TASKS = "ActivityTaskManager";
    private static final Comparator<TaskRecord> TASK_ID_COMPARATOR = $$Lambda$RecentTasks$KPkDUQ9KJvmXlmV8HHAucQJJdQ.INSTANCE;
    private long mActiveTasksSessionDurationMs;
    private final ArrayList<Callbacks> mCallbacks = new ArrayList<>();
    private boolean mFreezeTaskListReordering;
    private long mFreezeTaskListTimeoutMs = FREEZE_TASK_LIST_TIMEOUT_MS;
    private int mGlobalMaxNumTasks;
    private boolean mHasVisibleRecentTasks;
    private final boolean mIsLite = SystemProperties.getBoolean("ro.build.hw_emui_ultra_lite", false);
    private final WindowManagerPolicyConstants.PointerEventListener mListener = new WindowManagerPolicyConstants.PointerEventListener() {
        /* class com.android.server.wm.RecentTasks.AnonymousClass1 */

        public void onPointerEvent(MotionEvent ev) {
            if (RecentTasks.this.mFreezeTaskListReordering && ev.getAction() == 0) {
                RecentTasks.this.mService.mH.post(PooledLambda.obtainRunnable(new Consumer(ev.getDisplayId(), (int) ev.getX(), (int) ev.getY()) {
                    /* class com.android.server.wm.$$Lambda$RecentTasks$1$yqVuu6fkQgjlTTs6kgJbxqq3Hng */
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ int f$2;
                    private final /* synthetic */ int f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        RecentTasks.AnonymousClass1.this.lambda$onPointerEvent$0$RecentTasks$1(this.f$1, this.f$2, this.f$3, obj);
                    }
                }, (Object) null).recycleOnUse());
            }
        }

        public /* synthetic */ void lambda$onPointerEvent$0$RecentTasks$1(int displayId, int x, int y, Object nonArg) {
            synchronized (RecentTasks.this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (RecentTasks.this.mService.mRootActivityContainer.getActivityDisplay(displayId).mDisplayContent.pointWithinAppWindow(x, y)) {
                        ActivityStack stack = RecentTasks.this.mService.getTopDisplayFocusedStack();
                        RecentTasks.this.resetFreezeTaskListReordering(stack != null ? stack.topTask() : null);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    };
    private int mMaxNumVisibleTasks;
    private int mMinNumVisibleTasks;
    private final SparseArray<SparseBooleanArray> mPersistedTaskIds = new SparseArray<>(5);
    private ComponentName mRecentsComponent = null;
    private int mRecentsUid = -1;
    private final Runnable mResetFreezeTaskListOnTimeoutRunnable = new Runnable() {
        /* class com.android.server.wm.$$Lambda$Z9QEXZevRsInPMEXX0zFWg8YGMQ */

        @Override // java.lang.Runnable
        public final void run() {
            RecentTasks.this.resetFreezeTaskListReorderingOnTimeout();
        }
    };
    private final ActivityTaskManagerService mService;
    private final ActivityStackSupervisor mSupervisor;
    private final TaskPersister mTaskPersister;
    private final ArrayList<TaskRecord> mTasks = new ArrayList<>();
    private final HashMap<ComponentName, ActivityInfo> mTmpAvailActCache = new HashMap<>();
    private final HashMap<String, ApplicationInfo> mTmpAvailAppCache = new HashMap<>();
    private final SparseBooleanArray mTmpQuietProfileUserIds = new SparseBooleanArray();
    private final ArrayList<TaskRecord> mTmpRecents = new ArrayList<>();
    private final SparseBooleanArray mUsersWithRecentsLoaded = new SparseBooleanArray(5);

    /* access modifiers changed from: package-private */
    public interface Callbacks {
        void onRecentTaskAdded(TaskRecord taskRecord);

        void onRecentTaskRemoved(TaskRecord taskRecord, boolean z, boolean z2);
    }

    static /* synthetic */ int lambda$static$0(TaskRecord lhs, TaskRecord rhs) {
        return rhs.taskId - lhs.taskId;
    }

    @VisibleForTesting
    RecentTasks(ActivityTaskManagerService service, TaskPersister taskPersister) {
        this.mService = service;
        this.mSupervisor = this.mService.mStackSupervisor;
        this.mTaskPersister = taskPersister;
        this.mGlobalMaxNumTasks = ActivityTaskManager.getMaxRecentTasksStatic();
        this.mHasVisibleRecentTasks = true;
    }

    RecentTasks(ActivityTaskManagerService service, ActivityStackSupervisor stackSupervisor) {
        File systemDir = Environment.getDataSystemDirectory();
        Resources res = service.mContext.getResources();
        this.mService = service;
        this.mSupervisor = this.mService.mStackSupervisor;
        this.mTaskPersister = new TaskPersister(systemDir, stackSupervisor, service, this, stackSupervisor.mPersisterQueue);
        this.mGlobalMaxNumTasks = ActivityTaskManager.getMaxRecentTasksStatic();
        this.mHasVisibleRecentTasks = res.getBoolean(17891466);
        loadParametersFromResources(res);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setParameters(int minNumVisibleTasks, int maxNumVisibleTasks, long activeSessionDurationMs) {
        this.mMinNumVisibleTasks = minNumVisibleTasks;
        this.mMaxNumVisibleTasks = maxNumVisibleTasks;
        this.mActiveTasksSessionDurationMs = activeSessionDurationMs;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setGlobalMaxNumTasks(int globalMaxNumTasks) {
        this.mGlobalMaxNumTasks = globalMaxNumTasks;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setFreezeTaskListTimeout(long timeoutMs) {
        this.mFreezeTaskListTimeoutMs = timeoutMs;
    }

    /* access modifiers changed from: package-private */
    public WindowManagerPolicyConstants.PointerEventListener getInputListener() {
        return this.mListener;
    }

    /* access modifiers changed from: package-private */
    public void setFreezeTaskListReordering() {
        this.mFreezeTaskListReordering = true;
        this.mService.mH.removeCallbacks(this.mResetFreezeTaskListOnTimeoutRunnable);
        this.mService.mH.postDelayed(this.mResetFreezeTaskListOnTimeoutRunnable, this.mFreezeTaskListTimeoutMs);
    }

    /* access modifiers changed from: package-private */
    public void resetFreezeTaskListReordering(TaskRecord topTask) {
        if (this.mFreezeTaskListReordering) {
            this.mFreezeTaskListReordering = false;
            this.mService.mH.removeCallbacks(this.mResetFreezeTaskListOnTimeoutRunnable);
            if (topTask != null) {
                this.mTasks.remove(topTask);
                this.mTasks.add(0, topTask);
            }
            trimInactiveRecentTasks();
            this.mService.getTaskChangeNotificationController().notifyTaskStackChanged();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void resetFreezeTaskListReorderingOnTimeout() {
        TaskRecord topTask;
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityStack focusedStack = this.mService.getTopDisplayFocusedStack();
                if (focusedStack != null) {
                    topTask = focusedStack.topTask();
                } else {
                    topTask = null;
                }
                resetFreezeTaskListReordering(topTask);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isFreezeTaskListReorderingSet() {
        return this.mFreezeTaskListReordering;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void loadParametersFromResources(Resources res) {
        long j;
        if (ActivityManager.isLowRamDeviceStatic()) {
            this.mMinNumVisibleTasks = res.getInteger(17694843);
            this.mMaxNumVisibleTasks = res.getInteger(17694834);
        } else if (SystemProperties.getBoolean("ro.recents.grid", false)) {
            this.mMinNumVisibleTasks = res.getInteger(17694842);
            this.mMaxNumVisibleTasks = res.getInteger(17694833);
        } else {
            this.mMinNumVisibleTasks = res.getInteger(17694841);
            this.mMaxNumVisibleTasks = res.getInteger(17694832);
        }
        int sessionDurationHrs = res.getInteger(17694729);
        if (sessionDurationHrs > 0) {
            j = TimeUnit.HOURS.toMillis((long) sessionDurationHrs);
        } else {
            j = -1;
        }
        this.mActiveTasksSessionDurationMs = j;
    }

    /* access modifiers changed from: package-private */
    public void loadRecentsComponent(Resources res) {
        ComponentName cn;
        String rawRecentsComponent = res.getString(17039888);
        if (!TextUtils.isEmpty(rawRecentsComponent) && (cn = ComponentName.unflattenFromString(rawRecentsComponent)) != null) {
            try {
                ApplicationInfo appInfo = AppGlobals.getPackageManager().getApplicationInfo(cn.getPackageName(), 0, this.mService.mContext.getUserId());
                if (appInfo != null) {
                    this.mRecentsUid = appInfo.uid;
                    this.mRecentsComponent = cn;
                }
            } catch (RemoteException e) {
                Slog.w("ActivityTaskManager", "Could not load application info for recents component: " + cn);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isCallerRecents(int callingUid) {
        return UserHandle.isSameApp(callingUid, this.mRecentsUid);
    }

    /* access modifiers changed from: package-private */
    public boolean isRecentsComponent(ComponentName cn, int uid) {
        return cn.equals(this.mRecentsComponent) && UserHandle.isSameApp(uid, this.mRecentsUid);
    }

    /* access modifiers changed from: package-private */
    public boolean isRecentsComponentHomeActivity(int userId) {
        ComponentName defaultHomeActivity = this.mService.getPackageManagerInternalLocked().getDefaultHomeActivity(userId);
        return (defaultHomeActivity == null || this.mRecentsComponent == null || !defaultHomeActivity.getPackageName().equals(this.mRecentsComponent.getPackageName())) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public ComponentName getRecentsComponent() {
        return this.mRecentsComponent;
    }

    /* access modifiers changed from: package-private */
    public int getRecentsComponentUid() {
        return this.mRecentsUid;
    }

    /* access modifiers changed from: package-private */
    public void registerCallback(Callbacks callback) {
        this.mCallbacks.add(callback);
    }

    /* access modifiers changed from: package-private */
    public void unregisterCallback(Callbacks callback) {
        this.mCallbacks.remove(callback);
    }

    private void notifyTaskAdded(TaskRecord task) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).onRecentTaskAdded(task);
        }
    }

    private void notifyTaskRemoved(TaskRecord task, boolean wasTrimmed, boolean killProcess) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).onRecentTaskRemoved(task, wasTrimmed, killProcess);
        }
    }

    /* access modifiers changed from: package-private */
    public void loadUserRecentsLocked(int userId) {
        if (!this.mUsersWithRecentsLoaded.get(userId) && !this.mIsLite) {
            loadPersistedTaskIdsForUserLocked(userId);
            SparseBooleanArray preaddedTasks = new SparseBooleanArray();
            Iterator<TaskRecord> it = this.mTasks.iterator();
            while (it.hasNext()) {
                TaskRecord task = it.next();
                if (task.userId == userId && shouldPersistTaskLocked(task)) {
                    preaddedTasks.put(task.taskId, true);
                }
            }
            Slog.i("ActivityTaskManager", "Loading recents for user " + userId + " into memory.");
            this.mTasks.addAll(this.mTaskPersister.restoreTasksForUserLocked(userId, preaddedTasks));
            cleanupLocked(userId);
            this.mUsersWithRecentsLoaded.put(userId, true);
            if (preaddedTasks.size() > 0) {
                syncPersistentTaskIdsLocked();
            }
        }
    }

    private void loadPersistedTaskIdsForUserLocked(int userId) {
        if (this.mPersistedTaskIds.get(userId) == null) {
            this.mPersistedTaskIds.put(userId, this.mTaskPersister.loadPersistedTaskIdsForUser(userId));
            Slog.i("ActivityTaskManager", "Loaded persisted task ids for user " + userId);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean containsTaskId(int taskId, int userId) {
        loadPersistedTaskIdsForUserLocked(userId);
        return this.mPersistedTaskIds.get(userId).get(taskId);
    }

    /* access modifiers changed from: package-private */
    public SparseBooleanArray getTaskIdsForUser(int userId) {
        loadPersistedTaskIdsForUserLocked(userId);
        return this.mPersistedTaskIds.get(userId);
    }

    /* access modifiers changed from: package-private */
    public void notifyTaskPersisterLocked(TaskRecord task, boolean flush) {
        ActivityStack stack = task != null ? task.getStack() : null;
        if (stack != null && stack.isHomeOrRecentsStack()) {
            return;
        }
        if ((stack == null || !HwPCUtils.isExtDynamicStack(stack.getStackId())) && !this.mIsLite) {
            syncPersistentTaskIdsLocked();
            this.mTaskPersister.wakeup(task, flush);
        }
    }

    private void syncPersistentTaskIdsLocked() {
        for (int i = this.mPersistedTaskIds.size() - 1; i >= 0; i--) {
            if (this.mUsersWithRecentsLoaded.get(this.mPersistedTaskIds.keyAt(i))) {
                this.mPersistedTaskIds.valueAt(i).clear();
            }
        }
        for (int i2 = this.mTasks.size() - 1; i2 >= 0; i2--) {
            TaskRecord task = this.mTasks.get(i2);
            if (shouldPersistTaskLocked(task)) {
                if (this.mPersistedTaskIds.get(task.userId) == null) {
                    Slog.wtf("ActivityTaskManager", "No task ids found for userId " + task.userId + ". task=" + task + " mPersistedTaskIds=" + this.mPersistedTaskIds);
                    this.mPersistedTaskIds.put(task.userId, new SparseBooleanArray());
                }
                this.mPersistedTaskIds.get(task.userId).put(task.taskId, true);
            }
        }
    }

    private static boolean shouldPersistTaskLocked(TaskRecord task) {
        ActivityStack stack = task.getStack();
        return task.isPersistable && (stack == null || !stack.isHomeOrRecentsStack());
    }

    /* access modifiers changed from: package-private */
    public void onSystemReadyLocked() {
        if (!this.mIsLite) {
            loadRecentsComponent(this.mService.mContext.getResources());
            this.mTasks.clear();
        }
    }

    /* access modifiers changed from: package-private */
    public Bitmap getTaskDescriptionIcon(String path) {
        return this.mTaskPersister.getTaskDescriptionIcon(path);
    }

    /* access modifiers changed from: package-private */
    public void saveImage(Bitmap image, String path) {
        if (!this.mIsLite) {
            this.mTaskPersister.saveImage(image, path);
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void flush() {
        if (!this.mIsLite) {
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    syncPersistentTaskIdsLocked();
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            this.mTaskPersister.flush();
        }
    }

    /* access modifiers changed from: package-private */
    public int[] usersWithRecentsLoadedLocked() {
        int[] usersWithRecentsLoaded = new int[this.mUsersWithRecentsLoaded.size()];
        int len = 0;
        for (int i = 0; i < usersWithRecentsLoaded.length; i++) {
            int userId = this.mUsersWithRecentsLoaded.keyAt(i);
            if (this.mUsersWithRecentsLoaded.valueAt(i)) {
                usersWithRecentsLoaded[len] = userId;
                len++;
            }
        }
        if (len < usersWithRecentsLoaded.length) {
            return Arrays.copyOf(usersWithRecentsLoaded, len);
        }
        return usersWithRecentsLoaded;
    }

    /* access modifiers changed from: package-private */
    public void unloadUserDataFromMemoryLocked(int userId) {
        if (this.mUsersWithRecentsLoaded.get(userId)) {
            Slog.i("ActivityTaskManager", "Unloading recents for user " + userId + " from memory.");
            this.mUsersWithRecentsLoaded.delete(userId);
            removeTasksForUserLocked(userId);
        }
        this.mPersistedTaskIds.delete(userId);
        this.mTaskPersister.unloadUserDataFromMemory(userId);
    }

    private void removeTasksForUserLocked(int userId) {
        if (userId <= 0) {
            Slog.i("ActivityTaskManager", "Can't remove recent task on user " + userId);
            return;
        }
        for (int i = this.mTasks.size() - 1; i >= 0; i--) {
            TaskRecord tr = this.mTasks.get(i);
            if (tr.userId == userId) {
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.i("ActivityTaskManager", "remove RecentTask " + tr + " when finishing user" + userId);
                }
                remove(tr);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onPackagesSuspendedChanged(String[] packages, boolean suspended, int userId) {
        Set<String> packageNames = Sets.newHashSet(packages);
        for (int i = this.mTasks.size() - 1; i >= 0; i--) {
            TaskRecord tr = this.mTasks.get(i);
            if (tr.realActivity != null && packageNames.contains(tr.realActivity.getPackageName()) && tr.userId == userId && tr.realActivitySuspended != suspended) {
                tr.realActivitySuspended = suspended;
                if (suspended) {
                    this.mSupervisor.removeTaskByIdLocked(tr.taskId, false, true, "suspended-package");
                }
                notifyTaskPersisterLocked(tr, false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onLockTaskModeStateChanged(int lockTaskModeState, int userId) {
        if (lockTaskModeState == 1) {
            for (int i = this.mTasks.size() - 1; i >= 0; i--) {
                TaskRecord tr = this.mTasks.get(i);
                if (tr.userId == userId && !this.mService.getLockTaskController().isTaskWhitelisted(tr)) {
                    remove(tr);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeTasksByPackageName(String packageName, int userId) {
        for (int i = this.mTasks.size() - 1; i >= 0; i--) {
            TaskRecord tr = this.mTasks.get(i);
            String taskPackageName = tr.getBaseIntent().getComponent().getPackageName();
            if (tr.userId == userId && taskPackageName.equals(packageName)) {
                this.mSupervisor.removeTaskByIdLocked(tr.taskId, true, true, "remove-package-task");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeAllVisibleTasks(int userId) {
        Set<Integer> profileIds = getProfileIds(userId);
        for (int i = this.mTasks.size() - 1; i >= 0; i--) {
            TaskRecord tr = this.mTasks.get(i);
            if (profileIds.contains(Integer.valueOf(tr.userId)) && isVisibleRecentTask(tr)) {
                this.mTasks.remove(i);
                notifyTaskRemoved(tr, true, true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void cleanupDisabledPackageTasksLocked(String packageName, Set<String> filterByClasses, int userId) {
        for (int i = this.mTasks.size() - 1; i >= 0; i--) {
            TaskRecord tr = this.mTasks.get(i);
            if (userId == -1 || tr.userId == userId) {
                ComponentName cn = tr.intent != null ? tr.intent.getComponent() : null;
                if (cn != null && cn.getPackageName().equals(packageName) && (filterByClasses == null || filterByClasses.contains(cn.getClassName()))) {
                    this.mSupervisor.removeTaskByIdLocked(tr.taskId, false, true, "disabled-package");
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void cleanupLocked(int userId) {
        int recentsCount = this.mTasks.size();
        if (recentsCount != 0) {
            this.mTmpAvailActCache.clear();
            this.mTmpAvailAppCache.clear();
            IPackageManager pm = AppGlobals.getPackageManager();
            for (int i = recentsCount - 1; i >= 0; i--) {
                TaskRecord task = this.mTasks.get(i);
                if (userId == -1 || task.userId == userId) {
                    if (task.autoRemoveRecents && task.getTopActivity() == null) {
                        remove(task);
                        Slog.w("ActivityTaskManager", "Removing auto-remove without activity: " + task);
                    } else if (task.realActivity != null) {
                        ActivityInfo ai = this.mTmpAvailActCache.get(task.realActivity);
                        if (ai == null) {
                            try {
                                ai = pm.getActivityInfo(task.realActivity, 268436480, userId);
                                if (ai == null) {
                                    ai = NO_ACTIVITY_INFO_TOKEN;
                                }
                                this.mTmpAvailActCache.put(task.realActivity, ai);
                            } catch (RemoteException e) {
                            }
                        }
                        if (ai == NO_ACTIVITY_INFO_TOKEN) {
                            ApplicationInfo app = this.mTmpAvailAppCache.get(task.realActivity.getPackageName());
                            if (app == null) {
                                try {
                                    app = pm.getApplicationInfo(task.realActivity.getPackageName(), 8192, userId);
                                    if (app == null) {
                                        app = NO_APPLICATION_INFO_TOKEN;
                                    }
                                    this.mTmpAvailAppCache.put(task.realActivity.getPackageName(), app);
                                } catch (RemoteException e2) {
                                }
                            }
                            if (app == NO_APPLICATION_INFO_TOKEN || (8388608 & app.flags) == 0) {
                                remove(task);
                                Slog.w("ActivityTaskManager", "Removing no longer valid recent: " + task);
                            } else {
                                if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS && task.isAvailable) {
                                    Slog.d("ActivityTaskManager", "Making recent unavailable: " + task);
                                }
                                task.isAvailable = false;
                            }
                        } else if (!ai.enabled || !ai.applicationInfo.enabled || (ai.applicationInfo.flags & 8388608) == 0) {
                            if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS && task.isAvailable) {
                                Slog.d("ActivityTaskManager", "Making recent unavailable: " + task + " (enabled=" + ai.enabled + "/" + ai.applicationInfo.enabled + " flags=" + Integer.toHexString(ai.applicationInfo.flags) + ")");
                            }
                            task.isAvailable = false;
                        } else {
                            if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS && !task.isAvailable) {
                                Slog.d("ActivityTaskManager", "Making recent available: " + task);
                            }
                            task.isAvailable = true;
                        }
                    }
                }
            }
            int i2 = 0;
            int recentsCount2 = this.mTasks.size();
            while (i2 < recentsCount2) {
                i2 = processNextAffiliateChainLocked(i2);
            }
        }
    }

    private boolean canAddTaskWithoutTrim(TaskRecord task) {
        return findRemoveIndexForAddTask(task) == -1;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<IBinder> getAppTasksList(int callingUid, String callingPackage) {
        Intent intent;
        ArrayList<IBinder> list = new ArrayList<>();
        int size = this.mTasks.size();
        for (int i = 0; i < size; i++) {
            TaskRecord tr = this.mTasks.get(i);
            if (tr.effectiveUid == callingUid && (intent = tr.getBaseIntent()) != null && callingPackage.equals(intent.getComponent().getPackageName())) {
                list.add(new AppTaskImpl(this.mService, tr.taskId, callingUid).asBinder());
            }
        }
        return list;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Set<Integer> getProfileIds(int userId) {
        Set<Integer> userIds = new ArraySet<>();
        List<UserInfo> profiles = this.mService.getUserManager().getProfiles(userId, false);
        for (int i = profiles.size() - 1; i >= 0; i--) {
            userIds.add(Integer.valueOf(profiles.get(i).id));
        }
        return userIds;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public UserInfo getUserInfo(int userId) {
        return this.mService.getUserManager().getUserInfo(userId);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int[] getCurrentProfileIds() {
        return this.mService.mAmInternal.getCurrentProfileIds();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isUserRunning(int userId, int flags) {
        return this.mService.mAmInternal.isUserRunning(userId, flags);
    }

    /* access modifiers changed from: package-private */
    public ParceledListSlice<ActivityManager.RecentTaskInfo> getRecentTasks(int maxNum, int flags, boolean getTasksAllowed, boolean getDetailedTasks, int userId, int callingUid) {
        return new ParceledListSlice<>(getRecentTasksImpl(maxNum, flags, getTasksAllowed, getDetailedTasks, userId, callingUid));
    }

    private ArrayList<ActivityManager.RecentTaskInfo> getRecentTasksImpl(int maxNum, int flags, boolean getTasksAllowed, boolean getDetailedTasks, int userId, int callingUid) {
        boolean withExcluded = (flags & 1) != 0;
        if (!isUserRunning(userId, 4)) {
            Slog.i("ActivityTaskManager", "user " + userId + " is still locked. Cannot load recents");
            return new ArrayList<>();
        }
        loadUserRecentsLocked(userId);
        Set<Integer> includedUsers = getProfileIds(userId);
        includedUsers.add(Integer.valueOf(userId));
        ArrayList<ActivityManager.RecentTaskInfo> res = new ArrayList<>();
        int size = this.mTasks.size();
        int numVisibleTasks = 0;
        for (int i = 0; i < size; i++) {
            TaskRecord tr = this.mTasks.get(i);
            if (!tr.inHwFreeFormWindowingMode() || !isCallerRecents(Binder.getCallingUid()) || tr.inHwPCMultiStackWindowingMode()) {
                if (isVisibleRecentTask(tr)) {
                    numVisibleTasks++;
                    if (isInVisibleRange(tr, i, numVisibleTasks, withExcluded)) {
                        if (res.size() < maxNum) {
                            if (!includedUsers.contains(Integer.valueOf(tr.userId))) {
                                if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                                    Slog.d("ActivityTaskManager", "Skipping, not user: " + tr);
                                }
                            } else if (tr.realActivitySuspended) {
                                if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                                    Slog.d("ActivityTaskManager", "Skipping, activity suspended: " + tr);
                                }
                            } else if (this.mService.mHwATMSEx.isMagicWinExcludeTaskFromRecents(tr)) {
                                if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                                    Slog.d("ActivityTaskManager", "Skipping magciwindow activity: " + tr);
                                }
                            } else if (withExcluded || tr.intent == null || (tr.intent.getFlags() & 8388608) == 0) {
                                if (!getTasksAllowed) {
                                    if (!tr.isActivityTypeHome()) {
                                        if (tr.effectiveUid != callingUid) {
                                            if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                                                Slog.d("ActivityTaskManager", "Skipping, not allowed: " + tr);
                                            }
                                        }
                                    }
                                }
                                if (!tr.autoRemoveRecents || tr.getTopActivity() != null) {
                                    if ((flags & 2) == 0 || tr.isAvailable) {
                                        if (tr.mUserSetupComplete) {
                                            ActivityManager.RecentTaskInfo rti = createRecentTaskInfo(tr);
                                            if (!getDetailedTasks) {
                                                rti.baseIntent.replaceExtras((Bundle) null);
                                            }
                                            res.add(rti);
                                        } else if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                                            Slog.d("ActivityTaskManager", "Skipping, user setup not complete: " + tr);
                                        }
                                    } else if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                                        Slog.d("ActivityTaskManager", "Skipping, unavail real act: " + tr);
                                    }
                                } else if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                                    Slog.d("ActivityTaskManager", "Skipping, auto-remove without activity: " + tr);
                                }
                            }
                        }
                    }
                }
            } else if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                Slog.i("ActivityTaskManager", "Skipping hw freeform for caller recents: " + tr);
            }
        }
        return res;
    }

    /* access modifiers changed from: package-private */
    public void getPersistableTaskIds(ArraySet<Integer> persistentTaskIds) {
        int size = this.mTasks.size();
        for (int i = 0; i < size; i++) {
            TaskRecord task = this.mTasks.get(i);
            ActivityStack stack = task.getStack();
            if ((task.isPersistable || task.inRecents) && (stack == null || !stack.isHomeOrRecentsStack())) {
                persistentTaskIds.add(Integer.valueOf(task.taskId));
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public ArrayList<TaskRecord> getRawTasks() {
        return this.mTasks;
    }

    /* access modifiers changed from: package-private */
    public SparseBooleanArray getRecentTaskIds() {
        SparseBooleanArray res = new SparseBooleanArray();
        int size = this.mTasks.size();
        int numVisibleTasks = 0;
        for (int i = 0; i < size; i++) {
            TaskRecord tr = this.mTasks.get(i);
            if (isVisibleRecentTask(tr)) {
                numVisibleTasks++;
                if (isInVisibleRange(tr, i, numVisibleTasks, false)) {
                    res.put(tr.taskId, true);
                }
            }
        }
        return res;
    }

    /* access modifiers changed from: package-private */
    public TaskRecord getTask(int id) {
        int recentsCount = this.mTasks.size();
        for (int i = 0; i < recentsCount; i++) {
            TaskRecord tr = this.mTasks.get(i);
            if (tr.taskId == id) {
                return tr;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void add(TaskRecord task) {
        int taskIndex;
        if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
            Slog.d("ActivityTaskManager", "add: task=" + task);
        }
        if (task.mStack == null || !HwPCUtils.isExtDynamicStack(task.mStack.mStackId)) {
            boolean isAffiliated = (task.mAffiliatedTaskId == task.taskId && task.mNextAffiliateTaskId == -1 && task.mPrevAffiliateTaskId == -1) ? false : true;
            int recentsCount = this.mTasks.size();
            if (task.voiceSession != null) {
                if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                    Slog.d("ActivityTaskManager", "addRecent: not adding voice interaction " + task);
                }
            } else if (isAffiliated || recentsCount <= 0 || this.mTasks.get(0) != task) {
                if (!isAffiliated || recentsCount <= 0 || !task.inRecents || task.mAffiliatedTaskId != this.mTasks.get(0).mAffiliatedTaskId) {
                    boolean needAffiliationFix = false;
                    if (task.inRecents) {
                        int taskIndex2 = this.mTasks.indexOf(task);
                        if (taskIndex2 < 0) {
                            Slog.wtf("ActivityTaskManager", "Task with inRecent not in recents: " + task);
                            needAffiliationFix = true;
                        } else if (!isAffiliated) {
                            if (!this.mFreezeTaskListReordering) {
                                this.mTasks.remove(taskIndex2);
                                this.mTasks.add(0, task);
                                if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                                    Slog.d("ActivityTaskManager", "addRecent: moving to top " + task + " from " + taskIndex2);
                                }
                            }
                            notifyTaskPersisterLocked(task, false);
                            return;
                        }
                    }
                    if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                        Slog.d("ActivityTaskManager", "addRecent: trimming tasks for " + task);
                    }
                    removeForAddTask(task);
                    task.inRecents = true;
                    if (!isAffiliated || needAffiliationFix) {
                        this.mTasks.add(0, task);
                        notifyTaskAdded(task);
                        if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                            Slog.d("ActivityTaskManager", "addRecent: adding " + task);
                        }
                    } else if (isAffiliated) {
                        TaskRecord other = task.mNextAffiliate;
                        if (other == null) {
                            other = task.mPrevAffiliate;
                        }
                        if (other != null) {
                            int otherIndex = this.mTasks.indexOf(other);
                            if (otherIndex >= 0) {
                                if (other == task.mNextAffiliate) {
                                    taskIndex = otherIndex + 1;
                                } else {
                                    taskIndex = otherIndex;
                                }
                                if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                                    Slog.d("ActivityTaskManager", "addRecent: new affiliated task added at " + taskIndex + ": " + task);
                                }
                                this.mTasks.add(taskIndex, task);
                                notifyTaskAdded(task);
                                if (!moveAffiliatedTasksToFront(task, taskIndex)) {
                                    needAffiliationFix = true;
                                } else {
                                    return;
                                }
                            } else {
                                if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                                    Slog.d("ActivityTaskManager", "addRecent: couldn't find other affiliation " + other);
                                }
                                needAffiliationFix = true;
                            }
                        } else {
                            if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                                Slog.d("ActivityTaskManager", "addRecent: adding affiliated task without next/prev:" + task);
                            }
                            needAffiliationFix = true;
                        }
                    }
                    if (needAffiliationFix) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                            Slog.d("ActivityTaskManager", "addRecent: regrouping affiliations");
                        }
                        cleanupLocked(task.userId);
                    }
                    trimInactiveRecentTasks();
                } else if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                    Slog.d("ActivityTaskManager", "addRecent: affiliated " + this.mTasks.get(0) + " at top when adding " + task);
                }
            } else if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                Slog.d("ActivityTaskManager", "addRecent: already at top: " + task);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean addToBottom(TaskRecord task) {
        if (!canAddTaskWithoutTrim(task)) {
            return false;
        }
        add(task);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void remove(TaskRecord task) {
        if (task != null && task.inHwFreeFormWindowingMode()) {
            this.mService.mHwATMSEx.removeHwFreeFormBoundsRecordById(task.taskId);
        }
        this.mTasks.remove(task);
        notifyTaskRemoved(task, false, false);
    }

    private void trimInactiveRecentTasks() {
        if (!this.mFreezeTaskListReordering) {
            int recentsCount = this.mTasks.size();
            while (recentsCount > this.mGlobalMaxNumTasks) {
                TaskRecord tr = this.mTasks.remove(recentsCount - 1);
                notifyTaskRemoved(tr, true, false);
                recentsCount--;
                if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
                    Slog.d("ActivityTaskManager", "Trimming over max-recents task=" + tr + " max=" + this.mGlobalMaxNumTasks);
                }
            }
            int[] profileUserIds = getCurrentProfileIds();
            this.mTmpQuietProfileUserIds.clear();
            for (int userId : profileUserIds) {
                UserInfo userInfo = getUserInfo(userId);
                if (userInfo != null && userInfo.isManagedProfile() && userInfo.isQuietModeEnabled()) {
                    this.mTmpQuietProfileUserIds.put(userId, true);
                }
                if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
                    Slog.d("ActivityTaskManager", "User: " + userInfo + " quiet=" + this.mTmpQuietProfileUserIds.get(userId));
                }
            }
            int numVisibleTasks = 0;
            int i = 0;
            while (i < this.mTasks.size()) {
                TaskRecord task = this.mTasks.get(i);
                if (isActiveRecentTask(task, this.mTmpQuietProfileUserIds)) {
                    if (!this.mHasVisibleRecentTasks) {
                        i++;
                    } else if (!isVisibleRecentTask(task)) {
                        i++;
                    } else {
                        numVisibleTasks++;
                        if (isInVisibleRange(task, i, numVisibleTasks, false) || !isTrimmable(task)) {
                            i++;
                        } else if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
                            Slog.d("ActivityTaskManager", "Trimming out-of-range visible task=" + task);
                        }
                    }
                } else if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
                    Slog.d("ActivityTaskManager", "Trimming inactive task=" + task);
                }
                this.mTasks.remove(task);
                notifyTaskRemoved(task, true, false);
                notifyTaskPersisterLocked(task, false);
            }
        }
    }

    private boolean isActiveRecentTask(TaskRecord task, SparseBooleanArray quietProfileUserIds) {
        TaskRecord affiliatedTask;
        if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
            Slog.d("ActivityTaskManager", "isActiveRecentTask: task=" + task + " globalMax=" + this.mGlobalMaxNumTasks);
        }
        if (quietProfileUserIds.get(task.userId)) {
            if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
                Slog.d("ActivityTaskManager", "\tisQuietProfileTask=true");
            }
            return false;
        } else if (task.mAffiliatedTaskId == -1 || task.mAffiliatedTaskId == task.taskId || (affiliatedTask = getTask(task.mAffiliatedTaskId)) == null || isActiveRecentTask(affiliatedTask, quietProfileUserIds)) {
            return true;
        } else {
            if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
                Slog.d("ActivityTaskManager", "\taffiliatedWithTask=" + affiliatedTask + " is not active");
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isVisibleRecentTask(TaskRecord task) {
        int windowingMode;
        ActivityDisplay display;
        if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
            Slog.d("ActivityTaskManager", "isVisibleRecentTask: task=" + task + " minVis=" + this.mMinNumVisibleTasks + " maxVis=" + this.mMaxNumVisibleTasks + " sessionDuration=" + this.mActiveTasksSessionDurationMs + " inactiveDuration=" + task.getInactiveDuration() + " activityType=" + task.getActivityType() + " windowingMode=" + task.getWindowingMode() + " intentFlags=" + task.getBaseIntent().getFlags());
        }
        int activityType = task.getActivityType();
        if (activityType == 2 || activityType == 3) {
            return false;
        }
        if ((activityType == 4 && (task.getBaseIntent().getFlags() & 8388608) == 8388608) || (windowingMode = task.getWindowingMode()) == 2) {
            return false;
        }
        if (windowingMode == 3) {
            if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS && task.getStack() != null) {
                Slog.d("ActivityTaskManager", "\ttop=" + task.getStack().topTask());
            }
            ActivityStack stack = task.getStack();
            if (stack != null && stack.topTask() == task) {
                return false;
            }
        }
        ActivityStack stack2 = task.getStack();
        if ((stack2 == null || (display = stack2.getDisplay()) == null || !display.isSingleTaskInstance()) && task != this.mService.getLockTaskController().getRootTask()) {
            return true;
        }
        return false;
    }

    private boolean isInVisibleRange(TaskRecord task, int taskIndex, int numVisibleTasks, boolean skipExcludedCheck) {
        if (!skipExcludedCheck) {
            if ((task.getBaseIntent().getFlags() & 8388608) == 8388608) {
                if (!((task.getBaseIntent().getHwFlags() & 16384) == 16384)) {
                    if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
                        Slog.d("ActivityTaskManager", "\texcludeFromRecents=true");
                    }
                    return taskIndex == 0;
                }
            }
        }
        int i = this.mMinNumVisibleTasks;
        if (i >= 0 && numVisibleTasks <= i) {
            return true;
        }
        int i2 = this.mMaxNumVisibleTasks;
        return i2 >= 0 ? numVisibleTasks <= i2 : this.mActiveTasksSessionDurationMs > FREEZE_TASK_LIST_TIMEOUT_MS && task.getInactiveDuration() <= this.mActiveTasksSessionDurationMs;
    }

    /* access modifiers changed from: protected */
    public boolean isTrimmable(TaskRecord task) {
        ActivityStack stack = task.getStack();
        if (stack == null) {
            return true;
        }
        if (stack.mDisplayId != 0) {
            return false;
        }
        ActivityDisplay display = stack.getDisplay();
        if (display.getIndexOf(stack) < display.getIndexOf(display.getHomeStack())) {
            return true;
        }
        return false;
    }

    private void removeForAddTask(TaskRecord task) {
        int removeIndex = findRemoveIndexForAddTask(task);
        if (removeIndex != -1) {
            TaskRecord removedTask = this.mTasks.remove(removeIndex);
            if (removedTask != task) {
                notifyTaskRemoved(removedTask, false, false);
                if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
                    Slog.d("ActivityTaskManager", "Trimming task=" + removedTask + " for addition of task=" + task);
                }
            }
            notifyTaskPersisterLocked(removedTask, false);
        }
    }

    private int findRemoveIndexForAddTask(TaskRecord task) {
        char c;
        if (this.mFreezeTaskListReordering) {
            return -1;
        }
        int recentsCount = this.mTasks.size();
        Intent intent = task.intent;
        boolean multiTasksAllowed = false;
        int flags = intent != null ? intent.getFlags() : 0;
        int i = 268959744;
        int i2 = 134217728;
        if (!((flags & 268959744) == 0 || (flags & 134217728) == 0)) {
            multiTasksAllowed = true;
        }
        int hwFlags = intent != null ? intent.getHwFlags() : 0;
        char c2 = 0;
        if (multiTasksAllowed && IS_HW_MULTIWINDOW_SUPPORTED && (hwFlags & 131072) != 0) {
            return -1;
        }
        boolean document = intent != null && intent.isDocument();
        int maxRecents = task.maxRecents - 1;
        int i3 = 0;
        while (i3 < recentsCount) {
            TaskRecord tr = this.mTasks.get(i3);
            if (task != tr) {
                if (!hasCompatibleActivityTypeAndWindowingMode(task, tr)) {
                    c = c2;
                } else if (task.userId != tr.userId) {
                    c = c2;
                } else if (this.mService.mHwATMSEx.isMagicWinSkipRemoveFromRecentTasks(task, tr)) {
                    c = c2;
                } else {
                    Intent trIntent = tr.intent;
                    boolean sameAffinity = task.affinity != null && task.affinity.equals(tr.affinity);
                    boolean sameIntent = intent != null && intent.filterEquals(trIntent);
                    boolean trIsDocument = trIntent != null && trIntent.isDocument();
                    boolean bothDocuments = document && trIsDocument;
                    if (sameAffinity || sameIntent || bothDocuments) {
                        boolean trMultiTasksAllowed = false;
                        int trFlags = trIntent != null ? trIntent.getFlags() : 0;
                        if (!((trFlags & i) == 0 || (trFlags & i2) == 0)) {
                            trMultiTasksAllowed = true;
                        }
                        int trHwFlags = trIntent != null ? trIntent.getHwFlags() : 0;
                        if (!trMultiTasksAllowed || !IS_HW_MULTIWINDOW_SUPPORTED) {
                            c = 0;
                        } else {
                            c = 0;
                            if ((trHwFlags & 131072) != 0) {
                                continue;
                            }
                        }
                        if (bothDocuments) {
                            if (!((task.realActivity == null || tr.realActivity == null || !task.realActivity.equals(tr.realActivity)) ? false : true)) {
                                continue;
                            } else if (maxRecents > 0) {
                                maxRecents--;
                                if (sameIntent && !multiTasksAllowed) {
                                }
                            }
                        } else if (!document && !trIsDocument) {
                        }
                    } else {
                        c = 0;
                    }
                }
                i3++;
                c2 = c;
                i = 268959744;
                i2 = 134217728;
            }
            return i3;
        }
        return -1;
    }

    private int processNextAffiliateChainLocked(int start) {
        TaskRecord startTask = this.mTasks.get(start);
        int affiliateId = startTask.mAffiliatedTaskId;
        if (startTask.taskId == affiliateId && startTask.mPrevAffiliate == null && startTask.mNextAffiliate == null) {
            startTask.inRecents = true;
            return start + 1;
        }
        this.mTmpRecents.clear();
        for (int i = this.mTasks.size() - 1; i >= start; i--) {
            TaskRecord task = this.mTasks.get(i);
            if (task.mAffiliatedTaskId == affiliateId) {
                this.mTasks.remove(i);
                this.mTmpRecents.add(task);
            }
        }
        Collections.sort(this.mTmpRecents, TASK_ID_COMPARATOR);
        TaskRecord first = this.mTmpRecents.get(0);
        first.inRecents = true;
        if (first.mNextAffiliate != null) {
            Slog.w("ActivityTaskManager", "Link error 1 first.next=" + first.mNextAffiliate);
            first.setNextAffiliate(null);
            notifyTaskPersisterLocked(first, false);
        }
        int tmpSize = this.mTmpRecents.size();
        for (int i2 = 0; i2 < tmpSize - 1; i2++) {
            TaskRecord next = this.mTmpRecents.get(i2);
            TaskRecord prev = this.mTmpRecents.get(i2 + 1);
            if (next.mPrevAffiliate != prev) {
                Slog.w("ActivityTaskManager", "Link error 2 next=" + next + " prev=" + next.mPrevAffiliate + " setting prev=" + prev);
                next.setPrevAffiliate(prev);
                notifyTaskPersisterLocked(next, false);
            }
            if (prev.mNextAffiliate != next) {
                Slog.w("ActivityTaskManager", "Link error 3 prev=" + prev + " next=" + prev.mNextAffiliate + " setting next=" + next);
                prev.setNextAffiliate(next);
                notifyTaskPersisterLocked(prev, false);
            }
            prev.inRecents = true;
        }
        TaskRecord last = this.mTmpRecents.get(tmpSize - 1);
        if (last.mPrevAffiliate != null) {
            Slog.w("ActivityTaskManager", "Link error 4 last.prev=" + last.mPrevAffiliate);
            last.setPrevAffiliate(null);
            notifyTaskPersisterLocked(last, false);
        }
        this.mTasks.addAll(start, this.mTmpRecents);
        this.mTmpRecents.clear();
        return start + tmpSize;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0185, code lost:
        android.util.Slog.wtf("ActivityTaskManager", "Bad chain @" + r8 + ": middle task " + r14 + " @" + r8 + " has bad next affiliate " + r14.mNextAffiliate + " id " + r14.mNextAffiliateTaskId + ", expected " + r11);
        r6 = false;
     */
    private boolean moveAffiliatedTasksToFront(TaskRecord task, int taskIndex) {
        int recentsCount = this.mTasks.size();
        TaskRecord top = task;
        int topIndex = taskIndex;
        while (top.mNextAffiliate != null && topIndex > 0) {
            top = top.mNextAffiliate;
            topIndex--;
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
            Slog.d("ActivityTaskManager", "addRecent: adding affilliates starting at " + topIndex + " from intial " + taskIndex);
        }
        boolean sane = top.mAffiliatedTaskId == task.mAffiliatedTaskId;
        int endIndex = topIndex;
        TaskRecord prev = top;
        while (true) {
            if (endIndex >= recentsCount) {
                break;
            }
            TaskRecord cur = this.mTasks.get(endIndex);
            if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                Slog.d("ActivityTaskManager", "addRecent: looking at next chain @" + endIndex + " " + cur);
            }
            if (cur == top) {
                if (!(cur.mNextAffiliate == null && cur.mNextAffiliateTaskId == -1)) {
                    break;
                }
            } else if (cur.mNextAffiliate == prev) {
                if (cur.mNextAffiliateTaskId != prev.taskId) {
                    break;
                }
            } else {
                break;
            }
            if (cur.mPrevAffiliateTaskId == -1) {
                if (cur.mPrevAffiliate != null) {
                    Slog.wtf("ActivityTaskManager", "Bad chain @" + endIndex + ": last task " + cur + " has previous affiliate " + cur.mPrevAffiliate);
                    sane = false;
                }
                if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                    Slog.d("ActivityTaskManager", "addRecent: end of chain @" + endIndex);
                }
            } else if (cur.mPrevAffiliate == null) {
                Slog.wtf("ActivityTaskManager", "Bad chain @" + endIndex + ": task " + cur + " has previous affiliate " + cur.mPrevAffiliate + " but should be id " + cur.mPrevAffiliate);
                sane = false;
                break;
            } else if (cur.mAffiliatedTaskId != task.mAffiliatedTaskId) {
                Slog.wtf("ActivityTaskManager", "Bad chain @" + endIndex + ": task " + cur + " has affiliated id " + cur.mAffiliatedTaskId + " but should be " + task.mAffiliatedTaskId);
                sane = false;
                break;
            } else {
                prev = cur;
                endIndex++;
                if (endIndex >= recentsCount) {
                    Slog.wtf("ActivityTaskManager", "Bad chain ran off index " + endIndex + ": last task " + prev);
                    sane = false;
                    break;
                }
                top = top;
            }
        }
        Slog.wtf("ActivityTaskManager", "Bad chain @" + endIndex + ": first task has next affiliate: " + prev);
        sane = false;
        if (sane && endIndex < taskIndex) {
            Slog.wtf("ActivityTaskManager", "Bad chain @" + endIndex + ": did not extend to task " + task + " @" + taskIndex);
            sane = false;
        }
        if (!sane) {
            return false;
        }
        for (int i = topIndex; i <= endIndex; i++) {
            if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                Slog.d("ActivityTaskManager", "addRecent: moving affiliated " + task + " from " + i + " to " + (i - topIndex));
            }
            this.mTasks.add(i - topIndex, this.mTasks.remove(i));
        }
        if (!ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
            return true;
        }
        Slog.d("ActivityTaskManager", "addRecent: done moving tasks  " + topIndex + " to " + endIndex);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, boolean dumpAll, String dumpPackage) {
        pw.println("ACTIVITY MANAGER RECENT TASKS (dumpsys activity recents)");
        pw.println("mRecentsUid=" + this.mRecentsUid);
        pw.println("mRecentsComponent=" + this.mRecentsComponent);
        pw.println("mFreezeTaskListReordering=" + this.mFreezeTaskListReordering);
        pw.println("mFreezeTaskListReorderingPendingTimeout=" + this.mService.mH.hasCallbacks(this.mResetFreezeTaskListOnTimeoutRunnable));
        if (!this.mTasks.isEmpty()) {
            boolean printedHeader = false;
            int size = this.mTasks.size();
            boolean printedAnything = false;
            for (int i = 0; i < size; i++) {
                TaskRecord tr = this.mTasks.get(i);
                if (dumpPackage == null || (tr.realActivity != null && dumpPackage.equals(tr.realActivity.getPackageName()))) {
                    if (!printedHeader) {
                        pw.println("  Recent tasks:");
                        printedHeader = true;
                        printedAnything = true;
                    }
                    pw.print("  * Recent #");
                    pw.print(i);
                    pw.print(": ");
                    pw.println(tr);
                    if (dumpAll) {
                        tr.dump(pw, "    ");
                    }
                }
            }
            if (this.mHasVisibleRecentTasks) {
                boolean printedHeader2 = false;
                ArrayList<ActivityManager.RecentTaskInfo> tasks = getRecentTasksImpl(Integer.MAX_VALUE, 0, true, false, this.mService.getCurrentUserId(), 1000);
                for (int i2 = 0; i2 < tasks.size(); i2++) {
                    ActivityManager.RecentTaskInfo taskInfo = tasks.get(i2);
                    if (!printedHeader2) {
                        if (printedAnything) {
                            pw.println();
                        }
                        pw.println("  Visible recent tasks (most recent first):");
                        printedHeader2 = true;
                        printedAnything = true;
                    }
                    pw.print("  * RecentTaskInfo #");
                    pw.print(i2);
                    pw.print(": ");
                    taskInfo.dump(pw, "    ");
                }
            }
            if (!printedAnything) {
                pw.println("  (nothing)");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.RecentTaskInfo createRecentTaskInfo(TaskRecord tr) {
        ActivityManager.RecentTaskInfo rti = new ActivityManager.RecentTaskInfo();
        tr.fillTaskInfo(rti);
        rti.id = rti.isRunning ? rti.taskId : -1;
        rti.persistentId = rti.taskId;
        return rti;
    }

    private boolean hasCompatibleActivityTypeAndWindowingMode(TaskRecord t1, TaskRecord t2) {
        int activityType = t1.getActivityType();
        int windowingMode = t1.getWindowingMode();
        boolean isUndefinedType = activityType == 0;
        boolean isUndefinedMode = windowingMode == 0;
        int otherActivityType = t2.getActivityType();
        int otherWindowingMode = t2.getWindowingMode();
        boolean isOtherUndefinedType = otherActivityType == 0;
        boolean isOtherUndefinedMode = otherWindowingMode == 0;
        boolean isCompatibleType = activityType == otherActivityType || isUndefinedType || isOtherUndefinedType;
        boolean isCompatibleMode = windowingMode == otherWindowingMode || isUndefinedMode || isOtherUndefinedMode;
        if (isCompatibleType) {
            if (isCompatibleMode) {
                return true;
            }
            if (ActivityTaskManagerService.IS_HW_MULTIWINDOW_SUPPORTED && ((t1.getBaseIntent() == null || (t1.getBaseIntent().getFlags() & 8388608) != 8388608) && windowingMode != 2)) {
                return true;
            }
        }
        return false;
    }
}
