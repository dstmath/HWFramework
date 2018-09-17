package org.junit.rules;

import java.util.concurrent.TimeUnit;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class Timeout implements TestRule {
    private final TimeUnit timeUnit;
    private final long timeout;

    public static class Builder {
        private boolean lookForStuckThread = false;
        private TimeUnit timeUnit = TimeUnit.SECONDS;
        private long timeout = 0;

        protected Builder() {
        }

        public Builder withTimeout(long timeout, TimeUnit unit) {
            this.timeout = timeout;
            this.timeUnit = unit;
            return this;
        }

        protected long getTimeout() {
            return this.timeout;
        }

        protected TimeUnit getTimeUnit() {
            return this.timeUnit;
        }

        public Timeout build() {
            return new Timeout(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Deprecated
    public Timeout(int millis) {
        this((long) millis, TimeUnit.MILLISECONDS);
    }

    public Timeout(long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    protected Timeout(Builder builder) {
        this.timeout = builder.getTimeout();
        this.timeUnit = builder.getTimeUnit();
    }

    public static Timeout millis(long millis) {
        return new Timeout(millis, TimeUnit.MILLISECONDS);
    }

    public static Timeout seconds(long seconds) {
        return new Timeout(seconds, TimeUnit.SECONDS);
    }

    protected final long getTimeout(TimeUnit unit) {
        return unit.convert(this.timeout, this.timeUnit);
    }

    protected Statement createFailOnTimeoutStatement(Statement statement) throws Exception {
        return FailOnTimeout.builder().withTimeout(this.timeout, this.timeUnit).build(statement);
    }

    public Statement apply(Statement base, Description description) {
        try {
            return createFailOnTimeoutStatement(base);
        } catch (final Exception e) {
            return new Statement() {
                public void evaluate() throws Throwable {
                    throw new RuntimeException("Invalid parameters for Timeout", e);
                }
            };
        }
    }
}
