package javax.security.cert;

public class CertificateParsingException extends CertificateException {
    private static final long serialVersionUID = -8449352422951136229L;

    public CertificateParsingException() {
    }

    public CertificateParsingException(String message) {
        super(message);
    }
}
