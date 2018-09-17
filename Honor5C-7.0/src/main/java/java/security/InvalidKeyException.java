package java.security;

public class InvalidKeyException extends KeyException {
    private static final long serialVersionUID = 5698479920593359816L;

    public InvalidKeyException(String msg) {
        super(msg);
    }

    public InvalidKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidKeyException(Throwable cause) {
        super(cause);
    }
}
