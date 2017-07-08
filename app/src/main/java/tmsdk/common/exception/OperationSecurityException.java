package tmsdk.common.exception;

/* compiled from: Unknown */
public class OperationSecurityException extends Exception {
    public OperationSecurityException(String str) {
        super(str);
    }

    public OperationSecurityException(Throwable th) {
        super(th == null ? "" : th.getMessage(), th);
    }
}
