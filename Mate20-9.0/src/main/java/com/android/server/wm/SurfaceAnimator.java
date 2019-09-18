package com.android.server.wm;

import android.os.SystemClock;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.SurfaceControl;
import com.android.internal.annotations.VisibleForTesting;
import java.io.PrintWriter;

class SurfaceAnimator {
    private static final int REPARENT_DELAY_TIME = 30;
    private static final String TAG = "WindowManager";
    private final Animatable mAnimatable;
    private AnimationAdapter mAnimation;
    @VisibleForTesting
    final Runnable mAnimationFinishedCallback;
    private boolean mAnimationStartDelayed;
    private long mLastLeashAnimation;
    @VisibleForTesting
    SurfaceControl mLeash;
    private SurfaceControl mProvisionalLeash;
    private final WindowManagerService mService;

    interface Animatable {
        void commitPendingTransaction();

        SurfaceControl getAnimationLeashParent();

        SurfaceControl getParentSurfaceControl();

        SurfaceControl.Transaction getPendingTransaction();

        SurfaceControl getSurfaceControl();

        int getSurfaceHeight();

        int getSurfaceWidth();

        SurfaceControl.Builder makeAnimationLeash();

        void onAnimationLeashCreated(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl);

        void onAnimationLeashDestroyed(SurfaceControl.Transaction transaction);

        boolean shouldDeferAnimationFinish(Runnable endDeferFinishCallback) {
            return false;
        }
    }

    interface OnAnimationFinishedCallback {
        void onAnimationFinished(AnimationAdapter animationAdapter);
    }

    SurfaceAnimator(Animatable animatable, Runnable animationFinishedCallback, WindowManagerService service) {
        this.mAnimatable = animatable;
        this.mService = service;
        this.mAnimationFinishedCallback = animationFinishedCallback;
    }

    private OnAnimationFinishedCallback getFinishedCallback() {
        return new OnAnimationFinishedCallback() {
            public final void onAnimationFinished(AnimationAdapter animationAdapter) {
                SurfaceAnimator.lambda$getFinishedCallback$1(SurfaceAnimator.this, animationAdapter);
            }
        };
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0043, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0046, code lost:
        return;
     */
    public static /* synthetic */ void lambda$getFinishedCallback$1(SurfaceAnimator surfaceAnimator, AnimationAdapter anim) {
        synchronized (surfaceAnimator.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                SurfaceAnimator target = surfaceAnimator.mService.mAnimationTransferMap.remove(anim);
                if (target != null) {
                    target.getFinishedCallback().onAnimationFinished(anim);
                    WindowManagerService.resetPriorityAfterLockedSection();
                } else if (anim != surfaceAnimator.mAnimation) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                } else {
                    surfaceAnimator.mService.mAnimationFinishMap.put(surfaceAnimator.mAnimatable, anim);
                    Runnable resetAndInvokeFinish = new Runnable() {
                        public final void run() {
                            SurfaceAnimator.lambda$getFinishedCallback$0(SurfaceAnimator.this);
                        }
                    };
                    if (!surfaceAnimator.mAnimatable.shouldDeferAnimationFinish(resetAndInvokeFinish)) {
                        resetAndInvokeFinish.run();
                    }
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public static /* synthetic */ void lambda$getFinishedCallback$0(SurfaceAnimator surfaceAnimator) {
        surfaceAnimator.reset(surfaceAnimator.mAnimatable.getPendingTransaction(), true);
        if (surfaceAnimator.mAnimationFinishedCallback != null) {
            surfaceAnimator.mAnimationFinishedCallback.run();
        }
        surfaceAnimator.mService.mAnimationFinishMap.remove(surfaceAnimator.mAnimatable);
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
        boolean delayed = this.mAnimationStartDelayed;
        this.mAnimationStartDelayed = false;
        if (delayed && this.mAnimation != null) {
            this.mAnimation.startAnimation(this.mLeash, this.mAnimatable.getPendingTransaction(), getFinishedCallback());
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
        t.setLayer(this.mLeash != null ? this.mLeash : this.mAnimatable.getSurfaceControl(), layer);
    }

    /* access modifiers changed from: package-private */
    public void setRelativeLayer(SurfaceControl.Transaction t, SurfaceControl relativeTo, int layer) {
        t.setRelativeLayer(this.mLeash != null ? this.mLeash : this.mAnimatable.getSurfaceControl(), relativeTo, layer);
    }

    /* access modifiers changed from: package-private */
    public void reparent(SurfaceControl.Transaction t, SurfaceControl newParent) {
        t.reparent(this.mLeash != null ? this.mLeash : this.mAnimatable.getSurfaceControl(), newParent.getHandle());
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
            if (this.mLeash == null) {
                Slog.w(TAG, "Unable to transfer animation, leash is null");
                return;
            }
            t.reparent(surface, this.mLeash.getHandle());
            t.reparent(this.mLeash, parent.getHandle());
            this.mAnimatable.onAnimationLeashCreated(t, this.mLeash);
            this.mService.mAnimationTransferMap.put(this.mAnimation, this);
            AnimationAdapter anim = this.mService.mAnimationFinishMap.remove(from.mAnimatable);
            if (anim != null && anim == this.mAnimation) {
                getFinishedCallback().onAnimationFinished(anim);
                Slog.w(TAG, "transferAnimation, finish animation");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimationStartDelayed() {
        return this.mAnimationStartDelayed;
    }

    private void cancelAnimation(SurfaceControl.Transaction t, boolean restarting, boolean forwardCancel) {
        SurfaceControl leash = this.mLeash;
        AnimationAdapter animation = this.mAnimation;
        reset(t, forwardCancel);
        if (animation != null) {
            if (!this.mAnimationStartDelayed && forwardCancel) {
                animation.onAnimationCancelled(leash);
            }
            if (!restarting) {
                this.mAnimationFinishedCallback.run();
            }
        }
        if (!restarting) {
            this.mAnimationStartDelayed = false;
        }
    }

    private void reset(SurfaceControl.Transaction t, boolean destroyLeash) {
        SurfaceControl surface = this.mAnimatable.getSurfaceControl();
        SurfaceControl parent = this.mAnimatable.getParentSurfaceControl();
        boolean scheduleAnim = false;
        boolean destroy = (this.mLeash == null || surface == null || parent == null) ? false : true;
        if (destroy) {
            t.reparent(surface, parent.getHandle());
            scheduleAnim = true;
        }
        this.mService.mAnimationTransferMap.remove(this.mAnimation);
        if (this.mLeash != null && destroyLeash) {
            t.destroy(this.mLeash);
            scheduleAnim = true;
        }
        if (this.mLeash != null) {
            this.mProvisionalLeash = this.mLeash;
        }
        this.mLeash = null;
        this.mAnimation = null;
        this.mLastLeashAnimation = SystemClock.uptimeMillis();
        if (destroy) {
            this.mAnimatable.onAnimationLeashDestroyed(t);
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
        SurfaceControl.Builder parent = this.mAnimatable.makeAnimationLeash().setParent(this.mAnimatable.getAnimationLeashParent());
        SurfaceControl leash = parent.setName(surface + " - animation-leash").setSize(width, height).build();
        if (!hidden) {
            t.show(leash);
        }
        t.reparent(surface, leash.getHandle());
        return leash;
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        if (this.mAnimation != null) {
            this.mAnimation.writeToProto(proto, 1146756268035L);
        }
        if (this.mLeash != null) {
            this.mLeash.writeToProto(proto, 1146756268033L);
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
        if (this.mAnimation != null) {
            AnimationAdapter animationAdapter = this.mAnimation;
            animationAdapter.dump(pw, prefix + "  ");
            return;
        }
        pw.print(prefix);
        pw.println("null");
    }
}
