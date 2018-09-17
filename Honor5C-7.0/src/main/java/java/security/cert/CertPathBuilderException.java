package java.security.cert;

import java.security.GeneralSecurityException;

public class CertPathBuilderException extends GeneralSecurityException {
    private static final long serialVersionUID = 5316471420178794402L;

    public CertPathBuilderException(String msg) {
        super(msg);
    }

    public CertPathBuilderException(Throwable cause) {
        super(cause);
    }

    public CertPathBuilderException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
