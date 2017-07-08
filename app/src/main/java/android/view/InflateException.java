package android.view;

public class InflateException extends RuntimeException {
    public InflateException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public InflateException(String detailMessage) {
        super(detailMessage);
    }

    public InflateException(Throwable throwable) {
        super(throwable);
    }
}
