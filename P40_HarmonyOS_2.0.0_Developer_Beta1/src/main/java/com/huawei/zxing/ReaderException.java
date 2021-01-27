package com.huawei.zxing;

public abstract class ReaderException extends Exception {
    ReaderException() {
    }

    @Override // java.lang.Throwable
    public final Throwable fillInStackTrace() {
        return null;
    }
}
