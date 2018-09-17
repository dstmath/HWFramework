package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class NetworkQuotaInfo implements Parcelable {
    public static final Creator<NetworkQuotaInfo> CREATOR = new Creator<NetworkQuotaInfo>() {
        public NetworkQuotaInfo createFromParcel(Parcel in) {
            return new NetworkQuotaInfo(in);
        }

        public NetworkQuotaInfo[] newArray(int size) {
            return new NetworkQuotaInfo[size];
        }
    };
    public static final long NO_LIMIT = -1;
    private final long mEstimatedBytes;
    private final long mHardLimitBytes;
    private final long mSoftLimitBytes;

    public NetworkQuotaInfo(long estimatedBytes, long softLimitBytes, long hardLimitBytes) {
        this.mEstimatedBytes = estimatedBytes;
        this.mSoftLimitBytes = softLimitBytes;
        this.mHardLimitBytes = hardLimitBytes;
    }

    public NetworkQuotaInfo(Parcel in) {
        this.mEstimatedBytes = in.readLong();
        this.mSoftLimitBytes = in.readLong();
        this.mHardLimitBytes = in.readLong();
    }

    public long getEstimatedBytes() {
        return this.mEstimatedBytes;
    }

    public long getSoftLimitBytes() {
        return this.mSoftLimitBytes;
    }

    public long getHardLimitBytes() {
        return this.mHardLimitBytes;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.mEstimatedBytes);
        out.writeLong(this.mSoftLimitBytes);
        out.writeLong(this.mHardLimitBytes);
    }
}
