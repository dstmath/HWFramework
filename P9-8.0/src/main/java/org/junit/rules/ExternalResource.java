package org.junit.rules;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public abstract class ExternalResource implements TestRule {
    public Statement apply(Statement base, Description description) {
        return statement(base);
    }

    private Statement statement(final Statement base) {
        return new Statement() {
            public void evaluate() throws Throwable {
                ExternalResource.this.before();
                try {
                    base.evaluate();
                } finally {
                    ExternalResource.this.after();
                }
            }
        };
    }

    protected void before() throws Throwable {
    }

    protected void after() {
    }
}
