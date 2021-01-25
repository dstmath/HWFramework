package com.huawei.okio;

import java.util.concurrent.TimeUnit;

final class PushableTimeout extends Timeout {
    private long originalDeadlineNanoTime;
    private boolean originalHasDeadline;
    private long originalTimeoutNanos;
    private Timeout pushed;

    PushableTimeout() {
    }

    /* access modifiers changed from: package-private */
    public void push(Timeout pushed2) {
        this.pushed = pushed2;
        this.originalHasDeadline = pushed2.hasDeadline();
        this.originalDeadlineNanoTime = this.originalHasDeadline ? pushed2.deadlineNanoTime() : -1;
        this.originalTimeoutNanos = pushed2.timeoutNanos();
        pushed2.timeout(minTimeout(this.originalTimeoutNanos, timeoutNanos()), TimeUnit.NANOSECONDS);
        if (this.originalHasDeadline && hasDeadline()) {
            pushed2.deadlineNanoTime(Math.min(deadlineNanoTime(), this.originalDeadlineNanoTime));
        } else if (hasDeadline()) {
            pushed2.deadlineNanoTime(deadlineNanoTime());
        }
    }

    /* access modifiers changed from: package-private */
    public void pop() {
        this.pushed.timeout(this.originalTimeoutNanos, TimeUnit.NANOSECONDS);
        if (this.originalHasDeadline) {
            this.pushed.deadlineNanoTime(this.originalDeadlineNanoTime);
        } else {
            this.pushed.clearDeadline();
        }
    }
}
