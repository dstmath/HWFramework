package android.text.style;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;

public class SuggestionRangeSpan extends CharacterStyle implements ParcelableSpan {
    private int mBackgroundColor;

    @UnsupportedAppUsage
    public SuggestionRangeSpan() {
        this.mBackgroundColor = 0;
    }

    @UnsupportedAppUsage
    public SuggestionRangeSpan(Parcel src) {
        this.mBackgroundColor = src.readInt();
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
        dest.writeInt(this.mBackgroundColor);
    }

    @Override // android.text.ParcelableSpan
    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    @Override // android.text.ParcelableSpan
    public int getSpanTypeIdInternal() {
        return 21;
    }

    @UnsupportedAppUsage
    public void setBackgroundColor(int backgroundColor) {
        this.mBackgroundColor = backgroundColor;
    }

    @Override // android.text.style.CharacterStyle
    public void updateDrawState(TextPaint tp) {
        tp.bgColor = this.mBackgroundColor;
    }
}
