package android.rms.iaware;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class CollectData implements Parcelable {
    public static final Parcelable.Creator<CollectData> CREATOR = new Parcelable.Creator<CollectData>() {
        /* class android.rms.iaware.CollectData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CollectData createFromParcel(Parcel source) {
            return new CollectData(source.readInt(), source.readLong(), source.readString(), source.readBundle());
        }

        @Override // android.os.Parcelable.Creator
        public CollectData[] newArray(int size) {
            return new CollectData[size];
        }
    };
    private static final int MAX_POOL_SIZE = 10;
    private static final Object POOL_SYNC = new Object();
    private static CollectData sPool;
    private static int sPoolSize = 0;
    private Bundle mBundle;
    private String mData;
    private int mResId;
    private long mTimeStamp;
    CollectData next;

    public CollectData() {
        this(0, 0, null, null);
    }

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

    public static CollectData obtain() {
        synchronized (POOL_SYNC) {
            if (sPool == null) {
                return new CollectData();
            }
            CollectData collectData = sPool;
            sPool = collectData.next;
            collectData.next = null;
            sPoolSize--;
            return collectData;
        }
    }

    public static CollectData obtain(int resid, long timestamp, String data) {
        CollectData collectData = obtain();
        collectData.mResId = resid;
        collectData.mTimeStamp = timestamp;
        collectData.mData = data;
        return collectData;
    }

    public static CollectData obtain(int resid, long timestamp, Bundle bundle) {
        CollectData collectData = obtain();
        collectData.mResId = resid;
        collectData.mTimeStamp = timestamp;
        collectData.mBundle = bundle;
        return collectData;
    }

    public void recycle() {
        this.mResId = 0;
        this.mTimeStamp = 0;
        this.mData = null;
        this.mBundle = null;
        synchronized (POOL_SYNC) {
            if (sPoolSize < 10) {
                this.next = sPool;
                sPool = this;
                sPoolSize++;
            }
        }
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
