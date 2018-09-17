package java.nio.charset;

public class CoderMalfunctionError extends Error {
    private static final long serialVersionUID = -1151412348057794301L;

    public CoderMalfunctionError(Exception cause) {
        super((Throwable) cause);
    }
}
