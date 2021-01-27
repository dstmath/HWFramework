package android.app.usage;

import android.annotation.UnsupportedAppUsage;
import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;

public final class ConfigurationStats implements Parcelable {
    public static final Parcelable.Creator<ConfigurationStats> CREATOR = new Parcelable.Creator<ConfigurationStats>() {
        /* class android.app.usage.ConfigurationStats.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ConfigurationStats createFromParcel(Parcel source) {
            ConfigurationStats stats = new ConfigurationStats();
            if (source.readInt() != 0) {
                stats.mConfiguration = Configuration.CREATOR.createFromParcel(source);
            }
            stats.mBeginTimeStamp = source.readLong();
            stats.mEndTimeStamp = source.readLong();
            stats.mLastTimeActive = source.readLong();
            stats.mTotalTimeActive = source.readLong();
            stats.mActivationCount = source.readInt();
            return stats;
        }

        @Override // android.os.Parcelable.Creator
        public ConfigurationStats[] newArray(int size) {
            return new ConfigurationStats[size];
        }
    };
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public int mActivationCount;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public long mBeginTimeStamp;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public Configuration mConfiguration;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public long mEndTimeStamp;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public long mLastTimeActive;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public long mTotalTimeActive;

    public ConfigurationStats() {
    }

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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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
