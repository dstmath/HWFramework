package android.hardware.camera2.legacy;

import android.os.SystemClock;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

class PerfMeasurement {
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
        this.mCollectedGpuDurations = new ArrayList();
        this.mCollectedCpuDurations = new ArrayList();
        this.mCollectedTimestamps = new ArrayList();
        this.mTimestampQueue = new LinkedList();
        this.mCpuDurationsQueue = new LinkedList();
        this.mNativeContext = nativeCreateContext(DEFAULT_MAX_QUERIES);
    }

    public PerfMeasurement(int maxQueries) {
        this.mCompletedQueryCount = 0;
        this.mCollectedGpuDurations = new ArrayList();
        this.mCollectedCpuDurations = new ArrayList();
        this.mCollectedTimestamps = new ArrayList();
        this.mTimestampQueue = new LinkedList();
        this.mCpuDurationsQueue = new LinkedList();
        if (maxQueries < 1) {
            throw new IllegalArgumentException("maxQueries is less than 1");
        }
        this.mNativeContext = nativeCreateContext(maxQueries);
    }

    public static boolean isGlTimingSupported() {
        return nativeQuerySupport();
    }

    public void dumpPerformanceData(String path) {
        IOException e;
        Throwable th;
        Throwable th2 = null;
        BufferedWriter bufferedWriter = null;
        try {
            BufferedWriter dump = new BufferedWriter(new FileWriter(path));
            try {
                dump.write("timestamp gpu_duration cpu_duration\n");
                for (int i = 0; i < this.mCollectedGpuDurations.size(); i++) {
                    Object[] objArr = new Object[DEFAULT_MAX_QUERIES];
                    objArr[0] = this.mCollectedTimestamps.get(i);
                    objArr[1] = this.mCollectedGpuDurations.get(i);
                    objArr[2] = this.mCollectedCpuDurations.get(i);
                    dump.write(String.format("%d %d %d\n", objArr));
                }
                this.mCollectedTimestamps.clear();
                this.mCollectedGpuDurations.clear();
                this.mCollectedCpuDurations.clear();
                if (dump != null) {
                    try {
                        dump.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (IOException e2) {
                        e = e2;
                        bufferedWriter = dump;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                bufferedWriter = dump;
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    try {
                        throw th2;
                    } catch (IOException e3) {
                        e = e3;
                        Log.e(TAG, "Error writing data dump to " + path + ":" + e);
                        return;
                    }
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (th2 == null) {
                throw th;
            }
            throw th2;
        }
    }

    public void startTimer() {
        nativeStartGlTimer(this.mNativeContext);
        this.mStartTimeNs = SystemClock.elapsedRealtimeNanos();
    }

    public void stopTimer() {
        long j = NO_DURATION_YET;
        this.mCpuDurationsQueue.add(Long.valueOf(SystemClock.elapsedRealtimeNanos() - this.mStartTimeNs));
        nativeStopGlTimer(this.mNativeContext);
        long duration = getNextGlDuration();
        if (duration > 0) {
            long j2;
            this.mCollectedGpuDurations.add(Long.valueOf(duration));
            ArrayList arrayList = this.mCollectedTimestamps;
            if (this.mTimestampQueue.isEmpty()) {
                j2 = NO_DURATION_YET;
            } else {
                j2 = ((Long) this.mTimestampQueue.poll()).longValue();
            }
            arrayList.add(Long.valueOf(j2));
            ArrayList arrayList2 = this.mCollectedCpuDurations;
            if (!this.mCpuDurationsQueue.isEmpty()) {
                j = ((Long) this.mCpuDurationsQueue.poll()).longValue();
            }
            arrayList2.add(Long.valueOf(j));
        }
        if (duration == FAILED_TIMING) {
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

    protected void finalize() {
        nativeDeleteContext(this.mNativeContext);
    }
}
