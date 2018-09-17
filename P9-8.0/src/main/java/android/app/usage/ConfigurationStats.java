package android.app.usage;

import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class ConfigurationStats implements Parcelable {
    public static final Creator<ConfigurationStats> CREATOR = new Creator<ConfigurationStats>() {
        public ConfigurationStats createFromParcel(Parcel source) {
            ConfigurationStats stats = new ConfigurationStats();
            if (source.readInt() != 0) {
                stats.mConfiguration = (Configuration) Configuration.CREATOR.createFromParcel(source);
            }
            stats.mBeginTimeStamp = source.readLong();
            stats.mEndTimeStamp = source.readLong();
            stats.mLastTimeActive = source.readLong();
            stats.mTotalTimeActive = source.readLong();
            stats.mActivationCount = source.readInt();
            return stats;
        }

        public ConfigurationStats[] newArray(int size) {
            return new ConfigurationStats[size];
        }
    };
    public int mActivationCount;
    public long mBeginTimeStamp;
    public Configuration mConfiguration;
    public long mEndTimeStamp;
    public long mLastTimeActive;
    public long mTotalTimeActive;

    public ConfigurationStats(ConfigurationStats stats) {
        this.mConfiguration = stats.mConfiguration;
        this.mBeginTimeStamp = stats.mBeginTimeStamp;
        this.mEndTimeStamp = stats.mEndTimeStamp;
        this.mLastTimeActive = stats.mLastTimeActive;
        this.mTotalTimeActive = stats.mTotalTimeActive;
        this.mActivationCount = stats.mActivationCount;
    }

    public Configuration getConfiguration() {
        return this.mConfiguration;
    }

    public long getFirstTimeStamp() {
        return this.mBeginTimeStamp;
    }

    public long getLastTimeStamp() {
        return this.mEndTimeStamp;
    }

    public long getLastTimeActive() {
        return this.mLastTimeActive;
    }

    public long getTotalTimeActive() {
        return this.mTotalTimeActive;
    }

    public int getActivationCount() {
        return this.mActivationCount;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (this.mConfiguration != null) {
            dest.writeInt(1);
            this.mConfiguration.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        dest.writeLong(this.mBeginTimeStamp);
        dest.writeLong(this.mEndTimeStamp);
        dest.writeLong(this.mLastTimeActive);
        dest.writeLong(this.mTotalTimeActive);
        dest.writeInt(this.mActivationCount);
    }
}
