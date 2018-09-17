package com.android.server.am;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import com.google.android.collect.Sets;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

public class RecentTasks extends ArrayList<TaskRecord> {
    private static final int DEFAULT_INITIAL_CAPACITY = 5;
    private static final int MAX_RECENT_BITMAPS = 3;
    private static final boolean MOVE_AFFILIATED_TASKS_TO_FRONT = false;
    private static final String TAG = "ActivityManager";
    private static final String TAG_RECENTS = (TAG + ActivityManagerDebugConfig.POSTFIX_RECENTS);
    private static final String TAG_TASKS = (TAG + ActivityManagerDebugConfig.POSTFIX_TASKS);
    private static Comparator<TaskRecord> sTaskRecordComparator = new Comparator<TaskRecord>() {
        public int compare(TaskRecord lhs, TaskRecord rhs) {
            return rhs.taskId - lhs.taskId;
        }
    };
    private final boolean mIsLite = SystemProperties.getBoolean("ro.build.hw_emui_ultra_lite", false);
    final SparseArray<SparseBooleanArray> mPersistedTaskIds = new SparseArray(5);
    private final ActivityManagerService mService;
    private final TaskPersister mTaskPersister;
    private final ActivityInfo mTmpActivityInfo = new ActivityInfo();
    private final ApplicationInfo mTmpAppInfo = new ApplicationInfo();
    private final HashMap<ComponentName, ActivityInfo> mTmpAvailActCache = new HashMap();
    private final HashMap<String, ApplicationInfo> mTmpAvailAppCache = new HashMap();
    private final ArrayList<TaskRecord> mTmpRecents = new ArrayList();
    private final SparseBooleanArray mUsersWithRecentsLoaded = new SparseBooleanArray(5);

    RecentTasks(ActivityManagerService service, ActivityStackSupervisor mStackSupervisor) {
        File systemDir = Environment.getDataSystemDirectory();
        this.mService = service;
        this.mTaskPersister = new TaskPersister(systemDir, mStackSupervisor, service, this);
        mStackSupervisor.setRecentTasks(this);
    }

    void loadUserRecentsLocked(int userId) {
        if (!this.mUsersWithRecentsLoaded.get(userId) && !this.mIsLite) {
            loadPersistedTaskIdsForUserLocked(userId);
            SparseBooleanArray preaddedTasks = new SparseBooleanArray();
            for (TaskRecord task : this) {
                if (task.userId == userId && shouldPersistTaskLocked(task)) {
                    preaddedTasks.put(task.taskId, true);
                }
            }
            Slog.i(TAG, "Loading recents for user " + userId + " into memory.");
            addAll(this.mTaskPersister.restoreTasksForUserLocked(userId, preaddedTasks));
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
            Slog.i(TAG, "Loaded persisted task ids for user " + userId);
        }
    }

    boolean taskIdTakenForUserLocked(int taskId, int userId) {
        loadPersistedTaskIdsForUserLocked(userId);
        return ((SparseBooleanArray) this.mPersistedTaskIds.get(userId)).get(taskId);
    }

    void notifyTaskPersisterLocked(TaskRecord task, boolean flush) {
        ActivityStack stack = task != null ? task.getStack() : null;
        if (stack != null && stack.isHomeOrRecentsStack()) {
            return;
        }
        if ((stack == null || !HwPCUtils.isPcDynamicStack(stack.getStackId())) && !this.mIsLite) {
            syncPersistentTaskIdsLocked();
            this.mTaskPersister.wakeup(task, flush);
        }
    }

    private void syncPersistentTaskIdsLocked() {
        int i;
        for (i = this.mPersistedTaskIds.size() - 1; i >= 0; i--) {
            if (this.mUsersWithRecentsLoaded.get(this.mPersistedTaskIds.keyAt(i))) {
                ((SparseBooleanArray) this.mPersistedTaskIds.valueAt(i)).clear();
            }
        }
        for (i = size() - 1; i >= 0; i--) {
            TaskRecord task = (TaskRecord) get(i);
            if (shouldPersistTaskLocked(task)) {
                if (this.mPersistedTaskIds.get(task.userId) == null) {
                    Slog.wtf(TAG, "No task ids found for userId " + task.userId + ". task=" + task + " mPersistedTaskIds=" + this.mPersistedTaskIds);
                    this.mPersistedTaskIds.put(task.userId, new SparseBooleanArray());
                }
                ((SparseBooleanArray) this.mPersistedTaskIds.get(task.userId)).put(task.taskId, true);
            }
        }
    }

    private static boolean shouldPersistTaskLocked(TaskRecord task) {
        ActivityStack<?> stack = task.getStack();
        if (task.isPersistable) {
            return stack != null ? stack.isHomeOrRecentsStack() ^ 1 : true;
        } else {
            return false;
        }
    }

    void onSystemReadyLocked() {
        if (!this.mIsLite) {
            clear();
            this.mTaskPersister.startPersisting();
        }
    }

    Bitmap getTaskDescriptionIcon(String path) {
        return this.mTaskPersister.getTaskDescriptionIcon(path);
    }

    Bitmap getImageFromWriteQueue(String path) {
        return this.mTaskPersister.getImageFromWriteQueue(path);
    }

    void saveImage(Bitmap image, String path) {
        if (!this.mIsLite) {
            this.mTaskPersister.saveImage(image, path);
        }
    }

    void flush() {
        if (!this.mIsLite) {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    syncPersistentTaskIdsLocked();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            this.mTaskPersister.flush();
        }
    }

    int[] usersWithRecentsLoadedLocked() {
        int[] usersWithRecentsLoaded = new int[this.mUsersWithRecentsLoaded.size()];
        int len = 0;
        for (int i = 0; i < usersWithRecentsLoaded.length; i++) {
            int userId = this.mUsersWithRecentsLoaded.keyAt(i);
            if (this.mUsersWithRecentsLoaded.valueAt(i)) {
                int len2 = len + 1;
                usersWithRecentsLoaded[len] = userId;
                len = len2;
            }
        }
        if (len < usersWithRecentsLoaded.length) {
            return Arrays.copyOf(usersWithRecentsLoaded, len);
        }
        return usersWithRecentsLoaded;
    }

    private void unloadUserRecentsLocked(int userId) {
        if (this.mUsersWithRecentsLoaded.get(userId)) {
            Slog.i(TAG, "Unloading recents for user " + userId + " from memory.");
            this.mUsersWithRecentsLoaded.delete(userId);
            removeTasksForUserLocked(userId);
        }
    }

    void unloadUserDataFromMemoryLocked(int userId) {
        unloadUserRecentsLocked(userId);
        this.mPersistedTaskIds.delete(userId);
        this.mTaskPersister.unloadUserDataFromMemory(userId);
    }

    TaskRecord taskForIdLocked(int id) {
        int recentsCount = size();
        for (int i = 0; i < recentsCount; i++) {
            TaskRecord tr = (TaskRecord) get(i);
            if (tr.taskId == id) {
                return tr;
            }
        }
        return null;
    }

    void removeTasksForUserLocked(int userId) {
        if (userId <= 0) {
            Slog.i(TAG, "Can't remove recent task on user " + userId);
            return;
        }
        for (int i = size() - 1; i >= 0; i--) {
            TaskRecord tr = (TaskRecord) get(i);
            if (tr.userId == userId) {
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.i(TAG_TASKS, "remove RecentTask " + tr + " when finishing user" + userId);
                }
                remove(i);
                tr.removedFromRecents();
            }
        }
    }

    void onPackagesSuspendedChanged(String[] packages, boolean suspended, int userId) {
        Set<String> packageNames = Sets.newHashSet(packages);
        for (int i = size() - 1; i >= 0; i--) {
            TaskRecord tr = (TaskRecord) get(i);
            if (tr.realActivity != null && packageNames.contains(tr.realActivity.getPackageName()) && tr.userId == userId && tr.realActivitySuspended != suspended) {
                tr.realActivitySuspended = suspended;
                notifyTaskPersisterLocked(tr, false);
            }
        }
    }

    void cleanupLocked(int userId) {
        int recentsCount = size();
        if (recentsCount != 0) {
            int i;
            IPackageManager pm = AppGlobals.getPackageManager();
            for (i = recentsCount - 1; i >= 0; i--) {
                TaskRecord task = (TaskRecord) get(i);
                if (userId == -1 || task.userId == userId) {
                    if (task.autoRemoveRecents && task.getTopActivity() == null) {
                        remove(i);
                        task.removedFromRecents();
                        Slog.w(TAG, "Removing auto-remove without activity: " + task);
                    } else if (task.realActivity != null) {
                        ActivityInfo ai = (ActivityInfo) this.mTmpAvailActCache.get(task.realActivity);
                        if (ai == null) {
                            try {
                                ai = pm.getActivityInfo(task.realActivity, 268435456, userId);
                                if (ai == null) {
                                    ai = this.mTmpActivityInfo;
                                }
                                this.mTmpAvailActCache.put(task.realActivity, ai);
                            } catch (RemoteException e) {
                            }
                        }
                        if (ai == this.mTmpActivityInfo) {
                            ApplicationInfo app = (ApplicationInfo) this.mTmpAvailAppCache.get(task.realActivity.getPackageName());
                            if (app == null) {
                                try {
                                    app = pm.getApplicationInfo(task.realActivity.getPackageName(), 8192, userId);
                                    if (app == null) {
                                        app = this.mTmpAppInfo;
                                    }
                                    this.mTmpAvailAppCache.put(task.realActivity.getPackageName(), app);
                                } catch (RemoteException e2) {
                                }
                            }
                            if (app == this.mTmpAppInfo || (app.flags & 8388608) == 0) {
                                remove(i);
                                task.removedFromRecents();
                                Slog.w(TAG, "Removing no longer valid recent: " + task);
                            } else {
                                if (ActivityManagerDebugConfig.DEBUG_RECENTS && task.isAvailable) {
                                    Slog.d(TAG_RECENTS, "Making recent unavailable: " + task);
                                }
                                task.isAvailable = false;
                            }
                        } else if (ai.enabled && (ai.applicationInfo.enabled ^ 1) == 0 && (ai.applicationInfo.flags & 8388608) != 0) {
                            if (ActivityManagerDebugConfig.DEBUG_RECENTS && (task.isAvailable ^ 1) != 0) {
                                Slog.d(TAG_RECENTS, "Making recent available: " + task);
                            }
                            task.isAvailable = true;
                        } else {
                            if (ActivityManagerDebugConfig.DEBUG_RECENTS && task.isAvailable) {
                                Slog.d(TAG_RECENTS, "Making recent unavailable: " + task + " (enabled=" + ai.enabled + "/" + ai.applicationInfo.enabled + " flags=" + Integer.toHexString(ai.applicationInfo.flags) + ")");
                            }
                            task.isAvailable = false;
                        }
                    }
                }
            }
            i = 0;
            recentsCount = size();
            while (i < recentsCount) {
                i = processNextAffiliateChainLocked(i);
            }
        }
    }

    private final boolean moveAffiliatedTasksToFront(TaskRecord task, int taskIndex) {
        int recentsCount = size();
        TaskRecord top = task;
        int topIndex = taskIndex;
        while (top.mNextAffiliate != null && topIndex > 0) {
            top = top.mNextAffiliate;
            topIndex--;
        }
        if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
            Slog.d(TAG_RECENTS, "addRecent: adding affilliates starting at " + topIndex + " from intial " + taskIndex);
        }
        boolean sane = top.mAffiliatedTaskId == task.mAffiliatedTaskId;
        int endIndex = topIndex;
        TaskRecord prev = top;
        while (endIndex < recentsCount) {
            TaskRecord cur = (TaskRecord) get(endIndex);
            if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                Slog.d(TAG_RECENTS, "addRecent: looking at next chain @" + endIndex + " " + cur);
            }
            if (cur != top) {
                if (!(cur.mNextAffiliate == prev && cur.mNextAffiliateTaskId == prev.taskId)) {
                    Slog.wtf(TAG, "Bad chain @" + endIndex + ": middle task " + cur + " @" + endIndex + " has bad next affiliate " + cur.mNextAffiliate + " id " + cur.mNextAffiliateTaskId + ", expected " + prev);
                    sane = false;
                    break;
                }
            } else if (!(cur.mNextAffiliate == null && cur.mNextAffiliateTaskId == -1)) {
                Slog.wtf(TAG, "Bad chain @" + endIndex + ": first task has next affiliate: " + prev);
                sane = false;
                break;
            }
            if (cur.mPrevAffiliateTaskId != -1) {
                if (cur.mPrevAffiliate != null) {
                    if (cur.mAffiliatedTaskId == task.mAffiliatedTaskId) {
                        prev = cur;
                        endIndex++;
                        if (endIndex >= recentsCount) {
                            Slog.wtf(TAG, "Bad chain ran off index " + endIndex + ": last task " + cur);
                            sane = false;
                            break;
                        }
                    }
                    Slog.wtf(TAG, "Bad chain @" + endIndex + ": task " + cur + " has affiliated id " + cur.mAffiliatedTaskId + " but should be " + task.mAffiliatedTaskId);
                    sane = false;
                    break;
                }
                Slog.wtf(TAG, "Bad chain @" + endIndex + ": task " + cur + " has previous affiliate " + cur.mPrevAffiliate + " but should be id " + cur.mPrevAffiliate);
                sane = false;
                break;
            }
            if (cur.mPrevAffiliate != null) {
                Slog.wtf(TAG, "Bad chain @" + endIndex + ": last task " + cur + " has previous affiliate " + cur.mPrevAffiliate);
                sane = false;
            }
            if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                Slog.d(TAG_RECENTS, "addRecent: end of chain @" + endIndex);
            }
        }
        if (sane && endIndex < taskIndex) {
            Slog.wtf(TAG, "Bad chain @" + endIndex + ": did not extend to task " + task + " @" + taskIndex);
            sane = false;
        }
        if (!sane) {
            return false;
        }
        for (int i = topIndex; i <= endIndex; i++) {
            if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                Slog.d(TAG_RECENTS, "addRecent: moving affiliated " + task + " from " + i + " to " + (i - topIndex));
            }
            add(i - topIndex, (TaskRecord) remove(i));
        }
        if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
            Slog.d(TAG_RECENTS, "addRecent: done moving tasks  " + topIndex + " to " + endIndex);
        }
        return true;
    }

    final void addLocked(TaskRecord task) {
        if (task.mStack == null || !HwPCUtils.isPcDynamicStack(task.mStack.mStackId)) {
            boolean isAffiliated = (task.mAffiliatedTaskId == task.taskId && task.mNextAffiliateTaskId == -1) ? task.mPrevAffiliateTaskId != -1 : true;
            int recentsCount = size();
            if (task.voiceSession != null) {
                if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                    Slog.d(TAG_RECENTS, "addRecent: not adding voice interaction " + task);
                }
            } else if (!isAffiliated && recentsCount > 0 && get(0) == task) {
                if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                    Slog.d(TAG_RECENTS, "addRecent: already at top: " + task);
                }
            } else if (isAffiliated && recentsCount > 0 && task.inRecents && task.mAffiliatedTaskId == ((TaskRecord) get(0)).mAffiliatedTaskId) {
                if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                    Slog.d(TAG_RECENTS, "addRecent: affiliated " + get(0) + " at top when adding " + task);
                }
            } else {
                int taskIndex;
                boolean needAffiliationFix = false;
                if (task.inRecents) {
                    taskIndex = indexOf(task);
                    if (taskIndex < 0) {
                        Slog.wtf(TAG, "Task with inRecent not in recents: " + task);
                        needAffiliationFix = true;
                    } else if (!isAffiliated) {
                        remove(taskIndex);
                        add(0, task);
                        notifyTaskPersisterLocked(task, false);
                        if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                            Slog.d(TAG_RECENTS, "addRecent: moving to top " + task + " from " + taskIndex);
                        }
                        return;
                    } else if (!moveAffiliatedTasksToFront(task, taskIndex)) {
                        needAffiliationFix = true;
                    } else {
                        return;
                    }
                }
                if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                    Slog.d(TAG_RECENTS, "addRecent: trimming tasks for " + task);
                }
                trimForTaskLocked(task, true);
                int maxRecents = ActivityManager.getMaxRecentTasksStatic();
                for (recentsCount = size(); recentsCount >= maxRecents; recentsCount--) {
                    ((TaskRecord) remove(recentsCount - 1)).removedFromRecents();
                }
                task.inRecents = true;
                if (!isAffiliated || needAffiliationFix) {
                    add(0, task);
                    if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                        Slog.d(TAG_RECENTS, "addRecent: adding " + task);
                    }
                } else if (isAffiliated) {
                    TaskRecord other = task.mNextAffiliate;
                    if (other == null) {
                        other = task.mPrevAffiliate;
                    }
                    if (other != null) {
                        int otherIndex = indexOf(other);
                        if (otherIndex >= 0) {
                            if (other == task.mNextAffiliate) {
                                taskIndex = otherIndex + 1;
                            } else {
                                taskIndex = otherIndex;
                            }
                            if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                                Slog.d(TAG_RECENTS, "addRecent: new affiliated task added at " + taskIndex + ": " + task);
                            }
                            add(taskIndex, task);
                            if (!moveAffiliatedTasksToFront(task, taskIndex)) {
                                needAffiliationFix = true;
                            } else {
                                return;
                            }
                        }
                        if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                            Slog.d(TAG_RECENTS, "addRecent: couldn't find other affiliation " + other);
                        }
                        needAffiliationFix = true;
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                            Slog.d(TAG_RECENTS, "addRecent: adding affiliated task without next/prev:" + task);
                        }
                        needAffiliationFix = true;
                    }
                }
                if (needAffiliationFix) {
                    if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                        Slog.d(TAG_RECENTS, "addRecent: regrouping affiliations");
                    }
                    cleanupLocked(task.userId);
                }
            }
        }
    }

    int trimForTaskLocked(TaskRecord task, boolean doTrim) {
        int recentsCount = size();
        Intent intent = task.intent;
        boolean document = intent != null ? intent.isDocument() : false;
        int maxRecents = task.maxRecents - 1;
        ActivityStack stack = task.getStack();
        int i = 0;
        while (i < recentsCount) {
            TaskRecord tr = (TaskRecord) get(i);
            ActivityStack trStack = tr.getStack();
            if (task != tr) {
                if ((stack == null || trStack == null || stack == trStack) && task.userId == tr.userId) {
                    if (i > 3) {
                        tr.freeLastThumbnail();
                    }
                    Intent trIntent = tr.intent;
                    boolean sameAffinity = task.affinity != null ? task.affinity.equals(tr.affinity) : false;
                    int sameIntentFilter = intent != null ? intent.filterEquals(trIntent) : 0;
                    boolean multiTasksAllowed = false;
                    int flags = intent.getFlags();
                    if (!((268959744 & flags) == 0 || (134217728 & flags) == 0)) {
                        multiTasksAllowed = true;
                    }
                    boolean trIsDocument = trIntent != null ? trIntent.isDocument() : false;
                    int bothDocuments = document ? trIsDocument : 0;
                    boolean taskHasExcludeFlag = (task.intent.getFlags() & 8388608) != 0;
                    if ((sameAffinity && !taskHasExcludeFlag) || (sameIntentFilter ^ 1) == 0 || (bothDocuments ^ 1) == 0) {
                        if (bothDocuments != 0) {
                            boolean sameActivity;
                            if (task.realActivity == null || tr.realActivity == null) {
                                sameActivity = false;
                            } else {
                                sameActivity = task.realActivity.equals(tr.realActivity);
                            }
                            if (!sameActivity) {
                                continue;
                            } else if (maxRecents > 0) {
                                maxRecents--;
                                if (!doTrim) {
                                    continue;
                                } else if ((sameIntentFilter ^ 1) != 0) {
                                    continue;
                                } else if (multiTasksAllowed) {
                                    continue;
                                }
                            }
                        } else if (document) {
                            continue;
                        } else if (trIsDocument) {
                            continue;
                        }
                    }
                }
                i++;
            }
            if (!doTrim) {
                return i;
            }
            tr.disposeThumbnail();
            remove(i);
            if (task != tr) {
                tr.removedFromRecents();
            }
            i--;
            recentsCount--;
            if (task.intent == null) {
                task = tr;
            }
            notifyTaskPersisterLocked(tr, false);
            i++;
        }
        return -1;
    }

    private int processNextAffiliateChainLocked(int start) {
        TaskRecord startTask = (TaskRecord) get(start);
        int affiliateId = startTask.mAffiliatedTaskId;
        if (startTask.taskId == affiliateId && startTask.mPrevAffiliate == null && startTask.mNextAffiliate == null) {
            startTask.inRecents = true;
            return start + 1;
        }
        int i;
        this.mTmpRecents.clear();
        for (i = size() - 1; i >= start; i--) {
            TaskRecord task = (TaskRecord) get(i);
            if (task.mAffiliatedTaskId == affiliateId) {
                remove(i);
                this.mTmpRecents.add(task);
            }
        }
        Collections.sort(this.mTmpRecents, sTaskRecordComparator);
        TaskRecord first = (TaskRecord) this.mTmpRecents.get(0);
        first.inRecents = true;
        if (first.mNextAffiliate != null) {
            Slog.w(TAG, "Link error 1 first.next=" + first.mNextAffiliate);
            first.setNextAffiliate(null);
            notifyTaskPersisterLocked(first, false);
        }
        int tmpSize = this.mTmpRecents.size();
        for (i = 0; i < tmpSize - 1; i++) {
            TaskRecord next = (TaskRecord) this.mTmpRecents.get(i);
            TaskRecord prev = (TaskRecord) this.mTmpRecents.get(i + 1);
            if (next.mPrevAffiliate != prev) {
                Slog.w(TAG, "Link error 2 next=" + next + " prev=" + next.mPrevAffiliate + " setting prev=" + prev);
                next.setPrevAffiliate(prev);
                notifyTaskPersisterLocked(next, false);
            }
            if (prev.mNextAffiliate != next) {
                Slog.w(TAG, "Link error 3 prev=" + prev + " next=" + prev.mNextAffiliate + " setting next=" + next);
                prev.setNextAffiliate(next);
                notifyTaskPersisterLocked(prev, false);
            }
            prev.inRecents = true;
        }
        TaskRecord last = (TaskRecord) this.mTmpRecents.get(tmpSize - 1);
        if (last.mPrevAffiliate != null) {
            Slog.w(TAG, "Link error 4 last.prev=" + last.mPrevAffiliate);
            last.setPrevAffiliate(null);
            notifyTaskPersisterLocked(last, false);
        }
        addAll(start, this.mTmpRecents);
        this.mTmpRecents.clear();
        return start + tmpSize;
    }
}
