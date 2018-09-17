package android.accounts;

public class OperationCanceledException extends AccountsException {
    public OperationCanceledException(String message) {
        super(message);
    }

    public OperationCanceledException(String message, Throwable cause) {
        super(message, cause);
    }

    public OperationCanceledException(Throwable cause) {
        super(cause);
    }
}
