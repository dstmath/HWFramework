package android.icu.impl;

public class InvalidFormatException extends Exception {
    static final long serialVersionUID = 8883328905089345791L;

    public InvalidFormatException(Throwable cause) {
        super(cause);
    }

    public InvalidFormatException(String message) {
        super(message);
    }
}
