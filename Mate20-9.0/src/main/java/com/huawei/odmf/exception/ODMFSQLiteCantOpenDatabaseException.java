package com.huawei.odmf.exception;

public class ODMFSQLiteCantOpenDatabaseException extends ODMFRuntimeException {
    public ODMFSQLiteCantOpenDatabaseException() {
    }

    public ODMFSQLiteCantOpenDatabaseException(String message) {
        super(message);
    }

    public ODMFSQLiteCantOpenDatabaseException(Throwable cause) {
        super(cause);
    }

    public ODMFSQLiteCantOpenDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
