package org.junit.internal.runners.statements;

import java.util.ArrayList;
import java.util.List;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

public class RunAfters extends Statement {
    private final List<FrameworkMethod> afters;
    private final Statement next;
    private final Object target;

    public RunAfters(Statement next, List<FrameworkMethod> afters, Object target) {
        this.next = next;
        this.afters = afters;
        this.target = target;
    }

    public void evaluate() throws Throwable {
        List<Throwable> errors = new ArrayList();
        try {
            this.next.evaluate();
            for (FrameworkMethod each : this.afters) {
                try {
                    each.invokeExplosively(this.target, new Object[0]);
                } catch (Throwable e) {
                    errors.add(e);
                }
            }
        } catch (Throwable e2) {
            errors.add(e2);
        }
        MultipleFailureException.assertEmpty(errors);
    }
}
