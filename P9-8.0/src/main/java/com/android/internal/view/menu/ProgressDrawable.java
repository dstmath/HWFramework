package com.android.internal.view.menu;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

public class ProgressDrawable extends BitmapDrawable {
    private static final float BLUR_RADIUS = 10.0f;
    private static final float HALF = 0.5f;
    private static final int LIMIT_RADIUS = 10;
    private static final int MAX_PROGRESS = 100;
    private static final float MAX_RADIUS = 360.0f;
    private static final String TAG = "ProgressDrawable";
    private Paint mPaint;
    private int mProgress;
    private int mProgressRadius;
    private float mProgressStartAngle = 270.0f;
    private RectF mRectF;
    private float mSweep;

    public ProgressDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
        if (res == null) {
            Log.w(TAG, "ProgressDrawable: res is null.");
            return;
        }
        this.mPaint = new Paint();
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(res.getColor(33882453, null));
        this.mPaint.setStrokeWidth((float) res.getDimensionPixelSize(34472136));
        this.mProgressRadius = res.getDimensionPixelSize(34472137);
        this.mPaint.setMaskFilter(new BlurMaskFilter(BLUR_RADIUS, Blur.OUTER));
    }

    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        float offsetWidth = ((float) (bounds.width() - this.mProgressRadius)) * HALF;
        float offsetHeight = ((float) (bounds.height() - this.mProgressRadius)) * HALF;
        this.mRectF = new RectF(((float) bounds.left) + offsetWidth, ((float) bounds.top) + offsetHeight, ((float) bounds.right) - offsetWidth, ((float) bounds.bottom) - offsetHeight);
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawArc(this.mRectF, this.mProgressStartAngle, this.mSweep, false, this.mPaint);
    }

    public void setProgress(int progress) {
        if (this.mProgress < 0) {
            this.mProgress = 0;
        }
        this.mProgress = progress;
        this.mSweep = (((float) this.mProgress) * MAX_RADIUS) / 100.0f;
        if (this.mSweep > 0.0f && this.mSweep < BLUR_RADIUS) {
            this.mSweep = BLUR_RADIUS;
        }
        invalidateSelf();
    }

    public void setProgressRadius(int iRadius) {
        if (iRadius > 0) {
            this.mProgressRadius = iRadius;
        }
    }

    public int getProgress() {
        return this.mProgress;
    }
}
