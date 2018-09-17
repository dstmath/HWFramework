package android.hardware.camera2.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class LongParcelable implements Parcelable {
    public static final Creator<LongParcelable> CREATOR = new Creator<LongParcelable>() {
        public LongParcelable createFromParcel(Parcel in) {
            return new LongParcelable(in, null);
        }

        public LongParcelable[] newArray(int size) {
            return new LongParcelable[size];
        }
    };
    private long number;

    /* synthetic */ LongParcelable(Parcel in, LongParcelable -this1) {
        this(in);
    }

    public LongParcelable() {
        this.number = 0;
    }

    public LongParcelable(long number) {
        this.number = number;
    }

    private LongParcelable(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.number);
    }

    public void readFromParcel(Parcel in) {
        this.number = in.readLong();
    }

    public long getNumber() {
        return this.number;
    }

    public void setNumber(long number) {
        this.number = number;
    }
}
