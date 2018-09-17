package java.security;

public class KeyStoreException extends GeneralSecurityException {
    private static final long serialVersionUID = -1119353179322377262L;

    public KeyStoreException(String msg) {
        super(msg);
    }

    public KeyStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyStoreException(Throwable cause) {
        super(cause);
    }
}
