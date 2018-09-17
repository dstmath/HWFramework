package android.database.sqlite;

public class SQLiteOutOfMemoryException extends SQLiteException {
    public SQLiteOutOfMemoryException(String error) {
        super(error);
    }
}
