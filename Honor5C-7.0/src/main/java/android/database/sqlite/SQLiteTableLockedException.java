package android.database.sqlite;

public class SQLiteTableLockedException extends SQLiteException {
    public SQLiteTableLockedException(String error) {
        super(error);
    }
}
