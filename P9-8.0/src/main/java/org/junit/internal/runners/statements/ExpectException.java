package org.junit.internal.runners.statements;

import org.junit.internal.AssumptionViolatedException;
import org.junit.runners.model.Statement;

public class ExpectException extends Statement {
    private final Class<? extends Throwable> expected;
    private final Statement next;

    public ExpectException(Statement next, Class<? extends Throwable> expected) {
        this.next = next;
        this.expected = expected;
    }

    public void evaluate() throws Exception {
        boolean complete = false;
        try {
            this.next.evaluate();
            complete = true;
        } catch (AssumptionViolatedException e) {
            throw e;
        } catch (Throwable e2) {
            if (!this.expected.isAssignableFrom(e2.getClass())) {
                Exception exception = new Exception("Unexpected exception, expected<" + this.expected.getName() + "> but was<" + e2.getClass().getName() + ">", e2);
            }
        }
        if (complete) {
            throw new AssertionError("Expected exception: " + this.expected.getName());
        }
    }
}
