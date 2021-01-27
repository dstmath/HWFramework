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
        this(-16776961, 2, 2);
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

    @Override // android.text.ParcelableSpan
    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    @Override // android.text.ParcelableSpan
    public int getSpanTypeIdInternal() {
        return 9;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    @Override // android.text.ParcelableSpan
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

    @Override // android.text.style.LeadingMarginSpan
    public int getLeadingMargin(boolean first) {
        return this.mStripeWidth + this.mGapWidth;
    }

    @Override // android.text.style.LeadingMarginSpan
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
        Paint.Style style = p.getStyle();
        int color = p.getColor();
        p.setStyle(Paint.Style.FILL);
        p.setColor(this.mColor);
        c.drawRect((float) x, (float) top, (float) ((this.mStripeWidth * dir) + x), (float) bottom, p);
        p.setStyle(style);
        p.setColor(color);
    }
}
