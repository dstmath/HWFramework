package org.junit.rules;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public abstract class Verifier implements TestRule {
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            public void evaluate() throws Throwable {
                base.evaluate();
                Verifier.this.verify();
            }
        };
    }

    protected void verify() throws Throwable {
    }
}
