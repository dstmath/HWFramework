package android.graphics.drawable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.RippleComponent.RenderNodeAnimatorSet;
import android.util.FloatProperty;
import android.view.DisplayListCanvas;
import android.view.RenderNodeAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.PathInterpolator;

class HwRippleForegroundImpl extends RippleComponent {
    public static final int DURATION_ENTER = 100;
    public static final int DURATION_EXIT = 200;
    private static final TimeInterpolator INTERPOLATOR_ENTER = new PathInterpolator(0.3f, 0.15f, 0.1f, 0.85f);
    private static final TimeInterpolator INTERPOLATOR_EXIT = new AccelerateDecelerateInterpolator();
    public static final float LINEAR_FROM = 0.8f;
    private static final FloatProperty<HwRippleForegroundImpl> OPACITY = new FloatProperty<HwRippleForegroundImpl>("opacity") {
        public void setValue(HwRippleForegroundImpl object, float value) {
            object.mOpacity = value;
            object.invalidateSelf();
        }

        public Float get(HwRippleForegroundImpl object) {
            return Float.valueOf(object.mOpacity);
        }
    };
    public static final int RADIUS_DEF = 111;
    public static final float RAIDUS_FROM = 0.57f;
    private static final FloatProperty<HwRippleForegroundImpl> TWEEN_RADIUS = new FloatProperty<HwRippleForegroundImpl>("tweenRadius") {
        public void setValue(HwRippleForegroundImpl object, float value) {
            object.mTweenRadius = value;
            object.invalidateSelf();
        }

        public Float get(HwRippleForegroundImpl object) {
            return Float.valueOf(object.mTweenRadius);
        }
    };
    private float mOpacity = 1.0f;
    private CanvasProperty<Float> mPropB;
    private CanvasProperty<Float> mPropL;
    private CanvasProperty<Paint> mPropPaint;
    private CanvasProperty<Float> mPropR;
    private CanvasProperty<Float> mPropRadius;
    private CanvasProperty<Float> mPropRx;
    private CanvasProperty<Float> mPropRy;
    private CanvasProperty<Float> mPropT;
    private CanvasProperty<Float> mPropX;
    private CanvasProperty<Float> mPropY;
    private float mTweenRadius = 0.0f;
    private int mType;

    public HwRippleForegroundImpl(RippleDrawable owner, Rect bounds, boolean isBounded, boolean forceSoftware, int type) {
        super(owner, bounds, forceSoftware);
        this.mType = type;
    }

    protected void onTargetRadiusChanged(float targetRadius) {
        if (!this.mHasMaxRadius) {
            this.mHasMaxRadius = true;
            this.mTargetRadius = 111.0f;
        }
    }

    protected void jumpValuesToExit() {
        this.mTweenRadius = 1.0f;
        this.mOpacity = 0.0f;
    }

    public void exit() {
        if (this.mSoftwareAnimator == null || !this.mSoftwareAnimator.isRunning()) {
            super.exit();
        } else {
            this.mSoftwareAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    HwRippleForegroundImpl.this.mSoftwareAnimator.removeAllListeners();
                    super.exit();
                }
            });
        }
    }

    protected Animator createSoftwareEnter(boolean fast) {
        ObjectAnimator opacity = ObjectAnimator.ofFloat(this, OPACITY, new float[]{1.0f, 1.0f});
        opacity.setDuration(100);
        opacity.setInterpolator(INTERPOLATOR_ENTER);
        AnimatorSet set = new AnimatorSet();
        Builder builder = set.play(opacity);
        switch (this.mType) {
            case 0:
                createSoftwareEnterLinear(builder);
                break;
            default:
                createSoftwareEnterRadial(builder);
                break;
        }
        return set;
    }

    private void createSoftwareEnterLinear(Builder builder) {
        ObjectAnimator tweenRadius = ObjectAnimator.ofFloat(this, TWEEN_RADIUS, new float[]{0.8f, 1.0f});
        tweenRadius.setDuration(100);
        tweenRadius.setInterpolator(INTERPOLATOR_ENTER);
        builder.with(tweenRadius);
    }

    private void createSoftwareEnterRadial(Builder builder) {
        ObjectAnimator tweenRadius = ObjectAnimator.ofFloat(this, TWEEN_RADIUS, new float[]{0.57f, 1.0f});
        tweenRadius.setDuration(100);
        tweenRadius.setInterpolator(INTERPOLATOR_ENTER);
        builder.with(tweenRadius);
    }

    protected Animator createSoftwareExit() {
        ObjectAnimator opacity = ObjectAnimator.ofFloat(this, OPACITY, new float[]{0.0f});
        opacity.setAutoCancel(true);
        opacity.setDuration(200);
        opacity.setInterpolator(INTERPOLATOR_EXIT);
        AnimatorSet set = new AnimatorSet();
        Builder builder = set.play(opacity);
        switch (this.mType) {
            case 0:
                createSoftwareExitLinear(builder);
                break;
            default:
                createSoftwareExitRadial(builder);
                break;
        }
        return set;
    }

    private void createSoftwareExitLinear(Builder builder) {
        ObjectAnimator tweenRadius = ObjectAnimator.ofFloat(this, "TweenRadius", new float[]{this.mTweenRadius, 0.8f});
        tweenRadius.setDuration(200);
        tweenRadius.setInterpolator(INTERPOLATOR_EXIT);
        builder.with(tweenRadius);
    }

    private void createSoftwareExitRadial(Builder builder) {
    }

    protected RenderNodeAnimatorSet createHardwareExit(Paint p) {
        p.setAlpha((int) ((this.mOpacity * ((float) p.getAlpha())) + 0.5f));
        this.mPropPaint = CanvasProperty.createPaint(p);
        RenderNodeAnimator exit = new RenderNodeAnimator(this.mPropPaint, 1, 0.0f);
        exit.setInterpolator(INTERPOLATOR_EXIT);
        exit.setDuration(200);
        RenderNodeAnimatorSet set = new RenderNodeAnimatorSet();
        set.add(exit);
        switch (this.mType) {
            case 0:
                createHardwareExitLinear(set);
                break;
            default:
                createHardwareExitRadical(set);
                break;
        }
        return set;
    }

    private void createHardwareExitLinear(RenderNodeAnimatorSet set) {
        Rect rt = this.mBounds;
        this.mPropRx = CanvasProperty.createFloat(0.0f);
        this.mPropRy = CanvasProperty.createFloat(0.0f);
        this.mPropL = CanvasProperty.createFloat((float) rt.left);
        this.mPropR = CanvasProperty.createFloat((float) rt.right);
        this.mPropT = CanvasProperty.createFloat((float) rt.top);
        this.mPropB = CanvasProperty.createFloat((float) rt.bottom);
        RenderNodeAnimator rectT = new RenderNodeAnimator(this.mPropT, ((float) rt.centerY()) - ((((float) rt.height()) * 0.8f) / 2.0f));
        rectT.setDuration(200);
        rectT.setInterpolator(INTERPOLATOR_EXIT);
        RenderNodeAnimator rectB = new RenderNodeAnimator(this.mPropB, ((float) rt.centerY()) + ((((float) rt.height()) * 0.8f) / 2.0f));
        rectB.setDuration(200);
        rectB.setInterpolator(INTERPOLATOR_EXIT);
        set.add(rectT);
        set.add(rectB);
    }

    private void createHardwareExitRadical(RenderNodeAnimatorSet set) {
        this.mPropRadius = CanvasProperty.createFloat(this.mTargetRadius);
        this.mPropX = CanvasProperty.createFloat(0.0f);
        this.mPropY = CanvasProperty.createFloat(0.0f);
    }

    protected boolean drawSoftware(Canvas c, Paint p) {
        int origAlpha = p.getAlpha();
        int alpha = (int) ((((float) origAlpha) * this.mOpacity) + 0.5f);
        if (alpha > 0) {
            p.setAlpha(alpha);
            switch (this.mType) {
                case 0:
                    Rect rt = this.mBounds;
                    c.drawRect((float) rt.left, ((float) rt.centerY()) - ((this.mTweenRadius * ((float) rt.height())) / 2.0f), (float) rt.right, ((this.mTweenRadius * ((float) rt.height())) / 2.0f) + ((float) rt.centerY()), p);
                    break;
                default:
                    float x = this.mBounds.exactCenterX();
                    float y = this.mBounds.exactCenterY();
                    c.translate(x, y);
                    c.drawCircle(0.0f, 0.0f, this.mTweenRadius * this.mTargetRadius, p);
                    c.translate(-x, -y);
                    break;
            }
            p.setAlpha(origAlpha);
        }
        return false;
    }

    protected boolean drawHardware(DisplayListCanvas c) {
        switch (this.mType) {
            case 0:
                c.drawRoundRect(this.mPropL, this.mPropT, this.mPropR, this.mPropB, this.mPropRx, this.mPropRy, this.mPropPaint);
                break;
            default:
                float x = this.mBounds.exactCenterX();
                float y = this.mBounds.exactCenterY();
                c.translate(x, y);
                c.drawCircle(this.mPropX, this.mPropY, this.mPropRadius, this.mPropPaint);
                c.translate(-x, -y);
                break;
        }
        return true;
    }
}
