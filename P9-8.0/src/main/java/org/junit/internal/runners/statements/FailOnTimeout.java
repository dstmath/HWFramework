package org.junit.internal.runners.statements;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestTimedOutException;

public class FailOnTimeout extends Statement {
    private final Statement originalStatement;
    private final TimeUnit timeUnit;
    private final long timeout;

    public static class Builder {
        private long timeout;
        private TimeUnit unit;

        /* synthetic */ Builder(Builder -this0) {
            this();
        }

        private Builder() {
            this.timeout = 0;
            this.unit = TimeUnit.SECONDS;
        }

        public Builder withTimeout(long timeout, TimeUnit unit) {
            if (timeout < 0) {
                throw new IllegalArgumentException("timeout must be non-negative");
            } else if (unit == null) {
                throw new NullPointerException("TimeUnit cannot be null");
            } else {
                this.timeout = timeout;
                this.unit = unit;
                return this;
            }
        }

        public FailOnTimeout build(Statement statement) {
            if (statement != null) {
                return new FailOnTimeout(this, statement, null);
            }
            throw new NullPointerException("statement cannot be null");
        }
    }

    private class CallableStatement implements Callable<Throwable> {
        private final CountDownLatch startLatch;

        /* synthetic */ CallableStatement(FailOnTimeout this$0, CallableStatement -this1) {
            this();
        }

        private CallableStatement() {
            this.startLatch = new CountDownLatch(1);
        }

        public Throwable call() throws Exception {
            try {
                this.startLatch.countDown();
                FailOnTimeout.this.originalStatement.evaluate();
                return null;
            } catch (Exception e) {
                throw e;
            } catch (Throwable e2) {
                return e2;
            }
        }

        public void awaitStarted() throws InterruptedException {
            this.startLatch.await();
        }
    }

    /* synthetic */ FailOnTimeout(Builder builder, Statement statement, FailOnTimeout -this2) {
        this(builder, statement);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Deprecated
    public FailOnTimeout(Statement statement, long timeoutMillis) {
        this(builder().withTimeout(timeoutMillis, TimeUnit.MILLISECONDS), statement);
    }

    private FailOnTimeout(Builder builder, Statement statement) {
        this.originalStatement = statement;
        this.timeout = builder.timeout;
        this.timeUnit = builder.unit;
    }

    public void evaluate() throws Throwable {
        CallableStatement callable = new CallableStatement(this, null);
        FutureTask<Throwable> task = new FutureTask(callable);
        Thread thread = new Thread(task, "Time-limited test");
        thread.setDaemon(true);
        thread.start();
        callable.awaitStarted();
        Throwable throwable = getResult(task, thread);
        if (throwable != null) {
            throw throwable;
        }
    }

    private Throwable getResult(FutureTask<Throwable> task, Thread thread) {
        try {
            if (this.timeout > 0) {
                return (Throwable) task.get(this.timeout, this.timeUnit);
            }
            return (Throwable) task.get();
        } catch (InterruptedException e) {
            return e;
        } catch (ExecutionException e2) {
            return e2.getCause();
        } catch (TimeoutException e3) {
            return createTimeoutException(thread);
        }
    }

    private Exception createTimeoutException(Thread thread) {
        StackTraceElement[] stackTrace = thread.getStackTrace();
        Exception currThreadException = new TestTimedOutException(this.timeout, this.timeUnit);
        if (stackTrace != null) {
            currThreadException.setStackTrace(stackTrace);
            thread.interrupt();
        }
        return currThreadException;
    }
}
