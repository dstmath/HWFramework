package android.icu.util;

public class ICUUncheckedIOException extends RuntimeException {
    private static final long serialVersionUID = 1210263498513384449L;

    public ICUUncheckedIOException(String message) {
        super(message);
    }

    public ICUUncheckedIOException(Throwable cause) {
        super(cause);
    }

    public ICUUncheckedIOException(String message, Throwable cause) {
        super(message, cause);
    }
}
