package org.junit.internal.runners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

@Deprecated
public class ClassRoadie {
    private Description description;
    private RunNotifier notifier;
    private final Runnable runnable;
    private TestClass testClass;

    public ClassRoadie(RunNotifier notifier, TestClass testClass, Description description, Runnable runnable) {
        this.notifier = notifier;
        this.testClass = testClass;
        this.description = description;
        this.runnable = runnable;
    }

    protected void runUnprotected() {
        this.runnable.run();
    }

    protected void addFailure(Throwable targetException) {
        this.notifier.fireTestFailure(new Failure(this.description, targetException));
    }

    public void runProtected() {
        try {
            runBefores();
            runUnprotected();
        } catch (FailedBefore e) {
        } finally {
            runAfters();
        }
    }

    private void runBefores() throws FailedBefore {
        try {
            for (Method before : this.testClass.getBefores()) {
                before.invoke(null, new Object[0]);
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
        for (Method after : this.testClass.getAfters()) {
            try {
                after.invoke(null, new Object[0]);
            } catch (InvocationTargetException e) {
                addFailure(e.getTargetException());
            } catch (Throwable e2) {
                addFailure(e2);
            }
        }
    }
}
