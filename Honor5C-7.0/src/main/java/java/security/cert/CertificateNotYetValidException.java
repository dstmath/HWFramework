package java.security.cert;

public class CertificateNotYetValidException extends CertificateException {
    static final long serialVersionUID = 4355919900041064702L;

    public CertificateNotYetValidException(String message) {
        super(message);
    }
}
