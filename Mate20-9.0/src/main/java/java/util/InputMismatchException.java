package java.util;

public class InputMismatchException extends NoSuchElementException {
    private static final long serialVersionUID = 8811230760997066428L;

    public InputMismatchException() {
    }

    public InputMismatchException(String s) {
        super(s);
    }
}
