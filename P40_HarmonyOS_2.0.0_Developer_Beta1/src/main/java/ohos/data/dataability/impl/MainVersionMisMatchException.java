package ohos.data.dataability.impl;

public class MainVersionMisMatchException extends RuntimeException {
    private static final long serialVersionUID = 406284844862429904L;

    public MainVersionMisMatchException() {
    }

    public MainVersionMisMatchException(String str) {
        super(str);
    }

    public MainVersionMisMatchException(String str, Throwable th) {
        super(str, th);
    }
}
