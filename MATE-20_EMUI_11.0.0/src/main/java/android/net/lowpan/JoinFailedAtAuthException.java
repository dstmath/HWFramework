package android.net.lowpan;

public class JoinFailedAtAuthException extends JoinFailedException {
    public JoinFailedAtAuthException() {
    }

    public JoinFailedAtAuthException(String message) {
        super(message);
    }

    public JoinFailedAtAuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public JoinFailedAtAuthException(Exception cause) {
        super(cause);
    }
}
