package org.junit.runners.model;

public interface RunnerScheduler {
    void finished();

    void schedule(Runnable runnable);
}
