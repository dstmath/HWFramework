package javax.net.ssl;

public class SSLHandshakeException extends SSLException {
    private static final long serialVersionUID = -5045881315018326890L;

    public SSLHandshakeException(String reason) {
        super(reason);
    }
}
