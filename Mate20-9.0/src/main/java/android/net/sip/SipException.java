package android.net.sip;

public class SipException extends Exception {
    public SipException() {
    }

    public SipException(String message) {
        super(message);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    public SipException(String message, Throwable cause) {
        super(message, r0);
        Throwable th;
        if (!(cause instanceof javax.sip.SipException) || cause.getCause() == null) {
            th = cause;
        } else {
            th = cause.getCause();
        }
    }
}
