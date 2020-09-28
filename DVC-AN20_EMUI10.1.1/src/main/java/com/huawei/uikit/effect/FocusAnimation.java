package com.huawei.uikit.effect;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import java.lang.ref.WeakReference;

public class FocusAnimation {
    private static final long ANIMATION_INTERVAL = 66;
    private static final int BLUE_MASK = 255;
    private static final int BLUE_MASK_RADIUS = 5;
    private static final float[] BLUR_ARRAY = {0.0f, LEFTGLOWEDGE_START_PERCENT, MIDGLOWEDGE_START_PERCENT, RIGHTGLOWEDGE_START_PERCENT, 0.75f, 1.0f};
    private static final float[] BLUR_ARRAY_DOUBLE = {0.0f, LEFTGLOWEDGE_START_PERCENT_FORLARGEASPECT, MIDGLOWEDGE_START_PERCENT_FORLARGEASPECT, RIGHTGLOWEDGE_START_PERCENT_FORLARGEASPECT, RIGHTEDGE_START_PERCENT_FORLARGEASPECT, 1.0f};
    private static final long BOUNDARY_ALPHACHANGE_CYCLE = 3000;
    private static final int BOUNDARY_END_ALPHA = 150;
    private static final int BOUNDARY_START_ALPHA = 25;
    private static final int BOUNDARY_WIDTH = 1;
    private static final float DOUBLE = 2.0f;
    private static final float DP_PIXEL_VALUE = 0.5f;
    private static final int FADE_IN_BEGIN = 0;
    private static final int FADE_IN_END = 255;
    private static final long FIRST_FADE_IN_TIME = 1000;
    private static final float GLOWEDGE_END_ANGLE = 360.0f;
    private static final float GLOWEDGE_START_ANGLE = 0.0f;
    private static final long GLOW_ROTATE_CYCLE = 6000;
    private static final int GREEN_MASK = 65280;
    private static final int GREEN_SHIFTLEFT_BITS = 8;
    private static final float LEFTEDGE_START_PERCENT = 0.0f;
    private static final float LEFTGLOWEDGE_START_PERCENT = 0.25f;
    private static final float LEFTGLOWEDGE_START_PERCENT_FORLARGEASPECT = 0.33f;
    private static final int MAX_ALPHA = 255;
    private static final float MIDGLOWEDGE_START_PERCENT = 0.41f;
    private static final float MIDGLOWEDGE_START_PERCENT_FORLARGEASPECT = 0.44f;
    private static final int RED_MASK = 16711680;
    private static final int RED_SHIFTLEFT_BITS = 16;
    private static final float RIGHTEDGE_END_PERCENT = 1.0f;
    private static final float RIGHTEDGE_START_PERCENT = 0.75f;
    private static final float RIGHTEDGE_START_PERCENT_FORLARGEASPECT = 0.67f;
    private static final float RIGHTGLOWEDGE_START_PERCENT = 0.58f;
    private static final float RIGHTGLOWEDGE_START_PERCENT_FORLARGEASPECT = 0.56f;
    private static final long START_DELAY = 100;
    private static final String TAG = "FocusAnimation:";
    private int mAlpha;
    private ValueAnimator mAlphaAnimator;
    private int mAlphaFadeIn;
    private float mAnimatedValue;
    private WeakReference<Context> mContext;
    private ValueAnimator mDegreeAnimator;
    private ValueAnimator mFadeInAnimator;
    private long mLastDrawBoundaryTime;
    private long mLastDrawGlowEdgeTime;
    private long mLastFadeInTime;
    private BlurMaskFilter mMaskFilter;
    private int mPadding;
    private Paint mPaint;
    private float mScale = 0.0f;
    private int mScrollX = 0;
    private int mScrollY = 0;
    private PorterDuffXfermode mSrcInXferMode;
    private WeakReference<View> mView;

    public FocusAnimation(Context context, View view) {
        this.mContext = new WeakReference<>(context);
        this.mView = new WeakReference<>(view);
        drawEnvironmentInit();
    }

    public void startAnimation() {
        if (!this.mDegreeAnimator.isStarted()) {
            this.mAlphaFadeIn = 0;
            this.mAlpha = 0;
            this.mDegreeAnimator.start();
            this.mFadeInAnimator.start();
        }
    }

    public void stopAnimation() {
        if (this.mDegreeAnimator.isStarted()) {
            this.mDegreeAnimator.cancel();
            this.mFadeInAnimator.cancel();
            this.mAlphaAnimator.cancel();
            this.mAlphaFadeIn = 0;
            this.mAlpha = 0;
            if (this.mView.get() != null) {
                this.mView.get().invalidate();
            }
        }
    }

    public void drawFocusAnimation(Canvas canvas, Path path, Rect pathRect, int pathColor) {
        float[] blurArray;
        if (isValid(canvas, path, pathRect)) {
            this.mScrollX = this.mView.get().getScrollX();
            this.mScrollY = this.mView.get().getScrollY();
            canvas.translate((float) this.mScrollX, (float) this.mScrollY);
            int sc = canvas.saveLayerAlpha((float) (pathRect.left - this.mPadding), (float) (pathRect.top - this.mPadding), (float) (pathRect.right + this.mPadding), (float) (pathRect.bottom + this.mPadding), 255);
            int maxHeight = (int) Math.sqrt(Math.pow((double) (((float) pathRect.width()) + (((float) this.mPadding) * DOUBLE)), 2.0d) + Math.pow((double) (((float) pathRect.height()) + (((float) this.mPadding) * DOUBLE)), 2.0d));
            this.mPaint.setStyle(Paint.Style.STROKE);
            this.mPaint.setColor(pathColor);
            this.mPaint.setShader(null);
            canvas.drawPath(path, this.mPaint);
            this.mPaint.setXfermode(this.mSrcInXferMode);
            canvas.translate((((float) pathRect.width()) / DOUBLE) + ((float) pathRect.left), (((float) pathRect.height()) / DOUBLE) + ((float) pathRect.top));
            canvas.rotate(this.mAnimatedValue);
            this.mPaint.setStyle(Paint.Style.FILL);
            int red = (RED_MASK & pathColor) >> 16;
            int green = (GREEN_MASK & pathColor) >> 8;
            int blue = pathColor & 255;
            int boundaryColor = Color.argb(this.mAlpha, red, green, blue);
            int glowColor = Color.argb(this.mAlphaFadeIn, red, green, blue);
            if (((float) pathRect.width()) / ((float) pathRect.height()) > DOUBLE) {
                blurArray = BLUR_ARRAY_DOUBLE;
            } else {
                blurArray = BLUR_ARRAY;
            }
            this.mPaint.setShader(new LinearGradient((float) (-((int) (((float) pathRect.width()) / DOUBLE))), 0.0f, (float) ((int) (((float) pathRect.width()) / DOUBLE)), 0.0f, new int[]{boundaryColor, boundaryColor, glowColor, glowColor, boundaryColor, boundaryColor}, blurArray, Shader.TileMode.CLAMP));
            float f = ((float) maxHeight) / DOUBLE;
            int i = this.mPadding;
            canvas.drawRect((float) (-((int) (f + ((float) i)))), (float) (-((int) ((((float) maxHeight) / DOUBLE) + ((float) i)))), (float) ((int) ((((float) maxHeight) / DOUBLE) + ((float) i))), (float) ((int) ((((float) maxHeight) / DOUBLE) + ((float) i))), this.mPaint);
            this.mPaint.setXfermode(null);
            canvas.restoreToCount(sc);
            canvas.translate((float) (-this.mScrollX), (float) (-this.mScrollY));
        }
    }

    private int dp2pixel(int dpValue) {
        return (int) ((((float) dpValue) * this.mScale) + 0.5f);
    }

    private void drawEnvironmentInit() {
        this.mSrcInXferMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        if (this.mContext.get() != null) {
            this.mScale = this.mContext.get().getResources().getDisplayMetrics().density;
        }
        this.mPaint = new Paint();
        this.mPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mPaint.setStrokeWidth((float) dp2pixel(1));
        this.mPaint.setAntiAlias(true);
        this.mPadding = dp2pixel(10);
        this.mMaskFilter = new BlurMaskFilter((float) dp2pixel(5), BlurMaskFilter.Blur.SOLID);
        this.mPaint.setMaskFilter(this.mMaskFilter);
        this.mLastDrawBoundaryTime = 0;
        this.mLastDrawGlowEdgeTime = 0;
        this.mDegreeAnimator = ValueAnimator.ofFloat(0.0f, GLOWEDGE_END_ANGLE);
        this.mDegreeAnimator.setStartDelay(START_DELAY);
        this.mDegreeAnimator.setDuration(GLOW_ROTATE_CYCLE);
        this.mDegreeAnimator.setRepeatCount(-1);
        this.mDegreeAnimator.setInterpolator(new LinearInterpolator());
        this.mDegreeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.huawei.uikit.effect.FocusAnimation.AnonymousClass1 */

            public void onAnimationUpdate(ValueAnimator animation) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - FocusAnimation.this.mLastDrawGlowEdgeTime > FocusAnimation.ANIMATION_INTERVAL) {
                    FocusAnimation.this.mAnimatedValue = ((Float) animation.getAnimatedValue()).floatValue();
                    FocusAnimation.this.mLastDrawGlowEdgeTime = currentTime;
                    if (FocusAnimation.this.mView.get() != null) {
                        ((View) FocusAnimation.this.mView.get()).invalidate();
                    } else {
                        animation.cancel();
                    }
                }
            }
        });
        createAlphaAnimator();
        createFadeInAnimator();
    }

    private void createAlphaAnimator() {
        this.mAlphaAnimator = ValueAnimator.ofInt(BOUNDARY_START_ALPHA, BOUNDARY_END_ALPHA);
        this.mAlphaAnimator.setDuration(BOUNDARY_ALPHACHANGE_CYCLE);
        this.mAlphaAnimator.setRepeatCount(-1);
        this.mAlphaAnimator.setRepeatMode(2);
        this.mAlphaAnimator.setInterpolator(new LinearInterpolator());
        this.mAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.huawei.uikit.effect.FocusAnimation.AnonymousClass2 */

            public void onAnimationUpdate(ValueAnimator animation) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - FocusAnimation.this.mLastDrawBoundaryTime > FocusAnimation.ANIMATION_INTERVAL) {
                    FocusAnimation.this.mAlpha = ((Integer) animation.getAnimatedValue()).intValue();
                    FocusAnimation.this.mLastDrawBoundaryTime = currentTime;
                }
            }
        });
    }

    private void createFadeInAnimator() {
        this.mFadeInAnimator = ValueAnimator.ofInt(0, 255);
        this.mFadeInAnimator.setStartDelay(START_DELAY);
        this.mFadeInAnimator.setDuration(FIRST_FADE_IN_TIME);
        this.mFadeInAnimator.setInterpolator(new AccelerateInterpolator());
        this.mFadeInAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.huawei.uikit.effect.FocusAnimation.AnonymousClass3 */

            public void onAnimationUpdate(ValueAnimator animation) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - FocusAnimation.this.mLastFadeInTime > FocusAnimation.ANIMATION_INTERVAL) {
                    FocusAnimation.this.mAlphaFadeIn = ((Integer) animation.getAnimatedValue()).intValue();
                    if (FocusAnimation.this.mAlphaFadeIn <= FocusAnimation.BOUNDARY_START_ALPHA) {
                        FocusAnimation focusAnimation = FocusAnimation.this;
                        focusAnimation.mAlpha = focusAnimation.mAlphaFadeIn;
                    } else if (!FocusAnimation.this.mAlphaAnimator.isStarted()) {
                        FocusAnimation.this.mAlphaAnimator.start();
                    }
                    FocusAnimation.this.mLastFadeInTime = currentTime;
                }
            }
        });
    }

    private boolean isValid(Canvas canvas, Path path, Rect pathRect) {
        if (canvas == null || this.mView.get() == null) {
            Log.w(TAG, "The canvas or view is null");
            return false;
        } else if (path != null && pathRect != null) {
            return true;
        } else {
            Log.w(TAG, "The path or pathRect is null");
            return false;
        }
    }

    public Drawable createDrawable(Path path, Rect pathRect, int pathColor) {
        return new FocusAnimationDrawable(path, pathRect, pathColor);
    }

    private class FocusAnimationDrawable extends Drawable {
        private Path mPath;
        private int mPathColor;
        private Rect mRect;

        FocusAnimationDrawable(Path path, Rect rect, int pathColor) {
            this.mPath = path;
            this.mRect = rect;
            this.mPathColor = pathColor;
        }

        public void draw(Canvas canvas) {
            if (FocusAnimation.this.mDegreeAnimator.isRunning() && FocusAnimation.this.mAlphaAnimator.isRunning()) {
                FocusAnimation.this.drawFocusAnimation(canvas, this.mPath, this.mRect, this.mPathColor);
            }
        }

        public void setAlpha(int alpha) {
        }

        /* access modifiers changed from: protected */
        public void onBoundsChange(Rect bounds) {
            if (bounds != null) {
                super.onBoundsChange(bounds);
            }
        }

        public void setColorFilter(ColorFilter colorFilter) {
        }

        public int getOpacity() {
            return -3;
        }
    }
}
