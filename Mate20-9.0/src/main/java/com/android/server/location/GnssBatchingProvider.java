package com.android.server.location;

import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;

public class GnssBatchingProvider {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "GnssBatchingProvider";
    private boolean mEnabled;
    private final GnssBatchingProviderNative mNative;
    private long mPeriodNanos;
    private boolean mStarted;
    private boolean mWakeOnFifoFull;

    @VisibleForTesting
    static class GnssBatchingProviderNative {
        GnssBatchingProviderNative() {
        }

        public int getBatchSize() {
            return GnssBatchingProvider.native_get_batch_size();
        }

        public boolean startBatch(long periodNanos, boolean wakeOnFifoFull) {
            return GnssBatchingProvider.native_start_batch(periodNanos, wakeOnFifoFull);
        }

        public void flushBatch() {
            GnssBatchingProvider.native_flush_batch();
        }

        public boolean stopBatch() {
            return GnssBatchingProvider.native_stop_batch();
        }

        public boolean initBatching() {
            return GnssBatchingProvider.native_init_batching();
        }

        public void cleanupBatching() {
            GnssBatchingProvider.native_cleanup_batching();
        }
    }

    /* access modifiers changed from: private */
    public static native void native_cleanup_batching();

    /* access modifiers changed from: private */
    public static native void native_flush_batch();

    /* access modifiers changed from: private */
    public static native int native_get_batch_size();

    /* access modifiers changed from: private */
    public static native boolean native_init_batching();

    /* access modifiers changed from: private */
    public static native boolean native_start_batch(long j, boolean z);

    /* access modifiers changed from: private */
    public static native boolean native_stop_batch();

    GnssBatchingProvider() {
        this(new GnssBatchingProviderNative());
    }

    @VisibleForTesting
    GnssBatchingProvider(GnssBatchingProviderNative gnssBatchingProviderNative) {
        this.mNative = gnssBatchingProviderNative;
    }

    public int getBatchSize() {
        return this.mNative.getBatchSize();
    }

    public void enable() {
        this.mEnabled = this.mNative.initBatching();
        if (!this.mEnabled) {
            Log.e(TAG, "Failed to initialize GNSS batching");
        }
    }

    public boolean start(long periodNanos, boolean wakeOnFifoFull) {
        if (!this.mEnabled) {
            throw new IllegalStateException();
        } else if (periodNanos <= 0) {
            Log.e(TAG, "Invalid periodNanos " + periodNanos + " in batching request, not started");
            return false;
        } else {
            this.mStarted = this.mNative.startBatch(periodNanos, wakeOnFifoFull);
            if (this.mStarted) {
                this.mPeriodNanos = periodNanos;
                this.mWakeOnFifoFull = wakeOnFifoFull;
            }
            return this.mStarted;
        }
    }

    public void flush() {
        if (!this.mStarted) {
            Log.w(TAG, "Cannot flush since GNSS batching has not started.");
        } else {
            this.mNative.flushBatch();
        }
    }

    public boolean stop() {
        boolean stopped = this.mNative.stopBatch();
        if (stopped) {
            this.mStarted = false;
        }
        return stopped;
    }

    public void disable() {
        stop();
        this.mNative.cleanupBatching();
        this.mEnabled = false;
    }

    /* access modifiers changed from: package-private */
    public void resumeIfStarted() {
        if (DEBUG) {
            Log.d(TAG, "resumeIfStarted");
        }
        if (this.mStarted) {
            this.mNative.startBatch(this.mPeriodNanos, this.mWakeOnFifoFull);
        }
    }
}
