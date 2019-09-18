package android.database.sqlite;

public class SQLiteOutOfMemoryException extends SQLiteException {
    public SQLiteOutOfMemoryException() {
    }

    public SQLiteOutOfMemoryException(String error) {
        super(error);
    }
}
