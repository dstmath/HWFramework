package android.database.sqlite;

public class SQLiteDatatypeMismatchException extends SQLiteException {
    public SQLiteDatatypeMismatchException() {
    }

    public SQLiteDatatypeMismatchException(String error) {
        super(error);
    }
}
