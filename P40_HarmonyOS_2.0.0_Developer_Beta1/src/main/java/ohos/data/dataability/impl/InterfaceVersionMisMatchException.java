package ohos.data.dataability.impl;

public class InterfaceVersionMisMatchException extends RuntimeException {
    private static final long serialVersionUID = 5598059529073735825L;

    public InterfaceVersionMisMatchException() {
    }

    public InterfaceVersionMisMatchException(String str) {
        super(str);
    }

    public InterfaceVersionMisMatchException(String str, Throwable th) {
        super(str, th);
    }
}
