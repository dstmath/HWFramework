package org.bouncycastle.jce.exception;

public interface ExtException {
    @Override // org.bouncycastle.jce.exception.ExtException
    Throwable getCause();
}
