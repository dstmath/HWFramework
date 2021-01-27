package android.graphics.drawable;

import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
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
    private int mAlpha;
    private ObjectAnimator mAnimator;
    private int mColor;
    private double mDeltaRadius;
    private float mDetailCenterX;
    private float mDetailCenterY;
    private float mDetailRadius;
    private float mFrame;
    private float mFrameTmp;
    private Paint mPaint;
    private PathInterpolator mPathInterpolator;
    private float mProgress;
    private float mWholeCenterX;
    private float mWholeCenterY;
    private float mWholeRadius;

    public HwLoadingDrawableImpl(Resources res, int size) {
        this(res, size, DEFAULT_COLOR);
    }

    public HwLoadingDrawableImpl(Resources res, int size, int color) {
        super(res, Bitmap.createBitmap(Math.min(size, (int) MAX_BITMAP_SIZE), Math.min(size, (int) MAX_BITMAP_SIZE), Bitmap.Config.ARGB_8888));
        this.mProgress = 0.0f;
        this.mWholeRadius = 0.0f;
        this.mFrame = 0.0f;
        this.mFrameTmp = this.mFrame;
        this.mPathInterpolator = new PathInterpolator(INTERPOLATOR_PARAM_1, 0.0f, INTERPOLATOR_PARAM_3, 1.0f);
        init(color);
    }

    private void init(int color) {
        this.mAnimator = ObjectAnimator.ofFloat(this, "progress", 0.0f, 1.0f);
        this.mAnimator.setDuration(1000L);
        this.mAnimator.setRepeatCount(-1);
        this.mAnimator.setInterpolator(new LinearInterpolator());
        this.mPaint = new Paint();
        this.mWholeCenterX = 0.0f;
        this.mWholeCenterY = 0.0f;
        this.mColor = color;
        this.mPaint.setColor(this.mColor);
        this.mPaint.setAntiAlias(true);
        setProgress(0.0f);
    }

    private void setVariousRadius(Canvas canvas) {
        this.mWholeRadius = getHalfCanvasWidthHeightMin(canvas) * CANVAS_WHOLE_RADIUS_RATIO;
        float f = this.mWholeRadius;
        this.mDetailRadius = WHOLE_DETAIL_RADIUS_RATIO * f;
        this.mDetailCenterX = this.mWholeCenterX;
        this.mDetailCenterY = this.mWholeCenterY - f;
    }

    private float getHalfCanvasWidthHeightMin(Canvas canvas) {
        this.mWholeCenterX = ((float) canvas.getWidth()) / 2.0f;
        this.mWholeCenterY = ((float) canvas.getHeight()) / 2.0f;
        float f = this.mWholeCenterX;
        float f2 = this.mWholeCenterY;
        return f < f2 ? f : f2;
    }

    public void draw(Canvas canvas) {
        if (canvas == null) {
            Log.e(TAG, " draw canvasa is null");
            return;
        }
        this.mPaint.setColor(this.mColor);
        setVariousRadius(canvas);
        if (this.mProgress * 60.0f >= 60.0f) {
            this.mProgress = 0.0f;
        }
        canvas.save();
        for (int i = 0; i < 12; i++) {
            this.mFrameTmp = (this.mProgress * 60.0f) + ((float) (i * 5));
            this.mPaint.setAlpha(((int) calculateDeltaRadiusOrAlpha(this.mFrameTmp, false)) + 127);
            canvas.drawCircle(this.mDetailCenterX, this.mDetailCenterY, this.mDetailRadius + (((float) calculateDeltaRadiusOrAlpha(this.mFrameTmp, true)) * this.mDetailRadius), this.mPaint);
            canvas.rotate(-30.0f, this.mWholeCenterX, this.mWholeCenterY);
        }
        canvas.restore();
    }

    private double calculateDeltaRadiusOrAlpha(float tmpframe, boolean isType) {
        float frame = tmpframe % 60.0f;
        this.mDeltaRadius = 0.0d;
        this.mAlpha = 128;
        if (frame >= 0.0f && frame < TIME_STAMP_2) {
            this.mDeltaRadius = (double) this.mPathInterpolator.getInterpolation(WHOLE_DETAIL_RADIUS_RATIO * frame);
        } else if (frame >= TIME_STAMP_2 && frame < TIME_STAMP_3) {
            this.mDeltaRadius = (double) this.mPathInterpolator.getInterpolation((-0.043333333f * frame) + 1.4333334f);
        } else if (frame < TIME_STAMP_3 || frame >= 60.0f) {
            Log.e(TAG, "Wrong frame: " + frame);
        } else {
            this.mDeltaRadius = 0.0d;
        }
        if (isType) {
            return this.mDeltaRadius;
        }
        this.mAlpha = (int) (this.mDeltaRadius * ((double) this.mAlpha));
        return (double) this.mAlpha;
    }

    private void setProgress(float progress) {
        this.mProgress = progress;
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Animatable
    public void start() {
        ObjectAnimator objectAnimator = this.mAnimator;
        if (objectAnimator != null && !objectAnimator.isRunning()) {
            this.mAnimator.start();
        }
    }

    @Override // android.graphics.drawable.Animatable
    public void stop() {
        ObjectAnimator objectAnimator = this.mAnimator;
        if (objectAnimator != null && objectAnimator.isRunning()) {
            this.mAnimator.end();
        }
    }

    @Override // android.graphics.drawable.Animatable
    public boolean isRunning() {
        ObjectAnimator objectAnimator = this.mAnimator;
        return objectAnimator != null && objectAnimator.isRunning();
    }

    public void setColor(int color) {
        this.mColor = color;
    }
}
