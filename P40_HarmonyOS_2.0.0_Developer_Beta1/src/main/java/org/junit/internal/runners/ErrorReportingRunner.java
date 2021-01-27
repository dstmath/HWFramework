package org.junit.internal.runners;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

public class ErrorReportingRunner extends Runner {
    private final List<Throwable> causes;
    private final Class<?> testClass;

    public ErrorReportingRunner(Class<?> testClass2, Throwable cause) {
        if (testClass2 != null) {
            this.testClass = testClass2;
            this.causes = getCauses(cause);
            return;
        }
        throw new NullPointerException("Test class cannot be null");
    }

    @Override // org.junit.runner.Runner, org.junit.runner.Describable
    public Description getDescription() {
        Description description = Description.createSuiteDescription(this.testClass);
        for (Throwable each : this.causes) {
            description.addChild(describeCause(each));
        }
        return description;
    }

    @Override // org.junit.runner.Runner
    public void run(RunNotifier notifier) {
        for (Throwable each : this.causes) {
            runCause(each, notifier);
        }
    }

    private List<Throwable> getCauses(Throwable cause) {
        if (cause instanceof InvocationTargetException) {
            return getCauses(cause.getCause());
        }
        if (cause instanceof InitializationError) {
            return ((InitializationError) cause).getCauses();
        }
        if (cause instanceof InitializationError) {
            return ((InitializationError) cause).getCauses();
        }
        return Arrays.asList(cause);
    }

    private Description describeCause(Throwable child) {
        return Description.createTestDescription(this.testClass, "initializationError");
    }

    private void runCause(Throwable child, RunNotifier notifier) {
        Description description = describeCause(child);
        notifier.fireTestStarted(description);
        notifier.fireTestFailure(new Failure(description, child));
        notifier.fireTestFinished(description);
    }
}
