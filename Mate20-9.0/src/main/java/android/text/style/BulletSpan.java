package android.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.text.Layout;
import android.text.ParcelableSpan;
import android.text.Spanned;

public class BulletSpan implements LeadingMarginSpan, ParcelableSpan {
    private static final int STANDARD_BULLET_RADIUS = 4;
    private static final int STANDARD_COLOR = 0;
    public static final int STANDARD_GAP_WIDTH = 2;
    private Path mBulletPath;
    private final int mBulletRadius;
    private final int mColor;
    private final int mGapWidth;
    private final boolean mWantColor;

    public BulletSpan() {
        this(2, 0, false, 4);
    }

    public BulletSpan(int gapWidth) {
        this(gapWidth, 0, false, 4);
    }

    public BulletSpan(int gapWidth, int color) {
        this(gapWidth, color, true, 4);
    }

    public BulletSpan(int gapWidth, int color, int bulletRadius) {
        this(gapWidth, color, true, bulletRadius);
    }

    private BulletSpan(int gapWidth, int color, boolean wantColor, int bulletRadius) {
        this.mBulletPath = null;
        this.mGapWidth = gapWidth;
        this.mBulletRadius = bulletRadius;
        this.mColor = color;
        this.mWantColor = wantColor;
    }

    public BulletSpan(Parcel src) {
        this.mBulletPath = null;
        this.mGapWidth = src.readInt();
        this.mWantColor = src.readInt() != 0;
        this.mColor = src.readInt();
        this.mBulletRadius = src.readInt();
    }

    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    public int getSpanTypeIdInternal() {
        return 8;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    public void writeToParcelInternal(Parcel dest, int flags) {
        dest.writeInt(this.mGapWidth);
        dest.writeInt(this.mWantColor ? 1 : 0);
        dest.writeInt(this.mColor);
        dest.writeInt(this.mBulletRadius);
    }

    public int getLeadingMargin(boolean first) {
        return (2 * this.mBulletRadius) + this.mGapWidth;
    }

    public int getGapWidth() {
        return this.mGapWidth;
    }

    public int getBulletRadius() {
        return this.mBulletRadius;
    }

    public int getColor() {
        return this.mColor;
    }

    public void drawLeadingMargin(Canvas canvas, Paint paint, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
        int line;
        Canvas canvas2 = canvas;
        Paint paint2 = paint;
        int i = start;
        Layout layout2 = layout;
        if (((Spanned) text).getSpanStart(this) == i) {
            Paint.Style style = paint.getStyle();
            int oldcolor = 0;
            if (this.mWantColor) {
                oldcolor = paint.getColor();
                paint2.setColor(this.mColor);
            }
            paint2.setStyle(Paint.Style.FILL);
            if (layout2 != null) {
                line = bottom - layout2.getLineExtra(layout2.getLineForOffset(i));
            } else {
                line = bottom;
            }
            float yPosition = ((float) (top + line)) / 2.0f;
            float xPosition = (float) (x + (this.mBulletRadius * dir));
            if (canvas2.isHardwareAccelerated()) {
                if (this.mBulletPath == null) {
                    this.mBulletPath = new Path();
                    this.mBulletPath.addCircle(0.0f, 0.0f, (float) this.mBulletRadius, Path.Direction.CW);
                }
                canvas2.save();
                canvas2.translate(xPosition, yPosition);
                canvas2.drawPath(this.mBulletPath, paint2);
                canvas2.restore();
            } else {
                canvas2.drawCircle(xPosition, yPosition, (float) this.mBulletRadius, paint2);
            }
            if (this.mWantColor) {
                paint2.setColor(oldcolor);
            }
            paint2.setStyle(style);
            return;
        }
    }
}
