package android.database;

public class SQLException extends RuntimeException {
    public SQLException(String error) {
        super(error);
    }

    public SQLException(String error, Throwable cause) {
        super(error, cause);
    }
}
