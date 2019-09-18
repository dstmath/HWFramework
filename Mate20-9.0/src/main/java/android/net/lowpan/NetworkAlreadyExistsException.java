package android.net.lowpan;

public class NetworkAlreadyExistsException extends LowpanException {
    public NetworkAlreadyExistsException() {
    }

    public NetworkAlreadyExistsException(String message) {
        super(message, null);
    }

    public NetworkAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetworkAlreadyExistsException(Exception cause) {
        super(cause);
    }
}
