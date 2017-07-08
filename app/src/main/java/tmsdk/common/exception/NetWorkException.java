package tmsdk.common.exception;

/* compiled from: Unknown */
public class NetWorkException extends Exception {
    private int AR;

    public NetWorkException(int i, String str) {
        super(str);
        this.AR = i;
    }

    public NetWorkException(int i, String str, Throwable th) {
        super(str, th);
        this.AR = i;
    }

    public NetWorkException(int i, Throwable th) {
        super(th.getMessage(), th);
        this.AR = i;
    }

    public int getErrCode() {
        return this.AR;
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
