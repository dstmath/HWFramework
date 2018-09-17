package android.graphics.drawable;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.res.Resources;
import android.cover.CoverManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;

public class HwLoadingDrawableImpl extends HwLoadingDrawable implements Animatable {
    private static final float BALL_SIZE_FACTOR = 0.075f;
    private static final int DEFAULT_COLOR = -16744193;
    private static final int DURATION = 1300;
    private static final int INTERVAL = 65;
    private static final int MAX_BITMAP_SIZE = 250;
    private static final int NUM_OF_POINTS = 5;
    private static final String TAG = "HwLoadingDrawable";
    private static final int THRESHOLD = 200;
    private ObjectAnimator mAnimator;
    private Bitmap mBallBitmap;
    private BitmapDrawable mBallDrawable;
    private Ball[] mBalls;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private ColorMatrixColorFilter mFilter;
    private TimeInterpolator mFractionInterpolator;
    private int mHeight;
    private boolean mIsBitmapCreated;
    private Paint mPaint;
    private float mProgress;
    private float mRingRadius;
    private Bitmap mTmp;
    private Canvas mTmpCanvas;
    private int mWidth;

    static class Ball {
        int size;
        int x;
        int y;

        Ball(int centerX, int centerY, int size) {
            this.x = centerX;
            this.y = centerY;
            this.size = size;
        }
    }

    public HwLoadingDrawableImpl(Resources res, int size) {
        this(res, size, DEFAULT_COLOR);
    }

    public HwLoadingDrawableImpl(Resources res, int size, int color) {
        super(res, Bitmap.createBitmap(Math.min(size, MAX_BITMAP_SIZE), Math.min(size, MAX_BITMAP_SIZE), Config.ARGB_8888));
        this.mProgress = 0.0f;
        init(res, color);
    }

    private void init(Resources res, int color) {
        this.mFractionInterpolator = new PathInterpolator(0.59f, 0.19f, 0.45f, 0.88f);
        this.mWidth = getIntrinsicWidth();
        this.mHeight = getIntrinsicHeight();
        setBitmap(null);
        this.mBitmap = null;
        this.mCanvas = null;
        this.mTmp = null;
        this.mTmpCanvas = null;
        this.mIsBitmapCreated = false;
        this.mBallDrawable = (BitmapDrawable) res.getDrawable(33751548, null);
        this.mBallBitmap = this.mBallDrawable.getBitmap();
        this.mRingRadius = ((float) Math.min(this.mWidth, this.mHeight)) * 0.4f;
        this.mAnimator = ObjectAnimator.ofFloat(this, "progress", new float[]{0.0f, 1.0f});
        this.mAnimator.setDuration(1300);
        this.mAnimator.setRepeatCount(-1);
        this.mAnimator.setInterpolator(new LinearInterpolator());
        this.mBalls = new Ball[5];
        for (int i = 0; i < 5; i++) {
            this.mBalls[i] = new Ball(0, 0, (int) (((float) (((5 - i) + 1) * this.mWidth)) * BALL_SIZE_FACTOR));
        }
        this.mPaint = new Paint(1);
        this.mFilter = new ColorMatrixColorFilter(createThresholdMatrix(200, color));
        setProgress(0.0f);
    }

    public boolean setVisible(boolean visible, boolean restart) {
        if (!visible && this.mIsBitmapCreated) {
            setBitmap(null);
            this.mBitmap = null;
            this.mCanvas = null;
            this.mTmp = null;
            this.mTmpCanvas = null;
            this.mIsBitmapCreated = false;
        }
        return super.setVisible(visible, restart);
    }

    private void updatePointsPosition(float progress) {
        float percent = progress;
        float step = 0.05f * (3.0f * (0.5f - Math.abs(0.5f - progress)));
        int centerX = this.mWidth / 2;
        int centerY = this.mHeight / 2;
        for (int i = 0; i < 5; i++) {
            if (percent < 0.0f) {
                percent += 1.0f;
            }
            float fraction = this.mFractionInterpolator.getInterpolation(percent);
            this.mBalls[i].x = ((int) getPositionXByPercent(fraction)) + centerX;
            this.mBalls[i].y = ((int) getPositionYByPercent(fraction)) + centerY;
            percent -= step;
        }
    }

    public void draw(Canvas canvas) {
        if (!this.mIsBitmapCreated && isVisible()) {
            setBitmap(Bitmap.createBitmap(this.mWidth, this.mHeight, Config.ARGB_8888));
            this.mBitmap = getBitmap();
            this.mCanvas = new Canvas(this.mBitmap);
            this.mTmp = Bitmap.createBitmap(this.mWidth, this.mHeight, Config.ARGB_8888);
            this.mTmpCanvas = new Canvas(this.mTmp);
            this.mIsBitmapCreated = true;
            this.mProgress = 0.0f;
        }
        updatePointsPosition(this.mProgress);
        updateBitmap();
        super.draw(canvas);
    }

    private void updateBitmap() {
        if (this.mIsBitmapCreated) {
            int i;
            this.mTmpCanvas.drawColor(CoverManager.DEFAULT_COLOR);
            this.mPaint.setColorFilter(null);
            this.mTmpCanvas.drawBitmap(this.mBallBitmap, null, new Rect(this.mBalls[0].x - (this.mBalls[0].size / 2), this.mBalls[0].y - (this.mBalls[0].size / 2), this.mBalls[0].x + (this.mBalls[0].size / 2), this.mBalls[0].y + (this.mBalls[0].size / 2)), this.mPaint);
            this.mPaint.setXfermode(new PorterDuffXfermode(Mode.ADD));
            for (i = 1; i < 5; i++) {
                this.mTmpCanvas.drawBitmap(this.mBallBitmap, null, new Rect(this.mBalls[i].x - (this.mBalls[i].size / 2), this.mBalls[i].y - (this.mBalls[i].size / 2), this.mBalls[i].x + (this.mBalls[i].size / 2), this.mBalls[i].y + (this.mBalls[i].size / 2)), this.mPaint);
            }
            this.mCanvas.drawColor(0, Mode.CLEAR);
            this.mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
            this.mPaint.setColorFilter(this.mFilter);
            for (i = 0; i < 5; i++) {
                this.mCanvas.drawBitmap(this.mTmp, new Rect(this.mBalls[i].x - (this.mBalls[i].size / 2), this.mBalls[i].y - (this.mBalls[i].size / 2), this.mBalls[i].x + (this.mBalls[i].size / 2), this.mBalls[i].y + (this.mBalls[i].size / 2)), new Rect(this.mBalls[i].x - (this.mBalls[i].size / 2), this.mBalls[i].y - (this.mBalls[i].size / 2), this.mBalls[i].x + (this.mBalls[i].size / 2), this.mBalls[i].y + (this.mBalls[i].size / 2)), this.mPaint);
            }
        }
    }

    private void setProgress(float progress) {
        this.mProgress = progress;
        invalidateSelf();
    }

    public void start() {
        if (this.mAnimator != null && (this.mAnimator.isRunning() ^ 1) != 0) {
            this.mAnimator.start();
        }
    }

    public void stop() {
        if (this.mAnimator != null && this.mAnimator.isRunning()) {
            this.mAnimator.end();
        }
    }

    public boolean isRunning() {
        return this.mAnimator != null ? this.mAnimator.isRunning() : false;
    }

    private double convertToRadian(float p) {
        return Math.toRadians((double) ((360.0f * p) - 105.0f));
    }

    private float getPositionXByPercent(float percent) {
        return ((float) Math.cos(convertToRadian(percent))) * this.mRingRadius;
    }

    private float getPositionYByPercent(float percent) {
        return ((float) Math.sin(convertToRadian(percent))) * this.mRingRadius;
    }

    public static ColorMatrix createThresholdMatrix(int threshold, int color) {
        float gradient;
        if (threshold == 255 || threshold <= 0) {
            gradient = 255.0f;
        } else {
            gradient = 255.0f / ((float) (255 - threshold));
        }
        return new ColorMatrix(new float[]{0.0f, 0.0f, 0.0f, 0.0f, (float) Color.red(color), 0.0f, 0.0f, 0.0f, 0.0f, (float) Color.green(color), 0.0f, 0.0f, 0.0f, 0.0f, (float) Color.blue(color), gradient, 0.0f, 0.0f, 0.0f, (-gradient) * ((float) threshold)});
    }

    public void setColor(int color) {
        this.mFilter = new ColorMatrixColorFilter(createThresholdMatrix(200, color));
    }
}
