package com.android.server.wm;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.util.ArrayMap;
import android.view.WindowManagerInternal.AppTransitionListener;
import android.view.animation.LinearInterpolator;

public class BoundsAnimationController {
    private static final boolean DEBUG = false;
    private static final int DEBUG_ANIMATION_SLOW_DOWN_FACTOR = 1;
    private static final boolean DEBUG_LOCAL = false;
    private static final String TAG = null;
    private final AppTransition mAppTransition;
    private final AppTransitionNotifier mAppTransitionNotifier;
    private boolean mFinishAnimationAfterTransition;
    private final Handler mHandler;
    private ArrayMap<AnimateBoundsUser, BoundsAnimator> mRunningAnimations;

    public interface AnimateBoundsUser {
        void getFullScreenBounds(Rect rect);

        void moveToFullscreen();

        void onAnimationEnd();

        void onAnimationStart();

        boolean setPinnedStackSize(Rect rect, Rect rect2);

        boolean setSize(Rect rect);
    }

    private final class AppTransitionNotifier extends AppTransitionListener implements Runnable {
        private AppTransitionNotifier() {
        }

        public void onAppTransitionCancelledLocked() {
            animationFinished();
        }

        public void onAppTransitionFinishedLocked(IBinder token) {
            animationFinished();
        }

        private void animationFinished() {
            if (BoundsAnimationController.this.mFinishAnimationAfterTransition) {
                BoundsAnimationController.this.mHandler.removeCallbacks(this);
                BoundsAnimationController.this.mHandler.post(this);
            }
        }

        public void run() {
            for (int i = 0; i < BoundsAnimationController.this.mRunningAnimations.size(); i += BoundsAnimationController.DEBUG_ANIMATION_SLOW_DOWN_FACTOR) {
                ((BoundsAnimator) BoundsAnimationController.this.mRunningAnimations.valueAt(i)).onAnimationEnd(null);
            }
        }
    }

    private final class BoundsAnimator extends ValueAnimator implements AnimatorUpdateListener, AnimatorListener {
        private final Rect mFrom;
        private final int mFrozenTaskHeight;
        private final int mFrozenTaskWidth;
        private final boolean mMoveToFullScreen;
        private final boolean mReplacement;
        private final AnimateBoundsUser mTarget;
        private final Rect mTmpRect;
        private final Rect mTmpTaskBounds;
        private final Rect mTo;
        private boolean mWillReplace;

        BoundsAnimator(AnimateBoundsUser target, Rect from, Rect to, boolean moveToFullScreen, boolean replacement) {
            this.mTmpRect = new Rect();
            this.mTmpTaskBounds = new Rect();
            this.mTarget = target;
            this.mFrom = from;
            this.mTo = to;
            this.mMoveToFullScreen = moveToFullScreen;
            this.mReplacement = replacement;
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

        boolean animatingToLargerSize() {
            if (this.mFrom.width() * this.mFrom.height() > this.mTo.width() * this.mTo.height()) {
                return BoundsAnimationController.DEBUG;
            }
            return true;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            float value = ((Float) animation.getAnimatedValue()).floatValue();
            float remains = 1.0f - value;
            this.mTmpRect.left = (int) (((((float) this.mFrom.left) * remains) + (((float) this.mTo.left) * value)) + TaskPositioner.RESIZING_HINT_ALPHA);
            this.mTmpRect.top = (int) (((((float) this.mFrom.top) * remains) + (((float) this.mTo.top) * value)) + TaskPositioner.RESIZING_HINT_ALPHA);
            this.mTmpRect.right = (int) (((((float) this.mFrom.right) * remains) + (((float) this.mTo.right) * value)) + TaskPositioner.RESIZING_HINT_ALPHA);
            this.mTmpRect.bottom = (int) (((((float) this.mFrom.bottom) * remains) + (((float) this.mTo.bottom) * value)) + TaskPositioner.RESIZING_HINT_ALPHA);
            this.mTmpTaskBounds.set(this.mTmpRect.left, this.mTmpRect.top, this.mTmpRect.left + this.mFrozenTaskWidth, this.mTmpRect.top + this.mFrozenTaskHeight);
            if (!this.mTarget.setPinnedStackSize(this.mTmpRect, this.mTmpTaskBounds)) {
                animation.cancel();
            }
        }

        public void onAnimationStart(Animator animation) {
            BoundsAnimationController.this.mFinishAnimationAfterTransition = BoundsAnimationController.DEBUG;
            if (!this.mReplacement) {
                this.mTarget.onAnimationStart();
            }
            if (animatingToLargerSize()) {
                this.mTmpRect.set(this.mFrom.left, this.mFrom.top, this.mFrom.left + this.mFrozenTaskWidth, this.mFrom.top + this.mFrozenTaskHeight);
                this.mTarget.setPinnedStackSize(this.mFrom, this.mTmpRect);
            }
        }

        public void onAnimationEnd(Animator animation) {
            if (!BoundsAnimationController.this.mAppTransition.isRunning() || BoundsAnimationController.this.mFinishAnimationAfterTransition) {
                finishAnimation();
                this.mTarget.setPinnedStackSize(this.mTo, null);
                if (this.mMoveToFullScreen && !this.mWillReplace) {
                    this.mTarget.moveToFullscreen();
                }
                return;
            }
            BoundsAnimationController.this.mFinishAnimationAfterTransition = true;
        }

        public void onAnimationCancel(Animator animation) {
            finishAnimation();
        }

        public void cancel() {
            this.mWillReplace = true;
            super.cancel();
        }

        public boolean isAnimatingTo(Rect bounds) {
            return this.mTo.equals(bounds);
        }

        private void finishAnimation() {
            if (!this.mWillReplace) {
                this.mTarget.onAnimationEnd();
            }
            removeListener(this);
            removeUpdateListener(this);
            BoundsAnimationController.this.mRunningAnimations.remove(this.mTarget);
        }

        public void onAnimationRepeat(Animator animation) {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.BoundsAnimationController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.BoundsAnimationController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.BoundsAnimationController.<clinit>():void");
    }

    BoundsAnimationController(AppTransition transition, Handler handler) {
        this.mRunningAnimations = new ArrayMap();
        this.mAppTransitionNotifier = new AppTransitionNotifier();
        this.mFinishAnimationAfterTransition = DEBUG;
        this.mHandler = handler;
        this.mAppTransition = transition;
        this.mAppTransition.registerListenerLocked(this.mAppTransitionNotifier);
    }

    void animateBounds(AnimateBoundsUser target, Rect from, Rect to, int animationDuration) {
        boolean moveToFullscreen = DEBUG;
        if (to == null) {
            to = new Rect();
            target.getFullScreenBounds(to);
            moveToFullscreen = true;
        }
        BoundsAnimator existing = (BoundsAnimator) this.mRunningAnimations.get(target);
        boolean replacing = existing != null ? true : DEBUG;
        if (replacing) {
            if (!existing.isAnimatingTo(to)) {
                existing.cancel();
            } else {
                return;
            }
        }
        BoundsAnimator animator = new BoundsAnimator(target, from, to, moveToFullscreen, replacing);
        this.mRunningAnimations.put(target, animator);
        animator.setFloatValues(new float[]{0.0f, 1.0f});
        if (animationDuration == -1) {
            animationDuration = 250;
        }
        animator.setDuration((long) (animationDuration * DEBUG_ANIMATION_SLOW_DOWN_FACTOR));
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }
}
