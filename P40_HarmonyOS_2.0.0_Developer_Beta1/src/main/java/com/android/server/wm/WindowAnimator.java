package com.android.server.wm;

import android.content.Context;
import android.hardware.display.HwFoldScreenState;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.view.Choreographer;
import android.view.SurfaceControl;
import com.android.server.AnimationThread;
import com.android.server.LocalServices;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.WindowManagerService;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import java.io.PrintWriter;
import java.util.ArrayList;

public class WindowAnimator {
    private static final boolean PROP_FOLD_SWITCH_DEBUG = SystemProperties.getBoolean("persist.debug.fold_switch", true);
    private static final String TAG = "WindowManager";
    private final ArrayList<Runnable> mAfterPrepareSurfacesRunnables = new ArrayList<>();
    private boolean mAnimating;
    final Choreographer.FrameCallback mAnimationFrameCallback;
    private boolean mAnimationFrameCallbackScheduled;
    int mBulkUpdateParams = 0;
    private Choreographer mChoreographer;
    final Context mContext;
    long mCurrentTime;
    SparseArray<DisplayContentsAnimator> mDisplayContentsAnimators = new SparseArray<>(2);
    private HwFoldScreenManagerInternal mFsmInternal;
    private boolean mInExecuteAfterPrepareSurfacesRunnables;
    private boolean mInitialized = false;
    boolean mIsLazying = false;
    private boolean mLastRootAnimating;
    Object mLastWindowFreezeSource;
    final WindowManagerPolicy mPolicy;
    private boolean mRemoveReplacedWindows = false;
    final WindowManagerService mService;
    private final SurfaceControl.Transaction mTransaction = new SurfaceControl.Transaction();
    int offsetLayer = 0;

    WindowAnimator(WindowManagerService service) {
        this.mService = service;
        this.mContext = service.mContext;
        this.mPolicy = service.mPolicy;
        AnimationThread.getHandler().runWithScissors(new Runnable() {
            /* class com.android.server.wm.$$Lambda$WindowAnimator$U3Fu5_RzEyNo8Jt6zTb2ozdXiqM */

            @Override // java.lang.Runnable
            public final void run() {
                WindowAnimator.this.lambda$new$0$WindowAnimator();
            }
        }, 0);
        this.mAnimationFrameCallback = new Choreographer.FrameCallback() {
            /* class com.android.server.wm.$$Lambda$WindowAnimator$ddXU8gK8rmDqri0OZVMNa3Y4GHk */

            @Override // android.view.Choreographer.FrameCallback
            public final void doFrame(long j) {
                WindowAnimator.this.lambda$new$1$WindowAnimator(j);
            }
        };
    }

    public /* synthetic */ void lambda$new$0$WindowAnimator() {
        this.mChoreographer = Choreographer.getSfInstance();
    }

    /* JADX INFO: finally extract failed */
    public /* synthetic */ void lambda$new$1$WindowAnimator(long frameTimeNs) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mAnimationFrameCallbackScheduled = false;
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        animate(frameTimeNs);
    }

    /* access modifiers changed from: package-private */
    public void addDisplayLocked(int displayId) {
        getDisplayContentsAnimatorLocked(displayId);
    }

    /* access modifiers changed from: package-private */
    public void removeDisplayLocked(int displayId) {
        DisplayContentsAnimator displayAnimator = this.mDisplayContentsAnimators.get(displayId);
        if (!(displayAnimator == null || displayAnimator.mScreenRotationAnimation == null)) {
            displayAnimator.mScreenRotationAnimation.kill();
            displayAnimator.mScreenRotationAnimation = null;
        }
        this.mDisplayContentsAnimators.delete(displayId);
    }

    /* access modifiers changed from: package-private */
    public void ready() {
        this.mInitialized = true;
    }

    private void animate(long frameTimeNs) {
        String str;
        WindowManagerService windowManagerService;
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mInitialized) {
                    scheduleAnimation();
                } else {
                    return;
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mCurrentTime = frameTimeNs / 1000000;
                this.mBulkUpdateParams = 4;
                this.mAnimating = false;
                this.mIsLazying = false;
                this.mService.openSurfaceTransaction();
                try {
                    AccessibilityController accessibilityController = this.mService.mAccessibilityController;
                    int numDisplays = this.mDisplayContentsAnimators.size();
                    for (int i = 0; i < numDisplays; i++) {
                        DisplayContent dc = this.mService.mRoot.getDisplayContent(this.mDisplayContentsAnimators.keyAt(i));
                        DisplayContentsAnimator displayAnimator = this.mDisplayContentsAnimators.valueAt(i);
                        ScreenRotationAnimation screenRotationAnimation = displayAnimator.mScreenRotationAnimation;
                        if (screenRotationAnimation != null && screenRotationAnimation.isAnimating()) {
                            if (screenRotationAnimation.stepAnimationLocked(this.mCurrentTime)) {
                                setAnimating(true);
                            } else {
                                this.mBulkUpdateParams |= 1;
                                screenRotationAnimation.kill();
                                displayAnimator.mScreenRotationAnimation = null;
                                if (accessibilityController != null) {
                                    accessibilityController.onRotationChangedLocked(dc);
                                }
                                if (HwFoldScreenState.isFoldScreenDevice() && this.mService.isFoldRotationFreezed()) {
                                    Slog.i(TAG, "screen fold enter anim done: send unfreeze fold rotation msg");
                                    this.mService.mH.removeMessages(WindowManagerService.H.UNFREEZE_FOLD_ROTATION);
                                    this.mService.mH.sendEmptyMessage(WindowManagerService.H.UNFREEZE_FOLD_ROTATION);
                                }
                                handleResumeDispModeChange(true);
                            }
                        }
                        dc.updateWindowsForAnimator();
                        dc.updateBackgroundForAnimator();
                        dc.prepareSurfaces();
                    }
                    for (int i2 = 0; i2 < numDisplays; i2++) {
                        int displayId = this.mDisplayContentsAnimators.keyAt(i2);
                        DisplayContent dc2 = this.mService.mRoot.getDisplayContent(displayId);
                        dc2.checkAppWindowsReadyToShow();
                        ScreenRotationAnimation screenRotationAnimation2 = this.mDisplayContentsAnimators.valueAt(i2).mScreenRotationAnimation;
                        if (screenRotationAnimation2 != null) {
                            screenRotationAnimation2.updateSurfaces(this.mTransaction);
                        }
                        orAnimating(dc2.getDockedDividerController().animate(this.mCurrentTime));
                        if (accessibilityController != null) {
                            accessibilityController.drawMagnifiedRegionBorderIfNeededLocked(displayId);
                        }
                    }
                    if (!this.mAnimating && !this.mIsLazying) {
                        cancelAnimation();
                    }
                    if (this.mService.mWatermark != null) {
                        this.mService.mWatermark.drawIfNeeded();
                    }
                    SurfaceControl.mergeToGlobalTransaction(this.mTransaction);
                    windowManagerService = this.mService;
                    str = "WindowAnimator";
                } catch (RuntimeException e) {
                    Slog.wtf(TAG, "Unhandled exception in Window Manager", e);
                    windowManagerService = this.mService;
                    str = "WindowAnimator";
                } catch (Throwable th) {
                    this.mService.closeSurfaceTransaction("WindowAnimator");
                    throw th;
                }
                windowManagerService.closeSurfaceTransaction(str);
                boolean hasPendingLayoutChanges = this.mService.mRoot.hasPendingLayoutChanges(this);
                boolean doRequest = false;
                if (this.mBulkUpdateParams != 0) {
                    doRequest = this.mService.mRoot.copyAnimToLayoutParams();
                }
                if (hasPendingLayoutChanges || doRequest) {
                    this.mService.mWindowPlacerLocked.requestTraversal();
                }
                boolean rootAnimating = this.mService.mRoot.isSelfOrChildAnimating();
                if (rootAnimating && !this.mLastRootAnimating) {
                    this.mService.mTaskSnapshotController.setPersisterPaused(true);
                    Trace.asyncTraceBegin(32, "animating", 0);
                }
                if (!rootAnimating && this.mLastRootAnimating) {
                    this.mService.mWindowPlacerLocked.requestTraversal();
                    this.mService.mTaskSnapshotController.setPersisterPaused(false);
                    Trace.asyncTraceEnd(32, "animating", 0);
                }
                this.mLastRootAnimating = rootAnimating;
                if (this.mRemoveReplacedWindows) {
                    this.mService.mRoot.removeReplacedWindows();
                    this.mRemoveReplacedWindows = false;
                }
                this.mService.destroyPreservedSurfaceLocked();
                executeAfterPrepareSurfacesRunnables();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private static String bulkUpdateParamsToString(int bulkUpdateParams) {
        StringBuilder builder = new StringBuilder(128);
        if ((bulkUpdateParams & 1) != 0) {
            builder.append(" UPDATE_ROTATION");
        }
        if ((bulkUpdateParams & 4) != 0) {
            builder.append(" ORIENTATION_CHANGE_COMPLETE");
        }
        return builder.toString();
    }

    public void dumpLocked(PrintWriter pw, String prefix, boolean dumpAll) {
        String subPrefix = "  " + prefix;
        String subSubPrefix = "  " + subPrefix;
        for (int i = 0; i < this.mDisplayContentsAnimators.size(); i++) {
            pw.print(prefix);
            pw.print("DisplayContentsAnimator #");
            pw.print(this.mDisplayContentsAnimators.keyAt(i));
            pw.println(":");
            DisplayContentsAnimator displayAnimator = this.mDisplayContentsAnimators.valueAt(i);
            this.mService.mRoot.getDisplayContent(this.mDisplayContentsAnimators.keyAt(i)).dumpWindowAnimators(pw, subPrefix);
            if (displayAnimator.mScreenRotationAnimation != null) {
                pw.print(subPrefix);
                pw.println("mScreenRotationAnimation:");
                displayAnimator.mScreenRotationAnimation.printTo(subSubPrefix, pw);
            } else if (dumpAll) {
                pw.print(subPrefix);
                pw.println("no ScreenRotationAnimation ");
            }
            pw.println();
        }
        pw.println();
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mCurrentTime=");
            pw.println(TimeUtils.formatUptime(this.mCurrentTime));
        }
        if (this.mBulkUpdateParams != 0) {
            pw.print(prefix);
            pw.print("mBulkUpdateParams=0x");
            pw.print(Integer.toHexString(this.mBulkUpdateParams));
            pw.println(bulkUpdateParamsToString(this.mBulkUpdateParams));
        }
    }

    /* access modifiers changed from: package-private */
    public int getPendingLayoutChanges(int displayId) {
        DisplayContent displayContent;
        if (displayId >= 0 && (displayContent = this.mService.mRoot.getDisplayContent(displayId)) != null) {
            return displayContent.pendingLayoutChanges;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void setPendingLayoutChanges(int displayId, int changes) {
        DisplayContent displayContent;
        if (displayId >= 0 && (displayContent = this.mService.mRoot.getDisplayContent(displayId)) != null) {
            displayContent.pendingLayoutChanges |= changes;
        }
    }

    private DisplayContentsAnimator getDisplayContentsAnimatorLocked(int displayId) {
        if (displayId < 0) {
            return null;
        }
        DisplayContentsAnimator displayAnimator = this.mDisplayContentsAnimators.get(displayId);
        if (displayAnimator != null || this.mService.mRoot.getDisplayContent(displayId) == null) {
            return displayAnimator;
        }
        DisplayContentsAnimator displayAnimator2 = new DisplayContentsAnimator();
        this.mDisplayContentsAnimators.put(displayId, displayAnimator2);
        return displayAnimator2;
    }

    /* access modifiers changed from: package-private */
    public void setScreenRotationAnimationLocked(int displayId, ScreenRotationAnimation animation) {
        DisplayContentsAnimator animator = getDisplayContentsAnimatorLocked(displayId);
        if (animator != null) {
            animator.mScreenRotationAnimation = animation;
        }
    }

    /* access modifiers changed from: package-private */
    public ScreenRotationAnimation getScreenRotationAnimationLocked(int displayId) {
        DisplayContentsAnimator animator;
        if (displayId >= 0 && (animator = getDisplayContentsAnimatorLocked(displayId)) != null) {
            return animator.mScreenRotationAnimation;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void requestRemovalOfReplacedWindows(WindowState win) {
        this.mRemoveReplacedWindows = true;
    }

    /* access modifiers changed from: package-private */
    public void scheduleAnimation() {
        if (!this.mAnimationFrameCallbackScheduled) {
            this.mAnimationFrameCallbackScheduled = true;
            this.mChoreographer.postFrameCallback(this.mAnimationFrameCallback);
        }
    }

    private void cancelAnimation() {
        if (this.mAnimationFrameCallbackScheduled) {
            this.mAnimationFrameCallbackScheduled = false;
            this.mChoreographer.removeFrameCallback(this.mAnimationFrameCallback);
            handleResumeDispModeChange(false);
        }
    }

    /* access modifiers changed from: private */
    public class DisplayContentsAnimator {
        ScreenRotationAnimation mScreenRotationAnimation;

        private DisplayContentsAnimator() {
            this.mScreenRotationAnimation = null;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimating() {
        return this.mAnimating;
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimationScheduled() {
        return this.mAnimationFrameCallbackScheduled;
    }

    /* access modifiers changed from: package-private */
    public Choreographer getChoreographer() {
        return this.mChoreographer;
    }

    /* access modifiers changed from: package-private */
    public void setAnimating(boolean animating) {
        this.mAnimating = animating;
    }

    /* access modifiers changed from: package-private */
    public void orAnimating(boolean animating) {
        this.mAnimating |= animating;
    }

    /* access modifiers changed from: package-private */
    public void addAfterPrepareSurfacesRunnable(Runnable r) {
        if (this.mInExecuteAfterPrepareSurfacesRunnables) {
            r.run();
            return;
        }
        this.mAfterPrepareSurfacesRunnables.add(r);
        scheduleAnimation();
    }

    /* access modifiers changed from: package-private */
    public void executeAfterPrepareSurfacesRunnables() {
        if (!this.mInExecuteAfterPrepareSurfacesRunnables) {
            this.mInExecuteAfterPrepareSurfacesRunnables = true;
            int size = this.mAfterPrepareSurfacesRunnables.size();
            for (int i = 0; i < size; i++) {
                this.mAfterPrepareSurfacesRunnables.get(i).run();
            }
            this.mAfterPrepareSurfacesRunnables.clear();
            this.mInExecuteAfterPrepareSurfacesRunnables = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void setIsLazying(boolean isLazying) {
        this.mIsLazying = isLazying;
    }

    private void handleResumeDispModeChange(boolean force) {
        if (HwFoldScreenState.isFoldScreenDevice() && PROP_FOLD_SWITCH_DEBUG) {
            if (this.mFsmInternal == null) {
                this.mFsmInternal = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
            }
            if (this.mFsmInternal == null) {
                return;
            }
            if (force || isAnimating()) {
                Slog.i(TAG, "resumeDispModeChange from WA force=" + force);
                this.mFsmInternal.resumeDispModeChange();
            }
        }
    }
}
