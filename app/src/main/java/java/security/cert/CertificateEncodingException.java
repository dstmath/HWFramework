package java.security.cert;

public class CertificateEncodingException extends CertificateException {
    private static final long serialVersionUID = 6219492851589449162L;

    public CertificateEncodingException(String message) {
        super(message);
    }

    public CertificateEncodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CertificateEncodingException(Throwable cause) {
        super(cause);
    }
}
