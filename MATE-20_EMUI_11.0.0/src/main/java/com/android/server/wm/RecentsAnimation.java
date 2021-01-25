package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.IAssistDataReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.os.RemoteException;
import android.os.Trace;
import android.util.Slog;
import android.view.IRecentsAnimationRunner;
import com.android.server.wm.ActivityDisplay;
import com.android.server.wm.RecentsAnimationController;

class RecentsAnimation implements RecentsAnimationController.RecentsAnimationCallbacks, ActivityDisplay.OnStackOrderChangedListener {
    private static final boolean DEBUG = false;
    private static final String LEGACY_RECENTS_PACKAGE_NAME_LAUNCHER = "com.huawei.android.launcher.quickstep.RecentsActivity";
    private static final String TAG = RecentsAnimation.class.getSimpleName();
    private final ActivityStartController mActivityStartController;
    private final int mCallingPid;
    private final ActivityDisplay mDefaultDisplay = this.mService.mRootActivityContainer.getDefaultDisplay();
    private ActivityRecord mLaunchedTargetActivity;
    private ActivityStack mRestoreTargetBehindStack;
    private final ActivityTaskManagerService mService;
    private final ActivityStackSupervisor mStackSupervisor;
    private int mTargetActivityType;
    private final WindowManagerService mWindowManager;

    RecentsAnimation(ActivityTaskManagerService atm, ActivityStackSupervisor stackSupervisor, ActivityStartController activityStartController, WindowManagerService wm, int callingPid) {
        this.mService = atm;
        this.mStackSupervisor = stackSupervisor;
        this.mActivityStartController = activityStartController;
        this.mWindowManager = wm;
        this.mCallingPid = callingPid;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x00ad  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x00af  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0107 A[SYNTHETIC, Splitter:B:32:0x0107] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0122 A[Catch:{ Exception -> 0x01db, all -> 0x01d7 }] */
    public void startRecentsActivity(Intent intent, IRecentsAnimationRunner recentsAnimationRunner, ComponentName recentsComponent, int recentsUid, @Deprecated IAssistDataReceiver assistDataReceiver) {
        int i;
        ActivityRecord targetActivity;
        boolean hasExistingActivity;
        Exception e;
        Exception e2;
        ActivityStack targetStack;
        ActivityRecord targetActivity2;
        Slog.d(TAG, "startRecentsActivity(): intent=" + intent + " assistDataReceiver=" + assistDataReceiver);
        Trace.traceBegin(64, "RecentsAnimation#startRecentsActivity");
        DisplayContent dc = this.mService.mRootActivityContainer.getDefaultDisplay().mDisplayContent;
        if (!this.mWindowManager.canStartRecentsAnimation()) {
            notifyAnimationCancelBeforeStart(recentsAnimationRunner);
            Slog.d(TAG, "Can't start recents animation, nextAppTransition=" + dc.mAppTransition.getAppTransition());
            return;
        }
        int userId = this.mService.getCurrentUserId();
        if (intent.getComponent() != null) {
            if (recentsComponent.equals(intent.getComponent())) {
                i = 3;
                this.mTargetActivityType = i;
                if (intent.getComponent() != null && LEGACY_RECENTS_PACKAGE_NAME_LAUNCHER.equals(intent.getComponent().getClassName())) {
                    this.mTargetActivityType = 3;
                }
                ActivityStack targetStack2 = this.mDefaultDisplay.getStack(0, this.mTargetActivityType);
                targetActivity = getTargetActivity(targetStack2, intent.getComponent(), userId);
                hasExistingActivity = targetActivity == null;
                if (hasExistingActivity) {
                    this.mRestoreTargetBehindStack = targetActivity.getDisplay().getStackAbove(targetStack2);
                    if (this.mRestoreTargetBehindStack == null) {
                        notifyAnimationCancelBeforeStart(recentsAnimationRunner);
                        Slog.d(TAG, "No stack above target stack=" + targetStack2);
                        return;
                    }
                }
                if (targetActivity == null || !targetActivity.visible) {
                    this.mService.mRootActivityContainer.sendPowerHintForLaunchStartIfNeeded(true, targetActivity);
                }
                this.mStackSupervisor.getActivityMetricsLogger().notifyActivityLaunching(intent);
                this.mService.mH.post(new Runnable() {
                    /* class com.android.server.wm.$$Lambda$RecentsAnimation$e3kosml870P6Bh_K_Z_6yyLHZk */

                    @Override // java.lang.Runnable
                    public final void run() {
                        RecentsAnimation.this.lambda$startRecentsActivity$0$RecentsAnimation();
                    }
                });
                this.mWindowManager.deferSurfaceLayout();
                if (!hasExistingActivity) {
                    try {
                        this.mDefaultDisplay.moveStackBehindBottomMostVisibleStack(targetStack2);
                        if (targetStack2.topTask() != targetActivity.getTaskRecord()) {
                            targetStack2.addTask(targetActivity.getTaskRecord(), true, "startRecentsActivity");
                        }
                        targetStack = targetStack2;
                        targetActivity2 = targetActivity;
                    } catch (Exception e3) {
                        e2 = e3;
                        try {
                            Slog.e(TAG, "Failed to start recents activity", e2);
                            throw e2;
                        } catch (Throwable th) {
                            e = th;
                            this.mWindowManager.continueSurfaceLayout();
                            Trace.traceEnd(64);
                            throw e;
                        }
                    } catch (Throwable th2) {
                        e = th2;
                        this.mWindowManager.continueSurfaceLayout();
                        Trace.traceEnd(64);
                        throw e;
                    }
                } else {
                    ActivityOptions options = ActivityOptions.makeBasic();
                    options.setLaunchActivityType(this.mTargetActivityType);
                    options.setAvoidMoveToFront();
                    intent.addFlags(268500992);
                    try {
                        this.mActivityStartController.obtainStarter(intent, "startRecentsActivity_noTargetActivity").setCallingUid(recentsUid).setCallingPackage(recentsComponent.getPackageName()).setActivityOptions(SafeActivityOptions.fromBundle(options.toBundle())).setMayWait(userId).execute();
                        ActivityStack targetStack3 = this.mDefaultDisplay.getStack(0, this.mTargetActivityType);
                        ActivityRecord targetActivity3 = getTargetActivity(targetStack3, intent.getComponent(), userId);
                        this.mDefaultDisplay.moveStackBehindBottomMostVisibleStack(targetStack3);
                        this.mWindowManager.prepareAppTransition(0, false);
                        this.mWindowManager.executeAppTransition();
                        targetStack = targetStack3;
                        targetActivity2 = targetActivity3;
                    } catch (Exception e4) {
                        e2 = e4;
                        Slog.e(TAG, "Failed to start recents activity", e2);
                        throw e2;
                    }
                }
                targetActivity2.mLaunchTaskBehind = true;
                this.mLaunchedTargetActivity = targetActivity2;
                this.mWindowManager.cancelRecentsAnimationSynchronously(2, "startRecentsActivity");
                targetActivity2.mLaunchTaskBehind = true;
                this.mWindowManager.initializeRecentsAnimation(this.mTargetActivityType, recentsAnimationRunner, this, this.mDefaultDisplay.mDisplayId, this.mStackSupervisor.mRecentTasks.getRecentTaskIds());
                this.mService.mRootActivityContainer.ensureActivitiesVisible(null, 0, true);
                this.mStackSupervisor.getActivityMetricsLogger().notifyActivityLaunched(2, targetActivity2);
                this.mDefaultDisplay.registerStackOrderChangedListener(this);
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
            }
        }
        i = 2;
        this.mTargetActivityType = i;
        this.mTargetActivityType = 3;
        ActivityStack targetStack22 = this.mDefaultDisplay.getStack(0, this.mTargetActivityType);
        targetActivity = getTargetActivity(targetStack22, intent.getComponent(), userId);
        if (targetActivity == null) {
        }
        if (hasExistingActivity) {
        }
        this.mService.mRootActivityContainer.sendPowerHintForLaunchStartIfNeeded(true, targetActivity);
        this.mStackSupervisor.getActivityMetricsLogger().notifyActivityLaunching(intent);
        this.mService.mH.post(new Runnable() {
            /* class com.android.server.wm.$$Lambda$RecentsAnimation$e3kosml870P6Bh_K_Z_6yyLHZk */

            @Override // java.lang.Runnable
            public final void run() {
                RecentsAnimation.this.lambda$startRecentsActivity$0$RecentsAnimation();
            }
        });
        this.mWindowManager.deferSurfaceLayout();
        if (!hasExistingActivity) {
        }
        try {
            targetActivity2.mLaunchTaskBehind = true;
            this.mLaunchedTargetActivity = targetActivity2;
            this.mWindowManager.cancelRecentsAnimationSynchronously(2, "startRecentsActivity");
            targetActivity2.mLaunchTaskBehind = true;
            this.mWindowManager.initializeRecentsAnimation(this.mTargetActivityType, recentsAnimationRunner, this, this.mDefaultDisplay.mDisplayId, this.mStackSupervisor.mRecentTasks.getRecentTaskIds());
            this.mService.mRootActivityContainer.ensureActivitiesVisible(null, 0, true);
            this.mStackSupervisor.getActivityMetricsLogger().notifyActivityLaunched(2, targetActivity2);
            this.mDefaultDisplay.registerStackOrderChangedListener(this);
            this.mWindowManager.continueSurfaceLayout();
            Trace.traceEnd(64);
        } catch (Exception e5) {
            e2 = e5;
            Slog.e(TAG, "Failed to start recents activity", e2);
            throw e2;
        } catch (Throwable th3) {
            e = th3;
            this.mWindowManager.continueSurfaceLayout();
            Trace.traceEnd(64);
            throw e;
        }
    }

    public /* synthetic */ void lambda$startRecentsActivity$0$RecentsAnimation() {
        this.mService.mAmInternal.setRunningRemoteAnimation(this.mCallingPid, true);
    }

    /* access modifiers changed from: private */
    /* renamed from: finishAnimation */
    public void lambda$onAnimationFinished$3$RecentsAnimation(@RecentsAnimationController.ReorderMode int reorderMode, boolean sendUserLeaveHint) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mDefaultDisplay.unregisterStackOrderChangedListener(this);
                RecentsAnimationController controller = this.mWindowManager.getRecentsAnimationController();
                if (controller != null) {
                    if (reorderMode != 0) {
                        this.mService.mRootActivityContainer.sendPowerHintForLaunchEndIfNeeded();
                    }
                    if (reorderMode == 1) {
                        this.mService.stopAppSwitches();
                    }
                    this.mService.mH.post(new Runnable() {
                        /* class com.android.server.wm.$$Lambda$RecentsAnimation$maWFdpvN04gpjsVfJu49wyo8hQ */

                        @Override // java.lang.Runnable
                        public final void run() {
                            RecentsAnimation.this.lambda$finishAnimation$1$RecentsAnimation();
                        }
                    });
                    this.mWindowManager.inSurfaceTransaction(new Runnable(reorderMode, sendUserLeaveHint, controller) {
                        /* class com.android.server.wm.$$Lambda$RecentsAnimation$t0H9VDhk8jOhDLGudyjnaASceuk */
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ boolean f$2;
                        private final /* synthetic */ RecentsAnimationController f$3;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                            this.f$3 = r4;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            RecentsAnimation.this.lambda$finishAnimation$2$RecentsAnimation(this.f$1, this.f$2, this.f$3);
                        }
                    });
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public /* synthetic */ void lambda$finishAnimation$1$RecentsAnimation() {
        this.mService.mAmInternal.setRunningRemoteAnimation(this.mCallingPid, false);
    }

    public /* synthetic */ void lambda$finishAnimation$2$RecentsAnimation(int reorderMode, boolean sendUserLeaveHint, RecentsAnimationController controller) {
        ActivityRecord targetActivity;
        Trace.traceBegin(64, "RecentsAnimation#onAnimationFinished_inSurfaceTransaction");
        this.mWindowManager.deferSurfaceLayout();
        try {
            this.mWindowManager.setRecentAnimationFinishing(true);
            this.mWindowManager.cleanupRecentsAnimation(reorderMode);
            ActivityStack targetStack = this.mDefaultDisplay.getStack(0, this.mTargetActivityType);
            if (targetStack != null) {
                targetActivity = targetStack.isInStackLocked(this.mLaunchedTargetActivity);
            } else {
                targetActivity = null;
            }
            if (targetActivity == null) {
                this.mWindowManager.continueSurfaceLayout();
                this.mWindowManager.setRecentAnimationFinishing(false);
                Slog.i(TAG, "Recent animation finished");
                Trace.traceEnd(64);
                return;
            }
            targetActivity.mLaunchTaskBehind = false;
            if (reorderMode == 1) {
                this.mStackSupervisor.mNoAnimActivities.add(targetActivity);
                if (sendUserLeaveHint) {
                    this.mStackSupervisor.mUserLeaving = true;
                    targetStack.moveTaskToFrontLocked(targetActivity.getTaskRecord(), true, null, targetActivity.appTimeTracker, "RecentsAnimation.onAnimationFinished()");
                } else {
                    targetStack.moveToFront("RecentsAnimation.onAnimationFinished()");
                }
            } else if (reorderMode == 2) {
                targetActivity.getDisplay().moveStackBehindStack(targetStack, this.mRestoreTargetBehindStack);
            } else {
                if (!controller.shouldCancelWithDeferredScreenshot() && !targetStack.isFocusedStackOnDisplay()) {
                    targetStack.ensureActivitiesVisibleLocked(null, 0, false);
                }
                this.mWindowManager.continueSurfaceLayout();
                this.mWindowManager.setRecentAnimationFinishing(false);
                Slog.i(TAG, "Recent animation finished");
                Trace.traceEnd(64);
                return;
            }
            this.mWindowManager.prepareAppTransition(0, false);
            this.mService.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
            this.mService.mRootActivityContainer.resumeFocusedStacksTopActivities();
            this.mWindowManager.executeAppTransition();
            this.mWindowManager.checkSplitScreenMinimizedChanged(true);
            this.mWindowManager.continueSurfaceLayout();
            this.mWindowManager.setRecentAnimationFinishing(false);
            Slog.i(TAG, "Recent animation finished");
            Trace.traceEnd(64);
        } catch (Exception e) {
            Slog.e(TAG, "Failed to clean up recents activity", e);
            throw e;
        } catch (Throwable th) {
            this.mWindowManager.continueSurfaceLayout();
            this.mWindowManager.setRecentAnimationFinishing(false);
            Slog.i(TAG, "Recent animation finished");
            Trace.traceEnd(64);
            throw th;
        }
    }

    @Override // com.android.server.wm.RecentsAnimationController.RecentsAnimationCallbacks
    public void onAnimationFinished(@RecentsAnimationController.ReorderMode int reorderMode, boolean runSychronously, boolean sendUserLeaveHint) {
        if (runSychronously) {
            lambda$onAnimationFinished$3$RecentsAnimation(reorderMode, sendUserLeaveHint);
        } else {
            this.mService.mH.post(new Runnable(reorderMode, sendUserLeaveHint) {
                /* class com.android.server.wm.$$Lambda$RecentsAnimation$yp3SVPfM17AJdya7PiWVlmTQumE */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ boolean f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    RecentsAnimation.this.lambda$onAnimationFinished$3$RecentsAnimation(this.f$1, this.f$2);
                }
            });
        }
    }

    @Override // com.android.server.wm.ActivityDisplay.OnStackOrderChangedListener
    public void onStackOrderChanged(ActivityStack stack) {
        if (this.mDefaultDisplay.getIndexOf(stack) != -1 && stack.shouldBeVisible(null)) {
            if (!stack.inHwFreeFormWindowingMode()) {
                RecentsAnimationController controller = this.mWindowManager.getRecentsAnimationController();
                if (controller != null) {
                    this.mService.mRootActivityContainer.getDefaultDisplay().mDisplayContent.mBoundsAnimationController.setAnimationType(controller.shouldCancelWithDeferredScreenshot() ? 1 : 0);
                    if ((!controller.isAnimatingTask((Task) stack.getTaskStack().getTopChild()) || controller.isTargetApp(stack.getTopActivity().mAppWindowToken)) && controller.shouldCancelWithDeferredScreenshot()) {
                        controller.cancelOnNextTransitionStart();
                    } else if (!controller.isAnimatingTaskStack(stack.getTaskStack()) || stack.inHwMultiWindowingMode() || stack.isActivityTypeHome()) {
                        this.mWindowManager.cancelRecentsAnimationSynchronously(0, "stackOrderChanged");
                    }
                }
            } else if (ActivityTaskManagerDebugConfig.DEBUG_HWFREEFORM) {
                Slog.w(TAG, "start hwfreeform stack don't cancel recents animation");
            }
        }
    }

    private void notifyAnimationCancelBeforeStart(IRecentsAnimationRunner recentsAnimationRunner) {
        try {
            recentsAnimationRunner.onAnimationCanceled(false);
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to cancel recents animation before start", e);
        }
    }

    private ActivityStack getTopNonAlwaysOnTopStack() {
        for (int i = this.mDefaultDisplay.getChildCount() - 1; i >= 0; i--) {
            ActivityStack s = this.mDefaultDisplay.getChildAt(i);
            if (!s.getWindowConfiguration().isAlwaysOnTop()) {
                return s;
            }
        }
        return null;
    }

    private ActivityRecord getTargetActivity(ActivityStack targetStack, ComponentName component, int userId) {
        if (targetStack == null) {
            return null;
        }
        for (int i = targetStack.getChildCount() - 1; i >= 0; i--) {
            TaskRecord task = targetStack.getChildAt(i);
            if (task.userId == userId && task.getBaseIntent().getComponent().equals(component)) {
                return task.getTopActivity();
            }
        }
        return null;
    }
}
