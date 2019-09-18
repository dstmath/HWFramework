package com.android.internal.os;

import android.os.SystemClock;
import com.android.internal.os.KernelUidCpuTimeReaderBase.Callback;

public abstract class KernelUidCpuTimeReaderBase<T extends Callback> {
    protected static final boolean DEBUG = false;
    private static final long DEFAULT_THROTTLE_INTERVAL = 10000;
    private final String TAG = getClass().getSimpleName();
    private long mLastTimeReadMs = Long.MIN_VALUE;
    private long mThrottleInterval = DEFAULT_THROTTLE_INTERVAL;

    public interface Callback {
    }

    /* access modifiers changed from: protected */
    public abstract void readDeltaImpl(T t);

    public void readDelta(T cb) {
        if (SystemClock.elapsedRealtime() >= this.mLastTimeReadMs + this.mThrottleInterval) {
            readDeltaImpl(cb);
            this.mLastTimeReadMs = SystemClock.elapsedRealtime();
        }
    }

    public void setThrottleInterval(long throttleInterval) {
        if (throttleInterval >= 0) {
            this.mThrottleInterval = throttleInterval;
        }
    }
}
