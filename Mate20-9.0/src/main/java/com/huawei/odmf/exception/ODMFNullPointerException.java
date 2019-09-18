package com.huawei.odmf.exception;

public class ODMFNullPointerException extends ODMFException {
    public ODMFNullPointerException() {
    }

    public ODMFNullPointerException(String message) {
        super(message);
    }

    public ODMFNullPointerException(Throwable cause) {
        super(cause);
    }

    public ODMFNullPointerException(String message, Throwable cause) {
        super(message, cause);
    }
}
