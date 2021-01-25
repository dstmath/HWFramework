package com.huawei.okio;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

public class Timeout {
    public static final Timeout NONE = new Timeout() {
        /* class com.huawei.okio.Timeout.AnonymousClass1 */

        @Override // com.huawei.okio.Timeout
        public Timeout timeout(long timeout, TimeUnit unit) {
            return this;
        }

        @Override // com.huawei.okio.Timeout
        public Timeout deadlineNanoTime(long deadlineNanoTime) {
            return this;
        }

        @Override // com.huawei.okio.Timeout
        public void throwIfReached() throws IOException {
        }
    };
    private long deadlineNanoTime;
    private boolean hasDeadline;
    private long timeoutNanos;

    public Timeout timeout(long timeout, TimeUnit unit) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout < 0: " + timeout);
        } else if (unit != null) {
            this.timeoutNanos = unit.toNanos(timeout);
            return this;
        } else {
            throw new IllegalArgumentException("unit == null");
        }
    }

    public long timeoutNanos() {
        return this.timeoutNanos;
    }

    public boolean hasDeadline() {
        return this.hasDeadline;
    }

    public long deadlineNanoTime() {
        if (this.hasDeadline) {
            return this.deadlineNanoTime;
        }
        throw new IllegalStateException("No deadline");
    }

    public Timeout deadlineNanoTime(long deadlineNanoTime2) {
        this.hasDeadline = true;
        this.deadlineNanoTime = deadlineNanoTime2;
        return this;
    }

    public final Timeout deadline(long duration, TimeUnit unit) {
        if (duration <= 0) {
            throw new IllegalArgumentException("duration <= 0: " + duration);
        } else if (unit != null) {
            return deadlineNanoTime(System.nanoTime() + unit.toNanos(duration));
        } else {
            throw new IllegalArgumentException("unit == null");
        }
    }

    public Timeout clearTimeout() {
        this.timeoutNanos = 0;
        return this;
    }

    public Timeout clearDeadline() {
        this.hasDeadline = false;
        return this;
    }

    public void throwIfReached() throws IOException {
        if (Thread.interrupted()) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("interrupted");
        } else if (this.hasDeadline && this.deadlineNanoTime - System.nanoTime() <= 0) {
            throw new InterruptedIOException("deadline reached");
        }
    }

    public final void waitUntilNotified(Object monitor) throws InterruptedIOException {
        long deadlineNanos;
        try {
            boolean hasDeadline2 = hasDeadline();
            long timeoutNanos2 = timeoutNanos();
            if (hasDeadline2 || timeoutNanos2 != 0) {
                long start = System.nanoTime();
                if (hasDeadline2 && timeoutNanos2 != 0) {
                    deadlineNanos = Math.min(timeoutNanos2, deadlineNanoTime() - start);
                } else if (hasDeadline2) {
                    deadlineNanos = deadlineNanoTime() - start;
                } else {
                    deadlineNanos = timeoutNanos2;
                }
                long elapsedNanos = 0;
                if (deadlineNanos > 0) {
                    long waitMillis = deadlineNanos / 1000000;
                    monitor.wait(waitMillis, (int) (deadlineNanos - (1000000 * waitMillis)));
                    elapsedNanos = System.nanoTime() - start;
                }
                if (elapsedNanos >= deadlineNanos) {
                    throw new InterruptedIOException("timeout");
                }
                return;
            }
            monitor.wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("interrupted");
        }
    }

    static long minTimeout(long aNanos, long bNanos) {
        if (aNanos == 0) {
            return bNanos;
        }
        if (bNanos != 0 && aNanos >= bNanos) {
            return bNanos;
        }
        return aNanos;
    }
}
