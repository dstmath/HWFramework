package com.huawei.hwsqlite;

public class SQLiteFullException extends SQLiteException {
    public SQLiteFullException() {
    }

    public SQLiteFullException(String error) {
        super(error);
    }
}
