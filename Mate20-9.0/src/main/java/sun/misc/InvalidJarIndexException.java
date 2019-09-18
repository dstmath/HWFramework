package sun.misc;

public class InvalidJarIndexException extends RuntimeException {
    static final long serialVersionUID = -6159797516569680148L;

    public InvalidJarIndexException() {
    }

    public InvalidJarIndexException(String s) {
        super(s);
    }
}
