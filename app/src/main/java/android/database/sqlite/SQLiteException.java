package android.database.sqlite;

import android.database.SQLException;

public class SQLiteException extends SQLException {
    public SQLiteException(String error) {
        super(error);
    }

    public SQLiteException(String error, Throwable cause) {
        super(error, cause);
    }
}
