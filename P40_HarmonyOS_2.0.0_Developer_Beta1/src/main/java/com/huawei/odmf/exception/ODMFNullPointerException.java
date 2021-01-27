package com.huawei.odmf.exception;

public class ODMFNullPointerException extends ODMFException {
    public ODMFNullPointerException() {
    }

    public ODMFNullPointerException(String str) {
        super(str);
    }

    public ODMFNullPointerException(Throwable th) {
        super(th);
    }

    public ODMFNullPointerException(String str, Throwable th) {
        super(str, th);
    }
}
