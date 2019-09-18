package com.huawei.odmf.exception;

public class ODMFSQLiteDiskIOException extends ODMFRuntimeException {
    public ODMFSQLiteDiskIOException() {
    }

    public ODMFSQLiteDiskIOException(String message) {
        super(message);
    }

    public ODMFSQLiteDiskIOException(Throwable cause) {
        super(cause);
    }

    public ODMFSQLiteDiskIOException(String message, Throwable cause) {
        super(message, cause);
    }
}
