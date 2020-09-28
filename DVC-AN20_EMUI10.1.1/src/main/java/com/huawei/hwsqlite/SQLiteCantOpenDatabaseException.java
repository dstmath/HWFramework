package com.huawei.hwsqlite;

public class SQLiteCantOpenDatabaseException extends SQLiteException {
    public SQLiteCantOpenDatabaseException() {
    }

    public SQLiteCantOpenDatabaseException(String error) {
        super(error);
    }
}
