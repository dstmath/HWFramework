package com.huawei.hwsqlite;

public class SQLiteDatatypeMismatchException extends SQLiteException {
    public SQLiteDatatypeMismatchException() {
    }

    public SQLiteDatatypeMismatchException(String error) {
        super(error);
    }
}
