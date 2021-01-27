package com.huawei.odmf.exception;

public class ODMFConcurrentModificationException extends ODMFException {
    public ODMFConcurrentModificationException() {
    }

    public ODMFConcurrentModificationException(String str) {
        super(str);
    }

    public ODMFConcurrentModificationException(Throwable th) {
        super(th);
    }
}
