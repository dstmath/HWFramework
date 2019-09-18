package android.text.style;

import android.os.Parcel;
import android.text.Layout;
import android.text.ParcelableSpan;

public interface AlignmentSpan extends ParagraphStyle {

    public static class Standard implements AlignmentSpan, ParcelableSpan {
        private final Layout.Alignment mAlignment;

        public Standard(Layout.Alignment align) {
            this.mAlignment = align;
        }

        public Standard(Parcel src) {
            this.mAlignment = Layout.Alignment.valueOf(src.readString());
        }

        public int getSpanTypeId() {
            return getSpanTypeIdInternal();
        }

        public int getSpanTypeIdInternal() {
            return 1;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            writeToParcelInternal(dest, flags);
        }

        public void writeToParcelInternal(Parcel dest, int flags) {
            dest.writeString(this.mAlignment.name());
        }

        public Layout.Alignment getAlignment() {
            return this.mAlignment;
        }
    }

    Layout.Alignment getAlignment();
}
