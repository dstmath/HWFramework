package java.lang;

public class NoClassDefFoundError extends LinkageError {
    private static final long serialVersionUID = 9095859863287012458L;

    public NoClassDefFoundError(String s) {
        super(s);
    }

    private NoClassDefFoundError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
