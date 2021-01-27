package com.huawei.hwsqlite;

import android.database.SQLException;

public class SQLiteException extends SQLException {
    public SQLiteException() {
    }

    public SQLiteException(String error) {
        super(error);
    }

    public SQLiteException(String error, Throwable cause) {
        super(error, cause);
    }
}
