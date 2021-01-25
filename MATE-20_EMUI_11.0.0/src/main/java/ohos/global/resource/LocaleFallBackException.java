package ohos.global.resource;

public class LocaleFallBackException extends Exception {
    private static final long serialVersionUID = 5879927773416506302L;

    public LocaleFallBackException(String str, Throwable th) {
        super(str, th);
    }
}
