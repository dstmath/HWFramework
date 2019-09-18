package org.junit.rules;

import java.util.concurrent.TimeUnit;
import org.junit.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public abstract class Stopwatch implements TestRule {
    private final Clock clock;
    private volatile long endNanos;
    private volatile long startNanos;

    static class Clock {
        Clock() {
        }

        public long nanoTime() {
            return System.nanoTime();
        }
    }

    private class InternalWatcher extends TestWatcher {
        private InternalWatcher() {
        }

        /* access modifiers changed from: protected */
        public void starting(Description description) {
            Stopwatch.this.starting();
        }

        /* access modifiers changed from: protected */
        public void finished(Description description) {
            Stopwatch.this.finished(Stopwatch.this.getNanos(), description);
        }

        /* access modifiers changed from: protected */
        public void succeeded(Description description) {
            Stopwatch.this.stopping();
            Stopwatch.this.succeeded(Stopwatch.this.getNanos(), description);
        }

        /* access modifiers changed from: protected */
        public void failed(Throwable e, Description description) {
            Stopwatch.this.stopping();
            Stopwatch.this.failed(Stopwatch.this.getNanos(), e, description);
        }

        /* access modifiers changed from: protected */
        public void skipped(AssumptionViolatedException e, Description description) {
            Stopwatch.this.stopping();
            Stopwatch.this.skipped(Stopwatch.this.getNanos(), e, description);
        }
    }

    public Stopwatch() {
        this(new Clock());
    }

    Stopwatch(Clock clock2) {
        this.clock = clock2;
    }

    public long runtime(TimeUnit unit) {
        return unit.convert(getNanos(), TimeUnit.NANOSECONDS);
    }

    /* access modifiers changed from: protected */
    public void succeeded(long nanos, Description description) {
    }

    /* access modifiers changed from: protected */
    public void failed(long nanos, Throwable e, Description description) {
    }

    /* access modifiers changed from: protected */
    public void skipped(long nanos, AssumptionViolatedException e, Description description) {
    }

    /* access modifiers changed from: protected */
    public void finished(long nanos, Description description) {
    }

    /* access modifiers changed from: private */
    public long getNanos() {
        if (this.startNanos != 0) {
            long currentEndNanos = this.endNanos;
            if (currentEndNanos == 0) {
                currentEndNanos = this.clock.nanoTime();
            }
            return currentEndNanos - this.startNanos;
        }
        throw new IllegalStateException("Test has not started");
    }

    /* access modifiers changed from: private */
    public void starting() {
        this.startNanos = this.clock.nanoTime();
        this.endNanos = 0;
    }

    /* access modifiers changed from: private */
    public void stopping() {
        this.endNanos = this.clock.nanoTime();
    }

    public final Statement apply(Statement base, Description description) {
        return new InternalWatcher().apply(base, description);
    }
}
