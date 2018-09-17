package tmsdk.common.exception;

public class WifiApproveException extends Exception {
    public WifiApproveException(String str) {
        super(str);
    }

    public WifiApproveException(String str, Throwable th) {
        super(str, th);
    }

    public WifiApproveException(Throwable th) {
        super(th.getMessage(), th);
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
