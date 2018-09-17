package javax.sip;

public class TransportNotSupportedException extends SipException {
    public TransportNotSupportedException(String message) {
        super(message);
    }

    public TransportNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
