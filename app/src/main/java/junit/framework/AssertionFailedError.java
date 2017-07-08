package junit.framework;

public class AssertionFailedError extends AssertionError {
    private static final long serialVersionUID = 1;

    public AssertionFailedError(String message) {
        super(defaultString(message));
    }

    private static String defaultString(String message) {
        return message == null ? "" : message;
    }
}
