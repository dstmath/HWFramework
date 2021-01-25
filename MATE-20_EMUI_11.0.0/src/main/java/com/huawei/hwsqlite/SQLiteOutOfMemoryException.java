package com.huawei.hwsqlite;

public class SQLiteOutOfMemoryException extends SQLiteException {
    public SQLiteOutOfMemoryException() {
    }

    public SQLiteOutOfMemoryException(String error) {
        super(error);
    }
}
