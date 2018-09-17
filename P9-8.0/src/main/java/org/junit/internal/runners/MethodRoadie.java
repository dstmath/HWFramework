package org.junit.internal.runners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.TestTimedOutException;

@Deprecated
public class MethodRoadie {
    private final Description description;
    private final RunNotifier notifier;
    private final Object test;
    private TestMethod testMethod;

    public MethodRoadie(Object test, TestMethod method, RunNotifier notifier, Description description) {
        this.test = test;
        this.notifier = notifier;
        this.description = description;
        this.testMethod = method;
    }

    public void run() {
        if (this.testMethod.isIgnored()) {
            this.notifier.fireTestIgnored(this.description);
            return;
        }
        this.notifier.fireTestStarted(this.description);
        try {
            long timeout = this.testMethod.getTimeout();
            if (timeout > 0) {
                runWithTimeout(timeout);
            } else {
                runTest();
            }
            this.notifier.fireTestFinished(this.description);
        } catch (Throwable th) {
            this.notifier.fireTestFinished(this.description);
        }
    }

    private void runWithTimeout(final long timeout) {
        runBeforesThenTestThenAfters(new Runnable() {
            public void run() {
                ExecutorService service = Executors.newSingleThreadExecutor();
                Future<Object> result = service.submit(new Callable<Object>() {
                    public Object call() throws Exception {
                        MethodRoadie.this.runTestMethod();
                        return null;
                    }
                });
                service.shutdown();
                try {
                    if (!service.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                        service.shutdownNow();
                    }
                    result.get(0, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    MethodRoadie.this.addFailure(new TestTimedOutException(timeout, TimeUnit.MILLISECONDS));
                } catch (Exception e2) {
                    MethodRoadie.this.addFailure(e2);
                }
            }
        });
    }

    public void runTest() {
        runBeforesThenTestThenAfters(new Runnable() {
            public void run() {
                MethodRoadie.this.runTestMethod();
            }
        });
    }

    public void runBeforesThenTestThenAfters(Runnable test) {
        try {
            runBefores();
            test.run();
            runAfters();
        } catch (FailedBefore e) {
            runAfters();
        } catch (Exception e2) {
            throw new RuntimeException("test should never throw an exception to this level");
        } catch (Throwable th) {
            runAfters();
        }
    }

    protected void runTestMethod() {
        try {
            this.testMethod.invoke(this.test);
            if (this.testMethod.expectsException()) {
                addFailure(new AssertionError("Expected exception: " + this.testMethod.getExpectedException().getName()));
            }
        } catch (InvocationTargetException e) {
            Throwable actual = e.getTargetException();
            if (!(actual instanceof AssumptionViolatedException)) {
                if (!this.testMethod.expectsException()) {
                    addFailure(actual);
                } else if (this.testMethod.isUnexpected(actual)) {
                    addFailure(new Exception("Unexpected exception, expected<" + this.testMethod.getExpectedException().getName() + "> but was<" + actual.getClass().getName() + ">", actual));
                }
            }
        } catch (Throwable e2) {
            addFailure(e2);
        }
    }

    private void runBefores() throws FailedBefore {
        try {
            for (Method before : this.testMethod.getBefores()) {
                before.invoke(this.test, new Object[0]);
            }
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (AssumptionViolatedException e2) {
            throw new FailedBefore();
        } catch (Throwable e3) {
            addFailure(e3);
            FailedBefore failedBefore = new FailedBefore();
        }
    }

    private void runAfters() {
        for (Method after : this.testMethod.getAfters()) {
            try {
                after.invoke(this.test, new Object[0]);
            } catch (InvocationTargetException e) {
                addFailure(e.getTargetException());
            } catch (Throwable e2) {
                addFailure(e2);
            }
        }
    }

    protected void addFailure(Throwable e) {
        this.notifier.fireTestFailure(new Failure(this.description, e));
    }
}
