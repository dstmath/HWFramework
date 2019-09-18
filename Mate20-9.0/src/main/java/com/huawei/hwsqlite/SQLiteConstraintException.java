package com.huawei.hwsqlite;

public class SQLiteConstraintException extends SQLiteException {
    public SQLiteConstraintException() {
    }

    public SQLiteConstraintException(String error) {
        super(error);
    }
}
