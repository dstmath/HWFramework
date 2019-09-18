package com.android.server.am;

import android.app.ActivityManager;
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
import android.graphics.Rect;
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
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.am.TaskRecord;
import com.android.server.pm.DumpState;
import com.android.server.slice.SliceClientPermissions;
import com.google.android.collect.Sets;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RecentTasks {
    private static final int DEFAULT_INITIAL_CAPACITY = 5;
    private static final boolean MOVE_AFFILIATED_TASKS_TO_FRONT = false;
    private static final ActivityInfo NO_ACTIVITY_INFO_TOKEN = new ActivityInfo();
    private static final ApplicationInfo NO_APPLICATION_INFO_TOKEN = new ApplicationInfo();
    private static final String TAG = "ActivityManager";
    private static final String TAG_RECENTS = "ActivityManager";
    private static final String TAG_TASKS = "ActivityManager";
    private static final Comparator<TaskRecord> TASK_ID_COMPARATOR = $$Lambda$RecentTasks$NgzE6eN0wIO1cgLW7RzciPDBTHk.INSTANCE;
    private static final boolean TRIMMED = true;
    private long mActiveTasksSessionDurationMs;
    private final ArrayList<Callbacks> mCallbacks = new ArrayList<>();
    private int mGlobalMaxNumTasks;
    private boolean mHasVisibleRecentTasks;
    private final boolean mIsLite = SystemProperties.getBoolean("ro.build.hw_emui_ultra_lite", false);
    private int mMaxNumVisibleTasks;
    private int mMinNumVisibleTasks;
    private final SparseArray<SparseBooleanArray> mPersistedTaskIds = new SparseArray<>(5);
    private ComponentName mRecentsComponent = null;
    private int mRecentsUid = -1;
    private final ActivityManagerService mService;
    private final TaskPersister mTaskPersister;
    private final ArrayList<TaskRecord> mTasks = new ArrayList<>();
    private final HashMap<ComponentName, ActivityInfo> mTmpAvailActCache = new HashMap<>();
    private final HashMap<String, ApplicationInfo> mTmpAvailAppCache = new HashMap<>();
    private final SparseBooleanArray mTmpQuietProfileUserIds = new SparseBooleanArray();
    private final ArrayList<TaskRecord> mTmpRecents = new ArrayList<>();
    private final TaskRecord.TaskActivitiesReport mTmpReport = new TaskRecord.TaskActivitiesReport();
    private final UserController mUserController;
    private final SparseBooleanArray mUsersWithRecentsLoaded = new SparseBooleanArray(5);

    interface Callbacks {
        void onRecentTaskAdded(TaskRecord taskRecord);

        void onRecentTaskRemoved(TaskRecord taskRecord, boolean z);
    }

    static /* synthetic */ int lambda$static$0(TaskRecord lhs, TaskRecord rhs) {
        return rhs.taskId - lhs.taskId;
    }

    @VisibleForTesting
    RecentTasks(ActivityManagerService service, TaskPersister taskPersister, UserController userController) {
        this.mService = service;
        this.mUserController = userController;
        this.mTaskPersister = taskPersister;
        this.mGlobalMaxNumTasks = ActivityManager.getMaxRecentTasksStatic();
        this.mHasVisibleRecentTasks = true;
    }

    RecentTasks(ActivityManagerService service, ActivityStackSupervisor stackSupervisor) {
        File systemDir = Environment.getDataSystemDirectory();
        Resources res = service.mContext.getResources();
        this.mService = service;
        this.mUserController = service.mUserController;
        this.mTaskPersister = new TaskPersister(systemDir, stackSupervisor, service, this);
        this.mGlobalMaxNumTasks = ActivityManager.getMaxRecentTasksStatic();
        this.mHasVisibleRecentTasks = res.getBoolean(17956982);
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
    public void loadParametersFromResources(Resources res) {
        long j;
        if (ActivityManager.isLowRamDeviceStatic()) {
            this.mMinNumVisibleTasks = res.getInteger(17694818);
            this.mMaxNumVisibleTasks = res.getInteger(17694810);
        } else if (SystemProperties.getBoolean("ro.recents.grid", false)) {
            this.mMinNumVisibleTasks = res.getInteger(17694817);
            this.mMaxNumVisibleTasks = res.getInteger(17694809);
        } else {
            this.mMinNumVisibleTasks = res.getInteger(17694816);
            this.mMaxNumVisibleTasks = res.getInteger(17694808);
        }
        int sessionDurationHrs = res.getInteger(17694728);
        if (sessionDurationHrs > 0) {
            j = TimeUnit.HOURS.toMillis((long) sessionDurationHrs);
        } else {
            j = -1;
        }
        this.mActiveTasksSessionDurationMs = j;
    }

    /* access modifiers changed from: package-private */
    public void loadRecentsComponent(Resources res) {
        String rawRecentsComponent = res.getString(17039840);
        if (!TextUtils.isEmpty(rawRecentsComponent)) {
            ComponentName cn = ComponentName.unflattenFromString(rawRecentsComponent);
            if (cn != null) {
                try {
                    ApplicationInfo appInfo = AppGlobals.getPackageManager().getApplicationInfo(cn.getPackageName(), 0, this.mService.mContext.getUserId());
                    if (appInfo != null) {
                        this.mRecentsUid = appInfo.uid;
                        this.mRecentsComponent = cn;
                    }
                } catch (RemoteException e) {
                    Slog.w(ActivityManagerService.TAG, "Could not load application info for recents component: " + cn);
                }
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

    private void notifyTaskRemoved(TaskRecord task, boolean wasTrimmed) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).onRecentTaskRemoved(task, wasTrimmed);
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
            Slog.i(ActivityManagerService.TAG, "Loading recents for user " + userId + " into memory.");
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
            Slog.i(ActivityManagerService.TAG, "Loaded persisted task ids for user " + userId);
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
                    Slog.wtf(ActivityManagerService.TAG, "No task ids found for userId " + task.userId + ". task=" + task + " mPersistedTaskIds=" + this.mPersistedTaskIds);
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
            this.mTaskPersister.startPersisting();
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

    /* access modifiers changed from: package-private */
    public void flush() {
        if (!this.mIsLite) {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    syncPersistentTaskIdsLocked();
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
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
            Slog.i(ActivityManagerService.TAG, "Unloading recents for user " + userId + " from memory.");
            this.mUsersWithRecentsLoaded.delete(userId);
            removeTasksForUserLocked(userId);
        }
        this.mPersistedTaskIds.delete(userId);
        this.mTaskPersister.unloadUserDataFromMemory(userId);
    }

    private void removeTasksForUserLocked(int userId) {
        if (userId <= 0) {
            Slog.i(ActivityManagerService.TAG, "Can't remove recent task on user " + userId);
            return;
        }
        for (int i = this.mTasks.size() - 1; i >= 0; i--) {
            if (this.mTasks.get(i).userId == userId) {
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.i(ActivityManagerService.TAG, "remove RecentTask " + tr + " when finishing user" + userId);
                }
                remove(this.mTasks.get(i));
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
                    this.mService.mStackSupervisor.removeTaskByIdLocked(tr.taskId, false, true, "suspended-package");
                }
                notifyTaskPersisterLocked(tr, false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onLockTaskModeStateChanged(int lockTaskModeState, int userId) {
        if (lockTaskModeState == 1) {
            int i = this.mTasks.size() - 1;
            while (true) {
                int i2 = i;
                if (i2 >= 0) {
                    TaskRecord tr = this.mTasks.get(i2);
                    if (tr.userId == userId && !this.mService.getLockTaskController().isTaskWhitelisted(tr)) {
                        remove(tr);
                    }
                    i = i2 - 1;
                } else {
                    return;
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
                this.mService.mStackSupervisor.removeTaskByIdLocked(tr.taskId, true, true, "remove-package-task");
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
                    this.mService.mStackSupervisor.removeTaskByIdLocked(tr.taskId, false, true, "disabled-package");
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
                        this.mTasks.remove(i);
                        notifyTaskRemoved(task, false);
                        Slog.w(ActivityManagerService.TAG, "Removing auto-remove without activity: " + task);
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
                                this.mTasks.remove(i);
                                notifyTaskRemoved(task, false);
                                Slog.w(ActivityManagerService.TAG, "Removing no longer valid recent: " + task);
                            } else {
                                if (ActivityManagerDebugConfig.DEBUG_RECENTS && task.isAvailable) {
                                    Slog.d(ActivityManagerService.TAG, "Making recent unavailable: " + task);
                                }
                                task.isAvailable = false;
                            }
                        } else if (!ai.enabled || !ai.applicationInfo.enabled || (ai.applicationInfo.flags & DumpState.DUMP_VOLUMES) == 0) {
                            if (ActivityManagerDebugConfig.DEBUG_RECENTS && task.isAvailable) {
                                Slog.d(ActivityManagerService.TAG, "Making recent unavailable: " + task + " (enabled=" + ai.enabled + SliceClientPermissions.SliceAuthority.DELIMITER + ai.applicationInfo.enabled + " flags=" + Integer.toHexString(ai.applicationInfo.flags) + ")");
                            }
                            task.isAvailable = false;
                        } else {
                            if (ActivityManagerDebugConfig.DEBUG_RECENTS && !task.isAvailable) {
                                Slog.d(ActivityManagerService.TAG, "Making recent available: " + task);
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
        ArrayList<IBinder> list = new ArrayList<>();
        int size = this.mTasks.size();
        for (int i = 0; i < size; i++) {
            TaskRecord tr = this.mTasks.get(i);
            if (tr.effectiveUid == callingUid) {
                Intent intent = tr.getBaseIntent();
                if (intent != null && callingPackage.equals(intent.getComponent().getPackageName())) {
                    list.add(new AppTaskImpl(this.mService, createRecentTaskInfo(tr).persistentId, callingUid).asBinder());
                }
            }
        }
        return list;
    }

    /* access modifiers changed from: package-private */
    public ParceledListSlice<ActivityManager.RecentTaskInfo> getRecentTasks(int maxNum, int flags, boolean getTasksAllowed, boolean getDetailedTasks, int userId, int callingUid) {
        RecentTasks recentTasks = this;
        int i = userId;
        int i2 = 0;
        boolean withExcluded = (flags & 1) != 0;
        if (!recentTasks.mService.isUserRunning(i, 4)) {
            Slog.i(ActivityManagerService.TAG, "user " + i + " is still locked. Cannot load recents");
            return ParceledListSlice.emptyList();
        }
        recentTasks.loadUserRecentsLocked(i);
        Set<Integer> includedUsers = recentTasks.mUserController.getProfileIds(i);
        includedUsers.add(Integer.valueOf(userId));
        ArrayList<ActivityManager.RecentTaskInfo> res = new ArrayList<>();
        int size = recentTasks.mTasks.size();
        int numVisibleTasks = 0;
        while (i2 < size) {
            TaskRecord tr = recentTasks.mTasks.get(i2);
            if (recentTasks.isVisibleRecentTask(tr)) {
                numVisibleTasks++;
                if (recentTasks.isInVisibleRange(tr, numVisibleTasks)) {
                    if (res.size() < maxNum) {
                        if (!includedUsers.contains(Integer.valueOf(tr.userId))) {
                            if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                                Slog.d(ActivityManagerService.TAG, "Skipping, not user: " + tr);
                            }
                        } else if (tr.realActivitySuspended) {
                            if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                                Slog.d(ActivityManagerService.TAG, "Skipping, activity suspended: " + tr);
                            }
                        } else if (withExcluded || tr.intent == null || (tr.intent.getFlags() & DumpState.DUMP_VOLUMES) == 0) {
                            if (getTasksAllowed || tr.isActivityTypeHome()) {
                                int i3 = callingUid;
                            } else if (tr.effectiveUid != callingUid) {
                                if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                                    Slog.d(ActivityManagerService.TAG, "Skipping, not allowed: " + tr);
                                }
                                i2++;
                                recentTasks = this;
                            }
                            if (tr.autoRemoveRecents && tr.getTopActivity() == null) {
                                if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                                    Slog.d(ActivityManagerService.TAG, "Skipping, auto-remove without activity: " + tr);
                                }
                                i2++;
                                recentTasks = this;
                            } else if ((flags & 2) == 0 || tr.isAvailable) {
                                if (tr.mUserSetupComplete) {
                                    ActivityManager.RecentTaskInfo rti = recentTasks.createRecentTaskInfo(tr);
                                    if (!getDetailedTasks) {
                                        rti.baseIntent.replaceExtras(null);
                                    }
                                    res.add(rti);
                                } else if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                                    Slog.d(ActivityManagerService.TAG, "Skipping, user setup not complete: " + tr);
                                }
                                i2++;
                                recentTasks = this;
                            } else {
                                if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                                    Slog.d(ActivityManagerService.TAG, "Skipping, unavail real act: " + tr);
                                }
                                i2++;
                                recentTasks = this;
                            }
                        }
                    }
                    int i4 = callingUid;
                    i2++;
                    recentTasks = this;
                }
            }
            int i5 = maxNum;
            int i42 = callingUid;
            i2++;
            recentTasks = this;
        }
        int i6 = maxNum;
        int i7 = callingUid;
        return new ParceledListSlice<>(res);
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
                if (isInVisibleRange(tr, numVisibleTasks)) {
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
        if (ActivityManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
            Slog.d(ActivityManagerService.TAG, "add: task=" + task);
        }
        if (task.mStack == null || !HwPCUtils.isExtDynamicStack(task.mStack.mStackId)) {
            boolean isAffiliated = (task.mAffiliatedTaskId == task.taskId && task.mNextAffiliateTaskId == -1 && task.mPrevAffiliateTaskId == -1) ? false : true;
            int recentsCount = this.mTasks.size();
            if (task.voiceSession != null) {
                if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                    Slog.d(ActivityManagerService.TAG, "addRecent: not adding voice interaction " + task);
                }
            } else if (!isAffiliated && recentsCount > 0 && this.mTasks.get(0) == task) {
                if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                    Slog.d(ActivityManagerService.TAG, "addRecent: already at top: " + task);
                }
            } else if (!isAffiliated || recentsCount <= 0 || !task.inRecents || task.mAffiliatedTaskId != this.mTasks.get(0).mAffiliatedTaskId) {
                boolean needAffiliationFix = false;
                if (task.inRecents) {
                    int taskIndex2 = this.mTasks.indexOf(task);
                    if (taskIndex2 >= 0) {
                        this.mTasks.remove(taskIndex2);
                        this.mTasks.add(0, task);
                        notifyTaskPersisterLocked(task, false);
                        if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                            Slog.d(ActivityManagerService.TAG, "addRecent: moving to top " + task + " from " + taskIndex2);
                        }
                        return;
                    }
                    Slog.wtf(ActivityManagerService.TAG, "Task with inRecent not in recents: " + task);
                    needAffiliationFix = true;
                }
                if (ActivityManagerDebugConfig.DEBUG_RECENTS != 0) {
                    Slog.d(ActivityManagerService.TAG, "addRecent: trimming tasks for " + task);
                }
                removeForAddTask(task);
                task.inRecents = true;
                if (!isAffiliated || needAffiliationFix) {
                    this.mTasks.add(0, task);
                    notifyTaskAdded(task);
                    if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                        Slog.d(ActivityManagerService.TAG, "addRecent: adding " + task);
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
                            if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                                Slog.d(ActivityManagerService.TAG, "addRecent: new affiliated task added at " + taskIndex + ": " + task);
                            }
                            this.mTasks.add(taskIndex, task);
                            notifyTaskAdded(task);
                            if (!moveAffiliatedTasksToFront(task, taskIndex)) {
                                needAffiliationFix = true;
                            } else {
                                return;
                            }
                        } else {
                            if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                                Slog.d(ActivityManagerService.TAG, "addRecent: couldn't find other affiliation " + other);
                            }
                            needAffiliationFix = true;
                        }
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                            Slog.d(ActivityManagerService.TAG, "addRecent: adding affiliated task without next/prev:" + task);
                        }
                        needAffiliationFix = true;
                    }
                }
                if (needAffiliationFix) {
                    if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                        Slog.d(ActivityManagerService.TAG, "addRecent: regrouping affiliations");
                    }
                    cleanupLocked(task.userId);
                }
                trimInactiveRecentTasks();
            } else {
                if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                    Slog.d(ActivityManagerService.TAG, "addRecent: affiliated " + this.mTasks.get(0) + " at top when adding " + task);
                }
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
        this.mTasks.remove(task);
        notifyTaskRemoved(task, false);
    }

    private void trimInactiveRecentTasks() {
        int recentsCount = this.mTasks.size();
        while (recentsCount > this.mGlobalMaxNumTasks) {
            notifyTaskRemoved(this.mTasks.remove(recentsCount - 1), true);
            recentsCount--;
            if (ActivityManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
                Slog.d(ActivityManagerService.TAG, "Trimming over max-recents task=" + tr + " max=" + this.mGlobalMaxNumTasks);
            }
        }
        int[] profileUserIds = this.mUserController.getCurrentProfileIds();
        this.mTmpQuietProfileUserIds.clear();
        for (int userId : profileUserIds) {
            UserInfo userInfo = this.mUserController.getUserInfo(userId);
            if (userInfo != null && userInfo.isManagedProfile() && userInfo.isQuietModeEnabled()) {
                this.mTmpQuietProfileUserIds.put(userId, true);
            }
            if (ActivityManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
                Slog.d(ActivityManagerService.TAG, "User: " + userInfo + " quiet=" + this.mTmpQuietProfileUserIds.get(userId));
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
                    if (isInVisibleRange(task, numVisibleTasks) || !isTrimmable(task)) {
                        i++;
                    } else if (ActivityManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
                        Slog.d(ActivityManagerService.TAG, "Trimming out-of-range visible task=" + task);
                    }
                }
            } else if (ActivityManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
                Slog.d(ActivityManagerService.TAG, "Trimming inactive task=" + task);
            }
            this.mTasks.remove(task);
            notifyTaskRemoved(task, true);
            notifyTaskPersisterLocked(task, false);
        }
    }

    private boolean isActiveRecentTask(TaskRecord task, SparseBooleanArray quietProfileUserIds) {
        if (ActivityManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
            Slog.d(ActivityManagerService.TAG, "isActiveRecentTask: task=" + task + " globalMax=" + this.mGlobalMaxNumTasks);
        }
        if (quietProfileUserIds.get(task.userId)) {
            if (ActivityManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
                Slog.d(ActivityManagerService.TAG, "\tisQuietProfileTask=true");
            }
            return false;
        }
        if (!(task.mAffiliatedTaskId == -1 || task.mAffiliatedTaskId == task.taskId)) {
            TaskRecord affiliatedTask = getTask(task.mAffiliatedTaskId);
            if (affiliatedTask != null && !isActiveRecentTask(affiliatedTask, quietProfileUserIds)) {
                if (ActivityManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
                    Slog.d(ActivityManagerService.TAG, "\taffiliatedWithTask=" + affiliatedTask + " is not active");
                }
                return false;
            }
        }
        return true;
    }

    private boolean isVisibleRecentTask(TaskRecord task) {
        if (ActivityManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
            Slog.d(ActivityManagerService.TAG, "isVisibleRecentTask: task=" + task + " minVis=" + this.mMinNumVisibleTasks + " maxVis=" + this.mMaxNumVisibleTasks + " sessionDuration=" + this.mActiveTasksSessionDurationMs + " inactiveDuration=" + task.getInactiveDuration() + " activityType=" + task.getActivityType() + " windowingMode=" + task.getWindowingMode() + " intentFlags=" + task.getBaseIntent().getFlags());
        }
        switch (task.getActivityType()) {
            case 2:
            case 3:
                return false;
            case 4:
                if ((task.getBaseIntent().getFlags() & DumpState.DUMP_VOLUMES) == 8388608) {
                    return false;
                }
                break;
        }
        switch (task.getWindowingMode()) {
            case 2:
                return false;
            case 3:
                if (ActivityManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
                    Slog.d(ActivityManagerService.TAG, "\ttop=" + task.getStack().topTask());
                }
                ActivityStack stack = task.getStack();
                if (stack != null && stack.topTask() == task) {
                    return false;
                }
        }
        if (task == this.mService.getLockTaskController().getRootTask()) {
            return false;
        }
        return true;
    }

    private boolean isInVisibleRange(TaskRecord task, int numVisibleTasks) {
        boolean z = false;
        if ((task.getBaseIntent().getFlags() & DumpState.DUMP_VOLUMES) == 8388608) {
            if (!((task.getBaseIntent().getHwFlags() & 16384) == 16384)) {
                if (ActivityManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
                    Slog.d(ActivityManagerService.TAG, "\texcludeFromRecents=true");
                }
                if (numVisibleTasks == 1) {
                    z = true;
                }
                return z;
            }
        }
        if (this.mMinNumVisibleTasks >= 0 && numVisibleTasks <= this.mMinNumVisibleTasks) {
            return true;
        }
        if (this.mMaxNumVisibleTasks < 0) {
            return this.mActiveTasksSessionDurationMs > 0 && task.getInactiveDuration() <= this.mActiveTasksSessionDurationMs;
        }
        if (numVisibleTasks <= this.mMaxNumVisibleTasks) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public boolean isTrimmable(TaskRecord task) {
        ActivityStack stack = task.getStack();
        ActivityStack homeStack = this.mService.mStackSupervisor.mHomeStack;
        boolean z = true;
        if (stack == null) {
            return true;
        }
        if (stack.getDisplay() != homeStack.getDisplay()) {
            return false;
        }
        ActivityDisplay display = stack.getDisplay();
        if (display.getIndexOf(stack) >= display.getIndexOf(homeStack)) {
            z = false;
        }
        return z;
    }

    private void removeForAddTask(TaskRecord task) {
        int removeIndex = findRemoveIndexForAddTask(task);
        if (removeIndex != -1) {
            TaskRecord removedTask = this.mTasks.remove(removeIndex);
            if (removedTask != task) {
                notifyTaskRemoved(removedTask, false);
                if (ActivityManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS) {
                    Slog.d(ActivityManagerService.TAG, "Trimming task=" + removedTask + " for addition of task=" + task);
                }
            }
            notifyTaskPersisterLocked(removedTask, false);
        }
    }

    private int findRemoveIndexForAddTask(TaskRecord task) {
        TaskRecord taskRecord = task;
        int recentsCount = this.mTasks.size();
        Intent intent = taskRecord.intent;
        boolean z = true;
        boolean document = intent != null && intent.isDocument();
        int maxRecents = taskRecord.maxRecents - 1;
        int i = 0;
        while (i < recentsCount) {
            TaskRecord tr = this.mTasks.get(i);
            if (taskRecord != tr) {
                if (hasCompatibleActivityTypeAndWindowingMode(taskRecord, tr) && taskRecord.userId == tr.userId) {
                    Intent trIntent = tr.intent;
                    boolean sameAffinity = (taskRecord.affinity == null || !taskRecord.affinity.equals(tr.affinity)) ? false : z;
                    boolean sameIntent = (intent == null || !intent.filterEquals(trIntent)) ? false : z;
                    boolean multiTasksAllowed = false;
                    int flags = intent.getFlags();
                    if (!((268959744 & flags) == 0 || (134217728 & flags) == 0)) {
                        multiTasksAllowed = true;
                    }
                    boolean trIsDocument = (trIntent == null || !trIntent.isDocument()) ? false : z;
                    boolean bothDocuments = (!document || !trIsDocument) ? false : z;
                    if (sameAffinity || sameIntent || bothDocuments) {
                        if (bothDocuments) {
                            if (!((taskRecord.realActivity == null || tr.realActivity == null || !taskRecord.realActivity.equals(tr.realActivity)) ? false : true)) {
                                continue;
                            } else if (maxRecents > 0) {
                                maxRecents--;
                                if (sameIntent && !multiTasksAllowed) {
                                }
                            }
                        } else if (!document && !trIsDocument) {
                        }
                    }
                }
                i++;
                z = true;
            }
            return i;
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
            Slog.w(ActivityManagerService.TAG, "Link error 1 first.next=" + first.mNextAffiliate);
            first.setNextAffiliate(null);
            notifyTaskPersisterLocked(first, false);
        }
        int tmpSize = this.mTmpRecents.size();
        for (int i2 = 0; i2 < tmpSize - 1; i2++) {
            TaskRecord next = this.mTmpRecents.get(i2);
            TaskRecord prev = this.mTmpRecents.get(i2 + 1);
            if (next.mPrevAffiliate != prev) {
                Slog.w(ActivityManagerService.TAG, "Link error 2 next=" + next + " prev=" + next.mPrevAffiliate + " setting prev=" + prev);
                next.setPrevAffiliate(prev);
                notifyTaskPersisterLocked(next, false);
            }
            if (prev.mNextAffiliate != next) {
                Slog.w(ActivityManagerService.TAG, "Link error 3 prev=" + prev + " next=" + prev.mNextAffiliate + " setting next=" + next);
                prev.setNextAffiliate(next);
                notifyTaskPersisterLocked(prev, false);
            }
            prev.inRecents = true;
        }
        TaskRecord last = this.mTmpRecents.get(tmpSize - 1);
        if (last.mPrevAffiliate != null) {
            Slog.w(ActivityManagerService.TAG, "Link error 4 last.prev=" + last.mPrevAffiliate);
            last.setPrevAffiliate(null);
            notifyTaskPersisterLocked(last, false);
        }
        this.mTasks.addAll(start, this.mTmpRecents);
        this.mTmpRecents.clear();
        return start + tmpSize;
    }

    private boolean moveAffiliatedTasksToFront(TaskRecord task, int taskIndex) {
        TaskRecord cur;
        int recentsCount = this.mTasks.size();
        TaskRecord top = task;
        int topIndex = taskIndex;
        while (top.mNextAffiliate != null && topIndex > 0) {
            top = top.mNextAffiliate;
            topIndex--;
        }
        if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
            Slog.d(ActivityManagerService.TAG, "addRecent: adding affilliates starting at " + topIndex + " from intial " + taskIndex);
        }
        boolean sane = top.mAffiliatedTaskId == task.mAffiliatedTaskId;
        int endIndex = topIndex;
        TaskRecord prev = top;
        while (true) {
            if (endIndex >= recentsCount) {
                break;
            }
            cur = this.mTasks.get(endIndex);
            if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                Slog.d(ActivityManagerService.TAG, "addRecent: looking at next chain @" + endIndex + " " + cur);
            }
            if (cur == top) {
                if (!(cur.mNextAffiliate == null && cur.mNextAffiliateTaskId == -1)) {
                    Slog.wtf(ActivityManagerService.TAG, "Bad chain @" + endIndex + ": first task has next affiliate: " + prev);
                    sane = false;
                }
            } else if (!(cur.mNextAffiliate == prev && cur.mNextAffiliateTaskId == prev.taskId)) {
                Slog.wtf(ActivityManagerService.TAG, "Bad chain @" + endIndex + ": middle task " + cur + " @" + endIndex + " has bad next affiliate " + cur.mNextAffiliate + " id " + cur.mNextAffiliateTaskId + ", expected " + prev);
                sane = false;
            }
            if (cur.mPrevAffiliateTaskId != -1) {
                if (cur.mPrevAffiliate != null) {
                    if (cur.mAffiliatedTaskId == task.mAffiliatedTaskId) {
                        prev = cur;
                        endIndex++;
                        if (endIndex >= recentsCount) {
                            Slog.wtf(ActivityManagerService.TAG, "Bad chain ran off index " + endIndex + ": last task " + prev);
                            sane = false;
                            break;
                        }
                    } else {
                        Slog.wtf(ActivityManagerService.TAG, "Bad chain @" + endIndex + ": task " + cur + " has affiliated id " + cur.mAffiliatedTaskId + " but should be " + task.mAffiliatedTaskId);
                        sane = false;
                        break;
                    }
                } else {
                    Slog.wtf(ActivityManagerService.TAG, "Bad chain @" + endIndex + ": task " + cur + " has previous affiliate " + cur.mPrevAffiliate + " but should be id " + cur.mPrevAffiliate);
                    sane = false;
                    break;
                }
            } else {
                if (cur.mPrevAffiliate != null) {
                    Slog.wtf(ActivityManagerService.TAG, "Bad chain @" + endIndex + ": last task " + cur + " has previous affiliate " + cur.mPrevAffiliate);
                    sane = false;
                }
                if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                    Slog.d(ActivityManagerService.TAG, "addRecent: end of chain @" + endIndex);
                }
            }
        }
        Slog.wtf(ActivityManagerService.TAG, "Bad chain @" + endIndex + ": middle task " + cur + " @" + endIndex + " has bad next affiliate " + cur.mNextAffiliate + " id " + cur.mNextAffiliateTaskId + ", expected " + prev);
        sane = false;
        if (sane && endIndex < taskIndex) {
            Slog.wtf(ActivityManagerService.TAG, "Bad chain @" + endIndex + ": did not extend to task " + task + " @" + taskIndex);
            sane = false;
        }
        if (!sane) {
            return false;
        }
        for (int i = topIndex; i <= endIndex; i++) {
            if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                Slog.d(ActivityManagerService.TAG, "addRecent: moving affiliated " + task + " from " + i + " to " + (i - topIndex));
            }
            this.mTasks.add(i - topIndex, this.mTasks.remove(i));
        }
        if (ActivityManagerDebugConfig.DEBUG_RECENTS != 0) {
            Slog.d(ActivityManagerService.TAG, "addRecent: done moving tasks  " + topIndex + " to " + endIndex);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, boolean dumpAll, String dumpPackage) {
        pw.println("ACTIVITY MANAGER RECENT TASKS (dumpsys activity recents)");
        pw.println("mRecentsUid=" + this.mRecentsUid);
        pw.println("mRecentsComponent=" + this.mRecentsComponent);
        if (!this.mTasks.isEmpty()) {
            boolean printedAnything = false;
            boolean printedHeader = false;
            int size = this.mTasks.size();
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
            if (!printedAnything) {
                pw.println("  (nothing)");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.RecentTaskInfo createRecentTaskInfo(TaskRecord tr) {
        ActivityManager.RecentTaskInfo rti = new ActivityManager.RecentTaskInfo();
        rti.id = tr.getTopActivity() == null ? -1 : tr.taskId;
        rti.persistentId = tr.taskId;
        rti.baseIntent = new Intent(tr.getBaseIntent());
        rti.origActivity = tr.origActivity;
        rti.realActivity = tr.realActivity;
        rti.description = tr.lastDescription;
        rti.stackId = tr.getStackId();
        rti.userId = tr.userId;
        rti.taskDescription = new ActivityManager.TaskDescription(tr.lastTaskDescription);
        rti.lastActiveTime = tr.lastActiveTime;
        rti.affiliatedTaskId = tr.mAffiliatedTaskId;
        rti.affiliatedTaskColor = tr.mAffiliatedTaskColor;
        rti.numActivities = 0;
        if (!tr.matchParentBounds()) {
            rti.bounds = new Rect(tr.getOverrideBounds());
        }
        rti.supportsSplitScreenMultiWindow = tr.supportsSplitScreenWindowingMode();
        rti.resizeMode = tr.mResizeMode;
        rti.configuration.setTo(tr.getConfiguration());
        tr.getNumRunningActivities(this.mTmpReport);
        rti.numActivities = this.mTmpReport.numActivities;
        ComponentName componentName = null;
        rti.baseActivity = this.mTmpReport.base != null ? this.mTmpReport.base.intent.getComponent() : null;
        if (this.mTmpReport.top != null) {
            componentName = this.mTmpReport.top.intent.getComponent();
        }
        rti.topActivity = componentName;
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
        if (!isCompatibleType || !isCompatibleMode) {
            return false;
        }
        return true;
    }
}
