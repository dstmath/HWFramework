package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class UidTraffic implements Cloneable, Parcelable {
    public static final Creator<UidTraffic> CREATOR = new Creator<UidTraffic>() {
        public UidTraffic createFromParcel(Parcel source) {
            return new UidTraffic(source);
        }

        public UidTraffic[] newArray(int size) {
            return new UidTraffic[size];
        }
    };
    private final int mAppUid;
    private long mRxBytes;
    private long mTxBytes;

    public UidTraffic(int appUid) {
        this.mAppUid = appUid;
    }

    public UidTraffic(int appUid, long rx, long tx) {
        this.mAppUid = appUid;
        this.mRxBytes = rx;
        this.mTxBytes = tx;
    }

    UidTraffic(Parcel in) {
        this.mAppUid = in.readInt();
        this.mRxBytes = in.readLong();
        this.mTxBytes = in.readLong();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mAppUid);
        dest.writeLong(this.mRxBytes);
        dest.writeLong(this.mTxBytes);
    }

    public void setRxBytes(long bytes) {
        this.mRxBytes = bytes;
    }

    public void setTxBytes(long bytes) {
        this.mTxBytes = bytes;
    }

    public void addRxBytes(long bytes) {
        this.mRxBytes += bytes;
    }

    public void addTxBytes(long bytes) {
        this.mTxBytes += bytes;
    }

    public int getUid() {
        return this.mAppUid;
    }

    public long getRxBytes() {
        return this.mRxBytes;
    }

    public long getTxBytes() {
        return this.mTxBytes;
    }

    public int describeContents() {
        return 0;
    }

    public UidTraffic clone() {
        return new UidTraffic(this.mAppUid, this.mRxBytes, this.mTxBytes);
    }

    public String toString() {
        return "UidTraffic{mAppUid=" + this.mAppUid + ", mRxBytes=" + this.mRxBytes + ", mTxBytes=" + this.mTxBytes + '}';
    }
}
