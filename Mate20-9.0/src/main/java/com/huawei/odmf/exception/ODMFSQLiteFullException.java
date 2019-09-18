package com.huawei.odmf.exception;

public class ODMFSQLiteFullException extends ODMFRuntimeException {
    public ODMFSQLiteFullException() {
    }

    public ODMFSQLiteFullException(String message) {
        super(message);
    }

    public ODMFSQLiteFullException(Throwable cause) {
        super(cause);
    }

    public ODMFSQLiteFullException(String message, Throwable cause) {
        super(message, cause);
    }
}
