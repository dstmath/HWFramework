package android.os.health;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class TimerStat implements Parcelable {
    public static final Creator<TimerStat> CREATOR = new Creator<TimerStat>() {
        public TimerStat createFromParcel(Parcel in) {
            return new TimerStat(in);
        }

        public TimerStat[] newArray(int size) {
            return new TimerStat[size];
        }
    };
    private int mCount;
    private long mTime;

    public TimerStat(int count, long time) {
        this.mCount = count;
        this.mTime = time;
    }

    public TimerStat(Parcel in) {
        this.mCount = in.readInt();
        this.mTime = in.readLong();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mCount);
        out.writeLong(this.mTime);
    }

    public void setCount(int count) {
        this.mCount = count;
    }

    public int getCount() {
        return this.mCount;
    }

    public void setTime(long time) {
        this.mTime = time;
    }

    public long getTime() {
        return this.mTime;
    }
}
