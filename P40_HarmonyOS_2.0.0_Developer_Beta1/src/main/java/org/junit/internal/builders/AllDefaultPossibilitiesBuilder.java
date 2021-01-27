package org.junit.internal.builders;

import java.util.Arrays;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class AllDefaultPossibilitiesBuilder extends RunnerBuilder {
    private final boolean canUseSuiteMethod;

    public AllDefaultPossibilitiesBuilder(boolean canUseSuiteMethod2) {
        this.canUseSuiteMethod = canUseSuiteMethod2;
    }

    @Override // org.junit.runners.model.RunnerBuilder
    public Runner runnerForClass(Class<?> testClass) throws Throwable {
        for (RunnerBuilder each : Arrays.asList(ignoredBuilder(), annotatedBuilder(), suiteMethodBuilder(), junit3Builder(), junit4Builder())) {
            Runner runner = each.safeRunnerForClass(testClass);
            if (runner != null) {
                return runner;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public JUnit4Builder junit4Builder() {
        return new JUnit4Builder();
    }

    /* access modifiers changed from: protected */
    public JUnit3Builder junit3Builder() {
        return new JUnit3Builder();
    }

    /* access modifiers changed from: protected */
    public AnnotatedBuilder annotatedBuilder() {
        return new AnnotatedBuilder(this);
    }

    /* access modifiers changed from: protected */
    public IgnoredBuilder ignoredBuilder() {
        return new IgnoredBuilder();
    }

    /* access modifiers changed from: protected */
    public RunnerBuilder suiteMethodBuilder() {
        if (this.canUseSuiteMethod) {
            return new SuiteMethodBuilder();
        }
        return new NullBuilder();
    }
}
