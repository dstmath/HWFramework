package com.huawei.odmf.exception;

public class ODMFException extends RuntimeException {
    public ODMFException() {
    }

    public ODMFException(String str) {
        super(str);
    }

    public ODMFException(Throwable th) {
        super(th);
    }

    public ODMFException(String str, Throwable th) {
        super(str, th);
    }
}
