package org.junit.internal.runners.model;

import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.MultipleFailureException;

public class EachTestNotifier {
    private final Description description;
    private final RunNotifier notifier;

    public EachTestNotifier(RunNotifier notifier, Description description) {
        this.notifier = notifier;
        this.description = description;
    }

    public void addFailure(Throwable targetException) {
        if (targetException instanceof MultipleFailureException) {
            addMultipleFailureException((MultipleFailureException) targetException);
        } else {
            this.notifier.fireTestFailure(new Failure(this.description, targetException));
        }
    }

    private void addMultipleFailureException(MultipleFailureException mfe) {
        for (Throwable each : mfe.getFailures()) {
            addFailure(each);
        }
    }

    public void addFailedAssumption(AssumptionViolatedException e) {
        this.notifier.fireTestAssumptionFailed(new Failure(this.description, e));
    }

    public void fireTestFinished() {
        this.notifier.fireTestFinished(this.description);
    }

    public void fireTestStarted() {
        this.notifier.fireTestStarted(this.description);
    }

    public void fireTestIgnored() {
        this.notifier.fireTestIgnored(this.description);
    }
}
