package com.huawei.odmf.exception;

public class ODMFRelatedObjectNotFoundException extends ODMFException {
    public ODMFRelatedObjectNotFoundException() {
    }

    public ODMFRelatedObjectNotFoundException(String str) {
        super(str);
    }

    public ODMFRelatedObjectNotFoundException(Throwable th) {
        super(th);
    }
}
