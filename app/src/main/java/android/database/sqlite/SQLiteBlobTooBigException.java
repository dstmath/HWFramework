package android.database.sqlite;

public class SQLiteBlobTooBigException extends SQLiteException {
    public SQLiteBlobTooBigException(String error) {
        super(error);
    }
}
