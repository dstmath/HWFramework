package com.android.systemui.shared.recents.model;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.util.Log;
import android.util.SparseBooleanArray;
import com.android.systemui.shared.recents.hwutil.HwRecentsTaskUtils;
import com.android.systemui.shared.recents.model.Task;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RecentsTaskLoadPlan {
    private static final boolean IS_EMUI_LITE = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    private static final boolean IS_NOVA_PERFORMANCE = SystemProperties.getBoolean("ro.config.hw_nova_performance", false);
    private static final int MAX_RECENT_TASK_EMUI = 20;
    private static final int MAX_RECENT_TASK_EMUI_LITE = 15;
    public static final String TAG = "RecentsTaskLoadPlan";
    private final Context mContext;
    private final KeyguardManager mKeyguardManager;
    private List<ActivityManager.RecentTaskInfo> mRawTasks;
    private TaskStack mStack;
    private final SparseBooleanArray mTmpLockedUsers = new SparseBooleanArray();

    public static class Options {
        public boolean loadIcons = true;
        public boolean loadThumbnails = false;
        public int numVisibleTaskThumbnails = 0;
        public int numVisibleTasks = 0;
        public boolean onlyLoadForCache = false;
        public boolean onlyLoadPausedActivities = false;
        public int runningTaskId = -1;
    }

    public static class PreloadOptions {
        public boolean loadTitles = true;
    }

    public static int getMaxRecentTasks() {
        if (isEmuiLite()) {
            return 15;
        }
        return 20;
    }

    public RecentsTaskLoadPlan(Context context) {
        this.mContext = context;
        this.mKeyguardManager = (KeyguardManager) context.getSystemService("keyguard");
    }

    public void preloadPlan(PreloadOptions opts, RecentsTaskLoader loader, int runningTaskId, int currentUserId) {
        int i;
        int taskCount;
        Resources res;
        String str;
        String str2;
        boolean z;
        Drawable drawable;
        PreloadOptions preloadOptions = opts;
        RecentsTaskLoader recentsTaskLoader = loader;
        Map<String, Boolean> map = HwRecentsTaskUtils.refreshToCache(this.mContext);
        HwRecentsTaskUtils.refreshPlayingMusicUidSet();
        Resources res2 = this.mContext.getResources();
        ArrayList<Task> allTasks = new ArrayList<>();
        if (this.mRawTasks == null) {
            this.mRawTasks = ActivityManagerWrapper.getInstance().getRecentTasks(getMaxRecentTasks(), currentUserId);
            Collections.reverse(this.mRawTasks);
        } else {
            int i2 = currentUserId;
        }
        int taskCount2 = this.mRawTasks.size();
        int i3 = 0;
        while (i3 < taskCount2) {
            ActivityManager.RecentTaskInfo t = this.mRawTasks.get(i3);
            int windowingMode = t.configuration.windowConfiguration.getWindowingMode();
            Task.TaskKey taskKey = new Task.TaskKey(t.persistentId, windowingMode, t.baseIntent, t.userId, t.lastActiveTime);
            Task.TaskKey taskKey2 = taskKey;
            boolean isFreeformTask = windowingMode == 5;
            boolean isStackTask = !isFreeformTask || HwRecentsTaskUtils.ENABLED_FREEFORM;
            boolean isLaunchTarget = taskKey2.id == runningTaskId;
            ActivityInfo info = recentsTaskLoader.getAndUpdateActivityInfo(taskKey2);
            if (info == null) {
                res = res2;
                taskCount = taskCount2;
                i = i3;
            } else {
                if (preloadOptions.loadTitles) {
                    str = recentsTaskLoader.getAndUpdateActivityTitle(taskKey2, t.taskDescription);
                } else {
                    str = "";
                }
                String title = str;
                if (preloadOptions.loadTitles) {
                    str2 = recentsTaskLoader.getAndUpdateContentDescription(taskKey2, t.taskDescription);
                } else {
                    str2 = "";
                }
                String titleDescription = str2;
                if (isStackTask) {
                    z = false;
                    drawable = recentsTaskLoader.getAndUpdateActivityIcon(taskKey2, t.taskDescription, false);
                } else {
                    z = false;
                    drawable = null;
                }
                Drawable icon = drawable;
                ThumbnailData thumbnail = recentsTaskLoader.getAndUpdateThumbnail(taskKey2, z, z);
                int activityColor = recentsTaskLoader.getActivityPrimaryColor(t.taskDescription);
                res = res2;
                int backgroundColor = recentsTaskLoader.getActivityBackgroundColor(t.taskDescription);
                boolean isSystemApp = (info == null || (info.applicationInfo.flags & 1) == 0) ? false : true;
                taskCount = taskCount2;
                if (this.mTmpLockedUsers.indexOfKey(t.userId) < 0) {
                    boolean z2 = isFreeformTask;
                    int i4 = windowingMode;
                    this.mTmpLockedUsers.put(t.userId, this.mKeyguardManager.isDeviceLocked(t.userId));
                } else {
                    int i5 = windowingMode;
                }
                boolean isLocked = this.mTmpLockedUsers.get(t.userId);
                String packageName = info != null ? info.packageName : "";
                ActivityInfo activityInfo = info;
                i = i3;
                Task task = new Task(taskKey2, icon, thumbnail, title, titleDescription, activityColor, backgroundColor, isLaunchTarget, isStackTask, isSystemApp, t.supportsSplitScreenMultiWindow, t.taskDescription, t.resizeMode, t.topActivity, isLocked);
                task.setPakcageName(packageName);
                if (!shouldSkipLoadTask(map, t, task)) {
                    allTasks.add(task);
                }
            }
            i3 = i + 1;
            res2 = res;
            taskCount2 = taskCount;
            preloadOptions = opts;
            recentsTaskLoader = loader;
            int i6 = currentUserId;
        }
        int i7 = taskCount2;
        Log.i(TAG, "to show tasks size is " + allTasks.size());
        this.mStack = new TaskStack();
        this.mStack.setTasks((List<Task>) allTasks, false);
    }

    public void executePlan(Options opts, RecentsTaskLoader loader) {
        Resources resources = this.mContext.getResources();
        ArrayList<Task> tasks = this.mStack.getTasks();
        int taskCount = tasks.size();
        int i = 0;
        while (i < taskCount) {
            Task task = tasks.get(i);
            Task.TaskKey taskKey = task.key;
            boolean isRunningTask = task.key.id == opts.runningTaskId;
            boolean isVisibleTask = i >= taskCount - opts.numVisibleTasks;
            boolean isVisibleThumbnail = i >= taskCount - opts.numVisibleTaskThumbnails;
            if (!opts.onlyLoadPausedActivities || !isRunningTask) {
                if (opts.loadIcons && ((isRunningTask || isVisibleTask) && task.icon == null)) {
                    task.icon = loader.getAndUpdateActivityIcon(taskKey, task.taskDescription, true);
                }
                if (opts.loadThumbnails && isVisibleThumbnail) {
                    task.thumbnail = loader.getAndUpdateThumbnail(taskKey, true, true);
                }
            }
            i++;
        }
    }

    public TaskStack getTaskStack() {
        return this.mStack;
    }

    public boolean hasTasks() {
        boolean z = false;
        if (this.mStack == null) {
            return false;
        }
        if (this.mStack.getTaskCount() > 0) {
            z = true;
        }
        return z;
    }

    private boolean shouldSkipLoadTask(Map<String, Boolean> lockMap, ActivityManager.RecentTaskInfo recentInfo, Task task) {
        if (!HwRecentsTaskUtils.willRemovedTask(recentInfo)) {
            return false;
        }
        if (lockMap.get(task.packageName) == null ? false : lockMap.get(task.packageName).booleanValue()) {
            Log.d(TAG, task.packageName + "is locked, so need load it");
            return false;
        } else if (HwRecentsTaskUtils.getPlayingMusicUid(this.mContext, task)) {
            Log.d(TAG, task.packageName + "is music, so need load it");
            return false;
        } else {
            Log.i(TAG, "in removing, will remove task: " + task.packageName);
            return true;
        }
    }

    public static boolean isEmuiLite() {
        return IS_EMUI_LITE || IS_NOVA_PERFORMANCE;
    }
}
