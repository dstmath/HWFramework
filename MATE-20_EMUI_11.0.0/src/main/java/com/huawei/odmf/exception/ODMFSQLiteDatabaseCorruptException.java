package com.huawei.odmf.exception;

public class ODMFSQLiteDatabaseCorruptException extends ODMFRuntimeException {
    public ODMFSQLiteDatabaseCorruptException() {
    }

    public ODMFSQLiteDatabaseCorruptException(String str) {
        super(str);
    }

    public ODMFSQLiteDatabaseCorruptException(Throwable th) {
        super(th);
    }

    public ODMFSQLiteDatabaseCorruptException(String str, Throwable th) {
        super(str, th);
    }
}
