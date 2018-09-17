package tmsdk.common.exception;

public class NetWorkException extends Exception {
    private int yA;

    public NetWorkException(int i, String str) {
        super(str);
        this.yA = i;
    }

    public NetWorkException(int i, String str, Throwable th) {
        super(str, th);
        this.yA = i;
    }

    public NetWorkException(int i, Throwable th) {
        super(th.getMessage(), th);
        this.yA = i;
    }

    public int getErrCode() {
        return this.yA;
    }

    public String getErrMsg() {
        String message = getMessage();
        if (message == null) {
            Throwable cause = getCause();
            if (cause != null) {
                message = cause.getMessage();
            }
        }
        return message == null ? "" : message;
    }
}
