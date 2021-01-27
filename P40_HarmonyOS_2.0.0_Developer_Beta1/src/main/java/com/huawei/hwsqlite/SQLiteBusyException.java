package com.huawei.hwsqlite;

public class SQLiteBusyException extends SQLiteException {
    public SQLiteBusyException() {
    }

    public SQLiteBusyException(String message) {
        super(message);
    }
}
