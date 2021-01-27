package android.security.keystore;

import java.security.InvalidKeyException;

public class UserNotAuthenticatedException extends InvalidKeyException {
    public UserNotAuthenticatedException() {
        super("User not authenticated");
    }

    public UserNotAuthenticatedException(String message) {
        super(message);
    }

    public UserNotAuthenticatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
