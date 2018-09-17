package java.io;

public class InterruptedIOException extends IOException {
    private static final long serialVersionUID = 4020568460727500567L;
    public int bytesTransferred = 0;

    public InterruptedIOException(String s) {
        super(s);
    }

    public InterruptedIOException(Throwable cause) {
        super(cause);
    }

    public InterruptedIOException(String detailMessage, Throwable cause) {
        super(detailMessage, cause);
    }
}
