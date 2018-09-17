package org.junit.internal.runners.statements;

import org.junit.runners.model.Statement;

public class Fail extends Statement {
    private final Throwable error;

    public Fail(Throwable e) {
        this.error = e;
    }

    public void evaluate() throws Throwable {
        throw this.error;
    }
}
