package com.android.server.wm;

import android.app.ActivityManager;
import android.graphics.Rect;
import android.util.ArrayMap;
import com.android.server.HwServiceExFactory;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/* access modifiers changed from: package-private */
public class TaskSnapshotCache {
    private final ArrayMap<AppWindowToken, Integer> mAppTaskMap = new ArrayMap<>();
    public IHwTaskSnapshotCacheEx mHwTaskSnapshotCacheEx = HwServiceExFactory.getHwTaskSnapshotCacheEx();
    private ActivityManager.TaskSnapshot mLastForegroundSnapshot;
    private final TaskSnapshotLoader mLoader;
    private final ArrayMap<Integer, CacheEntry> mRunningCache = new ArrayMap<>();
    private final WindowManagerService mService;
    private final Map<Integer, Rect> mSnapshotsBounds = new HashMap();

    TaskSnapshotCache(WindowManagerService service, TaskSnapshotLoader loader) {
        this.mService = service;
        this.mLoader = loader;
    }

    /* access modifiers changed from: package-private */
    public void putForegroundSnapShot(Task task, ActivityManager.TaskSnapshot snapshot) {
        this.mLastForegroundSnapshot = snapshot;
    }

    /* access modifiers changed from: package-private */
    public void clearForegroundTaskSnapshot() {
        this.mLastForegroundSnapshot = null;
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.TaskSnapshot getLastForegroundSnapshot() {
        return this.mLastForegroundSnapshot;
    }

    /* access modifiers changed from: package-private */
    public void putSnapshot(Task task, ActivityManager.TaskSnapshot snapshot) {
        CacheEntry entry = this.mRunningCache.get(Integer.valueOf(task.mTaskId));
        if (entry != null) {
            this.mAppTaskMap.remove(entry.topApp);
            this.mHwTaskSnapshotCacheEx.removeLruTaskIdList(task.mTaskId);
        }
        this.mAppTaskMap.put((AppWindowToken) task.getTopChild(), Integer.valueOf(task.mTaskId));
        this.mRunningCache.put(Integer.valueOf(task.mTaskId), new CacheEntry(snapshot, (AppWindowToken) task.getTopChild()));
        this.mSnapshotsBounds.put(Integer.valueOf(task.mTaskId), snapshot.getWindowBounds());
        this.mHwTaskSnapshotCacheEx.addLruTaskIdList(task.mTaskId);
        if (this.mHwTaskSnapshotCacheEx.isOverMaxCacheThreshold()) {
            removeRunningEntry(this.mHwTaskSnapshotCacheEx.getLeastRecentTaskId());
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.TaskSnapshot getSnapshot(int taskId, int userId, boolean restoreFromDisk, boolean reducedResolution) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                CacheEntry entry = this.mRunningCache.get(Integer.valueOf(taskId));
                if (entry != null) {
                    return entry.snapshot;
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        if (!restoreFromDisk) {
            return null;
        }
        return tryRestoreFromDisk(taskId, userId, reducedResolution);
    }

    private ActivityManager.TaskSnapshot tryRestoreFromDisk(int taskId, int userId, boolean reducedResolution) {
        ActivityManager.TaskSnapshot snapshot = this.mLoader.loadTask(taskId, userId, reducedResolution);
        if (snapshot == null) {
            return null;
        }
        snapshot.setWindowBounds(this.mSnapshotsBounds.get(Integer.valueOf(taskId)));
        return snapshot;
    }

    /* access modifiers changed from: package-private */
    public void onAppRemoved(AppWindowToken wtoken) {
        Integer taskId = this.mAppTaskMap.get(wtoken);
        if (taskId != null) {
            removeRunningEntry(taskId.intValue());
        }
    }

    /* access modifiers changed from: package-private */
    public void onAppDied(AppWindowToken wtoken) {
        Integer taskId = this.mAppTaskMap.get(wtoken);
        if (taskId != null) {
            removeRunningEntry(taskId.intValue());
        }
    }

    /* access modifiers changed from: package-private */
    public void onTaskRemoved(int taskId) {
        removeRunningEntry(taskId);
        this.mSnapshotsBounds.remove(Integer.valueOf(taskId));
    }

    private void removeRunningEntry(int taskId) {
        CacheEntry entry = this.mRunningCache.get(Integer.valueOf(taskId));
        if (entry != null) {
            this.mAppTaskMap.remove(entry.topApp);
            this.mRunningCache.remove(Integer.valueOf(taskId));
        }
        this.mHwTaskSnapshotCacheEx.removeLruTaskIdList(taskId);
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        String doublePrefix = prefix + "  ";
        String triplePrefix = doublePrefix + "  ";
        pw.println(prefix + "SnapshotCache");
        for (int i = this.mRunningCache.size() + -1; i >= 0; i += -1) {
            CacheEntry entry = this.mRunningCache.valueAt(i);
            pw.println(doublePrefix + "Entry taskId=" + this.mRunningCache.keyAt(i));
            pw.println(triplePrefix + "topApp=" + entry.topApp);
            pw.println(triplePrefix + "snapshot=" + entry.snapshot);
        }
        this.mHwTaskSnapshotCacheEx.dump(pw, prefix);
    }

    /* access modifiers changed from: private */
    public static final class CacheEntry {
        final ActivityManager.TaskSnapshot snapshot;
        final AppWindowToken topApp;

        CacheEntry(ActivityManager.TaskSnapshot snapshot2, AppWindowToken topApp2) {
            this.snapshot = snapshot2;
            this.topApp = topApp2;
        }
    }

    /* access modifiers changed from: package-private */
    public void clearSnapshot() {
        this.mAppTaskMap.clear();
        this.mRunningCache.clear();
    }
}
