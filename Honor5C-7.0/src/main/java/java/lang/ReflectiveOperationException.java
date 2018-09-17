package java.lang;

public class ReflectiveOperationException extends Exception {
    static final long serialVersionUID = 123456789;

    public ReflectiveOperationException(String message) {
        super(message);
    }

    public ReflectiveOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReflectiveOperationException(Throwable cause) {
        super(cause);
    }
}
