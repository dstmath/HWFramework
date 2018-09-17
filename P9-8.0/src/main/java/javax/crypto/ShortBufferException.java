package javax.crypto;

import java.security.GeneralSecurityException;

public class ShortBufferException extends GeneralSecurityException {
    private static final long serialVersionUID = 8427718640832943747L;

    public ShortBufferException(String msg) {
        super(msg);
    }
}
