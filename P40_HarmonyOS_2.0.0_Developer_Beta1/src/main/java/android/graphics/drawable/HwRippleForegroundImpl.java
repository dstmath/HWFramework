package android.graphics.drawable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.Paint;
import android.graphics.RecordingCanvas;
import android.graphics.Rect;
import android.util.Log;
import android.util.MathUtils;
import android.view.RenderNodeAnimator;
import android.view.animation.AnimationUtils;
import android.view.animation.PathInterpolator;
import huawei.android.provider.HanziToPinyin;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

class HwRippleForegroundImpl extends RippleComponent {
    private static final int ALPHA_ANIMATOR_DURATION = 100;
    private static final float ALPHA_VALUE_MAX = 1.0f;
    private static final float ALPHA_VALUE_MIN = 0.0f;
    private static final PathInterpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    private static final float FLOAT_TO_INT_ROUND_PRECISION = 0.5f;
    private static final int MAX_ALPHA_DURATION = 250;
    private static final int RADIUS_ANIMATOR_DURATION = 100;
    private static final float RADIUS_VALUE_MAX = 1.0f;
    private static final float RIPPLE_START_RADIUS_SCALE = 0.9f;
    private static final String TAG = "HwRippleForegroundImpl";
    private float mAlpha;
    private final AnimatorListenerAdapter mAnimationListener;
    private List<RenderNodeAnimator> mHwAnimators;
    private CanvasProperty<Paint> mHwPaint;
    private CanvasProperty<Float> mHwRadius;
    private CanvasProperty<Float> mHwX;
    private CanvasProperty<Float> mHwY;
    private boolean mIsForceSw;
    private boolean mIsUsingHw;
    private List<RenderNodeAnimator> mPendingHwAnimators;
    private float mRadius;
    private long mStartTime;
    private List<Animator> mSwAnimators;

    HwRippleForegroundImpl(RippleDrawable owner, Rect bounds, boolean isBounded, boolean isForceSoftware, int type) {
        this(owner, bounds, isForceSoftware);
    }

    HwRippleForegroundImpl(RippleDrawable owner, Rect bounds, boolean isForceSoftware) {
        super(owner, bounds);
        this.mAlpha = 0.0f;
        this.mRadius = 0.0f;
        this.mSwAnimators = new CopyOnWriteArrayList();
        this.mPendingHwAnimators = new CopyOnWriteArrayList();
        this.mHwAnimators = new CopyOnWriteArrayList();
        this.mAnimationListener = new AnimatorListenerAdapter() {
            /* class android.graphics.drawable.HwRippleForegroundImpl.AnonymousClass1 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                HwRippleForegroundImpl.this.removeUnusedHwAnimator();
                HwRippleForegroundImpl.this.removeUnusedSwAnimator();
                Log.i(HwRippleForegroundImpl.TAG, "onAnimationEnd " + HwRippleForegroundImpl.this.mPendingHwAnimators.size() + HanziToPinyin.Token.SEPARATOR + HwRippleForegroundImpl.this.mHwAnimators.size() + HanziToPinyin.Token.SEPARATOR + HwRippleForegroundImpl.this.mSwAnimators.size());
                if (HwRippleForegroundImpl.this.mHwAnimators.isEmpty()) {
                    HwRippleForegroundImpl.this.clearHw();
                }
            }
        };
        this.mIsForceSw = isForceSoftware;
    }

    public final void enter() {
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        startSwEnter();
        startHwEnter();
        Log.i(TAG, "HwRippleForegroundImpl enter");
    }

    private void startSwEnter() {
        this.mSwAnimators.forEach($$Lambda$GH0R7RFIaNRo5vIhbBgtisLMbI.INSTANCE);
        this.mSwAnimators.clear();
        ObjectAnimator radiusAnimator = ObjectAnimator.ofFloat(this, "radius", 1.0f).setDuration(100L);
        radiusAnimator.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", 1.0f).setDuration(100L);
        alphaAnimator.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
        this.mSwAnimators.add(radiusAnimator);
        this.mSwAnimators.add(alphaAnimator);
        radiusAnimator.start();
        alphaAnimator.start();
    }

    private void startHwEnter() {
        if (!this.mIsForceSw) {
            this.mHwX = CanvasProperty.createFloat(getHwX());
            this.mHwY = CanvasProperty.createFloat(getHwY());
            this.mHwRadius = CanvasProperty.createFloat(this.mTargetRadius * RIPPLE_START_RADIUS_SCALE);
            this.mHwPaint = CanvasProperty.createPaint(this.mOwner.getRipplePaint());
            RenderNodeAnimator radiusAnimator = new RenderNodeAnimator(this.mHwRadius, this.mTargetRadius).setDuration(100);
            radiusAnimator.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
            RenderNodeAnimator alphaAnimator = new RenderNodeAnimator(this.mHwPaint, 1, (float) this.mOwner.getRipplePaint().getAlpha()).setDuration(100);
            alphaAnimator.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
            alphaAnimator.setStartValue(0.0f);
            this.mPendingHwAnimators.add(radiusAnimator);
            this.mPendingHwAnimators.add(alphaAnimator);
            invalidateSelf();
        }
    }

    private float getHwX() {
        return this.mBounds.exactCenterX();
    }

    private float getHwY() {
        return this.mBounds.exactCenterY();
    }

    public final void exit() {
        startSwExit();
        startHwExit();
        Log.i(TAG, "HwRippleForegroundImpl exit");
    }

    private void startSwExit() {
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", 0.0f).setDuration(100L);
        alphaAnimator.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
        alphaAnimator.addListener(this.mAnimationListener);
        alphaAnimator.setStartDelay(getExitDelay());
        this.mSwAnimators.add(alphaAnimator);
        alphaAnimator.start();
    }

    private void startHwExit() {
        CanvasProperty<Paint> canvasProperty;
        if (!this.mIsForceSw && (canvasProperty = this.mHwPaint) != null) {
            RenderNodeAnimator alphaAnimator = new RenderNodeAnimator(canvasProperty, 1, 0.0f).setDuration(100);
            alphaAnimator.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
            alphaAnimator.setStartValue((float) this.mOwner.getRipplePaint().getAlpha());
            alphaAnimator.setStartDelay(getExitDelay());
            alphaAnimator.addListener(this.mAnimationListener);
            this.mPendingHwAnimators.add(alphaAnimator);
            invalidateSelf();
        }
    }

    private long getExitDelay() {
        long timeSpend = AnimationUtils.currentAnimationTimeMillis() - this.mStartTime;
        if (timeSpend <= 0 || timeSpend >= 250) {
            return 0;
        }
        return 250 - timeSpend;
    }

    public void draw(Canvas canvas, Paint paint) {
        this.mIsUsingHw = !this.mIsForceSw && (canvas instanceof RecordingCanvas) && this.mHwPaint != null;
        removeUnusedSwAnimator();
        if (this.mIsUsingHw) {
            drawHw((RecordingCanvas) canvas);
        } else {
            drawSw(canvas, paint);
        }
    }

    static /* synthetic */ boolean lambda$removeUnusedSwAnimator$0(Animator anim) {
        return !anim.isRunning();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeUnusedSwAnimator() {
        this.mSwAnimators.removeIf($$Lambda$HwRippleForegroundImpl$xU4pDjCMVGojqUOuNIpjOICi3oo.INSTANCE);
    }

    private void drawHw(RecordingCanvas canvas) {
        startHwAnimator(canvas);
        removeUnusedHwAnimator();
        canvas.drawCircle(this.mHwX, this.mHwY, this.mHwRadius, this.mHwPaint);
    }

    private void drawSw(Canvas canvas, Paint paint) {
        if (paint == null || canvas == null) {
            Log.e(TAG, "drawSw input param is null");
            return;
        }
        int paintAlpha = paint.getAlpha();
        int alpha = (int) ((((float) paintAlpha) * this.mAlpha) + 0.5f);
        float radius = getCurrentRadius();
        if (alpha > 0 && radius > 0.0f) {
            paint.setAlpha(alpha);
            canvas.drawCircle(getHwX(), getHwY(), radius, paint);
            paint.setAlpha(paintAlpha);
        }
    }

    private void startHwAnimator(RecordingCanvas recordingCanvas) {
        this.mPendingHwAnimators.forEach(new Consumer(recordingCanvas) {
            /* class android.graphics.drawable.$$Lambda$HwRippleForegroundImpl$om5Q5NdAXLg9BHQ_lW84dm9NM14 */
            private final /* synthetic */ RecordingCanvas f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                HwRippleForegroundImpl.this.lambda$startHwAnimator$1$HwRippleForegroundImpl(this.f$1, (RenderNodeAnimator) obj);
            }
        });
        if (!this.mPendingHwAnimators.isEmpty()) {
            this.mPendingHwAnimators.clear();
        }
    }

    public /* synthetic */ void lambda$startHwAnimator$1$HwRippleForegroundImpl(RecordingCanvas recordingCanvas, RenderNodeAnimator animator) {
        animator.setTarget(recordingCanvas);
        animator.start();
        this.mHwAnimators.add(animator);
    }

    static /* synthetic */ boolean lambda$removeUnusedHwAnimator$2(RenderNodeAnimator anim) {
        return !anim.isRunning();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeUnusedHwAnimator() {
        this.mHwAnimators.removeIf($$Lambda$HwRippleForegroundImpl$5MD2EPR0KHilj9GazbCVyHU22dU.INSTANCE);
    }

    private float getCurrentRadius() {
        return MathUtils.lerp(0.0f, this.mTargetRadius, this.mRadius);
    }

    public void getBounds(Rect bounds) {
        int radius = ((int) this.mTargetRadius) + 1;
        if (bounds != null) {
            bounds.set(-radius, -radius, radius, radius);
        } else {
            Log.e(TAG, "getBounds bounds is null");
        }
    }

    /* access modifiers changed from: protected */
    public void onTargetRadiusChanged(float targetRadius) {
        clearAnimation();
    }

    private void clearAnimation() {
        this.mHwAnimators.forEach($$Lambda$HwRippleForegroundImpl$LQ7txkhVl8dS08DIpXmkIirg62s.INSTANCE);
        this.mHwAnimators.clear();
        clearHw();
        invalidateSelf();
    }

    static /* synthetic */ void lambda$clearAnimation$3(RenderNodeAnimator animator) {
        animator.removeAllListeners();
        animator.end();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearHw() {
        this.mHwPaint = null;
        this.mHwRadius = null;
        this.mHwX = null;
        this.mHwY = null;
        this.mIsUsingHw = false;
    }

    public void end() {
        this.mSwAnimators.forEach($$Lambda$RxCKGW6MkcsUhUvjMPifhn1hQlc.INSTANCE);
        this.mHwAnimators.forEach($$Lambda$yDPXodsSuGnGN7GyWgonmhIHenw.INSTANCE);
        this.mSwAnimators.clear();
        this.mHwAnimators.clear();
    }

    public float getRadius() {
        return this.mRadius;
    }

    public void setRadius(float value) {
        this.mRadius = value;
        onPropValueSet();
    }

    private void onPropValueSet() {
        if (!this.mIsUsingHw) {
            invalidateSelf();
        }
    }

    public float getAlpha() {
        return this.mAlpha;
    }

    public void setAlpha(float value) {
        this.mAlpha = value;
        onPropValueSet();
    }
}
