package java.security;

public class GeneralSecurityException extends Exception {
    private static final long serialVersionUID = 894798122053539237L;

    public GeneralSecurityException() {
    }

    public GeneralSecurityException(String msg) {
        super(msg);
    }

    public GeneralSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeneralSecurityException(Throwable cause) {
        super(cause);
    }
}
