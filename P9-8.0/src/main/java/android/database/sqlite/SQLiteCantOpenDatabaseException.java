package android.database.sqlite;

public class SQLiteCantOpenDatabaseException extends SQLiteException {
    public SQLiteCantOpenDatabaseException(String error) {
        super(error);
    }
}
