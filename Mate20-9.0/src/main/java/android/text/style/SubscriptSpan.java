package android.text.style;

import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;

public class SubscriptSpan extends MetricAffectingSpan implements ParcelableSpan {
    public SubscriptSpan() {
    }

    public SubscriptSpan(Parcel src) {
    }

    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    public int getSpanTypeIdInternal() {
        return 15;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    public void writeToParcelInternal(Parcel dest, int flags) {
    }

    public void updateDrawState(TextPaint textPaint) {
        textPaint.baselineShift -= (int) (textPaint.ascent() / 2.0f);
    }

    public void updateMeasureState(TextPaint textPaint) {
        textPaint.baselineShift -= (int) (textPaint.ascent() / 2.0f);
    }
}
