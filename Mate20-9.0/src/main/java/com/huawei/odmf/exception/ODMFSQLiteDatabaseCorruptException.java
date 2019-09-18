package com.huawei.odmf.exception;

public class ODMFSQLiteDatabaseCorruptException extends ODMFRuntimeException {
    public ODMFSQLiteDatabaseCorruptException() {
    }

    public ODMFSQLiteDatabaseCorruptException(String message) {
        super(message);
    }

    public ODMFSQLiteDatabaseCorruptException(Throwable cause) {
        super(cause);
    }

    public ODMFSQLiteDatabaseCorruptException(String message, Throwable cause) {
        super(message, cause);
    }
}
