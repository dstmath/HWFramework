package com.android.server.wm;

import android.content.Context;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.view.Choreographer;
import android.view.SurfaceControl;
import com.android.server.AnimationThread;
import com.android.server.policy.WindowManagerPolicy;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Consumer;

public class WindowAnimator {
    private static final long KEYGUARD_ANIM_TIMEOUT_MS = 2000;
    private static final String TAG = "WindowManager";
    private final ArrayList<Runnable> mAfterPrepareSurfacesRunnables = new ArrayList<>();
    int mAnimTransactionSequence;
    private boolean mAnimating;
    final Choreographer.FrameCallback mAnimationFrameCallback;
    private boolean mAnimationFrameCallbackScheduled;
    boolean mAppWindowAnimating;
    int mBulkUpdateParams = 0;
    /* access modifiers changed from: private */
    public Choreographer mChoreographer;
    final Context mContext;
    long mCurrentTime;
    SparseArray<DisplayContentsAnimator> mDisplayContentsAnimators = new SparseArray<>(2);
    private boolean mInExecuteAfterPrepareSurfacesRunnables;
    private boolean mInitialized = false;
    boolean mIsLazying = false;
    private boolean mLastRootAnimating;
    Object mLastWindowFreezeSource;
    final WindowManagerPolicy mPolicy;
    private boolean mRemoveReplacedWindows = false;
    final WindowManagerService mService;
    private final SurfaceControl.Transaction mTransaction = new SurfaceControl.Transaction();
    WindowState mWindowDetachedWallpaper = null;
    int offsetLayer = 0;

    private class DisplayContentsAnimator {
        ScreenRotationAnimation mScreenRotationAnimation;

        private DisplayContentsAnimator() {
            this.mScreenRotationAnimation = null;
        }
    }

    WindowAnimator(WindowManagerService service) {
        this.mService = service;
        this.mContext = service.mContext;
        this.mPolicy = service.mPolicy;
        AnimationThread.getHandler().runWithScissors(new Runnable() {
            public final void run() {
                WindowAnimator.this.mChoreographer = Choreographer.getSfInstance();
            }
        }, 0);
        this.mAnimationFrameCallback = new Choreographer.FrameCallback() {
            public final void doFrame(long j) {
                WindowAnimator.lambda$new$1(WindowAnimator.this, j);
            }
        };
    }

    public static /* synthetic */ void lambda$new$1(WindowAnimator windowAnimator, long frameTimeNs) {
        synchronized (windowAnimator.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                windowAnimator.mAnimationFrameCallbackScheduled = false;
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        windowAnimator.animate(frameTimeNs);
    }

    /* access modifiers changed from: package-private */
    public void addDisplayLocked(int displayId) {
        getDisplayContentsAnimatorLocked(displayId);
        if (displayId == 0) {
            this.mInitialized = true;
        }
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

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0015, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
        r1 = r12.mService.mWindowMap;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        com.android.server.wm.WindowManagerService.boostPriorityForLockedSection();
        r12.mCurrentTime = r13 / 1000000;
        r12.mBulkUpdateParams = 8;
        r12.mAnimating = false;
        r12.mIsLazying = false;
        r12.mService.openSurfaceTransaction();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r3 = r12.mService.mAccessibilityController;
        r4 = r12.mDisplayContentsAnimators.size();
        r5 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0041, code lost:
        if (r5 >= r4) goto L_0x009a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0043, code lost:
        r7 = r12.mService.mRoot.getDisplayContent(r12.mDisplayContentsAnimators.keyAt(r5));
        r8 = r12.mDisplayContentsAnimators.valueAt(r5);
        r9 = r8.mScreenRotationAnimation;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005b, code lost:
        if (r9 == null) goto L_0x0089;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0061, code lost:
        if (r9.isAnimating() == false) goto L_0x0089;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0069, code lost:
        if (r9.stepAnimationLocked(r12.mCurrentTime) == false) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006b, code lost:
        setAnimating(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x006f, code lost:
        r12.mBulkUpdateParams |= 1;
        r9.kill();
        r8.mScreenRotationAnimation = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x007a, code lost:
        if (r3 == null) goto L_0x0089;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x007e, code lost:
        if (r7.isDefaultDisplay == false) goto L_0x0089;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0080, code lost:
        r3.onRotationChangedLocked(r12.mService.getDefaultDisplayContentLocked());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0089, code lost:
        r12.mAnimTransactionSequence++;
        r7.updateWindowsForAnimator(r12);
        r7.updateWallpaperForAnimator(r12);
        r7.prepareSurfaces();
        r5 = r5 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x009a, code lost:
        r5 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x009b, code lost:
        if (r5 >= r4) goto L_0x00e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x009d, code lost:
        r6 = r12.mDisplayContentsAnimators.keyAt(r5);
        r7 = r12.mService.mRoot.getDisplayContent(r6);
        r7.checkAppWindowsReadyToShow();
        r8 = r12.mDisplayContentsAnimators.valueAt(r5).mScreenRotationAnimation;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00b8, code lost:
        if (r8 == null) goto L_0x00bf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00ba, code lost:
        r8.updateSurfaces(r12.mTransaction);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00bf, code lost:
        orAnimating(r7.getDockedDividerController().animate(r12.mCurrentTime));
        updateBlurLayers(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00cf, code lost:
        if (r3 == null) goto L_0x00e4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00d3, code lost:
        if (r7.isDefaultDisplay != false) goto L_0x00e1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00d9, code lost:
        if (android.util.HwPCUtils.enabledInPad() == false) goto L_0x00e4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00df, code lost:
        if (android.util.HwPCUtils.isPcCastModeInServer() == false) goto L_0x00e4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00e1, code lost:
        r3.drawMagnifiedRegionBorderIfNeededLocked();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00e4, code lost:
        r5 = r5 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00e9, code lost:
        if (r12.mAnimating != 0) goto L_0x00f2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00ed, code lost:
        if (r12.mIsLazying != false) goto L_0x00f2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00ef, code lost:
        cancelAnimation();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00f6, code lost:
        if (r12.mService.mWatermark == null) goto L_0x00ff;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00f8, code lost:
        r12.mService.mWatermark.drawIfNeeded();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ff, code lost:
        android.view.SurfaceControl.mergeToGlobalTransaction(r12.mTransaction);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:?, code lost:
        r3 = r12.mService;
        r4 = "WindowAnimator";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0109, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x010c, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:?, code lost:
        android.util.Slog.wtf(TAG, "Unhandled exception in Window Manager", r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:?, code lost:
        r3 = r12.mService;
        r4 = "WindowAnimator";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0118, code lost:
        r3.closeSurfaceTransaction(r4);
        r3 = r12.mService.mRoot.hasPendingLayoutChanges(r12);
        r4 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0127, code lost:
        if (r12.mBulkUpdateParams != 0) goto L_0x0129;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0129, code lost:
        r4 = r12.mService.mRoot.copyAnimToLayoutParams();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0136, code lost:
        r12.mService.mWindowPlacerLocked.requestTraversal();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x013d, code lost:
        r5 = r12.mService.mRoot.isSelfOrChildAnimating();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x014d, code lost:
        r12.mService.mTaskSnapshotController.setPersisterPaused(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0158, code lost:
        if (android.util.Jlog.isPerfTest() != false) goto L_0x015a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x015a, code lost:
        android.util.Jlog.i(3052, android.util.Jlog.getMessage("WindowAnimator", "animate", "ANIMATE_BEGIN"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0169, code lost:
        android.os.Trace.asyncTraceBegin(32, "animating", 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0174, code lost:
        r12.mService.mWindowPlacerLocked.requestTraversal();
        r12.mService.mTaskSnapshotController.setPersisterPaused(false);
        android.os.Trace.asyncTraceEnd(32, "animating", 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x018b, code lost:
        if (android.util.Jlog.isPerfTest() != false) goto L_0x018d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x018d, code lost:
        android.util.Jlog.i(3055, android.util.Jlog.getMessage("WindowAnimator", "animate", "ANIMATE_END"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x019c, code lost:
        r12.mLastRootAnimating = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x01a0, code lost:
        if (r12.mRemoveReplacedWindows != false) goto L_0x01a2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01a2, code lost:
        r12.mService.mRoot.removeReplacedWindows();
        r12.mRemoveReplacedWindows = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x01ab, code lost:
        r12.mService.destroyPreservedSurfaceLocked();
        executeAfterPrepareSurfacesRunnables();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x01b3, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x01b4, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x01b7, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:?, code lost:
        r12.mService.closeSurfaceTransaction("WindowAnimator");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x01bf, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x01c0, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x01c2, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x01c5, code lost:
        throw r0;
     */
    private void animate(long frameTimeNs) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (!this.mInitialized) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                scheduleAnimation();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
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

    /* access modifiers changed from: package-private */
    public int getPendingLayoutChanges(int displayId) {
        int i = 0;
        if (displayId < 0) {
            return 0;
        }
        DisplayContent displayContent = this.mService.mRoot.getDisplayContent(displayId);
        if (displayContent != null) {
            i = displayContent.pendingLayoutChanges;
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public void setPendingLayoutChanges(int displayId, int changes) {
        if (displayId >= 0) {
            DisplayContent displayContent = this.mService.mRoot.getDisplayContent(displayId);
            if (displayContent != null) {
                displayContent.pendingLayoutChanges |= changes;
            }
        }
    }

    private DisplayContentsAnimator getDisplayContentsAnimatorLocked(int displayId) {
        if (displayId < 0) {
            return null;
        }
        DisplayContentsAnimator displayAnimator = this.mDisplayContentsAnimators.get(displayId);
        if (displayAnimator == null && this.mService.mRoot.getDisplayContent(displayId) != null) {
            displayAnimator = new DisplayContentsAnimator();
            this.mDisplayContentsAnimators.put(displayId, displayAnimator);
        }
        return displayAnimator;
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

    private void updateBlurLayers(int displayId) {
        DisplayContent displayContent = this.mService.mRoot.getDisplayContent(displayId);
        if (displayContent != null) {
            displayContent.forAllWindows((Consumer<WindowState>) $$Lambda$WindowAnimator$QPolCGKJCfzSmj0sq1NSNBg04bk.INSTANCE, false);
        }
    }

    static /* synthetic */ void lambda$updateBlurLayers$2(WindowState win) {
        if ((win.mAttrs.flags & 4) != 0 && win.isDisplayedLw() && !win.mAnimatingExit) {
            win.mWinAnimator.updateBlurLayer(win.mAttrs);
        }
    }
}
