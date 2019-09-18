package com.huawei.odmf.exception;

public class ODMFIllegalStateException extends ODMFException {
    public ODMFIllegalStateException() {
    }

    public ODMFIllegalStateException(String message) {
        super(message);
    }

    public ODMFIllegalStateException(Throwable cause) {
        super(cause);
    }

    public ODMFIllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
