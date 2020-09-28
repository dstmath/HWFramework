package android.database.sqlite;

public class SQLiteFullException extends SQLiteException {
    public SQLiteFullException() {
    }

    public SQLiteFullException(String error) {
        super(error);
    }
}
