package java.io;

public class InterruptedIOException extends IOException {
    private static final long serialVersionUID = 4020568460727500567L;
    public int bytesTransferred;

    public InterruptedIOException() {
        this.bytesTransferred = 0;
    }

    public InterruptedIOException(String s) {
        super(s);
        this.bytesTransferred = 0;
    }

    public InterruptedIOException(Throwable cause) {
        super(cause);
        this.bytesTransferred = 0;
    }

    public InterruptedIOException(String detailMessage, Throwable cause) {
        super(detailMessage, cause);
        this.bytesTransferred = 0;
    }
}
