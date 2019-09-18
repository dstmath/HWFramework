package android.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.text.Layout;
import android.text.ParcelableSpan;

public interface LeadingMarginSpan extends ParagraphStyle {

    public interface LeadingMarginSpan2 extends LeadingMarginSpan, WrapTogetherSpan {
        int getLeadingMarginLineCount();
    }

    public static class Standard implements LeadingMarginSpan, ParcelableSpan {
        private final int mFirst;
        private final int mRest;

        public Standard(int first, int rest) {
            this.mFirst = first;
            this.mRest = rest;
        }

        public Standard(int every) {
            this(every, every);
        }

        public Standard(Parcel src) {
            this.mFirst = src.readInt();
            this.mRest = src.readInt();
        }

        public int getSpanTypeId() {
            return getSpanTypeIdInternal();
        }

        public int getSpanTypeIdInternal() {
            return 10;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            writeToParcelInternal(dest, flags);
        }

        public void writeToParcelInternal(Parcel dest, int flags) {
            dest.writeInt(this.mFirst);
            dest.writeInt(this.mRest);
        }

        public int getLeadingMargin(boolean first) {
            return first ? this.mFirst : this.mRest;
        }

        public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
        }
    }

    void drawLeadingMargin(Canvas canvas, Paint paint, int i, int i2, int i3, int i4, int i5, CharSequence charSequence, int i6, int i7, boolean z, Layout layout);

    int getLeadingMargin(boolean z);
}
