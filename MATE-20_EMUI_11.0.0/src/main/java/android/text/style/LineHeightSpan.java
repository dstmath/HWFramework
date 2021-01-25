package android.text.style;

import android.graphics.Paint;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import com.android.internal.util.Preconditions;

public interface LineHeightSpan extends ParagraphStyle, WrapTogetherSpan {

    public interface WithDensity extends LineHeightSpan {
        void chooseHeight(CharSequence charSequence, int i, int i2, int i3, int i4, Paint.FontMetricsInt fontMetricsInt, TextPaint textPaint);
    }

    void chooseHeight(CharSequence charSequence, int i, int i2, int i3, int i4, Paint.FontMetricsInt fontMetricsInt);

    public static class Standard implements LineHeightSpan, ParcelableSpan {
        private final int mHeight;

        public Standard(int height) {
            boolean z = height > 0;
            Preconditions.checkArgument(z, "Height:" + height + "must be positive");
            this.mHeight = height;
        }

        public Standard(Parcel src) {
            this.mHeight = src.readInt();
        }

        public int getHeight() {
            return this.mHeight;
        }

        @Override // android.text.ParcelableSpan
        public int getSpanTypeId() {
            return getSpanTypeIdInternal();
        }

        @Override // android.text.ParcelableSpan
        public int getSpanTypeIdInternal() {
            return 28;
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
            dest.writeInt(this.mHeight);
        }

        @Override // android.text.style.LineHeightSpan
        public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int lineHeight, Paint.FontMetricsInt fm) {
            int originHeight = fm.descent - fm.ascent;
            if (originHeight > 0) {
                fm.descent = Math.round(((float) fm.descent) * ((((float) this.mHeight) * 1.0f) / ((float) originHeight)));
                fm.ascent = fm.descent - this.mHeight;
            }
        }
    }
}
