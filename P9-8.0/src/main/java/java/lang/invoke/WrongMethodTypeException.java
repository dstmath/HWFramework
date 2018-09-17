package java.lang.invoke;

public class WrongMethodTypeException extends RuntimeException {
    private static final long serialVersionUID = 292;

    public WrongMethodTypeException(String s) {
        super(s);
    }

    WrongMethodTypeException(String s, Throwable cause) {
        super(s, cause);
    }

    WrongMethodTypeException(Throwable cause) {
        super(cause);
    }
}
