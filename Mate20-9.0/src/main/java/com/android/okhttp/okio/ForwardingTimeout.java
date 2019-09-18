package com.android.okhttp.okio;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ForwardingTimeout extends Timeout {
    private Timeout delegate;

    public ForwardingTimeout(Timeout delegate2) {
        if (delegate2 != null) {
            this.delegate = delegate2;
            return;
        }
        throw new IllegalArgumentException("delegate == null");
    }

    public final Timeout delegate() {
        return this.delegate;
    }

    public final ForwardingTimeout setDelegate(Timeout delegate2) {
        if (delegate2 != null) {
            this.delegate = delegate2;
            return this;
        }
        throw new IllegalArgumentException("delegate == null");
    }

    public Timeout timeout(long timeout, TimeUnit unit) {
        return this.delegate.timeout(timeout, unit);
    }

    public long timeoutNanos() {
        return this.delegate.timeoutNanos();
    }

    public boolean hasDeadline() {
        return this.delegate.hasDeadline();
    }

    public long deadlineNanoTime() {
        return this.delegate.deadlineNanoTime();
    }

    public Timeout deadlineNanoTime(long deadlineNanoTime) {
        return this.delegate.deadlineNanoTime(deadlineNanoTime);
    }

    public Timeout clearTimeout() {
        return this.delegate.clearTimeout();
    }

    public Timeout clearDeadline() {
        return this.delegate.clearDeadline();
    }

    public void throwIfReached() throws IOException {
        this.delegate.throwIfReached();
    }
}
