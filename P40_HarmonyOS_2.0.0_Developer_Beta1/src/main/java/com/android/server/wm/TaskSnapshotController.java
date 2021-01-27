package com.android.server.wm;

import android.app.ActivityManager;
import android.common.HwFrameworkFactory;
import android.graphics.Bitmap;
import android.graphics.GraphicBuffer;
import android.graphics.RecordingCanvas;
import android.graphics.Rect;
import android.graphics.RenderNode;
import android.hardware.display.HwFoldScreenState;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.ArraySet;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.DisplayInfo;
import android.view.SurfaceControl;
import android.view.ThreadedRenderer;
import android.view.WindowManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.graphics.ColorUtils;
import com.android.internal.util.ToBooleanFunction;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.TaskSnapshotSurface;
import com.android.server.wm.utils.HwDisplaySizeUtil;
import com.android.server.wm.utils.InsetUtils;
import com.google.android.collect.Sets;
import java.io.PrintWriter;
import java.util.function.Consumer;

/* access modifiers changed from: package-private */
public class TaskSnapshotController {
    private static final boolean CONFIG_PIXEL_FORMAT_REDUCED = SystemProperties.getBoolean("hw_sc.snapshot_pixel_format_reduced", false);
    private static final int CONFIG_SCALE = SystemProperties.getInt("ro.config.hw_snapshot_scale", -1);
    private static final boolean HW_SNAPSHOT = SystemProperties.getBoolean("ro.huawei.only_hwsnapshot", true);
    private static final boolean IS_HW_TV_MULTI_TASK_ENABLE = ("tv".equals(SystemProperties.get("ro.build.characteristics", "")) && SystemProperties.getBoolean("hw_mc.tvlauncher.multitask_enable", false));
    protected static final boolean IS_NOTCH_PROP = (!SystemProperties.get("ro.config.hw_notch_size", "").equals(""));
    @VisibleForTesting
    static final int SNAPSHOT_MODE_APP_THEME = 1;
    @VisibleForTesting
    static final int SNAPSHOT_MODE_NONE = 2;
    @VisibleForTesting
    static final int SNAPSHOT_MODE_REAL = 0;
    private static final String TAG = "WindowManager";
    private static final boolean USE_CONFIG_SCALE;
    private final TaskSnapshotCache mCache;
    private final float mFullSnapshotScale;
    private final Handler mHandler = new Handler();
    private final boolean mIsRunningOnIoT;
    private final boolean mIsRunningOnTv;
    private final boolean mIsRunningOnWear;
    private boolean mIsTvMultiWindowSnapShot;
    private final TaskSnapshotLoader mLoader;
    private final TaskSnapshotPersister mPersister;
    private final WindowManagerService mService;
    private final ArraySet<Task> mSkipClosingAppSnapshotTasks = new ArraySet<>();
    private final Rect mTmpRect = new Rect();
    private final ArraySet<Task> mTmpTasks = new ArraySet<>();

    static {
        int i = CONFIG_SCALE;
        USE_CONFIG_SCALE = i >= 50 && i < 100;
    }

    TaskSnapshotController(WindowManagerService service) {
        this.mService = service;
        this.mPersister = new TaskSnapshotPersister(this.mService, $$Lambda$OPdXuZQLetMnocdH6XV32JbNQ3I.INSTANCE);
        this.mLoader = new TaskSnapshotLoader(this.mPersister);
        this.mCache = new TaskSnapshotCache(this.mService, this.mLoader);
        this.mIsRunningOnTv = this.mService.mContext.getPackageManager().hasSystemFeature("android.software.leanback");
        this.mIsRunningOnIoT = this.mService.mContext.getPackageManager().hasSystemFeature("android.hardware.type.embedded");
        this.mIsRunningOnWear = this.mService.mContext.getPackageManager().hasSystemFeature("android.hardware.type.watch");
        this.mFullSnapshotScale = this.mService.mContext.getResources().getFloat(17105060);
    }

    /* access modifiers changed from: package-private */
    public void systemReady() {
        this.mPersister.start();
    }

    /* access modifiers changed from: package-private */
    public void onTransitionStarting(DisplayContent displayContent) {
        handleClosingApps(displayContent.mClosingApps);
    }

    /* access modifiers changed from: package-private */
    public void notifyAppVisibilityChanged(AppWindowToken appWindowToken, boolean visible) {
        if (!visible) {
            handleClosingApps(Sets.newArraySet(new AppWindowToken[]{appWindowToken}));
        }
    }

    private void handleClosingApps(ArraySet<AppWindowToken> closingApps) {
        if (!shouldDisableSnapshots()) {
            getClosingTasks(closingApps, this.mTmpTasks);
            snapshotTasks(this.mTmpTasks);
            this.mSkipClosingAppSnapshotTasks.clear();
            WindowManagerService windowManagerService = this.mService;
            windowManagerService.handleWaitBlurTasks(windowManagerService);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void addSkipClosingAppSnapshotTasks(ArraySet<Task> tasks) {
        this.mSkipClosingAppSnapshotTasks.addAll((ArraySet<? extends Task>) tasks);
    }

    /* access modifiers changed from: package-private */
    public void snapshotTasks(ArraySet<Task> tasks) {
        ActivityManager.TaskSnapshot snapshot;
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task task = tasks.valueAt(i);
            int mode = getSnapshotMode(task);
            if (mode == 0) {
                snapshot = snapshotTask(task);
            } else if (mode == 1) {
                snapshot = drawAppThemeSnapshot(task);
            } else if (mode != 2) {
                snapshot = null;
            }
            if (snapshot != null) {
                GraphicBuffer buffer = snapshot.getSnapshot();
                if (buffer.getWidth() == 0 || buffer.getHeight() == 0) {
                    buffer.destroy();
                    Slog.e(TAG, "Invalid task snapshot dimensions " + buffer.getWidth() + "x" + buffer.getHeight());
                } else {
                    this.mCache.putSnapshot(task, snapshot);
                    this.mPersister.persistSnapshot(task.mTaskId, task.mUserId, snapshot);
                    task.onSnapshotChanged(snapshot);
                    this.mService.addToWaitBlurTaskMap(findAppTokenForSnapshot(task), task, snapshot);
                }
            }
        }
    }

    public void saveSnapshot(Task task, ActivityManager.TaskSnapshot snapshot) {
        this.mCache.putSnapshot(task, snapshot);
        this.mPersister.persistSnapshot(task.mTaskId, task.mUserId, snapshot);
        task.onSnapshotChanged(snapshot);
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.TaskSnapshot getSnapshot(int taskId, int userId, boolean restoreFromDisk, boolean reducedResolution) {
        return this.mCache.getSnapshot(taskId, userId, restoreFromDisk, reducedResolution || TaskSnapshotPersister.DISABLE_FULL_SIZED_BITMAPS || USE_CONFIG_SCALE);
    }

    /* access modifiers changed from: package-private */
    public WindowManagerPolicy.StartingSurface createStartingSurface(AppWindowToken token, ActivityManager.TaskSnapshot snapshot) {
        return TaskSnapshotSurface.create(this.mService, token, snapshot);
    }

    private AppWindowToken findAppTokenForSnapshot(Task task) {
        WindowState winState;
        for (int i = task.getChildCount() - 1; i >= 0; i--) {
            AppWindowToken appWindowToken = (AppWindowToken) task.getChildAt(i);
            if (appWindowToken != null && appWindowToken.isSurfaceShowing() && (winState = appWindowToken.findMainWindow()) != null && (((winState.getAttrs().hwFlags & 2048) == 0 || task.getChildCount() <= 1) && appWindowToken.forAllWindows((ToBooleanFunction<WindowState>) $$Lambda$TaskSnapshotController$b7mc92hqzbRpmpc99dYS4wKuL6Y.INSTANCE, true))) {
                return appWindowToken;
            }
        }
        return null;
    }

    static /* synthetic */ boolean lambda$findAppTokenForSnapshot$0(WindowState ws) {
        return ws.mWinAnimator != null && ws.mWinAnimator.getShown() && ws.mWinAnimator.mLastAlpha > 0.0f;
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl.ScreenshotGraphicBuffer createTaskSnapshot(Task task, float scaleFraction) {
        return createTaskSnapshot(task, scaleFraction, false);
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl.ScreenshotGraphicBuffer createTaskSnapshot(Task task, float scaleFraction, boolean pixelFormatReduced) {
        AppWindowToken appWindowToken = findAppTokenForSnapshot(task);
        if (appWindowToken == null) {
            Slog.w(TAG, "Failed to take screenshot. No visible windows for " + task);
            return null;
        }
        WindowState mainWindow = appWindowToken.findMainWindow();
        if (mainWindow == null) {
            Slog.w(TAG, "Failed to take screenshot. No main window for " + task);
            return null;
        } else if (task.getSurfaceControl() == null) {
            return null;
        } else {
            task.getBounds(this.mTmpRect);
            if (!appWindowToken.inHwMultiStackWindowingMode() && appWindowToken.getConfiguration().orientation == 2 && this.mTmpRect.width() < this.mTmpRect.height()) {
                Rect rect = this.mTmpRect;
                rect.set(rect.top, this.mTmpRect.left, this.mTmpRect.bottom, this.mTmpRect.right);
                Slog.i(TAG, "Screenshot bounds is updated to: " + this.mTmpRect);
            }
            boolean isWindowDismatchTask = task.inHwFreeFormWindowingMode() && !this.mTmpRect.equals(mainWindow.getContainingFrame()) && mainWindow.getSurfaceControl() != null;
            if (isWindowDismatchTask) {
                this.mTmpRect.set(mainWindow.getContainingFrame());
            }
            float scale = (!task.inHwFreeFormWindowingMode() || task.mStack == null) ? 1.0f : task.mStack.mHwStackScale;
            this.mTmpRect.scale(scale);
            if (this.mIsTvMultiWindowSnapShot) {
                scale = 1.0f;
            }
            this.mTmpRect.offsetTo(0, 0);
            if (isWindowDismatchTask) {
                Rect taskBounds = task.getBounds();
                this.mTmpRect.offset(mainWindow.getContainingFrame().left - taskBounds.left, mainWindow.getContainingFrame().top - taskBounds.top);
            }
            if (task.getDisplayContent() != null && task.getDisplayContent().getDisplayId() == 0 && !task.inHwMultiStackWindowingMode() && !task.getDisplayContent().mChangingApps.contains(appWindowToken)) {
                int rotation = this.mService.getDefaultDisplayContentLocked().getRotation();
                if (HwDisplaySizeUtil.hasSideInScreen()) {
                    Rect frameRect = mainWindow.getDisplayFrameLw();
                    if (rotation == 0 || rotation == 2) {
                        frameRect.top = this.mTmpRect.top;
                        frameRect.bottom = this.mTmpRect.bottom;
                    }
                    this.mTmpRect.intersectUnchecked(frameRect);
                }
                if (IS_NOTCH_PROP && rotation == 1 && !mainWindow.mWindowFrames.mFrame.isEmpty() && this.mService.mPolicy.isNotchDisplayDisabled() && !this.mService.mAtmService.mInFreeformSnapshot) {
                    this.mTmpRect.intersect(mainWindow.mWindowFrames.mDisplayFrame);
                }
            }
            SurfaceControl.ScreenshotGraphicBuffer screenshotBuffer = null;
            GraphicBuffer buffer = null;
            if (task.inFreeformWindowingMode()) {
                DisplayInfo displayInfo = appWindowToken.mDisplayContent.getDisplayInfo();
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
                this.mTmpRect.intersect(mainWindow.mWindowFrames.mFrame);
                DisplayContent displayContent = appWindowToken.mDisplayContent;
                DisplayContent.convertCropForSurfaceFlinger(this.mTmpRect, rot, dw, dh);
                ScreenRotationAnimation screenRotationAnimation = this.mService.mAnimator.getScreenRotationAnimationLocked(0);
                boolean inRotation = screenRotationAnimation != null && screenRotationAnimation.isAnimating();
                if (SurfaceControl.getInternalDisplayToken() != null) {
                    screenshotBuffer = SurfaceControl.screenshotToBuffer(SurfaceControl.getInternalDisplayToken(), this.mTmpRect, dw, dh, inRotation, rot);
                    buffer = screenshotBuffer != null ? screenshotBuffer.getGraphicBuffer() : null;
                }
            } else {
                SurfaceControl.ScreenshotGraphicBuffer screenshotBuffer2 = SurfaceControl.captureLayers(task.getSurfaceControl().getHandle(), this.mTmpRect, scaleFraction / scale, pixelFormatReduced);
                if (!HwFoldScreenState.isFoldScreenDevice() || !this.mService.isInSubFoldScaleMode() || HwPCUtils.isValidExtDisplayId(mainWindow.getDisplayId()) || this.mService.mAtmService.mHwATMSEx.isVirtualDisplayId(mainWindow.getDisplayId(), "padCast") || mainWindow.getSurfaceControl() == null) {
                    screenshotBuffer = screenshotBuffer2;
                } else {
                    if (task.inHwFreeFormWindowingMode()) {
                        this.mTmpRect.offsetTo((int) (((float) mainWindow.mAttrs.surfaceInsets.left) * scale), (int) (((float) mainWindow.mAttrs.surfaceInsets.top) * scale));
                    }
                    this.mTmpRect.scale(this.mService.mSubFoldModeScale);
                    screenshotBuffer = SurfaceControl.captureLayers(mainWindow.getSurfaceControl().getHandle(), this.mTmpRect, scaleFraction / (this.mService.mSubFoldModeScale * scale), pixelFormatReduced);
                }
                buffer = screenshotBuffer != null ? screenshotBuffer.getGraphicBuffer() : null;
            }
            if (buffer != null && buffer.getWidth() > 1 && buffer.getHeight() > 1) {
                return screenshotBuffer;
            }
            Slog.w(TAG, "Failed to take screenshot for " + task);
            return null;
        }
    }

    private ActivityManager.TaskSnapshot snapshotTask(Task task) {
        return snapshotTask(task, false);
    }

    private ActivityManager.TaskSnapshot snapshotTask(Task task, boolean animationLeashDelay) {
        float scaleFraction;
        boolean pixelFormatReduced;
        boolean pixelFormatReduced2;
        if (!this.mService.mPolicy.isScreenOn()) {
            Slog.i(TAG, "Attempted to take screenshot while display was off.");
            return null;
        }
        AppWindowToken appWindowToken = findAppTokenForSnapshot(task);
        if (appWindowToken == null) {
            Slog.w(TAG, "Failed to take screenshot. No visible windows for " + task);
            return null;
        } else if (appWindowToken.mHadTakenSnapShot && HW_SNAPSHOT && !this.mIsTvMultiWindowSnapShot) {
            Slog.w(TAG, "Failed to take screenshot " + appWindowToken + " mHadTakenSnapShot " + appWindowToken.mHadTakenSnapShot);
            return null;
        } else if (appWindowToken.hasCommittedReparentToAnimationLeash(animationLeashDelay)) {
            Slog.w(TAG, "Failed to take screenshot. App is animating " + appWindowToken);
            return null;
        } else {
            boolean isLowRamDevice = ActivityManager.isLowRamDeviceStatic();
            if (isLowRamDevice) {
                scaleFraction = this.mPersister.getReducedScale();
            } else {
                scaleFraction = this.mFullSnapshotScale;
            }
            WindowState mainWindow = appWindowToken.findMainWindow();
            if (mainWindow == null) {
                Slog.w(TAG, "Failed to take screenshot. No main window for " + task);
                return null;
            }
            boolean isReduced = isLowRamDevice;
            if (USE_CONFIG_SCALE) {
                scaleFraction = ((float) CONFIG_SCALE) / 100.0f;
                isReduced = true;
            }
            boolean isAdjustForIme = false;
            if (CONFIG_PIXEL_FORMAT_REDUCED) {
                WindowState topVisibleWindow = appWindowToken.getTopVisibleWindow();
                if (topVisibleWindow == null) {
                    pixelFormatReduced2 = false;
                } else {
                    pixelFormatReduced2 = ((topVisibleWindow.getAttrs().flags & Integer.MIN_VALUE) == 0 && (topVisibleWindow.getAttrs().flags & 134217728) == 0) ? false : true;
                }
                pixelFormatReduced = pixelFormatReduced2;
            } else {
                pixelFormatReduced = false;
            }
            SurfaceControl.ScreenshotGraphicBuffer screenshotBuffer = createTaskSnapshot(task, scaleFraction, pixelFormatReduced);
            if (screenshotBuffer == null) {
                return null;
            }
            appWindowToken.mHadTakenSnapShot = true;
            ActivityManager.TaskSnapshot taskSnapshot = new ActivityManager.TaskSnapshot(appWindowToken.mActivityComponent, screenshotBuffer.getGraphicBuffer(), screenshotBuffer.getColorSpace(), appWindowToken.getTask().getConfiguration().orientation, getInsets(mainWindow), isReduced, scaleFraction, true, task.getWindowingMode(), getSystemUiVisibility(task), !appWindowToken.fillsParent() || (mainWindow.getAttrs().format != -1));
            if (task.inHwSplitScreenWindowingMode() && task.mStack != null && task.mStack.isAdjustedForIme()) {
                isAdjustForIme = true;
            }
            taskSnapshot.setWindowBounds(isAdjustForIme ? task.mStack.getRawBounds() : task.getBounds());
            return taskSnapshot;
        }
    }

    private boolean shouldDisableSnapshots() {
        return (this.mIsRunningOnTv && !IS_HW_TV_MULTI_TASK_ENABLE) || this.mIsRunningOnIoT;
    }

    private Rect getInsets(WindowState state) {
        Rect insets = minRect(state.getContentInsets(), state.getStableInsets());
        InsetUtils.addInsets(insets, state.mAppToken.getLetterboxInsets());
        if (HwFrameworkFactory.getHwApsImpl() != null) {
            HwFrameworkFactory.getHwApsImpl().scaleInsetsWhenSdrUpInRog(state.getOwningPackage(), insets);
        }
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
        if (topChild != null && topChild.toString().contains("VolumeAdjustmentTutorialMainActivity")) {
            return 2;
        }
        if (topChild == null || !topChild.shouldUseAppThemeSnapshot()) {
            return 0;
        }
        return 1;
    }

    private ActivityManager.TaskSnapshot drawAppThemeSnapshot(Task task) {
        WindowState mainWindow;
        AppWindowToken topChild = (AppWindowToken) task.getTopChild();
        if (topChild == null || (mainWindow = topChild.findMainWindow()) == null) {
            return null;
        }
        int color = ColorUtils.setAlphaComponent(task.getTaskDescription().getBackgroundColor(), 255);
        WindowManager.LayoutParams attrs = mainWindow.getAttrs();
        TaskSnapshotSurface.SystemBarBackgroundPainter decorPainter = new TaskSnapshotSurface.SystemBarBackgroundPainter(attrs.flags, attrs.privateFlags, attrs.systemUiVisibility, task.getTaskDescription(), this.mFullSnapshotScale);
        int width = (int) (((float) task.getBounds().width()) * this.mFullSnapshotScale);
        int height = (int) (((float) task.getBounds().height()) * this.mFullSnapshotScale);
        RenderNode node = RenderNode.create("TaskSnapshotController", null);
        boolean isAdjustForIme = false;
        node.setLeftTopRightBottom(0, 0, width, height);
        node.setClipToBounds(false);
        RecordingCanvas c = node.start(width, height);
        c.drawColor(color);
        decorPainter.setInsets(mainWindow.getContentInsets(), mainWindow.getStableInsets());
        decorPainter.drawDecors(c, null);
        node.end(c);
        Bitmap hwBitmap = ThreadedRenderer.createHardwareBitmap(node, width, height);
        if (hwBitmap == null) {
            return null;
        }
        if (task.inHwSplitScreenWindowingMode() && task.mStack != null && task.mStack.isAdjustedForIme()) {
            isAdjustForIme = true;
        }
        if (isAdjustForIme) {
            topChild.mHadTakenSnapShot = true;
        }
        ActivityManager.TaskSnapshot taskSnapshot = new ActivityManager.TaskSnapshot(topChild.mActivityComponent, hwBitmap.createGraphicBufferHandle(), hwBitmap.getColorSpace(), topChild.getTask().getConfiguration().orientation, getInsets(mainWindow), ActivityManager.isLowRamDeviceStatic(), this.mFullSnapshotScale, false, task.getWindowingMode(), getSystemUiVisibility(task), false);
        taskSnapshot.setWindowBounds(isAdjustForIme ? task.mStack.getRawBounds() : task.getBounds());
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
                /* class com.android.server.wm.$$Lambda$TaskSnapshotController$qBG2kMqHK9gvuY43J0TfS4aSVU */
                private final /* synthetic */ WindowManagerPolicy.ScreenOffListener f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    TaskSnapshotController.this.lambda$screenTurningOff$2$TaskSnapshotController(this.f$1);
                }
            });
        }
    }

    /* JADX INFO: finally extract failed */
    public /* synthetic */ void lambda$screenTurningOff$2$TaskSnapshotController(WindowManagerPolicy.ScreenOffListener listener) {
        try {
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mTmpTasks.clear();
                    this.mService.mRoot.forAllTasks(new Consumer() {
                        /* class com.android.server.wm.$$Lambda$TaskSnapshotController$ewiDm2ws6pdTXd1elso7FtoLKw */

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            TaskSnapshotController.this.lambda$screenTurningOff$1$TaskSnapshotController((Task) obj);
                        }
                    });
                    snapshotTasks(this.mTmpTasks);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            listener.onScreenOff();
        }
    }

    public /* synthetic */ void lambda$screenTurningOff$1$TaskSnapshotController(Task task) {
        if (task.isVisible()) {
            this.mTmpTasks.add(task);
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
        pw.println(prefix + "mFullSnapshotScale=" + this.mFullSnapshotScale);
        this.mCache.dump(pw, prefix);
    }

    public ActivityManager.TaskSnapshot getForegroundTaskSnapshot() {
        return this.mCache.getLastForegroundSnapshot();
    }

    public void clearForegroundTaskSnapshot() {
        this.mCache.clearForegroundTaskSnapshot();
    }

    public ActivityManager.TaskSnapshot createForegroundTaskSnapshot(AppWindowToken appWindowToken) {
        final ActivityManager.TaskSnapshot snapshot;
        final Task task = appWindowToken.getTask();
        int mode = getSnapshotMode(task);
        if (mode == 0) {
            appWindowToken.mHadTakenSnapShot = false;
            snapshot = snapshotTask(task, true);
        } else if (mode == 1) {
            snapshot = drawAppThemeSnapshot(task);
        } else if (mode != 2) {
            snapshot = null;
        } else {
            snapshot = null;
        }
        if (snapshot != null) {
            GraphicBuffer buffer = snapshot.getSnapshot();
            if (buffer.getWidth() == 0 || buffer.getHeight() == 0) {
                buffer.destroy();
                Slog.e(TAG, "getForgroundTaskSnapshot Invalid task snapshot dimensions " + buffer.getWidth() + "x" + buffer.getHeight());
                return null;
            }
            this.mCache.putForegroundSnapShot(task, snapshot);
            if (HW_SNAPSHOT) {
                this.mCache.putSnapshot(task, snapshot);
                this.mPersister.persistSnapshot(task.mTaskId, task.mUserId, snapshot);
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.wm.TaskSnapshotController.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        task.onSnapshotChanged(snapshot);
                    }
                });
                this.mService.addToWaitBlurTaskMap(findAppTokenForSnapshot(task), task, snapshot);
            }
        }
        return snapshot;
    }

    public void clearSnapshot() {
        this.mCache.clearSnapshot();
    }

    public void snapShotTaskForTvMultiWindow(Task task) {
        ActivityManager.TaskSnapshot snapshot;
        this.mIsTvMultiWindowSnapShot = true;
        int mode = getSnapshotMode(task);
        if (mode == 0) {
            snapshot = snapshotTask(task);
        } else if (mode == 1) {
            snapshot = drawAppThemeSnapshot(task);
        } else if (mode != 2) {
            snapshot = null;
        } else {
            return;
        }
        if (snapshot != null) {
            GraphicBuffer buffer = snapshot.getSnapshot();
            if (buffer.getWidth() == 0 || buffer.getHeight() == 0) {
                buffer.destroy();
                Slog.e(TAG, "Invalid task snapshot dimensions " + buffer.getWidth() + "x" + buffer.getHeight());
            } else {
                this.mCache.putSnapshot(task, snapshot);
                this.mPersister.persistSnapshot(task.mTaskId, task.mUserId, snapshot);
                task.onSnapshotChanged(snapshot);
            }
        }
        this.mIsTvMultiWindowSnapShot = false;
    }
}
