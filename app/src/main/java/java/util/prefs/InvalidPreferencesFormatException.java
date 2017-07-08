package java.util.prefs;

public class InvalidPreferencesFormatException extends Exception {
    private static final long serialVersionUID = -791715184232119669L;

    public InvalidPreferencesFormatException(Throwable cause) {
        super(cause);
    }

    public InvalidPreferencesFormatException(String message) {
        super(message);
    }

    public InvalidPreferencesFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
