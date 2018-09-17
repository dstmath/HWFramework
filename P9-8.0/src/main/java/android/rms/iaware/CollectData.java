package android.rms.iaware;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class CollectData implements Parcelable {
    public static final Creator<CollectData> CREATOR = new Creator<CollectData>() {
        public CollectData createFromParcel(Parcel source) {
            return new CollectData(source.readInt(), source.readLong(), source.readString(), source.readBundle());
        }

        public CollectData[] newArray(int size) {
            return new CollectData[size];
        }
    };
    private Bundle mBundle;
    private String mData;
    private int mResId;
    private long mTimeStamp;

    public CollectData(int resid, long timestamp, String data) {
        this(resid, timestamp, data, null);
    }

    public CollectData(int resid, long timeStamp, Bundle bundle) {
        this(resid, timeStamp, null, bundle);
    }

    public CollectData(int resid, long timeStamp, String strData, Bundle bundleData) {
        this.mResId = resid;
        this.mTimeStamp = timeStamp;
        this.mData = strData;
        this.mBundle = bundleData;
    }

    public int getResId() {
        return this.mResId;
    }

    public void setResId(int mResId) {
        this.mResId = mResId;
    }

    public long getTimeStamp() {
        return this.mTimeStamp;
    }

    public void setTimeStamp(long mTimeStamp) {
        this.mTimeStamp = mTimeStamp;
    }

    public String getData() {
        return this.mData;
    }

    public void setData(String mData) {
        this.mData = mData;
    }

    public Bundle getBundle() {
        return this.mBundle;
    }

    public void setBundle(Bundle bundle) {
        this.mBundle = bundle;
    }

    public String toString() {
        return "[mResId=" + this.mResId + ", mTimeStamp=" + this.mTimeStamp + ", mData=" + this.mData + ", mBundle=" + this.mBundle + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mResId);
        dest.writeLong(this.mTimeStamp);
        dest.writeString(this.mData);
        dest.writeBundle(this.mBundle);
    }
}
