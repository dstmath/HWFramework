package android.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import com.android.internal.R;

/* access modifiers changed from: package-private */
public class RoundScrollbarRenderer {
    private static final int DEFAULT_THUMB_COLOR = -1512723;
    private static final int DEFAULT_TRACK_COLOR = 1291845631;
    private static final int MAX_SCROLLBAR_ANGLE_SWIPE = 16;
    private static final int MIN_SCROLLBAR_ANGLE_SWIPE = 6;
    private static final int SCROLLBAR_ANGLE_RANGE = 90;
    private static final float WIDTH_PERCENTAGE = 0.02f;
    private final int mMaskThickness;
    private final View mParent;
    private final RectF mRect = new RectF();
    private final Paint mThumbPaint = new Paint();
    private final Paint mTrackPaint = new Paint();

    public RoundScrollbarRenderer(View parent) {
        this.mThumbPaint.setAntiAlias(true);
        this.mThumbPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mThumbPaint.setStyle(Paint.Style.STROKE);
        this.mTrackPaint.setAntiAlias(true);
        this.mTrackPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mTrackPaint.setStyle(Paint.Style.STROKE);
        this.mParent = parent;
        this.mMaskThickness = parent.getContext().getResources().getDimensionPixelSize(R.dimen.circular_display_mask_thickness);
    }

    public void drawRoundScrollbars(Canvas canvas, float alpha, Rect bounds) {
        if (alpha != 0.0f) {
            float maxScroll = (float) this.mParent.computeVerticalScrollRange();
            float scrollExtent = (float) this.mParent.computeVerticalScrollExtent();
            if (scrollExtent > 0.0f && maxScroll > scrollExtent) {
                float linearThumbLength = (float) this.mParent.computeVerticalScrollExtent();
                float thumbWidth = ((float) this.mParent.getWidth()) * WIDTH_PERCENTAGE;
                this.mThumbPaint.setStrokeWidth(thumbWidth);
                this.mTrackPaint.setStrokeWidth(thumbWidth);
                setThumbColor(applyAlpha(DEFAULT_THUMB_COLOR, alpha));
                setTrackColor(applyAlpha(DEFAULT_TRACK_COLOR, alpha));
                float sweepAngle = clamp((linearThumbLength / maxScroll) * 90.0f, 6.0f, 16.0f);
                float startAngle = clamp((((90.0f - sweepAngle) * ((float) Math.max(0, this.mParent.computeVerticalScrollOffset()))) / (maxScroll - linearThumbLength)) - 45.0f, -45.0f, 45.0f - sweepAngle);
                float inset = (thumbWidth / 2.0f) + ((float) this.mMaskThickness);
                this.mRect.set(((float) bounds.left) + inset, ((float) bounds.top) + inset, ((float) bounds.right) - inset, ((float) bounds.bottom) - inset);
                canvas.drawArc(this.mRect, -45.0f, 90.0f, false, this.mTrackPaint);
                canvas.drawArc(this.mRect, startAngle, sweepAngle, false, this.mThumbPaint);
            }
        }
    }

    private static float clamp(float val, float min, float max) {
        if (val < min) {
            return min;
        }
        if (val > max) {
            return max;
        }
        return val;
    }

    private static int applyAlpha(int color, float alpha) {
        return Color.argb((int) (((float) Color.alpha(color)) * alpha), Color.red(color), Color.green(color), Color.blue(color));
    }

    private void setThumbColor(int thumbColor) {
        if (this.mThumbPaint.getColor() != thumbColor) {
            this.mThumbPaint.setColor(thumbColor);
        }
    }

    private void setTrackColor(int trackColor) {
        if (this.mTrackPaint.getColor() != trackColor) {
            this.mTrackPaint.setColor(trackColor);
        }
    }
}
