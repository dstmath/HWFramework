package com.huawei.odmf.exception;

public class ODMFSQLiteAccessPermException extends ODMFRuntimeException {
    public ODMFSQLiteAccessPermException() {
    }

    public ODMFSQLiteAccessPermException(String message) {
        super(message);
    }

    public ODMFSQLiteAccessPermException(Throwable cause) {
        super(cause);
    }

    public ODMFSQLiteAccessPermException(String message, Throwable cause) {
        super(message, cause);
    }
}
