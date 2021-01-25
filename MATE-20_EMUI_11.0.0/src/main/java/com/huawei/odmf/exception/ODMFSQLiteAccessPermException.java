package com.huawei.odmf.exception;

public class ODMFSQLiteAccessPermException extends ODMFRuntimeException {
    public ODMFSQLiteAccessPermException() {
    }

    public ODMFSQLiteAccessPermException(String str) {
        super(str);
    }

    public ODMFSQLiteAccessPermException(Throwable th) {
        super(th);
    }

    public ODMFSQLiteAccessPermException(String str, Throwable th) {
        super(str, th);
    }
}
