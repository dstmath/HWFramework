package android.database.sqlite;

public class SQLiteReadOnlyDatabaseException extends SQLiteException {
    public SQLiteReadOnlyDatabaseException(String error) {
        super(error);
    }
}
