package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.GraphicBuffer;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.ArraySet;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.DisplayInfo;
import android.view.DisplayListCanvas;
import android.view.RenderNode;
import android.view.SurfaceControl;
import android.view.ThreadedRenderer;
import android.view.WindowManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.graphics.ColorUtils;
import com.android.server.am.ActivityRecord;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.TaskSnapshotSurface;
import com.android.server.wm.utils.InsetUtils;
import com.google.android.collect.Sets;
import java.io.PrintWriter;
import java.util.function.Consumer;

class TaskSnapshotController {
    static final boolean IS_EMUI_LITE = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    protected static final boolean IS_NOTCH_PROP = (!SystemProperties.get("ro.config.hw_notch_size", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS).equals(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS));
    @VisibleForTesting
    static final int SNAPSHOT_MODE_APP_THEME = 1;
    @VisibleForTesting
    static final int SNAPSHOT_MODE_NONE = 2;
    @VisibleForTesting
    static final int SNAPSHOT_MODE_REAL = 0;
    private static final String TAG = "WindowManager";
    private final TaskSnapshotCache mCache;
    private final Handler mHandler = new Handler();
    private final boolean mIsRunningOnIoT;
    private final boolean mIsRunningOnTv;
    private final boolean mIsRunningOnWear;
    private final TaskSnapshotLoader mLoader = new TaskSnapshotLoader(this.mPersister);
    private final TaskSnapshotPersister mPersister = new TaskSnapshotPersister($$Lambda$TaskSnapshotController$OPdXuZQLetMnocdH6XV32JbNQ3I.INSTANCE);
    private final WindowManagerService mService;
    private final ArraySet<Task> mSkipClosingAppSnapshotTasks = new ArraySet<>();
    private final Rect mTmpRect = new Rect();
    private final ArraySet<Task> mTmpTasks = new ArraySet<>();

    TaskSnapshotController(WindowManagerService service) {
        this.mService = service;
        this.mCache = new TaskSnapshotCache(this.mService, this.mLoader);
        this.mIsRunningOnTv = this.mService.mContext.getPackageManager().hasSystemFeature("android.software.leanback");
        this.mIsRunningOnIoT = this.mService.mContext.getPackageManager().hasSystemFeature("android.hardware.type.embedded");
        this.mIsRunningOnWear = this.mService.mContext.getPackageManager().hasSystemFeature("android.hardware.type.watch");
    }

    /* access modifiers changed from: package-private */
    public void systemReady() {
        this.mPersister.start();
    }

    /* access modifiers changed from: package-private */
    public void onTransitionStarting() {
        handleClosingApps(this.mService.mClosingApps);
    }

    /* access modifiers changed from: package-private */
    public void notifyAppVisibilityChanged(AppWindowToken appWindowToken, boolean visible) {
        if (!visible) {
            handleClosingApps(Sets.newArraySet(new AppWindowToken[]{appWindowToken}));
        }
    }

    private boolean checkWhiteListApp(ArraySet<AppWindowToken> closingApps) {
        for (int i = closingApps.size() - 1; i >= 0; i--) {
            ActivityRecord tmp = ActivityRecord.forToken(closingApps.valueAt(i).token);
            if (tmp != null && (tmp.realActivity.getPackageName().equals("com.sina.weibo") || tmp.realActivity.getPackageName().equals("com.taobao.taobao"))) {
                return true;
            }
        }
        return false;
    }

    private void handleClosingApps(ArraySet<AppWindowToken> closingApps) {
        if (shouldDisableSnapshots()) {
            if (checkWhiteListApp(closingApps)) {
                Slog.i(TAG, "We got the white list app");
            } else {
                return;
            }
        }
        getClosingTasks(closingApps, this.mTmpTasks);
        snapshotTasks(this.mTmpTasks);
        this.mSkipClosingAppSnapshotTasks.clear();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void addSkipClosingAppSnapshotTasks(ArraySet<Task> tasks) {
        this.mSkipClosingAppSnapshotTasks.addAll(tasks);
    }

    /* access modifiers changed from: package-private */
    public void snapshotTasks(ArraySet<Task> tasks) {
        ActivityManager.TaskSnapshot snapshot;
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task task = tasks.valueAt(i);
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
                GraphicBuffer buffer = snapshot.getSnapshot();
                if (buffer.getWidth() == 0 || buffer.getHeight() == 0) {
                    buffer.destroy();
                    Slog.e(TAG, "Invalid task snapshot dimensions " + buffer.getWidth() + "x" + buffer.getHeight());
                } else {
                    this.mCache.putSnapshot(task, snapshot);
                    this.mPersister.persistSnapshot(task.mTaskId, task.mUserId, snapshot);
                    if (task.getController() != null) {
                        task.getController().reportSnapshotChanged(snapshot);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.TaskSnapshot getSnapshot(int taskId, int userId, boolean restoreFromDisk, boolean reducedResolution) {
        return this.mCache.getSnapshot(taskId, userId, restoreFromDisk, reducedResolution || TaskSnapshotPersister.DISABLE_FULL_SIZED_BITMAPS);
    }

    /* access modifiers changed from: package-private */
    public WindowManagerPolicy.StartingSurface createStartingSurface(AppWindowToken token, ActivityManager.TaskSnapshot snapshot) {
        return TaskSnapshotSurface.create(this.mService, token, snapshot);
    }

    private ActivityManager.TaskSnapshot snapshotTask(Task task) {
        return snapshotTask(task, false);
    }

    private ActivityManager.TaskSnapshot snapshotTask(Task task, boolean animationLeashDelay) {
        float scaleFraction;
        GraphicBuffer buffer;
        Task task2 = task;
        AppWindowToken top = (AppWindowToken) task.getTopChild();
        if (top == null) {
            return null;
        }
        WindowState mainWindow = top.findMainWindow();
        if (mainWindow == null) {
            return null;
        }
        if (!this.mService.mPolicy.isScreenOn()) {
            Slog.i(TAG, "Attempted to take screenshot while display was off.");
            return null;
        } else if (task.getSurfaceControl() == null) {
            return null;
        } else {
            if (top.hasCommittedReparentToAnimationLeash(animationLeashDelay)) {
                Slog.w(TAG, "Failed to take screenshot. App is animating " + top);
                return null;
            } else if (!top.forAllWindows($$Lambda$TaskSnapshotController$1IXTXVXjIGs9ncGKW_v40ivZeoI.INSTANCE, true)) {
                Slog.w(TAG, "Failed to take screenshot. No visible windows for " + task2);
                return null;
            } else {
                boolean isLowRamDevice = ActivityManager.isLowRamDeviceStatic();
                float scaleFraction2 = isLowRamDevice ? TaskSnapshotPersister.REDUCED_SCALE : 1.0f;
                if (!isLowRamDevice && IS_EMUI_LITE) {
                    Context context = ActivityThread.currentApplication();
                    if (context != null) {
                        scaleFraction2 *= context.getResources().getFraction(34668545, 1, 1);
                    }
                }
                if (TaskSnapshotPersister.USE_CONFIG_SCALE) {
                    scaleFraction = ((float) TaskSnapshotPersister.CONFIG_SCALE) / 100.0f;
                } else {
                    scaleFraction = scaleFraction2;
                }
                task2.getBounds(this.mTmpRect);
                if (!HwPCUtils.isValidExtDisplayId(mainWindow.getDisplayId()) || this.mService.mHwWMSEx.getPCScreenDisplayMode() == 0) {
                    if (top.getConfiguration().orientation == 2 && this.mTmpRect.width() < this.mTmpRect.height()) {
                        this.mTmpRect.set(this.mTmpRect.top, this.mTmpRect.left, this.mTmpRect.bottom, this.mTmpRect.right);
                        Slog.i(TAG, "Screenshot bounds is updated to: " + this.mTmpRect);
                    }
                    this.mTmpRect.offsetTo(0, 0);
                } else {
                    Point p = new Point(0, 0);
                    this.mService.mHwWMSEx.updateDimPositionForPCMode(task2, this.mTmpRect);
                    this.mService.mHwWMSEx.updateSurfacePositionForPCMode(mainWindow, p);
                    this.mTmpRect.offsetTo(p.x + mainWindow.mAttrs.surfaceInsets.left, p.y + mainWindow.mAttrs.surfaceInsets.top);
                }
                if (IS_NOTCH_PROP && this.mService.getDefaultDisplayContentLocked().getRotation() == 1 && !mainWindow.mFrame.isEmpty() && this.mService.mPolicy.isNotchDisplayDisabled()) {
                    this.mTmpRect.intersect(mainWindow.mFrame);
                }
                if (this.mService.getLazyMode() == 0) {
                    if (task.inFreeformWindowingMode()) {
                        DisplayInfo displayInfo = top.mDisplayContent.getDisplayInfo();
                        int dw = displayInfo.logicalWidth;
                        int dh = displayInfo.logicalHeight;
                        this.mTmpRect.set(new Rect(0, 0, dw, dh));
                        int rot = this.mService.getDefaultDisplayContentLocked().getRotation();
                        int i = 3;
                        if (rot == 1 || rot == 3) {
                            if (rot != 1) {
                                i = 1;
                            }
                            rot = i;
                        }
                        this.mTmpRect.intersect(mainWindow.mFrame);
                        DisplayContent displayContent = top.mDisplayContent;
                        DisplayContent.convertCropForSurfaceFlinger(this.mTmpRect, rot, dw, dh);
                        ScreenRotationAnimation screenRotationAnimation = this.mService.mAnimator.getScreenRotationAnimationLocked(0);
                        buffer = SurfaceControl.screenshotToBuffer(this.mTmpRect, dw, dh, 0, 1, screenRotationAnimation != null && screenRotationAnimation.isAnimating(), rot);
                    } else {
                        buffer = SurfaceControl.captureLayers(task.getSurfaceControl().getHandle(), this.mTmpRect, scaleFraction);
                    }
                    if (HwFoldScreenState.isFoldScreenDevice() && this.mService.isInSubFoldScaleMode() && mainWindow.getSurfaceControl() != null) {
                        this.mTmpRect.scale(this.mService.mSubFoldModeScale);
                        buffer = SurfaceControl.captureLayers(mainWindow.getSurfaceControl().getHandle(), this.mTmpRect, scaleFraction / this.mService.mSubFoldModeScale);
                    }
                } else if (mainWindow.getSurfaceControl() != null) {
                    this.mTmpRect.scale(0.75f);
                    buffer = SurfaceControl.captureLayers(mainWindow.getSurfaceControl().getHandle(), this.mTmpRect, scaleFraction / 0.75f);
                } else {
                    buffer = null;
                }
                boolean isWindowTranslucent = mainWindow.getAttrs().format != -1;
                if (buffer == null || buffer.getWidth() <= 1) {
                } else if (buffer.getHeight() <= 1) {
                    float f = scaleFraction;
                } else {
                    float f2 = scaleFraction;
                    ActivityManager.TaskSnapshot taskSnapshot = new ActivityManager.TaskSnapshot(buffer, top.getConfiguration().orientation, getInsets(mainWindow), isLowRamDevice, scaleFraction, true, task.getWindowingMode(), getSystemUiVisibility(task), !top.fillsParent() || isWindowTranslucent);
                    return taskSnapshot;
                }
                Slog.w(TAG, "Failed to take screenshot for " + task2);
                return null;
            }
        }
    }

    static /* synthetic */ boolean lambda$snapshotTask$0(WindowState ws) {
        return (ws.mAppToken == null || ws.mAppToken.isSurfaceShowing()) && ws.mWinAnimator != null && ws.mWinAnimator.getShown() && ws.mWinAnimator.mLastAlpha > 0.0f;
    }

    private boolean shouldDisableSnapshots() {
        return this.mIsRunningOnWear || this.mIsRunningOnTv || this.mIsRunningOnIoT;
    }

    private Rect getInsets(WindowState state) {
        Rect insets = minRect(state.mContentInsets, state.mStableInsets);
        InsetUtils.addInsets(insets, state.mAppToken.getLetterboxInsets());
        return insets;
    }

    private Rect minRect(Rect rect1, Rect rect2) {
        return new Rect(Math.min(rect1.left, rect2.left), Math.min(rect1.top, rect2.top), Math.min(rect1.right, rect2.right), Math.min(rect1.bottom, rect2.bottom));
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void getClosingTasks(ArraySet<AppWindowToken> closingApps, ArraySet<Task> outClosingTasks) {
        outClosingTasks.clear();
        for (int i = closingApps.size() - 1; i >= 0; i--) {
            Task task = closingApps.valueAt(i).getTask();
            if (task != null && !task.isVisible() && !this.mSkipClosingAppSnapshotTasks.contains(task)) {
                outClosingTasks.add(task);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getSnapshotMode(Task task) {
        AppWindowToken topChild = (AppWindowToken) task.getTopChild();
        if (!task.isActivityTypeStandardOrUndefined() && !task.isActivityTypeAssistant()) {
            return 2;
        }
        if (topChild == null || !topChild.shouldUseAppThemeSnapshot()) {
            return 0;
        }
        return 1;
    }

    private ActivityManager.TaskSnapshot drawAppThemeSnapshot(Task task) {
        AppWindowToken topChild = (AppWindowToken) task.getTopChild();
        if (topChild == null) {
            return null;
        }
        WindowState mainWindow = topChild.findMainWindow();
        if (mainWindow == null) {
            return null;
        }
        int color = ColorUtils.setAlphaComponent(task.getTaskDescription().getBackgroundColor(), 255);
        int statusBarColor = task.getTaskDescription().getStatusBarColor();
        int navigationBarColor = task.getTaskDescription().getNavigationBarColor();
        WindowManager.LayoutParams attrs = mainWindow.getAttrs();
        TaskSnapshotSurface.SystemBarBackgroundPainter decorPainter = new TaskSnapshotSurface.SystemBarBackgroundPainter(attrs.flags, attrs.privateFlags, attrs.systemUiVisibility, statusBarColor, navigationBarColor);
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
        ActivityManager.TaskSnapshot taskSnapshot = new ActivityManager.TaskSnapshot(hwBitmap.createGraphicBufferHandle(), topChild.getConfiguration().orientation, mainWindow.mStableInsets, ActivityManager.isLowRamDeviceStatic(), 1.0f, false, task.getWindowingMode(), getSystemUiVisibility(task), false);
        return taskSnapshot;
    }

    /* access modifiers changed from: package-private */
    public void onAppRemoved(AppWindowToken wtoken) {
        this.mCache.onAppRemoved(wtoken);
    }

    /* access modifiers changed from: package-private */
    public void onAppDied(AppWindowToken wtoken) {
        this.mCache.onAppDied(wtoken);
    }

    /* access modifiers changed from: package-private */
    public void notifyTaskRemovedFromRecents(int taskId, int userId) {
        this.mCache.onTaskRemoved(taskId);
        this.mPersister.onTaskRemovedFromRecents(taskId, userId);
    }

    /* access modifiers changed from: package-private */
    public void removeObsoleteTaskFiles(ArraySet<Integer> persistentTaskIds, int[] runningUserIds) {
        this.mPersister.removeObsoleteFiles(persistentTaskIds, runningUserIds);
    }

    /* access modifiers changed from: package-private */
    public void setPersisterPaused(boolean paused) {
        this.mPersister.setPaused(paused);
    }

    /* access modifiers changed from: package-private */
    public void screenTurningOff(WindowManagerPolicy.ScreenOffListener listener) {
        if (shouldDisableSnapshots()) {
            listener.onScreenOff();
        } else {
            this.mHandler.post(new Runnable(listener) {
                private final /* synthetic */ WindowManagerPolicy.ScreenOffListener f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    TaskSnapshotController.lambda$screenTurningOff$2(TaskSnapshotController.this, this.f$1);
                }
            });
        }
    }

    public static /* synthetic */ void lambda$screenTurningOff$2(TaskSnapshotController taskSnapshotController, WindowManagerPolicy.ScreenOffListener listener) {
        try {
            synchronized (taskSnapshotController.mService.mWindowMap) {
                WindowManagerService.boostPriorityForLockedSection();
                taskSnapshotController.mTmpTasks.clear();
                taskSnapshotController.mService.mRoot.forAllTasks(new Consumer() {
                    public final void accept(Object obj) {
                        TaskSnapshotController.lambda$screenTurningOff$1(TaskSnapshotController.this, (Task) obj);
                    }
                });
                taskSnapshotController.snapshotTasks(taskSnapshotController.mTmpTasks);
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            listener.onScreenOff();
        } catch (Throwable th) {
            listener.onScreenOff();
            throw th;
        }
    }

    public static /* synthetic */ void lambda$screenTurningOff$1(TaskSnapshotController taskSnapshotController, Task task) {
        if (task.isVisible()) {
            taskSnapshotController.mTmpTasks.add(task);
        }
    }

    private int getSystemUiVisibility(Task task) {
        WindowState topFullscreenWindow;
        AppWindowToken topFullscreenToken = task.getTopFullscreenAppToken();
        if (topFullscreenToken != null) {
            topFullscreenWindow = topFullscreenToken.getTopFullscreenWindow();
        } else {
            topFullscreenWindow = null;
        }
        if (topFullscreenWindow != null) {
            return topFullscreenWindow.getSystemUiVisibility();
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        this.mCache.dump(pw, prefix);
    }

    public ActivityManager.TaskSnapshot getForegroundTaskSnapshot() {
        return this.mCache.getLastForegroundSnapshot();
    }

    public void clearForegroundTaskSnapshot() {
        this.mCache.clearForegroundTaskSnapshot();
    }

    public ActivityManager.TaskSnapshot createForegroundTaskSnapshot(AppWindowToken appWindowToken) {
        ActivityManager.TaskSnapshot snapshot;
        Task task = appWindowToken.getTask();
        switch (getSnapshotMode(task)) {
            case 0:
                snapshot = snapshotTask(task, true);
                break;
            case 1:
                snapshot = drawAppThemeSnapshot(task);
                break;
            case 2:
                snapshot = null;
                break;
            default:
                snapshot = null;
                break;
        }
        if (snapshot != null) {
            GraphicBuffer buffer = snapshot.getSnapshot();
            if (buffer.getWidth() == 0 || buffer.getHeight() == 0) {
                buffer.destroy();
                Slog.e(TAG, "getForgroundTaskSnapshot Invalid task snapshot dimensions " + buffer.getWidth() + "x" + buffer.getHeight());
                return null;
            }
            this.mCache.putForegroundSnapShot(task, snapshot);
        }
        return snapshot;
    }
}
