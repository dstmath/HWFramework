package org.junit.runner.notification;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener.ThreadSafe;

@ThreadSafe
final class SynchronizedRunListener extends RunListener {
    private final RunListener listener;
    private final Object monitor;

    SynchronizedRunListener(RunListener listener, Object monitor) {
        this.listener = listener;
        this.monitor = monitor;
    }

    public void testRunStarted(Description description) throws Exception {
        synchronized (this.monitor) {
            this.listener.testRunStarted(description);
        }
    }

    public void testRunFinished(Result result) throws Exception {
        synchronized (this.monitor) {
            this.listener.testRunFinished(result);
        }
    }

    public void testStarted(Description description) throws Exception {
        synchronized (this.monitor) {
            this.listener.testStarted(description);
        }
    }

    public void testFinished(Description description) throws Exception {
        synchronized (this.monitor) {
            this.listener.testFinished(description);
        }
    }

    public void testFailure(Failure failure) throws Exception {
        synchronized (this.monitor) {
            this.listener.testFailure(failure);
        }
    }

    public void testAssumptionFailure(Failure failure) {
        synchronized (this.monitor) {
            this.listener.testAssumptionFailure(failure);
        }
    }

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
