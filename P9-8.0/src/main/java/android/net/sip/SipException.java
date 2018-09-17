package android.net.sip;

public class SipException extends Exception {
    public SipException(String message) {
        super(message);
    }

    public SipException(String message, Throwable cause) {
        if ((cause instanceof javax.sip.SipException) && cause.getCause() != null) {
            cause = cause.getCause();
        }
        super(message, cause);
    }
}
