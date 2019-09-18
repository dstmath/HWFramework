package java.lang.reflect;

public class InvocationTargetException extends ReflectiveOperationException {
    private static final long serialVersionUID = 4085088731926701167L;
    private Throwable target;

    protected InvocationTargetException() {
        super((Throwable) null);
    }

    public InvocationTargetException(Throwable target2) {
        super((Throwable) null);
        this.target = target2;
    }

    public InvocationTargetException(Throwable target2, String s) {
        super(s, null);
        this.target = target2;
    }

    public Throwable getTargetException() {
        return this.target;
    }

    public Throwable getCause() {
        return this.target;
    }
}
