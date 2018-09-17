package java.lang;

public class AssertionError extends Error {
    private static final long serialVersionUID = -5013299493970297370L;

    private AssertionError(String detailMessage) {
        super(detailMessage);
    }

    public AssertionError(Object detailMessage) {
        this(String.valueOf(detailMessage));
        if (detailMessage instanceof Throwable) {
            initCause((Throwable) detailMessage);
        }
    }

    public AssertionError(boolean detailMessage) {
        this(String.valueOf(detailMessage));
    }

    public AssertionError(char detailMessage) {
        this(String.valueOf(detailMessage));
    }

    public AssertionError(int detailMessage) {
        this(String.valueOf(detailMessage));
    }

    public AssertionError(long detailMessage) {
        this(String.valueOf(detailMessage));
    }

    public AssertionError(float detailMessage) {
        this(String.valueOf(detailMessage));
    }

    public AssertionError(double detailMessage) {
        this(String.valueOf(detailMessage));
    }

    public AssertionError(String message, Throwable cause) {
        super(message, cause);
    }
}
