package android.text.style;

import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;

public class AbsoluteSizeSpan extends MetricAffectingSpan implements ParcelableSpan {
    private final boolean mDip;
    private final int mSize;

    public AbsoluteSizeSpan(int size) {
        this(size, false);
    }

    public AbsoluteSizeSpan(int size, boolean dip) {
        this.mSize = size;
        this.mDip = dip;
    }

    public AbsoluteSizeSpan(Parcel src) {
        this.mSize = src.readInt();
        this.mDip = src.readInt() != 0;
    }

    @Override // android.text.ParcelableSpan
    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    @Override // android.text.ParcelableSpan
    public int getSpanTypeIdInternal() {
        return 16;
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
        dest.writeInt(this.mSize);
        dest.writeInt(this.mDip ? 1 : 0);
    }

    public int getSize() {
        return this.mSize;
    }

    public boolean getDip() {
        return this.mDip;
    }

    @Override // android.text.style.CharacterStyle
    public void updateDrawState(TextPaint ds) {
        if (this.mDip) {
            ds.setTextSize(((float) this.mSize) * ds.density);
        } else {
            ds.setTextSize((float) this.mSize);
        }
    }

    @Override // android.text.style.MetricAffectingSpan
    public void updateMeasureState(TextPaint ds) {
        if (this.mDip) {
            ds.setTextSize(((float) this.mSize) * ds.density);
        } else {
            ds.setTextSize((float) this.mSize);
        }
    }
}
