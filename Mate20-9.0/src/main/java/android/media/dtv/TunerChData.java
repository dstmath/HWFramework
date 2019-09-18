package android.media.dtv;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class TunerChData implements Parcelable {
    public static final int BUFF_MAX_SIZE = Integer.MAX_VALUE;
    public static final Parcelable.Creator<TunerChData> CREATOR = new Parcelable.Creator<TunerChData>() {
        public TunerChData createFromParcel(Parcel source) {
            return new TunerChData(source);
        }

        public TunerChData[] newArray(int size) {
            return new TunerChData[size];
        }
    };
    public static final String TAG = "TunerChData";
    private byte[] mData;
    private int mSize;
    private int mTsPktSize;

    public TunerChData(int size) {
        this.mSize = 0;
        this.mSize = size;
        this.mData = new byte[this.mSize];
    }

    private TunerChData(Parcel in) {
        this.mSize = 0;
        this.mSize = in.readInt();
        this.mData = new byte[this.mSize];
        in.readByteArray(this.mData);
        this.mTsPktSize = in.readInt();
    }

    public int getSize() {
        return this.mSize;
    }

    public void setSize(int size) {
        this.mSize = size;
    }

    public byte[] getData() {
        return (byte[]) this.mData.clone();
    }

    public void setData(byte[] data) {
        if (this.mSize < data.length || data.length <= 0) {
            Log.e("TunerChData", "data.length " + data.length);
            return;
        }
        this.mData = (byte[]) data.clone();
    }

    public long getTsPktSize() {
        return (long) this.mTsPktSize;
    }

    public void setTsPktSize(int tsPktSize) {
        this.mTsPktSize = tsPktSize;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSize);
        dest.writeByteArray(this.mData);
        dest.writeInt(this.mTsPktSize);
    }

    public void readFromParcel(Parcel source) {
        this.mSize = source.readInt();
        source.readByteArray(this.mData);
        this.mTsPktSize = source.readInt();
    }
}
