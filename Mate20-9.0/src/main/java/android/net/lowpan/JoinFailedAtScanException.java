package android.net.lowpan;

public class JoinFailedAtScanException extends JoinFailedException {
    public JoinFailedAtScanException() {
    }

    public JoinFailedAtScanException(String message) {
        super(message);
    }

    public JoinFailedAtScanException(String message, Throwable cause) {
        super(message, cause);
    }

    public JoinFailedAtScanException(Exception cause) {
        super(cause);
    }
}
