package java.security;

public class KeyManagementException extends KeyException {
    private static final long serialVersionUID = 947674216157062695L;

    public KeyManagementException(String msg) {
        super(msg);
    }

    public KeyManagementException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyManagementException(Throwable cause) {
        super(cause);
    }
}
