package javax.net.ssl;

public class SSLProtocolException extends SSLException {
    private static final long serialVersionUID = 5445067063799134928L;

    public SSLProtocolException(String reason) {
        super(reason);
    }
}
