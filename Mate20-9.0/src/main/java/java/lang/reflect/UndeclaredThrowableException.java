package java.lang.reflect;

public class UndeclaredThrowableException extends RuntimeException {
    static final long serialVersionUID = 330127114055056639L;
    private Throwable undeclaredThrowable;

    public UndeclaredThrowableException(Throwable undeclaredThrowable2) {
        super((Throwable) null);
        this.undeclaredThrowable = undeclaredThrowable2;
    }

    public UndeclaredThrowableException(Throwable undeclaredThrowable2, String s) {
        super(s, null);
        this.undeclaredThrowable = undeclaredThrowable2;
    }

    public Throwable getUndeclaredThrowable() {
        return this.undeclaredThrowable;
    }

    public Throwable getCause() {
        return this.undeclaredThrowable;
    }
}
