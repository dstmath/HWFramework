package org.junit.rules;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public abstract class Verifier implements TestRule {
    @Override // org.junit.rules.TestRule
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            /* class org.junit.rules.Verifier.AnonymousClass1 */

            @Override // org.junit.runners.model.Statement
            public void evaluate() throws Throwable {
                base.evaluate();
                Verifier.this.verify();
            }
        };
    }

    /* access modifiers changed from: protected */
    public void verify() throws Throwable {
    }
}
