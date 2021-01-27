package org.junit.internal.runners.statements;

import java.util.List;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class RunBefores extends Statement {
    private final List<FrameworkMethod> befores;
    private final Statement next;
    private final Object target;

    public RunBefores(Statement next2, List<FrameworkMethod> befores2, Object target2) {
        this.next = next2;
        this.befores = befores2;
        this.target = target2;
    }

    @Override // org.junit.runners.model.Statement
    public void evaluate() throws Throwable {
        for (FrameworkMethod before : this.befores) {
            before.invokeExplosively(this.target, new Object[0]);
        }
        this.next.evaluate();
    }
}
