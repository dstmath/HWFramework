package android.nfc;

public class FormatException extends Exception {
    public FormatException(String message) {
        super(message);
    }

    public FormatException(String message, Throwable e) {
        super(message, e);
    }
}
