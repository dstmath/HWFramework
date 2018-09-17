package java.io;

public class IOError extends Error {
    private static final long serialVersionUID = 67100927991680413L;

    public IOError(Throwable cause) {
        super(cause);
    }
}
