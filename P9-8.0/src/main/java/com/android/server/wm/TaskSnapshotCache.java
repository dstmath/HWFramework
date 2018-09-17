package com.android.server.wm;

import android.app.ActivityManager.TaskSnapshot;
import android.util.ArrayMap;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;

class TaskSnapshotCache {
    private final ArrayMap<AppWindowToken, Integer> mAppTaskMap = new ArrayMap();
    private final TaskSnapshotLoader mLoader;
    private final ArrayMap<Integer, CacheEntry> mRunningCache = new ArrayMap();
    private final WindowManagerService mService;
    private LinkedList<Integer> mTaskIdList = null;
    private int mTaskIdMaxNum = 0;

    private static final class CacheEntry {
        final TaskSnapshot snapshot;
        final AppWindowToken topApp;

        CacheEntry(TaskSnapshot snapshot, AppWindowToken topApp) {
            this.snapshot = snapshot;
            this.topApp = topApp;
        }
    }

    TaskSnapshotCache(WindowManagerService service, TaskSnapshotLoader loader) {
        this.mService = service;
        this.mLoader = loader;
    }

    TaskSnapshotCache(WindowManagerService service, TaskSnapshotLoader loader, int num) {
        this.mService = service;
        this.mLoader = loader;
        this.mTaskIdMaxNum = num;
        this.mTaskIdList = new LinkedList();
    }

    void putSnapshot(Task task, TaskSnapshot snapshot) {
        CacheEntry entry = (CacheEntry) this.mRunningCache.get(Integer.valueOf(task.mTaskId));
        if (entry != null) {
            this.mAppTaskMap.remove(entry.topApp);
            removemTaskIdList(task.mTaskId);
            this.mTaskIdList.add(Integer.valueOf(task.mTaskId));
        } else {
            if (this.mTaskIdList.size() >= this.mTaskIdMaxNum) {
                removeRunningEntry(((Integer) this.mTaskIdList.poll()).intValue());
            }
            this.mTaskIdList.add(Integer.valueOf(task.mTaskId));
        }
        this.mAppTaskMap.put((AppWindowToken) task.getTopChild(), Integer.valueOf(task.mTaskId));
        this.mRunningCache.put(Integer.valueOf(task.mTaskId), new CacheEntry(snapshot, (AppWindowToken) task.getTopChild()));
    }

    /* JADX WARNING: Missing block: B:13:0x0035, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:14:0x0038, code:
            if (r8 != false) goto L_0x0041;
     */
    /* JADX WARNING: Missing block: B:15:0x003a, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:19:0x0045, code:
            return tryRestoreFromDisk(r6, r7, r9);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    TaskSnapshot getSnapshot(int taskId, int userId, boolean restoreFromDisk, boolean reducedResolution) {
        TaskSnapshot taskSnapshot;
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                CacheEntry entry = (CacheEntry) this.mRunningCache.get(Integer.valueOf(taskId));
                if (entry == null || entry.snapshot.getSnapshot() == null || (entry.snapshot.getSnapshot().isDestroyed() ^ 1) == 0) {
                } else {
                    taskSnapshot = entry.snapshot;
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return taskSnapshot;
    }

    private TaskSnapshot tryRestoreFromDisk(int taskId, int userId, boolean reducedResolution) {
        TaskSnapshot snapshot = this.mLoader.loadTask(taskId, userId, reducedResolution);
        if (snapshot == null) {
            return null;
        }
        return snapshot;
    }

    void onAppRemoved(AppWindowToken wtoken) {
        Integer taskId = (Integer) this.mAppTaskMap.get(wtoken);
        if (taskId != null) {
            removemTaskIdList(taskId.intValue());
            removeRunningEntry(taskId.intValue());
        }
    }

    void onAppDied(AppWindowToken wtoken) {
        Integer taskId = (Integer) this.mAppTaskMap.get(wtoken);
        if (taskId != null) {
            removemTaskIdList(taskId.intValue());
            removeRunningEntry(taskId.intValue());
        }
    }

    void onTaskRemoved(int taskId) {
        removemTaskIdList(taskId);
        removeRunningEntry(taskId);
    }

    private void removemTaskIdList(int taskId) {
        Iterator<Integer> it = this.mTaskIdList.iterator();
        while (it.hasNext()) {
            if (taskId == ((Integer) it.next()).intValue()) {
                it.remove();
                return;
            }
        }
    }

    private void removeRunningEntry(int taskId) {
        CacheEntry entry = (CacheEntry) this.mRunningCache.get(Integer.valueOf(taskId));
        if (entry != null) {
            this.mAppTaskMap.remove(entry.topApp);
            this.mRunningCache.remove(Integer.valueOf(taskId));
        }
    }

    public int releaseSnapshots(int memLevel) {
        int reservedSize = (this.mTaskIdMaxNum + 1) / 2;
        if (memLevel != 0) {
            reservedSize = 1;
        }
        int result = 0;
        while (this.mTaskIdList.size() > reservedSize) {
            removeRunningEntry(((Integer) this.mTaskIdList.poll()).intValue());
            result++;
        }
        return result;
    }

    void dump(PrintWriter pw, String prefix) {
        String doublePrefix = prefix + "  ";
        String triplePrefix = doublePrefix + "  ";
        pw.println(prefix + "SnapshotCache");
        for (int i = this.mRunningCache.size() - 1; i >= 0; i--) {
            CacheEntry entry = (CacheEntry) this.mRunningCache.valueAt(i);
            pw.println(doublePrefix + "Entry taskId=" + this.mRunningCache.keyAt(i));
            pw.println(triplePrefix + "topApp=" + entry.topApp);
            pw.println(triplePrefix + "snapshot=" + entry.snapshot);
        }
        Iterator<Integer> it = this.mTaskIdList.iterator();
        pw.println(prefix + "mTaskIdList");
        while (it.hasNext()) {
            pw.println(doublePrefix + "Entry taskId= " + ((Integer) it.next()).intValue());
        }
    }
}
