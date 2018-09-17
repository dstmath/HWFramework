package java.util.concurrent;

public class RejectedExecutionException extends RuntimeException {
    private static final long serialVersionUID = -375805702767069545L;

    public RejectedExecutionException(String message) {
        super(message);
    }

    public RejectedExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RejectedExecutionException(Throwable cause) {
        super(cause);
    }
}
