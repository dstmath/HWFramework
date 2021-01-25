package org.junit.experimental;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.runner.Computer;
import org.junit.runner.Runner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.RunnerScheduler;

public class ParallelComputer extends Computer {
    private final boolean classes;
    private final boolean methods;

    public ParallelComputer(boolean classes2, boolean methods2) {
        this.classes = classes2;
        this.methods = methods2;
    }

    public static Computer classes() {
        return new ParallelComputer(true, false);
    }

    public static Computer methods() {
        return new ParallelComputer(false, true);
    }

    private static Runner parallelize(Runner runner) {
        if (runner instanceof ParentRunner) {
            ((ParentRunner) runner).setScheduler(new RunnerScheduler() {
                /* class org.junit.experimental.ParallelComputer.AnonymousClass1 */
                private final ExecutorService fService = Executors.newCachedThreadPool();

                @Override // org.junit.runners.model.RunnerScheduler
                public void schedule(Runnable childStatement) {
                    this.fService.submit(childStatement);
                }

                @Override // org.junit.runners.model.RunnerScheduler
                public void finished() {
                    try {
                        this.fService.shutdown();
                        this.fService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.err);
                    }
                }
            });
        }
        return runner;
    }

    @Override // org.junit.runner.Computer
    public Runner getSuite(RunnerBuilder builder, Class<?>[] classes2) throws InitializationError {
        Runner suite = super.getSuite(builder, classes2);
        return this.classes ? parallelize(suite) : suite;
    }

    /* access modifiers changed from: protected */
    @Override // org.junit.runner.Computer
    public Runner getRunner(RunnerBuilder builder, Class<?> testClass) throws Throwable {
        Runner runner = super.getRunner(builder, testClass);
        return this.methods ? parallelize(runner) : runner;
    }
}
