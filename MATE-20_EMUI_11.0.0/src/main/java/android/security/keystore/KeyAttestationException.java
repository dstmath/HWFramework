package android.security.keystore;

public class KeyAttestationException extends Exception {
    public KeyAttestationException(String detailMessage) {
        super(detailMessage);
    }

    public KeyAttestationException(String message, Throwable cause) {
        super(message, cause);
    }
}
