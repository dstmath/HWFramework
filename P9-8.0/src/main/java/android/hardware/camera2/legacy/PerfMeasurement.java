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
        this.mNativeContext = nativeCreateContext(3);
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

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0091 A:{SYNTHETIC, Splitter: B:26:0x0091} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00a4 A:{Catch:{ IOException -> 0x0097 }} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0096 A:{SYNTHETIC, Splitter: B:29:0x0096} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dumpPerformanceData(String path) {
        IOException e;
        Throwable th;
        Throwable th2 = null;
        BufferedWriter dump = null;
        try {
            BufferedWriter dump2 = new BufferedWriter(new FileWriter(path));
            try {
                dump2.write("timestamp gpu_duration cpu_duration\n");
                for (int i = 0; i < this.mCollectedGpuDurations.size(); i++) {
                    dump2.write(String.format("%d %d %d\n", new Object[]{this.mCollectedTimestamps.get(i), this.mCollectedGpuDurations.get(i), this.mCollectedCpuDurations.get(i)}));
                }
                this.mCollectedTimestamps.clear();
                this.mCollectedGpuDurations.clear();
                this.mCollectedCpuDurations.clear();
                if (dump2 != null) {
                    try {
                        dump2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (IOException e2) {
                        e = e2;
                        dump = dump2;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                dump = dump2;
                if (dump != null) {
                    try {
                        dump.close();
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
            if (dump != null) {
            }
            if (th2 == null) {
            }
        }
    }

    public void startTimer() {
        nativeStartGlTimer(this.mNativeContext);
        this.mStartTimeNs = SystemClock.elapsedRealtimeNanos();
    }

    public void stopTimer() {
        long j = -1;
        this.mCpuDurationsQueue.add(Long.valueOf(SystemClock.elapsedRealtimeNanos() - this.mStartTimeNs));
        nativeStopGlTimer(this.mNativeContext);
        long duration = getNextGlDuration();
        if (duration > 0) {
            long j2;
            this.mCollectedGpuDurations.add(Long.valueOf(duration));
            ArrayList arrayList = this.mCollectedTimestamps;
            if (this.mTimestampQueue.isEmpty()) {
                j2 = -1;
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

    protected void finalize() {
        nativeDeleteContext(this.mNativeContext);
    }
}
