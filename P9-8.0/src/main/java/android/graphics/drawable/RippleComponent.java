package android.graphics.drawable;

import android.animation.Animator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.camera2.params.TonemapCurve;
import android.view.DisplayListCanvas;
import android.view.RenderNodeAnimator;
import java.util.ArrayList;

abstract class RippleComponent {
    protected final Rect mBounds;
    protected float mDensityScale;
    private final boolean mForceSoftware;
    private RenderNodeAnimatorSet mHardwareAnimator;
    private boolean mHasDisplayListCanvas;
    protected boolean mHasMaxRadius;
    private boolean mHasPendingHardwareAnimator;
    private final RippleDrawable mOwner;
    protected Animator mSoftwareAnimator;
    protected float mTargetRadius;

    public static class RenderNodeAnimatorSet {
        private final ArrayList<RenderNodeAnimator> mAnimators = new ArrayList();

        public void add(RenderNodeAnimator anim) {
            this.mAnimators.add(anim);
        }

        public void clear() {
            this.mAnimators.clear();
        }

        public void start(DisplayListCanvas target) {
            if (target == null) {
                throw new IllegalArgumentException("Hardware canvas must be non-null");
            }
            ArrayList<RenderNodeAnimator> animators = this.mAnimators;
            int N = animators.size();
            for (int i = 0; i < N; i++) {
                RenderNodeAnimator anim = (RenderNodeAnimator) animators.get(i);
                anim.setTarget(target);
                anim.start();
            }
        }

        public void cancel() {
            ArrayList<RenderNodeAnimator> animators = this.mAnimators;
            int N = animators.size();
            for (int i = 0; i < N; i++) {
                ((RenderNodeAnimator) animators.get(i)).cancel();
            }
        }

        public void end() {
            ArrayList<RenderNodeAnimator> animators = this.mAnimators;
            int N = animators.size();
            for (int i = 0; i < N; i++) {
                ((RenderNodeAnimator) animators.get(i)).end();
            }
        }

        public boolean isRunning() {
            ArrayList<RenderNodeAnimator> animators = this.mAnimators;
            int N = animators.size();
            for (int i = 0; i < N; i++) {
                if (((RenderNodeAnimator) animators.get(i)).isRunning()) {
                    return true;
                }
            }
            return false;
        }
    }

    protected abstract RenderNodeAnimatorSet createHardwareExit(Paint paint);

    protected abstract Animator createSoftwareEnter(boolean z);

    protected abstract Animator createSoftwareExit();

    protected abstract boolean drawHardware(DisplayListCanvas displayListCanvas);

    protected abstract boolean drawSoftware(Canvas canvas, Paint paint);

    protected abstract void jumpValuesToExit();

    public RippleComponent(RippleDrawable owner, Rect bounds, boolean forceSoftware) {
        this.mOwner = owner;
        this.mBounds = bounds;
        this.mForceSoftware = forceSoftware;
    }

    public void onBoundsChange() {
        if (!this.mHasMaxRadius) {
            this.mTargetRadius = getTargetRadius(this.mBounds);
            onTargetRadiusChanged(this.mTargetRadius);
        }
    }

    public final void setup(float maxRadius, int densityDpi) {
        if (maxRadius >= TonemapCurve.LEVEL_BLACK) {
            this.mHasMaxRadius = true;
            this.mTargetRadius = maxRadius;
        } else {
            this.mTargetRadius = getTargetRadius(this.mBounds);
        }
        this.mDensityScale = ((float) densityDpi) * 0.00625f;
        onTargetRadiusChanged(this.mTargetRadius);
    }

    private static float getTargetRadius(Rect bounds) {
        float halfWidth = ((float) bounds.width()) / 2.0f;
        float halfHeight = ((float) bounds.height()) / 2.0f;
        return (float) Math.sqrt((double) ((halfWidth * halfWidth) + (halfHeight * halfHeight)));
    }

    public final void enter(boolean fast) {
        cancel();
        this.mSoftwareAnimator = createSoftwareEnter(fast);
        if (this.mSoftwareAnimator != null) {
            this.mSoftwareAnimator.start();
        }
    }

    public void exit() {
        cancel();
        if (this.mHasDisplayListCanvas) {
            this.mHasPendingHardwareAnimator = true;
            invalidateSelf();
            return;
        }
        this.mSoftwareAnimator = createSoftwareExit();
        this.mSoftwareAnimator.start();
    }

    public void cancel() {
        cancelSoftwareAnimations();
        endHardwareAnimations();
    }

    public void end() {
        endSoftwareAnimations();
        endHardwareAnimations();
    }

    public boolean draw(Canvas c, Paint p) {
        boolean hasDisplayListCanvas;
        if (this.mForceSoftware || !c.isHardwareAccelerated()) {
            hasDisplayListCanvas = false;
        } else {
            hasDisplayListCanvas = c instanceof DisplayListCanvas;
        }
        if (this.mHasDisplayListCanvas != hasDisplayListCanvas) {
            this.mHasDisplayListCanvas = hasDisplayListCanvas;
            if (!hasDisplayListCanvas) {
                endHardwareAnimations();
            }
        }
        if (hasDisplayListCanvas) {
            DisplayListCanvas hw = (DisplayListCanvas) c;
            startPendingAnimation(hw, p);
            if (this.mHardwareAnimator != null) {
                return drawHardware(hw);
            }
        }
        return drawSoftware(c, p);
    }

    public void getBounds(Rect bounds) {
        int r = (int) Math.ceil((double) this.mTargetRadius);
        bounds.set(-r, -r, r, r);
    }

    private void startPendingAnimation(DisplayListCanvas hw, Paint p) {
        if (this.mHasPendingHardwareAnimator) {
            this.mHasPendingHardwareAnimator = false;
            this.mHardwareAnimator = createHardwareExit(new Paint(p));
            this.mHardwareAnimator.start(hw);
            jumpValuesToExit();
        }
    }

    private void cancelSoftwareAnimations() {
        if (this.mSoftwareAnimator != null) {
            this.mSoftwareAnimator.cancel();
            this.mSoftwareAnimator = null;
        }
    }

    private void endSoftwareAnimations() {
        if (this.mSoftwareAnimator != null) {
            this.mSoftwareAnimator.end();
            this.mSoftwareAnimator = null;
        }
    }

    private void endHardwareAnimations() {
        if (this.mHardwareAnimator != null) {
            this.mHardwareAnimator.end();
            this.mHardwareAnimator = null;
        }
        if (this.mHasPendingHardwareAnimator) {
            this.mHasPendingHardwareAnimator = false;
            jumpValuesToExit();
        }
    }

    protected final void invalidateSelf() {
        this.mOwner.invalidateSelf(false);
    }

    protected final boolean isHardwareAnimating() {
        if (this.mHardwareAnimator == null || !this.mHardwareAnimator.isRunning()) {
            return this.mHasPendingHardwareAnimator;
        }
        return true;
    }

    protected final void onHotspotBoundsChanged() {
        if (!this.mHasMaxRadius) {
            float halfWidth = ((float) this.mBounds.width()) / 2.0f;
            float halfHeight = ((float) this.mBounds.height()) / 2.0f;
            onTargetRadiusChanged((float) Math.sqrt((double) ((halfWidth * halfWidth) + (halfHeight * halfHeight))));
        }
    }

    protected void onTargetRadiusChanged(float targetRadius) {
    }
}
