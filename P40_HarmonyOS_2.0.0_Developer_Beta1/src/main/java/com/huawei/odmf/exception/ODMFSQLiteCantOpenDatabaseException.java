package com.huawei.odmf.exception;

public class ODMFSQLiteCantOpenDatabaseException extends ODMFRuntimeException {
    public ODMFSQLiteCantOpenDatabaseException() {
    }

    public ODMFSQLiteCantOpenDatabaseException(String str) {
        super(str);
    }

    public ODMFSQLiteCantOpenDatabaseException(Throwable th) {
        super(th);
    }

    public ODMFSQLiteCantOpenDatabaseException(String str, Throwable th) {
        super(str, th);
    }
}
