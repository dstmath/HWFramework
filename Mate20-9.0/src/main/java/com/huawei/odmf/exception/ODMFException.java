package com.huawei.odmf.exception;

public class ODMFException extends RuntimeException {
    public ODMFException() {
    }

    public ODMFException(String message) {
        super(message);
    }

    public ODMFException(Throwable cause) {
        super(cause);
    }

    public ODMFException(String message, Throwable cause) {
        super(message, cause);
    }
}
