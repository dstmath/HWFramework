package com.huawei.zxing;

public abstract class ReaderException extends Exception {
    ReaderException() {
    }

    public final Throwable fillInStackTrace() {
        return null;
    }
}
