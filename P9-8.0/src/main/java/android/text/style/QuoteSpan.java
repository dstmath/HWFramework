package android.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Parcel;
import android.text.Layout;
import android.text.ParcelableSpan;

public class QuoteSpan implements LeadingMarginSpan, ParcelableSpan {
    private static final int GAP_WIDTH = 2;
    private static final int STRIPE_WIDTH = 2;
    private final int mColor;

    public QuoteSpan() {
        this.mColor = -16776961;
    }

    public QuoteSpan(int color) {
        this.mColor = color;
    }

    public QuoteSpan(Parcel src) {
        this.mColor = src.readInt();
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
    }

    public int getColor() {
        return this.mColor;
    }

    public int getLeadingMargin(boolean first) {
        return 4;
    }

    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
        Style style = p.getStyle();
        int color = p.getColor();
        p.setStyle(Style.FILL);
        p.setColor(this.mColor);
        c.drawRect((float) x, (float) top, (float) ((dir * 2) + x), (float) bottom, p);
        p.setStyle(style);
        p.setColor(color);
    }
}
