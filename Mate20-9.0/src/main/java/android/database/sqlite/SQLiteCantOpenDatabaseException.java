package android.database.sqlite;

public class SQLiteCantOpenDatabaseException extends SQLiteException {
    public SQLiteCantOpenDatabaseException() {
    }

    public SQLiteCantOpenDatabaseException(String error) {
        super(error);
    }
}
