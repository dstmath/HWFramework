package org.bouncycastle.operator;

import java.io.IOException;

public class OperatorStreamException extends IOException {
    private Throwable cause;

    public OperatorStreamException(String str, Throwable th) {
        super(str);
        this.cause = th;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
