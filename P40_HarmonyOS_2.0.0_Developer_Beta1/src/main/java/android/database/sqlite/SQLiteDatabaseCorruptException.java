package android.database.sqlite;

public class SQLiteDatabaseCorruptException extends SQLiteException {
    public SQLiteDatabaseCorruptException() {
    }

    public SQLiteDatabaseCorruptException(String error) {
        super(error);
    }

    public static boolean isCorruptException(Throwable th) {
        while (th != null) {
            if (th instanceof SQLiteDatabaseCorruptException) {
                return true;
            }
            th = th.getCause();
        }
        return false;
    }
}
