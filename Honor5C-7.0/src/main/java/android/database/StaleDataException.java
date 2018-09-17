package android.database;

public class StaleDataException extends RuntimeException {
    public StaleDataException(String description) {
        super(description);
    }
}
