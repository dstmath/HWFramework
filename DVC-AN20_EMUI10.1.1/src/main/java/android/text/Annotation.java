package android.text;

import android.os.Parcel;

public class Annotation implements ParcelableSpan {
    private final String mKey;
    private final String mValue;

    public Annotation(String key, String value) {
        this.mKey = key;
        this.mValue = value;
    }

    public Annotation(Parcel src) {
        this.mKey = src.readString();
        this.mValue = src.readString();
    }

    @Override // android.text.ParcelableSpan
    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    @Override // android.text.ParcelableSpan
    public int getSpanTypeIdInternal() {
        return 18;
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
        dest.writeString(this.mKey);
        dest.writeString(this.mValue);
    }

    public String getKey() {
        return this.mKey;
    }

    public String getValue() {
        return this.mValue;
    }
}
