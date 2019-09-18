package com.huawei.odmf.exception;

public class ODMFRelatedObjectNotFoundException extends ODMFException {
    public ODMFRelatedObjectNotFoundException() {
    }

    public ODMFRelatedObjectNotFoundException(String message) {
        super(message);
    }

    public ODMFRelatedObjectNotFoundException(Throwable cause) {
        super(cause);
    }
}
