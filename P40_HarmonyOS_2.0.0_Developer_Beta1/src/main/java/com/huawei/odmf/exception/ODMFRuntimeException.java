package com.huawei.odmf.exception;

public class ODMFRuntimeException extends ODMFException {
    public ODMFRuntimeException() {
    }

    public ODMFRuntimeException(String str) {
        super(str);
    }

    public ODMFRuntimeException(Throwable th) {
        super(th);
    }

    public ODMFRuntimeException(String str, Throwable th) {
        super(str, th);
    }
}
