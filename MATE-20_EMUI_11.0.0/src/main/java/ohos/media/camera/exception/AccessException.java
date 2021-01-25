package ohos.media.camera.exception;

public class AccessException extends ConnectException {
    private static final long serialVersionUID = -37259206497828953L;

    public AccessException(String str) {
        super(str);
    }

    public AccessException(int i) {
        super(i);
    }
}
