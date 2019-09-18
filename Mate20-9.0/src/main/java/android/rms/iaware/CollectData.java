package android.rms.iaware;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class CollectData implements Parcelable {
    public static final Parcelable.Creator<CollectData> CREATOR = new Parcelable.Creator<CollectData>() {
        public CollectData createFromParcel(Parcel source) {
            CollectData collectData = new CollectData(source.readInt(), source.readLong(), source.readString(), source.readBundle());
            return collectData;
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

    public void setResId(int mResId2) {
        this.mResId = mResId2;
    }

    public long getTimeStamp() {
        return this.mTimeStamp;
    }

    public void setTimeStamp(long mTimeStamp2) {
        this.mTimeStamp = mTimeStamp2;
    }

    public String getData() {
        return this.mData;
    }

    public void setData(String mData2) {
        this.mData = mData2;
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
