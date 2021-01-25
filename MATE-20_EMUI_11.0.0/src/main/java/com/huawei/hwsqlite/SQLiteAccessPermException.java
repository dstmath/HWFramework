package com.huawei.hwsqlite;

public class SQLiteAccessPermException extends SQLiteException {
    public SQLiteAccessPermException() {
    }

    public SQLiteAccessPermException(String error) {
        super(error);
    }
}
