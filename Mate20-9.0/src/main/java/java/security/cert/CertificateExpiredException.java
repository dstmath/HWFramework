package java.security.cert;

public class CertificateExpiredException extends CertificateException {
    private static final long serialVersionUID = 9071001339691533771L;

    public CertificateExpiredException() {
    }

    public CertificateExpiredException(String message) {
        super(message);
    }
}
