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
import android.util.Log;
import android.util.MathUtils;
import android.view.RenderNodeAnimator;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import java.util.ArrayList;

/* access modifiers changed from: package-private */
public class HwRippleForegroundImpl extends RippleComponent {
    private static final TimeInterpolator DECELERATE_INTERPOLATOR = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    private static final TimeInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final FloatProperty<HwRippleForegroundImpl> OPACITY = new FloatProperty<HwRippleForegroundImpl>("opacity") {
        /* class android.graphics.drawable.HwRippleForegroundImpl.AnonymousClass3 */

        public void setValue(HwRippleForegroundImpl object, float value) {
            object.mOpacity = value;
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
    private static final String TAG = "HwRippleForegroundImpl";
    private static final FloatProperty<HwRippleForegroundImpl> TWEEN_ORIGIN = new FloatProperty<HwRippleForegroundImpl>("tweenOrigin") {
        /* class android.graphics.drawable.HwRippleForegroundImpl.AnonymousClass2 */

        public void setValue(HwRippleForegroundImpl object, float value) {
            object.mTweenX = value;
            object.mTweenY = value;
            object.onAnimationPropertyChanged();
        }

        public Float get(HwRippleForegroundImpl object) {
            return Float.valueOf(object.mTweenX);
        }
    };
    private static final FloatProperty<HwRippleForegroundImpl> TWEEN_RADIUS = new FloatProperty<HwRippleForegroundImpl>("tweenRadius") {
        /* class android.graphics.drawable.HwRippleForegroundImpl.AnonymousClass1 */

        public void setValue(HwRippleForegroundImpl object, float value) {
            object.mTweenRadius = value;
            object.onAnimationPropertyChanged();
        }

        public Float get(HwRippleForegroundImpl object) {
            return Float.valueOf(object.mTweenRadius);
        }
    };
    private final AnimatorListenerAdapter mAnimationListener = new AnimatorListenerAdapter() {
        /* class android.graphics.drawable.HwRippleForegroundImpl.AnonymousClass4 */

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            HwRippleForegroundImpl.this.mIsHasFinishedExit = true;
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
    private final boolean mIsForceSoftware;
    private boolean mIsHasFinishedExit;
    private boolean mIsUsingProperties;
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

    HwRippleForegroundImpl(RippleDrawable owner, Rect bounds, boolean isBounded, boolean isForceSoftware, int type) {
        super(owner, bounds);
        this.mIsForceSoftware = isForceSoftware;
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

    private void drawSoftware(Canvas canvas, Paint paint) {
        int origAlpha = paint.getAlpha();
        int alpha = (int) ((((float) origAlpha) * this.mOpacity) + 0.5f);
        float radius = getCurrentRadius();
        if (alpha > 0 && radius > 0.0f) {
            float currentX = getCurrentX();
            float currentY = getCurrentY();
            paint.setAlpha(alpha);
            canvas.drawCircle(currentX, currentY, radius, paint);
            paint.setAlpha(origAlpha);
        }
    }

    private void startPending(RecordingCanvas recordingCanvas) {
        if (!this.mPendingHwAnimators.isEmpty()) {
            for (int i = 0; i < this.mPendingHwAnimators.size(); i++) {
                RenderNodeAnimator animator = this.mPendingHwAnimators.get(i);
                animator.setTarget(recordingCanvas);
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

    private void drawHardware(RecordingCanvas canvas, Paint paint) {
        startPending(canvas);
        pruneHwFinished();
        CanvasProperty<Paint> canvasProperty = this.mPropPaint;
        if (canvasProperty != null) {
            this.mIsUsingProperties = true;
            canvas.drawCircle(this.mPropX, this.mPropY, this.mPropRadius, canvasProperty);
            return;
        }
        this.mIsUsingProperties = false;
        drawSoftware(canvas, paint);
    }

    public void getBounds(Rect bounds) {
        int outerX = (int) this.mTargetX;
        int outerY = (int) this.mTargetY;
        int radius = ((int) this.mTargetRadius) + 1;
        if (bounds != null) {
            bounds.set(outerX - radius, outerY - radius, outerX + radius, outerY + radius);
        } else {
            Log.e(TAG, "get bounds parameter is null");
        }
    }

    public void move(float x, float y) {
        this.mStartingX = x;
        this.mStartingY = y;
        clampStartingPosition();
    }

    public boolean hasFinishedExit() {
        return this.mIsHasFinishedExit;
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
        ObjectAnimator tweenRadius = ObjectAnimator.ofFloat(this, TWEEN_RADIUS, 1.0f);
        tweenRadius.setDuration(100L);
        tweenRadius.setInterpolator(DECELERATE_INTERPOLATOR);
        tweenRadius.start();
        this.mRunningSwAnimators.add(tweenRadius);
        ObjectAnimator tweenOrigin = ObjectAnimator.ofFloat(this, TWEEN_ORIGIN, 1.0f);
        tweenOrigin.setDuration(100L);
        tweenOrigin.setInterpolator(DECELERATE_INTERPOLATOR);
        tweenOrigin.start();
        this.mRunningSwAnimators.add(tweenOrigin);
        ObjectAnimator opacity = ObjectAnimator.ofFloat(this, OPACITY, 1.0f);
        opacity.setDuration(100L);
        opacity.setInterpolator(DECELERATE_INTERPOLATOR);
        opacity.start();
        this.mRunningSwAnimators.add(opacity);
    }

    private void startSoftwareExit() {
        ObjectAnimator opacity = ObjectAnimator.ofFloat(this, OPACITY, 0.0f);
        opacity.setDuration(100L);
        opacity.setInterpolator(DECELERATE_INTERPOLATOR);
        opacity.addListener(this.mAnimationListener);
        opacity.setStartDelay(computeFadeOutDelay());
        opacity.start();
        this.mRunningSwAnimators.add(opacity);
    }

    private void startHardwareEnter() {
        if (!this.mIsForceSoftware) {
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
        CanvasProperty<Paint> canvasProperty;
        if (!this.mIsForceSoftware && (canvasProperty = this.mPropPaint) != null) {
            RenderNodeAnimator opacity = new RenderNodeAnimator(canvasProperty, 1, 0.0f);
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

    public void draw(Canvas canvas, Paint paint) {
        boolean isHasDisplayListCanvas = !this.mIsForceSoftware && (canvas instanceof RecordingCanvas);
        pruneSwFinished();
        if (isHasDisplayListCanvas) {
            drawHardware((RecordingCanvas) canvas, paint);
        } else {
            drawSoftware(canvas, paint);
        }
    }

    private void clampStartingPosition() {
        float centerX = this.mBounds.exactCenterX();
        float centerY = this.mBounds.exactCenterY();
        float dX = this.mStartingX - centerX;
        float dY = this.mStartingY - centerY;
        float radius = this.mTargetRadius - this.mStartRadius;
        if ((dX * dX) + (dY * dY) > radius * radius) {
            double angle = Math.atan2((double) dY, (double) dX);
            this.mClampedStartingX = ((float) (Math.cos(angle) * ((double) radius))) + centerX;
            this.mClampedStartingY = ((float) (Math.sin(angle) * ((double) radius))) + centerY;
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
        if (!this.mIsUsingProperties) {
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
        this.mIsUsingProperties = false;
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
