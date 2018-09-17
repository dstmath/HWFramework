package android.text.style;

import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;

public class ScaleXSpan extends MetricAffectingSpan implements ParcelableSpan {
    private final float mProportion;

    public ScaleXSpan(float proportion) {
        this.mProportion = proportion;
    }

    public ScaleXSpan(Parcel src) {
        this.mProportion = src.readFloat();
    }

    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    public int getSpanTypeIdInternal() {
        return 4;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    public void writeToParcelInternal(Parcel dest, int flags) {
        dest.writeFloat(this.mProportion);
    }

    public float getScaleX() {
        return this.mProportion;
    }

    public void updateDrawState(TextPaint ds) {
        ds.setTextScaleX(ds.getTextScaleX() * this.mProportion);
    }

    public void updateMeasureState(TextPaint ds) {
        ds.setTextScaleX(ds.getTextScaleX() * this.mProportion);
    }
}
