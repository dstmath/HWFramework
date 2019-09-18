package android.graphics.drawable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.FloatProperty;
import android.util.MathUtils;
import android.view.DisplayListCanvas;
import android.view.RenderNodeAnimator;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import java.util.ArrayList;

class HwRippleForegroundImpl extends RippleComponent {
    private static final TimeInterpolator DECELERATE_INTERPOLATOR = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    private static final TimeInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final FloatProperty<HwRippleForegroundImpl> OPACITY = new FloatProperty<HwRippleForegroundImpl>("opacity") {
        public void setValue(HwRippleForegroundImpl object, float value) {
            float unused = object.mOpacity = value;
            object.onAnimationPropertyChanged();
        }

        public Float get(HwRippleForegroundImpl object) {
            return Float.valueOf(object.mOpacity);
        }
    };
    private static final int OPACITY_ENTER_DURATION = 100;
    private static final int OPACITY_EXIT_DURATION = 100;
    private static final int OPACITY_HOLD_DURATION = 250;
    private static final int RIPPLE_ENTER_DURATION = 100;
    private static final int RIPPLE_ORIGIN_DURATION = 100;
    private static final float RIPPLE_START_RADIUS_SCALE = 0.9f;
    private static final FloatProperty<HwRippleForegroundImpl> TWEEN_ORIGIN = new FloatProperty<HwRippleForegroundImpl>("tweenOrigin") {
        public void setValue(HwRippleForegroundImpl object, float value) {
            float unused = object.mTweenX = value;
            float unused2 = object.mTweenY = value;
            object.onAnimationPropertyChanged();
        }

        public Float get(HwRippleForegroundImpl object) {
            return Float.valueOf(object.mTweenX);
        }
    };
    private static final FloatProperty<HwRippleForegroundImpl> TWEEN_RADIUS = new FloatProperty<HwRippleForegroundImpl>("tweenRadius") {
        public void setValue(HwRippleForegroundImpl object, float value) {
            float unused = object.mTweenRadius = value;
            object.onAnimationPropertyChanged();
        }

        public Float get(HwRippleForegroundImpl object) {
            return Float.valueOf(object.mTweenRadius);
        }
    };
    private final AnimatorListenerAdapter mAnimationListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animator) {
            boolean unused = HwRippleForegroundImpl.this.mHasFinishedExit = true;
            HwRippleForegroundImpl.this.pruneHwFinished();
            HwRippleForegroundImpl.this.pruneSwFinished();
            if (HwRippleForegroundImpl.this.mRunningHwAnimators.isEmpty()) {
                HwRippleForegroundImpl.this.clearHwProps();
            }
        }
    };
    private float mClampedStartingX;
    private float mClampedStartingY;
    private long mEnterStartedAtMillis;
    private final boolean mForceSoftware;
    /* access modifiers changed from: private */
    public boolean mHasFinishedExit;
    /* access modifiers changed from: private */
    public float mOpacity = 0.0f;
    private ArrayList<RenderNodeAnimator> mPendingHwAnimators = new ArrayList<>();
    private CanvasProperty<Paint> mPropPaint;
    private CanvasProperty<Float> mPropRadius;
    private CanvasProperty<Float> mPropX;
    private CanvasProperty<Float> mPropY;
    /* access modifiers changed from: private */
    public ArrayList<RenderNodeAnimator> mRunningHwAnimators = new ArrayList<>();
    private ArrayList<Animator> mRunningSwAnimators = new ArrayList<>();
    private float mStartRadius = 0.0f;
    private float mStartingX;
    private float mStartingY;
    private float mTargetX = 0.0f;
    private float mTargetY = 0.0f;
    /* access modifiers changed from: private */
    public float mTweenRadius = 0.0f;
    /* access modifiers changed from: private */
    public float mTweenX = 0.0f;
    /* access modifiers changed from: private */
    public float mTweenY = 0.0f;
    private boolean mUsingProperties;

    public HwRippleForegroundImpl(RippleDrawable owner, Rect bounds, boolean isBounded, boolean forceSoftware, int type) {
        super(owner, bounds);
        this.mForceSoftware = forceSoftware;
        this.mStartingX = 0.0f;
        this.mStartingY = 0.0f;
        this.mStartRadius = 0.0f;
        clampStartingPosition();
    }

    /* access modifiers changed from: protected */
    public void onTargetRadiusChanged(float targetRadius) {
        clampStartingPosition();
        switchToUiThreadAnimation();
    }

    private void drawSoftware(Canvas c, Paint p) {
        int origAlpha = p.getAlpha();
        int alpha = (int) ((((float) origAlpha) * this.mOpacity) + 0.5f);
        float radius = getCurrentRadius();
        if (alpha > 0 && radius > 0.0f) {
            float x = getCurrentX();
            float y = getCurrentY();
            p.setAlpha(alpha);
            c.drawCircle(x, y, radius, p);
            p.setAlpha(origAlpha);
        }
    }

    private void startPending(DisplayListCanvas c) {
        if (!this.mPendingHwAnimators.isEmpty()) {
            for (int i = 0; i < this.mPendingHwAnimators.size(); i++) {
                RenderNodeAnimator animator = this.mPendingHwAnimators.get(i);
                animator.setTarget(c);
                animator.start();
                this.mRunningHwAnimators.add(animator);
            }
            this.mPendingHwAnimators.clear();
        }
    }

    /* access modifiers changed from: private */
    public void pruneHwFinished() {
        if (!this.mRunningHwAnimators.isEmpty()) {
            for (int i = this.mRunningHwAnimators.size() - 1; i >= 0; i--) {
                if (!this.mRunningHwAnimators.get(i).isRunning()) {
                    this.mRunningHwAnimators.remove(i);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void pruneSwFinished() {
        if (!this.mRunningSwAnimators.isEmpty()) {
            for (int i = this.mRunningSwAnimators.size() - 1; i >= 0; i--) {
                if (!this.mRunningSwAnimators.get(i).isRunning()) {
                    this.mRunningSwAnimators.remove(i);
                }
            }
        }
    }

    private void drawHardware(DisplayListCanvas c, Paint p) {
        startPending(c);
        pruneHwFinished();
        if (this.mPropPaint != null) {
            this.mUsingProperties = true;
            c.drawCircle(this.mPropX, this.mPropY, this.mPropRadius, this.mPropPaint);
            return;
        }
        this.mUsingProperties = false;
        drawSoftware(c, p);
    }

    public void getBounds(Rect bounds) {
        int outerX = (int) this.mTargetX;
        int outerY = (int) this.mTargetY;
        int r = ((int) this.mTargetRadius) + 1;
        bounds.set(outerX - r, outerY - r, outerX + r, outerY + r);
    }

    public void move(float x, float y) {
        this.mStartingX = x;
        this.mStartingY = y;
        clampStartingPosition();
    }

    public boolean hasFinishedExit() {
        return this.mHasFinishedExit;
    }

    private long computeFadeOutDelay() {
        long timeSinceEnter = AnimationUtils.currentAnimationTimeMillis() - this.mEnterStartedAtMillis;
        if (timeSinceEnter <= 0 || timeSinceEnter >= 250) {
            return 0;
        }
        return 250 - timeSinceEnter;
    }

    private void startSoftwareEnter() {
        for (int i = 0; i < this.mRunningSwAnimators.size(); i++) {
            this.mRunningSwAnimators.get(i).cancel();
        }
        this.mRunningSwAnimators.clear();
        ObjectAnimator tweenRadius = ObjectAnimator.ofFloat(this, TWEEN_RADIUS, new float[]{1.0f});
        tweenRadius.setDuration(100);
        tweenRadius.setInterpolator(DECELERATE_INTERPOLATOR);
        tweenRadius.start();
        this.mRunningSwAnimators.add(tweenRadius);
        ObjectAnimator tweenOrigin = ObjectAnimator.ofFloat(this, TWEEN_ORIGIN, new float[]{1.0f});
        tweenOrigin.setDuration(100);
        tweenOrigin.setInterpolator(DECELERATE_INTERPOLATOR);
        tweenOrigin.start();
        this.mRunningSwAnimators.add(tweenOrigin);
        ObjectAnimator opacity = ObjectAnimator.ofFloat(this, OPACITY, new float[]{1.0f});
        opacity.setDuration(100);
        opacity.setInterpolator(DECELERATE_INTERPOLATOR);
        opacity.start();
        this.mRunningSwAnimators.add(opacity);
    }

    private void startSoftwareExit() {
        ObjectAnimator opacity = ObjectAnimator.ofFloat(this, OPACITY, new float[]{0.0f});
        opacity.setDuration(100);
        opacity.setInterpolator(DECELERATE_INTERPOLATOR);
        opacity.addListener(this.mAnimationListener);
        opacity.setStartDelay(computeFadeOutDelay());
        opacity.start();
        this.mRunningSwAnimators.add(opacity);
    }

    private void startHardwareEnter() {
        if (!this.mForceSoftware) {
            this.mPropX = CanvasProperty.createFloat(getCurrentX());
            this.mPropY = CanvasProperty.createFloat(getCurrentY());
            this.mPropRadius = CanvasProperty.createFloat(this.mTargetRadius * RIPPLE_START_RADIUS_SCALE);
            Paint paint = this.mOwner.getRipplePaint();
            this.mPropPaint = CanvasProperty.createPaint(paint);
            RenderNodeAnimator radius = new RenderNodeAnimator(this.mPropRadius, this.mTargetRadius);
            radius.setDuration(100);
            radius.setInterpolator(DECELERATE_INTERPOLATOR);
            this.mPendingHwAnimators.add(radius);
            RenderNodeAnimator opacity = new RenderNodeAnimator(this.mPropPaint, 1, (float) paint.getAlpha());
            opacity.setDuration(100);
            opacity.setInterpolator(DECELERATE_INTERPOLATOR);
            opacity.setStartValue(0.0f);
            this.mPendingHwAnimators.add(opacity);
            invalidateSelf();
        }
    }

    private void startHardwareExit() {
        if (!this.mForceSoftware && this.mPropPaint != null) {
            RenderNodeAnimator opacity = new RenderNodeAnimator(this.mPropPaint, 1, 0.0f);
            opacity.setDuration(100);
            opacity.setInterpolator(DECELERATE_INTERPOLATOR);
            opacity.addListener(this.mAnimationListener);
            opacity.setStartDelay(computeFadeOutDelay());
            opacity.setStartValue((float) this.mOwner.getRipplePaint().getAlpha());
            this.mPendingHwAnimators.add(opacity);
            invalidateSelf();
        }
    }

    public final void enter() {
        this.mEnterStartedAtMillis = AnimationUtils.currentAnimationTimeMillis();
        startSoftwareEnter();
        startHardwareEnter();
    }

    public final void exit() {
        startSoftwareExit();
        startHardwareExit();
    }

    private float getCurrentX() {
        return this.mBounds.exactCenterX();
    }

    private float getCurrentY() {
        return this.mBounds.exactCenterY();
    }

    private float getCurrentRadius() {
        return MathUtils.lerp(this.mStartRadius, this.mTargetRadius, this.mTweenRadius);
    }

    public void draw(Canvas c, Paint p) {
        boolean hasDisplayListCanvas = !this.mForceSoftware && (c instanceof DisplayListCanvas);
        pruneSwFinished();
        if (hasDisplayListCanvas) {
            drawHardware((DisplayListCanvas) c, p);
        } else {
            drawSoftware(c, p);
        }
    }

    private void clampStartingPosition() {
        float cX = this.mBounds.exactCenterX();
        float cY = this.mBounds.exactCenterY();
        float dX = this.mStartingX - cX;
        float dY = this.mStartingY - cY;
        float r = this.mTargetRadius - this.mStartRadius;
        if ((dX * dX) + (dY * dY) > r * r) {
            double angle = Math.atan2((double) dY, (double) dX);
            this.mClampedStartingX = ((float) (Math.cos(angle) * ((double) r))) + cX;
            this.mClampedStartingY = ((float) (Math.sin(angle) * ((double) r))) + cY;
            return;
        }
        this.mClampedStartingX = this.mStartingX;
        this.mClampedStartingY = this.mStartingY;
    }

    public void end() {
        for (int i = 0; i < this.mRunningSwAnimators.size(); i++) {
            this.mRunningSwAnimators.get(i).end();
        }
        this.mRunningSwAnimators.clear();
        for (int i2 = 0; i2 < this.mRunningHwAnimators.size(); i2++) {
            this.mRunningHwAnimators.get(i2).end();
        }
        this.mRunningHwAnimators.clear();
    }

    /* access modifiers changed from: private */
    public void onAnimationPropertyChanged() {
        if (!this.mUsingProperties) {
            invalidateSelf();
        }
    }

    /* access modifiers changed from: private */
    public void clearHwProps() {
        this.mPropPaint = null;
        this.mPropRadius = null;
        this.mPropX = null;
        this.mPropY = null;
        this.mUsingProperties = false;
    }

    private void switchToUiThreadAnimation() {
        for (int i = 0; i < this.mRunningHwAnimators.size(); i++) {
            Animator animator = this.mRunningHwAnimators.get(i);
            animator.removeListener(this.mAnimationListener);
            animator.end();
        }
        this.mRunningHwAnimators.clear();
        clearHwProps();
        invalidateSelf();
    }
}
