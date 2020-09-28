package android.hardware.display;

import android.annotation.SystemApi;
import android.content.Context;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.time.LocalDate;
import java.util.Arrays;

@SystemApi
public final class AmbientBrightnessDayStats implements Parcelable {
    public static final Parcelable.Creator<AmbientBrightnessDayStats> CREATOR = new Parcelable.Creator<AmbientBrightnessDayStats>() {
        /* class android.hardware.display.AmbientBrightnessDayStats.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AmbientBrightnessDayStats createFromParcel(Parcel source) {
            return new AmbientBrightnessDayStats(source);
        }

        @Override // android.os.Parcelable.Creator
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
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AmbientBrightnessDayStats other = (AmbientBrightnessDayStats) obj;
        if (!this.mLocalDate.equals(other.mLocalDate) || !Arrays.equals(this.mBucketBoundaries, other.mBucketBoundaries) || !Arrays.equals(this.mStats, other.mStats)) {
            return false;
        }
        return true;
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
        return this.mLocalDate + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + "{" + ((CharSequence) bucketBoundariesString) + "} " + "{" + ((CharSequence) statsString) + "}";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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
        float[] fArr = this.mBucketBoundaries;
        if (ambientBrightness < fArr[0]) {
            return -1;
        }
        int low = 0;
        int high = fArr.length - 1;
        while (low < high) {
            int mid = (low + high) / 2;
            float[] fArr2 = this.mBucketBoundaries;
            if (fArr2[mid] <= ambientBrightness && ambientBrightness < fArr2[mid + 1]) {
                return mid;
            }
            float[] fArr3 = this.mBucketBoundaries;
            if (fArr3[mid] < ambientBrightness) {
                low = mid + 1;
            } else if (fArr3[mid] > ambientBrightness) {
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
