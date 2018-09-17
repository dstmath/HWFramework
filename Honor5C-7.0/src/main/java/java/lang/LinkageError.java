package java.lang;

public class LinkageError extends Error {
    private static final long serialVersionUID = 3579600108157160122L;

    public LinkageError(String s) {
        super(s);
    }

    public LinkageError(String s, Throwable cause) {
        super(s, cause);
    }
}
