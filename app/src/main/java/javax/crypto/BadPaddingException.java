package javax.crypto;

import java.security.GeneralSecurityException;

public class BadPaddingException extends GeneralSecurityException {
    private static final long serialVersionUID = -5315033893984728443L;

    public BadPaddingException(String msg) {
        super(msg);
    }
}
