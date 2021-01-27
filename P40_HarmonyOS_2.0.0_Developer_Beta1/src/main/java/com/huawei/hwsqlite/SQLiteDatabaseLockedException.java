package com.huawei.hwsqlite;

public class SQLiteDatabaseLockedException extends SQLiteException {
    public SQLiteDatabaseLockedException() {
    }

    public SQLiteDatabaseLockedException(String error) {
        super(error);
    }
}
