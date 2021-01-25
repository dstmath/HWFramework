package com.android.server.input;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Region;
import android.graphics.Shader;
import android.util.Log;

public class HwCircleAnimation {
    private static final int ALPHA_COEF = 255;
    public static final int ANIMATION_TYPE_APP = 4;
    public static final int ANIMATION_TYPE_BACK = 2;
    public static final int ANIMATION_TYPE_HOME = 1;
    public static final int ANIMATION_TYPE_MULTI = 3;
    public static final float BG_ALPHA = 0.4f;
    public static final float BG_ALPHA_FILL = 0.2f;
    private static final int[] BG_COLORS = {-16777216, 2130706432, 0};
    private static final float[] BG_COLOR_POS = {0.0f, 0.3f, 1.0f};
    private static final int BG_HEIGHT = 480;
    public static final int CIRCLE_COLOR = -14104128;
    public static final int CORNER_RADIUS = 4;
    private static final int DENSITY_DPI_COEF = 160;
    private static final int DETA_Y = 150;
    private static final int DIVIDEND = 2;
    private static final int FIX_ROUND_OUTPUT = 2;
    public static final float INNER_ALPHA = 0.5f;
    private static final int INNER_RADIUS = 87;
    private static final int INNER_RADIUS_0 = 60;
    public static final int INNER_RADIUS_LUNCH = 99;
    private static final boolean IS_DEBUG = false;
    public static final int LUNCH_DURATION = 400;
    private static final float PROCESS_THRESHOLD = 1.0f;
    public static final int RINGOUT_DURATION = 200;
    public static final float RING_ALPHA = 0.7f;
    private static final int RING_RADIUS = 99;
    private static final int RING_RADIUS_0 = 42;
    private static final int RING_RADIUS_FILL = 36;
    private static final int RING_RADIUS_LUNCH = 225;
    private static final int RING_WIDTH = 2;
    private static final int RING_WIDTH_0 = 12;
    public static final float SMALL_ALPHA = 1.0f;
    private static final int SMALL_MULTI_LEN = 48;
    private static final int SMALL_RADIUS = 27;
    private static final int SMALL_WIDTH = 6;
    public static final String TAG = "pressure:HwCircleAnimation";
    private static int sBackgroundHeight = BG_HEIGHT;
    private static int sInnerRadius = INNER_RADIUS;
    private static int sInnerRadius0 = 60;
    private static int sRingRadius = 99;
    private static int sRingRadius0 = 42;
    private static int sRingRadiusLunch = RING_RADIUS_LUNCH;
    private static int sRingWidth = 2;
    private static int sRingWidth0 = 12;
    private static int sSmallMultiLen = 48;
    private static int sSmallPadding = 0;
    private static int sSmallRadius = 27;
    private static int sSmallWidth = 6;
    private static int sSmallY;
    private static int sSmallY0;
    private static int sSmallYlunch = (sSmallY + 150);
    ValueAnimator mAnimRing;
    AnimationUpdateListener mAnimUpdateListener;
    float mBgAlpha = 0.0f;
    Shader mBgShader = null;
    CornerPathEffect mCornerEffect;
    int mCx;
    int mCy;
    float mDpi = 1.0f;
    float mFillProcess;
    private int mHeigh;
    float mInnerAlpha;
    int mInnerRadius;
    boolean mIsCircleAnimating = false;
    private boolean mIsShowing = false;
    Matrix mMatrix = new Matrix();
    Paint mPaint = new Paint();
    PropertyValuesHolder mPvhRing0;
    PropertyValuesHolder mPvhRing1;
    PropertyValuesHolder mPvhRing2;
    PropertyValuesHolder mPvhRing3;
    PropertyValuesHolder mPvhRing4;
    PropertyValuesHolder mPvhRing5;
    PropertyValuesHolder mPvhRing6;
    PropertyValuesHolder mPvhRing7;
    float mRingAlpha = 0.0f;
    int mRingRadius;
    int mRingWidth;
    float mSmallAlpha = 0.0f;
    int mSmallY;
    int mType;
    private int mWidth;

    public interface AnimationUpdateListener {
        void onAnimationUpdate();
    }

    static {
        int i = sRingRadius0;
        int i2 = sSmallRadius;
        sSmallY0 = i + i2;
        sSmallY = sRingRadius + 36 + i2;
    }

    public HwCircleAnimation(AnimationUpdateListener listener, Resources resources) {
        if (listener == null || resources == null) {
            Log.w(TAG, "HwCircleAnimation update listener is null, animation need this to invalidate view, or resource is null: " + resources);
        }
        this.mPaint.setColor(CIRCLE_COLOR);
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mPaint.setAntiAlias(true);
        this.mCornerEffect = new CornerPathEffect(4.0f);
        this.mPaint.setPathEffect(this.mCornerEffect);
        this.mAnimUpdateListener = listener;
        if (resources != null) {
            this.mDpi = resources.getDisplayMetrics().density / ((float) (resources.getDisplayMetrics().densityDpi / DENSITY_DPI_COEF));
        }
        convDpi();
    }

    public void setViewSize(int width, int height) {
        this.mHeigh = height;
        this.mWidth = width;
        if (this.mBgShader == null) {
            this.mBgShader = new RadialGradient(0.0f, 0.0f, (float) sBackgroundHeight, BG_COLORS, BG_COLOR_POS, Shader.TileMode.CLAMP);
        }
    }

    private void convDpi() {
        float f = this.mDpi;
        sRingWidth0 = (int) (((float) sRingWidth0) * f);
        sRingWidth = (int) (((float) sRingWidth) * f);
        sRingRadius0 = (int) (((float) sRingRadius0) * f);
        sRingRadius = (int) (((float) sRingRadius) * f);
        sSmallRadius = (int) (((float) sSmallRadius) * f);
        sSmallWidth = (int) (((float) sSmallWidth) * f);
        sSmallPadding = (int) (((float) sSmallPadding) * f);
        sBackgroundHeight = (int) (((float) sBackgroundHeight) * f);
        sSmallY0 = (int) (((float) sSmallY0) * f);
        sSmallY = (int) (((float) sSmallY) * f);
        sInnerRadius0 = (int) (((float) sInnerRadius0) * f);
        sInnerRadius = (int) (((float) sInnerRadius) * f);
        sRingRadiusLunch = (int) (((float) sRingRadiusLunch) * f);
        sSmallYlunch = (int) (((float) sSmallYlunch) * f);
        sSmallMultiLen = (int) (((float) sSmallMultiLen) * f);
    }

    public void setCenterCoordinate(int cx, int cy) {
        this.mCx = cx;
        this.mCy = cy;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public void startRingOutAnim(int cx, int cy, int type) {
        Log.d(TAG, "startRingOutAnim " + cx + ", " + cy + "  mIsCircleAnimating = " + this.mIsCircleAnimating);
        if (!this.mIsCircleAnimating) {
            this.mType = type;
            this.mIsCircleAnimating = true;
            this.mCx = cx;
            this.mCy = cy;
            this.mMatrix.reset();
            this.mMatrix.setTranslate((float) this.mCx, (float) this.mCy);
            if (this.mAnimRing == null) {
                this.mPvhRing0 = PropertyValuesHolder.ofInt("ringWidth", 0, 0);
                this.mPvhRing1 = PropertyValuesHolder.ofInt("ringRadius", 0, 0);
                this.mPvhRing2 = PropertyValuesHolder.ofFloat("ringAlpha", 0.0f, 0.0f);
                this.mPvhRing3 = PropertyValuesHolder.ofFloat("smallAlpha", 0.0f, 0.0f);
                this.mPvhRing4 = PropertyValuesHolder.ofInt("smallY", 0, 0);
                this.mPvhRing5 = PropertyValuesHolder.ofFloat("bgAlpha", 0.0f, 0.0f);
                this.mPvhRing6 = PropertyValuesHolder.ofInt("innerRadius", 0, 0);
                this.mPvhRing7 = PropertyValuesHolder.ofFloat("innerAlpha", 0.0f, 0.0f);
                this.mAnimRing = ObjectAnimator.ofPropertyValuesHolder(this, this.mPvhRing0, this.mPvhRing1, this.mPvhRing2, this.mPvhRing3, this.mPvhRing4, this.mPvhRing5, this.mPvhRing6, this.mPvhRing7);
                this.mAnimRing.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    /* class com.android.server.input.HwCircleAnimation.AnonymousClass1 */

                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (HwCircleAnimation.this.mAnimUpdateListener != null) {
                            HwCircleAnimation.this.mAnimUpdateListener.onAnimationUpdate();
                        }
                    }
                });
            }
            this.mAnimRing.setDuration(200L);
            this.mPvhRing0.setIntValues(sRingWidth0, sRingWidth);
            this.mPvhRing1.setIntValues(sRingRadius0, sRingRadius);
            this.mPvhRing2.setFloatValues(0.0f, 0.7f);
            this.mPvhRing3.setFloatValues(0.0f, 1.0f);
            this.mPvhRing4.setIntValues(sSmallY0, sSmallY);
            this.mPvhRing5.setFloatValues(0.0f, 0.4f);
            this.mPvhRing6.setIntValues(0, sInnerRadius0);
            this.mPvhRing7.setFloatValues(0.0f, 0.5f);
            this.mAnimRing.start();
        }
    }

    public void endRingOutAnim(boolean isLunched) {
        Log.d(TAG, "endRingOutAnim = " + isLunched + ", mIsCircleAnimating = " + this.mIsCircleAnimating);
        if (this.mIsCircleAnimating) {
            this.mIsCircleAnimating = false;
            ValueAnimator valueAnimator = this.mAnimRing;
            if (valueAnimator != null && !isLunched) {
                valueAnimator.cancel();
                this.mFillProcess = 0.0f;
                this.mPvhRing0.setIntValues(this.mRingWidth, sRingWidth0);
                this.mPvhRing1.setIntValues(this.mRingRadius, sRingRadius0);
                this.mPvhRing2.setFloatValues(this.mRingAlpha, 0.0f);
                this.mPvhRing3.setFloatValues(this.mSmallAlpha, 0.0f);
                this.mPvhRing4.setIntValues(this.mSmallY, sSmallY0);
                this.mPvhRing5.setFloatValues(this.mBgAlpha, 0.0f);
                this.mPvhRing6.setIntValues(this.mInnerRadius, 0);
                this.mPvhRing7.setFloatValues(this.mInnerAlpha, 0.0f);
                this.mAnimRing.setDuration(200L);
                this.mAnimRing.start();
            }
        }
    }

    public synchronized void resetAnimaion() {
        boolean isUpdate = false;
        if (!(this.mRingAlpha == 0.0f && this.mSmallAlpha == 0.0f && this.mBgAlpha == 0.0f && this.mInnerAlpha == 0.0f && !this.mIsShowing)) {
            isUpdate = true;
        }
        this.mRingWidth = sRingWidth0;
        this.mRingRadius = sRingRadius0;
        this.mRingAlpha = 0.0f;
        this.mSmallAlpha = 0.0f;
        this.mSmallY = sSmallY0;
        this.mBgAlpha = 0.0f;
        this.mInnerRadius = 0;
        this.mInnerAlpha = 0.0f;
        if (isUpdate && this.mAnimUpdateListener != null) {
            this.mAnimUpdateListener.onAnimationUpdate();
        }
    }

    public boolean setFillProcess(float process) {
        float fillProcess = process;
        if (fillProcess <= 0.0f) {
            fillProcess = 0.0f;
        }
        if (fillProcess >= 1.0f) {
            fillProcess = 1.0f;
        }
        this.mFillProcess = fillProcess;
        if (this.mFillProcess >= 1.0f) {
            startLunchAnimation();
            return true;
        }
        AnimationUpdateListener animationUpdateListener = this.mAnimUpdateListener;
        if (animationUpdateListener == null) {
            return false;
        }
        animationUpdateListener.onAnimationUpdate();
        return false;
    }

    private void startLunchAnimation() {
        Log.d(TAG, "startLunchAnimation");
        ValueAnimator valueAnimator = this.mAnimRing;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.mFillProcess = 1.0f;
            this.mPvhRing0.setIntValues(sRingWidth, 0);
            this.mPvhRing1.setIntValues(sRingRadius, sRingRadiusLunch);
            this.mPvhRing2.setFloatValues(0.7f, 0.0f);
            this.mPvhRing3.setFloatValues(1.0f, 0.0f);
            this.mPvhRing4.setIntValues(sSmallY, sSmallYlunch);
            this.mPvhRing5.setFloatValues(0.4f, 0.0f);
            this.mPvhRing6.setIntValues(sInnerRadius0, 99);
            this.mPvhRing7.setFloatValues(0.5f, 0.0f);
            this.mAnimRing.setDuration(400L);
            this.mAnimRing.start();
        }
    }

    public void draw(Canvas canvas) {
        drawRing(canvas);
    }

    public synchronized void setTipShowStatus(boolean isShowStatus) {
        this.mIsShowing = isShowStatus;
    }

    private void drawRing(Canvas canvas) {
        if (this.mType == 4) {
            this.mBgShader.setLocalMatrix(this.mMatrix);
            this.mPaint.setStyle(Paint.Style.FILL);
            float alpha = this.mBgAlpha;
            if (alpha >= 0.4f) {
                alpha += this.mFillProcess * 0.2f;
            }
            this.mPaint.setAlpha((int) (alpha * 255.0f));
            Shader shader = this.mPaint.getShader();
            this.mPaint.setShader(this.mBgShader);
            canvas.drawCircle((float) this.mCx, (float) this.mCy, (float) sBackgroundHeight, this.mPaint);
            this.mPaint.setShader(shader);
        }
        if (this.mRingAlpha > 0.0f) {
            this.mPaint.setStyle(Paint.Style.STROKE);
            this.mPaint.setStrokeWidth((float) this.mRingWidth);
            this.mPaint.setAlpha((int) (this.mRingAlpha * 255.0f));
            canvas.save();
            int i = this.mCx;
            int i2 = sSmallRadius;
            int i3 = sSmallPadding;
            int i4 = this.mCy;
            int i5 = this.mSmallY;
            canvas.clipRect((float) ((i - i2) - i3), (float) (((i4 - i5) - i2) - i3), (float) (i + i2 + i3), (float) ((i4 - i5) + i2 + i3), Region.Op.XOR);
            canvas.drawCircle((float) this.mCx, (float) this.mCy, ((float) this.mRingRadius) + (this.mFillProcess * 36.0f), this.mPaint);
            canvas.restore();
            this.mPaint.setStrokeWidth((float) sSmallWidth);
            this.mPaint.setAlpha((int) (this.mSmallAlpha * 255.0f));
            if (this.mHeigh >= this.mWidth) {
                drawSmall(canvas, this.mType, this.mCx, this.mCy - this.mSmallY, this.mPaint);
            } else {
                drawSmall(canvas, this.mType, this.mCx - this.mSmallY, this.mCy, this.mPaint);
            }
        }
        if (this.mFillProcess > 0.0f) {
            this.mPaint.setStrokeWidth(0.0f);
            this.mPaint.setAlpha((int) (this.mInnerAlpha * 255.0f));
            canvas.drawCircle((float) this.mCx, (float) this.mCy, (float) (this.mInnerRadius + ((int) (this.mFillProcess * ((float) (sInnerRadius - sInnerRadius0))))), this.mPaint);
        }
    }

    private void setRingWidth(int width) {
        this.mRingWidth = width;
    }

    private void setRingRadius(int radius) {
        this.mRingRadius = radius;
    }

    private void setBgAlpha(float alpha) {
        this.mBgAlpha = alpha;
    }

    private void setRingAlpha(float alpha) {
        this.mRingAlpha = alpha;
    }

    private void setSmallAlpha(float alpha) {
        this.mSmallAlpha = alpha;
    }

    private void setSmallY(int smallY) {
        this.mSmallY = smallY;
    }

    private void setInnerRadius(int radius) {
        this.mInnerRadius = radius;
    }

    private void setInnerAlpha(float alpha) {
        this.mInnerAlpha = alpha;
    }

    private void drawSmall(Canvas canvas, int type, int cx, int cy, Paint paint) {
        int i = this.mType;
        if (i == 1) {
            canvas.drawCircle((float) cx, (float) cy, (float) sSmallRadius, paint);
        } else if (i == 2) {
            int i2 = sSmallMultiLen;
            canvas.drawLine((float) (cx - (i2 / 2)), (float) cy, (float) ((i2 / 2) + cx), (float) ((((i2 / 2) + cy) + 2) - 2), paint);
            int i3 = sSmallMultiLen;
            canvas.drawLine((float) (cx - (i3 / 2)), (float) cy, (float) ((i3 / 2) + cx), (float) (((cy - (i3 / 2)) - 2) - 2), paint);
            int i4 = sSmallMultiLen;
            canvas.drawLine((float) ((i4 / 2) + cx), (float) ((((i4 / 2) + cy) + 2) - 2), (float) ((i4 / 2) + cx), (float) (((cy - (i4 / 2)) - 2) + 2), paint);
        } else if (i == 3) {
            int i5 = sSmallMultiLen;
            canvas.drawRoundRect((float) (cx - (i5 / 2)), (float) (cy - (i5 / 2)), (float) ((i5 / 2) + cx), (float) ((i5 / 2) + cy), 4.0f, 4.0f, paint);
        }
    }
}
