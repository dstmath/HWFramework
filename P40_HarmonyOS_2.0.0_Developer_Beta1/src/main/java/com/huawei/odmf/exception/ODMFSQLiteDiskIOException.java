package com.huawei.odmf.exception;

public class ODMFSQLiteDiskIOException extends ODMFRuntimeException {
    public ODMFSQLiteDiskIOException() {
    }

    public ODMFSQLiteDiskIOException(String str) {
        super(str);
    }

    public ODMFSQLiteDiskIOException(Throwable th) {
        super(th);
    }

    public ODMFSQLiteDiskIOException(String str, Throwable th) {
        super(str, th);
    }
}
