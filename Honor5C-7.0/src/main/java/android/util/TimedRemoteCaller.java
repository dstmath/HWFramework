package android.util;

import android.os.SystemClock;
import java.util.concurrent.TimeoutException;

public abstract class TimedRemoteCaller<T> {
    public static final long DEFAULT_CALL_TIMEOUT_MILLIS = 5000;
    private static final int UNDEFINED_SEQUENCE = -1;
    private int mAwaitedSequence;
    private final long mCallTimeoutMillis;
    private final Object mLock;
    private int mReceivedSequence;
    private T mResult;
    private int mSequenceCounter;

    public TimedRemoteCaller(long callTimeoutMillis) {
        this.mLock = new Object();
        this.mReceivedSequence = UNDEFINED_SEQUENCE;
        this.mAwaitedSequence = UNDEFINED_SEQUENCE;
        this.mCallTimeoutMillis = callTimeoutMillis;
    }

    public final int onBeforeRemoteCall() {
        int i;
        synchronized (this.mLock) {
            i = this.mSequenceCounter;
            this.mSequenceCounter = i + 1;
            this.mAwaitedSequence = i;
            i = this.mAwaitedSequence;
        }
        return i;
    }

    public final T getResultTimed(int sequence) throws TimeoutException {
        T result;
        synchronized (this.mLock) {
            if (waitForResultTimedLocked(sequence)) {
                result = this.mResult;
                this.mResult = null;
            } else {
                throw new TimeoutException("No reponse for sequence: " + sequence);
            }
        }
        return result;
    }

    public final void onRemoteMethodResult(T result, int sequence) {
        synchronized (this.mLock) {
            if (sequence == this.mAwaitedSequence) {
                this.mReceivedSequence = sequence;
                this.mResult = result;
                this.mLock.notifyAll();
            }
        }
    }

    private boolean waitForResultTimedLocked(int sequence) {
        long startMillis = SystemClock.uptimeMillis();
        while (this.mReceivedSequence != sequence) {
            try {
                long waitMillis = this.mCallTimeoutMillis - (SystemClock.uptimeMillis() - startMillis);
                if (waitMillis <= 0) {
                    return false;
                }
                this.mLock.wait(waitMillis);
            } catch (InterruptedException e) {
            }
        }
        return true;
    }
}
