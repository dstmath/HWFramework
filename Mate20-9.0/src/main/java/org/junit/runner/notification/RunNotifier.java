package org.junit.runner.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

public class RunNotifier {
    /* access modifiers changed from: private */
    public final List<RunListener> listeners = new CopyOnWriteArrayList();
    private volatile boolean pleaseStop = false;

    private abstract class SafeNotifier {
        private final List<RunListener> currentListeners;

        /* access modifiers changed from: protected */
        public abstract void notifyListener(RunListener runListener) throws Exception;

        SafeNotifier(RunNotifier runNotifier) {
            this(runNotifier.listeners);
        }

        SafeNotifier(List<RunListener> currentListeners2) {
            this.currentListeners = currentListeners2;
        }

        /* access modifiers changed from: package-private */
        public void run() {
            int capacity = this.currentListeners.size();
            ArrayList<RunListener> safeListeners = new ArrayList<>(capacity);
            ArrayList<Failure> failures = new ArrayList<>(capacity);
            for (RunListener listener : this.currentListeners) {
                try {
                    notifyListener(listener);
                    safeListeners.add(listener);
                } catch (Exception e) {
                    failures.add(new Failure(Description.TEST_MECHANISM, e));
                }
            }
            RunNotifier.this.fireTestFailures(safeListeners, failures);
        }
    }

    public void addListener(RunListener listener) {
        if (listener != null) {
            this.listeners.add(wrapIfNotThreadSafe(listener));
            return;
        }
        throw new NullPointerException("Cannot add a null listener");
    }

    public void removeListener(RunListener listener) {
        if (listener != null) {
            this.listeners.remove(wrapIfNotThreadSafe(listener));
            return;
        }
        throw new NullPointerException("Cannot remove a null listener");
    }

    /* access modifiers changed from: package-private */
    public RunListener wrapIfNotThreadSafe(RunListener listener) {
        if (listener.getClass().isAnnotationPresent(RunListener.ThreadSafe.class)) {
            return listener;
        }
        return new SynchronizedRunListener(listener, this);
    }

    public void fireTestRunStarted(final Description description) {
        new SafeNotifier() {
            /* access modifiers changed from: protected */
            public void notifyListener(RunListener each) throws Exception {
                each.testRunStarted(description);
            }
        }.run();
    }

    public void fireTestRunFinished(final Result result) {
        new SafeNotifier() {
            /* access modifiers changed from: protected */
            public void notifyListener(RunListener each) throws Exception {
                each.testRunFinished(result);
            }
        }.run();
    }

    public void fireTestStarted(final Description description) throws StoppedByUserException {
        if (!this.pleaseStop) {
            new SafeNotifier() {
                /* access modifiers changed from: protected */
                public void notifyListener(RunListener each) throws Exception {
                    each.testStarted(description);
                }
            }.run();
            return;
        }
        throw new StoppedByUserException();
    }

    public void fireTestFailure(Failure failure) {
        fireTestFailures(this.listeners, Arrays.asList(new Failure[]{failure}));
    }

    /* access modifiers changed from: private */
    public void fireTestFailures(List<RunListener> listeners2, final List<Failure> failures) {
        if (!failures.isEmpty()) {
            new SafeNotifier(listeners2) {
                /* access modifiers changed from: protected */
                public void notifyListener(RunListener listener) throws Exception {
                    for (Failure each : failures) {
                        listener.testFailure(each);
                    }
                }
            }.run();
        }
    }

    public void fireTestAssumptionFailed(final Failure failure) {
        new SafeNotifier() {
            /* access modifiers changed from: protected */
            public void notifyListener(RunListener each) throws Exception {
                each.testAssumptionFailure(failure);
            }
        }.run();
    }

    public void fireTestIgnored(final Description description) {
        new SafeNotifier() {
            /* access modifiers changed from: protected */
            public void notifyListener(RunListener each) throws Exception {
                each.testIgnored(description);
            }
        }.run();
    }

    public void fireTestFinished(final Description description) {
        new SafeNotifier() {
            /* access modifiers changed from: protected */
            public void notifyListener(RunListener each) throws Exception {
                each.testFinished(description);
            }
        }.run();
    }

    public void pleaseStop() {
        this.pleaseStop = true;
    }

    public void addFirstListener(RunListener listener) {
        if (listener != null) {
            this.listeners.add(0, wrapIfNotThreadSafe(listener));
            return;
        }
        throw new NullPointerException("Cannot add a null listener");
    }
}
