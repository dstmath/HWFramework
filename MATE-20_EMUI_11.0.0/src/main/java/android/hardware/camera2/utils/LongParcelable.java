package android.hardware.camera2.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class LongParcelable implements Parcelable {
    public static final Parcelable.Creator<LongParcelable> CREATOR = new Parcelable.Creator<LongParcelable>() {
        /* class android.hardware.camera2.utils.LongParcelable.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public LongParcelable createFromParcel(Parcel in) {
            return new LongParcelable(in);
        }

        @Override // android.os.Parcelable.Creator
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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
