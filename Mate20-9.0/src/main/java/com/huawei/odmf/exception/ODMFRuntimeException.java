package com.huawei.odmf.exception;

public class ODMFRuntimeException extends ODMFException {
    public ODMFRuntimeException() {
    }

    public ODMFRuntimeException(String message) {
        super(message);
    }

    public ODMFRuntimeException(Throwable cause) {
        super(cause);
    }

    public ODMFRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
