package com.android.server.wm;

import android.app.ActivityManager;
import android.app.WindowConfiguration;
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
import android.view.RemoteAnimationTarget;
import android.view.SurfaceControl;
import android.view.inputmethod.InputMethodManagerInternal;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.LocalServices;
import com.android.server.wm.SurfaceAnimator;
import com.android.server.wm.utils.InsetUtils;
import com.google.android.collect.Sets;
import java.io.PrintWriter;
import java.util.ArrayList;

public class RecentsAnimationController implements IBinder.DeathRecipient {
    private static final long FAILSAFE_DELAY = 1000;
    public static final int REORDER_KEEP_IN_PLACE = 0;
    public static final int REORDER_MOVE_TO_ORIGINAL_POSITION = 2;
    public static final int REORDER_MOVE_TO_TOP = 1;
    /* access modifiers changed from: private */
    public static final String TAG = RecentsAnimationController.class.getSimpleName();
    private SurfaceAnimator mAnim;
    /* access modifiers changed from: private */
    public final RecentsAnimationCallbacks mCallbacks;
    /* access modifiers changed from: private */
    public boolean mCanceled;
    private final IRecentsAnimationController mController = new IRecentsAnimationController.Stub() {
        public ActivityManager.TaskSnapshot screenshotTask(int taskId) {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (RecentsAnimationController.this.mService.getWindowManagerLock()) {
                    if (RecentsAnimationController.this.mCanceled) {
                        Binder.restoreCallingIdentity(token);
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
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
            r2 = com.android.server.wm.RecentsAnimationController.access$500(r5.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x004a, code lost:
            if (r6 == false) goto L_0x004f;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x004c, code lost:
            r4 = 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x004f, code lost:
            r4 = 2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0050, code lost:
            r2.onAnimationFinished(r4, true);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0053, code lost:
            android.os.Binder.restoreCallingIdentity(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0057, code lost:
            return;
         */
        public void finish(boolean moveHomeToTop) {
            Slog.d(RecentsAnimationController.TAG, "finish(" + moveHomeToTop + "): mCanceled=" + RecentsAnimationController.this.mCanceled);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (RecentsAnimationController.this.mService.getWindowManagerLock()) {
                    if (RecentsAnimationController.this.mCanceled) {
                        Binder.restoreCallingIdentity(token);
                    }
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        public void setAnimationTargetsBehindSystemBars(boolean behindSystemBars) throws RemoteException {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (RecentsAnimationController.this.mService.getWindowManagerLock()) {
                    for (int i = RecentsAnimationController.this.mPendingAnimations.size() - 1; i >= 0; i--) {
                        ((TaskAnimationAdapter) RecentsAnimationController.this.mPendingAnimations.get(i)).mTask.setCanAffectSystemUiFlags(behindSystemBars);
                    }
                    RecentsAnimationController.this.mService.mWindowPlacerLocked.requestTraversal();
                }
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        public void setInputConsumerEnabled(boolean enabled) {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (RecentsAnimationController.this.mService.getWindowManagerLock()) {
                    if (RecentsAnimationController.this.mCanceled) {
                        Binder.restoreCallingIdentity(token);
                        return;
                    }
                    boolean unused = RecentsAnimationController.this.mInputConsumerEnabled = enabled;
                    RecentsAnimationController.this.mService.mInputMonitor.updateInputWindowsLw(true);
                    RecentsAnimationController.this.mService.scheduleAnimationLocked();
                    Binder.restoreCallingIdentity(token);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        public void setSplitScreenMinimized(boolean minimized) {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (RecentsAnimationController.this.mService.getWindowManagerLock()) {
                    if (RecentsAnimationController.this.mCanceled) {
                        Binder.restoreCallingIdentity(token);
                        return;
                    }
                    boolean unused = RecentsAnimationController.this.mSplitScreenMinimized = minimized;
                    RecentsAnimationController.this.mService.checkSplitScreenMinimizedChanged(true);
                    Binder.restoreCallingIdentity(token);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
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
    };
    private final int mDisplayId;
    private final Runnable mFailsafeRunnable = new Runnable() {
        public final void run() {
            RecentsAnimationController.this.cancelAnimation(2, "failSafeRunnable");
        }
    };
    /* access modifiers changed from: private */
    public boolean mInputConsumerEnabled;
    private boolean mLinkedToDeathOfRunner;
    private Rect mMinimizedHomeBounds = new Rect();
    /* access modifiers changed from: private */
    public final ArrayList<TaskAnimationAdapter> mPendingAnimations = new ArrayList<>();
    private SurfaceControl mPendingLeash;
    private boolean mPendingStart = true;
    private IRecentsAnimationRunner mRunner;
    /* access modifiers changed from: private */
    public final WindowManagerService mService;
    /* access modifiers changed from: private */
    public boolean mSplitScreenMinimized;
    private AppWindowToken mTargetAppToken;
    /* access modifiers changed from: private */
    public final Rect mTmpRect = new Rect();

    public interface RecentsAnimationCallbacks {
        void onAnimationFinished(@ReorderMode int i, boolean z);
    }

    public @interface ReorderMode {
    }

    @VisibleForTesting
    class TaskAnimationAdapter implements AnimationAdapter {
        private final Rect mBounds = new Rect();
        /* access modifiers changed from: private */
        public SurfaceAnimator.OnAnimationFinishedCallback mCapturedFinishCallback;
        private SurfaceControl mCapturedLeash;
        private final boolean mIsRecentTaskInvisible;
        private final Point mPosition = new Point();
        private RemoteAnimationTarget mTarget;
        /* access modifiers changed from: private */
        public final Task mTask;

        TaskAnimationAdapter(Task task, boolean isRecentTaskInvisible) {
            this.mTask = task;
            this.mIsRecentTaskInvisible = isRecentTaskInvisible;
            WindowContainer container = this.mTask.getParent();
            container.getRelativePosition(this.mPosition);
            container.getBounds(this.mBounds);
        }

        /* access modifiers changed from: package-private */
        public RemoteAnimationTarget createRemoteAnimationApp() {
            WindowState mainWindow;
            AppWindowToken topApp = this.mTask.getTopVisibleAppToken();
            if (topApp != null) {
                mainWindow = topApp.findMainWindow();
            } else {
                mainWindow = null;
            }
            if (mainWindow == null) {
                return null;
            }
            Rect insets = new Rect(mainWindow.mContentInsets);
            InsetUtils.addInsets(insets, mainWindow.mAppToken.getLetterboxInsets());
            RemoteAnimationTarget remoteAnimationTarget = new RemoteAnimationTarget(this.mTask.mTaskId, 1, this.mCapturedLeash, !topApp.fillsParent(), mainWindow.mWinAnimator.mLastClipRect, insets, this.mTask.getPrefixOrderIndex(), this.mPosition, this.mBounds, this.mTask.getWindowConfiguration(), this.mIsRecentTaskInvisible);
            this.mTarget = remoteAnimationTarget;
            return this.mTarget;
        }

        public boolean getDetachWallpaper() {
            return false;
        }

        public boolean getShowWallpaper() {
            return false;
        }

        public int getBackgroundColor() {
            return 0;
        }

        public void startAnimation(SurfaceControl animationLeash, SurfaceControl.Transaction t, SurfaceAnimator.OnAnimationFinishedCallback finishCallback) {
            t.setLayer(animationLeash, this.mTask.getPrefixOrderIndex());
            t.setPosition(animationLeash, (float) this.mPosition.x, (float) this.mPosition.y);
            RecentsAnimationController.this.mTmpRect.set(this.mBounds);
            RecentsAnimationController.this.mTmpRect.offsetTo(0, 0);
            t.setWindowCrop(animationLeash, RecentsAnimationController.this.mTmpRect);
            this.mCapturedLeash = animationLeash;
            this.mCapturedFinishCallback = finishCallback;
        }

        public void onAnimationCancelled(SurfaceControl animationLeash) {
            RecentsAnimationController.this.cancelAnimation(2, "taskAnimationAdapterCanceled");
        }

        public long getDurationHint() {
            return 0;
        }

        public long getStatusBarTransitionsStartTime() {
            return SystemClock.uptimeMillis();
        }

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

        public void writeToProto(ProtoOutputStream proto) {
            long token = proto.start(1146756268034L);
            if (this.mTarget != null) {
                this.mTarget.writeToProto(proto, 1146756268033L);
            }
            proto.end(token);
        }
    }

    RecentsAnimationController(WindowManagerService service, IRecentsAnimationRunner remoteAnimationRunner, RecentsAnimationCallbacks callbacks, int displayId) {
        this.mService = service;
        this.mRunner = remoteAnimationRunner;
        this.mCallbacks = callbacks;
        this.mDisplayId = displayId;
    }

    public void initialize(int targetActivityType, SparseBooleanArray recentTaskIds) {
        DisplayContent dc = this.mService.mRoot.getDisplayContent(this.mDisplayId);
        ArrayList<Task> visibleTasks = dc.getVisibleTasks();
        int taskCount = visibleTasks.size();
        for (int i = 0; i < taskCount; i++) {
            Task task = visibleTasks.get(i);
            WindowConfiguration config = task.getWindowConfiguration();
            if (!(config.tasksAreFloating() || config.getWindowingMode() == 3 || config.getActivityType() == targetActivityType)) {
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
            dc.getDockedDividerController().getHomeStackBoundsInDockedMode(this.mMinimizedHomeBounds);
            this.mService.mWindowPlacerLocked.performSurfacePlacement();
        } catch (RemoteException e) {
            cancelAnimation(2, "initialize-failedToLinkToDeath");
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public AnimationAdapter addAnimation(Task task, boolean isRecentTaskInvisible) {
        String str = TAG;
        Slog.d(str, "addAnimation(" + task.getName() + ")");
        this.mAnim = new SurfaceAnimator(task, null, this.mService);
        TaskAnimationAdapter taskAdapter = new TaskAnimationAdapter(task, isRecentTaskInvisible);
        this.mAnim.startAnimation(task.getPendingTransaction(), taskAdapter, false);
        this.mPendingLeash = this.mAnim.mLeash;
        task.commitPendingTransaction();
        this.mPendingAnimations.add(taskAdapter);
        return taskAdapter;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void removeAnimation(TaskAnimationAdapter taskAdapter) {
        taskAdapter.mTask.setCanAffectSystemUiFlags(true);
        taskAdapter.mCapturedFinishCallback.onAnimationFinished(taskAdapter);
        this.mPendingAnimations.remove(taskAdapter);
    }

    /* access modifiers changed from: package-private */
    public void startAnimation() {
        Rect minimizedHomeBounds;
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
                if (appAnimations.isEmpty() != 0) {
                    cancelAnimation(2, "startAnimation-noAppWindows");
                    return;
                }
                RemoteAnimationTarget[] appTargets = (RemoteAnimationTarget[]) appAnimations.toArray(new RemoteAnimationTarget[appAnimations.size()]);
                this.mPendingStart = false;
                Rect contentInsets = null;
                if (this.mTargetAppToken == null || !this.mTargetAppToken.inSplitScreenSecondaryWindowingMode()) {
                    minimizedHomeBounds = null;
                } else {
                    minimizedHomeBounds = this.mMinimizedHomeBounds;
                }
                if (!(this.mTargetAppToken == null || this.mTargetAppToken.findMainWindow() == null)) {
                    contentInsets = this.mTargetAppToken.findMainWindow().mContentInsets;
                }
                this.mRunner.onAnimationStart(this.mController, appTargets, contentInsets, minimizedHomeBounds);
                SparseIntArray reasons = new SparseIntArray();
                reasons.put(1, 5);
                this.mService.mH.obtainMessage(47, reasons).sendToTarget();
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to start recents animation", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelAnimation(@ReorderMode int reorderMode, String reason) {
        cancelAnimation(reorderMode, false, reason);
    }

    /* access modifiers changed from: package-private */
    public void cancelAnimationSynchronously(@ReorderMode int reorderMode, String reason) {
        cancelAnimation(reorderMode, true, reason);
    }

    private void cancelAnimation(@ReorderMode int reorderMode, boolean runSynchronously, String reason) {
        synchronized (this.mService.getWindowManagerLock()) {
            if (!this.mCanceled) {
                this.mService.mH.removeCallbacks(this.mFailsafeRunnable);
                this.mCanceled = true;
                try {
                    this.mRunner.onAnimationCanceled();
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to cancel recents animation", e);
                }
            } else {
                return;
            }
        }
        this.mCallbacks.onAnimationFinished(reorderMode, runSynchronously);
    }

    /* access modifiers changed from: package-private */
    public void cleanupAnimation(@ReorderMode int reorderMode) {
        for (int i = this.mPendingAnimations.size() - 1; i >= 0; i--) {
            TaskAnimationAdapter taskAdapter = this.mPendingAnimations.get(i);
            if (reorderMode == 1 || reorderMode == 0) {
                if (taskAdapter.mTask.getDimmer().mDimState != null) {
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
        this.mService.mInputMonitor.updateInputWindowsLw(true);
        this.mService.destroyInputConsumer("recents_animation_input_consumer");
        if (this.mTargetAppToken == null) {
            return;
        }
        if (reorderMode == 1 || reorderMode == 0) {
            this.mService.mAppTransition.notifyAppTransitionFinishedLocked(this.mTargetAppToken.token);
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleFailsafe() {
        this.mService.mH.postDelayed(this.mFailsafeRunnable, 1000);
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

    public void binderDied() {
        cancelAnimation(2, "binderDied");
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
    public boolean hasInputConsumerForApp(AppWindowToken appToken) {
        return this.mInputConsumerEnabled && isAnimatingApp(appToken);
    }

    /* access modifiers changed from: package-private */
    public boolean updateInputConsumerForApp(InputConsumerImpl recentsAnimationInputConsumer, boolean hasFocus) {
        WindowState targetAppMainWindow;
        if (this.mTargetAppToken != null) {
            targetAppMainWindow = this.mTargetAppToken.findMainWindow();
        } else {
            targetAppMainWindow = null;
        }
        if (targetAppMainWindow == null) {
            return false;
        }
        targetAppMainWindow.getBounds(this.mTmpRect);
        recentsAnimationInputConsumer.mWindowHandle.hasFocus = hasFocus;
        recentsAnimationInputConsumer.mWindowHandle.touchableRegion.set(this.mTmpRect);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isTargetApp(AppWindowToken token) {
        return this.mTargetAppToken != null && token == this.mTargetAppToken;
    }

    private boolean isTargetOverWallpaper() {
        if (this.mTargetAppToken == null) {
            return false;
        }
        return this.mTargetAppToken.windowsCanBeWallpaperTarget();
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

    public void dump(PrintWriter pw, String prefix) {
        String innerPrefix = prefix + "  ";
        pw.print(prefix);
        pw.println(RecentsAnimationController.class.getSimpleName() + ":");
        pw.print(innerPrefix);
        pw.println("mPendingStart=" + this.mPendingStart);
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
