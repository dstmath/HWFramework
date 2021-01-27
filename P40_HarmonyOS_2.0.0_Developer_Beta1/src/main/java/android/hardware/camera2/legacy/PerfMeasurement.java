package android.hardware.camera2.legacy;

import android.os.SystemClock;
import android.provider.SettingsStringUtil;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/* access modifiers changed from: package-private */
public class PerfMeasurement {
    public static final int DEFAULT_MAX_QUERIES = 3;
    private static final long FAILED_TIMING = -2;
    private static final long NO_DURATION_YET = -1;
    private static final String TAG = "PerfMeasurement";
    private ArrayList<Long> mCollectedCpuDurations;
    private ArrayList<Long> mCollectedGpuDurations;
    private ArrayList<Long> mCollectedTimestamps;
    private int mCompletedQueryCount;
    private Queue<Long> mCpuDurationsQueue;
    private final long mNativeContext;
    private long mStartTimeNs;
    private Queue<Long> mTimestampQueue;

    private static native long nativeCreateContext(int i);

    private static native void nativeDeleteContext(long j);

    protected static native long nativeGetNextGlDuration(long j);

    private static native boolean nativeQuerySupport();

    protected static native void nativeStartGlTimer(long j);

    protected static native void nativeStopGlTimer(long j);

    public PerfMeasurement() {
        this.mCompletedQueryCount = 0;
        this.mCollectedGpuDurations = new ArrayList<>();
        this.mCollectedCpuDurations = new ArrayList<>();
        this.mCollectedTimestamps = new ArrayList<>();
        this.mTimestampQueue = new LinkedList();
        this.mCpuDurationsQueue = new LinkedList();
        this.mNativeContext = nativeCreateContext(3);
    }

    public PerfMeasurement(int maxQueries) {
        this.mCompletedQueryCount = 0;
        this.mCollectedGpuDurations = new ArrayList<>();
        this.mCollectedCpuDurations = new ArrayList<>();
        this.mCollectedTimestamps = new ArrayList<>();
        this.mTimestampQueue = new LinkedList();
        this.mCpuDurationsQueue = new LinkedList();
        if (maxQueries >= 1) {
            this.mNativeContext = nativeCreateContext(maxQueries);
            return;
        }
        throw new IllegalArgumentException("maxQueries is less than 1");
    }

    public static boolean isGlTimingSupported() {
        return nativeQuerySupport();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0058, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005d, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x005e, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0061, code lost:
        throw r2;
     */
    public void dumpPerformanceData(String path) {
        try {
            BufferedWriter dump = new BufferedWriter(new FileWriter(path));
            dump.write("timestamp gpu_duration cpu_duration\n");
            for (int i = 0; i < this.mCollectedGpuDurations.size(); i++) {
                dump.write(String.format("%d %d %d\n", this.mCollectedTimestamps.get(i), this.mCollectedGpuDurations.get(i), this.mCollectedCpuDurations.get(i)));
            }
            this.mCollectedTimestamps.clear();
            this.mCollectedGpuDurations.clear();
            this.mCollectedCpuDurations.clear();
            dump.close();
        } catch (IOException e) {
            Log.e(TAG, "Error writing data dump to " + path + SettingsStringUtil.DELIMITER + e);
        }
    }

    public void startTimer() {
        nativeStartGlTimer(this.mNativeContext);
        this.mStartTimeNs = SystemClock.elapsedRealtimeNanos();
    }

    public void stopTimer() {
        this.mCpuDurationsQueue.add(Long.valueOf(SystemClock.elapsedRealtimeNanos() - this.mStartTimeNs));
        nativeStopGlTimer(this.mNativeContext);
        long duration = getNextGlDuration();
        if (duration > 0) {
            this.mCollectedGpuDurations.add(Long.valueOf(duration));
            long j = -1;
            this.mCollectedTimestamps.add(Long.valueOf(this.mTimestampQueue.isEmpty() ? -1 : this.mTimestampQueue.poll().longValue()));
            ArrayList<Long> arrayList = this.mCollectedCpuDurations;
            if (!this.mCpuDurationsQueue.isEmpty()) {
                j = this.mCpuDurationsQueue.poll().longValue();
            }
            arrayList.add(Long.valueOf(j));
        }
        if (duration == -2) {
            if (!this.mTimestampQueue.isEmpty()) {
                this.mTimestampQueue.poll();
            }
            if (!this.mCpuDurationsQueue.isEmpty()) {
                this.mCpuDurationsQueue.poll();
            }
        }
    }

    public void addTimestamp(long timestamp) {
        this.mTimestampQueue.add(Long.valueOf(timestamp));
    }

    private long getNextGlDuration() {
        long duration = nativeGetNextGlDuration(this.mNativeContext);
        if (duration > 0) {
            this.mCompletedQueryCount++;
        }
        return duration;
    }

    public int getCompletedQueryCount() {
        return this.mCompletedQueryCount;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        nativeDeleteContext(this.mNativeContext);
    }
}
