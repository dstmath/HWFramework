package android.accounts;

public class AuthenticatorException extends AccountsException {
    public AuthenticatorException(String message) {
        super(message);
    }

    public AuthenticatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticatorException(Throwable cause) {
        super(cause);
    }
}
