package java.time;

public class DateTimeException extends RuntimeException {
    private static final long serialVersionUID = -1632418723876261839L;

    public DateTimeException(String message) {
        super(message);
    }

    public DateTimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
