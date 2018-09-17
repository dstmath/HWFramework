package android.icu.util;

public class ICUException extends RuntimeException {
    private static final long serialVersionUID = -3067399656455755650L;

    public ICUException(String message) {
        super(message);
    }

    public ICUException(Throwable cause) {
        super(cause);
    }

    public ICUException(String message, Throwable cause) {
        super(message, cause);
    }
}
