package java.security;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

public abstract class AuthProvider extends Provider {
    public abstract void login(Subject subject, CallbackHandler callbackHandler) throws LoginException;

    public abstract void logout() throws LoginException;

    public abstract void setCallbackHandler(CallbackHandler callbackHandler);

    protected AuthProvider(String name, double version, String info) {
        super("", 0.0d, "");
    }
}
