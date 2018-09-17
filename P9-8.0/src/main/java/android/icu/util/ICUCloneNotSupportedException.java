package android.icu.util;

public class ICUCloneNotSupportedException extends ICUException {
    private static final long serialVersionUID = -4824446458488194964L;

    public ICUCloneNotSupportedException(String message) {
        super(message);
    }

    public ICUCloneNotSupportedException(Throwable cause) {
        super(cause);
    }

    public ICUCloneNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
