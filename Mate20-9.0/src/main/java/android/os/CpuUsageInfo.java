package android.os;

import android.os.Parcelable;

public final class CpuUsageInfo implements Parcelable {
    public static final Parcelable.Creator<CpuUsageInfo> CREATOR = new Parcelable.Creator<CpuUsageInfo>() {
        public CpuUsageInfo createFromParcel(Parcel in) {
            return new CpuUsageInfo(in);
        }

        public CpuUsageInfo[] newArray(int size) {
            return new CpuUsageInfo[size];
        }
    };
    private long mActive;
    private long mTotal;

    public CpuUsageInfo(long activeTime, long totalTime) {
        this.mActive = activeTime;
        this.mTotal = totalTime;
    }

    private CpuUsageInfo(Parcel in) {
        readFromParcel(in);
    }

    public long getActive() {
        return this.mActive;
    }

    public long getTotal() {
        return this.mTotal;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.mActive);
        out.writeLong(this.mTotal);
    }

    private void readFromParcel(Parcel in) {
        this.mActive = in.readLong();
        this.mTotal = in.readLong();
    }
}
