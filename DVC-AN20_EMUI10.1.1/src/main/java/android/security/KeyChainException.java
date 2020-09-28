package android.security;

public class KeyChainException extends Exception {
    public KeyChainException() {
    }

    public KeyChainException(String detailMessage) {
        super(detailMessage);
    }

    public KeyChainException(String message, Throwable cause) {
        super(message, cause);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public KeyChainException(Throwable cause) {
        super(cause == null ? null : cause.toString(), cause);
    }
}
