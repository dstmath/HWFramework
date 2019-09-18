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

    public ClassRoadie(RunNotifier notifier2, TestClass testClass2, Description description2, Runnable runnable2) {
        this.notifier = notifier2;
        this.testClass = testClass2;
        this.description = description2;
        this.runnable = runnable2;
    }

    /* access modifiers changed from: protected */
    public void runUnprotected() {
        this.runnable.run();
    }

    /* access modifiers changed from: protected */
    public void addFailure(Throwable targetException) {
        this.notifier.fireTestFailure(new Failure(this.description, targetException));
    }

    public void runProtected() {
        try {
            runBefores();
            runUnprotected();
        } catch (FailedBefore e) {
        } catch (Throwable th) {
            runAfters();
            throw th;
        }
        runAfters();
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
            throw new FailedBefore();
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
