package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityManager.StackId;
import android.app.ActivityManager.TaskSnapshot;
import android.graphics.Bitmap;
import android.graphics.GraphicBuffer;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Process;
import android.os.SystemProperties;
import android.util.ArraySet;
import android.util.Slog;
import android.view.DisplayListCanvas;
import android.view.RenderNode;
import android.view.ThreadedRenderer;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy.ScreenOffListener;
import android.view.WindowManagerPolicy.StartingSurface;
import com.android.server.wm.-$Lambda$v2Yn08uofw54W8n_7KsmBjqR0Z8.AnonymousClass1;
import com.android.server.wm.-$Lambda$v2Yn08uofw54W8n_7KsmBjqR0Z8.AnonymousClass2;
import com.google.android.collect.Sets;
import java.io.PrintWriter;

class TaskSnapshotController {
    static final int CONFIG_NUMS = 4;
    static final int SNAPSHOT_MAX_CACHE_NUM;
    static final int SNAPSHOT_MODE_APP_THEME = 1;
    static final int SNAPSHOT_MODE_NONE = 2;
    static final int SNAPSHOT_MODE_REAL = 0;
    private final TaskSnapshotCache mCache;
    private final Handler mHandler = new Handler();
    private final boolean mIsRunningOnTv;
    private final TaskSnapshotLoader mLoader = new TaskSnapshotLoader(this.mPersister);
    private final TaskSnapshotPersister mPersister = new TaskSnapshotPersister(new -$Lambda$v2Yn08uofw54W8n_7KsmBjqR0Z8());
    private final WindowManagerService mService;
    private final ArraySet<Task> mTmpTasks = new ArraySet();

    static {
        int i;
        if (ActivityManager.ENABLE_TASK_SNAPSHOTS) {
            i = 48;
        } else {
            i = parseConfig();
        }
        SNAPSHOT_MAX_CACHE_NUM = i;
    }

    TaskSnapshotController(WindowManagerService service) {
        this.mService = service;
        this.mCache = new TaskSnapshotCache(this.mService, this.mLoader, SNAPSHOT_MAX_CACHE_NUM);
        this.mIsRunningOnTv = this.mService.mContext.getPackageManager().hasSystemFeature("android.software.leanback");
    }

    void systemReady() {
        this.mPersister.start();
    }

    void onTransitionStarting() {
        handleClosingApps(this.mService.mClosingApps);
    }

    void notifyAppVisibilityChanged(AppWindowToken appWindowToken, boolean visible) {
        if (!visible) {
            handleClosingApps(Sets.newArraySet(new AppWindowToken[]{appWindowToken}));
        }
    }

    private void handleClosingApps(ArraySet<AppWindowToken> closingApps) {
        if (!shouldDisableSnapshots()) {
            getClosingTasks(closingApps, this.mTmpTasks);
            snapshotTasks(this.mTmpTasks);
        }
    }

    private void snapshotTasks(ArraySet<Task> tasks) {
        for (int i = tasks.size() - 1; i >= 0; i--) {
            TaskSnapshot snapshot;
            Task task = (Task) tasks.valueAt(i);
            switch (getSnapshotMode(task)) {
                case 0:
                    snapshot = snapshotTask(task);
                    break;
                case 1:
                    snapshot = drawAppThemeSnapshot(task);
                    break;
                case 2:
                    break;
                default:
                    snapshot = null;
                    break;
            }
            if (snapshot != null) {
                this.mCache.putSnapshot(task, snapshot);
                this.mPersister.persistSnapshot(task.mTaskId, task.mUserId, snapshot);
                if (task.getController() != null) {
                    task.getController().reportSnapshotChanged(snapshot);
                }
            }
        }
    }

    TaskSnapshot getSnapshot(int taskId, int userId, boolean restoreFromDisk, boolean reducedResolution) {
        return this.mCache.getSnapshot(taskId, userId, restoreFromDisk, reducedResolution);
    }

    StartingSurface createStartingSurface(AppWindowToken token, TaskSnapshot snapshot) {
        return TaskSnapshotSurface.create(this.mService, token, snapshot);
    }

    private TaskSnapshot snapshotTask(Task task) {
        AppWindowToken top = (AppWindowToken) task.getTopChild();
        if (top == null) {
            return null;
        }
        WindowState mainWindow = top.findMainWindow();
        if (mainWindow == null) {
            return null;
        }
        GraphicBuffer buffer = top.mDisplayContent.screenshotApplicationsToBuffer(top.token, -1, -1, false, 1.0f, false, true);
        if (buffer == null) {
            return null;
        }
        return new TaskSnapshot(buffer, top.getConfiguration().orientation, minRect(mainWindow.mContentInsets, mainWindow.mStableInsets), false, 1.0f);
    }

    private boolean shouldDisableSnapshots() {
        return (SNAPSHOT_MAX_CACHE_NUM <= 0 || ActivityManager.isLowRamDeviceStatic()) ? true : this.mIsRunningOnTv;
    }

    private Rect minRect(Rect rect1, Rect rect2) {
        return new Rect(Math.min(rect1.left, rect2.left), Math.min(rect1.top, rect2.top), Math.min(rect1.right, rect2.right), Math.min(rect1.bottom, rect2.bottom));
    }

    void getClosingTasks(ArraySet<AppWindowToken> closingApps, ArraySet<Task> outClosingTasks) {
        outClosingTasks.clear();
        for (int i = closingApps.size() - 1; i >= 0; i--) {
            Task task = ((AppWindowToken) closingApps.valueAt(i)).getTask();
            if (!(task == null || (task.isVisible() ^ 1) == 0)) {
                outClosingTasks.add(task);
            }
        }
    }

    int getSnapshotMode(Task task) {
        AppWindowToken topChild = (AppWindowToken) task.getTopChild();
        if (StackId.isHomeOrRecentsStack(task.mStack.mStackId)) {
            return 2;
        }
        if (topChild == null || !topChild.shouldUseAppThemeSnapshot()) {
            return 0;
        }
        return 1;
    }

    private TaskSnapshot drawAppThemeSnapshot(Task task) {
        AppWindowToken topChild = (AppWindowToken) task.getTopChild();
        if (topChild == null) {
            return null;
        }
        WindowState mainWindow = topChild.findMainWindow();
        if (mainWindow == null) {
            return null;
        }
        int color = task.getTaskDescription().getBackgroundColor();
        int statusBarColor = task.getTaskDescription().getStatusBarColor();
        int navigationBarColor = task.getTaskDescription().getNavigationBarColor();
        LayoutParams attrs = mainWindow.getAttrs();
        SystemBarBackgroundPainter decorPainter = new SystemBarBackgroundPainter(attrs.flags, attrs.privateFlags, attrs.systemUiVisibility, statusBarColor, navigationBarColor);
        int width = mainWindow.getFrameLw().width();
        int height = mainWindow.getFrameLw().height();
        RenderNode node = RenderNode.create("TaskSnapshotController", null);
        node.setLeftTopRightBottom(0, 0, width, height);
        node.setClipToBounds(false);
        DisplayListCanvas c = node.start(width, height);
        c.drawColor(color);
        decorPainter.setInsets(mainWindow.mContentInsets, mainWindow.mStableInsets);
        decorPainter.drawDecors(c, null);
        node.end(c);
        Bitmap hwBitmap = ThreadedRenderer.createHardwareBitmap(node, width, height);
        if (hwBitmap == null) {
            return null;
        }
        return new TaskSnapshot(hwBitmap.createGraphicBufferHandle(), topChild.getConfiguration().orientation, mainWindow.mStableInsets, false, 1.0f);
    }

    void onAppRemoved(AppWindowToken wtoken) {
        this.mCache.onAppRemoved(wtoken);
    }

    void onAppDied(AppWindowToken wtoken) {
        this.mCache.onAppDied(wtoken);
    }

    void notifyTaskRemovedFromRecents(int taskId, int userId) {
        this.mCache.onTaskRemoved(taskId);
        this.mPersister.onTaskRemovedFromRecents(taskId, userId);
    }

    void removeObsoleteTaskFiles(ArraySet<Integer> persistentTaskIds, int[] runningUserIds) {
        this.mPersister.removeObsoleteFiles(persistentTaskIds, runningUserIds);
    }

    void setPersisterPaused(boolean paused) {
        this.mPersister.setPaused(paused);
    }

    void screenTurningOff(ScreenOffListener listener) {
        if (shouldDisableSnapshots()) {
            listener.onScreenOff();
        } else {
            this.mHandler.post(new AnonymousClass2(this, listener));
        }
    }

    /* synthetic */ void lambda$-com_android_server_wm_TaskSnapshotController_12298(ScreenOffListener listener) {
        try {
            synchronized (this.mService.mWindowMap) {
                WindowManagerService.boostPriorityForLockedSection();
                this.mTmpTasks.clear();
                this.mService.mRoot.forAllTasks(new AnonymousClass1(this));
                snapshotTasks(this.mTmpTasks);
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            listener.onScreenOff();
        } catch (Throwable th) {
            listener.onScreenOff();
        }
    }

    /* synthetic */ void lambda$-com_android_server_wm_TaskSnapshotController_12463(Task task) {
        if (task.isVisible()) {
            this.mTmpTasks.add(task);
        }
    }

    void dump(PrintWriter pw, String prefix) {
        this.mCache.dump(pw, prefix);
    }

    public int releaseSnapshots(int memLevel) {
        if (shouldDisableSnapshots()) {
            return 0;
        }
        return this.mCache.releaseSnapshots(memLevel);
    }

    private static int parseConfig() {
        long totalRam = Process.getTotalMemory();
        String[] configNums = SystemProperties.get("ro.config.max_snapshot_num", "").split(",");
        if (configNums.length != 4) {
            return 0;
        }
        int[] maxNums = new int[4];
        int i = 0;
        while (i < 4) {
            try {
                maxNums[i] = Integer.parseInt(configNums[i]);
                i++;
            } catch (NumberFormatException e) {
                Slog.e("TaskSnapshot", "encount error when parse config");
                return 0;
            }
        }
        if (totalRam <= 2147483648L) {
            return maxNums[0];
        }
        if (totalRam <= 3221225472L) {
            return maxNums[1];
        }
        if (totalRam <= 4294967296L) {
            return maxNums[2];
        }
        return maxNums[3];
    }
}
