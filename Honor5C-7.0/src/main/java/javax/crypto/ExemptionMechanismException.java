package javax.crypto;

import java.security.GeneralSecurityException;

public class ExemptionMechanismException extends GeneralSecurityException {
    private static final long serialVersionUID = 1572699429277957109L;

    public ExemptionMechanismException(String msg) {
        super(msg);
    }
}
