package android.graphics.drawable;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.RippleComponent.RenderNodeAnimatorSet;
import android.hardware.camera2.params.TonemapCurve;
import android.util.FloatProperty;
import android.view.DisplayListCanvas;
import android.view.RenderNodeAnimator;
import android.view.animation.LinearInterpolator;

class RippleBackground extends RippleComponent {
    private static final TimeInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final BackgroundProperty OPACITY = new BackgroundProperty("opacity") {
        public void setValue(RippleBackground object, float value) {
            object.mOpacity = value;
            object.invalidateSelf();
        }

        public Float get(RippleBackground object) {
            return Float.valueOf(object.mOpacity);
        }
    };
    private static final int OPACITY_ENTER_DURATION = 600;
    private static final int OPACITY_ENTER_DURATION_FAST = 120;
    private static final int OPACITY_EXIT_DURATION = 480;
    private boolean mIsBounded;
    private float mOpacity = TonemapCurve.LEVEL_BLACK;
    private CanvasProperty<Paint> mPropPaint;
    private CanvasProperty<Float> mPropRadius;
    private CanvasProperty<Float> mPropX;
    private CanvasProperty<Float> mPropY;

    private static abstract class BackgroundProperty extends FloatProperty<RippleBackground> {
        public BackgroundProperty(String name) {
            super(name);
        }
    }

    public RippleBackground(RippleDrawable owner, Rect bounds, boolean isBounded, boolean forceSoftware) {
        super(owner, bounds, forceSoftware);
        this.mIsBounded = isBounded;
    }

    public boolean isVisible() {
        return this.mOpacity <= TonemapCurve.LEVEL_BLACK ? isHardwareAnimating() : true;
    }

    protected boolean drawSoftware(Canvas c, Paint p) {
        int origAlpha = p.getAlpha();
        int alpha = (int) ((((float) origAlpha) * this.mOpacity) + 0.5f);
        if (alpha <= 0) {
            return false;
        }
        p.setAlpha(alpha);
        c.drawCircle(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, this.mTargetRadius, p);
        p.setAlpha(origAlpha);
        return true;
    }

    protected boolean drawHardware(DisplayListCanvas c) {
        c.drawCircle(this.mPropX, this.mPropY, this.mPropRadius, this.mPropPaint);
        return true;
    }

    protected Animator createSoftwareEnter(boolean fast) {
        int duration = (int) ((1.0f - this.mOpacity) * ((float) (fast ? 120 : OPACITY_ENTER_DURATION)));
        ObjectAnimator opacity = ObjectAnimator.ofFloat((Object) this, OPACITY, 1.0f);
        opacity.setAutoCancel(true);
        opacity.setDuration((long) duration);
        opacity.setInterpolator(LINEAR_INTERPOLATOR);
        return opacity;
    }

    protected Animator createSoftwareExit() {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator exit = ObjectAnimator.ofFloat((Object) this, OPACITY, TonemapCurve.LEVEL_BLACK);
        exit.setInterpolator(LINEAR_INTERPOLATOR);
        exit.setDuration(480);
        exit.setAutoCancel(true);
        Builder builder = set.play(exit);
        int fastEnterDuration = this.mIsBounded ? (int) ((1.0f - this.mOpacity) * 120.0f) : 0;
        if (fastEnterDuration > 0) {
            Animator enter = ObjectAnimator.ofFloat((Object) this, OPACITY, 1.0f);
            enter.setInterpolator(LINEAR_INTERPOLATOR);
            enter.setDuration((long) fastEnterDuration);
            enter.setAutoCancel(true);
            builder.after(enter);
        }
        return set;
    }

    protected RenderNodeAnimatorSet createHardwareExit(Paint p) {
        RenderNodeAnimatorSet set = new RenderNodeAnimatorSet();
        int targetAlpha = p.getAlpha();
        p.setAlpha((int) ((this.mOpacity * ((float) targetAlpha)) + 0.5f));
        this.mPropPaint = CanvasProperty.createPaint(p);
        this.mPropRadius = CanvasProperty.createFloat(this.mTargetRadius);
        this.mPropX = CanvasProperty.createFloat(TonemapCurve.LEVEL_BLACK);
        this.mPropY = CanvasProperty.createFloat(TonemapCurve.LEVEL_BLACK);
        int fastEnterDuration = this.mIsBounded ? (int) ((1.0f - this.mOpacity) * 120.0f) : 0;
        RenderNodeAnimator exit = new RenderNodeAnimator(this.mPropPaint, 1, TonemapCurve.LEVEL_BLACK);
        exit.setInterpolator(LINEAR_INTERPOLATOR);
        exit.setDuration(480);
        if (fastEnterDuration > 0) {
            exit.setStartDelay((long) fastEnterDuration);
            exit.setStartValue((float) targetAlpha);
        }
        set.add(exit);
        if (fastEnterDuration > 0) {
            RenderNodeAnimator enter = new RenderNodeAnimator(this.mPropPaint, 1, (float) targetAlpha);
            enter.setInterpolator(LINEAR_INTERPOLATOR);
            enter.setDuration((long) fastEnterDuration);
            set.add(enter);
        }
        return set;
    }

    protected void jumpValuesToExit() {
        this.mOpacity = TonemapCurve.LEVEL_BLACK;
    }
}
