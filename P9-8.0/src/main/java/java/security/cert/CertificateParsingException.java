package java.security.cert;

public class CertificateParsingException extends CertificateException {
    private static final long serialVersionUID = -7989222416793322029L;

    public CertificateParsingException(String message) {
        super(message);
    }

    public CertificateParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CertificateParsingException(Throwable cause) {
        super(cause);
    }
}
