package android.text.style;

import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;

public class StrikethroughSpan extends CharacterStyle implements UpdateAppearance, ParcelableSpan {
    public StrikethroughSpan(Parcel src) {
    }

    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    public int getSpanTypeIdInternal() {
        return 5;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    public void writeToParcelInternal(Parcel dest, int flags) {
    }

    public void updateDrawState(TextPaint ds) {
        ds.setStrikeThruText(true);
    }
}
