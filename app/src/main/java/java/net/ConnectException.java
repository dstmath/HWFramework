package java.net;

public class ConnectException extends SocketException {
    private static final long serialVersionUID = 3831404271622369215L;

    public ConnectException(String msg) {
        super(msg);
    }

    public ConnectException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
