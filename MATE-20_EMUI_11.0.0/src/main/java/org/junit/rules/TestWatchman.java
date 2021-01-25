package org.junit.rules;

import org.junit.internal.AssumptionViolatedException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

@Deprecated
public class TestWatchman implements MethodRule {
    @Override // org.junit.rules.MethodRule
    public Statement apply(final Statement base, final FrameworkMethod method, Object target) {
        return new Statement() {
            /* class org.junit.rules.TestWatchman.AnonymousClass1 */

            @Override // org.junit.runners.model.Statement
            public void evaluate() throws Throwable {
                TestWatchman.this.starting(method);
                try {
                    base.evaluate();
                    TestWatchman.this.succeeded(method);
                    TestWatchman.this.finished(method);
                } catch (AssumptionViolatedException e) {
                    throw e;
                } catch (Throwable e2) {
                    TestWatchman.this.finished(method);
                    throw e2;
                }
            }
        };
    }

    public void succeeded(FrameworkMethod method) {
    }

    public void failed(Throwable e, FrameworkMethod method) {
    }

    public void starting(FrameworkMethod method) {
    }

    public void finished(FrameworkMethod method) {
    }
}
