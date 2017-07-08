package android.text.style;

import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;

public class AbsoluteSizeSpan extends MetricAffectingSpan implements ParcelableSpan {
    private boolean mDip;
    private final int mSize;

    public AbsoluteSizeSpan(int size) {
        this.mSize = size;
    }

    public AbsoluteSizeSpan(int size, boolean dip) {
        this.mSize = size;
        this.mDip = dip;
    }

    public AbsoluteSizeSpan(Parcel src) {
        boolean z = false;
        this.mSize = src.readInt();
        if (src.readInt() != 0) {
            z = true;
        }
        this.mDip = z;
    }

    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    public int getSpanTypeIdInternal() {
        return 16;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    public void writeToParcelInternal(Parcel dest, int flags) {
        dest.writeInt(this.mSize);
        dest.writeInt(this.mDip ? 1 : 0);
    }

    public int getSize() {
        return this.mSize;
    }

    public boolean getDip() {
        return this.mDip;
    }

    public void updateDrawState(TextPaint ds) {
        if (this.mDip) {
            ds.setTextSize(((float) this.mSize) * ds.density);
        } else {
            ds.setTextSize((float) this.mSize);
        }
    }

    public void updateMeasureState(TextPaint ds) {
        if (this.mDip) {
            ds.setTextSize(((float) this.mSize) * ds.density);
        } else {
            ds.setTextSize((float) this.mSize);
        }
    }
}
