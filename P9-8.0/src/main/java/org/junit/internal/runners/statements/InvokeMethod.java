package org.junit.internal.runners.statements;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class InvokeMethod extends Statement {
    private final Object target;
    private final FrameworkMethod testMethod;

    public InvokeMethod(FrameworkMethod testMethod, Object target) {
        this.testMethod = testMethod;
        this.target = target;
    }

    public void evaluate() throws Throwable {
        this.testMethod.invokeExplosively(this.target, new Object[0]);
    }
}
