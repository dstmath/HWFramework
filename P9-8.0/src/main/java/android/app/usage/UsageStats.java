package android.app.usage;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.ArrayMap;

public final class UsageStats implements Parcelable {
    public static final Creator<UsageStats> CREATOR = new Creator<UsageStats>() {
        public UsageStats createFromParcel(Parcel in) {
            UsageStats stats = new UsageStats();
            stats.mPackageName = in.readString();
            stats.mBeginTimeStamp = in.readLong();
            stats.mEndTimeStamp = in.readLong();
            stats.mLastTimeUsed = in.readLong();
            stats.mTotalTimeInForeground = in.readLong();
            stats.mLaunchCount = in.readInt();
            stats.mLastEvent = in.readInt();
            Bundle allCounts = in.readBundle();
            if (allCounts != null) {
                stats.mChooserCounts = new ArrayMap();
                for (String action : allCounts.keySet()) {
                    if (!stats.mChooserCounts.containsKey(action)) {
                        stats.mChooserCounts.put(action, new ArrayMap());
                    }
                    Bundle currentCounts = allCounts.getBundle(action);
                    if (currentCounts != null) {
                        for (String key : currentCounts.keySet()) {
                            int value = currentCounts.getInt(key);
                            if (value > 0) {
                                ((ArrayMap) stats.mChooserCounts.get(action)).put(key, Integer.valueOf(value));
                            }
                        }
                    }
                }
            }
            stats.mLandTimeInForeground = in.readLong();
            stats.mLastLandTimeUsed = in.readLong();
            stats.mTimeInPCForeground = in.readLong();
            stats.mLastTimeUsedInPC = in.readLong();
            return stats;
        }

        public UsageStats[] newArray(int size) {
            return new UsageStats[size];
        }
    };
    public long mBeginTimeStamp;
    public ArrayMap<String, ArrayMap<String, Integer>> mChooserCounts;
    public long mEndTimeStamp;
    public long mLandTimeInForeground;
    public int mLastEvent;
    public long mLastLandTimeUsed;
    public long mLastTimeUsed;
    public long mLastTimeUsedInPC;
    public int mLaunchCount;
    public String mPackageName;
    public long mTimeInPCForeground;
    public long mTotalTimeInForeground;

    public UsageStats(UsageStats stats) {
        this.mPackageName = stats.mPackageName;
        this.mBeginTimeStamp = stats.mBeginTimeStamp;
        this.mEndTimeStamp = stats.mEndTimeStamp;
        this.mLastTimeUsed = stats.mLastTimeUsed;
        this.mTotalTimeInForeground = stats.mTotalTimeInForeground;
        this.mLaunchCount = stats.mLaunchCount;
        this.mLastEvent = stats.mLastEvent;
        this.mChooserCounts = stats.mChooserCounts;
        this.mLandTimeInForeground = stats.mLandTimeInForeground;
        this.mLastLandTimeUsed = stats.mLastLandTimeUsed;
        this.mTimeInPCForeground = stats.mTimeInPCForeground;
        this.mLastTimeUsedInPC = stats.mLastTimeUsedInPC;
    }

    public UsageStats getObfuscatedForInstantApp() {
        UsageStats ret = new UsageStats(this);
        ret.mPackageName = UsageEvents.INSTANT_APP_PACKAGE_NAME;
        return ret;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public long getFirstTimeStamp() {
        return this.mBeginTimeStamp;
    }

    public long getLastTimeStamp() {
        return this.mEndTimeStamp;
    }

    public long getLastTimeUsed() {
        return this.mLastTimeUsed;
    }

    public long getTotalTimeInForeground() {
        return this.mTotalTimeInForeground;
    }

    public long getLandTimeInForeground() {
        return this.mLandTimeInForeground;
    }

    public long getLastLandTimeUsed() {
        return this.mLastLandTimeUsed;
    }

    public void add(UsageStats right) {
        if (this.mPackageName.equals(right.mPackageName)) {
            if (right.mBeginTimeStamp > this.mBeginTimeStamp) {
                this.mLastEvent = Math.max(this.mLastEvent, right.mLastEvent);
                this.mLastTimeUsed = Math.max(this.mLastTimeUsed, right.mLastTimeUsed);
                this.mLastLandTimeUsed = Math.max(this.mLastLandTimeUsed, right.mLastLandTimeUsed);
                this.mLastTimeUsedInPC = Math.max(this.mLastTimeUsedInPC, right.mLastTimeUsedInPC);
            }
            this.mBeginTimeStamp = Math.min(this.mBeginTimeStamp, right.mBeginTimeStamp);
            this.mEndTimeStamp = Math.max(this.mEndTimeStamp, right.mEndTimeStamp);
            this.mTotalTimeInForeground += right.mTotalTimeInForeground;
            this.mTimeInPCForeground += right.mTimeInPCForeground;
            this.mLaunchCount += right.mLaunchCount;
            if (this.mChooserCounts == null) {
                this.mChooserCounts = right.mChooserCounts;
            } else if (right.mChooserCounts != null) {
                int chooserCountsSize = right.mChooserCounts.size();
                for (int i = 0; i < chooserCountsSize; i++) {
                    String action = (String) right.mChooserCounts.keyAt(i);
                    ArrayMap<String, Integer> counts = (ArrayMap) right.mChooserCounts.valueAt(i);
                    if (!this.mChooserCounts.containsKey(action) || this.mChooserCounts.get(action) == null) {
                        this.mChooserCounts.put(action, counts);
                    } else {
                        int annotationSize = counts.size();
                        for (int j = 0; j < annotationSize; j++) {
                            String key = (String) counts.keyAt(j);
                            int rightValue = ((Integer) counts.valueAt(j)).intValue();
                            ((ArrayMap) this.mChooserCounts.get(action)).put(key, Integer.valueOf(((Integer) ((ArrayMap) this.mChooserCounts.get(action)).getOrDefault(key, Integer.valueOf(0))).intValue() + rightValue));
                        }
                    }
                }
            }
            this.mLandTimeInForeground += right.mLandTimeInForeground;
            return;
        }
        throw new IllegalArgumentException("Can't merge UsageStats for package '" + this.mPackageName + "' with UsageStats for package '" + right.mPackageName + "'.");
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPackageName);
        dest.writeLong(this.mBeginTimeStamp);
        dest.writeLong(this.mEndTimeStamp);
        dest.writeLong(this.mLastTimeUsed);
        dest.writeLong(this.mTotalTimeInForeground);
        dest.writeInt(this.mLaunchCount);
        dest.writeInt(this.mLastEvent);
        Bundle allCounts = new Bundle();
        if (this.mChooserCounts != null) {
            int chooserCountSize = this.mChooserCounts.size();
            for (int i = 0; i < chooserCountSize; i++) {
                String action = (String) this.mChooserCounts.keyAt(i);
                ArrayMap<String, Integer> counts = (ArrayMap) this.mChooserCounts.valueAt(i);
                Bundle currentCounts = new Bundle();
                int annotationSize = counts.size();
                for (int j = 0; j < annotationSize; j++) {
                    currentCounts.putInt((String) counts.keyAt(j), ((Integer) counts.valueAt(j)).intValue());
                }
                allCounts.putBundle(action, currentCounts);
            }
        }
        dest.writeBundle(allCounts);
        dest.writeLong(this.mLandTimeInForeground);
        dest.writeLong(this.mLastLandTimeUsed);
        dest.writeLong(this.mTimeInPCForeground);
        dest.writeLong(this.mLastTimeUsedInPC);
    }
}
