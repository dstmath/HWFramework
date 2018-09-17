package android.database.sqlite;

public class SQLiteDatabaseLockedException extends SQLiteException {
    public SQLiteDatabaseLockedException(String error) {
        super(error);
    }
}
