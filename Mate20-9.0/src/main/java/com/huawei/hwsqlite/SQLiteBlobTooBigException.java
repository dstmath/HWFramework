package com.huawei.hwsqlite;

public class SQLiteBlobTooBigException extends SQLiteException {
    public SQLiteBlobTooBigException() {
    }

    public SQLiteBlobTooBigException(String error) {
        super(error);
    }
}
