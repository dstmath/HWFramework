package com.android.server.wm;

import android.animation.AnimationHandler;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.ArrayMap;
import android.view.Choreographer;
import android.view.SurfaceControl;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.graphics.SfVsyncFrameCallbackProvider;
import com.android.server.AnimationThread;
import com.android.server.wm.LocalAnimationAdapter;
import com.android.server.wm.SurfaceAnimationRunner;
import java.lang.annotation.RCUnownedCapRef;

class SurfaceAnimationRunner {
    /* access modifiers changed from: private */
    public final AnimationHandler mAnimationHandler;
    @GuardedBy("mLock")
    private boolean mAnimationStartDeferred;
    private final AnimatorFactory mAnimatorFactory;
    private boolean mApplyScheduled;
    private final Runnable mApplyTransactionRunnable;
    /* access modifiers changed from: private */
    public final Object mCancelLock;
    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Choreographer mChoreographer;
    /* access modifiers changed from: private */
    public final SurfaceControl.Transaction mFrameTransaction;
    /* access modifiers changed from: private */
    public final Object mLock;
    @GuardedBy("mLock")
    @VisibleForTesting
    final ArrayMap<SurfaceControl, RunningAnimation> mPendingAnimations;
    @GuardedBy("mLock")
    @VisibleForTesting
    final ArrayMap<SurfaceControl, RunningAnimation> mRunningAnimations;

    @VisibleForTesting
    interface AnimatorFactory {
        ValueAnimator makeAnimator();
    }

    private static final class RunningAnimation {
        ValueAnimator mAnim;
        final LocalAnimationAdapter.AnimationSpec mAnimSpec;
        /* access modifiers changed from: private */
        @GuardedBy("mCancelLock")
        public boolean mCancelled;
        final Runnable mFinishCallback;
        final SurfaceControl mLeash;

        RunningAnimation(LocalAnimationAdapter.AnimationSpec animSpec, SurfaceControl leash, Runnable finishCallback) {
            this.mAnimSpec = animSpec;
            this.mLeash = leash;
            this.mFinishCallback = finishCallback;
        }
    }

    private class SfValueAnimator extends ValueAnimator {
        SfValueAnimator() {
            setFloatValues(new float[]{0.0f, 1.0f});
        }

        public AnimationHandler getAnimationHandler() {
            return SurfaceAnimationRunner.this.mAnimationHandler;
        }
    }

    SurfaceAnimationRunner() {
        this(null, null, new SurfaceControl.Transaction());
    }

    @VisibleForTesting
    SurfaceAnimationRunner(AnimationHandler.AnimationFrameCallbackProvider callbackProvider, AnimatorFactory animatorFactory, SurfaceControl.Transaction frameTransaction) {
        AnimationHandler.AnimationFrameCallbackProvider animationFrameCallbackProvider;
        this.mLock = new Object();
        this.mCancelLock = new Object();
        this.mApplyTransactionRunnable = new Runnable() {
            public final void run() {
                SurfaceAnimationRunner.this.applyTransaction();
            }
        };
        this.mPendingAnimations = new ArrayMap<>();
        this.mRunningAnimations = new ArrayMap<>();
        SurfaceAnimationThread.getHandler().runWithScissors(new Runnable() {
            public final void run() {
                SurfaceAnimationRunner.this.mChoreographer = Choreographer.getSfInstance();
            }
        }, 0);
        this.mFrameTransaction = frameTransaction;
        this.mAnimationHandler = new AnimationHandler();
        AnimationHandler animationHandler = this.mAnimationHandler;
        if (callbackProvider != null) {
            animationFrameCallbackProvider = callbackProvider;
        } else {
            animationFrameCallbackProvider = new SfVsyncFrameCallbackProvider(this.mChoreographer);
        }
        animationHandler.setProvider(animationFrameCallbackProvider);
        this.mAnimatorFactory = animatorFactory != null ? animatorFactory : new AnimatorFactory() {
            public final ValueAnimator makeAnimator() {
                return SurfaceAnimationRunner.lambda$new$1(SurfaceAnimationRunner.this);
            }
        };
    }

    public static /* synthetic */ ValueAnimator lambda$new$1(SurfaceAnimationRunner surfaceAnimationRunner) {
        return new SfValueAnimator();
    }

    /* access modifiers changed from: package-private */
    public void deferStartingAnimations() {
        synchronized (this.mLock) {
            this.mAnimationStartDeferred = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void continueStartingAnimations() {
        synchronized (this.mLock) {
            this.mAnimationStartDeferred = false;
            if (!this.mPendingAnimations.isEmpty()) {
                this.mChoreographer.postFrameCallback(new Choreographer.FrameCallback() {
                    public final void doFrame(long j) {
                        SurfaceAnimationRunner.this.startAnimations(j);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void startAnimation(LocalAnimationAdapter.AnimationSpec a, SurfaceControl animationLeash, SurfaceControl.Transaction t, Runnable finishCallback) {
        synchronized (this.mLock) {
            RunningAnimation runningAnim = new RunningAnimation(a, animationLeash, finishCallback);
            this.mPendingAnimations.put(animationLeash, runningAnim);
            if (!this.mAnimationStartDeferred) {
                this.mChoreographer.postFrameCallback(new Choreographer.FrameCallback() {
                    public final void doFrame(long j) {
                        SurfaceAnimationRunner.this.startAnimations(j);
                    }
                });
            }
            applyTransformation(runningAnim, t, 0);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x003a, code lost:
        return;
     */
    public void onAnimationCancelled(SurfaceControl leash) {
        synchronized (this.mLock) {
            if (this.mPendingAnimations.containsKey(leash)) {
                this.mPendingAnimations.remove(leash);
                return;
            }
            RunningAnimation anim = this.mRunningAnimations.get(leash);
            if (anim != null) {
                this.mRunningAnimations.remove(leash);
                synchronized (this.mCancelLock) {
                    boolean unused = anim.mCancelled = true;
                }
                SurfaceAnimationThread.getHandler().post(new Runnable(anim) {
                    private final /* synthetic */ SurfaceAnimationRunner.RunningAnimation f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        SurfaceAnimationRunner.lambda$onAnimationCancelled$2(SurfaceAnimationRunner.this, this.f$1);
                    }
                });
            }
        }
    }

    public static /* synthetic */ void lambda$onAnimationCancelled$2(SurfaceAnimationRunner surfaceAnimationRunner, RunningAnimation anim) {
        anim.mAnim.cancel();
        surfaceAnimationRunner.applyTransaction();
    }

    @GuardedBy("mLock")
    private void startPendingAnimationsLocked() {
        for (int i = this.mPendingAnimations.size() - 1; i >= 0; i--) {
            startAnimationLocked(this.mPendingAnimations.valueAt(i));
        }
        this.mPendingAnimations.clear();
    }

    @GuardedBy("mLock")
    @RCUnownedCapRef.List({@RCUnownedCapRef("a"), @RCUnownedCapRef("anim")})
    private void startAnimationLocked(final RunningAnimation a) {
        ValueAnimator anim = this.mAnimatorFactory.makeAnimator();
        anim.overrideDurationScale(1.0f);
        anim.setDuration(a.mAnimSpec.getDuration());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(a, anim) {
            private final /* synthetic */ SurfaceAnimationRunner.RunningAnimation f$1;
            private final /* synthetic */ ValueAnimator f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                SurfaceAnimationRunner.lambda$startAnimationLocked$3(SurfaceAnimationRunner.this, this.f$1, this.f$2, valueAnimator);
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                synchronized (SurfaceAnimationRunner.this.mCancelLock) {
                    if (!a.mCancelled) {
                        SurfaceAnimationRunner.this.mFrameTransaction.show(a.mLeash);
                    }
                }
            }

            public void onAnimationEnd(Animator animation) {
                synchronized (SurfaceAnimationRunner.this.mLock) {
                    SurfaceAnimationRunner.this.mRunningAnimations.remove(a.mLeash);
                    synchronized (SurfaceAnimationRunner.this.mCancelLock) {
                        if (!a.mCancelled) {
                            AnimationThread.getHandler().post(a.mFinishCallback);
                        }
                    }
                }
            }
        });
        a.mAnim = anim;
        this.mRunningAnimations.put(a.mLeash, a);
        anim.start();
        if (a.mAnimSpec.canSkipFirstFrame()) {
            anim.setCurrentPlayTime(this.mChoreographer.getFrameIntervalNanos() / 1000000);
        }
        anim.doAnimationFrame(this.mChoreographer.getFrameTime());
    }

    public static /* synthetic */ void lambda$startAnimationLocked$3(SurfaceAnimationRunner surfaceAnimationRunner, RunningAnimation rc_cap_a, ValueAnimator anim, ValueAnimator animation) {
        synchronized (surfaceAnimationRunner.mCancelLock) {
            if (!rc_cap_a.mCancelled) {
                long duration = anim.getDuration();
                long currentPlayTime = anim.getCurrentPlayTime();
                if (currentPlayTime > duration) {
                    currentPlayTime = duration;
                }
                surfaceAnimationRunner.applyTransformation(rc_cap_a, surfaceAnimationRunner.mFrameTransaction, currentPlayTime);
            }
        }
        surfaceAnimationRunner.scheduleApplyTransaction();
    }

    private void applyTransformation(RunningAnimation a, SurfaceControl.Transaction t, long currentPlayTime) {
        if (a.mAnimSpec.needsEarlyWakeup()) {
            t.setEarlyWakeup();
        }
        a.mAnimSpec.apply(t, a.mLeash, currentPlayTime);
    }

    /* access modifiers changed from: private */
    public void startAnimations(long frameTimeNanos) {
        synchronized (this.mLock) {
            startPendingAnimationsLocked();
        }
    }

    private void scheduleApplyTransaction() {
        if (!this.mApplyScheduled) {
            this.mChoreographer.postCallback(2, this.mApplyTransactionRunnable, null);
            this.mApplyScheduled = true;
        }
    }

    /* access modifiers changed from: private */
    public void applyTransaction() {
        this.mFrameTransaction.setAnimationTransaction();
        this.mFrameTransaction.apply();
        this.mApplyScheduled = false;
    }
}
