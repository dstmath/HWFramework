package android.graphics.drawable;

import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;

public class HwLoadingDrawableImpl extends HwLoadingDrawable implements Animatable {
    private static final int ALPHA_BASIC = 127;
    private static final int ALPHA_FULL = 255;
    private static final int ALPHA_INIT = 128;
    private static final int ANGLE = 30;
    private static final int ANGLE_TIMES = 12;
    private static final float CANVAS_WHOLE_CENTRE_RATIO = 2.0f;
    private static final float CANVAS_WHOLE_RADIUS_RATIO = 0.6944444f;
    private static final int DEFAULT_COLOR = -10066330;
    private static final int DURATION = 1000;
    private static final double GRADIENT_ANGLE = Math.toRadians(45.0d);
    private static final float INIT_NUM = 0.0f;
    private static final float INTERPOLATOR_PARAM_1 = 0.33f;
    private static final float INTERPOLATOR_PARAM_2 = 0.0f;
    private static final float INTERPOLATOR_PARAM_3 = 0.67f;
    private static final float INTERPOLATOR_PARAM_4 = 1.0f;
    private static final int MAX_BITMAP_SIZE = 250;
    private static final int MAX_DELTA_RADIUS = 1;
    private static final int MIN_DELTA_RADIUS = 0;
    private static final float RESET_FRAME = 0.0f;
    private static final String TAG = "HwLoadingDrawable";
    private static final int TIME_DIFFERENCE = 5;
    private static final float TIME_STAMP = 60.0f;
    private static final float TIME_STAMP_1 = 0.0f;
    private static final float TIME_STAMP_2 = 10.0f;
    private static final float TIME_STAMP_3 = 33.076923f;
    private static final float TIME_STAMP_4 = 60.0f;
    private static final float TIME_STAMP_TMP = 23.076923f;
    private static final int TOTAL_ANGLE = 360;
    private static final int TOTAL_FRAMES_PER_PERIOD = 60;
    private static final float WHOLE_CENTRE_X_INIT = 0.0f;
    private static final float WHOLE_CENTRE_Y_INIT = 0.0f;
    private static final float WHOLE_DETAIL_CENTRE_RATIO = 2.0f;
    private static final float WHOLE_DETAIL_RADIUS_RATIO = 0.1f;
    private static final float WHOLE_RADIUS_INIT = 0.0f;
    private ObjectAnimator mAnimator;
    private int[] mColor;
    private float mDetailRadius;
    private Paint mPaint;
    private PathInterpolator mPathInterpolator;
    private float mProgress;
    private float mWholeCenterX;
    private float mWholeCenterY;
    private float mWholeRadius;

    public HwLoadingDrawableImpl(Resources res, int size) {
        this(res, size, DEFAULT_COLOR);
    }

    public HwLoadingDrawableImpl(Resources res, int size, int... color) {
        super(res, Bitmap.createBitmap(min(size, MAX_BITMAP_SIZE), min(size, MAX_BITMAP_SIZE), Bitmap.Config.ARGB_8888));
        this.mProgress = 0.0f;
        this.mWholeRadius = 0.0f;
        this.mPathInterpolator = new PathInterpolator(INTERPOLATOR_PARAM_1, 0.0f, INTERPOLATOR_PARAM_3, 1.0f);
        init(color);
    }

    private static int min(int a, int b) {
        return a <= b ? a : b;
    }

    private void init(int... color) {
        this.mAnimator = ObjectAnimator.ofFloat(this, "progress", new float[]{0.0f, 1.0f});
        this.mAnimator.setDuration(1000);
        this.mAnimator.setRepeatCount(-1);
        this.mAnimator.setInterpolator(new LinearInterpolator());
        this.mPaint = new Paint(1);
        this.mWholeCenterX = 0.0f;
        this.mWholeCenterY = 0.0f;
        setColor(color);
        setProgress(0.0f);
    }

    private void setVariousRadius() {
        this.mWholeRadius = getHalfCanvasWidthHeightMin() * CANVAS_WHOLE_RADIUS_RATIO;
        this.mDetailRadius = this.mWholeRadius * WHOLE_DETAIL_RADIUS_RATIO;
    }

    private float getHalfCanvasWidthHeightMin() {
        Rect rect = getBounds();
        this.mWholeCenterX = ((float) (rect.left + rect.right)) / 2.0f;
        this.mWholeCenterY = ((float) (rect.top + rect.bottom)) / 2.0f;
        return this.mWholeCenterX < this.mWholeCenterY ? this.mWholeCenterX : this.mWholeCenterY;
    }

    /* access modifiers changed from: protected */
    public void onBoundsChange(Rect bounds) {
        HwLoadingDrawableImpl.super.onBoundsChange(bounds);
        setVariousRadius();
        setColor(this.mColor);
    }

    public void draw(Canvas canvas) {
        if (this.mProgress * 60.0f >= 60.0f) {
            this.mProgress = 0.0f;
        }
        canvas.save();
        for (int i = 0; i < 12; i++) {
            float mFrameTmp = (this.mProgress * 60.0f) + ((float) (i * 5));
            this.mPaint.setAlpha(127 + ((int) calculateDeltaRadiusOrAlpha(mFrameTmp, false)));
            double angle = Math.toRadians((double) (-30 * i));
            canvas.drawCircle(this.mWholeCenterX + ((float) (((double) this.mWholeRadius) * Math.cos(angle))), this.mWholeCenterY + ((float) (((double) this.mWholeRadius) * Math.sin(angle))), (float) (((double) this.mDetailRadius) * (1.0d + calculateDeltaRadiusOrAlpha(mFrameTmp, true))), this.mPaint);
        }
        canvas.restore();
    }

    private double calculateDeltaRadiusOrAlpha(float frame, boolean type) {
        float frame2 = frame % 60.0f;
        double deltaRadius = GRADIENT_ANGLE;
        if (frame2 >= 0.0f && frame2 < TIME_STAMP_2) {
            deltaRadius = (double) this.mPathInterpolator.getInterpolation(WHOLE_DETAIL_RADIUS_RATIO * frame2);
        } else if (frame2 >= TIME_STAMP_2 && frame2 < TIME_STAMP_3) {
            deltaRadius = (double) this.mPathInterpolator.getInterpolation((-0.043333333f * frame2) + 1.4333334f);
        } else if (frame2 >= TIME_STAMP_3 && frame2 < 60.0f) {
            deltaRadius = GRADIENT_ANGLE;
        }
        if (type) {
            return deltaRadius;
        }
        return (double) ((int) (((double) 128) * deltaRadius));
    }

    private void setProgress(float progress) {
        this.mProgress = progress;
        invalidateSelf();
    }

    public void start() {
        if (this.mAnimator != null && !this.mAnimator.isRunning()) {
            this.mAnimator.start();
        }
    }

    public void stop() {
        if (this.mAnimator != null && this.mAnimator.isRunning()) {
            this.mAnimator.end();
        }
    }

    public boolean isRunning() {
        return this.mAnimator != null && this.mAnimator.isRunning();
    }

    public void setColor(int... color) {
        this.mColor = color;
        if (this.mColor == null || this.mColor.length < 1) {
            this.mPaint.setColor(DEFAULT_COLOR);
        } else if (this.mColor.length <= 1 || this.mWholeRadius <= 1.0f) {
            this.mPaint.setColor(this.mColor[0]);
        } else {
            float cosValue = (float) Math.cos(GRADIENT_ANGLE);
            float sinValue = (float) Math.sin(GRADIENT_ANGLE);
            LinearGradient linearGradient = new LinearGradient((1.0f - cosValue) * this.mWholeRadius, (1.0f + sinValue) * this.mWholeRadius, (1.0f + cosValue) * this.mWholeRadius, this.mWholeRadius * (1.0f - sinValue), this.mColor, null, Shader.TileMode.CLAMP);
            this.mPaint.setShader(linearGradient);
        }
    }
}
