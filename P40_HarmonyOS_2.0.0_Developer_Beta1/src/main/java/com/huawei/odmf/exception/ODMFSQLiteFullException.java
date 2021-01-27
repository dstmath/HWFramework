package com.huawei.odmf.exception;

public class ODMFSQLiteFullException extends ODMFRuntimeException {
    public ODMFSQLiteFullException() {
    }

    public ODMFSQLiteFullException(String str) {
        super(str);
    }

    public ODMFSQLiteFullException(Throwable th) {
        super(th);
    }

    public ODMFSQLiteFullException(String str, Throwable th) {
        super(str, th);
    }
}
