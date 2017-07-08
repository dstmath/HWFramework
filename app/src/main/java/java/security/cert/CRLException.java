package java.security.cert;

import java.security.GeneralSecurityException;

public class CRLException extends GeneralSecurityException {
    private static final long serialVersionUID = -6694728944094197147L;

    public CRLException(String message) {
        super(message);
    }

    public CRLException(String message, Throwable cause) {
        super(message, cause);
    }

    public CRLException(Throwable cause) {
        super(cause);
    }
}
