package org.junit.rules;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public abstract class ExternalResource implements TestRule {
    @Override // org.junit.rules.TestRule
    public Statement apply(Statement base, Description description) {
        return statement(base);
    }

    private Statement statement(final Statement base) {
        return new Statement() {
            /* class org.junit.rules.ExternalResource.AnonymousClass1 */

            @Override // org.junit.runners.model.Statement
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

    /* access modifiers changed from: protected */
    public void before() throws Throwable {
    }

    /* access modifiers changed from: protected */
    public void after() {
    }
}
