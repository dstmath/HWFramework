package android.hardware.camera2.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class LongParcelable implements Parcelable {
    public static final Parcelable.Creator<LongParcelable> CREATOR = new Parcelable.Creator<LongParcelable>() {
        public LongParcelable createFromParcel(Parcel in) {
            return new LongParcelable(in);
        }

        public LongParcelable[] newArray(int size) {
            return new LongParcelable[size];
        }
    };
    private long number;

    public LongParcelable() {
        this.number = 0;
    }

    public LongParcelable(long number2) {
        this.number = number2;
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

    public void setNumber(long number2) {
        this.number = number2;
    }
}
