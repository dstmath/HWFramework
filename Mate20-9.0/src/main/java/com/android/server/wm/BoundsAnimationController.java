package com.android.server.wm;

import android.animation.AnimationHandler;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Slog;
import android.view.Choreographer;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.graphics.SfVsyncFrameCallbackProvider;
import com.android.server.wm.BoundsAnimationController;
import com.android.server.wm.WindowManagerInternal;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BoundsAnimationController {
    private static final boolean DEBUG = false;
    private static final int DEBUG_ANIMATION_SLOW_DOWN_FACTOR = 1;
    private static final boolean DEBUG_LOCAL = false;
    private static final int DEFAULT_TRANSITION_DURATION = 425;
    public static final int NO_PIP_MODE_CHANGED_CALLBACKS = 0;
    public static final int SCHEDULE_PIP_MODE_CHANGED_ON_END = 2;
    public static final int SCHEDULE_PIP_MODE_CHANGED_ON_START = 1;
    private static final String TAG = "WindowManager";
    private static final int WAIT_FOR_DRAW_TIMEOUT_MS = 3000;
    /* access modifiers changed from: private */
    public final AnimationHandler mAnimationHandler;
    /* access modifiers changed from: private */
    public final AppTransition mAppTransition;
    private final AppTransitionNotifier mAppTransitionNotifier = new AppTransitionNotifier();
    /* access modifiers changed from: private */
    public Choreographer mChoreographer;
    private final Interpolator mFastOutSlowInInterpolator;
    /* access modifiers changed from: private */
    public boolean mFinishAnimationAfterTransition = false;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public boolean mIsPrintLog = false;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    public ArrayMap<BoundsAnimationTarget, BoundsAnimator> mRunningAnimations = new ArrayMap<>();

    private final class AppTransitionNotifier extends WindowManagerInternal.AppTransitionListener implements Runnable {
        private AppTransitionNotifier() {
        }

        public void onAppTransitionCancelledLocked() {
            if (BoundsAnimationController.this.mIsPrintLog) {
                Slog.d(BoundsAnimationController.TAG, "onAppTransitionCancelledLocked: mFinishAnimationAfterTransition=" + BoundsAnimationController.this.mFinishAnimationAfterTransition);
            }
            animationFinished();
        }

        public void onAppTransitionFinishedLocked(IBinder token) {
            if (BoundsAnimationController.this.mIsPrintLog) {
                Slog.d(BoundsAnimationController.TAG, "onAppTransitionFinishedLocked: mFinishAnimationAfterTransition=" + BoundsAnimationController.this.mFinishAnimationAfterTransition);
            }
            animationFinished();
        }

        private void animationFinished() {
            if (BoundsAnimationController.this.mFinishAnimationAfterTransition) {
                BoundsAnimationController.this.mHandler.removeCallbacks(this);
                BoundsAnimationController.this.mHandler.post(this);
                if (BoundsAnimationController.this.mIsPrintLog) {
                    Slog.d(BoundsAnimationController.TAG, "animationFinished post callback");
                }
            }
        }

        public void run() {
            if (BoundsAnimationController.this.mIsPrintLog) {
                Slog.d(BoundsAnimationController.TAG, "animationFinished run callback");
            }
            synchronized (BoundsAnimationController.this.mLock) {
                for (int i = 0; i < BoundsAnimationController.this.mRunningAnimations.size(); i++) {
                    ((BoundsAnimator) BoundsAnimationController.this.mRunningAnimations.valueAt(i)).onAnimationEnd(null);
                }
            }
        }
    }

    @VisibleForTesting
    final class BoundsAnimator extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
        private final Rect mFrom = new Rect();
        private final int mFrozenTaskHeight;
        private final int mFrozenTaskWidth;
        /* access modifiers changed from: private */
        public boolean mMoveFromFullscreen;
        /* access modifiers changed from: private */
        public boolean mMoveToFullscreen;
        private int mPrevSchedulePipModeChangedState;
        private final Runnable mResumeRunnable = new Runnable() {
            public final void run() {
                BoundsAnimationController.BoundsAnimator.this.resume();
            }
        };
        /* access modifiers changed from: private */
        public int mSchedulePipModeChangedState;
        private boolean mSkipAnimationEnd;
        private boolean mSkipFinalResize;
        private final BoundsAnimationTarget mTarget;
        private final Rect mTmpRect = new Rect();
        private final Rect mTmpTaskBounds = new Rect();
        private final Rect mTo = new Rect();

        BoundsAnimator(BoundsAnimationTarget target, Rect from, Rect to, int schedulePipModeChangedState, int prevShedulePipModeChangedState, boolean moveFromFullscreen, boolean moveToFullscreen) {
            this.mTarget = target;
            this.mFrom.set(from);
            this.mTo.set(to);
            this.mSchedulePipModeChangedState = schedulePipModeChangedState;
            this.mPrevSchedulePipModeChangedState = prevShedulePipModeChangedState;
            this.mMoveFromFullscreen = moveFromFullscreen;
            this.mMoveToFullscreen = moveToFullscreen;
            addUpdateListener(this);
            addListener(this);
            if (animatingToLargerSize()) {
                this.mFrozenTaskWidth = this.mTo.width();
                this.mFrozenTaskHeight = this.mTo.height();
                return;
            }
            this.mFrozenTaskWidth = this.mFrom.width();
            this.mFrozenTaskHeight = this.mFrom.height();
        }

        public void onAnimationStart(Animator animation) {
            if (BoundsAnimationController.this.mIsPrintLog) {
                Slog.d(BoundsAnimationController.TAG, "onAnimationStart: mTarget=" + this.mTarget + " mPrevSchedulePipModeChangedState=" + this.mPrevSchedulePipModeChangedState + " mSchedulePipModeChangedState=" + this.mSchedulePipModeChangedState);
            }
            boolean unused = BoundsAnimationController.this.mFinishAnimationAfterTransition = false;
            this.mTmpRect.set(this.mFrom.left, this.mFrom.top, this.mFrom.left + this.mFrozenTaskWidth, this.mFrom.top + this.mFrozenTaskHeight);
            BoundsAnimationController.this.updateBooster();
            boolean z = true;
            if (this.mPrevSchedulePipModeChangedState == 0) {
                BoundsAnimationTarget boundsAnimationTarget = this.mTarget;
                if (this.mSchedulePipModeChangedState != 1) {
                    z = false;
                }
                boundsAnimationTarget.onAnimationStart(z, false);
                if (this.mMoveFromFullscreen && this.mTarget.shouldDeferStartOnMoveToFullscreen()) {
                    pause();
                }
            } else if (this.mPrevSchedulePipModeChangedState == 2 && this.mSchedulePipModeChangedState == 1) {
                this.mTarget.onAnimationStart(true, true);
            }
            if (animatingToLargerSize()) {
                this.mTarget.setPinnedStackSize(this.mFrom, this.mTmpRect);
                if (this.mMoveToFullscreen) {
                    pause();
                }
            }
        }

        public void pause() {
            super.pause();
            BoundsAnimationController.this.mHandler.postDelayed(this.mResumeRunnable, 3000);
        }

        public void resume() {
            BoundsAnimationController.this.mHandler.removeCallbacks(this.mResumeRunnable);
            super.resume();
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            float value = ((Float) animation.getAnimatedValue()).floatValue();
            float remains = 1.0f - value;
            this.mTmpRect.left = (int) ((((float) this.mFrom.left) * remains) + (((float) this.mTo.left) * value) + 0.5f);
            this.mTmpRect.top = (int) ((((float) this.mFrom.top) * remains) + (((float) this.mTo.top) * value) + 0.5f);
            this.mTmpRect.right = (int) ((((float) this.mFrom.right) * remains) + (((float) this.mTo.right) * value) + 0.5f);
            this.mTmpRect.bottom = (int) ((((float) this.mFrom.bottom) * remains) + (((float) this.mTo.bottom) * value) + 0.5f);
            if (BoundsAnimationController.this.mIsPrintLog) {
                Slog.d(BoundsAnimationController.TAG, "animateUpdate: mTarget=" + this.mTarget + " mBounds=" + this.mTmpRect + " from=" + this.mFrom + " mTo=" + this.mTo + " value=" + value + " remains=" + remains);
            }
            this.mTmpTaskBounds.set(this.mTmpRect.left, this.mTmpRect.top, this.mTmpRect.left + this.mFrozenTaskWidth, this.mTmpRect.top + this.mFrozenTaskHeight);
            if (!this.mTarget.setPinnedStackSize(this.mTmpRect, this.mTmpTaskBounds)) {
                if (this.mSchedulePipModeChangedState == 1) {
                    this.mSchedulePipModeChangedState = 2;
                }
                cancelAndCallAnimationEnd();
            }
        }

        public void onAnimationEnd(Animator animation) {
            if (BoundsAnimationController.this.mIsPrintLog) {
                Slog.d(BoundsAnimationController.TAG, "onAnimationEnd: mTarget=" + this.mTarget + " mSkipFinalResize=" + this.mSkipFinalResize + " mFinishAnimationAfterTransition=" + BoundsAnimationController.this.mFinishAnimationAfterTransition + " mAppTransitionIsRunning=" + BoundsAnimationController.this.mAppTransition.isRunning());
            }
            boolean z = true;
            if (!BoundsAnimationController.this.mAppTransition.isRunning() || BoundsAnimationController.this.mFinishAnimationAfterTransition) {
                if (!this.mSkipAnimationEnd) {
                    if (BoundsAnimationController.this.mIsPrintLog) {
                        Slog.d(BoundsAnimationController.TAG, "onAnimationEnd: mTarget=" + this.mTarget + " moveToFullscreen=" + this.mMoveToFullscreen);
                    }
                    BoundsAnimationTarget boundsAnimationTarget = this.mTarget;
                    if (this.mSchedulePipModeChangedState != 2) {
                        z = false;
                    }
                    boundsAnimationTarget.onAnimationEnd(z, !this.mSkipFinalResize ? this.mTo : null, this.mMoveToFullscreen);
                }
                boolean unused = BoundsAnimationController.this.mIsPrintLog = false;
                removeListener(this);
                removeUpdateListener(this);
                synchronized (BoundsAnimationController.this.mLock) {
                    BoundsAnimationController.this.mRunningAnimations.remove(this.mTarget);
                }
                BoundsAnimationController.this.updateBooster();
                return;
            }
            boolean unused2 = BoundsAnimationController.this.mFinishAnimationAfterTransition = true;
        }

        public void onAnimationCancel(Animator animation) {
            this.mSkipFinalResize = true;
            this.mMoveToFullscreen = false;
        }

        private void cancelAndCallAnimationEnd() {
            if (BoundsAnimationController.this.mIsPrintLog) {
                Slog.d(BoundsAnimationController.TAG, "cancelAndCallAnimationEnd: mTarget=" + this.mTarget);
            }
            this.mSkipAnimationEnd = false;
            super.cancel();
        }

        public void cancel() {
            if (BoundsAnimationController.this.mIsPrintLog) {
                Slog.d(BoundsAnimationController.TAG, "cancel: mTarget=" + this.mTarget);
            }
            this.mSkipAnimationEnd = true;
            super.cancel();
        }

        /* access modifiers changed from: package-private */
        public boolean isAnimatingTo(Rect bounds) {
            return this.mTo.equals(bounds);
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public boolean animatingToLargerSize() {
            return this.mFrom.width() * this.mFrom.height() <= this.mTo.width() * this.mTo.height();
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public AnimationHandler getAnimationHandler() {
            if (BoundsAnimationController.this.mAnimationHandler != null) {
                return BoundsAnimationController.this.mAnimationHandler;
            }
            return super.getAnimationHandler();
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SchedulePipModeChangedState {
    }

    BoundsAnimationController(Context context, AppTransition transition, Handler handler, AnimationHandler animationHandler) {
        this.mHandler = handler;
        this.mAppTransition = transition;
        this.mAppTransition.registerListenerLocked(this.mAppTransitionNotifier);
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563661);
        this.mAnimationHandler = animationHandler;
        if (animationHandler != null) {
            handler.runWithScissors(new Runnable() {
                public final void run() {
                    BoundsAnimationController.this.mChoreographer = Choreographer.getSfInstance();
                }
            }, 0);
            animationHandler.setProvider(new SfVsyncFrameCallbackProvider(this.mChoreographer));
        }
    }

    public void animateBounds(BoundsAnimationTarget target, Rect from, Rect to, int animationDuration, int schedulePipModeChangedState, boolean moveFromFullscreen, boolean moveToFullscreen) {
        animateBoundsImpl(target, from, to, animationDuration, schedulePipModeChangedState, moveFromFullscreen, moveToFullscreen);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00bb, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00c4, code lost:
        r0 = th;
     */
    @VisibleForTesting
    public BoundsAnimator animateBoundsImpl(BoundsAnimationTarget target, Rect from, Rect to, int animationDuration, int schedulePipModeChangedState, boolean moveFromFullscreen, boolean moveToFullscreen) {
        BoundsAnimator existing;
        boolean moveToFullscreen2;
        boolean moveFromFullscreen2;
        int schedulePipModeChangedState2;
        Rect rect;
        boolean moveToFullscreen3;
        boolean moveFromFullscreen3;
        BoundsAnimationTarget boundsAnimationTarget = target;
        int schedulePipModeChangedState3 = schedulePipModeChangedState;
        synchronized (this.mLock) {
            try {
                existing = this.mRunningAnimations.get(boundsAnimationTarget);
            } catch (Throwable th) {
                th = th;
                Rect rect2 = to;
                int i = animationDuration;
                while (true) {
                    throw th;
                }
            }
        }
        int prevSchedulePipModeChangedState = 0;
        if (existing != null) {
            rect = to;
            if (existing.isAnimatingTo(rect) && ((!moveToFullscreen || existing.mMoveToFullscreen) && (!moveFromFullscreen || existing.mMoveFromFullscreen))) {
                return existing;
            }
            prevSchedulePipModeChangedState = existing.mSchedulePipModeChangedState;
            if (existing.mSchedulePipModeChangedState == 1) {
                if (schedulePipModeChangedState3 != 1) {
                    schedulePipModeChangedState3 = 2;
                }
            } else if (existing.mSchedulePipModeChangedState == 2 && schedulePipModeChangedState3 != 1) {
                schedulePipModeChangedState3 = 2;
            }
            if (moveFromFullscreen || moveToFullscreen) {
                moveFromFullscreen3 = moveFromFullscreen;
                moveToFullscreen3 = moveToFullscreen;
            } else {
                moveToFullscreen3 = existing.mMoveToFullscreen;
                moveFromFullscreen3 = existing.mMoveFromFullscreen;
            }
            existing.cancel();
            schedulePipModeChangedState2 = schedulePipModeChangedState3;
            moveFromFullscreen2 = moveFromFullscreen3;
            moveToFullscreen2 = moveToFullscreen3;
        } else {
            rect = to;
            moveFromFullscreen2 = moveFromFullscreen;
            moveToFullscreen2 = moveToFullscreen;
            schedulePipModeChangedState2 = schedulePipModeChangedState3;
        }
        BoundsAnimator boundsAnimator = new BoundsAnimator(boundsAnimationTarget, from, rect, schedulePipModeChangedState2, prevSchedulePipModeChangedState, moveFromFullscreen2, moveToFullscreen2);
        BoundsAnimator animator = boundsAnimator;
        synchronized (this.mLock) {
            try {
                this.mRunningAnimations.put(boundsAnimationTarget, animator);
            } catch (Throwable th2) {
                th = th2;
                int i2 = animationDuration;
                while (true) {
                    throw th;
                }
            }
        }
        animator.setFloatValues(new float[]{0.0f, 1.0f});
        int i3 = animationDuration;
        animator.setDuration((long) ((i3 != -1 ? i3 : DEFAULT_TRANSITION_DURATION) * 1));
        animator.setInterpolator(this.mFastOutSlowInInterpolator);
        animator.start();
        this.mIsPrintLog = true;
        return animator;
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    public void onAllWindowsDrawn() {
        this.mHandler.post(new Runnable() {
            public final void run() {
                BoundsAnimationController.this.resume();
            }
        });
    }

    /* access modifiers changed from: private */
    public void resume() {
        synchronized (this.mLock) {
            for (int i = 0; i < this.mRunningAnimations.size(); i++) {
                this.mRunningAnimations.valueAt(i).resume();
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateBooster() {
        boolean isEmpty;
        synchronized (this.mLock) {
            isEmpty = this.mRunningAnimations.isEmpty();
        }
        WindowManagerService.sThreadPriorityBooster.setBoundsAnimationRunning(!isEmpty);
    }
}
