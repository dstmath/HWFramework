package ohos.system;

public final class OsHelperErrnoException extends Exception {
    private static final long serialVersionUID = 3446168872119564630L;
    private final int errnoValue;
    private final String exceptionMessage;

    public OsHelperErrnoException(int i, String str) {
        this.errnoValue = i;
        this.exceptionMessage = str;
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        return this.exceptionMessage;
    }

    public int getErrnoValue() {
        return this.errnoValue;
    }
}
