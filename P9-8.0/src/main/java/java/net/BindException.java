package java.net;

public class BindException extends SocketException {
    private static final long serialVersionUID = -5945005768251722951L;

    public BindException(String msg) {
        super(msg);
    }

    public BindException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
