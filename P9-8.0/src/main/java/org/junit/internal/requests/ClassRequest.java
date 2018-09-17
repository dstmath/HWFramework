package org.junit.internal.requests;

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Request;
import org.junit.runner.Runner;

public class ClassRequest extends Request {
    private final boolean canUseSuiteMethod;
    private final Class<?> fTestClass;
    private volatile Runner runner;
    private final Object runnerLock;

    public ClassRequest(Class<?> testClass, boolean canUseSuiteMethod) {
        this.runnerLock = new Object();
        this.fTestClass = testClass;
        this.canUseSuiteMethod = canUseSuiteMethod;
    }

    public ClassRequest(Class<?> testClass) {
        this(testClass, true);
    }

    public Runner getRunner() {
        if (this.runner == null) {
            synchronized (this.runnerLock) {
                if (this.runner == null) {
                    this.runner = new AllDefaultPossibilitiesBuilder(this.canUseSuiteMethod).safeRunnerForClass(this.fTestClass);
                }
            }
        }
        return this.runner;
    }
}
