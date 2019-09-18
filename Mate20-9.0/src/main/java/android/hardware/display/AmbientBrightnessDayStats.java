package android.hardware.display;

import android.annotation.SystemApi;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.time.LocalDate;
import java.util.Arrays;

@SystemApi
public final class AmbientBrightnessDayStats implements Parcelable {
    public static final Parcelable.Creator<AmbientBrightnessDayStats> CREATOR = new Parcelable.Creator<AmbientBrightnessDayStats>() {
        public AmbientBrightnessDayStats createFromParcel(Parcel source) {
            return new AmbientBrightnessDayStats(source);
        }

        public AmbientBrightnessDayStats[] newArray(int size) {
            return new AmbientBrightnessDayStats[size];
        }
    };
    private final float[] mBucketBoundaries;
    private final LocalDate mLocalDate;
    private final float[] mStats;

    public AmbientBrightnessDayStats(LocalDate localDate, float[] bucketBoundaries) {
        this(localDate, bucketBoundaries, null);
    }

    public AmbientBrightnessDayStats(LocalDate localDate, float[] bucketBoundaries, float[] stats) {
        Preconditions.checkNotNull(localDate);
        Preconditions.checkNotNull(bucketBoundaries);
        Preconditions.checkArrayElementsInRange(bucketBoundaries, 0.0f, Float.MAX_VALUE, "bucketBoundaries");
        if (bucketBoundaries.length >= 1) {
            checkSorted(bucketBoundaries);
            if (stats == null) {
                stats = new float[bucketBoundaries.length];
            } else {
                Preconditions.checkArrayElementsInRange(stats, 0.0f, Float.MAX_VALUE, Context.STATS_MANAGER);
                if (bucketBoundaries.length != stats.length) {
                    throw new IllegalArgumentException("Bucket boundaries and stats must be of same size.");
                }
            }
            this.mLocalDate = localDate;
            this.mBucketBoundaries = bucketBoundaries;
            this.mStats = stats;
            return;
        }
        throw new IllegalArgumentException("Bucket boundaries must contain at least 1 value");
    }

    public LocalDate getLocalDate() {
        return this.mLocalDate;
    }

    public float[] getStats() {
        return this.mStats;
    }

    public float[] getBucketBoundaries() {
        return this.mBucketBoundaries;
    }

    private AmbientBrightnessDayStats(Parcel source) {
        this.mLocalDate = LocalDate.parse(source.readString());
        this.mBucketBoundaries = source.createFloatArray();
        this.mStats = source.createFloatArray();
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AmbientBrightnessDayStats other = (AmbientBrightnessDayStats) obj;
        if (!this.mLocalDate.equals(other.mLocalDate) || !Arrays.equals(this.mBucketBoundaries, other.mBucketBoundaries) || !Arrays.equals(this.mStats, other.mStats)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (((((1 * 31) + this.mLocalDate.hashCode()) * 31) + Arrays.hashCode(this.mBucketBoundaries)) * 31) + Arrays.hashCode(this.mStats);
    }

    public String toString() {
        StringBuilder bucketBoundariesString = new StringBuilder();
        StringBuilder statsString = new StringBuilder();
        for (int i = 0; i < this.mBucketBoundaries.length; i++) {
            if (i != 0) {
                bucketBoundariesString.append(", ");
                statsString.append(", ");
            }
            bucketBoundariesString.append(this.mBucketBoundaries[i]);
            statsString.append(this.mStats[i]);
        }
        return this.mLocalDate + " " + "{" + bucketBoundariesString + "} " + "{" + statsString + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mLocalDate.toString());
        dest.writeFloatArray(this.mBucketBoundaries);
        dest.writeFloatArray(this.mStats);
    }

    public void log(float ambientBrightness, float durationSec) {
        int bucketIndex = getBucketIndex(ambientBrightness);
        if (bucketIndex >= 0) {
            float[] fArr = this.mStats;
            fArr[bucketIndex] = fArr[bucketIndex] + durationSec;
        }
    }

    private int getBucketIndex(float ambientBrightness) {
        if (ambientBrightness < this.mBucketBoundaries[0]) {
            return -1;
        }
        int low = 0;
        int high = this.mBucketBoundaries.length - 1;
        while (low < high) {
            int mid = (low + high) / 2;
            if (this.mBucketBoundaries[mid] <= ambientBrightness && ambientBrightness < this.mBucketBoundaries[mid + 1]) {
                return mid;
            }
            if (this.mBucketBoundaries[mid] < ambientBrightness) {
                low = mid + 1;
            } else if (this.mBucketBoundaries[mid] > ambientBrightness) {
                high = mid - 1;
            }
        }
        return low;
    }

    private static void checkSorted(float[] values) {
        if (values.length > 1) {
            float prevValue = values[0];
            for (int i = 1; i < values.length; i++) {
                Preconditions.checkState(prevValue < values[i]);
                prevValue = values[i];
            }
        }
    }
}
