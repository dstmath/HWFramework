package android.app.usage;

import android.os.Parcel;
import android.os.Parcelable;

public final class EventStats implements Parcelable {
    public static final Parcelable.Creator<EventStats> CREATOR = new Parcelable.Creator<EventStats>() {
        public EventStats createFromParcel(Parcel in) {
            EventStats stats = new EventStats();
            stats.mEventType = in.readInt();
            stats.mBeginTimeStamp = in.readLong();
            stats.mEndTimeStamp = in.readLong();
            stats.mLastEventTime = in.readLong();
            stats.mTotalTime = in.readLong();
            stats.mCount = in.readInt();
            return stats;
        }

        public EventStats[] newArray(int size) {
            return new EventStats[size];
        }
    };
    public long mBeginTimeStamp;
    public int mCount;
    public long mEndTimeStamp;
    public int mEventType;
    public long mLastEventTime;
    public long mTotalTime;

    public EventStats() {
    }

    public EventStats(EventStats stats) {
        this.mEventType = stats.mEventType;
        this.mBeginTimeStamp = stats.mBeginTimeStamp;
        this.mEndTimeStamp = stats.mEndTimeStamp;
        this.mLastEventTime = stats.mLastEventTime;
        this.mTotalTime = stats.mTotalTime;
        this.mCount = stats.mCount;
    }

    public int getEventType() {
        return this.mEventType;
    }

    public long getFirstTimeStamp() {
        return this.mBeginTimeStamp;
    }

    public long getLastTimeStamp() {
        return this.mEndTimeStamp;
    }

    public long getLastEventTime() {
        return this.mLastEventTime;
    }

    public int getCount() {
        return this.mCount;
    }

    public long getTotalTime() {
        return this.mTotalTime;
    }

    public void add(EventStats right) {
        if (this.mEventType == right.mEventType) {
            if (right.mBeginTimeStamp > this.mBeginTimeStamp) {
                this.mLastEventTime = Math.max(this.mLastEventTime, right.mLastEventTime);
            }
            this.mBeginTimeStamp = Math.min(this.mBeginTimeStamp, right.mBeginTimeStamp);
            this.mEndTimeStamp = Math.max(this.mEndTimeStamp, right.mEndTimeStamp);
            this.mTotalTime += right.mTotalTime;
            this.mCount += right.mCount;
            return;
        }
        throw new IllegalArgumentException("Can't merge EventStats for event #" + this.mEventType + " with EventStats for event #" + right.mEventType);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mEventType);
        dest.writeLong(this.mBeginTimeStamp);
        dest.writeLong(this.mEndTimeStamp);
        dest.writeLong(this.mLastEventTime);
        dest.writeLong(this.mTotalTime);
        dest.writeInt(this.mCount);
    }
}
