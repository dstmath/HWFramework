package java.lang;

public class SecurityException extends RuntimeException {
    private static final long serialVersionUID = 6878364983674394167L;

    public SecurityException(String s) {
        super(s);
    }

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecurityException(Throwable cause) {
        super(cause);
    }
}
