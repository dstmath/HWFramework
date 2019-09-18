package android.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.text.Layout;
import android.text.ParcelableSpan;

public class QuoteSpan implements LeadingMarginSpan, ParcelableSpan {
    public static final int STANDARD_COLOR = -16776961;
    public static final int STANDARD_GAP_WIDTH_PX = 2;
    public static final int STANDARD_STRIPE_WIDTH_PX = 2;
    private final int mColor;
    private final int mGapWidth;
    private final int mStripeWidth;

    public QuoteSpan() {
        this(STANDARD_COLOR, 2, 2);
    }

    public QuoteSpan(int color) {
        this(color, 2, 2);
    }

    public QuoteSpan(int color, int stripeWidth, int gapWidth) {
        this.mColor = color;
        this.mStripeWidth = stripeWidth;
        this.mGapWidth = gapWidth;
    }

    public QuoteSpan(Parcel src) {
        this.mColor = src.readInt();
        this.mStripeWidth = src.readInt();
        this.mGapWidth = src.readInt();
    }

    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    public int getSpanTypeIdInternal() {
        return 9;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    public void writeToParcelInternal(Parcel dest, int flags) {
        dest.writeInt(this.mColor);
        dest.writeInt(this.mStripeWidth);
        dest.writeInt(this.mGapWidth);
    }

    public int getColor() {
        return this.mColor;
    }

    public int getStripeWidth() {
        return this.mStripeWidth;
    }

    public int getGapWidth() {
        return this.mGapWidth;
    }

    public int getLeadingMargin(boolean first) {
        return this.mStripeWidth + this.mGapWidth;
    }

    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
        Paint paint = p;
        int i = x;
        Paint.Style style = paint.getStyle();
        int color = paint.getColor();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(this.mColor);
        c.drawRect((float) i, (float) top, (float) ((this.mStripeWidth * dir) + i), (float) bottom, paint);
        paint.setStyle(style);
        paint.setColor(color);
    }
}
