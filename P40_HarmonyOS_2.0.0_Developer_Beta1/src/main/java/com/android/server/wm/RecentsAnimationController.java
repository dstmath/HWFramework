package com.android.server.wm;

import android.app.ActivityManager;
import android.app.WindowConfiguration;
import android.common.HwFrameworkFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.proto.ProtoOutputStream;
import android.view.IRecentsAnimationController;
import android.view.IRecentsAnimationRunner;
import android.view.InputWindowHandle;
import android.view.RemoteAnimationTarget;
import android.view.SurfaceControl;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.LocalServices;
import com.android.server.inputmethod.InputMethodManagerInternal;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.wm.SurfaceAnimator;
import com.android.server.wm.WindowManagerInternal;
import com.android.server.wm.utils.InsetUtils;
import com.google.android.collect.Sets;
import java.io.PrintWriter;
import java.util.ArrayList;

public class RecentsAnimationController implements IBinder.DeathRecipient {
    private static final long FAILSAFE_DELAY = 1000;
    public static final int REORDER_KEEP_IN_PLACE = 0;
    public static final int REORDER_MOVE_TO_ORIGINAL_POSITION = 2;
    public static final int REORDER_MOVE_TO_TOP = 1;
    private static final String TAG = RecentsAnimationController.class.getSimpleName();
    final WindowManagerInternal.AppTransitionListener mAppTransitionListener = new WindowManagerInternal.AppTransitionListener() {
        /* class com.android.server.wm.RecentsAnimationController.AnonymousClass1 */

        @Override // com.android.server.wm.WindowManagerInternal.AppTransitionListener
        public int onAppTransitionStartingLocked(int transit, long duration, long statusBarAnimationStartTime, long statusBarAnimationDuration) {
            RecentsAnimationController.this.onTransitionStart();
            RecentsAnimationController.this.mService.mRoot.getDisplayContent(RecentsAnimationController.this.mDisplayId).mAppTransition.unregisterListener(this);
            return 0;
        }
    };
    private final RecentsAnimationCallbacks mCallbacks;
    private boolean mCancelOnNextTransitionStart;
    private boolean mCancelWithDeferredScreenshot;
    private boolean mCanceled;
    private final IRecentsAnimationController mController = new IRecentsAnimationController.Stub() {
        /* class com.android.server.wm.RecentsAnimationController.AnonymousClass2 */

        public ActivityManager.TaskSnapshot screenshotTask(int taskId) {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (RecentsAnimationController.this.mService.getWindowManagerLock()) {
                    if (RecentsAnimationController.this.mCanceled) {
                        return null;
                    }
                    for (int i = RecentsAnimationController.this.mPendingAnimations.size() - 1; i >= 0; i--) {
                        Task task = ((TaskAnimationAdapter) RecentsAnimationController.this.mPendingAnimations.get(i)).mTask;
                        if (task.mTaskId == taskId) {
                            TaskSnapshotController snapshotController = RecentsAnimationController.this.mService.mTaskSnapshotController;
                            ArraySet<Task> tasks = Sets.newArraySet(new Task[]{task});
                            snapshotController.snapshotTasks(tasks);
                            snapshotController.addSkipClosingAppSnapshotTasks(tasks);
                            ActivityManager.TaskSnapshot snapshot = snapshotController.getSnapshot(taskId, 0, false, false);
                            Binder.restoreCallingIdentity(token);
                            return snapshot;
                        }
                    }
                    Binder.restoreCallingIdentity(token);
                    return null;
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void finish(boolean moveHomeToTop, boolean sendUserLeaveHint) {
            int i;
            Slog.i(RecentsAnimationController.TAG, "finish(" + moveHomeToTop + "): mCanceled=" + RecentsAnimationController.this.mCanceled);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (RecentsAnimationController.this.mService.getWindowManagerLock()) {
                    if (RecentsAnimationController.this.mCanceled) {
                        return;
                    }
                }
                RecentsAnimationCallbacks recentsAnimationCallbacks = RecentsAnimationController.this.mCallbacks;
                if (moveHomeToTop) {
                    i = 1;
                } else {
                    i = 2;
                }
                recentsAnimationCallbacks.onAnimationFinished(i, true, sendUserLeaveHint);
                RecentsAnimationController.this.mService.mRoot.getDisplayContent(RecentsAnimationController.this.mDisplayId).mBoundsAnimationController.setAnimationType(1);
                Binder.restoreCallingIdentity(token);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setAnimationTargetsBehindSystemBars(boolean behindSystemBars) throws RemoteException {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (RecentsAnimationController.this.mService.getWindowManagerLock()) {
                    for (int i = RecentsAnimationController.this.mPendingAnimations.size() - 1; i >= 0; i--) {
                        Task task = ((TaskAnimationAdapter) RecentsAnimationController.this.mPendingAnimations.get(i)).mTask;
                        if (task.getActivityType() != RecentsAnimationController.this.mTargetActivityType) {
                            task.setCanAffectSystemUiFlags(behindSystemBars);
                        }
                    }
                    RecentsAnimationController.this.mService.mWindowPlacerLocked.requestTraversal();
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setInputConsumerEnabled(boolean enabled) {
            String str = RecentsAnimationController.TAG;
            Slog.d(str, "setInputConsumerEnabled(" + enabled + "): mCanceled=" + RecentsAnimationController.this.mCanceled);
            int callingUid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (RecentsAnimationController.this.mService.getWindowManagerLock()) {
                    if (!RecentsAnimationController.this.mCanceled) {
                        RecentsAnimationController.this.mInputConsumerEnabled = enabled;
                        InputMonitor inputMonitor = RecentsAnimationController.this.mService.mRoot.getDisplayContent(RecentsAnimationController.this.mDisplayId).getInputMonitor();
                        inputMonitor.updateInputWindowsLw(true);
                        if (RecentsAnimationController.this.mService.mAtmService.mRecentTasks.isCallerRecents(callingUid)) {
                            inputMonitor.updateInputWindowsImmediately();
                        }
                        RecentsAnimationController.this.mService.scheduleAnimationLocked();
                        Binder.restoreCallingIdentity(token);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setSplitScreenMinimized(boolean minimized) {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (RecentsAnimationController.this.mService.getWindowManagerLock()) {
                    if (!RecentsAnimationController.this.mCanceled) {
                        RecentsAnimationController.this.mSplitScreenMinimized = minimized;
                        RecentsAnimationController.this.mService.checkSplitScreenMinimizedChanged(true);
                        Binder.restoreCallingIdentity(token);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void hideCurrentInputMethod() {
            long token = Binder.clearCallingIdentity();
            try {
                InputMethodManagerInternal inputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
                if (inputMethodManagerInternal != null) {
                    inputMethodManagerInternal.hideCurrentInputMethod();
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setCancelWithDeferredScreenshot(boolean screenshot) {
            synchronized (RecentsAnimationController.this.mLock) {
                RecentsAnimationController.this.setCancelWithDeferredScreenshotLocked(screenshot);
            }
        }

        public void cleanupScreenshot() {
            synchronized (RecentsAnimationController.this.mLock) {
                if (RecentsAnimationController.this.mRecentScreenshotAnimator != null) {
                    RecentsAnimationController.this.mRecentScreenshotAnimator.cancelAnimation();
                    RecentsAnimationController.this.mRecentScreenshotAnimator = null;
                }
            }
        }
    };
    private final int mDisplayId;
    private final Runnable mFailsafeRunnable = new Runnable() {
        /* class com.android.server.wm.$$Lambda$RecentsAnimationController$4jQqaDgSmtGCjbUJiVoDh_jr9rY */

        @Override // java.lang.Runnable
        public final void run() {
            RecentsAnimationController.this.lambda$new$0$RecentsAnimationController();
        }
    };
    private boolean mInputConsumerEnabled;
    private boolean mLinkedToDeathOfRunner;
    final Object mLock = new Object();
    private Rect mMinimizedHomeBounds = new Rect();
    private final ArrayList<TaskAnimationAdapter> mPendingAnimations = new ArrayList<>();
    private boolean mPendingStart = true;
    SurfaceAnimator mRecentScreenshotAnimator;
    private IRecentsAnimationRunner mRunner;
    private final WindowManagerService mService;
    private boolean mSplitScreenMinimized;
    private final StatusBarManagerInternal mStatusBar;
    private int mTargetActivityType;
    private AppWindowToken mTargetAppToken;
    private final Rect mTmpRect = new Rect();

    public interface RecentsAnimationCallbacks {
        void onAnimationFinished(@ReorderMode int i, boolean z, boolean z2);
    }

    public @interface ReorderMode {
    }

    public /* synthetic */ void lambda$new$0$RecentsAnimationController() {
        cancelAnimation(2, "failSafeRunnable");
    }

    RecentsAnimationController(WindowManagerService service, IRecentsAnimationRunner remoteAnimationRunner, RecentsAnimationCallbacks callbacks, int displayId) {
        this.mService = service;
        this.mRunner = remoteAnimationRunner;
        this.mCallbacks = callbacks;
        this.mDisplayId = displayId;
        this.mStatusBar = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
    }

    public void initialize(int targetActivityType, SparseBooleanArray recentTaskIds) {
        initialize(this.mService.mRoot.getDisplayContent(this.mDisplayId), targetActivityType, recentTaskIds);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void initialize(DisplayContent dc, int targetActivityType, SparseBooleanArray recentTaskIds) {
        this.mTargetActivityType = targetActivityType;
        dc.mAppTransition.registerListenerLocked(this.mAppTransitionListener);
        ArrayList<Task> visibleTasks = dc.getVisibleTasks();
        TaskStack targetStack = dc.getStack(0, targetActivityType);
        if (targetStack != null) {
            for (int i = targetStack.getChildCount() - 1; i >= 0; i--) {
                Task t = (Task) targetStack.getChildAt(i);
                if (!visibleTasks.contains(t)) {
                    visibleTasks.add(t);
                }
            }
        }
        int taskCount = visibleTasks.size();
        for (int i2 = 0; i2 < taskCount; i2++) {
            Task task = visibleTasks.get(i2);
            WindowConfiguration config = task.getWindowConfiguration();
            if ((!config.tasksAreFloating() || config.inHwMagicWindowingMode()) && config.getWindowingMode() != 3) {
                addAnimation(task, !recentTaskIds.get(task.mTaskId));
            }
        }
        if (this.mPendingAnimations.isEmpty()) {
            cancelAnimation(2, "initialize-noVisibleTasks");
            return;
        }
        try {
            linkToDeathOfRunner();
            AppWindowToken recentsComponentAppToken = ((Task) dc.getStack(0, targetActivityType).getTopChild()).getTopFullscreenAppToken();
            if (recentsComponentAppToken != null) {
                this.mTargetAppToken = recentsComponentAppToken;
                if (recentsComponentAppToken.windowsCanBeWallpaperTarget()) {
                    dc.pendingLayoutChanges |= 4;
                    dc.setLayoutNeeded();
                }
            }
            TaskStack dockedStack = dc.getSplitScreenPrimaryStackIgnoringVisibility();
            dc.getDockedDividerController().getHomeStackBoundsInDockedMode(dc.getConfiguration(), dockedStack == null ? -1 : dockedStack.getDockSide(), this.mMinimizedHomeBounds);
            this.mService.mWindowPlacerLocked.performSurfacePlacement();
            this.mStatusBar.onRecentsAnimationStateChanged(true);
        } catch (RemoteException e) {
            cancelAnimation(2, "initialize-failedToLinkToDeath");
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public AnimationAdapter addAnimation(Task task, boolean isRecentTaskInvisible) {
        String str = TAG;
        Slog.i(str, "addAnimation(" + task.getName() + ")");
        TaskAnimationAdapter taskAdapter = new TaskAnimationAdapter(task, isRecentTaskInvisible);
        task.startAnimation(task.getPendingTransaction(), taskAdapter, false);
        task.commitPendingTransaction();
        this.mPendingAnimations.add(taskAdapter);
        return taskAdapter;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void removeAnimation(TaskAnimationAdapter taskAdapter) {
        String str = TAG;
        Slog.i(str, "removeAnimation(" + taskAdapter.mTask.mTaskId + ")");
        taskAdapter.mTask.setCanAffectSystemUiFlags(true);
        if (taskAdapter.mCapturedFinishCallback == null) {
            Slog.i(TAG, "removeAnimation: animationFinishedCallback is null");
        } else {
            taskAdapter.mCapturedFinishCallback.onAnimationFinished(taskAdapter);
        }
        this.mPendingAnimations.remove(taskAdapter);
    }

    /* access modifiers changed from: package-private */
    public void startAnimation() {
        Rect minimizedHomeBounds;
        Rect contentInsets;
        Slog.i(TAG, "startAnimation(): mPendingStart=" + this.mPendingStart + " mCanceled=" + this.mCanceled);
        if (this.mPendingStart && !this.mCanceled) {
            try {
                ArrayList<RemoteAnimationTarget> appAnimations = new ArrayList<>();
                for (int i = this.mPendingAnimations.size() - 1; i >= 0; i--) {
                    TaskAnimationAdapter taskAdapter = this.mPendingAnimations.get(i);
                    RemoteAnimationTarget target = taskAdapter.createRemoteAnimationApp();
                    if (target != null) {
                        appAnimations.add(target);
                    } else {
                        removeAnimation(taskAdapter);
                    }
                }
                if (appAnimations.isEmpty()) {
                    cancelAnimation(2, "startAnimation-noAppWindows");
                    return;
                }
                RemoteAnimationTarget[] appTargets = (RemoteAnimationTarget[]) appAnimations.toArray(new RemoteAnimationTarget[appAnimations.size()]);
                this.mPendingStart = false;
                this.mService.mRoot.getDisplayContent(this.mDisplayId).performLayout(false, false);
                if (this.mTargetAppToken == null || !this.mTargetAppToken.inSplitScreenSecondaryWindowingMode()) {
                    minimizedHomeBounds = null;
                } else {
                    minimizedHomeBounds = this.mMinimizedHomeBounds;
                }
                if (this.mTargetAppToken == null || this.mTargetAppToken.findMainWindow() == null) {
                    this.mService.getStableInsets(this.mDisplayId, this.mTmpRect);
                    contentInsets = this.mTmpRect;
                } else {
                    contentInsets = this.mTargetAppToken.findMainWindow().getContentInsets();
                }
                this.mRunner.onAnimationStart(this.mController, appTargets, contentInsets, minimizedHomeBounds);
                Slog.i(TAG, "startAnimation(): Notify animation start:");
                SparseIntArray reasons = new SparseIntArray();
                reasons.put(1, 5);
                this.mService.mAtmInternal.notifyAppTransitionStarting(reasons, SystemClock.uptimeMillis());
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to start recents animation", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelAnimation(@ReorderMode int reorderMode, String reason) {
        cancelAnimation(reorderMode, false, false, reason);
    }

    /* access modifiers changed from: package-private */
    public void cancelAnimationSynchronously(@ReorderMode int reorderMode, String reason) {
        cancelAnimation(reorderMode, true, false, reason);
    }

    /* access modifiers changed from: package-private */
    public void cancelAnimationWithScreenShot() {
        cancelAnimation(0, true, true, "stackOrderChanged");
    }

    private void cancelAnimation(@ReorderMode int reorderMode, boolean runSynchronously, boolean screenshot, String reason) {
        String str = TAG;
        Slog.i(str, "cancelAnimation(): reason=" + reason + " runSynchronously=" + runSynchronously);
        synchronized (this.mService.getWindowManagerLock()) {
            if (!this.mCanceled) {
                this.mService.mH.removeCallbacks(this.mFailsafeRunnable);
                this.mCanceled = true;
                if (screenshot) {
                    try {
                        screenshotRecentTask(this.mPendingAnimations.get(0).mTask, reorderMode, runSynchronously);
                        this.mRunner.onAnimationCanceled(true);
                    } catch (RemoteException e) {
                        Slog.e(TAG, "Failed to cancel recents animation", e);
                    }
                } else {
                    this.mRunner.onAnimationCanceled(false);
                    this.mCallbacks.onAnimationFinished(reorderMode, runSynchronously, false);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelOnNextTransitionStart() {
        this.mCancelOnNextTransitionStart = true;
    }

    /* access modifiers changed from: package-private */
    public void setCancelWithDeferredScreenshotLocked(boolean screenshot) {
        this.mCancelWithDeferredScreenshot = screenshot;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldCancelWithDeferredScreenshot() {
        return this.mCancelWithDeferredScreenshot;
    }

    /* access modifiers changed from: package-private */
    public void onTransitionStart() {
        if (!this.mCanceled && this.mCancelOnNextTransitionStart) {
            this.mCancelOnNextTransitionStart = false;
            cancelAnimationWithScreenShot();
        }
    }

    /* access modifiers changed from: package-private */
    public void screenshotRecentTask(Task task, @ReorderMode int reorderMode, boolean runSynchronously) {
        TaskScreenshotAnimatable animatable = TaskScreenshotAnimatable.create(task);
        if (animatable != null) {
            this.mRecentScreenshotAnimator = new SurfaceAnimator(animatable, new Runnable(reorderMode, runSynchronously) {
                /* class com.android.server.wm.$$Lambda$RecentsAnimationController$UtmXbQuPny5O24HGUrj6wbSP2A */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ boolean f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    RecentsAnimationController.this.lambda$screenshotRecentTask$1$RecentsAnimationController(this.f$1, this.f$2);
                }
            }, this.mService);
            this.mRecentScreenshotAnimator.transferAnimation(task.mSurfaceAnimator);
        }
    }

    public /* synthetic */ void lambda$screenshotRecentTask$1$RecentsAnimationController(int reorderMode, boolean runSynchronously) {
        this.mCallbacks.onAnimationFinished(reorderMode, runSynchronously, false);
    }

    /* access modifiers changed from: package-private */
    public void cleanupAnimation(@ReorderMode int reorderMode) {
        Slog.i(TAG, "cleanupAnimation(): Notify animation finished mPendingAnimations=" + this.mPendingAnimations.size() + " reorderMode=" + reorderMode);
        for (int i = this.mPendingAnimations.size() - 1; i >= 0; i--) {
            TaskAnimationAdapter taskAdapter = this.mPendingAnimations.get(i);
            if (reorderMode == 1 || reorderMode == 0) {
                if (reorderMode == 1 && taskAdapter.mTask.getDimmer().mDimState != null) {
                    taskAdapter.mTask.getPendingTransaction().hide(taskAdapter.mTask.getDimmer().mDimState.mDimLayer);
                }
                taskAdapter.mTask.dontAnimateDimExit();
            }
            removeAnimation(taskAdapter);
        }
        this.mService.mH.removeCallbacks(this.mFailsafeRunnable);
        unlinkToDeathOfRunner();
        this.mRunner = null;
        this.mCanceled = true;
        SurfaceAnimator surfaceAnimator = this.mRecentScreenshotAnimator;
        if (surfaceAnimator != null) {
            surfaceAnimator.cancelAnimation();
            this.mRecentScreenshotAnimator = null;
        }
        this.mService.mRoot.getDisplayContent(this.mDisplayId).getInputMonitor().updateInputWindowsLw(true);
        if (this.mTargetAppToken != null && (reorderMode == 1 || reorderMode == 0)) {
            this.mService.mRoot.getDisplayContent(this.mDisplayId).mAppTransition.notifyAppTransitionFinishedLocked(this.mTargetAppToken.token);
        }
        this.mStatusBar.onRecentsAnimationStateChanged(false);
    }

    /* access modifiers changed from: package-private */
    public void scheduleFailsafe() {
        this.mService.mH.postDelayed(this.mFailsafeRunnable, FAILSAFE_DELAY);
    }

    private void linkToDeathOfRunner() throws RemoteException {
        if (!this.mLinkedToDeathOfRunner) {
            this.mRunner.asBinder().linkToDeath(this, 0);
            this.mLinkedToDeathOfRunner = true;
        }
    }

    private void unlinkToDeathOfRunner() {
        if (this.mLinkedToDeathOfRunner) {
            this.mRunner.asBinder().unlinkToDeath(this, 0);
            this.mLinkedToDeathOfRunner = false;
        }
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        cancelAnimation(2, "binderDied");
        synchronized (this.mService.getWindowManagerLock()) {
            this.mService.mRoot.getDisplayContent(this.mDisplayId).getInputMonitor().destroyInputConsumer("recents_animation_input_consumer");
        }
    }

    /* access modifiers changed from: package-private */
    public void checkAnimationReady(WallpaperController wallpaperController) {
        if (this.mPendingStart) {
            if (!isTargetOverWallpaper() || (wallpaperController.getWallpaperTarget() != null && wallpaperController.wallpaperTransitionReady())) {
                this.mService.getRecentsAnimationController().startAnimation();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSplitScreenMinimized() {
        return this.mSplitScreenMinimized;
    }

    /* access modifiers changed from: package-private */
    public boolean isWallpaperVisible(WindowState w) {
        return w != null && w.mAppToken != null && this.mTargetAppToken == w.mAppToken && isTargetOverWallpaper();
    }

    /* access modifiers changed from: package-private */
    public boolean shouldApplyInputConsumer(AppWindowToken appToken) {
        return this.mInputConsumerEnabled && this.mTargetAppToken != appToken && isAnimatingApp(appToken);
    }

    /* access modifiers changed from: package-private */
    public boolean updateInputConsumerForApp(InputWindowHandle inputWindowHandle, boolean hasFocus) {
        WindowState targetAppMainWindow;
        AppWindowToken appWindowToken = this.mTargetAppToken;
        if (appWindowToken != null) {
            targetAppMainWindow = appWindowToken.findMainWindow();
        } else {
            targetAppMainWindow = null;
        }
        if (targetAppMainWindow == null) {
            return false;
        }
        targetAppMainWindow.getBounds(this.mTmpRect);
        inputWindowHandle.hasFocus = hasFocus;
        inputWindowHandle.touchableRegion.set(this.mTmpRect);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isTargetApp(AppWindowToken token) {
        AppWindowToken appWindowToken = this.mTargetAppToken;
        return appWindowToken != null && token == appWindowToken;
    }

    private boolean isTargetOverWallpaper() {
        AppWindowToken appWindowToken = this.mTargetAppToken;
        if (appWindowToken == null) {
            return false;
        }
        return appWindowToken.windowsCanBeWallpaperTarget();
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimatingTask(Task task) {
        for (int i = this.mPendingAnimations.size() - 1; i >= 0; i--) {
            if (task == this.mPendingAnimations.get(i).mTask) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimatingTaskStack(TaskStack taskStack) {
        if (taskStack == null) {
            return false;
        }
        for (int i = this.mPendingAnimations.size() - 1; i >= 0; i--) {
            if (taskStack == this.mPendingAnimations.get(i).mTask.mStack) {
                return true;
            }
        }
        return false;
    }

    private boolean isAnimatingApp(AppWindowToken appToken) {
        for (int i = this.mPendingAnimations.size() - 1; i >= 0; i--) {
            Task task = this.mPendingAnimations.get(i).mTask;
            for (int j = task.getChildCount() - 1; j >= 0; j--) {
                if (((AppWindowToken) task.getChildAt(j)) == appToken) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public class TaskAnimationAdapter implements AnimationAdapter {
        private final Rect mBounds = new Rect();
        private SurfaceAnimator.OnAnimationFinishedCallback mCapturedFinishCallback;
        private SurfaceControl mCapturedLeash;
        private final boolean mIsRecentTaskInvisible;
        private final Point mPosition = new Point();
        private RemoteAnimationTarget mTarget;
        private final Task mTask;

        TaskAnimationAdapter(Task task, boolean isRecentTaskInvisible) {
            this.mTask = task;
            this.mIsRecentTaskInvisible = isRecentTaskInvisible;
            WindowContainer container = this.mTask.getParent();
            container.getRelativeDisplayedPosition(this.mPosition);
            this.mBounds.set(container.getDisplayedBounds());
        }

        /* access modifiers changed from: package-private */
        public RemoteAnimationTarget createRemoteAnimationApp() {
            WindowState mainWindow;
            int mode;
            AppWindowToken topApp = this.mTask.getTopVisibleAppToken();
            if (topApp != null) {
                mainWindow = topApp.findMainWindow();
            } else {
                mainWindow = null;
            }
            if (mainWindow == null) {
                return null;
            }
            Rect insets = new Rect();
            mainWindow.getContentInsets(insets);
            if (HwFrameworkFactory.getHwApsImpl() != null) {
                HwFrameworkFactory.getHwApsImpl().scaleInsetsWhenSdrUpInRog(mainWindow.getOwningPackage(), insets);
            }
            InsetUtils.addInsets(insets, mainWindow.mAppToken.getLetterboxInsets());
            if (topApp.getActivityType() == RecentsAnimationController.this.mTargetActivityType) {
                mode = 0;
            } else {
                mode = 1;
            }
            this.mTarget = new RemoteAnimationTarget(this.mTask.mTaskId, mode, this.mCapturedLeash, !topApp.fillsParent(), mainWindow.mWinAnimator.mLastClipRect, insets, this.mTask.getPrefixOrderIndex(), this.mPosition, this.mBounds, this.mTask.getWindowConfiguration(), this.mIsRecentTaskInvisible, (SurfaceControl) null, (Rect) null);
            return this.mTarget;
        }

        @Override // com.android.server.wm.AnimationAdapter
        public boolean getShowWallpaper() {
            return false;
        }

        @Override // com.android.server.wm.AnimationAdapter
        public int getBackgroundColor() {
            return 0;
        }

        @Override // com.android.server.wm.AnimationAdapter
        public void startAnimation(SurfaceControl animationLeash, SurfaceControl.Transaction t, SurfaceAnimator.OnAnimationFinishedCallback finishCallback) {
            t.setLayer(animationLeash, this.mTask.getPrefixOrderIndex());
            t.setPosition(animationLeash, (float) this.mPosition.x, (float) this.mPosition.y);
            RecentsAnimationController.this.mTmpRect.set(this.mBounds);
            RecentsAnimationController.this.mTmpRect.offsetTo(0, 0);
            t.setWindowCrop(animationLeash, RecentsAnimationController.this.mTmpRect);
            this.mCapturedLeash = animationLeash;
            this.mCapturedFinishCallback = finishCallback;
        }

        @Override // com.android.server.wm.AnimationAdapter
        public void onAnimationCancelled(SurfaceControl animationLeash) {
            RecentsAnimationController.this.cancelAnimation(2, "taskAnimationAdapterCanceled");
        }

        @Override // com.android.server.wm.AnimationAdapter
        public long getDurationHint() {
            return 0;
        }

        @Override // com.android.server.wm.AnimationAdapter
        public long getStatusBarTransitionsStartTime() {
            return SystemClock.uptimeMillis();
        }

        @Override // com.android.server.wm.AnimationAdapter
        public void dump(PrintWriter pw, String prefix) {
            pw.print(prefix);
            pw.println("task=" + this.mTask);
            if (this.mTarget != null) {
                pw.print(prefix);
                pw.println("Target:");
                RemoteAnimationTarget remoteAnimationTarget = this.mTarget;
                remoteAnimationTarget.dump(pw, prefix + "  ");
            } else {
                pw.print(prefix);
                pw.println("Target: null");
            }
            pw.println("mIsRecentTaskInvisible=" + this.mIsRecentTaskInvisible);
            pw.println("mPosition=" + this.mPosition);
            pw.println("mBounds=" + this.mBounds);
            pw.println("mIsRecentTaskInvisible=" + this.mIsRecentTaskInvisible);
        }

        @Override // com.android.server.wm.AnimationAdapter
        public void writeToProto(ProtoOutputStream proto) {
            long token = proto.start(1146756268034L);
            RemoteAnimationTarget remoteAnimationTarget = this.mTarget;
            if (remoteAnimationTarget != null) {
                remoteAnimationTarget.writeToProto(proto, 1146756268033L);
            }
            proto.end(token);
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        String innerPrefix = prefix + "  ";
        pw.print(prefix);
        pw.println(RecentsAnimationController.class.getSimpleName() + ":");
        pw.print(innerPrefix);
        pw.println("mPendingStart=" + this.mPendingStart);
        pw.print(innerPrefix);
        pw.println("mPendingAnimations=" + this.mPendingAnimations.size());
        pw.print(innerPrefix);
        pw.println("mCanceled=" + this.mCanceled);
        pw.print(innerPrefix);
        pw.println("mInputConsumerEnabled=" + this.mInputConsumerEnabled);
        pw.print(innerPrefix);
        pw.println("mSplitScreenMinimized=" + this.mSplitScreenMinimized);
        pw.print(innerPrefix);
        pw.println("mTargetAppToken=" + this.mTargetAppToken);
        pw.print(innerPrefix);
        pw.println("isTargetOverWallpaper=" + isTargetOverWallpaper());
    }
}
