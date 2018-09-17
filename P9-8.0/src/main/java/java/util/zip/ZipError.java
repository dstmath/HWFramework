package java.util.zip;

public class ZipError extends InternalError {
    private static final long serialVersionUID = 853973422266861979L;

    public ZipError(String s) {
        super(s);
    }
}
