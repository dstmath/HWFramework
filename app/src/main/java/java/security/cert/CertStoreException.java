package java.security.cert;

import java.security.GeneralSecurityException;

public class CertStoreException extends GeneralSecurityException {
    private static final long serialVersionUID = 2395296107471573245L;

    public CertStoreException(String msg) {
        super(msg);
    }

    public CertStoreException(Throwable cause) {
        super(cause);
    }

    public CertStoreException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
