package java.lang;

public class BootstrapMethodError extends LinkageError {
    private static final long serialVersionUID = 292;

    public BootstrapMethodError(String s) {
        super(s);
    }

    public BootstrapMethodError(String s, Throwable cause) {
        super(s, cause);
    }

    public BootstrapMethodError(Throwable cause) {
        String str = null;
        if (cause != null) {
            str = cause.toString();
        }
        super(str);
        initCause(cause);
    }
}
