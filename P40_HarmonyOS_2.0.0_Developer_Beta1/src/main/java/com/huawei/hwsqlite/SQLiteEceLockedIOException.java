package com.huawei.hwsqlite;

public class SQLiteEceLockedIOException extends SQLiteException {
    public SQLiteEceLockedIOException() {
    }

    public SQLiteEceLockedIOException(String error) {
        super(error);
    }
}
