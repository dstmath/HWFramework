package org.junit.internal.runners.statements;

import java.util.List;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class RunBefores extends Statement {
    private final List<FrameworkMethod> befores;
    private final Statement next;
    private final Object target;

    public RunBefores(Statement next, List<FrameworkMethod> befores, Object target) {
        this.next = next;
        this.befores = befores;
        this.target = target;
    }

    public void evaluate() throws Throwable {
        for (FrameworkMethod before : this.befores) {
            before.invokeExplosively(this.target, new Object[0]);
        }
        this.next.evaluate();
    }
}
