package android.net.lowpan;

public class OperationCanceledException extends LowpanException {
    public OperationCanceledException() {
    }

    public OperationCanceledException(String message) {
        super(message);
    }

    public OperationCanceledException(String message, Throwable cause) {
        super(message, cause);
    }

    protected OperationCanceledException(Exception cause) {
        super(cause);
    }
}
