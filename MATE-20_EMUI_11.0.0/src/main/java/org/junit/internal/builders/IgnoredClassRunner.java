package org.junit.internal.builders;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

public class IgnoredClassRunner extends Runner {
    private final Class<?> clazz;

    public IgnoredClassRunner(Class<?> testClass) {
        this.clazz = testClass;
    }

    @Override // org.junit.runner.Runner
    public void run(RunNotifier notifier) {
        notifier.fireTestIgnored(getDescription());
    }

    @Override // org.junit.runner.Runner, org.junit.runner.Describable
    public Description getDescription() {
        return Description.createSuiteDescription(this.clazz);
    }
}
