package com.android.server.wm;

import android.content.Context;
import android.os.Trace;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.view.Choreographer;
import android.view.Choreographer.FrameCallback;
import android.view.WindowManagerPolicy;
import com.android.server.AnimationThread;
import com.android.server.wm.-$Lambda$OQfQhd_xsxt9hoLAjIbVfOwa-jY.AnonymousClass1;
import com.android.server.wm.-$Lambda$OQfQhd_xsxt9hoLAjIbVfOwa-jY.AnonymousClass2;
import java.io.PrintWriter;
import java.util.function.Consumer;

public class WindowAnimator {
    private static final long KEYGUARD_ANIM_TIMEOUT_MS = 2000;
    private static final String TAG = "WindowManager";
    int mAnimTransactionSequence;
    private boolean mAnimating;
    final FrameCallback mAnimationFrameCallback;
    private boolean mAnimationFrameCallbackScheduled;
    boolean mAppWindowAnimating;
    int mBulkUpdateParams = 0;
    private Choreographer mChoreographer;
    final Context mContext;
    long mCurrentTime;
    SparseArray<DisplayContentsAnimator> mDisplayContentsAnimators = new SparseArray(2);
    boolean mInitialized = false;
    boolean mIsLazying = false;
    private boolean mLastAnimating;
    Object mLastWindowFreezeSource;
    final WindowManagerPolicy mPolicy;
    private boolean mRemoveReplacedWindows = false;
    final WindowManagerService mService;
    WindowState mWindowDetachedWallpaper = null;
    private final WindowSurfacePlacer mWindowPlacerLocked;
    int offsetLayer = 0;

    private class DisplayContentsAnimator {
        ScreenRotationAnimation mScreenRotationAnimation;

        /* synthetic */ DisplayContentsAnimator(WindowAnimator this$0, DisplayContentsAnimator -this1) {
            this();
        }

        private DisplayContentsAnimator() {
            this.mScreenRotationAnimation = null;
        }
    }

    WindowAnimator(WindowManagerService service) {
        this.mService = service;
        this.mContext = service.mContext;
        this.mPolicy = service.mPolicy;
        this.mWindowPlacerLocked = service.mWindowPlacerLocked;
        AnimationThread.getHandler().runWithScissors(new AnonymousClass2(this), 0);
        this.mAnimationFrameCallback = new AnonymousClass1(this);
    }

    /* synthetic */ void lambda$-com_android_server_wm_WindowAnimator_4391() {
        this.mChoreographer = Choreographer.getSfInstance();
    }

    /* synthetic */ void lambda$-com_android_server_wm_WindowAnimator_4498(long frameTimeNs) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mAnimationFrameCallbackScheduled = false;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        animate(frameTimeNs);
    }

    void addDisplayLocked(int displayId) {
        getDisplayContentsAnimatorLocked(displayId);
        if (displayId == 0) {
            this.mInitialized = true;
        }
    }

    void removeDisplayLocked(int displayId) {
        DisplayContentsAnimator displayAnimator = (DisplayContentsAnimator) this.mDisplayContentsAnimators.get(displayId);
        if (!(displayAnimator == null || displayAnimator.mScreenRotationAnimation == null)) {
            displayAnimator.mScreenRotationAnimation.kill();
            displayAnimator.mScreenRotationAnimation = null;
        }
        this.mDisplayContentsAnimators.delete(displayId);
    }

    /* JADX WARNING: Missing block: B:10:0x0019, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
            r19.mService.executeEmptyAnimationTransaction();
            r13 = r19.mService.mWindowMap;
     */
    /* JADX WARNING: Missing block: B:11:0x0029, code:
            monitor-enter(r13);
     */
    /* JADX WARNING: Missing block: B:13:?, code:
            com.android.server.wm.WindowManagerService.boostPriorityForLockedSection();
            r19.mCurrentTime = r20 / 1000000;
            r19.mBulkUpdateParams = 8;
            r19.mAnimating = false;
            r19.mAppWindowAnimating = false;
            r19.mIsLazying = false;
            r19.mService.openSurfaceTransaction();
     */
    /* JADX WARNING: Missing block: B:15:?, code:
            r2 = r19.mService.mAccessibilityController;
            r10 = r19.mDisplayContentsAnimators.size();
            r9 = 0;
     */
    /* JADX WARNING: Missing block: B:16:0x0061, code:
            if (r9 >= r10) goto L_0x019f;
     */
    /* JADX WARNING: Missing block: B:17:0x0063, code:
            r3 = r19.mService.mRoot.getDisplayContentOrCreate(r19.mDisplayContentsAnimators.keyAt(r9));
            r3.stepAppWindowsAnimation(r19.mCurrentTime);
            r4 = (com.android.server.wm.WindowAnimator.DisplayContentsAnimator) r19.mDisplayContentsAnimators.valueAt(r9);
            r11 = r4.mScreenRotationAnimation;
     */
    /* JADX WARNING: Missing block: B:18:0x0088, code:
            if (r11 == null) goto L_0x00a0;
     */
    /* JADX WARNING: Missing block: B:20:0x008e, code:
            if (r11.isAnimating() == false) goto L_0x00a0;
     */
    /* JADX WARNING: Missing block: B:22:0x0098, code:
            if (r11.stepAnimationLocked(r19.mCurrentTime) == false) goto L_0x00c0;
     */
    /* JADX WARNING: Missing block: B:23:0x009a, code:
            setAnimating(true);
     */
    /* JADX WARNING: Missing block: B:24:0x00a0, code:
            r19.mAnimTransactionSequence++;
            r3.updateWindowsForAnimator(r19);
            r3.updateWallpaperForAnimator(r19);
            r3.prepareWindowSurfaces();
     */
    /* JADX WARNING: Missing block: B:25:0x00b7, code:
            r9 = r9 + 1;
     */
    /* JADX WARNING: Missing block: B:29:?, code:
            r19.mBulkUpdateParams |= 1;
            r11.kill();
            r4.mScreenRotationAnimation = null;
     */
    /* JADX WARNING: Missing block: B:30:0x00d0, code:
            if (r2 == null) goto L_0x00a0;
     */
    /* JADX WARNING: Missing block: B:32:0x00d4, code:
            if (r3.isDefaultDisplay == false) goto L_0x00a0;
     */
    /* JADX WARNING: Missing block: B:33:0x00d6, code:
            r2.onRotationChangedLocked(r19.mService.getDefaultDisplayContentLocked());
     */
    /* JADX WARNING: Missing block: B:34:0x00e2, code:
            r7 = move-exception;
     */
    /* JADX WARNING: Missing block: B:36:?, code:
            android.util.Slog.wtf(TAG, "Unhandled exception in Window Manager", r7);
     */
    /* JADX WARNING: Missing block: B:38:?, code:
            r19.mService.closeSurfaceTransaction();
     */
    /* JADX WARNING: Missing block: B:62:0x019f, code:
            r9 = 0;
     */
    /* JADX WARNING: Missing block: B:63:0x01a0, code:
            if (r9 >= r10) goto L_0x01ff;
     */
    /* JADX WARNING: Missing block: B:65:?, code:
            r5 = r19.mDisplayContentsAnimators.keyAt(r9);
            r3 = r19.mService.mRoot.getDisplayContentOrCreate(r5);
            r3.checkAppWindowsReadyToShow();
            r11 = ((com.android.server.wm.WindowAnimator.DisplayContentsAnimator) r19.mDisplayContentsAnimators.valueAt(r9)).mScreenRotationAnimation;
     */
    /* JADX WARNING: Missing block: B:66:0x01c3, code:
            if (r11 == null) goto L_0x01c8;
     */
    /* JADX WARNING: Missing block: B:67:0x01c5, code:
            r11.updateSurfacesInTransaction();
     */
    /* JADX WARNING: Missing block: B:68:0x01c8, code:
            orAnimating(r3.animateDimLayers());
            orAnimating(r3.getDockedDividerController().animate(r19.mCurrentTime));
            updateBlurLayers(r5);
     */
    /* JADX WARNING: Missing block: B:69:0x01e7, code:
            if (r2 == null) goto L_0x01fc;
     */
    /* JADX WARNING: Missing block: B:71:0x01eb, code:
            if (r3.isDefaultDisplay != false) goto L_0x01f9;
     */
    /* JADX WARNING: Missing block: B:73:0x01f1, code:
            if (android.util.HwPCUtils.enabledInPad() == false) goto L_0x01fc;
     */
    /* JADX WARNING: Missing block: B:75:0x01f7, code:
            if (android.util.HwPCUtils.isPcCastModeInServer() == false) goto L_0x01fc;
     */
    /* JADX WARNING: Missing block: B:76:0x01f9, code:
            r2.drawMagnifiedRegionBorderIfNeededLocked();
     */
    /* JADX WARNING: Missing block: B:77:0x01fc, code:
            r9 = r9 + 1;
     */
    /* JADX WARNING: Missing block: B:79:0x0205, code:
            if (r19.mService.mDragState == null) goto L_0x0222;
     */
    /* JADX WARNING: Missing block: B:80:0x0207, code:
            r19.mAnimating |= r19.mService.mDragState.stepAnimationLocked(r19.mCurrentTime);
     */
    /* JADX WARNING: Missing block: B:82:0x0226, code:
            if (r19.mAnimating != false) goto L_0x0233;
     */
    /* JADX WARNING: Missing block: B:84:0x022e, code:
            if ((r19.mIsLazying ^ 1) == 0) goto L_0x0233;
     */
    /* JADX WARNING: Missing block: B:85:0x0230, code:
            cancelAnimation();
     */
    /* JADX WARNING: Missing block: B:87:0x0239, code:
            if (r19.mService.mWatermark == null) goto L_0x0244;
     */
    /* JADX WARNING: Missing block: B:88:0x023b, code:
            r19.mService.mWatermark.drawIfNeeded();
     */
    /* JADX WARNING: Missing block: B:90:?, code:
            r19.mService.closeSurfaceTransaction();
     */
    /* JADX WARNING: Missing block: B:92:0x024f, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:95:?, code:
            r19.mService.closeSurfaceTransaction();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void animate(long frameTimeNs) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mInitialized) {
                    scheduleAnimation();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        boolean hasPendingLayoutChanges = this.mService.mRoot.hasPendingLayoutChanges(this);
        boolean doRequest = false;
        if (this.mBulkUpdateParams != 0) {
            doRequest = this.mService.mRoot.copyAnimToLayoutParams();
        }
        if (hasPendingLayoutChanges || doRequest) {
            this.mWindowPlacerLocked.requestTraversal();
        }
        if (this.mAnimating && (this.mLastAnimating ^ 1) != 0) {
            this.mService.mTaskSnapshotController.setPersisterPaused(true);
            Trace.asyncTraceBegin(32, "animating", 0);
        }
        if (!this.mAnimating && this.mLastAnimating) {
            this.mWindowPlacerLocked.requestTraversal();
            this.mService.mTaskSnapshotController.setPersisterPaused(false);
            Trace.asyncTraceEnd(32, "animating", 0);
        }
        this.mLastAnimating = this.mAnimating;
        if (this.mRemoveReplacedWindows) {
            this.mService.mRoot.removeReplacedWindows();
            this.mRemoveReplacedWindows = false;
        }
        this.mService.stopUsingSavedSurfaceLocked();
        this.mService.destroyPreservedSurfaceLocked();
        this.mService.mWindowPlacerLocked.destroyPendingSurfaces();
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    private void cancelAnimation() {
        if (this.mAnimationFrameCallbackScheduled) {
            this.mAnimationFrameCallbackScheduled = false;
            this.mChoreographer.removeFrameCallback(this.mAnimationFrameCallback);
        }
    }

    private static String bulkUpdateParamsToString(int bulkUpdateParams) {
        StringBuilder builder = new StringBuilder(128);
        if ((bulkUpdateParams & 1) != 0) {
            builder.append(" UPDATE_ROTATION");
        }
        if ((bulkUpdateParams & 2) != 0) {
            builder.append(" WALLPAPER_MAY_CHANGE");
        }
        if ((bulkUpdateParams & 4) != 0) {
            builder.append(" FORCE_HIDING_CHANGED");
        }
        if ((bulkUpdateParams & 8) != 0) {
            builder.append(" ORIENTATION_CHANGE_COMPLETE");
        }
        if ((bulkUpdateParams & 16) != 0) {
            builder.append(" TURN_ON_SCREEN");
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
            DisplayContentsAnimator displayAnimator = (DisplayContentsAnimator) this.mDisplayContentsAnimators.valueAt(i);
            this.mService.mRoot.getDisplayContentOrCreate(this.mDisplayContentsAnimators.keyAt(i)).dumpWindowAnimators(pw, subPrefix);
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
            pw.print("mAnimTransactionSequence=");
            pw.print(this.mAnimTransactionSequence);
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
        if (this.mWindowDetachedWallpaper != null) {
            pw.print(prefix);
            pw.print("mWindowDetachedWallpaper=");
            pw.println(this.mWindowDetachedWallpaper);
        }
    }

    int getPendingLayoutChanges(int displayId) {
        int i = 0;
        if (displayId < 0) {
            return 0;
        }
        DisplayContent displayContent = this.mService.mRoot.getDisplayContentOrCreate(displayId);
        if (displayContent != null) {
            i = displayContent.pendingLayoutChanges;
        }
        return i;
    }

    void setPendingLayoutChanges(int displayId, int changes) {
        if (displayId >= 0) {
            DisplayContent displayContent = this.mService.mRoot.getDisplayContentOrCreate(displayId);
            if (displayContent != null) {
                displayContent.pendingLayoutChanges |= changes;
            }
        }
    }

    private DisplayContentsAnimator getDisplayContentsAnimatorLocked(int displayId) {
        if (displayId < 0) {
            return null;
        }
        DisplayContentsAnimator displayAnimator = (DisplayContentsAnimator) this.mDisplayContentsAnimators.get(displayId);
        if (displayAnimator == null && this.mService.mRoot.getDisplayContent(displayId) != null) {
            displayAnimator = new DisplayContentsAnimator(this, null);
            this.mDisplayContentsAnimators.put(displayId, displayAnimator);
        }
        return displayAnimator;
    }

    void setScreenRotationAnimationLocked(int displayId, ScreenRotationAnimation animation) {
        DisplayContentsAnimator animator = getDisplayContentsAnimatorLocked(displayId);
        if (animator != null) {
            animator.mScreenRotationAnimation = animation;
        }
    }

    ScreenRotationAnimation getScreenRotationAnimationLocked(int displayId) {
        ScreenRotationAnimation screenRotationAnimation = null;
        if (displayId < 0) {
            return null;
        }
        DisplayContentsAnimator animator = getDisplayContentsAnimatorLocked(displayId);
        if (animator != null) {
            screenRotationAnimation = animator.mScreenRotationAnimation;
        }
        return screenRotationAnimation;
    }

    void requestRemovalOfReplacedWindows(WindowState win) {
        this.mRemoveReplacedWindows = true;
    }

    void scheduleAnimation() {
        if (!this.mAnimationFrameCallbackScheduled) {
            this.mAnimationFrameCallbackScheduled = true;
            this.mChoreographer.postFrameCallback(this.mAnimationFrameCallback);
        }
    }

    boolean isAnimating() {
        return this.mAnimating;
    }

    boolean isAnimationScheduled() {
        return this.mAnimationFrameCallbackScheduled;
    }

    Choreographer getChoreographer() {
        return this.mChoreographer;
    }

    void setAnimating(boolean animating) {
        this.mAnimating = animating;
    }

    void orAnimating(boolean animating) {
        this.mAnimating |= animating;
    }

    void setIsLazying(boolean isLazying) {
        this.mIsLazying = isLazying;
    }

    private void updateBlurLayers(int displayId) {
        this.mService.mRoot.getDisplayContentOrCreate(displayId).forAllWindows((Consumer) new -$Lambda$OQfQhd_xsxt9hoLAjIbVfOwa-jY(), false);
    }

    static /* synthetic */ void lambda$-com_android_server_wm_WindowAnimator_20360(WindowState win) {
        if ((win.mAttrs.flags & 4) != 0 && win.isDisplayedLw() && (win.mAnimatingExit ^ 1) != 0) {
            win.mWinAnimator.updateBlurLayer(win.mAttrs);
        }
    }
}
