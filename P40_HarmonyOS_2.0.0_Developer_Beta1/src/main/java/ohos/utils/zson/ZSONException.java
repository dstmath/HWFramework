package ohos.utils.zson;

public class ZSONException extends RuntimeException {
    private static final long serialVersionUID = 1968359624627646812L;

    public ZSONException(String str) {
        super(str);
    }

    public ZSONException(Exception exc) {
        super(exc);
    }
}
