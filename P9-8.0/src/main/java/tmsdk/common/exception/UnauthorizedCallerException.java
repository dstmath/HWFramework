package tmsdk.common.exception;

public class UnauthorizedCallerException extends SecurityException {
    public static final String DEFAULT_MESSAGE = "The caller is not permitted";

    public UnauthorizedCallerException() {
        this(DEFAULT_MESSAGE);
    }

    public UnauthorizedCallerException(String str) {
        super(str);
    }

    public UnauthorizedCallerException(String str, Throwable th) {
        super(str, th);
    }
}
