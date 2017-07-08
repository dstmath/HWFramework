package java.security.spec;

import java.security.GeneralSecurityException;

public class InvalidParameterSpecException extends GeneralSecurityException {
    private static final long serialVersionUID = -970468769593399342L;

    public InvalidParameterSpecException(String msg) {
        super(msg);
    }
}
