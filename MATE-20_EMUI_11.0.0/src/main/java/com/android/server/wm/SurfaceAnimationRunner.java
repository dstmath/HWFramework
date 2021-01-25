package com.android.server.wm;

import android.animation.AnimationHandler;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.PowerManagerInternal;
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

/* access modifiers changed from: package-private */
public class SurfaceAnimationRunner {
    private final AnimationHandler mAnimationHandler;
    @GuardedBy({"mLock"})
    private boolean mAnimationStartDeferred;
    private final AnimatorFactory mAnimatorFactory;
    private boolean mApplyScheduled;
    private final Runnable mApplyTransactionRunnable;
    private final Object mCancelLock;
    @VisibleForTesting
    Choreographer mChoreographer;
    private final SurfaceControl.Transaction mFrameTransaction;
    private final Object mLock;
    @GuardedBy({"mLock"})
    @VisibleForTesting
    final ArrayMap<SurfaceControl, RunningAnimation> mPendingAnimations;
    private final PowerManagerInternal mPowerManagerInternal;
    @GuardedBy({"mLock"})
    @VisibleForTesting
    final ArrayMap<SurfaceControl, RunningAnimation> mRunningAnimations;

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public interface AnimatorFactory {
        ValueAnimator makeAnimator();
    }

    SurfaceAnimationRunner(PowerManagerInternal powerManagerInternal) {
        this(null, null, new SurfaceControl.Transaction(), powerManagerInternal);
    }

    @VisibleForTesting
    SurfaceAnimationRunner(AnimationHandler.AnimationFrameCallbackProvider callbackProvider, AnimatorFactory animatorFactory, SurfaceControl.Transaction frameTransaction, PowerManagerInternal powerManagerInternal) {
        AnimationHandler.AnimationFrameCallbackProvider animationFrameCallbackProvider;
        AnimatorFactory animatorFactory2;
        this.mLock = new Object();
        this.mCancelLock = new Object();
        this.mApplyTransactionRunnable = new Runnable() {
            /* class com.android.server.wm.$$Lambda$SurfaceAnimationRunner$lSzwjoKEGADoEFOzdEnwriAk0T4 */

            @Override // java.lang.Runnable
            public final void run() {
                SurfaceAnimationRunner.this.applyTransaction();
            }
        };
        this.mPendingAnimations = new ArrayMap<>();
        this.mRunningAnimations = new ArrayMap<>();
        SurfaceAnimationThread.getHandler().runWithScissors(new Runnable() {
            /* class com.android.server.wm.$$Lambda$SurfaceAnimationRunner$xDyZdsMrcbp64p4BQmOGPvVnSWA */

            @Override // java.lang.Runnable
            public final void run() {
                SurfaceAnimationRunner.this.lambda$new$0$SurfaceAnimationRunner();
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
        if (animatorFactory != null) {
            animatorFactory2 = animatorFactory;
        } else {
            animatorFactory2 = new AnimatorFactory() {
                /* class com.android.server.wm.$$Lambda$SurfaceAnimationRunner$we7K92eAl3biB_bzyqbv5xCmasE */

                @Override // com.android.server.wm.SurfaceAnimationRunner.AnimatorFactory
                public final ValueAnimator makeAnimator() {
                    return SurfaceAnimationRunner.this.lambda$new$1$SurfaceAnimationRunner();
                }
            };
        }
        this.mAnimatorFactory = animatorFactory2;
        this.mPowerManagerInternal = powerManagerInternal;
        Choreographer choreographer = this.mChoreographer;
        if (choreographer != null) {
            choreographer.isWmsAnimClass = true;
        }
    }

    public /* synthetic */ void lambda$new$0$SurfaceAnimationRunner() {
        this.mChoreographer = Choreographer.getSfInstance();
    }

    public /* synthetic */ ValueAnimator lambda$new$1$SurfaceAnimationRunner() {
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
                    /* class com.android.server.wm.$$Lambda$SurfaceAnimationRunner$9Wa9MhcrSX12liOouHtYXEkDU60 */

                    @Override // android.view.Choreographer.FrameCallback
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
                    /* class com.android.server.wm.$$Lambda$SurfaceAnimationRunner$9Wa9MhcrSX12liOouHtYXEkDU60 */

                    @Override // android.view.Choreographer.FrameCallback
                    public final void doFrame(long j) {
                        SurfaceAnimationRunner.this.startAnimations(j);
                    }
                });
            }
            applyTransformation(runningAnim, t, 0);
        }
    }

    /* access modifiers changed from: package-private */
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
                    anim.mCancelled = true;
                }
                SurfaceAnimationThread.getHandler().post(new Runnable(anim) {
                    /* class com.android.server.wm.$$Lambda$SurfaceAnimationRunner$SGOilG6qRe0XTsTJRQqQKhta0pA */
                    private final /* synthetic */ SurfaceAnimationRunner.RunningAnimation f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        SurfaceAnimationRunner.this.lambda$onAnimationCancelled$2$SurfaceAnimationRunner(this.f$1);
                    }
                });
            }
        }
    }

    public /* synthetic */ void lambda$onAnimationCancelled$2$SurfaceAnimationRunner(RunningAnimation anim) {
        anim.mAnim.cancel();
        applyTransaction();
    }

    @GuardedBy({"mLock"})
    private void startPendingAnimationsLocked() {
        for (int i = this.mPendingAnimations.size() - 1; i >= 0; i--) {
            startAnimationLocked(this.mPendingAnimations.valueAt(i));
        }
        this.mPendingAnimations.clear();
    }

    @GuardedBy({"mLock"})
    @RCUnownedCapRef.List({@RCUnownedCapRef("a"), @RCUnownedCapRef("anim")})
    private void startAnimationLocked(final RunningAnimation a) {
        ValueAnimator anim = this.mAnimatorFactory.makeAnimator();
        anim.overrideDurationScale(1.0f);
        anim.setDuration(a.mAnimSpec.getDuration());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(a, anim) {
            /* class com.android.server.wm.$$Lambda$SurfaceAnimationRunner$puhYAP5tF0mSSJvaeUz59HnrkA */
            private final /* synthetic */ SurfaceAnimationRunner.RunningAnimation f$1;
            private final /* synthetic */ ValueAnimator f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                SurfaceAnimationRunner.this.lambda$startAnimationLocked$3$SurfaceAnimationRunner(this.f$1, this.f$2, valueAnimator);
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            /* class com.android.server.wm.SurfaceAnimationRunner.AnonymousClass1 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animation) {
                synchronized (SurfaceAnimationRunner.this.mCancelLock) {
                    if (!a.mCancelled) {
                        SurfaceAnimationRunner.this.mFrameTransaction.show(a.mLeash);
                    }
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
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

    public /* synthetic */ void lambda$startAnimationLocked$3$SurfaceAnimationRunner(RunningAnimation rcCapA, ValueAnimator anim, ValueAnimator animation) {
        synchronized (this.mCancelLock) {
            if (!rcCapA.mCancelled) {
                long duration = anim.getDuration();
                long currentPlayTime = anim.getCurrentPlayTime();
                if (currentPlayTime > duration) {
                    currentPlayTime = duration;
                }
                applyTransformation(rcCapA, this.mFrameTransaction, currentPlayTime);
            }
        }
        scheduleApplyTransaction();
    }

    private void applyTransformation(RunningAnimation a, SurfaceControl.Transaction t, long currentPlayTime) {
        if (a.mAnimSpec.needsEarlyWakeup()) {
            t.setEarlyWakeup();
        }
        if (a.mLeash != null && a.mLeash.isValid()) {
            a.mAnimSpec.apply(t, a.mLeash, currentPlayTime);
        }
    }

    /* access modifiers changed from: private */
    public void startAnimations(long frameTimeNanos) {
        synchronized (this.mLock) {
            startPendingAnimationsLocked();
        }
        this.mPowerManagerInternal.powerHint(2, 0);
    }

    private void scheduleApplyTransaction() {
        if (!this.mApplyScheduled) {
            this.mChoreographer.postCallback(3, this.mApplyTransactionRunnable, null);
            this.mApplyScheduled = true;
        }
    }

    /* access modifiers changed from: private */
    public void applyTransaction() {
        this.mFrameTransaction.setAnimationTransaction();
        this.mFrameTransaction.apply();
        this.mApplyScheduled = false;
    }

    /* access modifiers changed from: private */
    public static final class RunningAnimation {
        ValueAnimator mAnim;
        final LocalAnimationAdapter.AnimationSpec mAnimSpec;
        @GuardedBy({"mCancelLock"})
        private boolean mCancelled;
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
            setFloatValues(0.0f, 1.0f);
        }

        public AnimationHandler getAnimationHandler() {
            return SurfaceAnimationRunner.this.mAnimationHandler;
        }
    }
}
