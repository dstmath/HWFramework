package java.lang;

public class BootstrapMethodError extends LinkageError {
    private static final long serialVersionUID = 292;

    public BootstrapMethodError() {
    }

    public BootstrapMethodError(String s) {
        super(s);
    }

    public BootstrapMethodError(String s, Throwable cause) {
        super(s, cause);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public BootstrapMethodError(Throwable cause) {
        super(cause == null ? null : cause.toString());
        initCause(cause);
    }
}
