package android.os.health;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.ArrayMap;
import java.util.Arrays;
import java.util.Map;

public class HealthStats {
    private String mDataType;
    private int[] mMeasurementKeys;
    private long[] mMeasurementValues;
    private int[] mMeasurementsKeys;
    private ArrayMap<String, Long>[] mMeasurementsValues;
    private int[] mStatsKeys;
    private ArrayMap<String, HealthStats>[] mStatsValues;
    private int[] mTimerCounts;
    private int[] mTimerKeys;
    private long[] mTimerTimes;
    private int[] mTimersKeys;
    private ArrayMap<String, TimerStat>[] mTimersValues;

    private HealthStats() {
        throw new RuntimeException("unsupported");
    }

    public HealthStats(Parcel in) {
        int i;
        this.mDataType = in.readString();
        int count = in.readInt();
        this.mTimerKeys = new int[count];
        this.mTimerCounts = new int[count];
        this.mTimerTimes = new long[count];
        for (i = 0; i < count; i++) {
            this.mTimerKeys[i] = in.readInt();
            this.mTimerCounts[i] = in.readInt();
            this.mTimerTimes[i] = in.readLong();
        }
        count = in.readInt();
        this.mMeasurementKeys = new int[count];
        this.mMeasurementValues = new long[count];
        for (i = 0; i < count; i++) {
            this.mMeasurementKeys[i] = in.readInt();
            this.mMeasurementValues[i] = in.readLong();
        }
        count = in.readInt();
        this.mStatsKeys = new int[count];
        this.mStatsValues = new ArrayMap[count];
        for (i = 0; i < count; i++) {
            this.mStatsKeys[i] = in.readInt();
            this.mStatsValues[i] = createHealthStatsMap(in);
        }
        count = in.readInt();
        this.mTimersKeys = new int[count];
        this.mTimersValues = new ArrayMap[count];
        for (i = 0; i < count; i++) {
            this.mTimersKeys[i] = in.readInt();
            this.mTimersValues[i] = createParcelableMap(in, TimerStat.CREATOR);
        }
        count = in.readInt();
        this.mMeasurementsKeys = new int[count];
        this.mMeasurementsValues = new ArrayMap[count];
        for (i = 0; i < count; i++) {
            this.mMeasurementsKeys[i] = in.readInt();
            this.mMeasurementsValues[i] = createLongsMap(in);
        }
    }

    public String getDataType() {
        return this.mDataType;
    }

    public boolean hasTimer(int key) {
        return getIndex(this.mTimerKeys, key) >= 0;
    }

    public TimerStat getTimer(int key) {
        int index = getIndex(this.mTimerKeys, key);
        if (index >= 0) {
            return new TimerStat(this.mTimerCounts[index], this.mTimerTimes[index]);
        }
        throw new IndexOutOfBoundsException("Bad timer key dataType=" + this.mDataType + " key=" + key);
    }

    public int getTimerCount(int key) {
        int index = getIndex(this.mTimerKeys, key);
        if (index >= 0) {
            return this.mTimerCounts[index];
        }
        throw new IndexOutOfBoundsException("Bad timer key dataType=" + this.mDataType + " key=" + key);
    }

    public long getTimerTime(int key) {
        int index = getIndex(this.mTimerKeys, key);
        if (index >= 0) {
            return this.mTimerTimes[index];
        }
        throw new IndexOutOfBoundsException("Bad timer key dataType=" + this.mDataType + " key=" + key);
    }

    public int getTimerKeyCount() {
        return this.mTimerKeys.length;
    }

    public int getTimerKeyAt(int index) {
        return this.mTimerKeys[index];
    }

    public boolean hasMeasurement(int key) {
        return getIndex(this.mMeasurementKeys, key) >= 0;
    }

    public long getMeasurement(int key) {
        int index = getIndex(this.mMeasurementKeys, key);
        if (index >= 0) {
            return this.mMeasurementValues[index];
        }
        throw new IndexOutOfBoundsException("Bad measurement key dataType=" + this.mDataType + " key=" + key);
    }

    public int getMeasurementKeyCount() {
        return this.mMeasurementKeys.length;
    }

    public int getMeasurementKeyAt(int index) {
        return this.mMeasurementKeys[index];
    }

    public boolean hasStats(int key) {
        return getIndex(this.mStatsKeys, key) >= 0;
    }

    public Map<String, HealthStats> getStats(int key) {
        int index = getIndex(this.mStatsKeys, key);
        if (index >= 0) {
            return this.mStatsValues[index];
        }
        throw new IndexOutOfBoundsException("Bad stats key dataType=" + this.mDataType + " key=" + key);
    }

    public int getStatsKeyCount() {
        return this.mStatsKeys.length;
    }

    public int getStatsKeyAt(int index) {
        return this.mStatsKeys[index];
    }

    public boolean hasTimers(int key) {
        return getIndex(this.mTimersKeys, key) >= 0;
    }

    public Map<String, TimerStat> getTimers(int key) {
        int index = getIndex(this.mTimersKeys, key);
        if (index >= 0) {
            return this.mTimersValues[index];
        }
        throw new IndexOutOfBoundsException("Bad timers key dataType=" + this.mDataType + " key=" + key);
    }

    public int getTimersKeyCount() {
        return this.mTimersKeys.length;
    }

    public int getTimersKeyAt(int index) {
        return this.mTimersKeys[index];
    }

    public boolean hasMeasurements(int key) {
        return getIndex(this.mMeasurementsKeys, key) >= 0;
    }

    public Map<String, Long> getMeasurements(int key) {
        int index = getIndex(this.mMeasurementsKeys, key);
        if (index >= 0) {
            return this.mMeasurementsValues[index];
        }
        throw new IndexOutOfBoundsException("Bad measurements key dataType=" + this.mDataType + " key=" + key);
    }

    public int getMeasurementsKeyCount() {
        return this.mMeasurementsKeys.length;
    }

    public int getMeasurementsKeyAt(int index) {
        return this.mMeasurementsKeys[index];
    }

    private static int getIndex(int[] keys, int key) {
        return Arrays.binarySearch(keys, key);
    }

    private static ArrayMap<String, HealthStats> createHealthStatsMap(Parcel in) {
        int count = in.readInt();
        ArrayMap<String, HealthStats> result = new ArrayMap(count);
        for (int i = 0; i < count; i++) {
            result.put(in.readString(), new HealthStats(in));
        }
        return result;
    }

    private static <T extends Parcelable> ArrayMap<String, T> createParcelableMap(Parcel in, Creator<T> creator) {
        int count = in.readInt();
        ArrayMap<String, T> result = new ArrayMap(count);
        for (int i = 0; i < count; i++) {
            result.put(in.readString(), (Parcelable) creator.createFromParcel(in));
        }
        return result;
    }

    private static ArrayMap<String, Long> createLongsMap(Parcel in) {
        int count = in.readInt();
        ArrayMap<String, Long> result = new ArrayMap(count);
        for (int i = 0; i < count; i++) {
            result.put(in.readString(), Long.valueOf(in.readLong()));
        }
        return result;
    }
}
