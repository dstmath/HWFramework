package android.text.style;

import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;

public class UnderlineSpan extends CharacterStyle implements UpdateAppearance, ParcelableSpan {
    public UnderlineSpan() {
    }

    public UnderlineSpan(Parcel src) {
    }

    @Override // android.text.ParcelableSpan
    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    @Override // android.text.ParcelableSpan
    public int getSpanTypeIdInternal() {
        return 6;
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
    }

    @Override // android.text.style.CharacterStyle
    public void updateDrawState(TextPaint ds) {
        ds.setUnderlineText(true);
    }
}
