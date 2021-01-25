package android.net.lowpan;

public class JoinFailedException extends LowpanException {
    public JoinFailedException() {
    }

    public JoinFailedException(String message) {
        super(message);
    }

    public JoinFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    protected JoinFailedException(Exception cause) {
        super(cause);
    }
}
