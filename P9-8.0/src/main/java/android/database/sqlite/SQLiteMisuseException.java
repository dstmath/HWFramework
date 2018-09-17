package android.database.sqlite;

public class SQLiteMisuseException extends SQLiteException {
    public SQLiteMisuseException(String error) {
        super(error);
    }
}
