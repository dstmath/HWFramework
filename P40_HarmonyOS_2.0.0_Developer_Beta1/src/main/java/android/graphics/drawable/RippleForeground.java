package android.graphics.drawable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.Paint;
import android.graphics.RecordingCanvas;
import android.graphics.Rect;
import android.util.FloatProperty;
import android.util.MathUtils;
import android.view.RenderNodeAnimator;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import java.util.ArrayList;

/* access modifiers changed from: package-private */
public class RippleForeground extends RippleComponent {
    private static final TimeInterpolator DECELERATE_INTERPOLATOR = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    private static final TimeInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final FloatProperty<RippleForeground> OPACITY = new FloatProperty<RippleForeground>("opacity") {
        /* class android.graphics.drawable.RippleForeground.AnonymousClass4 */

        public void setValue(RippleForeground object, float value) {
            object.mOpacity = value;
            object.onAnimationPropertyChanged();
        }

        public Float get(RippleForeground object) {
            return Float.valueOf(object.mOpacity);
        }
    };
    private static final int OPACITY_ENTER_DURATION = 75;
    private static final int OPACITY_EXIT_DURATION = 150;
    private static final int OPACITY_HOLD_DURATION = 225;
    private static final int RIPPLE_ENTER_DURATION = 225;
    private static final int RIPPLE_ORIGIN_DURATION = 225;
    private static final FloatProperty<RippleForeground> TWEEN_ORIGIN = new FloatProperty<RippleForeground>("tweenOrigin") {
        /* class android.graphics.drawable.RippleForeground.AnonymousClass3 */

        public void setValue(RippleForeground object, float value) {
            object.mTweenX = value;
            object.mTweenY = value;
            object.onAnimationPropertyChanged();
        }

        public Float get(RippleForeground object) {
            return Float.valueOf(object.mTweenX);
        }
    };
    private static final FloatProperty<RippleForeground> TWEEN_RADIUS = new FloatProperty<RippleForeground>("tweenRadius") {
        /* class android.graphics.drawable.RippleForeground.AnonymousClass2 */

        public void setValue(RippleForeground object, float value) {
            object.mTweenRadius = value;
            object.onAnimationPropertyChanged();
        }

        public Float get(RippleForeground object) {
            return Float.valueOf(object.mTweenRadius);
        }
    };
    private final AnimatorListenerAdapter mAnimationListener = new AnimatorListenerAdapter() {
        /* class android.graphics.drawable.RippleForeground.AnonymousClass1 */

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            RippleForeground.this.mHasFinishedExit = true;
            RippleForeground.this.pruneHwFinished();
            RippleForeground.this.pruneSwFinished();
            if (RippleForeground.this.mRunningHwAnimators.isEmpty()) {
                RippleForeground.this.clearHwProps();
            }
        }
    };
    private float mClampedStartingX;
    private float mClampedStartingY;
    private long mEnterStartedAtMillis;
    private final boolean mForceSoftware;
    private boolean mHasFinishedExit;
    private float mOpacity = 0.0f;
    private ArrayList<RenderNodeAnimator> mPendingHwAnimators = new ArrayList<>();
    private CanvasProperty<Paint> mPropPaint;
    private CanvasProperty<Float> mPropRadius;
    private CanvasProperty<Float> mPropX;
    private CanvasProperty<Float> mPropY;
    private ArrayList<RenderNodeAnimator> mRunningHwAnimators = new ArrayList<>();
    private ArrayList<Animator> mRunningSwAnimators = new ArrayList<>();
    private float mStartRadius = 0.0f;
    private float mStartingX;
    private float mStartingY;
    private float mTargetX = 0.0f;
    private float mTargetY = 0.0f;
    private float mTweenRadius = 0.0f;
    private float mTweenX = 0.0f;
    private float mTweenY = 0.0f;
    private boolean mUsingProperties;

    public RippleForeground(RippleDrawable owner, Rect bounds, float startingX, float startingY, boolean forceSoftware) {
        super(owner, bounds);
        this.mForceSoftware = forceSoftware;
        this.mStartingX = startingX;
        this.mStartingY = startingY;
        this.mStartRadius = ((float) Math.max(bounds.width(), bounds.height())) * 0.3f;
        clampStartingPosition();
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.RippleComponent
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

    private void startPending(RecordingCanvas c) {
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
    /* access modifiers changed from: public */
    private void pruneHwFinished() {
        if (!this.mRunningHwAnimators.isEmpty()) {
            for (int i = this.mRunningHwAnimators.size() - 1; i >= 0; i--) {
                if (!this.mRunningHwAnimators.get(i).isRunning()) {
                    this.mRunningHwAnimators.remove(i);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pruneSwFinished() {
        if (!this.mRunningSwAnimators.isEmpty()) {
            for (int i = this.mRunningSwAnimators.size() - 1; i >= 0; i--) {
                if (!this.mRunningSwAnimators.get(i).isRunning()) {
                    this.mRunningSwAnimators.remove(i);
                }
            }
        }
    }

    private void drawHardware(RecordingCanvas c, Paint p) {
        startPending(c);
        pruneHwFinished();
        CanvasProperty<Paint> canvasProperty = this.mPropPaint;
        if (canvasProperty != null) {
            this.mUsingProperties = true;
            c.drawCircle(this.mPropX, this.mPropY, this.mPropRadius, canvasProperty);
            return;
        }
        this.mUsingProperties = false;
        drawSoftware(c, p);
    }

    @Override // android.graphics.drawable.RippleComponent
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
        if (timeSinceEnter <= 0 || timeSinceEnter >= 225) {
            return 0;
        }
        return 225 - timeSinceEnter;
    }

    private void startSoftwareEnter() {
        for (int i = 0; i < this.mRunningSwAnimators.size(); i++) {
            this.mRunningSwAnimators.get(i).cancel();
        }
        this.mRunningSwAnimators.clear();
        ObjectAnimator tweenRadius = ObjectAnimator.ofFloat(this, TWEEN_RADIUS, 1.0f);
        tweenRadius.setDuration(225L);
        tweenRadius.setInterpolator(DECELERATE_INTERPOLATOR);
        tweenRadius.start();
        this.mRunningSwAnimators.add(tweenRadius);
        ObjectAnimator tweenOrigin = ObjectAnimator.ofFloat(this, TWEEN_ORIGIN, 1.0f);
        tweenOrigin.setDuration(225L);
        tweenOrigin.setInterpolator(DECELERATE_INTERPOLATOR);
        tweenOrigin.start();
        this.mRunningSwAnimators.add(tweenOrigin);
        ObjectAnimator opacity = ObjectAnimator.ofFloat(this, OPACITY, 1.0f);
        opacity.setDuration(75L);
        opacity.setInterpolator(LINEAR_INTERPOLATOR);
        opacity.start();
        this.mRunningSwAnimators.add(opacity);
    }

    private void startSoftwareExit() {
        ObjectAnimator opacity = ObjectAnimator.ofFloat(this, OPACITY, 0.0f);
        opacity.setDuration(150L);
        opacity.setInterpolator(LINEAR_INTERPOLATOR);
        opacity.addListener(this.mAnimationListener);
        opacity.setStartDelay(computeFadeOutDelay());
        opacity.start();
        this.mRunningSwAnimators.add(opacity);
    }

    private void startHardwareEnter() {
        if (!this.mForceSoftware) {
            this.mPropX = CanvasProperty.createFloat(getCurrentX());
            this.mPropY = CanvasProperty.createFloat(getCurrentY());
            this.mPropRadius = CanvasProperty.createFloat(getCurrentRadius());
            Paint paint = this.mOwner.getRipplePaint();
            this.mPropPaint = CanvasProperty.createPaint(paint);
            RenderNodeAnimator radius = new RenderNodeAnimator(this.mPropRadius, this.mTargetRadius);
            radius.setDuration(225L);
            radius.setInterpolator(DECELERATE_INTERPOLATOR);
            this.mPendingHwAnimators.add(radius);
            RenderNodeAnimator x = new RenderNodeAnimator(this.mPropX, this.mTargetX);
            x.setDuration(225L);
            x.setInterpolator(DECELERATE_INTERPOLATOR);
            this.mPendingHwAnimators.add(x);
            RenderNodeAnimator y = new RenderNodeAnimator(this.mPropY, this.mTargetY);
            y.setDuration(225L);
            y.setInterpolator(DECELERATE_INTERPOLATOR);
            this.mPendingHwAnimators.add(y);
            RenderNodeAnimator opacity = new RenderNodeAnimator(this.mPropPaint, 1, (float) paint.getAlpha());
            opacity.setDuration(75L);
            opacity.setInterpolator(LINEAR_INTERPOLATOR);
            opacity.setStartValue(0.0f);
            this.mPendingHwAnimators.add(opacity);
            invalidateSelf();
        }
    }

    private void startHardwareExit() {
        CanvasProperty<Paint> canvasProperty;
        if (!this.mForceSoftware && (canvasProperty = this.mPropPaint) != null) {
            RenderNodeAnimator opacity = new RenderNodeAnimator(canvasProperty, 1, 0.0f);
            opacity.setDuration(150L);
            opacity.setInterpolator(LINEAR_INTERPOLATOR);
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
        return MathUtils.lerp(this.mClampedStartingX - this.mBounds.exactCenterX(), this.mTargetX, this.mTweenX);
    }

    private float getCurrentY() {
        return MathUtils.lerp(this.mClampedStartingY - this.mBounds.exactCenterY(), this.mTargetY, this.mTweenY);
    }

    private float getCurrentRadius() {
        return MathUtils.lerp(this.mStartRadius, this.mTargetRadius, this.mTweenRadius);
    }

    public void draw(Canvas c, Paint p) {
        boolean hasDisplayListCanvas = !this.mForceSoftware && (c instanceof RecordingCanvas);
        pruneSwFinished();
        if (hasDisplayListCanvas) {
            drawHardware((RecordingCanvas) c, p);
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
    /* access modifiers changed from: public */
    private void onAnimationPropertyChanged() {
        if (!this.mUsingProperties) {
            invalidateSelf();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearHwProps() {
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
