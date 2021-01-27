package com.huawei.odmf.exception;

public class ODMFIllegalStateException extends ODMFException {
    public ODMFIllegalStateException() {
    }

    public ODMFIllegalStateException(String str) {
        super(str);
    }

    public ODMFIllegalStateException(Throwable th) {
        super(th);
    }

    public ODMFIllegalStateException(String str, Throwable th) {
        super(str, th);
    }
}
