package org.junit.runner.notification;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

/* access modifiers changed from: package-private */
@RunListener.ThreadSafe
public final class SynchronizedRunListener extends RunListener {
    private final RunListener listener;
    private final Object monitor;

    SynchronizedRunListener(RunListener listener2, Object monitor2) {
        this.listener = listener2;
        this.monitor = monitor2;
    }

    @Override // org.junit.runner.notification.RunListener
    public void testRunStarted(Description description) throws Exception {
        synchronized (this.monitor) {
            this.listener.testRunStarted(description);
        }
    }

    @Override // org.junit.runner.notification.RunListener
    public void testRunFinished(Result result) throws Exception {
        synchronized (this.monitor) {
            this.listener.testRunFinished(result);
        }
    }

    @Override // org.junit.runner.notification.RunListener
    public void testStarted(Description description) throws Exception {
        synchronized (this.monitor) {
            this.listener.testStarted(description);
        }
    }

    @Override // org.junit.runner.notification.RunListener
    public void testFinished(Description description) throws Exception {
        synchronized (this.monitor) {
            this.listener.testFinished(description);
        }
    }

    @Override // org.junit.runner.notification.RunListener
    public void testFailure(Failure failure) throws Exception {
        synchronized (this.monitor) {
            this.listener.testFailure(failure);
        }
    }

    @Override // org.junit.runner.notification.RunListener
    public void testAssumptionFailure(Failure failure) {
        synchronized (this.monitor) {
            this.listener.testAssumptionFailure(failure);
        }
    }

    @Override // org.junit.runner.notification.RunListener
    public void testIgnored(Description description) throws Exception {
        synchronized (this.monitor) {
            this.listener.testIgnored(description);
        }
    }

    public int hashCode() {
        return this.listener.hashCode();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SynchronizedRunListener)) {
            return false;
        }
        return this.listener.equals(((SynchronizedRunListener) other).listener);
    }

    public String toString() {
        return this.listener.toString() + " (with synchronization wrapper)";
    }
}
