package android.security;

public class KeyChainException extends Exception {
    public KeyChainException(String detailMessage) {
        super(detailMessage);
    }

    public KeyChainException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyChainException(Throwable cause) {
        String str = null;
        if (cause != null) {
            str = cause.toString();
        }
        super(str, cause);
    }
}
