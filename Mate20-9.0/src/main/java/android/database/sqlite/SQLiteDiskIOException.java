package android.database.sqlite;

public class SQLiteDiskIOException extends SQLiteException {
    public SQLiteDiskIOException() {
    }

    public SQLiteDiskIOException(String error) {
        super(error);
    }
}
