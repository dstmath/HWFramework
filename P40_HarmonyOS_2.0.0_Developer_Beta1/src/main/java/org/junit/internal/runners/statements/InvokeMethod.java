package org.junit.internal.runners.statements;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class InvokeMethod extends Statement {
    private final Object target;
    private final FrameworkMethod testMethod;

    public InvokeMethod(FrameworkMethod testMethod2, Object target2) {
        this.testMethod = testMethod2;
        this.target = target2;
    }

    @Override // org.junit.runners.model.Statement
    public void evaluate() throws Throwable {
        this.testMethod.invokeExplosively(this.target, new Object[0]);
    }
}
