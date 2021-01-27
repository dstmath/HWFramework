package ohos.data.rdb;

public class RdbException extends RuntimeException {
    private static final long serialVersionUID = 2294829081846562812L;

    public RdbException() {
    }

    public RdbException(String str) {
        super(str);
    }

    public RdbException(String str, Throwable th) {
        super(str, th);
    }
}
