package com.android.server.wm;

import android.os.SystemClock;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.SurfaceControl;
import com.android.internal.annotations.VisibleForTesting;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public class SurfaceAnimator {
    private static final int REPARENT_DELAY_TIME = 30;
    private static final String TAG = "WindowManager";
    @VisibleForTesting
    final Animatable mAnimatable;
    private AnimationAdapter mAnimation;
    @VisibleForTesting
    final Runnable mAnimationFinishedCallback;
    private boolean mAnimationStartDelayed;
    private long mLastLeashAnimation;
    @VisibleForTesting
    SurfaceControl mLeash;
    private SurfaceControl mProvisionalLeash;
    private final WindowManagerService mService;

    /* access modifiers changed from: package-private */
    public interface OnAnimationFinishedCallback {
        void onAnimationFinished(AnimationAdapter animationAdapter);
    }

    SurfaceAnimator(Animatable animatable, Runnable animationFinishedCallback, WindowManagerService service) {
        this.mAnimatable = animatable;
        this.mService = service;
        this.mAnimationFinishedCallback = animationFinishedCallback;
    }

    private OnAnimationFinishedCallback getFinishedCallback() {
        return new OnAnimationFinishedCallback() {
            /* class com.android.server.wm.$$Lambda$SurfaceAnimator$PAdchxmxQxv_tWqu4JCd2Kfof3U */

            @Override // com.android.server.wm.SurfaceAnimator.OnAnimationFinishedCallback
            public final void onAnimationFinished(AnimationAdapter animationAdapter) {
                SurfaceAnimator.this.lambda$getFinishedCallback$1$SurfaceAnimator(animationAdapter);
            }
        };
    }

    public /* synthetic */ void lambda$getFinishedCallback$1$SurfaceAnimator(AnimationAdapter anim) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                SurfaceAnimator target = this.mService.mAnimationTransferMap.remove(anim);
                if (target != null) {
                    Slog.i(TAG, "mAnimationTransferMap remove mAnimatable:" + this.mAnimatable + " anim:" + anim);
                    target.getFinishedCallback().onAnimationFinished(anim);
                } else if (anim != this.mAnimation) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                } else {
                    Runnable resetAndInvokeFinish = new Runnable(anim) {
                        /* class com.android.server.wm.$$Lambda$SurfaceAnimator$ca6KMjW34RfmCUBeu6xgkX_Z2k */
                        private final /* synthetic */ AnimationAdapter f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            SurfaceAnimator.this.lambda$getFinishedCallback$0$SurfaceAnimator(this.f$1);
                        }
                    };
                    if (!this.mAnimatable.shouldDeferAnimationFinish(resetAndInvokeFinish)) {
                        resetAndInvokeFinish.run();
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public /* synthetic */ void lambda$getFinishedCallback$0$SurfaceAnimator(AnimationAdapter anim) {
        if (anim == this.mAnimation) {
            reset(this.mAnimatable.getPendingTransaction(), true);
            Runnable runnable = this.mAnimationFinishedCallback;
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void startAnimation(SurfaceControl.Transaction t, AnimationAdapter anim, boolean hidden) {
        cancelAnimation(t, true, true);
        this.mAnimation = anim;
        SurfaceControl surface = this.mAnimatable.getSurfaceControl();
        if (surface == null) {
            Slog.w(TAG, "Unable to start animation, surface is null or no children.");
            cancelAnimation();
            return;
        }
        this.mLeash = createAnimationLeash(surface, t, this.mAnimatable.getSurfaceWidth(), this.mAnimatable.getSurfaceHeight(), hidden);
        this.mAnimatable.onAnimationLeashCreated(t, this.mLeash);
        if (!this.mAnimationStartDelayed) {
            this.mAnimation.startAnimation(this.mLeash, t, getFinishedCallback());
        } else if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.i(TAG, "Animation start delayed");
        }
    }

    /* access modifiers changed from: package-private */
    public void startDelayingAnimationStart() {
        if (!isAnimating()) {
            this.mAnimationStartDelayed = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void endDelayingAnimationStart() {
        AnimationAdapter animationAdapter;
        boolean delayed = this.mAnimationStartDelayed;
        this.mAnimationStartDelayed = false;
        if (delayed && (animationAdapter = this.mAnimation) != null) {
            animationAdapter.startAnimation(this.mLeash, this.mAnimatable.getPendingTransaction(), getFinishedCallback());
            this.mAnimatable.commitPendingTransaction();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimating() {
        return this.mAnimation != null;
    }

    /* access modifiers changed from: package-private */
    public AnimationAdapter getAnimation() {
        return this.mAnimation;
    }

    /* access modifiers changed from: package-private */
    public void cancelAnimation() {
        cancelAnimation(this.mAnimatable.getPendingTransaction(), false, true);
        this.mAnimatable.commitPendingTransaction();
    }

    /* access modifiers changed from: package-private */
    public void setLayer(SurfaceControl.Transaction t, int layer) {
        SurfaceControl surfaceControl = this.mLeash;
        if (surfaceControl == null) {
            surfaceControl = this.mAnimatable.getSurfaceControl();
        }
        t.setLayer(surfaceControl, layer);
    }

    /* access modifiers changed from: package-private */
    public void setRelativeLayer(SurfaceControl.Transaction t, SurfaceControl relativeTo, int layer) {
        SurfaceControl surfaceControl = this.mLeash;
        if (surfaceControl == null) {
            surfaceControl = this.mAnimatable.getSurfaceControl();
        }
        t.setRelativeLayer(surfaceControl, relativeTo, layer);
    }

    /* access modifiers changed from: package-private */
    public void reparent(SurfaceControl.Transaction t, SurfaceControl newParent) {
        SurfaceControl surfaceControl = this.mLeash;
        if (surfaceControl == null) {
            surfaceControl = this.mAnimatable.getSurfaceControl();
        }
        t.reparent(surfaceControl, newParent);
    }

    /* access modifiers changed from: package-private */
    public boolean hasLeash() {
        return this.mLeash != null;
    }

    /* access modifiers changed from: package-private */
    public void transferAnimation(SurfaceAnimator from) {
        if (from.mLeash != null) {
            SurfaceControl surface = this.mAnimatable.getSurfaceControl();
            SurfaceControl parent = this.mAnimatable.getAnimationLeashParent();
            if (surface == null || parent == null) {
                Slog.w(TAG, "Unable to transfer animation, surface or parent is null");
                cancelAnimation();
                return;
            }
            endDelayingAnimationStart();
            SurfaceControl.Transaction t = this.mAnimatable.getPendingTransaction();
            cancelAnimation(t, true, true);
            this.mLeash = from.mLeash;
            this.mAnimation = from.mAnimation;
            from.cancelAnimation(t, false, false);
            SurfaceControl surfaceControl = this.mLeash;
            if (surfaceControl == null) {
                Slog.w(TAG, "Unable to transfer animation, leash is null");
                return;
            }
            t.reparent(surface, surfaceControl);
            t.reparent(this.mLeash, parent);
            this.mAnimatable.onAnimationLeashCreated(t, this.mLeash);
            this.mService.mAnimationTransferMap.put(this.mAnimation, this);
            Slog.i(TAG, "mAnimationTransferMap add mAnimation:" + this.mAnimation + " mAnimatable:" + this.mAnimatable);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimationStartDelayed() {
        return this.mAnimationStartDelayed;
    }

    private void cancelAnimation(SurfaceControl.Transaction t, boolean restarting, boolean forwardCancel) {
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.i(TAG, "Cancelling animation restarting=" + restarting);
        }
        SurfaceControl leash = this.mLeash;
        AnimationAdapter animation = this.mAnimation;
        reset(t, false);
        if (animation != null) {
            if (!this.mAnimationStartDelayed && forwardCancel) {
                animation.onAnimationCancelled(leash);
            }
            if (!restarting) {
                this.mAnimationFinishedCallback.run();
            }
        }
        if (forwardCancel && leash != null) {
            t.remove(leash);
            this.mService.scheduleAnimationLocked();
        }
        if (!restarting) {
            this.mAnimationStartDelayed = false;
        }
    }

    private void reset(SurfaceControl.Transaction t, boolean destroyLeash) {
        SurfaceControl surface = this.mAnimatable.getSurfaceControl();
        SurfaceControl parent = this.mAnimatable.getParentSurfaceControl();
        boolean scheduleAnim = false;
        boolean isAvoidRemoveLeash = true;
        boolean reparent = (this.mLeash == null || surface == null) ? false : true;
        if (reparent) {
            if (WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.i(TAG, "Reparenting to original parent: " + parent);
            }
            if (surface.isValid() && parent != null && parent.isValid()) {
                t.reparent(surface, parent);
                scheduleAnim = true;
            }
        }
        this.mService.mAnimationTransferMap.remove(this.mAnimation);
        if (surface != null) {
            isAvoidRemoveLeash = false;
        }
        SurfaceControl surfaceControl = this.mLeash;
        if (surfaceControl != null && destroyLeash && !isAvoidRemoveLeash) {
            t.remove(surfaceControl);
            scheduleAnim = true;
        }
        SurfaceControl surfaceControl2 = this.mLeash;
        if (surfaceControl2 != null) {
            this.mProvisionalLeash = surfaceControl2;
        }
        this.mLeash = null;
        this.mAnimation = null;
        this.mLastLeashAnimation = SystemClock.uptimeMillis();
        if (reparent) {
            this.mAnimatable.onAnimationLeashLost(t);
            scheduleAnim = true;
        }
        if (scheduleAnim) {
            this.mService.scheduleAnimationLocked();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isLeashAnimationDoing() {
        return SystemClock.uptimeMillis() - this.mLastLeashAnimation < 30;
    }

    private SurfaceControl createAnimationLeash(SurfaceControl surface, SurfaceControl.Transaction t, int width, int height, boolean hidden) {
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.i(TAG, "Reparenting to leash");
        }
        SurfaceControl.Builder parent = this.mAnimatable.makeAnimationLeash().setParent(this.mAnimatable.getAnimationLeashParent());
        SurfaceControl leash = parent.setName(surface + " - animation-leash").build();
        t.setWindowCrop(leash, width, height);
        if (!hidden) {
            t.show(leash);
        }
        t.reparent(surface, leash);
        return leash;
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        AnimationAdapter animationAdapter = this.mAnimation;
        if (animationAdapter != null) {
            animationAdapter.writeToProto(proto, 1146756268035L);
        }
        SurfaceControl surfaceControl = this.mLeash;
        if (surfaceControl != null) {
            surfaceControl.writeToProto(proto, 1146756268033L);
        }
        proto.write(1133871366146L, this.mAnimationStartDelayed);
        proto.end(token);
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mLeash=");
        pw.print(this.mLeash);
        if (this.mAnimationStartDelayed) {
            pw.print(" mAnimationStartDelayed=");
            pw.println(this.mAnimationStartDelayed);
        } else {
            pw.println();
        }
        pw.print(prefix);
        pw.println("Animation:");
        AnimationAdapter animationAdapter = this.mAnimation;
        if (animationAdapter != null) {
            animationAdapter.dump(pw, prefix + "  ");
            return;
        }
        pw.print(prefix);
        pw.println("null");
    }

    /* access modifiers changed from: package-private */
    public interface Animatable {
        void commitPendingTransaction();

        SurfaceControl getAnimationLeashParent();

        SurfaceControl getParentSurfaceControl();

        SurfaceControl.Transaction getPendingTransaction();

        SurfaceControl getSurfaceControl();

        int getSurfaceHeight();

        int getSurfaceWidth();

        SurfaceControl.Builder makeAnimationLeash();

        void onAnimationLeashCreated(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl);

        void onAnimationLeashLost(SurfaceControl.Transaction transaction);

        default boolean shouldDeferAnimationFinish(Runnable endDeferFinishCallback) {
            return false;
        }
    }
}
