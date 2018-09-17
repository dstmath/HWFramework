package com.android.server.input;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RadialGradient;
import android.graphics.Region.Op;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.Log;
import com.android.server.rms.iaware.cpu.CPUFeature;

public class HwCircleAnimation {
    public static final int ANIMATION_TYPE_APP = 4;
    public static final int ANIMATION_TYPE_BACK = 2;
    public static final int ANIMATION_TYPE_HOME = 1;
    public static final int ANIMATION_TYPE_MULTI = 3;
    public static final float BG_ALPHA = 0.4f;
    public static final float BG_ALPHA_FILL = 0.2f;
    private static final int[] BG_COLORS = new int[]{-16777216, 2130706432, 0};
    private static final float[] BG_COLOR_POS = new float[]{0.0f, 0.3f, 1.0f};
    private static int BG_HEIGHT = 480;
    public static final int CIRCLE_COLOR = -14104128;
    public static final int CORNER_RADIUS = 4;
    private static final boolean DEBUG = false;
    public static final float DENSITY_FHD = 3.0f;
    public static final float INNER_ALPHA = 0.5f;
    private static int INNER_RADIUS = 87;
    private static int INNER_RADIUS_0 = 60;
    public static final int INNER_RADIUS_LUNCH = 99;
    public static final int LUNCH_DURATION = 400;
    public static final int RINGOUT_DURATION = 200;
    public static final float RING_ALPHA = 0.7f;
    private static int RING_RADIUS = 99;
    private static int RING_RADIUS_0 = 42;
    private static final int RING_RADIUS_FILL = 36;
    private static int RING_RADIUS_LUNCH = 225;
    private static int RING_WIDTH = 2;
    private static int RING_WIDTH_0 = 12;
    public static final float SMALL_ALPHA = 1.0f;
    private static int SMALL_BACK_LEN = 54;
    private static int SMALL_MULTI_LEN = 48;
    private static int SMALL_PADDING = 0;
    private static int SMALL_RADIUS = 27;
    private static int SMALL_WIDTH = 6;
    private static int SMALL_Y = ((RING_RADIUS + 36) + SMALL_RADIUS);
    private static int SMALL_Y_0 = (RING_RADIUS_0 + SMALL_RADIUS);
    private static int SMALL_Y_LUNCH = (SMALL_Y + CPUFeature.MSG_SET_VIP_THREAD_PARAMS);
    public static final String TAG = "pressure:HwCircleAnimation";
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
    private boolean showing = false;

    public interface AnimationUpdateListener {
        void onAnimationUpdate();
    }

    public HwCircleAnimation(AnimationUpdateListener listener, Resources resources) {
        if (listener == null || resources == null) {
            Log.w(TAG, "HwCircleAnimation update listener is null, animation need this to invalidate view, or resource is null: " + resources);
        }
        this.mPaint.setColor(CIRCLE_COLOR);
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setStrokeCap(Cap.ROUND);
        this.mPaint.setAntiAlias(true);
        this.mCornerEffect = new CornerPathEffect(4.0f);
        this.mPaint.setPathEffect(this.mCornerEffect);
        this.mAnimUpdateListener = listener;
        if (resources != null) {
            this.mDpi = resources.getDisplayMetrics().density / ((float) (resources.getDisplayMetrics().densityDpi / 160));
        }
        convDpi();
    }

    public void setViewSize(int width, int height) {
        this.mHeigh = height;
        this.mWidth = width;
        if (this.mBgShader == null) {
            this.mBgShader = new RadialGradient(0.0f, 0.0f, (float) BG_HEIGHT, BG_COLORS, BG_COLOR_POS, TileMode.CLAMP);
        }
    }

    private void convDpi() {
        RING_WIDTH_0 = (int) (((float) RING_WIDTH_0) * this.mDpi);
        RING_WIDTH = (int) (((float) RING_WIDTH) * this.mDpi);
        RING_RADIUS_0 = (int) (((float) RING_RADIUS_0) * this.mDpi);
        RING_RADIUS = (int) (((float) RING_RADIUS) * this.mDpi);
        SMALL_RADIUS = (int) (((float) SMALL_RADIUS) * this.mDpi);
        SMALL_WIDTH = (int) (((float) SMALL_WIDTH) * this.mDpi);
        SMALL_PADDING = (int) (((float) SMALL_PADDING) * this.mDpi);
        BG_HEIGHT = (int) (((float) BG_HEIGHT) * this.mDpi);
        SMALL_Y_0 = (int) (((float) SMALL_Y_0) * this.mDpi);
        SMALL_Y = (int) (((float) SMALL_Y) * this.mDpi);
        INNER_RADIUS_0 = (int) (((float) INNER_RADIUS_0) * this.mDpi);
        INNER_RADIUS = (int) (((float) INNER_RADIUS) * this.mDpi);
        RING_RADIUS_LUNCH = (int) (((float) RING_RADIUS_LUNCH) * this.mDpi);
        SMALL_Y_LUNCH = (int) (((float) SMALL_Y_LUNCH) * this.mDpi);
        SMALL_MULTI_LEN = (int) (((float) SMALL_MULTI_LEN) * this.mDpi);
        SMALL_BACK_LEN = (int) (((float) SMALL_BACK_LEN) * this.mDpi);
    }

    public void setXY(int cx, int cy) {
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
                this.mPvhRing0 = PropertyValuesHolder.ofInt("ringWidth", new int[]{0, 0});
                this.mPvhRing1 = PropertyValuesHolder.ofInt("ringRadius", new int[]{0, 0});
                this.mPvhRing2 = PropertyValuesHolder.ofFloat("ringAlpha", new float[]{0.0f, 0.0f});
                this.mPvhRing3 = PropertyValuesHolder.ofFloat("smallAlpha", new float[]{0.0f, 0.0f});
                this.mPvhRing4 = PropertyValuesHolder.ofInt("smallY", new int[]{0, 0});
                this.mPvhRing5 = PropertyValuesHolder.ofFloat("bgAlpha", new float[]{0.0f, 0.0f});
                this.mPvhRing6 = PropertyValuesHolder.ofInt("innerRadius", new int[]{0, 0});
                this.mPvhRing7 = PropertyValuesHolder.ofFloat("innerAlpha", new float[]{0.0f, 0.0f});
                this.mAnimRing = ObjectAnimator.ofPropertyValuesHolder(this, new PropertyValuesHolder[]{this.mPvhRing0, this.mPvhRing1, this.mPvhRing2, this.mPvhRing3, this.mPvhRing4, this.mPvhRing5, this.mPvhRing6, this.mPvhRing7});
                this.mAnimRing.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (HwCircleAnimation.this.mAnimUpdateListener != null) {
                            HwCircleAnimation.this.mAnimUpdateListener.onAnimationUpdate();
                        }
                    }
                });
            }
            this.mAnimRing.setDuration(200);
            this.mPvhRing0.setIntValues(new int[]{RING_WIDTH_0, RING_WIDTH});
            this.mPvhRing1.setIntValues(new int[]{RING_RADIUS_0, RING_RADIUS});
            this.mPvhRing2.setFloatValues(new float[]{0.0f, 0.7f});
            this.mPvhRing3.setFloatValues(new float[]{0.0f, 1.0f});
            this.mPvhRing4.setIntValues(new int[]{SMALL_Y_0, SMALL_Y});
            this.mPvhRing5.setFloatValues(new float[]{0.0f, 0.4f});
            this.mPvhRing6.setIntValues(new int[]{0, INNER_RADIUS_0});
            this.mPvhRing7.setFloatValues(new float[]{0.0f, 0.5f});
            this.mAnimRing.start();
        }
    }

    public void endRingOutAnim(boolean isLunched) {
        Log.d(TAG, "endRingOutAnim = " + isLunched + ", mIsCircleAnimating = " + this.mIsCircleAnimating);
        if (this.mIsCircleAnimating) {
            this.mIsCircleAnimating = false;
            if (!(this.mAnimRing == null || (isLunched ^ 1) == 0)) {
                this.mAnimRing.cancel();
                this.mFillProcess = 0.0f;
                this.mPvhRing0.setIntValues(new int[]{this.mRingWidth, RING_WIDTH_0});
                this.mPvhRing1.setIntValues(new int[]{this.mRingRadius, RING_RADIUS_0});
                this.mPvhRing2.setFloatValues(new float[]{this.mRingAlpha, 0.0f});
                this.mPvhRing3.setFloatValues(new float[]{this.mSmallAlpha, 0.0f});
                this.mPvhRing4.setIntValues(new int[]{this.mSmallY, SMALL_Y_0});
                this.mPvhRing5.setFloatValues(new float[]{this.mBgAlpha, 0.0f});
                this.mPvhRing6.setIntValues(new int[]{this.mInnerRadius, 0});
                this.mPvhRing7.setFloatValues(new float[]{this.mInnerAlpha, 0.0f});
                this.mAnimRing.setDuration(200);
                this.mAnimRing.start();
            }
        }
    }

    /* JADX WARNING: Missing block: B:22:0x0046, code:
            if (r3.showing == false) goto L_0x0010;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void resetAnimaion() {
        boolean update = false;
        if (this.mRingAlpha == 0.0f && this.mSmallAlpha == 0.0f) {
            if (this.mBgAlpha == 0.0f) {
                if (this.mInnerAlpha == 0.0f) {
                }
            }
        }
        update = true;
        this.mRingWidth = RING_WIDTH_0;
        this.mRingRadius = RING_RADIUS_0;
        this.mRingAlpha = 0.0f;
        this.mSmallAlpha = 0.0f;
        this.mSmallY = SMALL_Y_0;
        this.mBgAlpha = 0.0f;
        this.mInnerRadius = 0;
        this.mInnerAlpha = 0.0f;
        if (update && this.mAnimUpdateListener != null) {
            this.mAnimUpdateListener.onAnimationUpdate();
        }
    }

    public boolean setFillProcess(float p) {
        if (p <= 0.0f) {
            p = 0.0f;
        }
        if (p >= 1.0f) {
            p = 1.0f;
        }
        this.mFillProcess = p;
        if (this.mFillProcess >= 1.0f) {
            startLunchAnimation();
            return true;
        }
        if (this.mAnimUpdateListener != null) {
            this.mAnimUpdateListener.onAnimationUpdate();
        }
        return false;
    }

    private void startLunchAnimation() {
        Log.d(TAG, "startLunchAnimation");
        if (this.mAnimRing != null) {
            this.mAnimRing.cancel();
            this.mFillProcess = 1.0f;
            this.mPvhRing0.setIntValues(new int[]{RING_WIDTH, 0});
            this.mPvhRing1.setIntValues(new int[]{RING_RADIUS, RING_RADIUS_LUNCH});
            this.mPvhRing2.setFloatValues(new float[]{0.7f, 0.0f});
            this.mPvhRing3.setFloatValues(new float[]{1.0f, 0.0f});
            this.mPvhRing4.setIntValues(new int[]{SMALL_Y, SMALL_Y_LUNCH});
            this.mPvhRing5.setFloatValues(new float[]{0.4f, 0.0f});
            this.mPvhRing6.setIntValues(new int[]{INNER_RADIUS_0, 99});
            this.mPvhRing7.setFloatValues(new float[]{0.5f, 0.0f});
            this.mAnimRing.setDuration(400);
            this.mAnimRing.start();
        }
    }

    public void draw(Canvas canvas) {
        drawRing(canvas);
    }

    public synchronized void setTipShowStatus(boolean status) {
        this.showing = status;
    }

    private void drawRing(Canvas canvas) {
        if (this.mType == 4) {
            Shader s = this.mPaint.getShader();
            this.mPaint.setShader(this.mBgShader);
            this.mBgShader.setLocalMatrix(this.mMatrix);
            this.mPaint.setStyle(Style.FILL);
            this.mPaint.setAlpha((int) ((this.mBgAlpha < 0.4f ? this.mBgAlpha : this.mBgAlpha + (this.mFillProcess * 0.2f)) * 255.0f));
            canvas.drawCircle((float) this.mCx, (float) this.mCy, (float) BG_HEIGHT, this.mPaint);
            this.mPaint.setShader(s);
        }
        if (this.mRingAlpha > 0.0f) {
            this.mPaint.setStyle(Style.STROKE);
            this.mPaint.setStrokeWidth((float) this.mRingWidth);
            this.mPaint.setAlpha((int) (this.mRingAlpha * 255.0f));
            canvas.save();
            canvas.clipRect((float) ((this.mCx - SMALL_RADIUS) - SMALL_PADDING), (float) (((this.mCy - this.mSmallY) - SMALL_RADIUS) - SMALL_PADDING), (float) ((this.mCx + SMALL_RADIUS) + SMALL_PADDING), (float) (((this.mCy - this.mSmallY) + SMALL_RADIUS) + SMALL_PADDING), Op.XOR);
            canvas.drawCircle((float) this.mCx, (float) this.mCy, ((float) this.mRingRadius) + (this.mFillProcess * 36.0f), this.mPaint);
            canvas.restore();
            this.mPaint.setStrokeWidth((float) SMALL_WIDTH);
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
            canvas.drawCircle((float) this.mCx, (float) this.mCy, (float) (this.mInnerRadius + ((int) (this.mFillProcess * ((float) (INNER_RADIUS - INNER_RADIUS_0))))), this.mPaint);
        }
    }

    private void setRingWidth(int w) {
        this.mRingWidth = w;
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

    private void setSmallY(int y) {
        this.mSmallY = y;
    }

    private void setInnerRadius(int r) {
        this.mInnerRadius = r;
    }

    private void setInnerAlpha(float alpha) {
        this.mInnerAlpha = alpha;
    }

    private void drawSmall(Canvas canvas, int type, int cx, int cy, Paint paint) {
        switch (this.mType) {
            case 1:
                canvas.drawCircle((float) cx, (float) cy, (float) SMALL_RADIUS, paint);
                return;
            case 2:
                canvas.drawLine((float) (cx - (SMALL_MULTI_LEN / 2)), (float) cy, (float) ((SMALL_MULTI_LEN / 2) + cx), (float) ((((SMALL_MULTI_LEN / 2) + cy) + 2) - 2), paint);
                canvas.drawLine((float) (cx - (SMALL_MULTI_LEN / 2)), (float) cy, (float) ((SMALL_MULTI_LEN / 2) + cx), (float) (((cy - (SMALL_MULTI_LEN / 2)) - 2) - 2), paint);
                canvas.drawLine((float) ((SMALL_MULTI_LEN / 2) + cx), (float) ((((SMALL_MULTI_LEN / 2) + cy) + 2) - 2), (float) ((SMALL_MULTI_LEN / 2) + cx), (float) (((cy - (SMALL_MULTI_LEN / 2)) - 2) + 2), paint);
                return;
            case 3:
                canvas.drawRoundRect((float) (cx - (SMALL_MULTI_LEN / 2)), (float) (cy - (SMALL_MULTI_LEN / 2)), (float) ((SMALL_MULTI_LEN / 2) + cx), (float) ((SMALL_MULTI_LEN / 2) + cy), 4.0f, 4.0f, paint);
                return;
            default:
                return;
        }
    }
}
