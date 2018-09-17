package javax.crypto;

import java.security.GeneralSecurityException;

public class NoSuchPaddingException extends GeneralSecurityException {
    private static final long serialVersionUID = -4572885201200175466L;

    public NoSuchPaddingException(String msg) {
        super(msg);
    }
}
