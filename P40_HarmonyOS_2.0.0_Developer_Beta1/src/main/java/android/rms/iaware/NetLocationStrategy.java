package android.rms.iaware;

import android.os.Parcel;
import android.os.Parcelable;

public class NetLocationStrategy implements Parcelable {
    public static final Parcelable.Creator<NetLocationStrategy> CREATOR = new Parcelable.Creator<NetLocationStrategy>() {
        /* class android.rms.iaware.NetLocationStrategy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NetLocationStrategy createFromParcel(Parcel source) {
            return new NetLocationStrategy(source.readLong(), source.readLong());
        }

        @Override // android.os.Parcelable.Creator
        public NetLocationStrategy[] newArray(int size) {
            return new NetLocationStrategy[size];
        }
    };
    public static final int NETLOCATION_MODEM = 2;
    public static final int NETLOCATION_WIFI = 1;
    private long mCycle;
    private long mTimeStamp;

    public NetLocationStrategy(long cycle, long timeStamp) {
        this.mCycle = cycle;
        this.mTimeStamp = timeStamp;
    }

    public long getCycle() {
        return this.mCycle;
    }

    public void setCycle(int cycle) {
        this.mCycle = (long) cycle;
    }

    public long getTimeStamp() {
        return this.mTimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.mTimeStamp = timeStamp;
    }

    public String toString() {
        return "[mCycle = " + this.mCycle + ", mTimeStamp = " + this.mTimeStamp + "]";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mCycle);
        dest.writeLong(this.mTimeStamp);
    }
}
