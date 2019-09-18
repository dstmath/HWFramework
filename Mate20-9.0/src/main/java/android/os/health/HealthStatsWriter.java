package android.os.health;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.health.HealthKeys;
import android.util.ArrayMap;

public class HealthStatsWriter {
    private final HealthKeys.Constants mConstants;
    private final boolean[] mMeasurementFields;
    private final long[] mMeasurementValues;
    private final ArrayMap<String, Long>[] mMeasurementsValues;
    private final ArrayMap<String, HealthStatsWriter>[] mStatsValues;
    private final int[] mTimerCounts;
    private final boolean[] mTimerFields;
    private final long[] mTimerTimes;
    private final ArrayMap<String, TimerStat>[] mTimersValues;

    public HealthStatsWriter(HealthKeys.Constants constants) {
        this.mConstants = constants;
        int timerCount = constants.getSize(0);
        this.mTimerFields = new boolean[timerCount];
        this.mTimerCounts = new int[timerCount];
        this.mTimerTimes = new long[timerCount];
        int measurementCount = constants.getSize(1);
        this.mMeasurementFields = new boolean[measurementCount];
        this.mMeasurementValues = new long[measurementCount];
        this.mStatsValues = new ArrayMap[constants.getSize(2)];
        this.mTimersValues = new ArrayMap[constants.getSize(3)];
        this.mMeasurementsValues = new ArrayMap[constants.getSize(4)];
    }

    public void addTimer(int timerId, int count, long time) {
        int index = this.mConstants.getIndex(0, timerId);
        this.mTimerFields[index] = true;
        this.mTimerCounts[index] = count;
        this.mTimerTimes[index] = time;
    }

    public void addMeasurement(int measurementId, long value) {
        int index = this.mConstants.getIndex(1, measurementId);
        this.mMeasurementFields[index] = true;
        this.mMeasurementValues[index] = value;
    }

    public void addStats(int key, String name, HealthStatsWriter value) {
        int index = this.mConstants.getIndex(2, key);
        ArrayMap<String, HealthStatsWriter> map = this.mStatsValues[index];
        if (map == null) {
            ArrayMap<String, HealthStatsWriter>[] arrayMapArr = this.mStatsValues;
            ArrayMap<String, HealthStatsWriter> arrayMap = new ArrayMap<>(1);
            arrayMapArr[index] = arrayMap;
            map = arrayMap;
        }
        map.put(name, value);
    }

    public void addTimers(int key, String name, TimerStat value) {
        int index = this.mConstants.getIndex(3, key);
        ArrayMap<String, TimerStat> map = this.mTimersValues[index];
        if (map == null) {
            ArrayMap<String, TimerStat>[] arrayMapArr = this.mTimersValues;
            ArrayMap<String, TimerStat> arrayMap = new ArrayMap<>(1);
            arrayMapArr[index] = arrayMap;
            map = arrayMap;
        }
        map.put(name, value);
    }

    public void addMeasurements(int key, String name, long value) {
        int index = this.mConstants.getIndex(4, key);
        ArrayMap<String, Long> map = this.mMeasurementsValues[index];
        if (map == null) {
            ArrayMap<String, Long>[] arrayMapArr = this.mMeasurementsValues;
            ArrayMap<String, Long> arrayMap = new ArrayMap<>(1);
            arrayMapArr[index] = arrayMap;
            map = arrayMap;
        }
        map.put(name, Long.valueOf(value));
    }

    public void flattenToParcel(Parcel out) {
        out.writeString(this.mConstants.getDataType());
        out.writeInt(countBooleanArray(this.mTimerFields));
        int[] keys = this.mConstants.getKeys(0);
        for (int i = 0; i < keys.length; i++) {
            if (this.mTimerFields[i]) {
                out.writeInt(keys[i]);
                out.writeInt(this.mTimerCounts[i]);
                out.writeLong(this.mTimerTimes[i]);
            }
        }
        out.writeInt(countBooleanArray(this.mMeasurementFields));
        int[] keys2 = this.mConstants.getKeys(1);
        for (int i2 = 0; i2 < keys2.length; i2++) {
            if (this.mMeasurementFields[i2]) {
                out.writeInt(keys2[i2]);
                out.writeLong(this.mMeasurementValues[i2]);
            }
        }
        out.writeInt(countObjectArray(this.mStatsValues));
        int[] keys3 = this.mConstants.getKeys(2);
        for (int i3 = 0; i3 < keys3.length; i3++) {
            if (this.mStatsValues[i3] != null) {
                out.writeInt(keys3[i3]);
                writeHealthStatsWriterMap(out, this.mStatsValues[i3]);
            }
        }
        out.writeInt(countObjectArray(this.mTimersValues));
        int[] keys4 = this.mConstants.getKeys(3);
        for (int i4 = 0; i4 < keys4.length; i4++) {
            if (this.mTimersValues[i4] != null) {
                out.writeInt(keys4[i4]);
                writeParcelableMap(out, this.mTimersValues[i4]);
            }
        }
        out.writeInt(countObjectArray(this.mMeasurementsValues));
        int[] keys5 = this.mConstants.getKeys(4);
        for (int i5 = 0; i5 < keys5.length; i5++) {
            if (this.mMeasurementsValues[i5] != null) {
                out.writeInt(keys5[i5]);
                writeLongsMap(out, this.mMeasurementsValues[i5]);
            }
        }
    }

    private static int countBooleanArray(boolean[] fields) {
        int count = 0;
        for (boolean z : fields) {
            if (z) {
                count++;
            }
        }
        return count;
    }

    private static <T> int countObjectArray(T[] fields) {
        int count = 0;
        for (T t : fields) {
            if (t != null) {
                count++;
            }
        }
        return count;
    }

    private static void writeHealthStatsWriterMap(Parcel out, ArrayMap<String, HealthStatsWriter> map) {
        int N = map.size();
        out.writeInt(N);
        for (int i = 0; i < N; i++) {
            out.writeString(map.keyAt(i));
            map.valueAt(i).flattenToParcel(out);
        }
    }

    private static <T extends Parcelable> void writeParcelableMap(Parcel out, ArrayMap<String, T> map) {
        int N = map.size();
        out.writeInt(N);
        for (int i = 0; i < N; i++) {
            out.writeString(map.keyAt(i));
            ((Parcelable) map.valueAt(i)).writeToParcel(out, 0);
        }
    }

    private static void writeLongsMap(Parcel out, ArrayMap<String, Long> map) {
        int N = map.size();
        out.writeInt(N);
        for (int i = 0; i < N; i++) {
            out.writeString(map.keyAt(i));
            out.writeLong(map.valueAt(i).longValue());
        }
    }
}
