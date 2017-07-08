package javax.net.ssl;

public class SSLKeyException extends SSLException {
    private static final long serialVersionUID = -8071664081941937874L;

    public SSLKeyException(String reason) {
        super(reason);
    }
}
