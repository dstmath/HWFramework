package javax.security.auth.login;

import java.security.GeneralSecurityException;

public class LoginException extends GeneralSecurityException {
    private static final long serialVersionUID = -4679091624035232488L;

    public LoginException(String msg) {
        super(msg);
    }
}
