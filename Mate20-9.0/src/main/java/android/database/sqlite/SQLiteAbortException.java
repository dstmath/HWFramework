package android.database.sqlite;

public class SQLiteAbortException extends SQLiteException {
    public SQLiteAbortException() {
    }

    public SQLiteAbortException(String error) {
        super(error);
    }
}
