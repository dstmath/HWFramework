package android.os.health;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.health.HealthKeys.Constants;
import android.util.ArrayMap;

public class HealthStatsWriter {
    private final Constants mConstants;
    private final boolean[] mMeasurementFields;
    private final long[] mMeasurementValues;
    private final ArrayMap<String, Long>[] mMeasurementsValues;
    private final ArrayMap<String, HealthStatsWriter>[] mStatsValues;
    private final int[] mTimerCounts;
    private final boolean[] mTimerFields;
    private final long[] mTimerTimes;
    private final ArrayMap<String, TimerStat>[] mTimersValues;

    public HealthStatsWriter(Constants constants) {
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
            map = new ArrayMap(1);
            this.mStatsValues[index] = map;
        }
        map.put(name, value);
    }

    public void addTimers(int key, String name, TimerStat value) {
        int index = this.mConstants.getIndex(3, key);
        ArrayMap<String, TimerStat> map = this.mTimersValues[index];
        if (map == null) {
            map = new ArrayMap(1);
            this.mTimersValues[index] = map;
        }
        map.put(name, value);
    }

    public void addMeasurements(int key, String name, long value) {
        int index = this.mConstants.getIndex(4, key);
        ArrayMap<String, Long> map = this.mMeasurementsValues[index];
        if (map == null) {
            map = new ArrayMap(1);
            this.mMeasurementsValues[index] = map;
        }
        map.put(name, Long.valueOf(value));
    }

    public void flattenToParcel(Parcel out) {
        int i;
        out.writeString(this.mConstants.getDataType());
        out.writeInt(countBooleanArray(this.mTimerFields));
        int[] keys = this.mConstants.getKeys(0);
        for (i = 0; i < keys.length; i++) {
            if (this.mTimerFields[i]) {
                out.writeInt(keys[i]);
                out.writeInt(this.mTimerCounts[i]);
                out.writeLong(this.mTimerTimes[i]);
            }
        }
        out.writeInt(countBooleanArray(this.mMeasurementFields));
        keys = this.mConstants.getKeys(1);
        for (i = 0; i < keys.length; i++) {
            if (this.mMeasurementFields[i]) {
                out.writeInt(keys[i]);
                out.writeLong(this.mMeasurementValues[i]);
            }
        }
        out.writeInt(countObjectArray(this.mStatsValues));
        keys = this.mConstants.getKeys(2);
        for (i = 0; i < keys.length; i++) {
            if (this.mStatsValues[i] != null) {
                out.writeInt(keys[i]);
                writeHealthStatsWriterMap(out, this.mStatsValues[i]);
            }
        }
        out.writeInt(countObjectArray(this.mTimersValues));
        keys = this.mConstants.getKeys(3);
        for (i = 0; i < keys.length; i++) {
            if (this.mTimersValues[i] != null) {
                out.writeInt(keys[i]);
                writeParcelableMap(out, this.mTimersValues[i]);
            }
        }
        out.writeInt(countObjectArray(this.mMeasurementsValues));
        keys = this.mConstants.getKeys(4);
        for (i = 0; i < keys.length; i++) {
            if (this.mMeasurementsValues[i] != null) {
                out.writeInt(keys[i]);
                writeLongsMap(out, this.mMeasurementsValues[i]);
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
            out.writeString((String) map.keyAt(i));
            ((HealthStatsWriter) map.valueAt(i)).flattenToParcel(out);
        }
    }

    private static <T extends Parcelable> void writeParcelableMap(Parcel out, ArrayMap<String, T> map) {
        int N = map.size();
        out.writeInt(N);
        for (int i = 0; i < N; i++) {
            out.writeString((String) map.keyAt(i));
            ((Parcelable) map.valueAt(i)).writeToParcel(out, 0);
        }
    }

    private static void writeLongsMap(Parcel out, ArrayMap<String, Long> map) {
        int N = map.size();
        out.writeInt(N);
        for (int i = 0; i < N; i++) {
            out.writeString((String) map.keyAt(i));
            out.writeLong(((Long) map.valueAt(i)).longValue());
        }
    }
}
