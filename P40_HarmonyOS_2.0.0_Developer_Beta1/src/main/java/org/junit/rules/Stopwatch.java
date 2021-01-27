package org.junit.rules;

import java.util.concurrent.TimeUnit;
import org.junit.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public abstract class Stopwatch implements TestRule {
    private final Clock clock;
    private volatile long endNanos;
    private volatile long startNanos;

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
    /* access modifiers changed from: public */
    private long getNanos() {
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
    /* access modifiers changed from: public */
    private void starting() {
        this.startNanos = this.clock.nanoTime();
        this.endNanos = 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopping() {
        this.endNanos = this.clock.nanoTime();
    }

    @Override // org.junit.rules.TestRule
    public final Statement apply(Statement base, Description description) {
        return new InternalWatcher().apply(base, description);
    }

    private class InternalWatcher extends TestWatcher {
        private InternalWatcher() {
        }

        /* access modifiers changed from: protected */
        @Override // org.junit.rules.TestWatcher
        public void starting(Description description) {
            Stopwatch.this.starting();
        }

        /* access modifiers changed from: protected */
        @Override // org.junit.rules.TestWatcher
        public void finished(Description description) {
            Stopwatch stopwatch = Stopwatch.this;
            stopwatch.finished(stopwatch.getNanos(), description);
        }

        /* access modifiers changed from: protected */
        @Override // org.junit.rules.TestWatcher
        public void succeeded(Description description) {
            Stopwatch.this.stopping();
            Stopwatch stopwatch = Stopwatch.this;
            stopwatch.succeeded(stopwatch.getNanos(), description);
        }

        /* access modifiers changed from: protected */
        @Override // org.junit.rules.TestWatcher
        public void failed(Throwable e, Description description) {
            Stopwatch.this.stopping();
            Stopwatch stopwatch = Stopwatch.this;
            stopwatch.failed(stopwatch.getNanos(), e, description);
        }

        /* access modifiers changed from: protected */
        @Override // org.junit.rules.TestWatcher
        public void skipped(AssumptionViolatedException e, Description description) {
            Stopwatch.this.stopping();
            Stopwatch stopwatch = Stopwatch.this;
            stopwatch.skipped(stopwatch.getNanos(), e, description);
        }
    }

    /* access modifiers changed from: package-private */
    public static class Clock {
        Clock() {
        }

        public long nanoTime() {
            return System.nanoTime();
        }
    }
}
