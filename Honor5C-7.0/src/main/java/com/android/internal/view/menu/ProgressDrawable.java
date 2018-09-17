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
import androidhwext.R;

public class ProgressDrawable extends BitmapDrawable {
    private int COLOR_DRAW;
    private Paint mPaint;
    private int mProgress;
    private int mProgressRadius;
    private float mProgressStartAngle;
    private RectF mRectF;
    private float mSweep;

    public ProgressDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
        this.mProgressStartAngle = 270.0f;
        this.COLOR_DRAW = -16744961;
        this.mPaint = new Paint();
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(this.COLOR_DRAW);
        this.mPaint.setStrokeWidth((float) res.getDimensionPixelSize(R.dimen.progress_stroke_width));
        this.mProgressRadius = res.getDimensionPixelSize(R.dimen.progress_radius);
        this.mPaint.setMaskFilter(new BlurMaskFilter(10.0f, Blur.OUTER));
    }

    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        float offsetWidth = ((float) (bounds.width() - this.mProgressRadius)) * 0.5f;
        float offsetHeight = ((float) (bounds.height() - this.mProgressRadius)) * 0.5f;
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
        this.mSweep = (((float) this.mProgress) * 360.0f) / 100.0f;
        if (this.mSweep > 0.0f && this.mSweep < 10.0f) {
            this.mSweep = 10.0f;
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
