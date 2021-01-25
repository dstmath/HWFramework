package com.android.server.foldscreenview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.DisplayListCanvas;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class KeyButtonRipple extends Drawable {
    private static final Interpolator ALPHA_OUT = new PathInterpolator(0.0f, 0.0f, 0.8f, 1.0f);
    private static final int ANIMATION_DURATION_FADE = 450;
    private static final int ANIMATION_DURATION_SCALE = 350;
    private static final int BG_BLACK_COLOR = -16777216;
    private static final int BG_WHITE_COLOR = -1;
    private static final float DEFAULT_PROP_VALUE = 0.0f;
    private static final float DIVIDE_FACTORE = 2.0f;
    private static final float GLOW_ALPHA_MASK = 255.0f;
    private static final float GLOW_MAX_ALPHA = 0.2f;
    private static final float GLOW_MAX_ALPHA_DARK = 0.1f;
    private static final float GLOW_MAX_SCALE_FACTOR = 1.35f;
    private static final float INTERPOLATOR_BASE_VALUE = 1.0f;
    private static final float INTERPOLATOR_POWER_VALUE = 400.0f;
    private static final float INTERPOLATOR_SCALE_VALUE = 1.4f;
    private static final float SCALE_FACTOR = 0.5f;
    private static final String TAG = "KeyButtonRipple";
    private boolean isDark = false;
    private boolean isDelayTouchFeedback = false;
    private boolean isDrawingHardwareGlow;
    private boolean isPressed;
    private boolean isSupportHardware;
    private boolean isVisible;
    private final AnimatorListenerAdapter mAnimatorListener;
    private CanvasProperty<Float> mBottomProp;
    private float mGlowAlpha = 0.0f;
    private float mGlowScale = 1.0f;
    private final Handler mHandler = new Handler();
    private final Interpolator mInterpolator = new LogInterpolator();
    private CanvasProperty<Float> mLeftProp;
    private int mMaxWidth;
    private CanvasProperty<Paint> mPaintProp;
    private CanvasProperty<Float> mRightProp;
    private Paint mRipplePaint;
    private final HashSet<Animator> mRunningAnimations = new HashSet<>(0);
    private CanvasProperty<Float> mRxProp;
    private CanvasProperty<Float> mRyProp;
    private final View mTargetView;
    private final ArrayList<Animator> mTmpArrays = new ArrayList<>(0);
    private CanvasProperty<Float> mTopProp;

    public KeyButtonRipple(Context ctx, View targetView) {
        this.mMaxWidth = ctx.getResources().getDimensionPixelSize(34472723);
        this.mTargetView = targetView;
        this.mAnimatorListener = new AnimatorListenerAdapter() {
            /* class com.android.server.foldscreenview.KeyButtonRipple.AnonymousClass1 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                KeyButtonRipple.this.mRunningAnimations.remove(animation);
                if (KeyButtonRipple.this.mRunningAnimations.isEmpty() && !KeyButtonRipple.this.isPressed) {
                    KeyButtonRipple.this.isVisible = false;
                    KeyButtonRipple.this.isDrawingHardwareGlow = false;
                    KeyButtonRipple.this.invalidateSelf();
                }
            }
        };
    }

    public void setDarkIntensity(float darkIntensity) {
        this.isDark = darkIntensity >= 0.5f;
    }

    public void setDelayTouchFeedback(boolean isDelay) {
        this.isDelayTouchFeedback = isDelay;
    }

    private Paint getRipplePaint() {
        if (this.mRipplePaint == null) {
            this.mRipplePaint = new Paint();
            this.mRipplePaint.setAntiAlias(true);
            this.mRipplePaint.setColor(this.isDark ? BG_BLACK_COLOR : -1);
        }
        return this.mRipplePaint;
    }

    private void drawSoftware(Canvas canvas) {
        if (this.mGlowAlpha > 0.0f) {
            Paint rp = getRipplePaint();
            rp.setAlpha((int) (this.mGlowAlpha * GLOW_ALPHA_MASK));
            float width = (float) getBounds().width();
            float height = (float) getBounds().height();
            boolean isHorizontal = width > height;
            float radius = ((float) getRippleSize()) * this.mGlowScale * 0.5f;
            float cx = width * 0.5f;
            float cy = height * 0.5f;
            float rx = isHorizontal ? radius : cx;
            float ry = isHorizontal ? cy : radius;
            float corner = isHorizontal ? cy : cx;
            canvas.drawRoundRect(cx - rx, cy - ry, cx + rx, cy + ry, corner, corner, rp);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        this.isSupportHardware = canvas.isHardwareAccelerated();
        if (!this.isSupportHardware || !(canvas instanceof DisplayListCanvas)) {
            drawSoftware(canvas);
        } else {
            drawHardware((DisplayListCanvas) canvas);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    private boolean isHorizontal() {
        return getBounds().width() > getBounds().height();
    }

    private void drawHardware(DisplayListCanvas c) {
        if (this.isDrawingHardwareGlow) {
            c.drawRoundRect(this.mLeftProp, this.mTopProp, this.mRightProp, this.mBottomProp, this.mRxProp, this.mRyProp, this.mPaintProp);
        }
    }

    public float getGlowAlpha() {
        return this.mGlowAlpha;
    }

    public void setGlowAlpha(float glowAlpha) {
        this.mGlowAlpha = glowAlpha;
        invalidateSelf();
    }

    public float getGlowScale() {
        return this.mGlowScale;
    }

    public void setGlowScale(float glowScale) {
        this.mGlowScale = glowScale;
        invalidateSelf();
    }

    private float getMaxGlowAlpha() {
        return this.isDark ? 0.1f : 0.2f;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean onStateChange(int[] state) {
        boolean isPressedState = false;
        int length = state.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            } else if (state[i] == 16842919) {
                isPressedState = true;
                break;
            } else {
                i++;
            }
        }
        if (isPressedState == this.isPressed) {
            return false;
        }
        setPressed(isPressedState);
        this.isPressed = isPressedState;
        return true;
    }

    @Override // android.graphics.drawable.Drawable
    public void jumpToCurrentState() {
        cancelAnimations();
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isStateful() {
        return true;
    }

    public boolean hasFocusStateSpecified() {
        return true;
    }

    public void setPressed(boolean isPressedButton) {
        if (this.isSupportHardware) {
            setPressedHardware(isPressedButton);
        } else {
            setPressedSoftware(isPressedButton);
        }
    }

    public void abortDelayedRipple() {
        this.mHandler.removeCallbacksAndMessages(null);
    }

    private void cancelAnimations() {
        this.isVisible = false;
        this.mTmpArrays.addAll(this.mRunningAnimations);
        Iterator<Animator> it = this.mTmpArrays.iterator();
        while (it.hasNext()) {
            it.next().cancel();
        }
        this.mTmpArrays.clear();
        this.mRunningAnimations.clear();
        this.mHandler.removeCallbacksAndMessages(null);
    }

    private void setPressedSoftware(boolean isPressedButton) {
        if (!isPressedButton) {
            exitSoftware();
        } else if (!this.isDelayTouchFeedback) {
            enterSoftware();
        } else if (this.mRunningAnimations.isEmpty()) {
            this.mHandler.removeCallbacksAndMessages(null);
            this.mHandler.postDelayed(new Runnable() {
                /* class com.android.server.foldscreenview.$$Lambda$KeyButtonRipple$R3v7YYveGyAOjqJxBq3U8zXl0 */

                @Override // java.lang.Runnable
                public final void run() {
                    KeyButtonRipple.m0lambda$R3v7YYveGyAOjqJxBq3U8zXl0(KeyButtonRipple.this);
                }
            }, (long) ViewConfiguration.getTapTimeout());
        } else if (this.isVisible) {
            enterSoftware();
        } else {
            Log.i(TAG, "nothing need to do");
        }
    }

    /* access modifiers changed from: public */
    private void enterSoftware() {
        cancelAnimations();
        this.isVisible = true;
        this.mGlowAlpha = getMaxGlowAlpha();
        ObjectAnimator scaleAnimator = ObjectAnimator.ofFloat(this, "glowScale", 0.0f, GLOW_MAX_SCALE_FACTOR);
        scaleAnimator.setInterpolator(this.mInterpolator);
        scaleAnimator.setDuration(350L);
        scaleAnimator.addListener(this.mAnimatorListener);
        scaleAnimator.start();
        this.mRunningAnimations.add(scaleAnimator);
        if (this.isDelayTouchFeedback && !this.isPressed) {
            exitSoftware();
        }
    }

    private void exitSoftware() {
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(this, "glowAlpha", this.mGlowAlpha, 0.0f);
        alphaAnimator.setInterpolator(ALPHA_OUT);
        alphaAnimator.setDuration(450L);
        alphaAnimator.addListener(this.mAnimatorListener);
        alphaAnimator.start();
        this.mRunningAnimations.add(alphaAnimator);
    }

    private void setPressedHardware(boolean isPressedButton) {
        View view = this.mTargetView;
        if (view == null || !view.isAttachedToWindow()) {
            Log.i(TAG, "setPressedHardware error:" + this.mTargetView);
        } else if (!isPressedButton) {
            exitHardware();
        } else if (!this.isDelayTouchFeedback) {
            enterHardware();
        } else if (this.mRunningAnimations.isEmpty()) {
            this.mHandler.removeCallbacksAndMessages(null);
            this.mHandler.postDelayed(new Runnable() {
                /* class com.android.server.foldscreenview.$$Lambda$KeyButtonRipple$KjJHEayr2V_5Dw8osPGI3vX7So */

                @Override // java.lang.Runnable
                public final void run() {
                    KeyButtonRipple.m1lambda$KjJHEayr2V_5Dw8osPGI3vX7So(KeyButtonRipple.this);
                }
            }, (long) ViewConfiguration.getTapTimeout());
        } else if (this.isVisible) {
            enterHardware();
        } else {
            Log.i(TAG, "nothing need to do");
        }
    }

    private void setExtendStart(CanvasProperty<Float> prop) {
        if (isHorizontal()) {
            this.mLeftProp = prop;
        } else {
            this.mTopProp = prop;
        }
    }

    private CanvasProperty<Float> getExtendStart() {
        return isHorizontal() ? this.mLeftProp : this.mTopProp;
    }

    private void setExtendEnd(CanvasProperty<Float> prop) {
        if (isHorizontal()) {
            this.mRightProp = prop;
        } else {
            this.mBottomProp = prop;
        }
    }

    private CanvasProperty<Float> getExtendEnd() {
        return isHorizontal() ? this.mRightProp : this.mBottomProp;
    }

    private int getExtendSize() {
        return isHorizontal() ? getBounds().width() : getBounds().height();
    }

    private int getRippleSize() {
        int size = isHorizontal() ? getBounds().width() : getBounds().height();
        int i = this.mMaxWidth;
        return size < i ? size : i;
    }

    /* access modifiers changed from: public */
    private void enterHardware() {
        cancelAnimations();
        this.isVisible = true;
        this.isDrawingHardwareGlow = true;
        setExtendStart(CanvasProperty.createFloat(((float) getExtendSize()) / 2.0f));
        RenderNodeAnimator startAnim = new RenderNodeAnimator(getExtendStart(), (((float) getExtendSize()) / 2.0f) - ((((float) getRippleSize()) * GLOW_MAX_SCALE_FACTOR) / 2.0f));
        startAnim.setDuration(350);
        startAnim.setInterpolator(this.mInterpolator);
        startAnim.addListener(this.mAnimatorListener);
        startAnim.setTarget(this.mTargetView);
        setExtendEnd(CanvasProperty.createFloat(((float) getExtendSize()) / 2.0f));
        RenderNodeAnimator endAnim = new RenderNodeAnimator(getExtendEnd(), (((float) getExtendSize()) / 2.0f) + ((((float) getRippleSize()) * GLOW_MAX_SCALE_FACTOR) / 2.0f));
        endAnim.setDuration(350);
        endAnim.setInterpolator(this.mInterpolator);
        endAnim.addListener(this.mAnimatorListener);
        endAnim.setTarget(this.mTargetView);
        if (isHorizontal()) {
            this.mTopProp = CanvasProperty.createFloat(0.0f);
            this.mBottomProp = CanvasProperty.createFloat((float) getBounds().height());
            this.mRxProp = CanvasProperty.createFloat(((float) getBounds().height()) / 2.0f);
            this.mRyProp = CanvasProperty.createFloat(((float) getBounds().height()) / 2.0f);
        } else {
            this.mLeftProp = CanvasProperty.createFloat(0.0f);
            this.mRightProp = CanvasProperty.createFloat((float) getBounds().width());
            this.mRxProp = CanvasProperty.createFloat(((float) getBounds().width()) / 2.0f);
            this.mRyProp = CanvasProperty.createFloat(((float) getBounds().width()) / 2.0f);
        }
        this.mGlowScale = GLOW_MAX_SCALE_FACTOR;
        this.mGlowAlpha = getMaxGlowAlpha();
        Paint ripplePaint = getRipplePaint();
        ripplePaint.setAlpha((int) (this.mGlowAlpha * GLOW_ALPHA_MASK));
        this.mPaintProp = CanvasProperty.createPaint(ripplePaint);
        startAnim.start();
        endAnim.start();
        this.mRunningAnimations.add(startAnim);
        this.mRunningAnimations.add(endAnim);
        invalidateSelf();
        if (this.isDelayTouchFeedback && !this.isPressed) {
            exitHardware();
        }
    }

    private void exitHardware() {
        this.mPaintProp = CanvasProperty.createPaint(getRipplePaint());
        RenderNodeAnimator opacityAnim = new RenderNodeAnimator(this.mPaintProp, 1, 0.0f);
        opacityAnim.setDuration(450);
        opacityAnim.setInterpolator(ALPHA_OUT);
        opacityAnim.addListener(this.mAnimatorListener);
        opacityAnim.setTarget(this.mTargetView);
        opacityAnim.start();
        this.mRunningAnimations.add(opacityAnim);
        invalidateSelf();
    }

    /* access modifiers changed from: private */
    public static final class LogInterpolator implements Interpolator {
        private LogInterpolator() {
        }

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float input) {
            return 1.0f - ((float) Math.pow(400.0d, (double) ((-input) * KeyButtonRipple.INTERPOLATOR_SCALE_VALUE)));
        }
    }
}
