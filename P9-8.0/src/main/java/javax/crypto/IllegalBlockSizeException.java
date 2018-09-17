package javax.crypto;

import java.security.GeneralSecurityException;

public class IllegalBlockSizeException extends GeneralSecurityException {
    private static final long serialVersionUID = -1965144811953540392L;

    public IllegalBlockSizeException(String msg) {
        super(msg);
    }
}
