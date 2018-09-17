package android.util;

import android.os.SystemClock;
import com.android.internal.annotations.GuardedBy;
import java.util.concurrent.TimeoutException;

public abstract class TimedRemoteCaller<T> {
    public static final long DEFAULT_CALL_TIMEOUT_MILLIS = 5000;
    @GuardedBy("mLock")
    private final SparseIntArray mAwaitedCalls = new SparseIntArray(1);
    private final long mCallTimeoutMillis;
    private final Object mLock = new Object();
    @GuardedBy("mLock")
    private final SparseArray<T> mReceivedCalls = new SparseArray(1);
    @GuardedBy("mLock")
    private int mSequenceCounter;

    public TimedRemoteCaller(long callTimeoutMillis) {
        this.mCallTimeoutMillis = callTimeoutMillis;
    }

    protected final int onBeforeRemoteCall() {
        int sequenceId;
        synchronized (this.mLock) {
            do {
                sequenceId = this.mSequenceCounter;
                this.mSequenceCounter = sequenceId + 1;
            } while (this.mAwaitedCalls.get(sequenceId) != 0);
            this.mAwaitedCalls.put(sequenceId, 1);
        }
        return sequenceId;
    }

    protected final void onRemoteMethodResult(T result, int sequence) {
        synchronized (this.mLock) {
            if (this.mAwaitedCalls.get(sequence) != 0) {
                this.mAwaitedCalls.delete(sequence);
                this.mReceivedCalls.put(sequence, result);
                this.mLock.notifyAll();
            }
        }
    }

    protected final T getResultTimed(int sequence) throws TimeoutException {
        long startMillis = SystemClock.uptimeMillis();
        while (true) {
            try {
                synchronized (this.mLock) {
                    if (this.mReceivedCalls.indexOfKey(sequence) >= 0) {
                        T removeReturnOld = this.mReceivedCalls.removeReturnOld(sequence);
                        return removeReturnOld;
                    }
                    long waitMillis = this.mCallTimeoutMillis - (SystemClock.uptimeMillis() - startMillis);
                    if (waitMillis <= 0) {
                        this.mAwaitedCalls.delete(sequence);
                        throw new TimeoutException("No response for sequence: " + sequence);
                    }
                    this.mLock.wait(waitMillis);
                }
            } catch (InterruptedException e) {
            }
        }
    }
}
