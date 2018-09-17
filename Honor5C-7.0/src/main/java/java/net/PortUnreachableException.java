package java.net;

public class PortUnreachableException extends SocketException {
    private static final long serialVersionUID = 8462541992376507323L;

    public PortUnreachableException(String msg) {
        super(msg);
    }

    public PortUnreachableException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
