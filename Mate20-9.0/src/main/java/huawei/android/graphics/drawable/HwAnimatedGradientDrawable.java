package huawei.android.graphics.drawable;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.animation.PathInterpolator;

public class HwAnimatedGradientDrawable extends GradientDrawable {
    private static final long ANIM_DURATION = 100;
    private static final float DECISION = 1.0E-7f;
    private static final int DEFAULT_COLOR = -16777216;
    private static final float DEFAULT_CORNER_RADIUS = 12.0f;
    private static final float DEFAULT_CORNER_RADIUS_IN_DP = 4.0f;
    private static final float DEFAULT_RECT_ALPHA_BIG = 0.05f;
    private static final float DEFAULT_RECT_ALPHA_SMALL = 0.0f;
    private static final float DEFAULT_RECT_SCALE_BIG = 1.0f;
    private static final float DEFAULT_RECT_SCALE_SMALL = 0.9f;
    private static final int MAX_ALPHA_INT = 255;
    private static final String TAG = "HwAnimatedGradientDrawable";
    private static final TimeInterpolator sStandardInterpolator = new PathInterpolator(0.2f, DEFAULT_RECT_ALPHA_SMALL, 0.4f, DEFAULT_RECT_SCALE_BIG);
    private boolean mEffectActive;
    private Animator mEnterAnim;
    private Animator mExitAnim;
    private boolean mForceDoScaleAnim;
    private float mMaxRectAlpha;
    private float mMaxRectScale;
    private float mMinRectScale;
    private float mRectAlpha;
    private float mRectScale;

    public HwAnimatedGradientDrawable() {
        this((int) DEFAULT_COLOR, (float) DEFAULT_RECT_ALPHA_BIG, (float) DEFAULT_CORNER_RADIUS);
    }

    public HwAnimatedGradientDrawable(int color, float maxAlpha, float cornerRadius) {
        init(color, maxAlpha, cornerRadius);
    }

    public HwAnimatedGradientDrawable(Context context) {
        this((int) DEFAULT_COLOR, (float) DEFAULT_RECT_ALPHA_BIG, context);
    }

    public HwAnimatedGradientDrawable(int color, float maxAlpha, Context context) {
        this(color, maxAlpha, DEFAULT_CORNER_RADIUS_IN_DP, context);
    }

    public HwAnimatedGradientDrawable(int color, float maxAlpha, float cornerRadiusInDp, Context context) {
        if (context != null) {
            init(color, maxAlpha, cornerRadiusInDp * context.getResources().getDisplayMetrics().density);
        } else {
            init(color, maxAlpha, DEFAULT_CORNER_RADIUS);
        }
    }

    private void init(int color, float maxAlpha, float cornerRadius) {
        setShape(0);
        setColor(color);
        setCornerRadius(cornerRadius);
        this.mEffectActive = false;
        this.mMaxRectAlpha = maxAlpha;
        this.mRectAlpha = DEFAULT_RECT_ALPHA_SMALL;
        this.mMaxRectScale = DEFAULT_RECT_SCALE_BIG;
        this.mMinRectScale = DEFAULT_RECT_SCALE_SMALL;
        this.mForceDoScaleAnim = false;
    }

    public void setMaxRectAlpha(float maxRectAlpha) {
        if (maxRectAlpha < DEFAULT_RECT_ALPHA_SMALL || maxRectAlpha > DEFAULT_RECT_SCALE_BIG) {
            Log.w(TAG, "illegal params: maxRectAlpha = " + maxRectAlpha);
            return;
        }
        this.mMaxRectAlpha = maxRectAlpha;
    }

    public float getMaxRectAlpha() {
        return this.mMaxRectAlpha;
    }

    public void setMaxRectScale(float maxRectScale) {
        if (maxRectScale < DEFAULT_RECT_ALPHA_SMALL || maxRectScale > DEFAULT_RECT_SCALE_BIG) {
            Log.w(TAG, "illegal params: maxRectScale = " + maxRectScale);
            return;
        }
        this.mMaxRectScale = maxRectScale;
    }

    public float getMaxRectScale() {
        return this.mMaxRectScale;
    }

    public void setMinRectScale(float minRectScale) {
        if (minRectScale < DEFAULT_RECT_ALPHA_SMALL || minRectScale > DEFAULT_RECT_SCALE_BIG) {
            Log.w(TAG, "illegal params: minRectScale = " + minRectScale);
            return;
        }
        this.mMinRectScale = minRectScale;
    }

    public float getMinRectScale() {
        return this.mMinRectScale;
    }

    public void setRectAlpha(float rectAlpha) {
        if (rectAlpha < DEFAULT_RECT_ALPHA_SMALL || rectAlpha > DEFAULT_RECT_SCALE_BIG) {
            Log.w(TAG, "illegal params: rectAlpha = " + rectAlpha);
            return;
        }
        this.mRectAlpha = rectAlpha;
        invalidateSelf();
    }

    public float getRectAlpha() {
        return this.mRectAlpha;
    }

    public void setRectScale(float rectScale) {
        if (rectScale < DEFAULT_RECT_ALPHA_SMALL || rectScale > DEFAULT_RECT_SCALE_BIG) {
            Log.w(TAG, "illegal params: rectScale = " + rectScale);
            return;
        }
        this.mRectScale = rectScale;
        invalidateSelf();
    }

    public float getRectScale() {
        return this.mRectScale;
    }

    public void setForceDoScaleAnim(boolean forceDoScaleAnim) {
        this.mForceDoScaleAnim = forceDoScaleAnim;
    }

    public boolean isForceDoScaleAnim() {
        return this.mForceDoScaleAnim;
    }

    public void draw(Canvas canvas) {
        float alpha = this.mRectAlpha;
        float scale = this.mRectScale;
        if (alpha >= DECISION) {
            setAlpha((int) (255.0f * alpha));
            canvas.save();
            canvas.scale(scale, scale, ((float) canvas.getWidth()) / 2.0f, ((float) canvas.getHeight()) / 2.0f);
            super.draw(canvas);
            canvas.restore();
        }
    }

    /* access modifiers changed from: protected */
    public boolean onStateChange(int[] stateSet) {
        boolean enabled = false;
        boolean z = false;
        boolean pressed = false;
        for (int state : stateSet) {
            if (state == 16842910) {
                enabled = true;
            } else if (state == 16842919) {
                pressed = true;
            }
        }
        if (enabled && pressed) {
            z = true;
        }
        setEffectActive(z);
        return true;
    }

    private void setEffectActive(boolean active) {
        if (this.mEffectActive != active) {
            this.mEffectActive = active;
            if (active) {
                if (this.mEnterAnim == null || !this.mEnterAnim.isRunning()) {
                    if (this.mExitAnim != null && this.mExitAnim.isRunning()) {
                        this.mExitAnim.cancel();
                    }
                    startEnterAnim();
                }
            } else if (this.mExitAnim == null || !this.mExitAnim.isRunning()) {
                if (this.mEnterAnim != null && this.mEnterAnim.isRunning()) {
                    this.mEnterAnim.cancel();
                }
                startExitAnim();
            }
        }
    }

    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);
        if (!visible) {
            clearEffect();
        } else if (changed) {
            if (this.mEffectActive) {
                this.mRectAlpha = this.mMaxRectAlpha;
                this.mRectScale = this.mMaxRectScale;
            } else {
                this.mRectAlpha = DEFAULT_RECT_ALPHA_SMALL;
            }
        }
        return changed;
    }

    private void clearEffect() {
        if (this.mEnterAnim != null && this.mEnterAnim.isRunning()) {
            this.mEnterAnim.end();
        }
        if (this.mExitAnim != null && this.mExitAnim.isRunning()) {
            this.mExitAnim.end();
        }
        this.mEnterAnim = null;
        this.mExitAnim = null;
        this.mEffectActive = false;
        this.mRectAlpha = DEFAULT_RECT_ALPHA_SMALL;
        invalidateSelf();
    }

    private void startEnterAnim() {
        Animator scaleAnimator;
        AnimatorSet animatorSet = new AnimatorSet();
        Animator alphaAnimator = ObjectAnimator.ofFloat(this, "rectAlpha", new float[]{this.mMaxRectAlpha});
        alphaAnimator.setDuration(ANIM_DURATION);
        alphaAnimator.setInterpolator(sStandardInterpolator);
        if (getCornerRadius() > DEFAULT_RECT_ALPHA_SMALL || this.mForceDoScaleAnim) {
            if (getRectAlpha() < DECISION) {
                scaleAnimator = ObjectAnimator.ofFloat(this, "rectScale", new float[]{this.mMinRectScale, this.mMaxRectScale});
            } else {
                scaleAnimator = ObjectAnimator.ofFloat(this, "rectScale", new float[]{this.mMaxRectScale});
            }
            scaleAnimator.setDuration(ANIM_DURATION);
            scaleAnimator.setInterpolator(sStandardInterpolator);
            animatorSet.playTogether(new Animator[]{alphaAnimator, scaleAnimator});
        } else {
            setRectScale(DEFAULT_RECT_SCALE_BIG);
            animatorSet.play(alphaAnimator);
        }
        this.mEnterAnim = animatorSet;
        this.mEnterAnim.start();
    }

    private void startExitAnim() {
        AnimatorSet animatorSet = new AnimatorSet();
        Animator alphaAnimator = ObjectAnimator.ofFloat(this, "rectAlpha", new float[]{0.0f});
        alphaAnimator.setDuration(ANIM_DURATION);
        alphaAnimator.setInterpolator(sStandardInterpolator);
        animatorSet.playTogether(new Animator[]{alphaAnimator});
        this.mExitAnim = animatorSet;
        this.mExitAnim.start();
    }

    public void getOutline(Outline outline) {
        outline.setRect(getBounds());
        outline.setAlpha(DEFAULT_RECT_ALPHA_SMALL);
    }

    public boolean isStateful() {
        return true;
    }
}
