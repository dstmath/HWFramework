package ohos.media.camera.exception;

public class ConnectException extends ServiceException {
    private static final long serialVersionUID = -1795300957471254971L;
    private int errorCode;

    public ConnectException() {
    }

    public ConnectException(String str) {
        super(str);
    }

    public ConnectException(int i) {
        this.errorCode = i;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
