package com.huawei.hwsqlite;

public class SQLiteDoneException extends SQLiteException {
    public SQLiteDoneException() {
    }

    public SQLiteDoneException(String error) {
        super(error);
    }
}
