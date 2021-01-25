package com.huawei.hwsqlite;

public class SQLiteAbortException extends SQLiteException {
    public SQLiteAbortException() {
    }

    public SQLiteAbortException(String error) {
        super(error);
    }
}
