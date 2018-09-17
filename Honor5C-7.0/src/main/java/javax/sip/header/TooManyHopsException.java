package javax.sip.header;

public class TooManyHopsException extends Exception {
    public TooManyHopsException(String message) {
        super(message);
    }

    public TooManyHopsException(String message, Throwable cause) {
        super(message, cause);
    }
}
