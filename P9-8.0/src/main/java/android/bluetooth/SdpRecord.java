package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;

public class SdpRecord implements Parcelable {
    public static final Creator CREATOR = new Creator() {
        public SdpRecord createFromParcel(Parcel in) {
            return new SdpRecord(in);
        }

        public SdpRecord[] newArray(int size) {
            return new SdpRecord[size];
        }
    };
    private final byte[] mRawData;
    private final int mRawSize;

    public String toString() {
        return "BluetoothSdpRecord [rawData=" + Arrays.toString(this.mRawData) + ", rawSize=" + this.mRawSize + "]";
    }

    public SdpRecord(int size_record, byte[] record) {
        this.mRawData = record;
        this.mRawSize = size_record;
    }

    public SdpRecord(Parcel in) {
        this.mRawSize = in.readInt();
        this.mRawData = new byte[this.mRawSize];
        in.readByteArray(this.mRawData);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRawSize);
        dest.writeByteArray(this.mRawData);
    }

    public byte[] getRawData() {
        return this.mRawData;
    }

    public int getRawSize() {
        return this.mRawSize;
    }
}
