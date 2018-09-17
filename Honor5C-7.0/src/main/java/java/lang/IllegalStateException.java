package java.lang;

public class IllegalStateException extends RuntimeException {
    static final long serialVersionUID = -1848914673093119416L;

    public IllegalStateException(String s) {
        super(s);
    }

    public IllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalStateException(Throwable cause) {
        super(cause);
    }
}
