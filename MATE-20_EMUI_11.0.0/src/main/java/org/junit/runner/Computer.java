package org.junit.runner;

import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class Computer {
    public static Computer serial() {
        return new Computer();
    }

    public Runner getSuite(final RunnerBuilder builder, Class<?>[] classes) throws InitializationError {
        return new Suite(new RunnerBuilder() {
            /* class org.junit.runner.Computer.AnonymousClass1 */

            @Override // org.junit.runners.model.RunnerBuilder
            public Runner runnerForClass(Class<?> testClass) throws Throwable {
                return Computer.this.getRunner(builder, testClass);
            }
        }, classes);
    }

    /* access modifiers changed from: protected */
    public Runner getRunner(RunnerBuilder builder, Class<?> testClass) throws Throwable {
        return builder.runnerForClass(testClass);
    }
}
